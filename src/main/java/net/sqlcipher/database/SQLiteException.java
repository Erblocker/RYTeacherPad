package net.sqlcipher.database;

import net.sqlcipher.SQLException;

public class SQLiteException extends SQLException {
    public SQLiteException(String error) {
        super(error);
    }
}
