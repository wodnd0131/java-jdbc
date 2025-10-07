package com.interface21.jdbc.core;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;

public class JdbcTemplate {

    private static final Logger log = LoggerFactory.getLogger(JdbcTemplate.class);

    private final DataSource dataSource;

    public JdbcTemplate(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public <T> T queryForObject(final String sql, final RowMapper<T> rowMapper, final Object... parameters) {
        return query(sql, getSinglePreparedStatementCallback(rowMapper, parameters));
    }

    public <T> List<T> queryForList(final String sql,final RowMapper<T> rowMapper, final Object... parameters) {
        return query(sql, getPreparedStatementCallbackForList(rowMapper, parameters));
    }

    public int update(final String sql,final Object... parameters) {
        return execute(sql, getSinglePreparedStatementCallback(parameters));
    }

    private <T> T query(final String sql, final PreparedStatementCallback<T> action) {
        return execute(sql, action);
    }

    public <T> PreparedStatementCallback<List<T>> getPreparedStatementCallbackForList(final RowMapper<T> rowMapper, final Object[] params) {
        return ps -> {
            setPreparedStatementParams(params, ps);

            List<T> results = new ArrayList<>();
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    results.add(rowMapper.mapRow(rs));
                }
            }
            return results;
        };
    }

    private <T> PreparedStatementCallback<T> getSinglePreparedStatementCallback(final RowMapper<T> rowMapper, final Object[] params) {
        return ps -> {
            setPreparedStatementParams(params, ps);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rowMapper.mapRow(rs);
                }
                return null;
            }
        };
    }
    private PreparedStatementCallback<Integer> getSinglePreparedStatementCallback(final Object[] params) {
        return ps -> {
            setPreparedStatementParams(params, ps);
            return ps.executeUpdate();
        };
    }

    private void setPreparedStatementParams(final Object[] params, final PreparedStatement ps) throws SQLException {
        for (int i = 0; i < params.length; i++) {
            ps.setObject(i + 1, params[i]);
        }
    }

    //https://github.com/spring-projects/spring-framework/blob/6aeb9d16e83c69d980f48a4a4f052bf52f31dfd0/spring-jdbc/src/main/java/org/springframework/jdbc/core/JdbcTemplate.java#L654
    private <T> T execute(final String sql, final PreparedStatementCallback<T> action) {
        try (final var conn = dataSource.getConnection();
             final var ps = conn.prepareStatement(sql)) {

            log.debug("query : {}", sql);
            return action.doInPreparedStatement(ps);

        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }
}
