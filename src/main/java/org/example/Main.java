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
        //if list is empty ticket no = 1, else ti ticketNo = list size + 1
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
        // //get variables as map
        Map<String, Object> variablesAsMap = job.getVariablesAsMap();

        // get ticketNo, effect and priority from map
        String effect = (String) variablesAsMap.get("effect");
        String priority = (String) variablesAsMap.get("priority");
        int ticketNo = (int) variablesAsMap.get("ticketNo");
        //create temp ticket and check changes
        Ticket tempTicket = getTicket(ticketNo);


        //ticket in array = temp ticket
        if (ticketNo <= ticketArrayList.size()) {
            ticketArrayList.set(ticketNo - 1, tempTicket);
        } else {
            System.out.println("The input ticket number is out of range");
            return;  // Return from the method or handle this wrong input case accordingly.
        }
        //create SlaTimeCalc object
        SlaTimeCalc timeCalc = new SlaTimeCalc(effect, priority);
        //get times from Calc
        tempTicket.setResponseTimeInDays(timeCalc.getResponseTime());
        tempTicket.setResolutionTimeInDays(timeCalc.getResolutionTime());
        //check for ticket updates
        tempTicket.checkPriorityEffect(priority, effect);
        //add ticket no to map
        HashMap<String, Object> variables = new HashMap<>();
        //complete job send ticketNo
        variables.put("responseTime", tempTicket.getResponseTimeInDays());
        variables.put("resolutionTime", tempTicket.getResolutionTimeInDays());
        // job complete
        client.newCompleteCommand(job.getKey())
                .variables(variables)
                .send()
                .exceptionally((throwable -> {
                    throw new RuntimeException("Could not complete job", throwable);
                }));

    }

    //get ticket
    //use newTicket name = getTicket(ticket number); to find ticket from array
    private Ticket getTicket(int ticketNo) {
        //check if ticket exists
        // Check if ticket exists before trying to get from list
        if (ticketNo <= ticketArrayList.size()) {
            return ticketArrayList.get(ticketNo - 1);
        } else {
            System.out.println("No such ticket exists in the list");
            return null;
            // check ticket priority and effect are not changed
        }
    }


    @ZeebeWorker(type = "closeTicket")
    public void closeTicket(final JobClient client, final ActivatedJob job) {
        // get variables as map
        Map<String, Object> variablesAsMap = job.getVariablesAsMap();
        // get ticket no as string
        int ticketNo = (int) variablesAsMap.get("ticketNo");
        //get ticket from array list (ticketNo - 1)
        Ticket tempTicket = ticketArrayList.get(ticketNo - 1);
        // ticket open = false
        tempTicket.setTicketOpen(false);
        //ticket in array = temp ticket
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