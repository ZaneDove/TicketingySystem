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
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.sql.Date;
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

    //Open ticket
    @ZeebeWorker(type = "OpenTicket")
    public void OpenTicket(final JobClient client, final ActivatedJob job) {
        //get variables as map
        Map<String, Object> variablesAsMap = job.getVariablesAsMap();

        // get User parameter
        String name = (String) variablesAsMap.get("name");
        String email = (String) variablesAsMap.get("email");

        //get Ticket parameter
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
        ticket = new Ticket(userID,ticketInfo,priority,effect,status,responseTimeInDays,resolutionTimeInDays,actualResolutionTime,loggedTime,ITMenber);
        ticketServer.insertTicket(ticket);

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

    //Determine SLA
    @ZeebeWorker(type = "DetermineSLA")
    public void DetermineSLA(final JobClient client, final ActivatedJob job) {
        // //get variables as map
        Map<String, Object> variablesAsMap = job.getVariablesAsMap();

        //IT manager overwrites the user's selection
        String effect = (String) variablesAsMap.get("effect");
        String priority = (String)variablesAsMap.get("priority");
        String member = (String)variablesAsMap.get("ITMenber");
        Boolean isDevelopment = (Boolean) variablesAsMap.get("isDevelopment");

        int TicketNo = ticket.getTicketNo();
        ticketServer.updateTicketPriorityAndEffect(TicketNo,priority,effect,member,isDevelopment);

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

    @ZeebeWorker(type = "ChangeOpenTicket")
    public void ChangeOpenTicket(final JobClient client, final ActivatedJob job){
        int TicketNo = ticket.getTicketNo();
        ticketServer.markTicketAsOpen(TicketNo);

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

    //Sending fake email use postman
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
        //get variables as map
        Map<String, Object> variablesAsMap = job.getVariablesAsMap();

        int TicketNO = ticket.getTicketNo();
        String datetime = (String) variablesAsMap.get("actualResolutionTime");

        // Assuming the datetime string is in the format "yyyy-MM-dd" since it's just a date
        LocalDate date = LocalDate.parse(datetime, DateTimeFormatter.ISO_LOCAL_DATE);

        // Correct way to convert LocalDate to java.sql.Date
        Date actualResolutionTime = Date.valueOf(date);
        int loggedTime = (int) variablesAsMap.get("loggedTime");

        ticketServer.setFinishTimes(TicketNO,actualResolutionTime,loggedTime);

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

    @ZeebeWorker(type = "MoreInfoStroeDB")
    public void MoreInfoStroeDB(final JobClient client, final ActivatedJob job){
        int TicketNo = ticket.getTicketNo();

        Map<String, Object> variablesAsMap = job.getVariablesAsMap();
        String issueDescription = (String) variablesAsMap.get("issueDescription");
        String moreInfo = (String) variablesAsMap.get("MoreInfo");

        ticketServer.appendToTicketInfo(TicketNo,moreInfo);

        //add ticket no to map
        HashMap<String, Object> variables = new HashMap<>();
        variables.put("issueDescription",issueDescription+"//  provide more information: "+moreInfo);

        // job complete
        client.newCompleteCommand(job.getKey())
                .variables(variables)
                .send()
                .exceptionally((throwable -> {
                    throw new RuntimeException("Could not complete job", throwable);
                }));


    }

    @ZeebeWorker(type = "closeTicket")
    public void closeTicket(final JobClient client, final ActivatedJob job){
        int TicketNo = ticket.getTicketNo();
        ticketServer.CloseTicket(TicketNo);

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

}