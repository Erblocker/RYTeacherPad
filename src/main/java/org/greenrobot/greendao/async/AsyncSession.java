package org.greenrobot.greendao.async;

import java.util.concurrent.Callable;
import org.greenrobot.greendao.AbstractDaoSession;
import org.greenrobot.greendao.async.AsyncOperation.OperationType;
import org.greenrobot.greendao.query.Query;

public class AsyncSession {
    private final AbstractDaoSession daoSession;
    private final AsyncOperationExecutor executor = new AsyncOperationExecutor();
    private int sessionFlags;

    public AsyncSession(AbstractDaoSession daoSession) {
        this.daoSession = daoSession;
    }

    public int getMaxOperationCountToMerge() {
        return this.executor.getMaxOperationCountToMerge();
    }

    public void setMaxOperationCountToMerge(int maxOperationCountToMerge) {
        this.executor.setMaxOperationCountToMerge(maxOperationCountToMerge);
    }

    public int getWaitForMergeMillis() {
        return this.executor.getWaitForMergeMillis();
    }

    public void setWaitForMergeMillis(int waitForMergeMillis) {
        this.executor.setWaitForMergeMillis(waitForMergeMillis);
    }

    public AsyncOperationListener getListener() {
        return this.executor.getListener();
    }

    public void setListener(AsyncOperationListener listener) {
        this.executor.setListener(listener);
    }

    public AsyncOperationListener getListenerMainThread() {
        return this.executor.getListenerMainThread();
    }

    public void setListenerMainThread(AsyncOperationListener listenerMainThread) {
        this.executor.setListenerMainThread(listenerMainThread);
    }

    public boolean isCompleted() {
        return this.executor.isCompleted();
    }

    public void waitForCompletion() {
        this.executor.waitForCompletion();
    }

    public boolean waitForCompletion(int maxMillis) {
        return this.executor.waitForCompletion(maxMillis);
    }

    public AsyncOperation insert(Object entity) {
        return insert(entity, 0);
    }

    public AsyncOperation insert(Object entity, int flags) {
        return enqueueEntityOperation(OperationType.Insert, entity, flags);
    }

    public <E> AsyncOperation insertInTx(Class<E> entityClass, E... entities) {
        return insertInTx((Class) entityClass, 0, (Object[]) entities);
    }

    public <E> AsyncOperation insertInTx(Class<E> entityClass, int flags, E... entities) {
        return enqueEntityOperation(OperationType.InsertInTxArray, entityClass, entities, flags);
    }

    public <E> AsyncOperation insertInTx(Class<E> entityClass, Iterable<E> entities) {
        return insertInTx((Class) entityClass, (Iterable) entities, 0);
    }

    public <E> AsyncOperation insertInTx(Class<E> entityClass, Iterable<E> entities, int flags) {
        return enqueEntityOperation(OperationType.InsertInTxIterable, entityClass, entities, flags);
    }

    public AsyncOperation insertOrReplace(Object entity) {
        return insertOrReplace(entity, 0);
    }

    public AsyncOperation insertOrReplace(Object entity, int flags) {
        return enqueueEntityOperation(OperationType.InsertOrReplace, entity, flags);
    }

    public <E> AsyncOperation insertOrReplaceInTx(Class<E> entityClass, E... entities) {
        return insertOrReplaceInTx((Class) entityClass, 0, (Object[]) entities);
    }

    public <E> AsyncOperation insertOrReplaceInTx(Class<E> entityClass, int flags, E... entities) {
        return enqueEntityOperation(OperationType.InsertOrReplaceInTxArray, entityClass, entities, flags);
    }

    public <E> AsyncOperation insertOrReplaceInTx(Class<E> entityClass, Iterable<E> entities) {
        return insertOrReplaceInTx((Class) entityClass, (Iterable) entities, 0);
    }

    public <E> AsyncOperation insertOrReplaceInTx(Class<E> entityClass, Iterable<E> entities, int flags) {
        return enqueEntityOperation(OperationType.InsertOrReplaceInTxIterable, entityClass, entities, flags);
    }

    public AsyncOperation update(Object entity) {
        return update(entity, 0);
    }

    public AsyncOperation update(Object entity, int flags) {
        return enqueueEntityOperation(OperationType.Update, entity, flags);
    }

    public <E> AsyncOperation updateInTx(Class<E> entityClass, E... entities) {
        return updateInTx((Class) entityClass, 0, (Object[]) entities);
    }

    public <E> AsyncOperation updateInTx(Class<E> entityClass, int flags, E... entities) {
        return enqueEntityOperation(OperationType.UpdateInTxArray, entityClass, entities, flags);
    }

    public <E> AsyncOperation updateInTx(Class<E> entityClass, Iterable<E> entities) {
        return updateInTx((Class) entityClass, (Iterable) entities, 0);
    }

