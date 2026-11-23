package com.university.scheduler.controller;

import com.university.scheduler.dto.ApiResponse;
import com.university.scheduler.service.ConcurrencyDemoService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/concurrency")
public class ConcurrencyDemoController {

    private final ConcurrencyDemoService concurrencyDemoService;

    public ConcurrencyDemoController(ConcurrencyDemoService concurrencyDemoService) {
        this.concurrencyDemoService = concurrencyDemoService;
    }

    @PostMapping("/run")
    public ResponseEntity<ApiResponse<ConcurrencyDemoService.DemoResult>> runDemo(
            @RequestParam(defaultValue = "4") int producers,
            @RequestParam(defaultValue = "2") int consumers,
            @RequestParam(defaultValue = "50") int itemsPerProducer,
            @RequestParam(defaultValue = "20") int queueCapacity) {

        ConcurrencyDemoService.DemoResult result = concurrencyDemoService.runDemonstration(
                producers, consumers, itemsPerProducer, queueCapacity
        );

        return ResponseEntity.ok(ApiResponse.success(
                "Multi-threaded concurrency simulation completed successfully",
                result
        ));
    }
}
