package net.sqlcipher.database;

import android.text.TextUtils;
import android.util.Log;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.regex.Pattern;
import net.sqlcipher.Cursor;
import net.sqlcipher.DatabaseUtils;
import net.sqlcipher.database.SQLiteDatabase.CursorFactory;
import org.ksoap2.SoapEnvelope;

public class SQLiteQueryBuilder {
    private static final String TAG = "SQLiteQueryBuilder";
    private static final Pattern sLimitPattern = Pattern.compile("\\s*\\d+\\s*(,\\s*\\d+\\s*)?");
    private boolean mDistinct = false;
    private CursorFactory mFactory = null;
    private Map<String, String> mProjectionMap = null;
    private boolean mStrictProjectionMap;
    private String mTables = "";
    private StringBuilder mWhereClause = null;

    public void setDistinct(boolean distinct) {
        this.mDistinct = distinct;
    }

    public String getTables() {
        return this.mTables;
    }

    public void setTables(String inTables) {
        this.mTables = inTables;
    }

    public void appendWhere(CharSequence inWhere) {
        if (this.mWhereClause == null) {
            this.mWhereClause = new StringBuilder(inWhere.length() + 16);
        }
        if (this.mWhereClause.length() == 0) {
            this.mWhereClause.append('(');
        }
        this.mWhereClause.append(inWhere);
    }

    public void appendWhereEscapeString(String inWhere) {
        if (this.mWhereClause == null) {
            this.mWhereClause = new StringBuilder(inWhere.length() + 16);
        }
        if (this.mWhereClause.length() == 0) {
            this.mWhereClause.append('(');
        }
        DatabaseUtils.appendEscapedSQLString(this.mWhereClause, inWhere);
    }

    public void setProjectionMap(Map<String, String> columnMap) {
        this.mProjectionMap = columnMap;
    }

    public void setCursorFactory(CursorFactory factory) {
        this.mFactory = factory;
    }

    public void setStrictProjectionMap(boolean flag) {
        this.mStrictProjectionMap = flag;
    }

    public static String buildQueryString(boolean distinct, String tables, String[] columns, String where, String groupBy, String having, String orderBy, String limit) {
        if (TextUtils.isEmpty(groupBy) && !TextUtils.isEmpty(having)) {
            throw new IllegalArgumentException("HAVING clauses are only permitted when using a groupBy clause");
        } else if (TextUtils.isEmpty(limit) || sLimitPattern.matcher(limit).matches()) {
            StringBuilder query = new StringBuilder(SoapEnvelope.VER12);
            query.append("SELECT ");
            if (distinct) {
                query.append("DISTINCT ");
            }
            if (columns == null || columns.length == 0) {
                query.append("* ");
            } else {
                appendColumns(query, columns);
            }
            query.append("FROM ");
            query.append(tables);
            appendClause(query, " WHERE ", where);
            appendClause(query, " GROUP BY ", groupBy);
            appendClause(query, " HAVING ", having);
            appendClause(query, " ORDER BY ", orderBy);
            appendClause(query, " LIMIT ", limit);
            return query.toString();
        } else {
            throw new IllegalArgumentException("invalid LIMIT clauses:" + limit);
        }
    }

    private static void appendClause(StringBuilder s, String name, String clause) {
        if (!TextUtils.isEmpty(clause)) {
            s.append(name);
            s.append(clause);
        }
    }

    private static void appendClauseEscapeClause(StringBuilder s, String name, String clause) {
        if (!TextUtils.isEmpty(clause)) {
            s.append(name);
            DatabaseUtils.appendEscapedSQLString(s, clause);
        }
    }

    public static void appendColumns(StringBuilder s, String[] columns) {
        int n = columns.length;
        for (int i = 0; i < n; i++) {
            String column = columns[i];
            if (column != null) {
                if (i > 0) {
                    s.append(", ");
                }
                s.append(column);
            }
        }
        s.append(' ');
    }

    public Cursor query(SQLiteDatabase db, String[] projectionIn, String selection, String[] selectionArgs, String groupBy, String having, String sortOrder) {
        return query(db, projectionIn, selection, selectionArgs, groupBy, having, sortOrder, null);
    }

