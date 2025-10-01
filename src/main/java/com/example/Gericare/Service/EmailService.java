package com.example.Gericare.Service;

public interface EmailService {
    void sendPasswordResetEmail(String to, String token);
}