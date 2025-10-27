package com.techcourse.service;

import com.interface21.dao.DataAccessException;
import com.interface21.jdbc.datasource.DataSourceUtils;
import com.interface21.transaction.support.TransactionSynchronizationManager;
import com.techcourse.config.DataSourceConfig;
import com.techcourse.dao.UserDao;
import com.techcourse.dao.UserHistoryDao;
import com.techcourse.domain.User;
import com.techcourse.domain.UserHistory;
import java.sql.Connection;
import java.sql.SQLException;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UserService {

    private static final Logger log = LoggerFactory.getLogger(UserService.class);

    private final UserDao userDao;
    private final UserHistoryDao userHistoryDao;

    public UserService(final UserDao userDao, final UserHistoryDao userHistoryDao) {
        this.userDao = userDao;
        this.userHistoryDao = userHistoryDao;
    }

    public User findById(final long id) {
        return userDao.findById(id);
    }

    public void insert(final User user) {
        userDao.insert(user);
    }

    public void changePassword(final long id, final String newPassword, final String createBy) {
        runInTransaction(() -> {
            final var user = findById(id);
            user.changePassword(newPassword);
            userDao.update(user);
            userHistoryDao.log(new UserHistory(user, createBy));
        });
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

    private void closeConnect(DataSource dataSource) {
        try {
            Connection connection = TransactionSynchronizationManager.unbindResource(dataSource);
            connection.setAutoCommit(false);
            connection.close();
        } catch (SQLException e) {
            throw new DataAccessException("Connect Close failed: " + e.getMessage(), e);
        }
    }

    private void rollbackConnection(Connection conn) {
        try {
            conn.rollback();
        } catch (SQLException ex) {
            log.error("Rollback failed: {}", ex.getMessage(), ex);
        }
    }
}
