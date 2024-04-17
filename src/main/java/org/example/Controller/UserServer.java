package org.example.Controller;

import org.example.Model.User;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class UserServer {
    /**
     * Inserts a new User into the database and updates the userNo attribute of the User object.
     * @param user The User object to insert into the database.
     */
    public void insertUser(User user) {
        String sql = "INSERT INTO User (name, email) VALUES (?, ?)";

        try (Connection connection = MySQLConnection.getConnection();
             PreparedStatement pstmt = connection.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, user.getName());
            pstmt.setString(2, user.getEmail());
            int affectedRows = pstmt.executeUpdate();

            if (affectedRows == 0) {
                throw new SQLException("Creating user failed, no rows affected.");
            }

            try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    user.setUserNo(generatedKeys.getInt(1)); // Update the userNo in the User object
                } else {
                    throw new SQLException("Creating user failed, no ID obtained.");
                }
            }
        } catch (SQLException | ClassNotFoundException e) {
            System.err.println("Failed to insert a User: " + e.getMessage());
        }
    }

}
