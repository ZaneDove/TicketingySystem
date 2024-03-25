package org.example;

import io.camunda.zeebe.client.api.response.ActivatedJob;
import io.camunda.zeebe.client.api.worker.JobClient;
import io.camunda.zeebe.spring.client.EnableZeebeClient;
import io.camunda.zeebe.spring.client.annotation.ZeebeWorker;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

@SpringBootApplication
@EnableZeebeClient
public class Main {
    ArrayList<Ticket> ticketArrayList = new ArrayList<Ticket>();

    public static void main(String[] args) {
        SpringApplication.run(Main.class, args);

    }

    //open ticket worker
    @ZeebeWorker(type = "OpenTicket")
    public void OpenTicket(final JobClient client, final ActivatedJob job) {
        //get variables as map
        Map<String, Object> variablesAsMap = job.getVariablesAsMap();
        // get priority and issueDescription
        String priority = (String) variablesAsMap.get("priority");
        String info = (String) variablesAsMap.get("issueDescription");
        String email = (String) variablesAsMap.get("email");
        String effect = (String) variablesAsMap.get("effect");
        //new ticket
        Ticket ticket;
        //if list is empty tiket no = 1, else tikcetno = list size + 1
        if (ticketArrayList.isEmpty()) {
            ticket = new Ticket(1, info, true, priority, email, effect);
        } else {
            ticket = new Ticket((ticketArrayList.size() + 1), info, true, priority, email, effect);
        }
        //add to list
        ticketArrayList.add(ticket);
        //add ticket no to map
        HashMap<String, Object> variables = new HashMap<>();
        //complete job send ticketNo
        variables.put("ticketNo", ticket.getTicketNo());
        client.newCompleteCommand(job.getKey())
                .variables(variables)
                .send()
                .exceptionally((throwable -> {
                    throw new RuntimeException("Could not complete job", throwable);
                }));

    }
        // zeebe worker start
    @ZeebeWorker(type = "DetermineSLA")
    public void DetermineSLA(final JobClient client, final ActivatedJob job) {
        // job complete
        client.newCompleteCommand(job.getKey())
                .send()
                .exceptionally((throwable -> {
                    throw new RuntimeException("Could not complete job", throwable);
                }));

    }
    // zeebe worker end

    @ZeebeWorker(type = "closeTicket")
    public void closeTicket(final JobClient client, final ActivatedJob job) {
        // get variables as map
        Map<String, Object> variablesAsMap = job.getVariablesAsMap();
        // get ticket no as string
        String ticketNoAsString = (String) variablesAsMap.get("ticketNo");
        //Convert to int
        int ticketNo = Integer.parseInt(ticketNoAsString);
        //get ticket from array list (ticketNo - 1)
        Ticket tempTicket = ticketArrayList.get(ticketNo - 1);
        // ticket open = false
        tempTicket.setTicketOpen(false);
        //ticket in arrray = temp ticket
        ticketArrayList.set(ticketNo - 1, tempTicket);
        //print ticket closed
        System.out.print("ticket " + ticketNo + " closed");
        //job complete
        client.newCompleteCommand(job.getKey())
                .send()
                .exceptionally((throwable -> {
                    throw new RuntimeException("Could not complete job", throwable);
                }));

    }


}