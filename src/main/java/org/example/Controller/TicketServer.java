package org.example.Controller;

import org.example.Model.Ticket;

import java.sql.*;

public class TicketServer {

    /**
     * Inserts a new Ticket into the database and retrieves the generated ticketNo.
     * @param ticket Ticket object
     */
    public void insertTicket(Ticket ticket) {
        String sql = "INSERT INTO Ticket (userID, ticketInfo, priority, effect, status, responseTimeInDays, resolutionTimeInDays, actualResolutionTime, loggedTime,ITMenber) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?,?)";

        try (Connection connection = MySQLConnection.getConnection();
             PreparedStatement pstmt = connection.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {
            pstmt.setInt(1, ticket.getUserID());
            pstmt.setString(2, ticket.getTicketInfo());
            pstmt.setString(3, ticket.getPriority());
            pstmt.setString(4, ticket.getEffect());
            pstmt.setString(5, ticket.getStatus());
            pstmt.setInt(6, ticket.getResponseTimeInDays());
            pstmt.setInt(7, ticket.getResolutionTimeInDays());
            pstmt.setTime(8, ticket.getActualResolutionTime());
            pstmt.setTime(9, ticket.getLoggedTime());
            pstmt.setString(10, ticket.getITMenber());

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
    public void updateTicketPriorityAndEffect(int ticketNo, String newPriority, String newEffect, String ITMenber,boolean isDevelopment) {
        int responseTimeInDays = calculateResponseTime(newEffect, newPriority);
        int resolutionTimeInDays = calculateResolutionTime(newEffect, newPriority);

        String sql = "UPDATE Ticket SET priority = ?, effect = ?, responseTimeInDays = ?, resolutionTimeInDays = ?, ITMenber = ?, isDevelopment = ? WHERE ticketNo = ?";

        try (Connection connection = MySQLConnection.getConnection();
             PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, newPriority);
            pstmt.setString(2, newEffect);
            pstmt.setInt(3, responseTimeInDays);
            pstmt.setInt(4, resolutionTimeInDays);
            pstmt.setString(5, ITMenber);
            pstmt.setBoolean(6,isDevelopment);
            pstmt.setInt(7, ticketNo);


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

    /**
     * Updates the status of an existing ticket in the database to "Open".
     * @param ticketNo The ID of the ticket to update.
     */
    public void markTicketAsOpen(int ticketNo) {
        String sql = "UPDATE Ticket SET status = 'Open' WHERE ticketNo = ?";

        try (Connection connection = MySQLConnection.getConnection();
             PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, ticketNo);

            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                System.out.println("Ticket " + ticketNo + " was successfully marked as Open.");
            } else {
                System.out.println("No ticket was updated, check the ticket number.");
            }
        } catch (SQLException | ClassNotFoundException e) {
            System.err.println("Failed to update the ticket status: " + e.getMessage());
        }
    }

    /**
     * Appends additional information to the TicketInfo field in the database.
     * @param ticketNo The ID of the ticket to update.
     * @param additionalInfo The additional information to append to the existing TicketInfo.
     */
    public void appendToTicketInfo(int ticketNo, String additionalInfo) {
        // Retrieve the current TicketInfo from the database
        String currentInfo = getCurrentTicketInfo(ticketNo);
        if (currentInfo == null) {
            System.err.println("No ticket found with ticketNo: " + ticketNo);
            return;
        }

        // Append the additional information to the current TicketInfo
        String updatedInfo = currentInfo + "//more info:" + additionalInfo;

        // SQL statement to update the TicketInfo field
        String sql = "UPDATE Ticket SET ticketInfo = ? WHERE ticketNo = ?";

        try (Connection connection = MySQLConnection.getConnection();
             PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, updatedInfo);
            pstmt.setInt(2, ticketNo);

            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                System.out.println("TicketInfo for ticket " + ticketNo + " was updated successfully.");
            } else {
                System.out.println("No ticket was updated, check the ticket number.");
            }
        } catch (SQLException | ClassNotFoundException e) {
            System.err.println("Failed to update the TicketInfo: " + e.getMessage());
        }
    }

    /**
     * Retrieves the current TicketInfo for a specific ticket.
     * @param ticketNo The ID of the ticket.
     * @return The current TicketInfo or null if the ticket cannot be found.
     */
    private String getCurrentTicketInfo(int ticketNo) {
        String sql = "SELECT ticketInfo FROM Ticket WHERE ticketNo = ?";

        try (Connection connection = MySQLConnection.getConnection();
             PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, ticketNo);

            try (ResultSet resultSet = pstmt.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getString("ticketInfo");
                }
            }
        } catch (SQLException | ClassNotFoundException e) {
            System.err.println("Failed to retrieve the TicketInfo: " + e.getMessage());
        }
        return null;
    }

    /**
     * Updates the actual resolution time and logged time for a given ticket.
     * @param ticketNo The ID of the ticket to update.
     * @param newActualResolutionTime The new actual resolution time to set.
     * @param newLoggedTime The new logged time to set in hours.
     */
    public void setFinishTimes(int ticketNo, Date newActualResolutionTime, int newLoggedTime) {
        String sql = "UPDATE Ticket SET actualResolutionTime = ?, loggedTime = ? WHERE ticketNo = ?";

        try (Connection connection = MySQLConnection.getConnection();
             PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setDate(1, newActualResolutionTime);
            pstmt.setInt(2, newLoggedTime);
            pstmt.setInt(3, ticketNo);

            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                System.out.println("Ticket " + ticketNo + " was updated successfully with new times.");
            } else {
                System.out.println("No ticket was updated, check the ticket number.");
            }
        } catch (SQLException | ClassNotFoundException e) {
            System.err.println("Failed to update the ticket times: " + e.getMessage());
        }
    }

    /**
     * Updates the status of an existing ticket in the database to "Close".
     * @param ticketNo The ID of the ticket to update.
     */
    public void CloseTicket(int ticketNo) {
        String sql = "UPDATE Ticket SET status = 'Close' WHERE ticketNo = ?";

        try (Connection connection = MySQLConnection.getConnection();
             PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, ticketNo);

            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                System.out.println("Ticket " + ticketNo + " was successfully marked as Open.");
            } else {
                System.out.println("No ticket was updated, check the ticket number.");
            }
        } catch (SQLException | ClassNotFoundException e) {
            System.err.println("Failed to update the ticket status: " + e.getMessage());
        }
    }
}
