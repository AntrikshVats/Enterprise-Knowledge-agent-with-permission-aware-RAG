package com.knowflow.controller;

import com.knowflow.dto.SecurityScenarioResultDto;
import com.knowflow.service.SecurityEvaluationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/demo")
public class DemoScenarioController {

    private final SecurityEvaluationService securityEvaluationService;

    public DemoScenarioController(SecurityEvaluationService securityEvaluationService) {
        this.securityEvaluationService = securityEvaluationService;
    }

    @GetMapping("/scenarios")
    public ResponseEntity<List<SecurityScenarioResultDto>> runSecurityScenarios() {
        List<SecurityScenarioResultDto> results = securityEvaluationService.runAllSecurityScenarios();
        return ResponseEntity.ok(results);
    }
}
