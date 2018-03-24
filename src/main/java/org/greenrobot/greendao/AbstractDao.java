package org.greenrobot.greendao;

import android.database.CrossProcessCursor;
import android.database.Cursor;
import android.database.CursorWindow;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import org.greenrobot.greendao.annotation.apihint.Experimental;
import org.greenrobot.greendao.database.Database;
import org.greenrobot.greendao.database.DatabaseStatement;
import org.greenrobot.greendao.identityscope.IdentityScope;
import org.greenrobot.greendao.identityscope.IdentityScopeLong;
import org.greenrobot.greendao.internal.DaoConfig;
import org.greenrobot.greendao.internal.FastCursor;
import org.greenrobot.greendao.internal.TableStatements;
import org.greenrobot.greendao.query.Query;
import org.greenrobot.greendao.query.QueryBuilder;
import org.greenrobot.greendao.rx.RxDao;
import rx.schedulers.Schedulers;

public abstract class AbstractDao<T, K> {
    protected final DaoConfig config;
    protected final Database db;
    protected final IdentityScope<K, T> identityScope;
    protected final IdentityScopeLong<T> identityScopeLong;
    protected final boolean isStandardSQLite;
    protected final int pkOrdinal;
    private volatile RxDao<T, K> rxDao;
    private volatile RxDao<T, K> rxDaoPlain;
    protected final AbstractDaoSession session;
    protected final TableStatements statements;

    protected abstract void bindValues(SQLiteStatement sQLiteStatement, T t);

    protected abstract void bindValues(DatabaseStatement databaseStatement, T t);

    protected abstract K getKey(T t);

    protected abstract boolean hasKey(T t);

    protected abstract boolean isEntityUpdateable();

    protected abstract T readEntity(Cursor cursor, int i);

    protected abstract void readEntity(Cursor cursor, T t, int i);

    protected abstract K readKey(Cursor cursor, int i);

    protected abstract K updateKeyAfterInsert(T t, long j);

    public AbstractDao(DaoConfig config) {
        this(config, null);
    }

    public AbstractDao(DaoConfig config, AbstractDaoSession daoSession) {
        this.config = config;
        this.session = daoSession;
        this.db = config.db;
        this.isStandardSQLite = this.db.getRawDatabase() instanceof SQLiteDatabase;
        this.identityScope = config.getIdentityScope();
        if (this.identityScope instanceof IdentityScopeLong) {
            this.identityScopeLong = (IdentityScopeLong) this.identityScope;
        } else {
            this.identityScopeLong = null;
        }
        this.statements = config.statements;
        this.pkOrdinal = config.pkProperty != null ? config.pkProperty.ordinal : -1;
    }

    public AbstractDaoSession getSession() {
        return this.session;
    }

    TableStatements getStatements() {
        return this.config.statements;
    }

    public String getTablename() {
        return this.config.tablename;
    }

    public Property[] getProperties() {
        return this.config.properties;
    }

    public Property getPkProperty() {
        return this.config.pkProperty;
    }

    public String[] getAllColumns() {
        return this.config.allColumns;
    }

    public String[] getPkColumns() {
        return this.config.pkColumns;
    }

    public String[] getNonPkColumns() {
        return this.config.nonPkColumns;
    }

    public T load(K key) {
        assertSinglePk();
        if (key == null) {
            return null;
        }
        if (this.identityScope != null) {
            T entity = this.identityScope.get(key);
            if (entity != null) {
                return entity;
            }
        }
        return loadUniqueAndCloseCursor(this.db.rawQuery(this.statements.getSelectByKey(), new String[]{key.toString()}));
    }

    public T loadByRowId(long rowId) {
        return loadUniqueAndCloseCursor(this.db.rawQuery(this.statements.getSelectByRowId(), new String[]{Long.toString(rowId)}));
    }

