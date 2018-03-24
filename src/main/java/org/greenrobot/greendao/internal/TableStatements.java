package org.greenrobot.greendao.internal;

import org.greenrobot.greendao.database.Database;
import org.greenrobot.greendao.database.DatabaseStatement;

public class TableStatements {
    private final String[] allColumns;
    private DatabaseStatement countStatement;
    private final Database db;
    private DatabaseStatement deleteStatement;
    private DatabaseStatement insertOrReplaceStatement;
    private DatabaseStatement insertStatement;
    private final String[] pkColumns;
    private volatile String selectAll;
    private volatile String selectByKey;
    private volatile String selectByRowId;
    private volatile String selectKeys;
    private final String tablename;
    private DatabaseStatement updateStatement;

    public TableStatements(Database db, String tablename, String[] allColumns, String[] pkColumns) {
        this.db = db;
        this.tablename = tablename;
        this.allColumns = allColumns;
        this.pkColumns = pkColumns;
    }

    public DatabaseStatement getInsertStatement() {
        if (this.insertStatement == null) {
            DatabaseStatement newInsertStatement = this.db.compileStatement(SqlUtils.createSqlInsert("INSERT INTO ", this.tablename, this.allColumns));
            synchronized (this) {
                if (this.insertStatement == null) {
                    this.insertStatement = newInsertStatement;
                }
            }
            if (this.insertStatement != newInsertStatement) {
                newInsertStatement.close();
            }
        }
        return this.insertStatement;
    }

    public DatabaseStatement getInsertOrReplaceStatement() {
        if (this.insertOrReplaceStatement == null) {
            DatabaseStatement newInsertOrReplaceStatement = this.db.compileStatement(SqlUtils.createSqlInsert("INSERT OR REPLACE INTO ", this.tablename, this.allColumns));
            synchronized (this) {
                if (this.insertOrReplaceStatement == null) {
                    this.insertOrReplaceStatement = newInsertOrReplaceStatement;
                }
            }
            if (this.insertOrReplaceStatement != newInsertOrReplaceStatement) {
                newInsertOrReplaceStatement.close();
            }
        }
        return this.insertOrReplaceStatement;
    }

    public DatabaseStatement getDeleteStatement() {
        if (this.deleteStatement == null) {
            DatabaseStatement newDeleteStatement = this.db.compileStatement(SqlUtils.createSqlDelete(this.tablename, this.pkColumns));
            synchronized (this) {
                if (this.deleteStatement == null) {
                    this.deleteStatement = newDeleteStatement;
                }
            }
            if (this.deleteStatement != newDeleteStatement) {
                newDeleteStatement.close();
            }
        }
        return this.deleteStatement;
    }

    public DatabaseStatement getUpdateStatement() {
        if (this.updateStatement == null) {
            DatabaseStatement newUpdateStatement = this.db.compileStatement(SqlUtils.createSqlUpdate(this.tablename, this.allColumns, this.pkColumns));
            synchronized (this) {
                if (this.updateStatement == null) {
                    this.updateStatement = newUpdateStatement;
                }
            }
            if (this.updateStatement != newUpdateStatement) {
                newUpdateStatement.close();
            }
        }
        return this.updateStatement;
    }

    public DatabaseStatement getCountStatement() {
        if (this.countStatement == null) {
            this.countStatement = this.db.compileStatement(SqlUtils.createSqlCount(this.tablename));
        }
        return this.countStatement;
    }

    public String getSelectAll() {
        if (this.selectAll == null) {
            this.selectAll = SqlUtils.createSqlSelect(this.tablename, "T", this.allColumns, false);
        }
        return this.selectAll;
    }

    public String getSelectKeys() {
        if (this.selectKeys == null) {
            this.selectKeys = SqlUtils.createSqlSelect(this.tablename, "T", this.pkColumns, false);
        }
        return this.selectKeys;
    }

    public String getSelectByKey() {
        if (this.selectByKey == null) {
            StringBuilder builder = new StringBuilder(getSelectAll());
            builder.append("WHERE ");
            SqlUtils.appendColumnsEqValue(builder, "T", this.pkColumns);
            this.selectByKey = builder.toString();
        }
        return this.selectByKey;
    }

    public String getSelectByRowId() {
        if (this.selectByRowId == null) {
            this.selectByRowId = getSelectAll() + "WHERE ROWID=?";
        }
        return this.selectByRowId;
    }
}