    public Cursor query(SQLiteDatabase db, String[] projectionIn, String selection, String[] selectionArgs, String groupBy, String having, String sortOrder, String limit) {
        if (this.mTables == null) {
            return null;
        }
        String sql = buildQuery(projectionIn, selection, selectionArgs, groupBy, having, sortOrder, limit);
        if (Log.isLoggable(TAG, 3)) {
            Log.d(TAG, "Performing query: " + sql);
        }
        return db.rawQueryWithFactory(this.mFactory, sql, selectionArgs, SQLiteDatabase.findEditTable(this.mTables));
    }

    public String buildQuery(String[] projectionIn, String selection, String[] selectionArgs, String groupBy, String having, String sortOrder, String limit) {
        String[] projection = computeProjection(projectionIn);
        StringBuilder where = new StringBuilder();
        boolean hasBaseWhereClause = this.mWhereClause != null && this.mWhereClause.length() > 0;
        if (hasBaseWhereClause) {
            where.append(this.mWhereClause.toString());
            where.append(')');
        }
        if (selection != null && selection.length() > 0) {
            if (hasBaseWhereClause) {
                where.append(" AND ");
            }
            where.append('(');
            where.append(selection);
            where.append(')');
        }
        return buildQueryString(this.mDistinct, this.mTables, projection, where.toString(), groupBy, having, sortOrder, limit);
    }

    public String buildUnionSubQuery(String typeDiscriminatorColumn, String[] unionColumns, Set<String> columnsPresentInTable, int computedColumnsOffset, String typeDiscriminatorValue, String selection, String[] selectionArgs, String groupBy, String having) {
        int unionColumnsCount = unionColumns.length;
        String[] projectionIn = new String[unionColumnsCount];
        for (int i = 0; i < unionColumnsCount; i++) {
            String unionColumn = unionColumns[i];
            if (unionColumn.equals(typeDiscriminatorColumn)) {
                projectionIn[i] = "'" + typeDiscriminatorValue + "' AS " + typeDiscriminatorColumn;
            } else if (i <= computedColumnsOffset || columnsPresentInTable.contains(unionColumn)) {
                projectionIn[i] = unionColumn;
            } else {
                projectionIn[i] = "NULL AS " + unionColumn;
            }
        }
        return buildQuery(projectionIn, selection, selectionArgs, groupBy, having, null, null);
    }

    public String buildUnionQuery(String[] subQueries, String sortOrder, String limit) {
        StringBuilder query = new StringBuilder(128);
        int subQueryCount = subQueries.length;
        String unionOperator = this.mDistinct ? " UNION " : " UNION ALL ";
        for (int i = 0; i < subQueryCount; i++) {
            if (i > 0) {
                query.append(unionOperator);
            }
            query.append(subQueries[i]);
        }
        appendClause(query, " ORDER BY ", sortOrder);
        appendClause(query, " LIMIT ", limit);
        return query.toString();
    }

    private String[] computeProjection(String[] projectionIn) {
        String[] projection;
        int i;
        if (projectionIn == null || projectionIn.length <= 0) {
            if (this.mProjectionMap == null) {
                return null;
            }
            Set<Entry<String, String>> entrySet = this.mProjectionMap.entrySet();
            projection = new String[entrySet.size()];
            i = 0;
            for (Entry<String, String> entry : entrySet) {
                if (!((String) entry.getKey()).equals("_count")) {
                    int i2 = i + 1;
                    projection[i] = (String) entry.getValue();
                    i = i2;
                }
            }
            return projection;
        } else if (this.mProjectionMap == null) {
            return projectionIn;
        } else {
            projection = new String[projectionIn.length];
            int length = projectionIn.length;
            for (i = 0; i < length; i++) {
                String userColumn = projectionIn[i];
                String column = (String) this.mProjectionMap.get(userColumn);
                if (column != null) {
                    projection[i] = column;
                } else if (this.mStrictProjectionMap || !(userColumn.contains(" AS ") || userColumn.contains(" as "))) {
                    throw new IllegalArgumentException("Invalid column " + projectionIn[i]);
                } else {
                    projection[i] = userColumn;
                }
            }
            return projection;
        }
    }
}
