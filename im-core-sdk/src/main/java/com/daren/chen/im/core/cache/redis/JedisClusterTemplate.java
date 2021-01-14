package com.daren.chen.im.core.cache.redis;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSON;

import redis.clients.jedis.HostAndPort;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisCluster;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.JedisPubSub;
import redis.clients.jedis.ScanParams;
import redis.clients.jedis.ScanResult;

/**
 * @author wchao
 * @modify 2016-08-29 增加了set(final String key, final Object value)和<T> T get(final String key,final Class<T> clazz)
 */
public class JedisClusterTemplate implements BaseJedisTemplate, Serializable {

    private static final long serialVersionUID = 9135301078135982677L;
    private static final Logger logger = LoggerFactory.getLogger(JedisClusterTemplate.class);
    private static volatile JedisClusterTemplate instance = null;
    private static volatile JedisCluster jedisCluster = null;
    private static RedisConfiguration redisConfig = null;
    private static final Set<HostAndPort> JEDIS_CLUSTER_NODES = new HashSet<>();

    private JedisClusterTemplate() {}

    public static JedisClusterTemplate me() throws Exception {
        if (instance == null) {
            synchronized (JedisClusterTemplate.class) {
                if (instance == null) {
                    redisConfig = RedisConfigurationFactory.parseConfiguration();
                    init();
                    instance = new JedisClusterTemplate();
                }
            }
        }
        return instance;
    }

    private static void init() throws Exception {

        if (redisConfig.getHost() == null) {
            logger.error("the server ip of redis  must be not null!");
            throw new Exception("the server ip of redis  must be not null!");
        }

        JedisPoolConfig poolConfig = new JedisPoolConfig();

        poolConfig.setMaxTotal(redisConfig.getMaxActive());
        poolConfig.setMaxIdle(redisConfig.getMaxIdle());
        poolConfig.setMaxWaitMillis(redisConfig.getMaxWait());
        poolConfig.setTestOnBorrow(true);
        poolConfig.setTestOnReturn(true);
        try {
            String clusterAddrs = redisConfig.getClusterAddr();
            String[] addrs = clusterAddrs.split(",");
            for (String addr : addrs) {
                String[] ipAndPort = addr.split(":");
                if (ipAndPort.length != 2) {
                    throw new Exception("System.properties中clusterAddr属性配置错误");
                }
                JEDIS_CLUSTER_NODES.add(new HostAndPort(ipAndPort[0], Integer.parseInt(ipAndPort[1])));
            }
            if (StringUtils.isEmpty(redisConfig.getAuth())) {
                jedisCluster = new JedisCluster(JEDIS_CLUSTER_NODES, redisConfig.getTimeout(), redisConfig.getTimeout(),
                    6, poolConfig);
            } else {
                jedisCluster = new JedisCluster(JEDIS_CLUSTER_NODES, redisConfig.getTimeout(), redisConfig.getTimeout(),
                    6, redisConfig.getAuth(), poolConfig);
            }
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("cann't create JedisPool for server" + redisConfig.getHost());
            throw new Exception("cann't create JedisPool for server" + redisConfig.getHost());
        }

    }

    abstract class Executor<T> {

        /**
         * 回调
         *
         * @return 执行结果
         */
        abstract T execute();

        /**
         * 调用{@link #execute()}并返回执行结果 它保证在执行{@link #execute()}之后释放数据源returnResource(jedis)
         *
         * @return 执行结果
         */
        public T getResult() {
            T result = null;
            try {
                result = execute();
            } catch (Throwable e) {
                throw new RuntimeException("Redis execute exception", e);
            }
            return result;
        }
    }

    /**
     * 模糊获取所有的key
     *
     * @return
     */
    @Override
    public Set<String> keys(String likeKey) {
        return new Executor<Set<String>>() {
            @Override
            Set<String> execute() {
                Set<String> keys = new HashSet<>();
                Collection<JedisPool> clusterNodes = jedisCluster.getClusterNodes().values();
                for (JedisPool clusterNode : clusterNodes) {
                    String cursor = ScanParams.SCAN_POINTER_START;
                    List<String> result;
                    do {
                        Jedis jedis = clusterNode.getResource();
                        ScanResult<String> scanResult =
                            jedis.scan(cursor, new ScanParams().match(likeKey + "*").count(200));
                        cursor = scanResult.getCursor();
                        result = scanResult.getResult();
                        if (!result.isEmpty()) {
                            keys.addAll(result);
                        }
                        jedis.close();
                    } while (!cursor.equals(ScanParams.SCAN_POINTER_START));
                }
                return keys;
            }
        }.getResult();
    }

