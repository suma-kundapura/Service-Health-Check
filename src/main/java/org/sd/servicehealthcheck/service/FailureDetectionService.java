package org.sd.servicehealthcheck.service;

import io.github.resilience4j.retry.annotation.Retry;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class FailureDetectionService {
    private final RestTemplate restTemplate;
    private final ConcurrentHashMap<String, TreeSet<String>> healthInstances = new ConcurrentHashMap<>();


    public FailureDetectionService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public void updateHealthInstances(String serviceName, List<String> instances) {
        TreeSet<String> liveInstances = new TreeSet<>();
       for(String instance : instances) {
          if(isHealthy(instance)) {
              liveInstances.add(instance);
          }
       }
         healthInstances.put(serviceName, liveInstances);
    }

    private boolean isHealthy(String url) {
        try{
            String healthUrl = url + "/actuator/health";
            String health = restTemplate.getForObject(healthUrl, String.class);
            return health != null && health.contains("\"status\":\"UP\"");
        } catch (Exception e) {
            return false;
        }
    }

    @Retry(name = "serviceCall", fallbackMethod = "fallback")
    public String callService(String serviceId, String endPoint) {
        TreeSet<String> instances = healthInstances.get(serviceId);
        if(instances == null || instances.isEmpty()) {
            throw new RuntimeException("No instances available for service: " + serviceId);
        }
        String instance = instances.first();
        String url = instance + endPoint;
        return restTemplate.getForObject(url, String.class);
    }

    public String fallback(String serviceId, String endPoint, Throwable t) {
        return "Service call failed for service: " + serviceId + " and endpoint: "
                + endPoint+ ". Please try again later.";
    }
}
