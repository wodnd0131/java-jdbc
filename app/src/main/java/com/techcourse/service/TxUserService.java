package com.techcourse.service;

import com.interface21.dao.DataAccessException;
import com.interface21.jdbc.datasource.DataSourceUtils;
import com.interface21.transaction.support.TransactionSynchronizationManager;
import com.techcourse.config.DataSourceConfig;
import com.techcourse.domain.User;
import java.sql.Connection;
import java.sql.SQLException;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TxUserService implements UserService {

    private static final Logger log = LoggerFactory.getLogger(TxUserService.class);

    private final UserService userService;

    public TxUserService(UserService userService) {
        this.userService = userService;
    }

    @Override
    public User findById(final long id) {
        return userService.findById(id);
    }

    @Override
    public void save(final User user) {
        userService.save(user);
    }

    @Override
    public void changePassword(final long id, final String newPassword, final String createBy) {
        runInTransaction(() -> userService.changePassword(id, newPassword, createBy));
    }

    public void runInTransaction(final Runnable action) {
        final var dataSource = DataSourceConfig.getInstance();
        final var conn = DataSourceUtils.getConnection(dataSource);
        TransactionSynchronizationManager.bindResource(dataSource, conn);
        try {
            conn.setAutoCommit(false);
            action.run();
            conn.commit();
        } catch (Exception e) {
            rollbackConnection(conn);
            throw new DataAccessException("Transaction failed: " + e.getMessage(), e);
        } finally {
            closeConnect(dataSource);
        }
    }

    private void rollbackConnection(Connection conn) {
        try {
            conn.rollback();
        } catch (SQLException ex) {
            log.error("Rollback failed: {}", ex.getMessage(), ex);
        }
    }

    private void closeConnect(DataSource dataSource) {
        try {
            Connection connection = TransactionSynchronizationManager.unbindResource(dataSource);
            connection.setAutoCommit(false);
            connection.close();
        } catch (SQLException e) {
            throw new DataAccessException("Connect Close failed: " + e.getMessage(), e);
        }
    }
}
