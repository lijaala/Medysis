package Medysis.Project.Service;

import Medysis.Project.DTO.AppointmentDTO;
import Medysis.Project.Model.Appointment;
import Medysis.Project.Model.Staff;
import Medysis.Project.Model.User;
import Medysis.Project.Repository.AppointmentRepository;
import Medysis.Project.Repository.StaffRepository;
import Medysis.Project.Repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class AppointmentService {

    @Autowired
    private AppointmentRepository appointmentRepository;
    @Autowired
    private StaffRepository staffRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private NotificationService notificationService;

    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private final DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("hh:mm a");
    private final ZoneId nepalTimeZone = ZoneId.of("Asia/Kathmandu");

    public Appointment bookAppointment(Integer patientID, String doctorID, LocalDate appDate, LocalTime appTime) {
        User patient = userRepository.findById(patientID)
                .orElseThrow(() -> new RuntimeException("Patient not found"));
        Staff doctor = staffRepository.findById(doctorID)
                .orElseThrow(() -> new RuntimeException("Doctor not found"));

        Appointment appointment = new Appointment();
        appointment.setPatientID(patient);
        appointment.setDoctorID(doctor);
        appointment.setAppDate(appDate);
        appointment.setAppTime(appTime);
        appointment.setStatus("Pending");

        Appointment savedAppointment = appointmentRepository.save(appointment);
        // Create notification for the patient


        String patientNotificationMessage = String.format("Appointment for %s at %s with Dr. %s has been booked. Please arrive 5 minutes before the appointment time.",
                savedAppointment.getAppDate().format(dateFormatter),
                savedAppointment.getAppTime().format(timeFormatter),
                doctor.getStaffName());
        notificationService.createUserNotifications(patientID, patientNotificationMessage, "appointment");

        // Create notification for the doctor
        String doctorNotificationMessage = String.format("New appointment booked for patient %s on %s at %s.",
                patient.getName(),
                savedAppointment.getAppDate().format(dateFormatter),
                savedAppointment.getAppTime().format(timeFormatter));
        notificationService.createStaffNotifications(doctorID, doctorNotificationMessage, "appointment");



        // Save appointment to database
        return savedAppointment;
    }
    public Appointment setFollowUpAppointment(Integer appointmentID, LocalDate followUpDate) {
        Appointment appointment = appointmentRepository.findById(appointmentID)
                .orElseThrow(() -> new RuntimeException("Appointment not found"));

        appointment.setFollowUpDate(followUpDate);
        Appointment updatedAppointment = appointmentRepository.save(appointment);

        // Send immediate follow-up notification
        sendFollowUpNotification(updatedAppointment);

        sendFollowUpReminderNotification(updatedAppointment); // Schedule/send reminder

        return updatedAppointment;
    }
    private void sendFollowUpNotification(Appointment appointment) {
        User patient = userRepository.findById(appointment.getPatientID().getUserID())
                .orElse(null);

        if (patient != null && appointment.getFollowUpDate() != null) {
            String formattedDate = appointment.getFollowUpDate().format(dateFormatter);
            String message = String.format("A follow-up appointment has been scheduled for %s.", formattedDate);
            notificationService.createUserNotifications(patient.getUserID(), message, "appointment_followup");
        } else {
            System.err.println("Could not send initial follow-up notification for Appointment ID: " + appointment.getAppointmentID() + ". Patient or follow-up date is missing.");
        }
    }

    private void sendFollowUpReminderNotification(Appointment appointment) {
        User patient = userRepository.findById(appointment.getPatientID().getUserID())
                .orElse(null);

        if (patient != null && appointment.getFollowUpDate() != null) {
            LocalDate reminderDate = appointment.getFollowUpDate().minusWeeks(1);
            LocalDate nowInNepal = LocalDate.now(nepalTimeZone); // Explicitly use Nepal Time Zone

            if (reminderDate.isEqual(nowInNepal)) {
                String formattedDate = appointment.getFollowUpDate().format(dateFormatter);
                String message = String.format("Reminder: Your follow-up appointment is scheduled for %s next week.", formattedDate);
                notificationService.createUserNotifications(patient.getUserID(), message, "appointment_followup_reminder");
            }
        } else {
            System.err.println("Could not send follow-up reminder notification for Appointment ID: " + appointment.getAppointmentID() + ". Patient or follow-up date is missing.");
        }
    }

    @Scheduled(cron = "0 45 8 * * *") // Run every day at 8:00 AM (server's time)
    public void checkAndSendFollowUpReminders() {
        LocalDate todayInNepal = LocalDate.now(nepalTimeZone); // Explicitly use Nepal Time Zone
        LocalDate reminderDate = todayInNepal.plusWeeks(1);
        System.out.println("Checking for follow-up reminders for " + reminderDate + " at " + java.time.LocalTime.now(nepalTimeZone) + " (Nepal Time)");

        List<Appointment> appointmentsForReminder = appointmentRepository.findByFollowUpDate(reminderDate);

        for (Appointment appointment : appointmentsForReminder) {
            sendFollowUpReminderNotification(appointment);
        }

        System.out.println("Checked for follow-up reminders for " + reminderDate + " at " + java.time.LocalTime.now(nepalTimeZone) + " (Nepal Time)");
    }

    public List<Appointment> getAppointmentsByRole(String userRole, String userId) {

        List<Appointment> appointments;

        if ("ROLE_DOCTOR".equals(userRole)) {
            Staff doctor = staffRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("Doctor not found"));
            appointments = appointmentRepository.findByDoctorID(doctor);
        } else {
            appointments = appointmentRepository.findAll();
        }

        List<Appointment> pendingAppointments = appointments.stream()
                .filter(appt -> "Pending".equals(appt.getStatus()))
                .sorted(Comparator.comparing(Appointment::getAppDate))
                .collect(Collectors.toList());

        List<Appointment> otherAppointments = appointments.stream()
                .filter(appt -> !"Pending".equals(appt.getStatus()))
                .collect(Collectors.toList());

        // Combine the lists: pending (nearest first) followed by other appointments
        List<Appointment> combinedList = new ArrayList<>(pendingAppointments);
        combinedList.addAll(otherAppointments);

        return combinedList;
    }


    public Appointment editAppointment(Integer appointmentID, String updatedBy, LocalDate appDate, LocalTime appTime, String status) {
        // Fetch the appointment from the database
        Appointment appointment = appointmentRepository.findById(appointmentID)
                .orElseThrow(() -> new RuntimeException("Appointment not found"));
        User patient = appointment.getPatientID();
        Staff doctor = appointment.getDoctorID();
        LocalDate originalDate = appointment.getAppDate();
        LocalTime originalTime = appointment.getAppTime();
        String originalStatus = appointment.getStatus();

        boolean dateChanged = false;
        boolean timeChanged = false;
        boolean statusChanged = false;

        if (appDate != null && !appDate.toString().isEmpty() && !appDate.equals(originalDate)) {
            appointment.setAppDate(appDate);
            dateChanged = true;
        }
        if (appTime != null && !appTime.toString().isEmpty() && !appTime.equals(originalTime)) {
            appointment.setAppTime(appTime);
            timeChanged = true;
        }
        if (status != null && !status.isEmpty() && !status.equals(originalStatus)) {
            appointment.setStatus(status);
            statusChanged = true;
        }
        appointment.setAppUpdatedBy(updatedBy);
        Appointment updatedAppointment = appointmentRepository.save(appointment);

        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("hh:mm a");

        // Notification for the patient
        if (statusChanged && status.equals("Cancelled")) {
            String patientNotificationMessage = String.format("Your appointment with Dr. %s on %s has been cancelled.",
                    doctor.getStaffName(),
                    originalDate.format(dateFormatter));
            notificationService.createUserNotifications(patient.getUserID(), patientNotificationMessage, "appointment");

            String doctorNotificationMessage = String.format("Appointment for patient %s on %s has been cancelled.",
                    patient.getName(),
                    originalDate.format(dateFormatter));
            notificationService.createStaffNotifications(doctor.getStaffID(), doctorNotificationMessage, "appointment");

        } else if (dateChanged && timeChanged) {
            String patientNotificationMessage = String.format("Your appointment on %s has been rescheduled to %s at %s.",
                    originalDate.format(dateFormatter),
                    updatedAppointment.getAppDate().format(dateFormatter),
                    updatedAppointment.getAppTime().format(timeFormatter));
            notificationService.createUserNotifications(patient.getUserID(), patientNotificationMessage, "appointment");

            String doctorNotificationMessage = String.format("Appointment for patient %s on %s has been rescheduled to %s at %s.",
                    patient.getName(),
                    originalDate.format(dateFormatter),
                    updatedAppointment.getAppDate().format(dateFormatter),
                    updatedAppointment.getAppTime().format(timeFormatter));
            notificationService.createStaffNotifications(doctor.getStaffID(), doctorNotificationMessage, "appointment");

        } else if (dateChanged) {
            String patientNotificationMessage = String.format("Your appointment on %s has been rescheduled to %s for %s.",
                    originalDate.format(dateFormatter),
                    updatedAppointment.getAppDate().format(dateFormatter),
                    originalTime.format(timeFormatter));
            notificationService.createUserNotifications(patient.getUserID(), patientNotificationMessage, "appointment");

            String doctorNotificationMessage = String.format("Appointment for patient %s on %s has been rescheduled to %s for %s.",
                    patient.getName(),
                    originalDate.format(dateFormatter),
                    updatedAppointment.getAppDate().format(dateFormatter),
                    originalTime.format(timeFormatter));
            notificationService.createStaffNotifications(doctor.getStaffID(), doctorNotificationMessage, "appointment");

        } else if (timeChanged) {
            String patientNotificationMessage = String.format("Your appointment on %s has been rescheduled from %s to %s.",
                    originalDate.format(dateFormatter),
                    originalTime.format(timeFormatter),
                    updatedAppointment.getAppTime().format(timeFormatter));
            notificationService.createUserNotifications(patient.getUserID(), patientNotificationMessage, "appointment");

            String doctorNotificationMessage = String.format("Appointment for patient %s on %s has been rescheduled from %s to %s.",
                    patient.getName(),
                    originalDate.format(dateFormatter),
                    originalTime.format(timeFormatter),
                    updatedAppointment.getAppTime().format(timeFormatter));
            notificationService.createStaffNotifications(doctor.getStaffID(), doctorNotificationMessage, "appointment");
        }

        return updatedAppointment;
    }


    public void completeAppointment(Integer appointmentID, String staffId) {
        Appointment appointment = appointmentRepository.findById(appointmentID)
                .orElseThrow(() -> new RuntimeException("Appointment not found for ID: " + appointmentID));

        Staff staff = staffRepository.findById(staffId)
                .orElseThrow(() -> new RuntimeException("Staff not found for ID: " + staffId));

        if (!appointment.getDoctorID().getStaffID().equals(staff.getStaffID())) {
            throw new RuntimeException("You are not authorized to complete this appointment.");
        }

        appointment.setStatus("Completed"); // Update the status
        appointmentRepository.save(appointment); // Save the changes
    }

    public boolean appointmentExists(Integer patientID, String doctorID, LocalDate appDate, LocalTime appTime) {
        User patient = userRepository.findById(patientID)
                .orElseThrow(() -> new RuntimeException("Patient not found"));
        Staff doctor = staffRepository.findById(doctorID)
                .orElseThrow(() -> new RuntimeException("Doctor not found"));
        return appointmentRepository.existsByPatientIDAndDoctorIDAndAppDateAndAppTime(patient, doctor, appDate, appTime);
    }



     public List<Appointment> getAllAppointments(){
        return appointmentRepository.findAll();
     }

    public List<Appointment> getAppointmentsByDoctorAndDate(String doctorID, LocalDate date) {
        // Fetch the Staff entity from the repository
        Optional<Staff> staffOpt = staffRepository.findById(doctorID);

        if (staffOpt.isPresent()) {
            Staff doctor = staffOpt.get();
            return appointmentRepository.findByDoctorIDAndAppDate(doctor, date);
        } else {
            throw new RuntimeException("Doctor with ID " + doctorID + " not found.");
        }
    }
    public List<Appointment> getAppointmentByUserId(Integer userID) {
        User user=userRepository.findById(userID).orElseThrow(() -> new RuntimeException("Patient not found"));

        return appointmentRepository.getAppointmentByPatientID(user);

    }
    public void cancelAppointment(Integer appointmentID, String userID) {
        Optional<Appointment> appointmentOptional = appointmentRepository.findById(appointmentID);

        if (appointmentOptional.isPresent()) {
            Appointment appointment = appointmentOptional.get();
            User patient = appointment.getPatientID();
            Staff doctor = appointment.getDoctorID();
            LocalDate appDate = appointment.getAppDate();
            LocalTime appTime = appointment.getAppTime();
            appointment.setStatus("Cancelled"); // Or another status
            appointment.setAppUpdatedBy(userID);
            appointmentRepository.save(appointment);
            DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("hh:mm a");

            // Notification for the patient
            String patientNotificationMessage = String.format("Your appointment with Dr. %s on %s at %s has been cancelled.",
                    doctor.getStaffName(),
                    appDate.format(dateFormatter),
                    appTime.format(timeFormatter));
            notificationService.createUserNotifications(patient.getUserID(), patientNotificationMessage, "appointment");

            // Notification for the doctor
            String doctorNotificationMessage = String.format("Appointment for patient %s on %s at %s has been cancelled.",
                    patient.getName(),
                    appDate.format(dateFormatter),
                    appTime.format(timeFormatter));
            notificationService.createStaffNotifications(doctor.getStaffID(), doctorNotificationMessage, "appointment");
        } else {
            throw new RuntimeException("Appointment not found");
        }
    }

}
