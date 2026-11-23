package com.university.scheduler;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class UniversitySchedulerApplication {

    public static void main(String[] args) {
        SpringApplication.run(UniversitySchedulerApplication.class, args);
    }
}