    /**
     * 删除模糊匹配的key
     *
     * @param likeKey
     *            模糊匹配的key
     * @return 删除成功的条数
     */
    @Override
    public long delKeysLike(final String likeKey) {
        return new Executor<Long>() {
            @Override
            Long execute() {
                Set<String> keys = keys(likeKey);
                long i = 0;
                if (keys != null && keys.size() > 0) {
                    for (String key : keys) {
                        i = i + jedisCluster.del(key);
                    }
                }
                return i;
            }
        }.getResult();
    }

    /**
     * 删除
     *
     * @param key
     *            匹配的key
     * @return 删除成功的条数
     */
    @Override
    public Long delKey(final String key) {
        return new Executor<Long>() {
            @Override
            Long execute() {
                return jedisCluster.del(key);
            }
        }.getResult();
    }

    /**
     * 删除
     *
     * @param keys
     *            匹配的key的集合
     * @return 删除成功的条数
     */
    @Override
    public Long delKeys(final String[] keys) {
        return new Executor<Long>() {

            @Override
            Long execute() {
                long i = 0;
                if (keys != null && keys.length > 0) {
                    for (String key : keys) {
                        i = i + jedisCluster.del(key);
                    }
                }
                return i;
            }
        }.getResult();
    }

    /**
     * 为给定 key 设置生存时间，当 key 过期时(生存时间为 0 )，它会被自动删除。 在 Redis 中，带有生存时间的 key 被称为『可挥发』(volatile)的。
     *
     * @param key
     *            key
     * @param expire
     *            生命周期，单位为秒
     * @return 1: 设置成功 0: 已经超时或key不存在
     */
    @Override
    public Long expire(final String key, final int expire) {
        return new Executor<Long>() {

            @Override
            Long execute() {
                return jedisCluster.expire(key, expire);
            }
        }.getResult();
    }

    /**
     * 一个跨jvm的id生成器，利用了redis原子性操作的特点
     *
     * @param key
     *            id的key
     * @return 返回生成的Id
     */
    @Override
    public long makeId(final String key) {
        return new Executor<Long>() {

            @Override
            Long execute() {
                long id = jedisCluster.incr(key);
                if ((id + 75807) >= Long.MAX_VALUE) {
                    // 避免溢出，重置，getSet命令之前允许incr插队，75807就是预留的插队空间
                    jedisCluster.getSet(key, "0");
                }
                return id;
            }
        }.getResult();
    }

    @Override
    public long decr(final String key, final long increment) {
        return new Executor<Long>() {

            @Override
            Long execute() {
                return jedisCluster.decrBy(key, increment);
            }
        }.getResult();
    }

    @Override
    public long incr(final String key, final long increment) {
        return new Executor<Long>() {

            @Override
            Long execute() {
                return jedisCluster.incrBy(key, increment);
            }
        }.getResult();
    }

    /* ======================================Strings====================================== */

    /**
     * 将字符串值 value 关联到 key 。 如果 key 已经持有其他值， setString 就覆写旧值，无视类型。 对于某个原本带有生存时间（TTL）的键来说， 当 setString 成功在这个键上执行时， 这个键原有的
     * TTL 将被清除。 时间复杂度：O(1)
     *
     * @param key
     *            key
     * @param value
     *            string value
     * @return 在设置操作成功完成时，才返回 OK 。
     */
    @Override
    public String setString(final String key, final String value) {
        return new Executor<String>() {

            @Override
            String execute() {
                return jedisCluster.set(key, value);
            }
        }.getResult();
    }

    @Override
    public boolean setString2Boolean(final String key, final String value, final int expire) {
        return new Executor<Boolean>() {

            @Override
            Boolean execute() {
                jedisCluster.setex(key, expire, value);
                return true;
            }
        }.getResult();
    }

    /**
     * 将值 value 关联到 key ，并将 key 的生存时间设为 expire (以秒为单位)。 如果 key 已经存在， 将覆写旧值。 类似于以下两个命令: SET key value EXPIRE key expire #
     * 设置生存时间 不同之处是这个方法是一个原子性(atomic)操作，关联值和设置生存时间两个动作会在同一时间内完成，在 Redis 用作缓存时，非常实用。 时间复杂度：O(1)
     *
     * @param key
     *            key
     * @param value
     *            string value
     * @param expire
     *            生命周期
     * @return 设置成功时返回 OK 。当 expire 参数不合法时，返回一个错误。
     */
    @Override
    public String setString(final String key, final String value, final int expire) {
        return new Executor<String>() {

            @Override
            String execute() {
                return jedisCluster.setex(key, expire, value);
            }
        }.getResult();
    }

