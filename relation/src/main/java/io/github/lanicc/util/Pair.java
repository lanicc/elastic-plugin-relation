package io.github.lanicc.util;

/**
 * Created on 2021/9/29.
 *
 * @author lan
 * @since 2.0.0
 */
public class Pair<K, V> {

    private K k;

    private V v;

    public Pair(K k, V v) {
    }

    public K k() {
        return k;
    }

    public V v() {
        return v;
    }
}
