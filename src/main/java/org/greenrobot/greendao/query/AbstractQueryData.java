package org.greenrobot.greendao.query;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import org.greenrobot.greendao.AbstractDao;

abstract class AbstractQueryData<T, Q extends AbstractQuery<T>> {
    final AbstractDao<T, ?> dao;
    final String[] initialValues;
    final Map<Long, WeakReference<Q>> queriesForThreads = new HashMap();
    final String sql;

    protected abstract Q createQuery();

    AbstractQueryData(AbstractDao<T, ?> dao, String sql, String[] initialValues) {
        this.dao = dao;
        this.sql = sql;
        this.initialValues = initialValues;
    }

    Q forCurrentThread(Q query) {
        if (Thread.currentThread() != query.ownerThread) {
            return forCurrentThread();
        }
        System.arraycopy(this.initialValues, 0, query.parameters, 0, this.initialValues.length);
        return query;
    }

    Q forCurrentThread() {
        Q query;
        long threadId = Thread.currentThread().getId();
        synchronized (this.queriesForThreads) {
            WeakReference<Q> queryRef = (WeakReference) this.queriesForThreads.get(Long.valueOf(threadId));
            query = queryRef != null ? (AbstractQuery) queryRef.get() : null;
            if (query == null) {
                gc();
                query = createQuery();
                this.queriesForThreads.put(Long.valueOf(threadId), new WeakReference(query));
            } else {
                System.arraycopy(this.initialValues, 0, query.parameters, 0, this.initialValues.length);
            }
        }
        return query;
    }

    void gc() {
        synchronized (this.queriesForThreads) {
            Iterator<Entry<Long, WeakReference<Q>>> iterator = this.queriesForThreads.entrySet().iterator();
            while (iterator.hasNext()) {
                if (((WeakReference) ((Entry) iterator.next()).getValue()).get() == null) {
                    iterator.remove();
                }
            }
        }
    }
}
