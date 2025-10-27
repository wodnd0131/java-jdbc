package com.interface21.transaction.support;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import javax.sql.DataSource;

public abstract class TransactionSynchronizationManager {

    private static final ThreadLocal<Map<DataSource, Connection>> resources = new ThreadLocal<>();

    private TransactionSynchronizationManager() {
    }

    public static Connection getResource(final DataSource key) {
        final var map = getDataSourceConnectionMap();
        return map.get(key);
    }

    public static void bindResource(final DataSource key, final Connection value) {
        final var map = getDataSourceConnectionMap();
        map.put(key, value);
    }

    public static Connection unbindResource(final DataSource key) throws SQLException {
        final var map = getDataSourceConnectionMap();
        final var connection = map.get(key);
        if (connection != null) {
            return map.remove(key);
        }
        return null;
    }

    private static Map<DataSource, Connection> getDataSourceConnectionMap() {
        Map<DataSource, Connection> dataSourceConnectionMap = resources.get();
        if (dataSourceConnectionMap == null) {
            dataSourceConnectionMap = new HashMap<>();
            resources.set(dataSourceConnectionMap);
        }
        return dataSourceConnectionMap;
    }
}
