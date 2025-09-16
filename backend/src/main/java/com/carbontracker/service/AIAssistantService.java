package com.carbontracker.service;

public interface AIAssistantService {
    void setApiKey(String key);
    String getMaskedApiKey();

    /** Generate natural language suggestions for a given log id. */
    String generateSuggestions(Long logId);
}
