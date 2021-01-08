package com.daren.chen.im.core.cache.redis;

/**
 * @Description:
 * @author: chendaren
 * @CreateDate: 2020/12/22 15:03
 */
public class PairEx<K, V, E> extends Pair<K, V> {

    private E expire;

    public PairEx(K key, V value) {
        super(key, value);
    }

    public PairEx(K key, V value, E expire) {
        super(key, value);
        this.expire = expire;
    }

    public E getExpire() {
        return expire;
    }

    public void setExpire(E expire) {
        this.expire = expire;
    }

}