    /**
     * 将 key 的值设为 value ，当且仅当 key 不存在。若给定的 key 已经存在，则 setStringIfNotExists 不做任何动作。 时间复杂度：O(1)
     *
     * @param key
     *            key
     * @param value
     *            string value
     * @return 设置成功，返回 1 。设置失败，返回 0 。
     */
    @Override
    public Long setStringIfNotExists(final String key, final String value) {
        return new Executor<Long>() {

            @Override
            Long execute() {
                return jedisCluster.setnx(key, value);
            }
        }.getResult();
    }

    @Override
    public boolean setStringIfNotExists(final String key, final String value, final int timeout) {
        return new Executor<Boolean>() {

            @Override
            Boolean execute() {
                Boolean result = jedisCluster.setnx(key, value) == 1;
                if (result) {
                    jedisCluster.expire(key, timeout);
                }
                return result;
            }
        }.getResult();
    }

    /**
     * 返回 key 所关联的字符串值。如果 key 不存在那么返回特殊值 nil 。 假如 key 储存的值不是字符串类型，返回一个错误，因为 getString 只能用于处理字符串值。 时间复杂度: O(1)
     *
     * @param key
     *            key
     * @return 当 key 不存在时，返回 nil ，否则，返回 key 的值。如果 key 不是字符串类型，那么返回一个错误。
     */
    @Override
    public String getString(final String key) {
        return new Executor<String>() {
            @Override
            String execute() {
                return jedisCluster.get(key);
            }
        }.getResult();
    }

    @Override
    public List<Object> batchSetString(List<Pair<String, String>> pairs) {
        return new Executor<List<Object>>() {
            @Override
            List<Object> execute() {
                if (pairs == null) {
                    return new ArrayList<>(2);
                }
                List<Object> objects = new ArrayList<>(pairs.size());
                for (Pair<String, String> pair : pairs) {
                    jedisCluster.set(pair.getKey(), pair.getValue());
                    objects.add(pair.getKey());
                }
                return objects;
            }
        }.getResult();
    }

    @Override
    public List<Object> batchSetStringEx(List<PairEx<String, String, Integer>> pairs) {
        return new Executor<List<Object>>() {
            @Override
            List<Object> execute() {
                if (pairs == null) {
                    return new ArrayList<>(2);
                }
                List<Object> objects = new ArrayList<>(pairs.size());
                for (PairEx<String, String, Integer> pair : pairs) {
                    jedisCluster.setex(pair.getKey(), pair.getExpire(), pair.getValue());
                    objects.add(pair.getKey());
                }
                return objects;
            }
        }.getResult();
    }

    @Override
    public List<String> batchGetString(String[] keys) {
        return new Executor<List<String>>() {
            @Override
            List<String> execute() {
                if (keys == null) {
                    return new ArrayList<>(2);
                }
                List<String> objects = new ArrayList<>(keys.length);
                for (String pair : keys) {
                    String set = jedisCluster.get(pair);
                    objects.add(set);
                }
                return objects;
            }
        }.getResult();
    }

    /* ======================================Hashes====================================== */

    /**
     * 将哈希表 key 中的域 field 的值设为 value 。 如果 key 不存在，一个新的哈希表被创建并进行 hashSet 操作。 如果域 field 已经存在于哈希表中，旧值将被覆盖。 时间复杂度: O(1)
     *
     * @param key
     *            key
     * @param field
     *            域
     * @param value
     *            string value
     * @return 如果 field 是哈希表中的一个新建域，并且值设置成功，返回 1 。如果哈希表中域 field 已经存在且旧值已被新值覆盖，返回 0 。
     */
    @Override
    public Long hashSet(final String key, final String field, final String value) {
        return new Executor<Long>() {

            @Override
            Long execute() {
                return jedisCluster.hset(key, field, value);
            }
        }.getResult();
    }

