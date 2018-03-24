package org.greenrobot.greendao.test;

import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.greenrobot.greendao.AbstractDao;
import org.greenrobot.greendao.DaoLog;
import org.greenrobot.greendao.Property;
import org.greenrobot.greendao.internal.SqlUtils;

public abstract class AbstractDaoTestSinglePk<D extends AbstractDao<T, K>, T, K> extends AbstractDaoTest<D, T, K> {
    private Property pkColumn;
    protected Set<K> usedPks = new HashSet();

    protected abstract T createEntity(K k);

    protected abstract K createRandomPk();

    public AbstractDaoTestSinglePk(Class<D> daoClass) {
        super(daoClass);
    }

    protected void setUp() throws Exception {
        super.setUp();
        for (Property column : this.daoAccess.getProperties()) {
            if (column.primaryKey) {
                if (this.pkColumn != null) {
                    throw new RuntimeException("Test does not work with multiple PK columns");
                }
                this.pkColumn = column;
            }
        }
        if (this.pkColumn == null) {
            throw new RuntimeException("Test does not work without a PK column");
        }
    }

    public void testInsertAndLoad() {
        K pk = nextPk();
        T entity = createEntity(pk);
        this.dao.insert(entity);
        assertEquals(pk, this.daoAccess.getKey(entity));
        T entity2 = this.dao.load(pk);
        assertNotNull(entity2);
        assertEquals(this.daoAccess.getKey(entity), this.daoAccess.getKey(entity2));
    }

    public void testInsertInTx() {
        this.dao.deleteAll();
        Iterable list = new ArrayList();
        for (int i = 0; i < 20; i++) {
            list.add(createEntityWithRandomPk());
        }
        this.dao.insertInTx(list);
        assertEquals((long) list.size(), this.dao.count());
    }

    public void testCount() {
        this.dao.deleteAll();
        assertEquals(0, this.dao.count());
        this.dao.insert(createEntityWithRandomPk());
        assertEquals(1, this.dao.count());
        this.dao.insert(createEntityWithRandomPk());
        assertEquals(2, this.dao.count());
    }

    public void testInsertTwice() {
        T entity = createEntity(nextPk());
        this.dao.insert(entity);
        try {
            this.dao.insert(entity);
            fail("Inserting twice should not work");
        } catch (SQLException e) {
        }
    }

    public void testInsertOrReplaceTwice() {
        T entity = createEntityWithRandomPk();
        long rowId1 = this.dao.insert(entity);
        long rowId2 = this.dao.insertOrReplace(entity);
        if (this.dao.getPkProperty().type == Long.class) {
            assertEquals(rowId1, rowId2);
        }
    }

    public void testInsertOrReplaceInTx() {
        this.dao.deleteAll();
        Iterable listPartial = new ArrayList();
        Iterable listAll = new ArrayList();
        for (int i = 0; i < 20; i++) {
            T entity = createEntityWithRandomPk();
            if (i % 2 == 0) {
                listPartial.add(entity);
            }
            listAll.add(entity);
        }
        this.dao.insertOrReplaceInTx(listPartial);
        this.dao.insertOrReplaceInTx(listAll);
        assertEquals((long) listAll.size(), this.dao.count());
    }

    public void testDelete() {
        K pk = nextPk();
        this.dao.deleteByKey(pk);
        this.dao.insert(createEntity(pk));
        assertNotNull(this.dao.load(pk));
        this.dao.deleteByKey(pk);
        assertNull(this.dao.load(pk));
    }

    public void testDeleteAll() {
        Iterable<T> entityList = new ArrayList();
        for (int i = 0; i < 10; i++) {
            entityList.add(createEntityWithRandomPk());
        }
        this.dao.insertInTx((Iterable) entityList);
        this.dao.deleteAll();
        assertEquals(0, this.dao.count());
        for (T entity : entityList) {
            K key = this.daoAccess.getKey(entity);
            assertNotNull(key);
            assertNull(this.dao.load(key));
        }
    }

    public void testDeleteInTx() {
        Iterable entityList = new ArrayList();
        for (int i = 0; i < 10; i++) {
            entityList.add(createEntityWithRandomPk());
        }
        this.dao.insertInTx(entityList);
        Iterable<T> entitiesToDelete = new ArrayList();
        entitiesToDelete.add(entityList.get(0));
        entitiesToDelete.add(entityList.get(3));
        entitiesToDelete.add(entityList.get(4));
        entitiesToDelete.add(entityList.get(8));
        this.dao.deleteInTx((Iterable) entitiesToDelete);
        assertEquals((long) (entityList.size() - entitiesToDelete.size()), this.dao.count());
        for (T deletedEntity : entitiesToDelete) {
            K key = this.daoAccess.getKey(deletedEntity);
            assertNotNull(key);
            assertNull(this.dao.load(key));
        }
    }

