package net.sqlcipher;

import android.database.CharArrayBuffer;

public abstract class AbstractWindowedCursor extends AbstractCursor {
    protected CursorWindow mWindow;

    public byte[] getBlob(int columnIndex) {
        checkPosition();
        synchronized (this.mUpdatedRows) {
            if (isFieldUpdated(columnIndex)) {
                byte[] bArr = (byte[]) getUpdatedField(columnIndex);
                return bArr;
            }
            return this.mWindow.getBlob(this.mPos, columnIndex);
        }
    }

    public String getString(int columnIndex) {
        checkPosition();
        synchronized (this.mUpdatedRows) {
            if (isFieldUpdated(columnIndex)) {
                String str = (String) getUpdatedField(columnIndex);
                return str;
            }
            return this.mWindow.getString(this.mPos, columnIndex);
        }
    }

    public void copyStringToBuffer(int columnIndex, CharArrayBuffer buffer) {
        checkPosition();
        synchronized (this.mUpdatedRows) {
            if (isFieldUpdated(columnIndex)) {
                super.copyStringToBuffer(columnIndex, buffer);
            }
        }
        this.mWindow.copyStringToBuffer(this.mPos, columnIndex, buffer);
    }

    public short getShort(int columnIndex) {
        checkPosition();
        synchronized (this.mUpdatedRows) {
            if (isFieldUpdated(columnIndex)) {
                short shortValue = ((Number) getUpdatedField(columnIndex)).shortValue();
                return shortValue;
            }
            return this.mWindow.getShort(this.mPos, columnIndex);
        }
    }

    public int getInt(int columnIndex) {
        checkPosition();
        synchronized (this.mUpdatedRows) {
            if (isFieldUpdated(columnIndex)) {
                int intValue = ((Number) getUpdatedField(columnIndex)).intValue();
                return intValue;
            }
            return this.mWindow.getInt(this.mPos, columnIndex);
        }
    }

    public long getLong(int columnIndex) {
        checkPosition();
        synchronized (this.mUpdatedRows) {
            if (isFieldUpdated(columnIndex)) {
                long longValue = ((Number) getUpdatedField(columnIndex)).longValue();
                return longValue;
            }
            return this.mWindow.getLong(this.mPos, columnIndex);
        }
    }

    public float getFloat(int columnIndex) {
        checkPosition();
        synchronized (this.mUpdatedRows) {
            if (isFieldUpdated(columnIndex)) {
                float floatValue = ((Number) getUpdatedField(columnIndex)).floatValue();
                return floatValue;
            }
            return this.mWindow.getFloat(this.mPos, columnIndex);
        }
    }

    public double getDouble(int columnIndex) {
        checkPosition();
        synchronized (this.mUpdatedRows) {
            if (isFieldUpdated(columnIndex)) {
                double doubleValue = ((Number) getUpdatedField(columnIndex)).doubleValue();
                return doubleValue;
            }
            return this.mWindow.getDouble(this.mPos, columnIndex);
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public boolean isNull(int columnIndex) {
        checkPosition();
        synchronized (this.mUpdatedRows) {
            if (isFieldUpdated(columnIndex)) {
                boolean z = getUpdatedField(columnIndex) == null;
            } else {
                return this.mWindow.isNull(this.mPos, columnIndex);
            }
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public boolean isBlob(int columnIndex) {
        checkPosition();
        synchronized (this.mUpdatedRows) {
            if (isFieldUpdated(columnIndex)) {
                Object object = getUpdatedField(columnIndex);
                boolean z = object == null || (object instanceof byte[]);
            } else {
                return this.mWindow.isBlob(this.mPos, columnIndex);
            }
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public boolean isString(int columnIndex) {
        checkPosition();
        synchronized (this.mUpdatedRows) {
            if (isFieldUpdated(columnIndex)) {
                Object object = getUpdatedField(columnIndex);
                boolean z = object == null || (object instanceof String);
            } else {
                return this.mWindow.isString(this.mPos, columnIndex);
            }
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public boolean isLong(int columnIndex) {
        checkPosition();
        synchronized (this.mUpdatedRows) {
            if (isFieldUpdated(columnIndex)) {
                Object object = getUpdatedField(columnIndex);
                boolean z = object != null && ((object instanceof Integer) || (object instanceof Long));
            } else {
                return this.mWindow.isLong(this.mPos, columnIndex);
            }
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public boolean isFloat(int columnIndex) {
        checkPosition();
        synchronized (this.mUpdatedRows) {
            if (isFieldUpdated(columnIndex)) {
                Object object = getUpdatedField(columnIndex);
                boolean z = object != null && ((object instanceof Float) || (object instanceof Double));
            } else {
                return this.mWindow.isFloat(this.mPos, columnIndex);
            }
        }
    }

    public int getType(int columnIndex) {
        checkPosition();
        return this.mWindow.getType(this.mPos, columnIndex);
    }

    protected void checkPosition() {
        super.checkPosition();
        if (this.mWindow == null) {
            throw new StaleDataException("Access closed cursor");
        }
    }

    public CursorWindow getWindow() {
        return this.mWindow;
    }

    public void setWindow(CursorWindow window) {
        if (this.mWindow != null) {
            this.mWindow.close();
        }
        this.mWindow = window;
    }

    public boolean hasWindow() {
        return this.mWindow != null;
    }
}
