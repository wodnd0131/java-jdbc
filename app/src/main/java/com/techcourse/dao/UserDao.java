package com.techcourse.dao;

import com.interface21.jdbc.core.RowMapper;
import com.techcourse.domain.User;
import com.interface21.jdbc.core.JdbcTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class UserDao {

    private static final Logger log = LoggerFactory.getLogger(UserDao.class);

    private final JdbcTemplate jdbcTemplate;

    private static final RowMapper<User> USER_ROW_MAPPER = rs -> new User(
            rs.getLong("id"),
            rs.getString("account"),
            rs.getString("password"),
            rs.getString("email")
    );

    public UserDao(final JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public int insert(final User user) {
        final var sql = "insert into users (account, password, email) values (?, ?, ?)";
        int hitRow = jdbcTemplate.update(sql, user.getAccount(), user.getPassword(), user.getEmail());
        log.debug("Inserted {} row(s) for user account: {}", hitRow, user.getAccount());
        return hitRow;
    }

    public void update(final User user) {
        final var sql = "update users set account = ?, password = ?, email = ? where id = ?";
        int hitRow = jdbcTemplate.update(sql,
                user.getAccount(),
                user.getPassword(),
                user.getEmail(),
                user.getId()
        );
        log.debug("Updated {} row(s) for user id: {}", hitRow, user.getId());
    }

    public List<User> findAll() {
        return List.of();
    }

    public User findById(final Long id) {
        final var sql = "select id, account, password, email from users where id = ?";
        User user = jdbcTemplate.queryForObject(sql, USER_ROW_MAPPER, id);
        log.debug("Found user by id {}: {}", id, user != null ? user : "null");
        return user;
    }

    public User findByAccount(final String account) {
        final var sql = "select id, account, password, email from users where account = ?";
        User user = jdbcTemplate.queryForObject(sql, USER_ROW_MAPPER, account);
        log.debug("Found user by account '{}': {}", account, user != null ? user : "null");
        return user;
    }
}