    /**
     * 将哈希表 key 中的域 field 的值设为 value 。 如果 key 不存在，一个新的哈希表被创建并进行 hashSet 操作。 如果域 field 已经存在于哈希表中，旧值将被覆盖。
     *
     * @param key
     *            key
     * @param field
     *            域
     * @param value
     *            string value
     * @param expire
     *            生命周期，单位为秒
     * @return 如果 field 是哈希表中的一个新建域，并且值设置成功，返回 1 。如果哈希表中域 field 已经存在且旧值已被新值覆盖，返回 0 。
     */
    @Override
    public Long hashSet(final String key, final String field, final String value, final int expire) {
        return new Executor<Long>() {
            @Override
            Long execute() {
                Long hset = jedisCluster.hset(key, field, value);
                jedisCluster.expire(key, expire);
                return hset;
            }
        }.getResult();
    }

    /**
     * 返回哈希表 key 中给定域 field 的值。 时间复杂度:O(1)
     *
     * @param key
     *            key
     * @param field
     *            域
     * @return 给定域的值。当给定域不存在或是给定 key 不存在时，返回 nil 。
     */
    @Override
    public String hashGet(final String key, final String field) {
        return new Executor<String>() {

            @Override
            String execute() {
                return jedisCluster.hget(key, field);
            }
        }.getResult();
    }

    /**
     * 返回哈希表 key 中给定域 field 的值。 如果哈希表 key 存在，同时设置这个 key 的生存时间
     *
     * @param key
     *            key
     * @param field
     *            域
     * @param expire
     *            生命周期，单位为秒
     * @return 给定域的值。当给定域不存在或是给定 key 不存在时，返回 nil 。
     */
    @Override
    public String hashGet(final String key, final String field, final int expire) {
        return new Executor<String>() {

            @Override
            String execute() {
                String hget = jedisCluster.hget(key, field);
                jedisCluster.expire(key, expire);
                return hget;
            }
        }.getResult();
    }

    /**
     * 同时将多个 field-value (域-值)对设置到哈希表 key 中。 时间复杂度: O(N) (N为fields的数量)
     *
     * @param key
     *            key
     * @param hash
     *            field-value的map
     * @return 如果命令执行成功，返回 OK 。当 key 不是哈希表(hash)类型时，返回一个错误。
     */
    @Override
    public String hashMultipleSet(final String key, final Map<String, String> hash) {
        return new Executor<String>() {
            @Override
            String execute() {
                return jedisCluster.hmset(key, hash);
            }
        }.getResult();
    }

    /**
     * 同时将多个 field-value (域-值)对设置到哈希表 key 中。同时设置这个 key 的生存时间
     *
     * @param key
     *            key
     * @param hash
     *            field-value的map
     * @param expire
     *            生命周期，单位为秒
     * @return 如果命令执行成功，返回 OK 。当 key 不是哈希表(hash)类型时，返回一个错误。
     */
    @Override
    public String hashMultipleSet(final String key, final Map<String, String> hash, final int expire) {
        return new Executor<String>() {

            @Override
            String execute() {
                if (hash != null) {
                    hash.forEach((k, v) -> {
                        jedisCluster.hset(key, k, v);
                    });
                }
                jedisCluster.expire(key, expire);
                return "OK";
            }
        }.getResult();
    }

    /**
     * 返回哈希表 key 中，一个或多个给定域的值。如果给定的域不存在于哈希表，那么返回一个 nil 值。 时间复杂度: O(N) (N为fields的数量)
     *
     * @param key
     *            key
     * @param fields
     *            field的数组
     * @return 一个包含多个给定域的关联值的表，表值的排列顺序和给定域参数的请求顺序一样。
     */
    @Override
    public List<String> hashMultipleGet(final String key, final String... fields) {
        return new Executor<List<String>>() {
            @Override
            List<String> execute() {
                return jedisCluster.hmget(key, fields);
            }
        }.getResult();
    }

    /**
     * 返回哈希表 key 中，一个或多个给定域的值。如果给定的域不存在于哈希表，那么返回一个 nil 值。 同时设置这个 key 的生存时间
     *
     * @param key
     *            key
     * @param fields
     *            field的数组
     * @param expire
     *            生命周期，单位为秒
     * @return 一个包含多个给定域的关联值的表，表值的排列顺序和给定域参数的请求顺序一样。
     */
    @Override
    public List<String> hashMultipleGet(final String key, final int expire, final String... fields) {
        return new Executor<List<String>>() {

            @Override
            List<String> execute() {
                List<String> list = new ArrayList<>();
                if (fields != null && fields.length > 0) {
                    for (String field : fields) {
                        String hget = jedisCluster.hget(key, field);
                        list.add(hget);
                    }
                }
                jedisCluster.expire(key, expire);
                return list;
            }
        }.getResult();
    }