    protected T loadUniqueAndCloseCursor(Cursor cursor) {
        try {
            T loadUnique = loadUnique(cursor);
            return loadUnique;
        } finally {
            cursor.close();
        }
    }

    protected T loadUnique(Cursor cursor) {
        if (!cursor.moveToFirst()) {
            return null;
        }
        if (cursor.isLast()) {
            return loadCurrent(cursor, 0, true);
        }
        throw new DaoException("Expected unique result, but count was " + cursor.getCount());
    }

    public List<T> loadAll() {
        return loadAllAndCloseCursor(this.db.rawQuery(this.statements.getSelectAll(), null));
    }

    public boolean detach(T entity) {
        if (this.identityScope == null) {
            return false;
        }
        return this.identityScope.detach(getKeyVerified(entity), entity);
    }

    public void detachAll() {
        if (this.identityScope != null) {
            this.identityScope.clear();
        }
    }

    protected List<T> loadAllAndCloseCursor(Cursor cursor) {
        try {
            List<T> loadAllFromCursor = loadAllFromCursor(cursor);
            return loadAllFromCursor;
        } finally {
            cursor.close();
        }
    }

    public void insertInTx(Iterable<T> entities) {
        insertInTx(entities, isEntityUpdateable());
    }

    public void insertInTx(T... entities) {
        insertInTx(Arrays.asList(entities), isEntityUpdateable());
    }

    public void insertInTx(Iterable<T> entities, boolean setPrimaryKey) {
        executeInsertInTx(this.statements.getInsertStatement(), entities, setPrimaryKey);
    }

    public void insertOrReplaceInTx(Iterable<T> entities, boolean setPrimaryKey) {
        executeInsertInTx(this.statements.getInsertOrReplaceStatement(), entities, setPrimaryKey);
    }

    public void insertOrReplaceInTx(Iterable<T> entities) {
        insertOrReplaceInTx(entities, isEntityUpdateable());
    }

    public void insertOrReplaceInTx(T... entities) {
        insertOrReplaceInTx(Arrays.asList(entities), isEntityUpdateable());
    }

    private void executeInsertInTx(DatabaseStatement stmt, Iterable<T> entities, boolean setPrimaryKey) {
        this.db.beginTransaction();
        try {
            synchronized (stmt) {
                if (this.identityScope != null) {
                    this.identityScope.lock();
                }
                try {
                    if (this.isStandardSQLite) {
                        SQLiteStatement rawStmt = (SQLiteStatement) stmt.getRawStatement();
                        for (Object entity : entities) {
                            bindValues(rawStmt, entity);
                            if (setPrimaryKey) {
                                updateKeyAfterInsertAndAttach(entity, rawStmt.executeInsert(), false);
                            } else {
                                rawStmt.execute();
                            }
                        }
                    } else {
                        for (Object entity2 : entities) {
                            bindValues(stmt, entity2);
                            if (setPrimaryKey) {
                                updateKeyAfterInsertAndAttach(entity2, stmt.executeInsert(), false);
                            } else {
                                stmt.execute();
                            }
                        }
                    }
                    if (this.identityScope != null) {
                        this.identityScope.unlock();
                    }
                } catch (Throwable th) {
                    if (this.identityScope != null) {
                        this.identityScope.unlock();
                    }
                }
            }
            this.db.setTransactionSuccessful();
        } finally {
            this.db.endTransaction();
        }
    }

    public long insert(T entity) {
        return executeInsert(entity, this.statements.getInsertStatement(), true);
    }

    public long insertWithoutSettingPk(T entity) {
        return executeInsert(entity, this.statements.getInsertOrReplaceStatement(), false);
    }

    public long insertOrReplace(T entity) {
        return executeInsert(entity, this.statements.getInsertOrReplaceStatement(), true);
    }

