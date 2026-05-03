package com.votingchain;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Main entry point for the Blockchain Voting System.
 */
@SpringBootApplication
public class VotingChainApplication {

    public static void main(String[] args) {
        System.out.println("==============================================");
        System.out.println("   Blockchain Voting System - Starting Up...");
        System.out.println("==============================================");
        SpringApplication.run(VotingChainApplication.class, args);
        System.out.println("==============================================");
        System.out.println("   System Ready: http://localhost:8080");
        System.out.println("==============================================");
    }

    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/**").allowedOrigins("*").allowedMethods("*");
            }
        };
    }
}
