package org.example;
import java.util.HashMap;
import java.util.Map;

import io.camunda.zeebe.spring.client.properties.ZeebeClientConfigurationProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import io.camunda.zeebe.client.api.response.ActivatedJob;
import io.camunda.zeebe.client.api.worker.JobClient;
import io.camunda.zeebe.spring.client.EnableZeebeClient;
import io.camunda.zeebe.spring.client.annotation.ZeebeWorker;

@SpringBootApplication
@EnableZeebeClient
public class Main {

    public static void main(String[] args) { SpringApplication.run(Main.class, args);

    }
}