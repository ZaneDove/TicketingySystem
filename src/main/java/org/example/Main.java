package org.example;

import io.camunda.zeebe.client.api.response.ActivatedJob;
import io.camunda.zeebe.client.api.worker.JobClient;
import io.camunda.zeebe.spring.client.EnableZeebeClient;
import io.camunda.zeebe.spring.client.annotation.ZeebeWorker;
import org.example.Controller.TicketServer;
import org.example.Controller.UserServer;
import org.example.Model.Ticket;
import org.example.Model.User;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.sql.Time;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

@SpringBootApplication
@EnableZeebeClient
public class Main {
    ArrayList<Ticket> ticketArrayList = new ArrayList<Ticket>();
    Ticket ticket;

    public static void main(String[] args) {
        SpringApplication.run(Main.class, args);
    }

    TicketServer ticketServer = new TicketServer();

    //open ticket worker
    @ZeebeWorker(type = "OpenTicket")
    public void OpenTicket(final JobClient client, final ActivatedJob job) {
        //get variables as map
        Map<String, Object> variablesAsMap = job.getVariablesAsMap();

        // get User parameter
        String name = (String) variablesAsMap.get("name");
        String email = (String) variablesAsMap.get("email");

        //get Ticket parameter
        Boolean isDevelopment = (Boolean) variablesAsMap.get("isDevelopment");
        String ticketInfo = (String) variablesAsMap.get("issueDescription");
        String priority = (String) variablesAsMap.get("priority");
        String effect = (String) variablesAsMap.get("effect");
        String status = "Open";
        int responseTimeInDays = 0;
        int resolutionTimeInDays = 0;
        Time actualResolutionTime = null;
        Time loggedTime = null;
        String ITMenber = null;

        //create User in DB
        User user = new User(name,email);
        UserServer userServer = new UserServer();
        userServer.insertUser(user);
        int userID = user.getUserNo();

        //create new ticket in DB
        ticket = new Ticket(isDevelopment,userID,ticketInfo,priority,effect,status,responseTimeInDays,resolutionTimeInDays,actualResolutionTime,loggedTime,ITMenber);
        ticketServer.insertTicket(ticket);

        System.out.println(isDevelopment+""+userID);

        //add ticket no to map
        HashMap<String, Object> variables = new HashMap<>();
        //complete job send ticketNo
        variables.put("ticketNo", ticket.getTicketNo());
        variables.put("priority_cover", ticket.getPriority());
        variables.put("effect_cover", ticket.getEffect());

        //Method run code
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

        //IT manager overwrites the user's selection
        String effect = null;
        String priority = null;

        if (variablesAsMap.get("check_PandE").equals(false)) {
            effect = (String)variablesAsMap.get("effect");
            priority = (String)variablesAsMap.get("priority");
        }
        effect = (String) variablesAsMap.get("effect_cover");
        priority = (String)variablesAsMap.get("priority_cover");
        String member = (String)variablesAsMap.get("ITMenber");

        int TicketNo = ticket.getTicketNo();
        ticketServer.updateTicketPriorityAndEffect(TicketNo,priority,effect,member);

        //add ticket no to map
        HashMap<String, Object> variables = new HashMap<>();

        // job complete
        client.newCompleteCommand(job.getKey())
                .variables(variables)
                .send()
                .exceptionally((throwable -> {
                    throw new RuntimeException("Could not complete job", throwable);
                }));

    }

//    //get ticket
//    //use newTicket name = getTicket(ticket number); to find ticket from array
//    private Ticket getTicket(int ticketNo) {
//        //check if ticket exists
//        // Check if ticket exists before trying to get from list
//        if (ticketNo <= ticketArrayList.size()) {
//            return ticketArrayList.get(ticketNo - 1);
//        } else {
//            System.out.println("No such ticket exists in the list");
//            return null;
//            // check ticket priority and effect are not changed
//        }
//    }
//
//
//    @ZeebeWorker(type = "closeTicket")
//    public void closeTicket(final JobClient client, final ActivatedJob job) {
//        // get variables as map
//        Map<String, Object> variablesAsMap = job.getVariablesAsMap();
//        // get ticket no as string
//        int ticketNo = (int) variablesAsMap.get("ticketNo");
//        //get ticket from array list (ticketNo - 1)
//        Ticket tempTicket = ticketArrayList.get(ticketNo - 1);
//        // ticket open = false
//        tempTicket.setTicketOpen(false);
//        //ticket in array = temp ticket
//        ticketArrayList.set(ticketNo - 1, tempTicket);
//        //print ticket closed
//        System.out.print("ticket " + ticketNo + " closed");
//        //job complete
//        client.newCompleteCommand(job.getKey())
//                .send()
//                .exceptionally((throwable -> {
//                    throw new RuntimeException("Could not complete job", throwable);
//                }));
//
//    }
//

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


    @ZeebeWorker(type = "CompletedTicket")
    public void CompletedTicket(final JobClient jobClient, final ActivatedJob job){
        int TicketNO = ticket.getTicketNo();
        ticketServer.markTicketAsCompleted(TicketNO);

        try {
            String requestBody = """
                    {
                      "to": "user@example.com",
                      "subject": "Ticket Completed",
                      "body": "Your ticket is complete, please confirm"
                    }
                    """;

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("https://d910014c-53fc-4d7e-9f39-23947305ef2a.mock.pstmn.io/CompletedTicket"))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                System.out.println("Ok, I'll confirm it later");
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