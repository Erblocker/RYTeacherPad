package net.sqlcipher.database;

public class SQLiteFullException extends SQLiteException {
    public SQLiteFullException(String error) {
        super(error);
    }
}
