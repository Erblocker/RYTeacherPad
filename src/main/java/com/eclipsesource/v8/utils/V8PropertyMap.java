package com.eclipsesource.v8.utils;

import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

class V8PropertyMap<V> implements Map<String, V> {
    private Hashtable<String, V> map = new Hashtable();
    private Set<String> nulls = new HashSet();

    V8PropertyMap() {
    }

    public int size() {
        return this.map.size() + this.nulls.size();
    }

    public boolean isEmpty() {
        return this.map.isEmpty() && this.nulls.isEmpty();
    }

    public boolean containsKey(Object key) {
        return this.map.containsKey(key) || this.nulls.contains(key);
    }

    public boolean containsValue(Object value) {
        if (value == null && !this.nulls.isEmpty()) {
            return true;
        }
        if (value == null) {
            return false;
        }
        return this.map.containsValue(value);
    }

    public V get(Object key) {
        if (this.nulls.contains(key)) {
            return null;
        }
        return this.map.get(key);
    }

    public V put(String key, V value) {
        if (value == null) {
            if (this.map.containsKey(key)) {
                this.map.remove(key);
            }
            this.nulls.add(key);
            return null;
        }
        if (this.nulls.contains(key)) {
            this.nulls.remove(key);
        }
        return this.map.put(key, value);
    }

    public V remove(Object key) {
        if (!this.nulls.contains(key)) {
            return this.map.remove(key);
        }
        this.nulls.remove(key);
        return null;
    }

    public void putAll(Map<? extends String, ? extends V> m) {
        for (Entry<? extends String, ? extends V> entry : m.entrySet()) {
            put((String) entry.getKey(), entry.getValue());
        }
    }

    public void clear() {
        this.map.clear();
        this.nulls.clear();
    }

    public Set<String> keySet() {
        HashSet<String> result = new HashSet(this.map.keySet());
        result.addAll(this.nulls);
        return result;
    }

    public Collection<V> values() {
        ArrayList<V> result = new ArrayList(this.map.values());
        for (int i = 0; i < this.nulls.size(); i++) {
            result.add(null);
        }
        return result;
    }

    public Set<Entry<String, V>> entrySet() {
        HashSet<Entry<String, V>> result = new HashSet(this.map.entrySet());
        for (String nullKey : this.nulls) {
            result.add(new SimpleEntry(nullKey, null));
        }
        return result;
    }
}
