package com.carbontracker.service;

public interface AIAssistantService {
    String generateSuggestions(Long emissionLogId);
    void setApiKey(String apiKey);
    String getMaskedApiKey();
}
