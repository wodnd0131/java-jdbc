package com.interface21.jdbc.datasource;

import java.sql.Connection;
import java.util.Optional;

public class ConnectionManager {

    private static final ConnectionManager INSTANCE = new ConnectionManager();
    private Connection connection;

    private ConnectionManager() {
    }

    public static ConnectionManager getInstance() {
        return INSTANCE;
    }

    public void setConnection(Connection connection) {
        this.connection = connection;
    }

    public Optional<Connection> getConnection() {
        return Optional.ofNullable(connection);
    }

    public boolean isConnecting() {
        return connection != null;
    }
}
