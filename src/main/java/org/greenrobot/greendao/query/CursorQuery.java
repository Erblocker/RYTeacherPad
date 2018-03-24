package org.greenrobot.greendao.query;

import android.database.Cursor;
import java.util.Date;
import org.greenrobot.greendao.AbstractDao;

public class CursorQuery<T> extends AbstractQueryWithLimit<T> {
    private final QueryData<T> queryData;

    private static final class QueryData<T2> extends AbstractQueryData<T2, CursorQuery<T2>> {
        private final int limitPosition;
        private final int offsetPosition;

        QueryData(AbstractDao dao, String sql, String[] initialValues, int limitPosition, int offsetPosition) {
            super(dao, sql, initialValues);
            this.limitPosition = limitPosition;
            this.offsetPosition = offsetPosition;
        }

        protected CursorQuery<T2> createQuery() {
            return new CursorQuery(this, this.dao, this.sql, (String[]) this.initialValues.clone(), this.limitPosition, this.offsetPosition);
        }
    }

    public /* bridge */ /* synthetic */ void setLimit(int i) {
        super.setLimit(i);
    }

    public /* bridge */ /* synthetic */ void setOffset(int i) {
        super.setOffset(i);
    }

    public static <T2> CursorQuery<T2> internalCreate(AbstractDao<T2, ?> dao, String sql, Object[] initialValues) {
        return create(dao, sql, initialValues, -1, -1);
    }

    static <T2> CursorQuery<T2> create(AbstractDao<T2, ?> dao, String sql, Object[] initialValues, int limitPosition, int offsetPosition) {
        return (CursorQuery) new QueryData(dao, sql, AbstractQuery.toStringArray(initialValues), limitPosition, offsetPosition).forCurrentThread();
    }

    private CursorQuery(QueryData<T> queryData, AbstractDao<T, ?> dao, String sql, String[] initialValues, int limitPosition, int offsetPosition) {
        super(dao, sql, initialValues, limitPosition, offsetPosition);
        this.queryData = queryData;
    }

    public CursorQuery forCurrentThread() {
        return (CursorQuery) this.queryData.forCurrentThread(this);
    }

    public Cursor query() {
        checkThread();
        return this.dao.getDatabase().rawQuery(this.sql, this.parameters);
    }

    public CursorQuery<T> setParameter(int index, Object parameter) {
        return (CursorQuery) super.setParameter(index, parameter);
    }

    public CursorQuery<T> setParameter(int index, Date parameter) {
        return (CursorQuery) super.setParameter(index, parameter);
    }

    public CursorQuery<T> setParameter(int index, Boolean parameter) {
        return (CursorQuery) super.setParameter(index, parameter);
    }
}