    private long executeInsert(T entity, DatabaseStatement stmt, boolean setKeyAndAttach) {
        long rowId;
        if (this.db.isDbLockedByCurrentThread()) {
            rowId = insertInsideTx(entity, stmt);
        } else {
            this.db.beginTransaction();
            try {
                rowId = insertInsideTx(entity, stmt);
                this.db.setTransactionSuccessful();
            } finally {
                this.db.endTransaction();
            }
        }
        if (setKeyAndAttach) {
            updateKeyAfterInsertAndAttach(entity, rowId, true);
        }
        return rowId;
    }

    private long insertInsideTx(T entity, DatabaseStatement stmt) {
        long executeInsert;
        synchronized (stmt) {
            if (this.isStandardSQLite) {
                SQLiteStatement rawStmt = (SQLiteStatement) stmt.getRawStatement();
                bindValues(rawStmt, (Object) entity);
                executeInsert = rawStmt.executeInsert();
            } else {
                bindValues(stmt, (Object) entity);
                executeInsert = stmt.executeInsert();
            }
        }
        return executeInsert;
    }

    protected void updateKeyAfterInsertAndAttach(T entity, long rowId, boolean lock) {
        if (rowId != -1) {
            attachEntity(updateKeyAfterInsert(entity, rowId), entity, lock);
        } else {
            DaoLog.w("Could not insert row (executeInsert returned -1)");
        }
    }

    public void save(T entity) {
        if (hasKey(entity)) {
            update(entity);
        } else {
            insert(entity);
        }
    }

    public void saveInTx(T... entities) {
        saveInTx(Arrays.asList(entities));
    }

    public void saveInTx(Iterable<T> entities) {
        int updateCount = 0;
        int insertCount = 0;
        for (T entity : entities) {
            if (hasKey(entity)) {
                updateCount++;
            } else {
                insertCount++;
            }
        }
        if (updateCount > 0 && insertCount > 0) {
            Iterable toUpdate = new ArrayList(updateCount);
            Iterable toInsert = new ArrayList(insertCount);
            for (T entity2 : entities) {
                if (hasKey(entity2)) {
                    toUpdate.add(entity2);
                } else {
                    toInsert.add(entity2);
                }
            }
            this.db.beginTransaction();
            try {
                updateInTx(toUpdate);
                insertInTx(toInsert);
                this.db.setTransactionSuccessful();
            } finally {
                this.db.endTransaction();
            }
        } else if (insertCount > 0) {
            insertInTx((Iterable) entities);
        } else if (updateCount > 0) {
            updateInTx((Iterable) entities);
        }
    }

    protected List<T> loadAllFromCursor(Cursor cursor) {
        int count = cursor.getCount();
        if (count == 0) {
            return new ArrayList();
        }
        List<T> list = new ArrayList(count);
        CursorWindow window = null;
        boolean useFastCursor = false;
        if (cursor instanceof CrossProcessCursor) {
            window = ((CrossProcessCursor) cursor).getWindow();
            if (window != null) {
                if (window.getNumRows() == count) {
                    cursor = new FastCursor(window);
                    useFastCursor = true;
                } else {
                    DaoLog.d("Window vs. result size: " + window.getNumRows() + "/" + count);
                }
            }
        }
        if (!cursor.moveToFirst()) {
            return list;
        }
        if (this.identityScope != null) {
            this.identityScope.lock();
            this.identityScope.reserveRoom(count);
        }
        if (!(useFastCursor || window == null)) {
            try {
                if (this.identityScope != null) {
                    loadAllUnlockOnWindowBounds(cursor, window, list);
                    if (this.identityScope != null) {
                        return list;
                    }
                    this.identityScope.unlock();
                    return list;
                }
            } catch (Throwable th) {
                if (this.identityScope != null) {
                    this.identityScope.unlock();
                }
            }
        }
        do {
            list.add(loadCurrent(cursor, 0, false));
        } while (cursor.moveToNext());
        if (this.identityScope != null) {
            return list;
        }
        this.identityScope.unlock();
        return list;
    }

