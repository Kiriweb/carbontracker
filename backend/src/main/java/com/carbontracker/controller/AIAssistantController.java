package com.carbontracker.controller;

import com.carbontracker.service.AIAssistantService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/ai")
public class AIAssistantController {

    private final AIAssistantService aiAssistantService;

    @Autowired
    public AIAssistantController(AIAssistantService aiAssistantService) {
        this.aiAssistantService = aiAssistantService;
    }

    @PostMapping("/key")
    public void setApiKey(@RequestBody String key) {
        aiAssistantService.setApiKey(key.replaceAll("\"", ""));
    }

    @GetMapping("/key")
    public String getMaskedKey() {
        return aiAssistantService.getMaskedApiKey();
    }

    @GetMapping("/suggestions/{logId}")
    public String getSuggestions(@PathVariable Long logId) {
        return aiAssistantService.generateSuggestions(logId);
    }
}