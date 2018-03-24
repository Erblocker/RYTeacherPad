package org.greenrobot.greendao.internal;

import java.util.Arrays;
import org.greenrobot.greendao.DaoLog;

public final class LongHashMap<T> {
    private int capacity;
    private int size;
    private Entry<T>[] table;
    private int threshold;

    static final class Entry<T> {
        final long key;
        Entry<T> next;
        T value;

        Entry(long key, T value, Entry<T> next) {
            this.key = key;
            this.value = value;
            this.next = next;
        }
    }

    public LongHashMap() {
        this(16);
    }

    public LongHashMap(int capacity) {
        this.capacity = capacity;
        this.threshold = (capacity * 4) / 3;
        this.table = new Entry[capacity];
    }

    public boolean containsKey(long key) {
        for (Entry<T> entry = this.table[((((int) (key >>> 32)) ^ ((int) key)) & Integer.MAX_VALUE) % this.capacity]; entry != null; entry = entry.next) {
            if (entry.key == key) {
                return true;
            }
        }
        return false;
    }

    public T get(long key) {
        for (Entry<T> entry = this.table[((((int) (key >>> 32)) ^ ((int) key)) & Integer.MAX_VALUE) % this.capacity]; entry != null; entry = entry.next) {
            if (entry.key == key) {
                return entry.value;
            }
        }
        return null;
    }

    public T put(long key, T value) {
        int index = ((((int) (key >>> 32)) ^ ((int) key)) & Integer.MAX_VALUE) % this.capacity;
        Entry<T> entryOriginal = this.table[index];
        for (Entry<T> entry = entryOriginal; entry != null; entry = entry.next) {
            if (entry.key == key) {
                T oldValue = entry.value;
                entry.value = value;
                return oldValue;
            }
        }
        this.table[index] = new Entry(key, value, entryOriginal);
        this.size++;
        if (this.size > this.threshold) {
            setCapacity(this.capacity * 2);
        }
        return null;
    }

    public T remove(long key) {
        int index = ((((int) (key >>> 32)) ^ ((int) key)) & Integer.MAX_VALUE) % this.capacity;
        Entry<T> previous = null;
        Entry<T> entry = this.table[index];
        while (entry != null) {
            Entry<T> next = entry.next;
            if (entry.key == key) {
                if (previous == null) {
                    this.table[index] = next;
                } else {
                    previous.next = next;
                }
                this.size--;
                return entry.value;
            }
            previous = entry;
            entry = next;
        }
        return null;
    }

    public void clear() {
        this.size = 0;
        Arrays.fill(this.table, null);
    }

    public int size() {
        return this.size;
    }

    public void setCapacity(int newCapacity) {
        Entry<T>[] newTable = new Entry[newCapacity];
        for (Entry<T> entry : this.table) {
            Entry<T> entry2;
            while (entry2 != null) {
                long key = entry2.key;
                int index = ((((int) (key >>> 32)) ^ ((int) key)) & Integer.MAX_VALUE) % newCapacity;
                Entry<T> originalNext = entry2.next;
                entry2.next = newTable[index];
                newTable[index] = entry2;
                entry2 = originalNext;
            }
        }
        this.table = newTable;
        this.capacity = newCapacity;
        this.threshold = (newCapacity * 4) / 3;
    }

    public void reserveRoom(int entryCount) {
        setCapacity((entryCount * 5) / 3);
    }

    public void logStats() {
        int collisions = 0;
        for (Entry<T> entry : this.table) {
            Entry<T> entry2;
            while (entry2 != null && entry2.next != null) {
                collisions++;
                entry2 = entry2.next;
            }
        }
        DaoLog.d("load: " + (((float) this.size) / ((float) this.capacity)) + ", size: " + this.size + ", capa: " + this.capacity + ", collisions: " + collisions + ", collision ratio: " + (((float) collisions) / ((float) this.size)));
    }
}