    /**
     * 批量的{@link #hashMultipleSet(String, Map)}，在管道中执行
     *
     * @param pairs
     *            多个hash的多个field
     * @return 操作状态的集合
     */
    @Override
    public List<Object> batchHashMultipleSet(final List<Pair<String, Map<String, String>>> pairs) {
        return new Executor<List<Object>>() {

            @Override
            List<Object> execute() {
                List<Object> list = new ArrayList<>();
                if (pairs != null && pairs.size() > 0) {
                    for (Pair<String, Map<String, String>> pair : pairs) {
                        String key = pair.getKey();
                        Map<String, String> value = pair.getValue();
                        if (value != null) {
                            value.forEach((k, v) -> {
                                Long hset = jedisCluster.hset(key, k, v);
                                list.add(hset);
                            });
                        }
                    }
                }
                return list;
            }
        }.getResult();
    }

    /**
     * 批量的{@link #hashMultipleSet(String, Map)}，在管道中执行
     *
     * @param data
     *            Map<String, Map<String, String>>格式的数据
     * @return 操作状态的集合
     */
    @Override
    public List<Object> batchHashMultipleSet(final Map<String, Map<String, String>> data) {
        return new Executor<List<Object>>() {

            @Override
            List<Object> execute() {
                List<Object> list = new ArrayList<>();
                if (data != null && data.size() > 0) {
                    data.forEach((key, value) -> {
                        if (value != null) {
                            value.forEach((k, v) -> {
                                Long hset = jedisCluster.hset(key, k, v);
                                list.add(hset);
                            });
                        }
                    });
                }
                return list;
            }
        }.getResult();
    }

    @Override
    public List<List<String>> batchHashMultipleGet(List<Pair<String, String[]>> pairs) {
        return new Executor<List<List<String>>>() {

            @Override
            List<List<String>> execute() {
                List<List<String>> list = new ArrayList<>();
                if (pairs != null && pairs.size() > 0) {
                    for (Pair<String, String[]> pair : pairs) {
                        String key = pair.getKey();
                        String[] value = pair.getValue();
                        if (value != null) {
                            for (String s : value) {
                                List<String> hmget = jedisCluster.hmget(key, s);
                                list.add(hmget);
                            }
                        }
                    }
                }
                return list;
            }
        }.getResult();
    }

    /**
     * 返回哈希表 key 中，所有的域和值。在返回值里，紧跟每个域名(field name)之后是域的值(value)，所以返回值的长度是哈希表大小的两倍。 时间复杂度: O(N)
     *
     * @param key
     *            key
     * @return 以列表形式返回哈希表的域和域的值。若 key 不存在，返回空列表。
     */
    @Override
    public Map<String, String> hashGetAll(final String key) {
        return new Executor<Map<String, String>>() {

            @Override
            Map<String, String> execute() {
                return jedisCluster.hgetAll(key);
            }
        }.getResult();
    }

    @Override
    public void batchSetExpire(List<PairEx<String, Void, Integer>> pairDatas) {
        if (pairDatas == null || pairDatas.size() == 0) {
            return;
        }
        new Executor<Void>() {
            @Override
            Void execute() {
                for (PairEx<String, Void, Integer> pairEx : pairDatas) {
                    jedisCluster.expire(pairEx.getKey(), pairEx.getExpire());
                }
                return null;
            }
        }.getResult();
    }

    /**
     * 返回哈希表 key 中，所有的域和值。在返回值里，紧跟每个域名(field name)之后是域的值(value)，所以返回值的长度是哈希表大小的两倍。 同时设置这个 key 的生存时间
     *
     * @param key
     *            key
     * @param expire
     *            生命周期，单位为秒
     * @return 以列表形式返回哈希表的域和域的值。若 key 不存在，返回空列表。
     */
    @Override
    public Map<String, String> hashGetAll(final String key, final int expire) {
        return new Executor<Map<String, String>>() {

            @Override
            Map<String, String> execute() {
                Map<String, String> stringStringMap = jedisCluster.hgetAll(key);
                jedisCluster.expire(key, expire);
                return stringStringMap;
            }
        }.getResult();
    }

