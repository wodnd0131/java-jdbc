package com.interface21.jdbc.core;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;

public class JdbcTemplate {

    private static final Logger log = LoggerFactory.getLogger(JdbcTemplate.class);

    private final DataSource dataSource;

    public JdbcTemplate(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public long update(final String sql,final Object... parameters) {
        return execute(sql, getPreparedStatementCallback(parameters));
    }

    //https://github.com/spring-projects/spring-framework/blob/6aeb9d16e83c69d980f48a4a4f052bf52f31dfd0/spring-jdbc/src/main/java/org/springframework/jdbc/core/JdbcTemplate.java#L654
    private  <T> T execute(final String sql, PreparedStatementCallback<T> action) {
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = dataSource.getConnection();
            ps = conn.prepareStatement(sql);

            log.debug("query : {}", sql);
            return action.doInPreparedStatement(ps);
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        } finally {
            try {
                if (ps != null) {
                    ps.close();
                }
            } catch (SQLException ignored) {}

            try {
                if (conn != null) {
                    conn.close();
                }
            } catch (SQLException ignored) {}
        }
    }

    private static PreparedStatementCallback<Integer> getPreparedStatementCallback(Object[] parameters) {
        return ps -> {
            for (int i = 0; i < parameters.length; i++) {
                ps.setObject(i + 1, parameters[i]);
            }
            return ps.executeUpdate();
        };
    }
}
