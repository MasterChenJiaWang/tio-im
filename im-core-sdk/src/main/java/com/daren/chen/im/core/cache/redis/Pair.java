package com.daren.chen.im.core.cache.redis;

import java.io.Serializable;

/**
 * @Description:
 * @author: chendaren
 * @CreateDate: 2020/12/22 15:02
 */
public class Pair<K, V> implements Serializable {
    /**
     * 键值对
     *
     * @version V1.0
     * @author fengjc
     * @param <K>
     *            key
     * @param <V>
     *            value
     */
    private K key;
    private V value;

    public Pair(K key, V value) {
        this.key = key;
        this.value = value;
    }

    public K getKey() {
        return key;
    }

    public void setKey(K key) {
        this.key = key;
    }

    public V getValue() {
        return value;
    }

    public void setValue(V value) {
        this.value = value;
    }
}
