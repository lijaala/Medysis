package Medysis.Project.Service;

import Medysis.Project.Model.*;
import Medysis.Project.Repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service

public class LabOrderService {
    @Autowired
    private LabOrderRepository labOrderRepository;

    @Autowired
    private LabResultRepository labResultsRepository;

    @Autowired
    private LabTestRepository labTestsRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private StaffRepository staffRepository;

    @Autowired
    private AppointmentRepository appointmentRepository;


    public LabOrder createLabOrder(int appointmentId, int userId, String staffId,  String urgency, List<Integer> testIds) {
        LabOrder labOrder = new LabOrder();
        User user = userRepository.findById(userId).orElse(null);
        Staff staffID = staffRepository.findById(staffId)
                .orElseThrow(() -> new RuntimeException("Staff not found for ID: " + staffId));

        Appointment appointment = appointmentRepository.findById(appointmentId).orElse(null);

        labOrder.setUserID(user);
        labOrder.setDoctorID(staffID);
        labOrder.setAppointmentID(appointment);
        labOrder.setOrderDate(LocalDate.now()); // Or get from the request
        labOrder.setUrgency(urgency);
        labOrderRepository.save(labOrder);



        for (Integer testId : testIds) {
            LabResults labResult = new LabResults();
            labResult.setOrderID(labOrder);
            labResult.setUserID(user);
            labResult.setDoctorID(staffID);
            labResult.setAppointmentID(appointment);
            LabTests labTest = labTestsRepository.findById(testId).orElse(null);
            labResult.setTestID(labTest);
            labResultsRepository.save(labResult);
        }

        return labOrder; // Or return a DTO if you prefer
    }

    public List<LabTests> getAvailableTests() {
        return labTestsRepository.findAll();
    }

}