    /**
     * 批量的{@link #hashGetAll(String)}
     *
     * @param keys
     *            key的数组
     * @return 执行结果的集合
     */
    @Override
    public List<Map<String, String>> batchHashGetAll(final String... keys) {
        return new Executor<List<Map<String, String>>>() {

            @Override
            List<Map<String, String>> execute() {
                List<Map<String, String>> result = new ArrayList<>(keys.length);
                for (String key : keys) {
                    Map<String, String> stringStringMap = jedisCluster.hgetAll(key);
                    result.add(stringStringMap);
                }
                return result;
            }
        }.getResult();
    }

    /**
     * 批量的{@link #hashMultipleGet(String, String...)}，与{@link #batchHashGetAll(String...)}不同的是，返回值为Map类型
     *
     * @param keys
     *            key的数组
     * @return 多个hash的所有filed和value
     */
    @Override
    public Map<String, Map<String, String>> batchHashGetAllForMap(final String... keys) {
        return new Executor<Map<String, Map<String, String>>>() {

            @Override
            Map<String, Map<String, String>> execute() {
                // 设置map容量防止rehash
                int capacity = 1;
                while ((int)(capacity * 0.75) <= keys.length) {
                    capacity <<= 1;
                }
                Map<String, Map<String, String>> result = new HashMap<>(capacity);
                for (String key : keys) {
                    Map<String, String> stringStringMap = jedisCluster.hgetAll(key);
                    result.put(key, stringStringMap);
                }
                return result;
            }
        }.getResult();
    }

    /**
     * 删除哈希表 key 中给定域 fields 的值。 时间复杂度:O(fields)
     *
     * @param key
     *            哈希表 key
     * @param fields
     *            哈希表的field
     * @return 1-fields存在并成功删除，0-不存在不做任何操作 。
     */
    @Override
    public Long hashDel(final String key, final String[] fields) {
        return new Executor<Long>() {
            @Override
            Long execute() {
                return jedisCluster.hdel(key, fields);
            }
        }.getResult();
    }

    /* ======================================List====================================== */

    /**
     * 将一个或多个值 value 插入到列表 key 的表尾(最右边)。
     *
     * @param key
     *            key
     * @param values
     *            value的数组
     * @return 执行 listPushTail 操作后，表的长度
     */
    @Override
    public Long listPushTail(final String key, final String... values) {
        return new Executor<Long>() {

            @Override
            Long execute() {
                return jedisCluster.rpush(key, values);
            }
        }.getResult();
    }

    /**
     * 将一个或多个值 value 插入到列表 key 的表头
     *
     * @param key
     *            key
     * @param value
     *            string value
     * @return 执行 listPushHead 命令后，列表的长度。
     */
    @Override
    public Long listPushHead(final String key, final String value) {
        return new Executor<Long>() {

            @Override
            Long execute() {
                return jedisCluster.lpush(key, value);
            }
        }.getResult();
    }

    /**
     * 从集合中删除值为value的指定元素;
     *
     * @param key
     * @param count
     * @param value
     * @return
     */
    @Override
    public Long listRemove(final String key, int count, final String value) {
        return new Executor<Long>() {

            @Override
            Long execute() {
                return jedisCluster.lrem(key, count, value);
            }
        }.getResult();
    }

    /**
     * 将一个或多个值 value 插入到列表 key 的表头, 当列表大于指定长度是就对列表进行修剪(trim)
     *
     * @param key
     *            key
     * @param value
     *            string value
     * @param size
     *            链表超过这个长度就修剪元素
     * @return 执行 listPushHeadAndTrim 命令后，列表的长度。
     */
    @Override
    public Long listPushHeadAndTrim(String key, String value, long size) {
        return new Executor<Long>() {
            @Override
            Long execute() {
                Long lpush = jedisCluster.lpush(key, value);
                jedisCluster.ltrim(key, 0, size - 1);
                return lpush;
            }
        }.getResult();
    }