    public <E> AsyncOperation updateInTx(Class<E> entityClass, Iterable<E> entities, int flags) {
        return enqueEntityOperation(OperationType.UpdateInTxIterable, entityClass, entities, flags);
    }

    public AsyncOperation delete(Object entity) {
        return delete(entity, 0);
    }

    public AsyncOperation delete(Object entity, int flags) {
        return enqueueEntityOperation(OperationType.Delete, entity, flags);
    }

    public AsyncOperation deleteByKey(Object key) {
        return deleteByKey(key, 0);
    }

    public AsyncOperation deleteByKey(Object key, int flags) {
        return enqueueEntityOperation(OperationType.DeleteByKey, key, flags);
    }

    public <E> AsyncOperation deleteInTx(Class<E> entityClass, E... entities) {
        return deleteInTx((Class) entityClass, 0, (Object[]) entities);
    }

    public <E> AsyncOperation deleteInTx(Class<E> entityClass, int flags, E... entities) {
        return enqueEntityOperation(OperationType.DeleteInTxArray, entityClass, entities, flags);
    }

    public <E> AsyncOperation deleteInTx(Class<E> entityClass, Iterable<E> entities) {
        return deleteInTx((Class) entityClass, (Iterable) entities, 0);
    }

    public <E> AsyncOperation deleteInTx(Class<E> entityClass, Iterable<E> entities, int flags) {
        return enqueEntityOperation(OperationType.DeleteInTxIterable, entityClass, entities, flags);
    }

    public <E> AsyncOperation deleteAll(Class<E> entityClass) {
        return deleteAll(entityClass, 0);
    }

    public <E> AsyncOperation deleteAll(Class<E> entityClass, int flags) {
        return enqueEntityOperation(OperationType.DeleteAll, entityClass, null, flags);
    }

    public AsyncOperation runInTx(Runnable runnable) {
        return runInTx(runnable, 0);
    }

    public AsyncOperation runInTx(Runnable runnable, int flags) {
        return enqueueDatabaseOperation(OperationType.TransactionRunnable, runnable, flags);
    }

    public AsyncOperation callInTx(Callable<?> callable) {
        return callInTx(callable, 0);
    }

    public AsyncOperation callInTx(Callable<?> callable, int flags) {
        return enqueueDatabaseOperation(OperationType.TransactionCallable, callable, flags);
    }

    public AsyncOperation queryList(Query<?> query) {
        return queryList(query, 0);
    }

    public AsyncOperation queryList(Query<?> query, int flags) {
        return enqueueDatabaseOperation(OperationType.QueryList, query, flags);
    }

    public AsyncOperation queryUnique(Query<?> query) {
        return queryUnique(query, 0);
    }

    public AsyncOperation queryUnique(Query<?> query, int flags) {
        return enqueueDatabaseOperation(OperationType.QueryUnique, query, flags);
    }

    public AsyncOperation load(Class<?> entityClass, Object key) {
        return load(entityClass, key, 0);
    }

    public AsyncOperation load(Class<?> entityClass, Object key, int flags) {
        return enqueEntityOperation(OperationType.Load, entityClass, key, flags);
    }

    public AsyncOperation loadAll(Class<?> entityClass) {
        return loadAll(entityClass, 0);
    }

    public AsyncOperation loadAll(Class<?> entityClass, int flags) {
        return enqueEntityOperation(OperationType.LoadAll, entityClass, null, flags);
    }

    public AsyncOperation count(Class<?> entityClass) {
        return count(entityClass, 0);
    }

    public AsyncOperation count(Class<?> entityClass, int flags) {
        return enqueEntityOperation(OperationType.Count, entityClass, null, flags);
    }

    public AsyncOperation refresh(Object entity) {
        return refresh(entity, 0);
    }

    public AsyncOperation refresh(Object entity, int flags) {
        return enqueueEntityOperation(OperationType.Refresh, entity, flags);
    }

    private AsyncOperation enqueueDatabaseOperation(OperationType type, Object param, int flags) {
        AsyncOperation operation = new AsyncOperation(type, null, this.daoSession.getDatabase(), param, flags | this.sessionFlags);
        this.executor.enqueue(operation);
        return operation;
    }

    private AsyncOperation enqueueEntityOperation(OperationType type, Object entity, int flags) {
        return enqueEntityOperation(type, entity.getClass(), entity, flags);
    }

    private <E> AsyncOperation enqueEntityOperation(OperationType type, Class<E> entityClass, Object param, int flags) {
        AsyncOperation operation = new AsyncOperation(type, this.daoSession.getDao(entityClass), null, param, flags | this.sessionFlags);
        this.executor.enqueue(operation);
        return operation;
    }

    public int getSessionFlags() {
        return this.sessionFlags;
    }

    public void setSessionFlags(int sessionFlags) {
        this.sessionFlags = sessionFlags;
    }
}
