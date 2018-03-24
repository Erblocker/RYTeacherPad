package net.sqlcipher.database;

public class SQLiteMisuseException extends SQLiteException {
    public SQLiteMisuseException(String error) {
        super(error);
    }
}