    private void loadAllUnlockOnWindowBounds(Cursor cursor, CursorWindow window, List<T> list) {
        int windowEnd = window.getStartPosition() + window.getNumRows();
        int row = 0;
        while (true) {
            list.add(loadCurrent(cursor, 0, false));
            row++;
            if (row >= windowEnd) {
                window = moveToNextUnlocked(cursor);
                if (window != null) {
                    windowEnd = window.getStartPosition() + window.getNumRows();
                } else {
                    return;
                }
            } else if (!cursor.moveToNext()) {
                return;
            }
            row++;
        }
    }

    private CursorWindow moveToNextUnlocked(Cursor cursor) {
        this.identityScope.unlock();
        try {
            if (cursor.moveToNext()) {
                CursorWindow window = ((CrossProcessCursor) cursor).getWindow();
                return window;
            }
            this.identityScope.lock();
            return null;
        } finally {
            this.identityScope.lock();
        }
    }

    protected final T loadCurrent(Cursor cursor, int offset, boolean lock) {
        T entity;
        if (this.identityScopeLong != null) {
            if (offset != 0 && cursor.isNull(this.pkOrdinal + offset)) {
                return null;
            }
            long key = cursor.getLong(this.pkOrdinal + offset);
            entity = lock ? this.identityScopeLong.get2(key) : this.identityScopeLong.get2NoLock(key);
            if (entity != null) {
                return entity;
            }
            entity = readEntity(cursor, offset);
            attachEntity(entity);
            if (lock) {
                this.identityScopeLong.put2(key, entity);
                return entity;
            }
            this.identityScopeLong.put2NoLock(key, entity);
            return entity;
        } else if (this.identityScope != null) {
            K key2 = readKey(cursor, offset);
            if (offset != 0 && key2 == null) {
                return null;
            }
            entity = lock ? this.identityScope.get(key2) : this.identityScope.getNoLock(key2);
            if (entity != null) {
                return entity;
            }
            entity = readEntity(cursor, offset);
            attachEntity(key2, entity, lock);
            return entity;
        } else if (offset != 0 && readKey(cursor, offset) == null) {
            return null;
        } else {
            entity = readEntity(cursor, offset);
            attachEntity(entity);
            return entity;
        }
    }

    protected final <O> O loadCurrentOther(AbstractDao<O, ?> dao, Cursor cursor, int offset) {
        return dao.loadCurrent(cursor, offset, true);
    }

    public List<T> queryRaw(String where, String... selectionArg) {
        return loadAllAndCloseCursor(this.db.rawQuery(this.statements.getSelectAll() + where, selectionArg));
    }

    public Query<T> queryRawCreate(String where, Object... selectionArg) {
        return queryRawCreateListArgs(where, Arrays.asList(selectionArg));
    }

    public Query<T> queryRawCreateListArgs(String where, Collection<Object> selectionArg) {
        return Query.internalCreate(this, this.statements.getSelectAll() + where, selectionArg.toArray());
    }

    public void deleteAll() {
        this.db.execSQL("DELETE FROM '" + this.config.tablename + "'");
        if (this.identityScope != null) {
            this.identityScope.clear();
        }
    }

    public void delete(T entity) {
        assertSinglePk();
        deleteByKey(getKeyVerified(entity));
    }

    public void deleteByKey(K key) {
        assertSinglePk();
        DatabaseStatement stmt = this.statements.getDeleteStatement();
        if (this.db.isDbLockedByCurrentThread()) {
            synchronized (stmt) {
                deleteByKeyInsideSynchronized(key, stmt);
            }
        } else {
            this.db.beginTransaction();
            try {
                synchronized (stmt) {
                    deleteByKeyInsideSynchronized(key, stmt);
                }
                this.db.setTransactionSuccessful();
            } finally {
                this.db.endTransaction();
            }
        }
        if (this.identityScope != null) {
            this.identityScope.remove((Object) key);
        }
    }

