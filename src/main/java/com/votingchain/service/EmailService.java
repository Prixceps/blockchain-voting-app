package com.votingchain.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    public void sendRegistrationEmail(String toEmail, String name, String voterId) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom("voting-system@noreply.com"); // Usually overridden by SMTP server depending on provider
        message.setTo(toEmail);
        message.setSubject("Your Voter Registration Details");
        message.setText("Hello " + name + ",\n\n" +
                "You have successfully registered for the Blockchain Voting System.\n" +
                "Your secure Voter ID is: " + voterId + "\n\n" +
                "Please keep this ID safe. You will need it to cast your vote.\n\n" +
                "Best regards,\n" +
                "Voting System Admin");

        mailSender.send(message);
    }
}
