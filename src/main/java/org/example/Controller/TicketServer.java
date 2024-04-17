package org.example.Controller;

import org.example.Model.Ticket;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class TicketServer {

    /**
     * Inserts a new Ticket into the database and retrieves the generated ticketNo.
     * @param ticket Ticket object
     */
    public void insertTicket(Ticket ticket) {
        String sql = "INSERT INTO Ticket (isDevelopment, userID, ticketInfo, priority, effect, status, responseTimeInDays, resolutionTimeInDays, actualResolutionTime, loggedTime,ITMenber) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?,?)";

        try (Connection connection = MySQLConnection.getConnection();
             PreparedStatement pstmt = connection.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {
            pstmt.setBoolean(1, ticket.getDevelopment());
            pstmt.setInt(2, ticket.getUserID());
            pstmt.setString(3, ticket.getTicketInfo());
            pstmt.setString(4, ticket.getPriority());
            pstmt.setString(5, ticket.getEffect());
            pstmt.setString(6, ticket.getStatus());
            pstmt.setInt(7, ticket.getResponseTimeInDays());
            pstmt.setInt(8, ticket.getResolutionTimeInDays());
            pstmt.setTime(9, ticket.getActualResolutionTime());
            pstmt.setTime(10, ticket.getLoggedTime());
            pstmt.setString(11, ticket.getITMenber());

            int affectedRows = pstmt.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Creating ticket failed, no rows affected.");
            }

            try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    ticket.setTicketNo(generatedKeys.getInt(1)); // Update the ticketNo in the Ticket object
                } else {
                    throw new SQLException("Creating ticket failed, no ID obtained.");
                }
            }
        } catch (SQLException | ClassNotFoundException e) {
            System.err.println("Failed to insert a Ticket: " + e.getMessage());
        }
    }

    /**
     * Updates the priority, effect, response time, and resolution time of an existing ticket in the database according to SLA.
     * @param ticketNo The ID of the ticket to update.
     * @param newPriority The new priority to set.
     * @param newEffect The new effect to set.
     * @param ITMenber The new effect to set.
     */
    public void updateTicketPriorityAndEffect(int ticketNo, String newPriority, String newEffect, String ITMenber) {
        int responseTimeInDays = calculateResponseTime(newEffect, newPriority);
        int resolutionTimeInDays = calculateResolutionTime(newEffect, newPriority);

        String sql = "UPDATE Ticket SET priority = ?, effect = ?, responseTimeInDays = ?, resolutionTimeInDays = ?, ITMenber = ? WHERE ticketNo = ?";

        try (Connection connection = MySQLConnection.getConnection();
             PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, newPriority);
            pstmt.setString(2, newEffect);
            pstmt.setInt(3, responseTimeInDays);
            pstmt.setInt(4, resolutionTimeInDays);
            pstmt.setString(5, ITMenber);
            pstmt.setInt(6, ticketNo);

            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                System.out.println("Ticket " + ticketNo + " was updated successfully with new SLA times.");
            } else {
                System.out.println("No ticket was updated, check the ticket number.");
            }
        } catch (SQLException | ClassNotFoundException e) {
            System.err.println("Failed to update the ticket: " + e.getMessage());
        }
    }

    private int calculateResponseTime(String effect, String priority) {
        switch (effect) {
            case "High":
                return 1; // High urgency has a response time of 1 day regardless of priority
            case "Medium":
                return 2; // Medium urgency has a response time of 2 days regardless of priority
            case "Low":
                if (priority.equals("Low")) {
                    return 3; // Non-urgent, Low priority
                }
                return 3; // Non-urgent, Medium and High priority use the same as Low since not specified differently
            default:
                return 3; // Default to the longest response time if not specified
        }
    }

    private int calculateResolutionTime(String effect, String priority) {
        if (priority.equals("High")) {
            return 1; // All high priority tickets have a resolution time of 1 day
        }
        return switch (effect) {
            case "High", "Medium" -> 3; // High and Medium urgency with Medium priority have a 3 day resolution time
            case "Low" -> 5; // All Low urgency tickets have a resolution time of 5 days
            default -> 5; // Default to the longest resolution time if not specified
        };
    }

    /**
     * Updates the status of an existing ticket in the database to "Completed".
     * @param ticketNo The ID of the ticket to update.
     */
    public void markTicketAsCompleted(int ticketNo) {
        String sql = "UPDATE Ticket SET status = 'Completed' WHERE ticketNo = ?";

        try (Connection connection = MySQLConnection.getConnection();
             PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, ticketNo);

            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                System.out.println("Ticket " + ticketNo + " was successfully marked as Completed.");
            } else {
                System.out.println("No ticket was updated, check the ticket number.");
            }
        } catch (SQLException | ClassNotFoundException e) {
            System.err.println("Failed to update the ticket status: " + e.getMessage());
        }
    }





}
