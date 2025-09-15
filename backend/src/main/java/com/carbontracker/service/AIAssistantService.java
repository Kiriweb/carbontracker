package com.carbontracker.service;

public interface AIAssistantService {
    // key management
    boolean hasKey();
    String maskedKey();
    void storeKey(String apiKeyPlaintext);
    void deleteKey();

    // auth utility
    String emailFromJwt(String jwt);

    // suggestions
    String generateSuggestionsForLog(String requesterEmail, Long logId);
}
