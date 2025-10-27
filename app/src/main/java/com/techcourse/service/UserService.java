package com.techcourse.service;

import com.interface21.dao.DataAccessException;
import com.interface21.jdbc.datasource.ConnectionManager;
import com.techcourse.dao.UserDao;
import com.techcourse.dao.UserHistoryDao;
import com.techcourse.domain.User;
import com.techcourse.domain.UserHistory;
import java.sql.SQLException;
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

    public void runInTransaction(Runnable action) {
        final var connectionManager = ConnectionManager.getInstance();
        final var connection = connectionManager.getConnection();
        if (connection.isPresent()) {
            final var conn = connection.get();
            try {
                conn.setAutoCommit(false);
                action.run();
                conn.commit();
            } catch (Exception e) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    log.error("Rollback failed: {}", ex.getMessage());
                }
                throw new DataAccessException("Transaction failed: " + e.getMessage());
            } finally {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException e) {
                    log.error("Connection Close failed: {}", e.getMessage());
                }
                connectionManager.setConnection(null);
            }
        } else {
            throw new DataAccessException("No connection available for transaction");
        }
    }
}
