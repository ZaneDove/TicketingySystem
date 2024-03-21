package org.example;

public class Ticket {
    int ticketNo;
    String TicketInfo;
    Boolean ticketOpen;
    String Priority;

    public Ticket(int ticketNo, String ticketInfo, Boolean ticketOpen, String priority) {
        this.ticketNo = ticketNo;
        TicketInfo = ticketInfo;
        this.ticketOpen = ticketOpen;
        Priority = priority;
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
