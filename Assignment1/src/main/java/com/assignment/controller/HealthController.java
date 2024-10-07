package com.assignment.controller;

import com.assignment.model.Health;
import com.assignment.repository.HealthRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

@RestController
@RequestMapping("/health")
public class HealthController {

    @Autowired
    private HealthRepository healthRepository;

    @Autowired
    private RestTemplate restTemplate; // Reuse RestTemplate from a Bean

    @Scheduled(fixedRate = 60000) // Sends the signal every minute
    public void sendAliveSignal() {
        boolean clientAlive = checkClientStatus();
        if (!clientAlive) {
            boolean responseAlive = checkResponseStatus();
            if (!responseAlive) {
                System.out.println("Both Client and Response services are down.");
            } else {
                System.out.println("Client is down, but Response service is alive.");
            }
        } else {
            System.out.println("Client is alive.");
        }
    }

    @GetMapping
    public ResponseEntity<Health> getHealthStatus() {
        Health health = healthRepository.findFirstByOrderByIdDesc();

        boolean clientAlive = checkClientStatus();
        if (!clientAlive) {
            boolean responseAlive = checkResponseStatus();
            if (!responseAlive) {
                return ResponseEntity.status(404).body(health);
            }
        }

        return ResponseEntity.ok(health);
    }

    private boolean checkClientStatus() {
        try {
            ResponseEntity<String> response = restTemplate.getForEntity("http://localhost:8080/client/health", String.class);
            if (response.getStatusCode().is2xxSuccessful()) {
                System.out.println("Client service is alive: " + response.getBody());
                return true;
            }
        } catch (HttpStatusCodeException e) {
            System.out.println("Client service is down: " + e.getMessage());
        } catch (Exception e) {
            System.out.println("Error while checking client status: " + e.getMessage());
        }
        return false;
    }

    private boolean checkResponseStatus() {
        try {
            ResponseEntity<String> response = restTemplate.getForEntity("http://localhost:8080/response", String.class);
            if (response.getStatusCode().is2xxSuccessful()) {
                System.out.println("Response service is alive: " + response.getBody());
                return true;
            }
        } catch (HttpStatusCodeException e) {
            System.out.println("Response service is down: " + e.getMessage());
        } catch (Exception e) {
            System.out.println("Error while checking response status: " + e.getMessage());
        }
        return false;
    }
}