    public void testDeleteByKeyInTx() {
        Iterable entityList = new ArrayList();
        for (int i = 0; i < 10; i++) {
            entityList.add(createEntityWithRandomPk());
        }
        this.dao.insertInTx(entityList);
        Iterable keysToDelete = new ArrayList();
        keysToDelete.add(this.daoAccess.getKey(entityList.get(0)));
        keysToDelete.add(this.daoAccess.getKey(entityList.get(3)));
        keysToDelete.add(this.daoAccess.getKey(entityList.get(4)));
        keysToDelete.add(this.daoAccess.getKey(entityList.get(8)));
        this.dao.deleteByKeyInTx(keysToDelete);
        assertEquals((long) (entityList.size() - keysToDelete.size()), this.dao.count());
        for (K key : keysToDelete) {
            assertNotNull(key);
            assertNull(this.dao.load(key));
        }
    }

    public void testRowId() {
        assertTrue(this.dao.insert(createEntityWithRandomPk()) != this.dao.insert(createEntityWithRandomPk()));
    }

    public void testLoadAll() {
        this.dao.deleteAll();
        Iterable list = new ArrayList();
        for (int i = 0; i < 15; i++) {
            list.add(createEntity(nextPk()));
        }
        this.dao.insertInTx(list);
        assertEquals(list.size(), this.dao.loadAll().size());
    }

    public void testQuery() {
        this.dao.insert(createEntityWithRandomPk());
        K pkForQuery = nextPk();
        this.dao.insert(createEntity(pkForQuery));
        this.dao.insert(createEntityWithRandomPk());
        String where = "WHERE " + this.dao.getPkColumns()[0] + "=?";
        List<T> list = this.dao.queryRaw(where, pkForQuery.toString());
        assertEquals(1, list.size());
        assertEquals(pkForQuery, this.daoAccess.getKey(list.get(0)));
    }

    public void testUpdate() {
        this.dao.deleteAll();
        T entity = createEntityWithRandomPk();
        this.dao.insert(entity);
        this.dao.update(entity);
        assertEquals(1, this.dao.count());
    }

    public void testReadWithOffset() {
        K pk = nextPk();
        this.dao.insert(createEntity(pk));
        Cursor cursor = queryWithDummyColumnsInFront(5, "42", pk);
        try {
            assertEquals(pk, this.daoAccess.getKey(this.daoAccess.readEntity(cursor, 5)));
        } finally {
            cursor.close();
        }
    }

    public void testLoadPkWithOffset() {
        runLoadPkTest(10);
    }

    public void testLoadPk() {
        runLoadPkTest(0);
    }

    public void testSave() {
        if (checkKeyIsNullable()) {
            this.dao.deleteAll();
            T entity = createEntity(null);
            if (entity != null) {
                this.dao.save(entity);
                this.dao.save(entity);
                assertEquals(1, this.dao.count());
            }
        }
    }

    public void testSaveInTx() {
        if (checkKeyIsNullable()) {
            this.dao.deleteAll();
            Iterable listPartial = new ArrayList();
            Iterable listAll = new ArrayList();
            for (int i = 0; i < 20; i++) {
                T entity = createEntity(null);
                if (i % 2 == 0) {
                    listPartial.add(entity);
                }
                listAll.add(entity);
            }
            this.dao.saveInTx(listPartial);
            this.dao.saveInTx(listAll);
            assertEquals((long) listAll.size(), this.dao.count());
        }
    }

    protected void runLoadPkTest(int offset) {
        K pk = nextPk();
        this.dao.insert(createEntity(pk));
        Cursor cursor = queryWithDummyColumnsInFront(offset, "42", pk);
        try {
            assertEquals(pk, this.daoAccess.readKey(cursor, offset));
        } finally {
            cursor.close();
        }
    }

    protected Cursor queryWithDummyColumnsInFront(int dummyCount, String valueForColumn, K pk) {
        int i;
        StringBuilder builder = new StringBuilder("SELECT ");
        for (i = 0; i < dummyCount; i++) {
            builder.append(valueForColumn).append(",");
        }
        SqlUtils.appendColumns(builder, "T", this.dao.getAllColumns()).append(" FROM ");
        builder.append('\"').append(this.dao.getTablename()).append('\"').append(" T");
        if (pk != null) {
            builder.append(" WHERE ");
            assertEquals(1, this.dao.getPkColumns().length);
            builder.append(this.dao.getPkColumns()[0]).append("=");
            DatabaseUtils.appendValueToSql(builder, pk);
        }
        Cursor cursor = this.db.rawQuery(builder.toString(), null);
        assertTrue(cursor.moveToFirst());
        i = 0;
        while (i < dummyCount) {
            try {
                assertEquals(valueForColumn, cursor.getString(i));
                i++;
            } catch (RuntimeException ex) {
                cursor.close();
                throw ex;
            }
        }
        if (pk != null) {
            assertEquals(1, cursor.getCount());
        }
        return cursor;
    }

    protected boolean checkKeyIsNullable() {
        if (createEntity(null) != null) {
            return true;
        }
        DaoLog.d("Test is not available for entities with non-null keys");
        return false;
    }

    protected K nextPk() {
        for (int i = 0; i < 100000; i++) {
            K pk = createRandomPk();
            if (this.usedPks.add(pk)) {
                return pk;
            }
        }
        throw new IllegalStateException("Could not find a new PK");
    }

    protected T createEntityWithRandomPk() {
        return createEntity(nextPk());
    }
}