    private void deleteByKeyInsideSynchronized(K key, DatabaseStatement stmt) {
        if (key instanceof Long) {
            stmt.bindLong(1, ((Long) key).longValue());
        } else if (key == null) {
            throw new DaoException("Cannot delete entity, key is null");
        } else {
            stmt.bindString(1, key.toString());
        }
        stmt.execute();
    }

    private void deleteInTxInternal(Iterable<T> entities, Iterable<K> keys) {
        assertSinglePk();
        DatabaseStatement stmt = this.statements.getDeleteStatement();
        Iterable keysToRemoveFromIdentityScope = null;
        this.db.beginTransaction();
        try {
            synchronized (stmt) {
                K key;
                if (this.identityScope != null) {
                    this.identityScope.lock();
                    keysToRemoveFromIdentityScope = new ArrayList();
                }
                if (entities != null) {
                    try {
                        for (T entity : entities) {
                            key = getKeyVerified(entity);
                            deleteByKeyInsideSynchronized(key, stmt);
                            if (keysToRemoveFromIdentityScope != null) {
                                keysToRemoveFromIdentityScope.add(key);
                            }
                        }
                    } catch (Throwable th) {
                        if (this.identityScope != null) {
                            this.identityScope.unlock();
                        }
                    }
                }
                if (keys != null) {
                    for (K key2 : keys) {
                        deleteByKeyInsideSynchronized(key2, stmt);
                        if (keysToRemoveFromIdentityScope != null) {
                            keysToRemoveFromIdentityScope.add(key2);
                        }
                    }
                }
                if (this.identityScope != null) {
                    this.identityScope.unlock();
                }
            }
            this.db.setTransactionSuccessful();
            if (!(keysToRemoveFromIdentityScope == null || this.identityScope == null)) {
                this.identityScope.remove(keysToRemoveFromIdentityScope);
            }
            this.db.endTransaction();
        } catch (Throwable th2) {
            this.db.endTransaction();
        }
    }

    public void deleteInTx(Iterable<T> entities) {
        deleteInTxInternal(entities, null);
    }

    public void deleteInTx(T... entities) {
        deleteInTxInternal(Arrays.asList(entities), null);
    }

    public void deleteByKeyInTx(Iterable<K> keys) {
        deleteInTxInternal(null, keys);
    }

    public void deleteByKeyInTx(K... keys) {
        deleteInTxInternal(null, Arrays.asList(keys));
    }

    public void refresh(T entity) {
        assertSinglePk();
        K key = getKeyVerified(entity);
        Cursor cursor = this.db.rawQuery(this.statements.getSelectByKey(), new String[]{key.toString()});
        try {
            if (!cursor.moveToFirst()) {
                throw new DaoException("Entity does not exist in the database anymore: " + entity.getClass() + " with key " + key);
            } else if (cursor.isLast()) {
                readEntity(cursor, entity, 0);
                attachEntity(key, entity, true);
            } else {
                throw new DaoException("Expected unique result, but count was " + cursor.getCount());
            }
        } finally {
            cursor.close();
        }
    }

    public void update(T entity) {
        assertSinglePk();
        DatabaseStatement stmt = this.statements.getUpdateStatement();
        if (this.db.isDbLockedByCurrentThread()) {
            synchronized (stmt) {
                if (this.isStandardSQLite) {
                    updateInsideSynchronized((Object) entity, (SQLiteStatement) stmt.getRawStatement(), true);
                } else {
                    updateInsideSynchronized((Object) entity, stmt, true);
                }
            }
            return;
        }
        this.db.beginTransaction();
        try {
            synchronized (stmt) {
                updateInsideSynchronized((Object) entity, stmt, true);
            }
            this.db.setTransactionSuccessful();
        } finally {
            this.db.endTransaction();
        }
    }

    public QueryBuilder<T> queryBuilder() {
        return QueryBuilder.internalCreate(this);
    }

