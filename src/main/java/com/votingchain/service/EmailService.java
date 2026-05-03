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

    @Value("${RESEND_API_KEY:}")
    private String resendApiKey;

    @Value("${RESEND_SENDER_EMAIL:onboarding@resend.dev}")
    private String senderEmail;

    private final RestTemplate restTemplate = new RestTemplate();

    public void sendRegistrationEmail(String toEmail, String name, String voterId) {
        if (resendApiKey == null || resendApiKey.isEmpty()) {
            System.err.println("Resend API Key not configured. Skipping email sending.");
            return;
        }

        String url = "https://api.resend.com/emails";

        // Build the Resend JSON Payload
        Map<String, Object> payload = new HashMap<>();
        
        payload.put("from", "Voting System <" + senderEmail + ">");
        payload.put("to", List.of(toEmail));
        payload.put("subject", "Your Voter Registration Details");
        
        String contentText = "Hello " + name + ",\n\n" +
                "You have successfully registered for the Blockchain Voting System.\n" +
                "Your secure Voter ID is: " + voterId + "\n\n" +
                "Please keep this ID safe. You will need it to cast your vote.\n\n" +
                "Best regards,\n" +
                "Voting System Admin";
                
        payload.put("text", contentText);

        // Set Headers
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(resendApiKey);

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(payload, headers);

        try {
            restTemplate.postForEntity(url, request, String.class);
            System.out.println("Email sent successfully to " + toEmail + " via Resend.");
        } catch (Exception e) {
            System.err.println("Failed to send email via Resend: " + e.getMessage());
        }
    }
}
