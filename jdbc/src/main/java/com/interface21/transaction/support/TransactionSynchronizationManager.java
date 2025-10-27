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

    public static Connection getResource(DataSource key) {
        Map<DataSource, Connection> map = resources.get();
        if (map == null) {
            return null;
        }

        return map.get(key);
    }

    public static void bindResource(DataSource key, Connection value) {
        Map<DataSource, Connection> map = resources.get();
        if (map == null) {
            map = new HashMap<>();
            resources.set(map);
        }

        map.put(key, value);
    }

    public static Connection unbindResource(DataSource key) throws SQLException {
        Map<DataSource, Connection> map = resources.get();
        if (map == null) {
            return key.getConnection();
        }
        return map.remove(key);
    }
}
