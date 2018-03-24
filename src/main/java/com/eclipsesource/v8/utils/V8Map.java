package com.eclipsesource.v8.utils;

import com.eclipsesource.v8.Releasable;
import com.eclipsesource.v8.V8Value;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

public class V8Map<V> implements Map<V8Value, V>, Releasable {
    private Map<V8Value, V> map = new HashMap();
    private Map<V8Value, V8Value> twinMap = new HashMap();

    public void release() {
        clear();
    }

    public int size() {
        return this.map.size();
    }

    public boolean isEmpty() {
        return this.map.isEmpty();
    }

    public boolean containsKey(Object key) {
        return this.map.containsKey(key);
    }

    public boolean containsValue(Object value) {
        return this.map.containsValue(value);
    }

    public V get(Object key) {
        return this.map.get(key);
    }

    public V put(V8Value key, V value) {
        remove(key);
        V8Value twin = key.twin();
        this.twinMap.put(twin, twin);
        return this.map.put(twin, value);
    }

    public V remove(Object key) {
        V result = this.map.remove(key);
        V8Value twin = (V8Value) this.twinMap.remove(key);
        if (twin != null) {
            twin.release();
        }
        return result;
    }

    public void putAll(Map<? extends V8Value, ? extends V> m) {
        for (Entry<? extends V8Value, ? extends V> entry : m.entrySet()) {
            put((V8Value) entry.getKey(), entry.getValue());
        }
    }

    public void clear() {
        this.map.clear();
        for (V8Value V8Value : this.twinMap.keySet()) {
            V8Value.release();
        }
        this.twinMap.clear();
    }

    public Set<V8Value> keySet() {
        return this.map.keySet();
    }

    public Collection<V> values() {
        return this.map.values();
    }

    public Set<Entry<V8Value, V>> entrySet() {
        return this.map.entrySet();
    }
}
