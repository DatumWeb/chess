package server;

import dataaccess.*;

public class DAOFactory {

    public enum DAOType {
        MEMORY,
        SQL
    }

    private static final DAOType DAO_TYPE = DAOType.SQL;

    public static UserDAO createUserDAO() throws DataAccessException {
        switch (DAO_TYPE) {
            case MEMORY:
                return new MemoryUserDAO();
            case SQL:
                return new SQLUserDAO();
            default:
                throw new DataAccessException("Unknown DAO type: " + DAO_TYPE);
        }
    }

    public static GameDAO createGameDAO() throws DataAccessException {
        switch (DAO_TYPE) {
            case MEMORY:
                return new MemoryGameDAO();
            case SQL:
                return new SQLGameDAO();
            default:
                throw new DataAccessException("Unknown DAO type: " + DAO_TYPE);
        }
    }

    public static AuthDAO createAuthDAO() throws DataAccessException {
        switch (DAO_TYPE) {
            case MEMORY:
                return new MemoryAuthDAO();
            case SQL:
                return new SQLAuthDAO();
            default:
                throw new DataAccessException("Unknown DAO type: " + DAO_TYPE);
        }
    }

    public static void initializeDatabase() throws DataAccessException {
        if (DAO_TYPE == DAOType.SQL) {
            DatabaseManager.createDatabase();
        }
    }
}