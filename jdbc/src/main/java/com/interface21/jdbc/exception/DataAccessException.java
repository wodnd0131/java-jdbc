package com.interface21.jdbc.exception;

import java.sql.SQLException;

public class DataAccessException extends RuntimeException {

    public DataAccessException(String msg, SQLException ex) {
        super(msg, ex);
    }

}
