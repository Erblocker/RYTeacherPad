package org.greenrobot.greendao.query;

import java.util.Date;
import java.util.List;
import org.greenrobot.greendao.AbstractDao;
import org.greenrobot.greendao.DaoException;
import org.greenrobot.greendao.annotation.apihint.Internal;
import org.greenrobot.greendao.rx.RxQuery;
import rx.schedulers.Schedulers;

public class Query<T> extends AbstractQueryWithLimit<T> {
    private final QueryData<T> queryData;
    private volatile RxQuery rxTxIo;
    private volatile RxQuery rxTxPlain;

    private static final class QueryData<T2> extends AbstractQueryData<T2, Query<T2>> {
        private final int limitPosition;
        private final int offsetPosition;

        QueryData(AbstractDao<T2, ?> dao, String sql, String[] initialValues, int limitPosition, int offsetPosition) {
            super(dao, sql, initialValues);
            this.limitPosition = limitPosition;
            this.offsetPosition = offsetPosition;
        }

        protected Query<T2> createQuery() {
            return new Query(this, this.dao, this.sql, (String[]) this.initialValues.clone(), this.limitPosition, this.offsetPosition);
        }
    }

    public /* bridge */ /* synthetic */ void setLimit(int i) {
        super.setLimit(i);
    }

    public /* bridge */ /* synthetic */ void setOffset(int i) {
        super.setOffset(i);
    }

    public static <T2> Query<T2> internalCreate(AbstractDao<T2, ?> dao, String sql, Object[] initialValues) {
        return create(dao, sql, initialValues, -1, -1);
    }

    static <T2> Query<T2> create(AbstractDao<T2, ?> dao, String sql, Object[] initialValues, int limitPosition, int offsetPosition) {
        return (Query) new QueryData(dao, sql, AbstractQuery.toStringArray(initialValues), limitPosition, offsetPosition).forCurrentThread();
    }

    private Query(QueryData<T> queryData, AbstractDao<T, ?> dao, String sql, String[] initialValues, int limitPosition, int offsetPosition) {
        super(dao, sql, initialValues, limitPosition, offsetPosition);
        this.queryData = queryData;
    }

    public Query<T> forCurrentThread() {
        return (Query) this.queryData.forCurrentThread(this);
    }

    public List<T> list() {
        checkThread();
        return this.daoAccess.loadAllAndCloseCursor(this.dao.getDatabase().rawQuery(this.sql, this.parameters));
    }

    public LazyList<T> listLazy() {
        checkThread();
        return new LazyList(this.daoAccess, this.dao.getDatabase().rawQuery(this.sql, this.parameters), true);
    }

    public LazyList<T> listLazyUncached() {
        checkThread();
        return new LazyList(this.daoAccess, this.dao.getDatabase().rawQuery(this.sql, this.parameters), false);
    }

    public CloseableListIterator<T> listIterator() {
        return listLazyUncached().listIteratorAutoClose();
    }

    public T unique() {
        checkThread();
        return this.daoAccess.loadUniqueAndCloseCursor(this.dao.getDatabase().rawQuery(this.sql, this.parameters));
    }

    public T uniqueOrThrow() {
        T entity = unique();
        if (entity != null) {
            return entity;
        }
        throw new DaoException("No entity found for query");
    }

    public Query<T> setParameter(int index, Object parameter) {
        return (Query) super.setParameter(index, parameter);
    }

    public Query<T> setParameter(int index, Date parameter) {
        return (Query) super.setParameter(index, parameter);
    }

    public Query<T> setParameter(int index, Boolean parameter) {
        return (Query) super.setParameter(index, parameter);
    }

    @Internal
    public RxQuery __internalRxPlain() {
        if (this.rxTxPlain == null) {
            this.rxTxPlain = new RxQuery(this);
        }
        return this.rxTxPlain;
    }

    @Internal
    public RxQuery __InternalRx() {
        if (this.rxTxIo == null) {
            this.rxTxIo = new RxQuery(this, Schedulers.io());
        }
        return this.rxTxIo;
    }
}
