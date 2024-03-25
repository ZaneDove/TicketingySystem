package org.example;

import java.sql.Time;
import java.time.LocalDateTime;

public class Ticket {
    int ticketNo;
    String TicketInfo;
    Boolean ticketOpen;
    String Priority;
    String email;
    String effect;
    LocalDateTime startTime;
    Time responseTime;
    Time resoultionTime;
    Time actualResoultionTime;
    Time loggedTime;

    public Ticket(int ticketNo, String ticketInfo, Boolean ticketOpen, String priority, String email, String effect) {
        this.ticketNo = ticketNo;
        TicketInfo = ticketInfo;
        this.ticketOpen = ticketOpen;
        Priority = priority;
        this.email = email;
        this.effect = effect;
        this.startTime = LocalDateTime.now();
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getEffect() {
        return effect;
    }

    public void setEffect(String effect) {
        this.effect = effect;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    public Time getResponseTime() {
        return responseTime;
    }

    public void setResponseTime(Time responseTime) {
        this.responseTime = responseTime;
    }

    public Time getResoultionTime() {
        return resoultionTime;
    }

    public void setResoultionTime(Time resoultionTime) {
        this.resoultionTime = resoultionTime;
    }

    public Time getActualResoultionTime() {
        return actualResoultionTime;
    }

    public void setActualResoultionTime(Time actualResoultionTime) {
        this.actualResoultionTime = actualResoultionTime;
    }

    public Time getLoggedTime() {
        return loggedTime;
    }

    public void setLoggedTime(Time loggedTime) {
        this.loggedTime = loggedTime;
    }

    public String getPriority() {
        return Priority;
    }

    public void setPriority(String priority) {
        Priority = priority;
    }

    public int getTicketNo() {
        return ticketNo;
    }

    public void setTicketNo(int ticketNo) {
        this.ticketNo = ticketNo;
    }

    public String getTicketInfo() {
        return TicketInfo;
    }

    public void setTicketInfo(String ticketInfo) {
        TicketInfo = ticketInfo;
    }

    public Boolean getTicketOpen() {
        return ticketOpen;
    }

    public void setTicketOpen(Boolean ticketOpen) {
        this.ticketOpen = ticketOpen;
    }
}
