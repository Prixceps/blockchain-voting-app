package com.votingchain.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class EmailService {

    @Value("${SENDGRID_API_KEY:}")
    private String sendGridApiKey;

    @Value("${SENDGRID_SENDER_EMAIL:}")
    private String senderEmail;

    private final RestTemplate restTemplate = new RestTemplate();

    public void sendRegistrationEmail(String toEmail, String name, String voterId) {
        if (sendGridApiKey == null || sendGridApiKey.isEmpty()) {
            System.err.println("SendGrid API Key not configured. Skipping email sending.");
            return;
        }

        String url = "https://api.sendgrid.com/v3/mail/send";

        // Build the SendGrid JSON Payload
        Map<String, Object> payload = new HashMap<>();
        
        Map<String, Object> personalizations = new HashMap<>();
        personalizations.put("to", List.of(Map.of("email", toEmail)));
        payload.put("personalizations", List.of(personalizations));
        
        payload.put("from", Map.of("email", senderEmail, "name", "Voting System"));
        payload.put("subject", "Your Voter Registration Details");
        
        String contentText = "Hello " + name + ",\n\n" +
                "You have successfully registered for the Blockchain Voting System.\n" +
                "Your secure Voter ID is: " + voterId + "\n\n" +
                "Please keep this ID safe. You will need it to cast your vote.\n\n" +
                "Best regards,\n" +
                "Voting System Admin";
                
        payload.put("content", List.of(Map.of("type", "text/plain", "value", contentText)));

        // Set Headers
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(sendGridApiKey);

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(payload, headers);

        try {
            restTemplate.postForEntity(url, request, String.class);
            System.out.println("Email sent successfully to " + toEmail + " via SendGrid.");
        } catch (Exception e) {
            System.err.println("Failed to send email via SendGrid: " + e.getMessage());
        }
    }
}