    protected void updateInsideSynchronized(T entity, DatabaseStatement stmt, boolean lock) {
        bindValues(stmt, (Object) entity);
        int index = this.config.allColumns.length + 1;
        K key = getKey(entity);
        if (key instanceof Long) {
            stmt.bindLong(index, ((Long) key).longValue());
        } else if (key == null) {
            throw new DaoException("Cannot update entity without key - was it inserted before?");
        } else {
            stmt.bindString(index, key.toString());
        }
        stmt.execute();
        attachEntity(key, entity, lock);
    }

    protected void updateInsideSynchronized(T entity, SQLiteStatement stmt, boolean lock) {
        bindValues(stmt, (Object) entity);
        int index = this.config.allColumns.length + 1;
        K key = getKey(entity);
        if (key instanceof Long) {
            stmt.bindLong(index, ((Long) key).longValue());
        } else if (key == null) {
            throw new DaoException("Cannot update entity without key - was it inserted before?");
        } else {
            stmt.bindString(index, key.toString());
        }
        stmt.execute();
        attachEntity(key, entity, lock);
    }

    protected final void attachEntity(K key, T entity, boolean lock) {
        attachEntity(entity);
        if (this.identityScope != null && key != null) {
            if (lock) {
                this.identityScope.put(key, entity);
            } else {
                this.identityScope.putNoLock(key, entity);
            }
        }
    }

    protected void attachEntity(T t) {
    }

    public void updateInTx(Iterable<T> entities) {
        DatabaseStatement stmt = this.statements.getUpdateStatement();
        this.db.beginTransaction();
        RuntimeException txEx = null;
        try {
            synchronized (stmt) {
                if (this.identityScope != null) {
                    this.identityScope.lock();
                }
                try {
                    if (this.isStandardSQLite) {
                        SQLiteStatement rawStmt = (SQLiteStatement) stmt.getRawStatement();
                        for (T entity : entities) {
                            updateInsideSynchronized((Object) entity, rawStmt, false);
                        }
                    } else {
                        for (T entity2 : entities) {
                            updateInsideSynchronized((Object) entity2, stmt, false);
                        }
                    }
                    if (this.identityScope != null) {
                        this.identityScope.unlock();
                    }
                } catch (Throwable th) {
                    if (this.identityScope != null) {
                        this.identityScope.unlock();
                    }
                }
            }
            this.db.setTransactionSuccessful();
        } catch (RuntimeException e) {
            txEx = e;
        } finally {
            try {
                this.db.endTransaction();
            } catch (RuntimeException e2) {
                if (txEx != null) {
                    DaoLog.w("Could not end transaction (rethrowing initial exception)", e2);
                    throw txEx;
                }
                throw e2;
            }
        }
        if (txEx != null) {
            throw txEx;
        }
    }

    public void updateInTx(T... entities) {
        updateInTx(Arrays.asList(entities));
    }

    protected void assertSinglePk() {
        if (this.config.pkColumns.length != 1) {
            throw new DaoException(this + " (" + this.config.tablename + ") does not have a single-column primary key");
        }
    }

    public long count() {
        return this.statements.getCountStatement().simpleQueryForLong();
    }

    protected K getKeyVerified(T entity) {
        K key = getKey(entity);
        if (key != null) {
            return key;
        }
        if (entity == null) {
            throw new NullPointerException("Entity may not be null");
        }
        throw new DaoException("Entity has no key");
    }

    @Experimental
    public RxDao<T, K> rxPlain() {
        if (this.rxDaoPlain == null) {
            this.rxDaoPlain = new RxDao(this);
        }
        return this.rxDaoPlain;
    }

    @Experimental
    public RxDao<T, K> rx() {
        if (this.rxDao == null) {
            this.rxDao = new RxDao(this, Schedulers.io());
        }
        return this.rxDao;
    }

    public Database getDatabase() {
        return this.db;
    }
}
