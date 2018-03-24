package org.greenrobot.greendao.query;

import java.util.Date;
import org.greenrobot.greendao.AbstractDao;
import org.greenrobot.greendao.DaoException;
import org.greenrobot.greendao.InternalQueryDaoAccess;

abstract class AbstractQuery<T> {
    protected final AbstractDao<T, ?> dao;
    protected final InternalQueryDaoAccess<T> daoAccess;
    protected final Thread ownerThread = Thread.currentThread();
    protected final String[] parameters;
    protected final String sql;

    protected static String[] toStringArray(Object[] values) {
        int length = values.length;
        String[] strings = new String[length];
        for (int i = 0; i < length; i++) {
            Object object = values[i];
            if (object != null) {
                strings[i] = object.toString();
            } else {
                strings[i] = null;
            }
        }
        return strings;
    }

    protected AbstractQuery(AbstractDao<T, ?> dao, String sql, String[] parameters) {
        this.dao = dao;
        this.daoAccess = new InternalQueryDaoAccess(dao);
        this.sql = sql;
        this.parameters = parameters;
    }

    public AbstractQuery<T> setParameter(int index, Object parameter) {
        checkThread();
        if (parameter != null) {
            this.parameters[index] = parameter.toString();
        } else {
            this.parameters[index] = null;
        }
        return this;
    }

    public AbstractQuery<T> setParameter(int index, Date parameter) {
        return setParameter(index, parameter != null ? Long.valueOf(parameter.getTime()) : null);
    }

    public AbstractQuery<T> setParameter(int index, Boolean parameter) {
        Object converted;
        if (parameter != null) {
            converted = Integer.valueOf(parameter.booleanValue() ? 1 : 0);
        } else {
            converted = null;
        }
        return setParameter(index, converted);
    }

    protected void checkThread() {
        if (Thread.currentThread() != this.ownerThread) {
            throw new DaoException("Method may be called only in owner thread, use forCurrentThread to get an instance for this thread");
        }
    }
}
