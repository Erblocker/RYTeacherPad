package com.foxit.sdk;

import java.util.Collection;
import java.util.HashMap;

/* compiled from: AbstractPageView */
class j<K, V> {
    private HashMap<K, V> a;

    public j(int i) {
        this.a = new HashMap(i);
    }

    public V a(K k, V v) {
        return this.a.put(k, v);
    }

    public void a(j<K, V> jVar) {
        this.a.putAll(jVar.a);
    }

    public V a(K k) {
        return this.a.get(k);
    }

    public V b(K k) {
        return this.a.remove(k);
    }

    public int a() {
        return this.a.size();
    }

    public Collection<V> b() {
        return this.a.values();
    }

    public void c() {
        this.a.clear();
    }
}
