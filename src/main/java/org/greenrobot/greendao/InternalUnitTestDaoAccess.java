package org.greenrobot.greendao;

import android.database.Cursor;
import org.greenrobot.greendao.database.Database;
import org.greenrobot.greendao.identityscope.IdentityScope;
import org.greenrobot.greendao.internal.DaoConfig;

public class InternalUnitTestDaoAccess<T, K> {
    private final AbstractDao<T, K> dao;

    public InternalUnitTestDaoAccess(Database db, Class<AbstractDao<T, K>> daoClass, IdentityScope<?, ?> identityScope) throws Exception {
        new DaoConfig(db, daoClass).setIdentityScope(identityScope);
        this.dao = (AbstractDao) daoClass.getConstructor(new Class[]{DaoConfig.class}).newInstance(new Object[]{daoConfig});
    }

    public K getKey(T entity) {
        return this.dao.getKey(entity);
    }

    public Property[] getProperties() {
        return this.dao.getProperties();
    }

    public boolean isEntityUpdateable() {
        return this.dao.isEntityUpdateable();
    }

    public T readEntity(Cursor cursor, int offset) {
        return this.dao.readEntity(cursor, offset);
    }

    public K readKey(Cursor cursor, int offset) {
        return this.dao.readKey(cursor, offset);
    }

    public AbstractDao<T, K> getDao() {
        return this.dao;
    }
}
