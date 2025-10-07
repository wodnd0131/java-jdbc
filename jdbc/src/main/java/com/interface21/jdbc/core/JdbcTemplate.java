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

    public <T> T query(final String sql, final PreparedStatementCallback<T> action) {
        return execute(sql, action);
    }

    public <T> T queryForObject(final String sql, final RowMapper<T> rowMapper, final Long id) {
        return query(sql, getPreparedStatementCallback(rowMapper, id));
    }


    //https://github.com/spring-projects/spring-framework/blob/6aeb9d16e83c69d980f48a4a4f052bf52f31dfd0/spring-jdbc/src/main/java/org/springframework/jdbc/core/JdbcTemplate.java#L654
    private <T> T execute(final String sql, PreparedStatementCallback<T> action) {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            log.debug("query : {}", sql);
            return action.doInPreparedStatement(ps);

        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    private <T> PreparedStatementCallback<T> getPreparedStatementCallback(final RowMapper<T> rowMapper, final Long id) {
        return ps -> {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rowMapper.mapRow(rs);
                }
                return null;
            }
        };
    }

    private PreparedStatementCallback<Integer> getPreparedStatementCallback(Object[] parameters) {
        return ps -> {
            for (int i = 0; i < parameters.length; i++) {
                ps.setObject(i + 1, parameters[i]);
            }
            return ps.executeUpdate();
        };
    }
}
