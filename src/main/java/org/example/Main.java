package org.example;

import io.camunda.zeebe.client.api.response.ActivatedJob;
import io.camunda.zeebe.client.api.worker.JobClient;
import io.camunda.zeebe.spring.client.EnableZeebeClient;
import io.camunda.zeebe.spring.client.annotation.ZeebeWorker;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
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
        variables.put("priority_cover", ticket.getPriority());
        variables.put("effect_cover", ticket.getEffect());

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


        String effect = null;
        String priority = null;
        if (variablesAsMap.get("effect_cover") == null ||variablesAsMap.get("priority_cover") == null) {
            effect = (String)variablesAsMap.get("effect");
            priority = (String)variablesAsMap.get("priority");
        }
        effect = (String) variablesAsMap.get("effect_cover");
        priority = (String)variablesAsMap.get("priority_cover");

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


    private final HttpClient httpClient = HttpClient.newHttpClient();

    @ZeebeWorker(type = "AskMoreInfo")
    public void askMoreInfo(final JobClient jobClient, final ActivatedJob job) {
        try {
            String requestBody = """
                    {
                      "to": "user@example.com",
                      "subject": "Test Email",
                      "body": "This is a test email to demonstrate the process."
                    }
                    """;

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("https://d910014c-53fc-4d7e-9f39-23947305ef2a.mock.pstmn.io/request-info"))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                System.out.println("More information is provided");
                // Assuming "success" is a variable in the BPMN model
                jobClient.newCompleteCommand(job.getKey())
                        .variables("{\"success\": true}")
                        .send()
                        .join();
            } else {
                // Non-successful status code handling
                System.out.println("Failed to send email. Status code: " + response.statusCode());
                jobClient.newFailCommand(job.getKey())
                        .retries(job.getRetries() - 1)
                        .errorMessage("Failed to send email")
                        .send()
                        .join();
            }
        } catch (Exception e) {
            e.printStackTrace();
            // Send a failure message back to the Zeebe engine
            jobClient.newFailCommand(job.getKey())
                    .retries(job.getRetries() - 1)
                    .errorMessage("Exception occurred: " + e.getMessage())
                    .send()
                    .join();
        }
    }

    @ZeebeWorker(type = "SentServey")
    public void SentServey(final JobClient jobClient, final ActivatedJob job) {
        try {
            String requestBody = """
                    {
                      "to": "user@example.com",
                      "subject": "Test Email",
                      "body": "This is a test email to demonstrate the process."
                    }
                    """;

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("https://d910014c-53fc-4d7e-9f39-23947305ef2a.mock.pstmn.io/submit-survey"))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                System.out.println("Receipt of survey results");
                // Assuming "success" is a variable in the BPMN model
                jobClient.newCompleteCommand(job.getKey())
                        .variables("{\"success\": true}")
                        .send()
                        .join();
            } else {
                // Non-successful status code handling
                System.out.println("Failed to send email. Status code: " + response.statusCode());
                jobClient.newFailCommand(job.getKey())
                        .retries(job.getRetries() - 1)
                        .errorMessage("Failed to send email")
                        .send()
                        .join();
            }
        } catch (Exception e) {
            e.printStackTrace();
            // Send a failure message back to the Zeebe engine
            jobClient.newFailCommand(job.getKey())
                    .retries(job.getRetries() - 1)
                    .errorMessage("Exception occurred: " + e.getMessage())
                    .send()
                    .join();
        }
    }


}