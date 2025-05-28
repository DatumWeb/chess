package dataaccess;

import java.sql.SQLException;

public class SQLDAOUtils {
    public static boolean isConnectionIssue(SQLException e) {
        String sqlState = e.getSQLState();
        String message = e.getMessage() != null ? e.getMessage().toLowerCase() : "";

        if (sqlState != null && sqlState.startsWith("08")) {
            return true;
        }
        return message.contains("communications link failure") ||
                message.contains("connection refused") ||
                message.contains("connection timed out") ||
                message.contains("cannot create poolableconnectionfactory") ||
                message.contains("unknown host") ||
                message.contains("network is unreachable");
    }
}