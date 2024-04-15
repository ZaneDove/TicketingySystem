package org.example;

import java.sql.Time;
import java.time.LocalDateTime;

public class Ticket {
    int ticketNo;
    String ticketInfo;
    Boolean ticketOpen;
    String priority;
    String email;
    String effect;
    LocalDateTime startTime;
    int responseTimeInDays;
    int resolutionTimeInDays;
    Time actualResoultionTime;
    Time loggedTime;

    public Ticket(int ticketNo, String ticketInfo, Boolean ticketOpen, String priority, String email, String effect) {
        this.ticketNo = ticketNo;
        this.ticketInfo = ticketInfo;
        this.ticketOpen = ticketOpen;
        this.priority = priority;
        this.email = email;
        this.effect = effect;
        this.startTime = LocalDateTime.now();
    }

    public void checkPriorityEffect(String newPriority, String NewEffect) {
        boolean priorityCheck = this.priority != null && this.priority.equals(newPriority);
        boolean effectCheck = this.effect != null && this.effect.equals(NewEffect);
        boolean ifAnyAreNull = this.effect == null && this.priority == null;
        if (!ifAnyAreNull) {
            if (!priorityCheck && !effectCheck) {
                System.out.println(ticketNo + " has been updated");
                this.priority = newPriority;
                this.effect = NewEffect;
            } else if (!priorityCheck && effectCheck) {
                System.out.println(ticketNo + " has been updated");
                this.priority = newPriority;
            } else if (priorityCheck && !effectCheck) {
                System.out.println(ticketNo + " has been updated");
                this.effect = NewEffect;
            }
        }
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

    public int getResponseTimeInDays() {
        return responseTimeInDays;
    }

    public void setResponseTimeInDays(int responseTime) {
        this.responseTimeInDays = responseTime;
    }

    public int getResolutionTimeInDays() {
        return resolutionTimeInDays;
    }

    public void setResolutionTimeInDays(int resolutionTimeInDays) {
        this.resolutionTimeInDays = resolutionTimeInDays;
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
        return priority;
    }

    public void setPriority(String priority) {
        this.priority = priority;
    }

    public int getTicketNo() {
        return ticketNo;
    }

    public void setTicketNo(int ticketNo) {
        this.ticketNo = ticketNo;
    }

    public String getTicketInfo() {
        return ticketInfo;
    }

    public void setTicketInfo(String ticketInfo) {
        this.ticketInfo = ticketInfo;
    }

    public Boolean getTicketOpen() {
        return ticketOpen;
    }

    public void setTicketOpen(Boolean ticketOpen) {
        this.ticketOpen = ticketOpen;
    }
}
