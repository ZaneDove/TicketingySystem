package org.example;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;

public class TicketServer {

    /**
     * 插入一个新的Ticket到数据库
     * @param ticket Ticket对象
     */
    public void insertTicket(Ticket ticket) {
        String sql = "INSERT INTO Ticket (isDevelopment, userID, ticketInfo, priority, effect, status, responseTimeInDays, resolutionTimeInDays, actualResolutionTime, loggedTime) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection connection = MySQLConnection.getConnection();
             PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setBoolean(1, ticket.isDevelopment);
            pstmt.setInt(2, ticket.userID);
            pstmt.setString(3, ticket.ticketInfo);
            pstmt.setString(4, ticket.priority);
            pstmt.setString(5, ticket.effect);
            pstmt.setString(6, ticket.status);
            pstmt.setInt(7, ticket.responseTimeInDays);
            pstmt.setInt(8, ticket.resolutionTimeInDays);
            pstmt.setTime(9, ticket.actualResolutionTime);  // Time is already the correct type
            pstmt.setTime(10, ticket.loggedTime);  // Time is already the correct type
            pstmt.executeUpdate();
        } catch (SQLException | ClassNotFoundException e) {
            System.err.println("Failed to insert a Ticket: " + e.getMessage());
        }
    }



}