    /**
     * 批量的{@link #listPushTail(String, String...)}，以锁的方式实现
     *
     * @param key
     *            key
     * @param values
     *            value的数组
     * @param delOld
     *            如果key存在，是否删除它。true 删除；false: 不删除，只是在行尾追加
     */
    @Override
    public void batchListPushTail(String key, String[] values, boolean delOld) {
        new Executor<Object>() {
            @Override
            Object execute() {
                if (delOld) {
                    RedisClusterLock redisClusterLock = new RedisClusterLock(jedisCluster);
                    String s = UUID.randomUUID().toString();
                    try {
                        Boolean aBoolean = redisClusterLock.setLockOfCluster(key + "_lock", s, 10000);
                        if (!aBoolean) {
                            return false;
                        }
                        jedisCluster.del(key);
                        for (String value : values) {
                            jedisCluster.rpush(key, value);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    } finally {
                        redisClusterLock.unLockOfCluster(key + "_lock", s);
                    }
                } else {
                    for (String value : values) {
                        jedisCluster.rpush(key, value);
                    }
                }
                return null;
            }
        }.getResult();
    }

    /**
     * 同{@link #batchListPushTail(String, String[], boolean)},不同的是利用redis的事务特性来实现
     *
     * @param key
     *            key
     * @param values
     *            value的数组
     * @return null
     */
    @Override
    @Deprecated
    public Object updateListInTransaction(String key, List<String> values) {
        // 集群不支持事务
        return null;
    }

    @Override
    public Long insertListIfNotExists(String key, String[] values) {

        return new Executor<Long>() {
            @Override
            Long execute() {
                RedisClusterLock redisClusterLock = new RedisClusterLock(jedisCluster);
                String s = UUID.randomUUID().toString();
                try {
                    Boolean aBoolean = redisClusterLock.setLockOfCluster(key + "_lock", s, 10000);
                    if (!aBoolean) {
                        return 0L;
                    }
                    if (!jedisCluster.exists(key)) {
                        return jedisCluster.rpush(key, values);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    redisClusterLock.unLockOfCluster(key + "_lock", s);
                }
                return 0L;
            }
        }.getResult();
    }

    /**
     * 返回list所有元素，下标从0开始，负值表示从后面计算，-1表示倒数第一个元素，key不存在返回空列表
     *
     * @param key
     *            key
     * @return list所有元素
     */
    @Override
    public List<String> listGetAll(final String key) {
        return new Executor<List<String>>() {

            @Override
            List<String> execute() {
                return jedisCluster.lrange(key, 0, -1);
            }
        }.getResult();
    }

    /**
     * 返回指定区间内的元素，下标从0开始，负值表示从后面计算，-1表示倒数第一个元素，key不存在返回空列表
     *
     * @param key
     *            key
     * @param beginIndex
     *            下标开始索引（包含）
     * @param endIndex
     *            下标结束索引（不包含）
     * @return 指定区间内的元素
     */
    @Override
    public List<String> listRange(final String key, final long beginIndex, final long endIndex) {
        return new Executor<List<String>>() {
            @Override
            List<String> execute() {
                return jedisCluster.lrange(key, beginIndex, endIndex - 1);
            }
        }.getResult();
    }

    /**
     * 一次获得多个链表的数据
     *
     * @param keys
     *            key的数组
     * @return 执行结果
     */
    @Override
    public Map<String, List<String>> batchGetAllList(final List<String> keys) {
        return new Executor<Map<String, List<String>>>() {
            @Override
            Map<String, List<String>> execute() {
                Map<String, List<String>> result = new HashMap<>();
                for (String key : keys) {
                    List<String> lrange = jedisCluster.lrange(key, 0, -1);
                    result.put(key, lrange);
                }
                return result;
            }
        }.getResult();
    }

    /**
     * 往有序集合sortSet中添加数据;
     *
     * @param key
     * @param score
     *            要排序的值
     * @param value
     * @return
     */
    @Override
    public Long sortSetPush(final String key, final double score, final String value) {
        return new Executor<Long>() {
            @Override
            Long execute() {
                return jedisCluster.zadd(key, score, value);
            }
        }.getResult();
    }

    /**
     *
     * @param key
     * @param value
     * @return
     */
    @Override
    public Long sortRemove(final String key, final String value) {
        return new Executor<Long>() {

            @Override
            Long execute() {
                return jedisCluster.zrem(key, value);
            }
        }.getResult();
    }

    /**
     * 根据Score获取集合区间数据;
     *
     * @param key
     * @param min
     *            score区间最小值
     * @param max
     *            scroe区间最大值
     * @return
     */
    @Override
    public Set<String> sorSetRangeByScore(final String key, final double min, final double max) {
        return new Executor<Set<String>>() {

            @Override
            Set<String> execute() {
                return jedisCluster.zrangeByScore(key, min, max);
            }
        }.getResult();
    }

    /**
     * 根据Score获取集合区间数据;
     *
     * @param key
     * @param min
     *            score区间最小值
     * @param max
     *            scroe区间最大值
     * @param offset
     *            偏移量（类似LIMIT 0,10）
     * @param count
     *            数量
     * @return
     */
    @Override
    public Set<String> sorSetRangeByScore(final String key, final double min, final double max, final int offset,
        final int count) {
        return new Executor<Set<String>>() {

            @Override
            Set<String> execute() {
                return jedisCluster.zrangeByScore(key, min, max, offset, count);
            }
        }.getResult();
    }

    /**
     *
     * @param channel
     *            频道
     * @param message
     *            信息
     * @return
     */
    @Override
    public Long publish(String channel, String message) {
        return null;
    }

    @Override
    public void publishAll(String channel, List<String> messages) {

    }

    @Override
    public void subscribe(JedisPubSub jedisPubSub, String channel) {

    }

    @Override
    public void unSubscribe(JedisPubSub jedisPubSub) {

    }

    /* ======================================Sorted set================================= */

    /**
     * 将一个 member 元素及其 score 值加入到有序集 key 当中。
     *
     * @param key
     *            key
     * @param score
     *            score 值可以是整数值或双精度浮点数。
     * @param member
     *            有序集的成员
     * @return 被成功添加的新成员的数量，不包括那些被更新的、已经存在的成员。
     */
    @Override
    public Long addWithSortedSet(final String key, final double score, final String member) {
        return new Executor<Long>() {

            @Override
            Long execute() {
                return jedisCluster.zadd(key, score, member);
            }
        }.getResult();
    }

    /**
     * 将多个 member 元素及其 score 值加入到有序集 key 当中。
     *
     * @param key
     *            key
     * @param scoreMembers
     *            score、member的pair
     * @return 被成功添加的新成员的数量，不包括那些被更新的、已经存在的成员。
     */
    @Override
    public Long addWithSortedSet(final String key, final Map<String, Double> scoreMembers) {
        return new Executor<Long>() {
            @Override
            Long execute() {
                return jedisCluster.zadd(key, scoreMembers);
            }
        }.getResult();
    }

    /**
     * 返回有序集 key 中， score 值介于 max 和 min 之间(默认包括等于 max 或 min )的所有的成员。 有序集成员按 score 值递减(从大到小)的次序排列。
     *
     * @param key
     *            key
     * @param max
     *            score最大值
     * @param min
     *            score最小值
     * @return 指定区间内，带有 score 值(可选)的有序集成员的列表
     */
    @Override
    public Set<String> revrangeByScoreWithSortedSet(final String key, final double max, final double min) {
        return new Executor<Set<String>>() {

            @Override
            Set<String> execute() {
                return jedisCluster.zrevrangeByScore(key, max, min);
            }
        }.getResult();
    }

    /**
     * 构造Pair键值对
     *
     * @param key
     *            key
     * @param value
     *            value
     * @return 键值对
     */
    public <K, V> Pair<K, V> makePair(K key, V value) {
        return new Pair<K, V>(key, value);
    }

    /**
     * 构造Pair键值对
     *
     * @param key
     *            key
     * @param value
     *            value
     * @param expire
     *            expire
     * @return 键值对
     */
    @Override
    public <K, V, E> PairEx<K, V, E> makePairEx(K key, V value, E expire) {
        return new PairEx<K, V, E>(key, value, expire);
    }

    /**
     * @author dave 向缓存中设置对象
     * @param key
     * @param value
     * @return
     */
    @Override
    public Object set(final String key, final Object value) {
        return new Executor<Object>() {
            @Override
            Object execute() {
                String objectJson = JSON.toJSONString(value);
                return jedisCluster.set(key, objectJson);
            }
        }.getResult();
    }

    /**
     * @author dave 向缓存中设置对象有有效期
     * @param key
     * @param value
     * @param expire
     * @return
     */
    @Override
    public Object set(final String key, final Object value, final int expire) {
        return new Executor<Object>() {
            @Override
            Object execute() {
                String objectJson = JSON.toJSONString(value);
                return jedisCluster.setex(key, expire, objectJson);
            }
        }.getResult();
    }

    /**
     * @author dave 根据key 获取对象
     * @param key
     * @return
     */
    @Override
    public <T> T get(final String key, final Class<T> clazz) {
        return new Executor<T>() {
            @Override
            T execute() {
                String json = jedisCluster.get(key);
                return JSON.parseObject(json, clazz);
            }
        }.getResult();
    }
}
