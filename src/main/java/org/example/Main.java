package org.example;
import java.util.ArrayList;
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
ArrayList<Ticket> ticketArrayList = new ArrayList<Ticket>();
    public static void main(String[] args) { SpringApplication.run(Main.class, args);

    }
    //open ticket worker
    @ZeebeWorker(type = "OpenTicket")
    public void OpenTicket(final JobClient client, final ActivatedJob job){
        Map<String, Object> variablesAsMap = job.getVariablesAsMap();
        String priority = (String) variablesAsMap.get("priority");
        String info = (String) variablesAsMap.get("issueDescription");
        Ticket ticket;
        if (ticketArrayList.isEmpty()){
            ticket = new Ticket(1, info, true, priority);
        }else {
            ticket = new Ticket((ticketArrayList.size() + 1), info, true, priority);
        }
        ticketArrayList.add(ticket);
        HashMap<String, Object> variables = new HashMap<>();
        variables.put("ticketNo",ticket.getTicketNo());
        client.newCompleteCommand(job.getKey())
                .variables(variables)
                .send()
                .exceptionally((throwable -> {
                    throw new RuntimeException("Could not complete job", throwable);
                }));

    }
    @ZeebeWorker(type = "DetermineSLA")
    public void DetermineSLA(final JobClient client, final ActivatedJob job) {

        client.newCompleteCommand(job.getKey())
                .send()
                .exceptionally((throwable -> {
                    throw new RuntimeException("Could not complete job", throwable);
                }));
    }
    //complete ticket
    @ZeebeWorker(type = "closeTicket")
    public void closeTicket(final JobClient client, final ActivatedJob job) {
        Map<String, Object> variablesAsMap = job.getVariablesAsMap();
        String ticketNoAsString = (String) variablesAsMap.get("ticketNo");
        int ticketNo = Integer.parseInt(ticketNoAsString);
        // get ticket from array
        Ticket ticket = ticketArrayList.get(ticketNo - 1);
        //set as ticket open to false
        ticket.setTicketOpen(false);
        ticketArrayList.set(ticketNo - 1, ticket);
        client.newCompleteCommand(job.getKey())
                .send()
                .exceptionally((throwable -> {
                    throw new RuntimeException("Could not complete job", throwable);
                }));
    }


}