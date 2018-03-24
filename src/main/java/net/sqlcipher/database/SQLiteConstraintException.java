package net.sqlcipher.database;

public class SQLiteConstraintException extends SQLiteException {
    public SQLiteConstraintException(String error) {
        super(error);
    }
}
