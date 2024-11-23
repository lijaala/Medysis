package Medysis.Project.Service;

import Medysis.Project.Model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    public void sendVerificationEmail(User user) {
        String verificationUrl="http://localhost:8081/api/auth/verify?code=" + user.getVerificationCode();

        SimpleMailMessage mailMessage = new SimpleMailMessage();
        mailMessage.setTo(user.getEmail());
        mailMessage.setSubject("Verify your account.[Medysis]");
        mailMessage.setText("Dear"+user.getName()+",\n\n"+
                "Please Click the link below to verify your account:\n"+
                verificationUrl+"\n\nThankyou!");
        mailSender.send(mailMessage);

    }
}
