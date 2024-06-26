package org.example.Model;

import java.sql.Time;
import java.time.LocalDateTime;

public class Ticket {
    // Ticket ID automatically generated by the database
    int ticketNo;

    // Ticket Type (Development or not)
    Boolean isDevelopment;

    // Linked User ID
    int userID;

    // Issue description
    String ticketInfo;

    // Ticket priority and effect
    String priority;
    String effect;

    // Ticket Status
    String status;

    String ITMenber;

    // Ticket Open time set automatically to current time
    LocalDateTime startTime = LocalDateTime.now();

    // Ticket expected response and resolution time (based on SLA)
    int responseTimeInDays;
    int resolutionTimeInDays;

    // Ticket Resolution time
    Time actualResolutionTime;

    // Employee working hours
    Time loggedTime;

    // Constructor without ticketNo and startTime as these are managed by the database/system
    public Ticket(int userID, String ticketInfo, String priority, String effect, String status, int responseTimeInDays, int resolutionTimeInDays, Time actualResolutionTime, Time loggedTime,String ITMenber) {
        this.userID = userID;
        this.ticketInfo = ticketInfo;
        this.priority = priority;
        this.effect = effect;
        this.status = status;
        this.responseTimeInDays = responseTimeInDays;
        this.resolutionTimeInDays = resolutionTimeInDays;
        this.actualResolutionTime = actualResolutionTime;
        this.loggedTime = loggedTime;
        this.ITMenber = ITMenber;
    }

    //update priority and effect
    public void checkPriorityEffect(String newPriority, String NewEffect) {
        boolean priorityCheck = this.priority != null && this.priority.equals(newPriority);
        boolean effectCheck = this.effect != null && this.effect.equals(NewEffect);

        if (!priorityCheck && !effectCheck) {
            System.out.println(ticketNo + " has been updated");
            this.priority = newPriority;
            this.effect = NewEffect;
        } else if (!priorityCheck) {
            System.out.println(ticketNo + " has been updated");
            this.priority = newPriority;
        } else if (!effectCheck) {
            System.out.println(ticketNo + " has been updated");
            this.effect = NewEffect;
        }
    }

    public String getITMenber() {
        return ITMenber;
    }

    public void setITMenber(String ITMenber) {
        this.ITMenber = ITMenber;
    }

    public void setTicketNo(int ticketNo) {
        this.ticketNo = ticketNo;
    }

    public int getTicketNo() {
        return ticketNo;
    }

    public Boolean getDevelopment() {
        return isDevelopment;
    }

    public void setDevelopment(Boolean development) {
        isDevelopment = development;
    }

    public int getUserID() {
        return userID;
    }

    public void setUserID(int userID) {
        this.userID = userID;
    }

    public String getTicketInfo() {
        return ticketInfo;
    }

    public void setTicketInfo(String ticketInfo) {
        this.ticketInfo = ticketInfo;
    }

    public String getPriority() {
        return priority;
    }

    public void setPriority(String priority) {
        this.priority = priority;
    }

    public String getEffect() {
        return effect;
    }

    public void setEffect(String effect) {
        this.effect = effect;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public int getResponseTimeInDays() {
        return responseTimeInDays;
    }

    public void setResponseTimeInDays(int responseTimeInDays) {
        this.responseTimeInDays = responseTimeInDays;
    }

    public int getResolutionTimeInDays() {
        return resolutionTimeInDays;
    }

    public void setResolutionTimeInDays(int resolutionTimeInDays) {
        this.resolutionTimeInDays = resolutionTimeInDays;
    }

    public Time getActualResolutionTime() {
        return actualResolutionTime;
    }

    public void setActualResolutionTime(Time actualResolutionTime) {
        this.actualResolutionTime = actualResolutionTime;
    }

    public Time getLoggedTime() {
        return loggedTime;
    }

    public void setLoggedTime(Time loggedTime) {
        this.loggedTime = loggedTime;
    }
}
