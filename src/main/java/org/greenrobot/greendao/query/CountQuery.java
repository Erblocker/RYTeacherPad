package org.greenrobot.greendao.query;

import android.database.Cursor;
import java.util.Date;
import org.greenrobot.greendao.AbstractDao;
import org.greenrobot.greendao.DaoException;
import org.greenrobot.greendao.database.Database;

public class CountQuery<T> extends AbstractQuery<T> {
    private final QueryData<T> queryData;

    private static final class QueryData<T2> extends AbstractQueryData<T2, CountQuery<T2>> {
        private QueryData(AbstractDao<T2, ?> dao, String sql, String[] initialValues) {
            super(dao, sql, initialValues);
        }

        protected CountQuery<T2> createQuery() {
            return new CountQuery(this, this.dao, this.sql, (String[]) this.initialValues.clone());
        }
    }

    static <T2> CountQuery<T2> create(AbstractDao<T2, ?> dao, String sql, Object[] initialValues) {
        return (CountQuery) new QueryData(dao, sql, AbstractQuery.toStringArray(initialValues)).forCurrentThread();
    }

    private CountQuery(QueryData<T> queryData, AbstractDao<T, ?> dao, String sql, String[] initialValues) {
        super(dao, sql, initialValues);
        this.queryData = queryData;
    }

    public CountQuery<T> forCurrentThread() {
        return (CountQuery) this.queryData.forCurrentThread(this);
    }

    public long count() {
        checkThread();
        Database database = this.dao.getDatabase();
        long j = this.sql;
        Cursor cursor = database.rawQuery(j, this.parameters);
        try {
            if (!cursor.moveToNext()) {
                throw new DaoException("No result for count");
            } else if (!cursor.isLast()) {
                throw new DaoException("Unexpected row count: " + cursor.getCount());
            } else if (cursor.getColumnCount() != 1) {
                throw new DaoException("Unexpected column count: " + cursor.getColumnCount());
            } else {
                j = cursor.getLong(0);
                return j;
            }
        } finally {
            cursor.close();
        }
    }

    public CountQuery<T> setParameter(int index, Object parameter) {
        return (CountQuery) super.setParameter(index, parameter);
    }

    public CountQuery<T> setParameter(int index, Date parameter) {
        return (CountQuery) super.setParameter(index, parameter);
    }

    public CountQuery<T> setParameter(int index, Boolean parameter) {
        return (CountQuery) super.setParameter(index, parameter);
    }
}
