package com.university.scheduler.controller;

import com.university.scheduler.algorithm.comparison.AlgorithmComparisonService;
import com.university.scheduler.dto.AlgorithmBenchmarkResultDto;
import com.university.scheduler.dto.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/benchmark")
public class AlgorithmComparisonController {

    private final AlgorithmComparisonService comparisonService;

    public AlgorithmComparisonController(AlgorithmComparisonService comparisonService) {
        this.comparisonService = comparisonService;
    }

    @GetMapping("/compare")
    public ResponseEntity<ApiResponse<List<AlgorithmBenchmarkResultDto>>> runComparison() {
        List<AlgorithmBenchmarkResultDto> results = comparisonService.runBenchmark();
        return ResponseEntity.ok(ApiResponse.success(
                "Benchmark comparison executed across identical dataset",
                results
        ));
    }
}
