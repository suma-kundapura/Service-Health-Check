package org.sd.servicehealthcheck.controller;

import org.sd.servicehealthcheck.service.FailureDetectionService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/health")
public class ServiceHealthController {

    private final FailureDetectionService failureDetectionService;

    public ServiceHealthController(FailureDetectionService failureDetectionService) {
        this.failureDetectionService = failureDetectionService;
    }

    @PostMapping("/update/{serviceId}")
    public String updateHealthInstances(@PathVariable String serviceId, @RequestBody List<String> instances) {
        failureDetectionService.updateHealthInstances(serviceId, instances);
        return "Health instances updated for service: " + serviceId;
    }

    @GetMapping("/call/{serviceId}/{endPoint}")
    public String callService(@PathVariable String serviceId, @PathVariable String endPoint) {
        return failureDetectionService.callService(serviceId, "/" + endPoint);
    }


}
