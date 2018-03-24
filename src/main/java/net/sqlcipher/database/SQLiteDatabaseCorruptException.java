package net.sqlcipher.database;

public class SQLiteDatabaseCorruptException extends SQLiteException {
    public SQLiteDatabaseCorruptException(String error) {
        super(error);
    }
}
