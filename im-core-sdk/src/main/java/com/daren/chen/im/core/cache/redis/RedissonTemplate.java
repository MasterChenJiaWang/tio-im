package com.daren.chen.im.core.cache.redis;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.StringUtils;
import org.redisson.Redisson;
import org.redisson.api.RAtomicLong;
import org.redisson.api.RBucket;
import org.redisson.api.RBuckets;
import org.redisson.api.RKeys;
import org.redisson.api.RList;
import org.redisson.api.RLock;
import org.redisson.api.RMap;
import org.redisson.api.RScoredSortedSet;
import org.redisson.api.RTopic;
import org.redisson.api.RedissonClient;
import org.redisson.client.codec.StringCodec;
import org.redisson.config.ClusterServersConfig;
import org.redisson.config.Config;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSON;
import com.daren.chen.im.core.ImConst;

import redis.clients.jedis.JedisPubSub;

/**
 * @author WChao
 * @date 2018年5月18日 下午2:46:55
 */
public class RedissonTemplate implements BaseJedisTemplate, Serializable {

    private static final long serialVersionUID = -4528751601700736437L;
    private static final Logger logger = LoggerFactory.getLogger(RedissonTemplate.class);
    private static volatile RedissonTemplate instance = null;
    private static RedisConfiguration redisConfig = null;
    private static final String REDIS = "redis";
    private static RedissonClient redissonClient = null;

    private RedissonTemplate() {}

    ;

    public static RedissonTemplate me() throws Exception {
        if (instance == null) {
            synchronized (RedissonTemplate.class) {
                if (instance == null) {
                    redisConfig = RedisConfigurationFactory.parseConfiguration();
                    init();
                    instance = new RedissonTemplate();
                }
            }
        }
        return instance;
    }

    private static void init() throws Exception {
        try {
            if (redisConfig.isCluster()) {
                String clusterAddr = redisConfig.getClusterAddr();
                if (StringUtils.isBlank(clusterAddr)) {
                    logger.error("the server Cluster Addr of redis must be not null!");
                    throw new Exception("the server Cluster Addr of redis must be not null!");
                }
                Config redissonConfig = new Config();
                // 与jedis兼容 使用相同编码
                redissonConfig.setCodec(new StringCodec());
                // redissonConfig.setCodec(new JsonJacksonCodec());
                ClusterServersConfig clusterServersConfig = redissonConfig.useClusterServers();
                String[] split = clusterAddr.split(",");
                if (split.length == 0) {
                    throw new Exception("the server Cluster Addr of redis must be not null!");
                }
                String[] strings = new String[split.length];
                for (int i = 0; i < split.length; i++) {
                    strings[i] = REDIS + "://" + split[i];
                }
                clusterServersConfig.addNodeAddress(strings).setPassword(redisConfig.getAuth())
                    .setTimeout(redisConfig.getTimeout()).setRetryAttempts(redisConfig.getRetryNum());
                redissonClient = Redisson.create(redissonConfig);
            } else {
                String host = redisConfig.getHost();
                logger.info("connect redis[{}]", ImConst.JIM);
                if (host == null) {
                    logger.error("the server ip of redis must be not null!");
                    throw new Exception("the server ip of redis must be not null!");
                }
                int port = redisConfig.getPort();
                String password = redisConfig.getAuth();
                Config redissonConfig = new Config();
                // 与jedis 兼容 使用相同编码
                redissonConfig.setCodec(new StringCodec());
                // redissonConfig.setCodec(new JsonJacksonCodec());
                redissonConfig.useSingleServer().setAddress(REDIS + "://" + host + ":" + port).setPassword(password)
                    .setDatabase(redisConfig.getDatabase()).setTimeout(redisConfig.getTimeout())
                    .setRetryAttempts(redisConfig.getRetryNum());
                redissonClient = Redisson.create(redissonConfig);
            }

        } catch (Exception e) {
            logger.error("can't create RedissonClient for server" + redisConfig.getHost());
            throw new Exception("can't create RedissonClient for server" + redisConfig.getHost());
        }

    }

    /**
     * 获取RedissonClient客户端;
     *
     * @return
     */
    public final RedissonClient getRedissonClient() {
        return redissonClient;
    }

    /**
     * 模糊获取所有的key
     *
     * @return
     */
    @Override
    public Set<String> keys(String likeKey) {
        RKeys keys = redissonClient.getKeys();
        Iterable<String> keysByPattern = keys.getKeysByPattern(likeKey + "*");
        Set<String> set = new HashSet<>();
        for (String s : keysByPattern) {
            set.add(s);
        }
        return set;
    }

    @Override
    public long delKeysLike(String likeKey) {
        RKeys keys = redissonClient.getKeys();
        return keys.deleteByPattern(likeKey + "*");
    }

    @Override
    public Long delKey(String key) {
        RKeys keys = redissonClient.getKeys();
        return keys.delete(key);
    }

    @Override
    public Long delKeys(String[] keys) {
        RKeys keys2 = redissonClient.getKeys();
        return keys2.delete(keys);
    }

    @Override
    public Long expire(String key, int expire) {
        RBucket<Object> bucket = redissonClient.getBucket(key);
        bucket.expire(expire, TimeUnit.SECONDS);
        return 1L;
    }

    @Override
    public long makeId(String key) {
        RAtomicLong atomicLong = redissonClient.getAtomicLong(key);
        return atomicLong.incrementAndGet();
    }

    @Override
    public long decr(String key, long increment) {
        RLock rLock = redissonClient.getLock(key + "_lock");
        try {
            rLock.lock(10, TimeUnit.SECONDS);
            RAtomicLong atomicLong = redissonClient.getAtomicLong(key);
            for (long i = 0; i < increment; i++) {
                atomicLong.decrementAndGet();
            }
            return atomicLong.get();
        } finally {
            if (rLock != null) {
                rLock.unlock();
            }
        }
    }

    @Override
    public long incr(String key, long increment) {
        RLock rLock = redissonClient.getLock(key + "_lock");
        try {
            rLock.lock(10, TimeUnit.SECONDS);
            RAtomicLong atomicLong = redissonClient.getAtomicLong(key);
            for (long i = 0; i < increment; i++) {
                atomicLong.incrementAndGet();
            }
            return atomicLong.get();
        } finally {
            if (rLock != null) {
                rLock.unlock();
            }
        }
    }

    @Override
    public String setString(String key, String value) {
        RBucket<String> bucket = redissonClient.getBucket(key);
        bucket.set(value);
        return "OK";
    }

    @Override
    public boolean setString2Boolean(String key, String value, int expire) {
        RBucket<String> bucket = redissonClient.getBucket(key);
        bucket.set(value, expire, TimeUnit.SECONDS);
        return true;
    }

    @Override
    public String setString(String key, String value, int expire) {
        RBucket<String> bucket = redissonClient.getBucket(key);
        bucket.set(value, expire, TimeUnit.SECONDS);
        return "OK";
    }

    @Override
    public Long setStringIfNotExists(String key, String value) {
        RBucket<String> bucket = redissonClient.getBucket(key);
        boolean b = bucket.trySet(value);
        return b ? 1L : 0;
    }

    @Override
    public boolean setStringIfNotExists(String key, String value, int timeout) {
        RBucket<String> bucket = redissonClient.getBucket(key);
        return bucket.trySet(value, timeout, TimeUnit.SECONDS);
    }

    @Override
    public String getString(String key) {
        RBucket<String> bucket = redissonClient.getBucket(key);
        return bucket.get();
    }

    @Override
    public List<Object> batchSetString(List<Pair<String, String>> pairs) {
        if (pairs == null || pairs.size() == 0) {
            return new ArrayList<>();
        }

        List<Object> objects = new ArrayList<>(pairs.size());
        for (Pair<String, String> pair : pairs) {
            RBucket<String> bucket = redissonClient.getBucket(pair.getKey());
            bucket.set(pair.getValue());
            objects.add("OK");
        }
        return objects;
    }

    @Override
    public List<Object> batchSetStringEx(List<PairEx<String, String, Integer>> pairs) {
        if (pairs == null || pairs.size() == 0) {
            return new ArrayList<>();
        }
        List<Object> objects = new ArrayList<>(pairs.size());
        for (PairEx<String, String, Integer> pair : pairs) {
            RBucket<String> bucket = redissonClient.getBucket(pair.getKey());
            bucket.set(pair.getValue(), pair.getExpire(), TimeUnit.SECONDS);
            objects.add("OK");
        }
        return objects;
    }

    @Override
    public List<String> batchGetString(String[] keys) {
        RBuckets buckets = redissonClient.getBuckets();
        Map<String, String> map = buckets.get(keys);
        if (map != null) {
            return (List<String>)map.values();
        }
        return new ArrayList<>();
    }

    @Override
    public Long hashSet(String key, String field, String value) {
        RMap<String, String> map = redissonClient.getMap(key);
        String put1 = map.put(field, value);
        return StringUtils.isNotBlank(put1) ? 1L : 0L;
    }

    @Override
    public Long hashSet(String key, String field, String value, int expire) {
        RMap<String, String> map = redissonClient.getMap(key);
        String put1 = map.put(field, value);
        map.expire(expire, TimeUnit.SECONDS);
        return StringUtils.isNotBlank(put1) ? 1L : 0L;
    }

    @Override
    public String hashGet(String key, String field) {
        RMap<String, String> map = redissonClient.getMap(key);
        return map.get(field);
    }

    @Override
    public String hashGet(String key, String field, int expire) {
        RMap<String, String> map = redissonClient.getMap(key);
        map.expire(expire, TimeUnit.SECONDS);
        return map.get(field);
    }

    @Override
    public String hashMultipleSet(String key, Map<String, String> hash) {
        RMap<String, String> map = redissonClient.getMap(key);
        if (hash != null && !hash.isEmpty()) {
            hash.forEach(map::put);
        }
        return "OK";

    }

    @Override
    public String hashMultipleSet(String key, Map<String, String> hash, int expire) {
        RMap<String, String> map = redissonClient.getMap(key);
        if (hash != null && !hash.isEmpty()) {
            hash.forEach(map::put);
        }
        map.expire(expire, TimeUnit.SECONDS);
        return "OK";
    }

    @Override
    public List<String> hashMultipleGet(String key, String... fields) {
        RMap<String, String> map = redissonClient.getMap(key);
        List<String> list = new ArrayList<>();
        if (fields == null || fields.length == 0) {
            return list;
        }
        for (String field : fields) {
            list.add(map.get(field));
        }
        return list;
    }

    @Override
    public List<String> hashMultipleGet(String key, int expire, String... fields) {
        RMap<String, String> map = redissonClient.getMap(key);
        map.expire(expire, TimeUnit.SECONDS);
        List<String> list = new ArrayList<>();
        if (fields == null || fields.length == 0) {
            return list;
        }
        for (String field : fields) {
            list.add(map.get(field));
        }
        return list;
    }

    @Override
    public List<Object> batchHashMultipleSet(List<Pair<String, Map<String, String>>> pairs) {
        if (pairs == null || pairs.size() == 0) {
            return new ArrayList<>();
        }
        List<Object> objects = new ArrayList<>();
        for (Pair<String, Map<String, String>> pair : pairs) {
            String s = this.hashMultipleSet(pair.getKey(), pair.getValue());
            objects.add(s);
        }
        return objects;
    }

    @Override
    public List<Object> batchHashMultipleSet(Map<String, Map<String, String>> data) {
        if (data == null || data.size() == 0) {
            return new ArrayList<>();
        }
        List<Object> objects = new ArrayList<>();
        data.forEach((k, v) -> {
            String s = this.hashMultipleSet(k, v);
            objects.add(s);
        });
        return objects;
    }

    @Override
    public List<List<String>> batchHashMultipleGet(List<Pair<String, String[]>> pairs) {
        if (pairs == null || pairs.size() == 0) {
            return new ArrayList<>();
        }
        List<List<String>> lists = new ArrayList<>();
        for (Pair<String, String[]> pair : pairs) {
            List<String> list = this.hashMultipleGet(pair.getKey(), pair.getValue());
            lists.add(list);
        }
        return lists;
    }

    @Override
    public Map<String, String> hashGetAll(String key) {
        return redissonClient.getMap(key);
    }

    @Override
    public void batchSetExpire(List<PairEx<String, Void, Integer>> pairDatas) {
        if (pairDatas == null || pairDatas.size() == 0) {
            return;
        }
        for (PairEx<String, Void, Integer> pairEx : pairDatas) {
            RBucket<Object> bucket = redissonClient.getBucket(pairEx.getKey());
            bucket.set(pairEx.getValue(), pairEx.getExpire(), TimeUnit.SECONDS);
        }
    }

    @Override
    public Map<String, String> hashGetAll(String key, int expire) {
        RMap<String, String> map = redissonClient.getMap(key);
        map.expire(expire, TimeUnit.SECONDS);
        return map;
    }

    @Override
    public List<Map<String, String>> batchHashGetAll(String... keys) {
        if (keys == null || keys.length == 0) {
            return new ArrayList<>();
        }
        List<Map<String, String>> objects = new ArrayList<>();
        for (String key : keys) {
            Map<String, String> map = redissonClient.getMap(key);
            objects.add(map);
        }
        return objects;
    }

    @Override
    public Map<String, Map<String, String>> batchHashGetAllForMap(String... keys) {
        if (keys == null || keys.length == 0) {
            return new HashMap<>();
        }
        Map<String, Map<String, String>> objectObjectHashMap = new HashMap<>();
        for (String key : keys) {
            Map<String, String> map = redissonClient.getMap(key);
            objectObjectHashMap.put(key, map);
        }
        return objectObjectHashMap;
    }

    @Override
    public Long hashDel(String key, String[] fields) {
        if (fields == null || fields.length == 0) {
            return 0L;
        }
        RMap<Object, Object> map = redissonClient.getMap(key);
        for (String field : fields) {
            map.remove(field);
        }
        return (long)fields.length;
    }

    @Override
    public Long listPushTail(String key, String... values) {
        if (values == null || values.length == 0) {
            return 0L;
        }
        RList<String> list = redissonClient.getList(key);
        Collections.addAll(list, values);
        return (long)list.size();
    }

    @Override
    public Long listPushHead(String key, String value) {
        RList<String> list = redissonClient.getList(key);
        list.add(0, value);
        return (long)list.size();
    }

    @Override
    public Long listRemove(String key, int count, String value) {
        RList<String> list = redissonClient.getList(key);
        if (0 == count) {
            list.remove(value);
            return 1L;
        } else if (count > 0) {
            long i = 0;
            for (String o : list) {
                if (value.equals(o)) {
                    list.remove(o);
                    i++;
                }
            }
            return i;
        } else {
            long i = 0;
            for (int i1 = list.size() - 1; i1 >= 0; i1--) {
                if (value.equals(list.get(i1))) {
                    list.remove(list.get(i1));
                    i++;
                }
            }
            return i;
        }
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
        RList<String> list = redissonClient.getList(key);
        list.add(0, value);
        int size1 = list.size();
        if (size1 > size) {
            list.trim(0, (int)size - 1);
        }
        return (long)list.size();
    }

    @Override
    public void batchListPushTail(String key, String[] values, boolean delOld) {
        if (delOld) {
            RLock lock = redissonClient.getLock(key + "_lock");
            try {
                // 尝试加锁，最多等待20秒，上锁以后10秒自动解锁
                lock.tryLock(20, 10, TimeUnit.SECONDS);
                if (values == null || values.length == 0) {
                    return;
                }
                RList<String> list = redissonClient.getList(key);
                list.delete();
                Collections.addAll(list, values);
            } catch (InterruptedException e) {
                e.printStackTrace();
            } finally {
                lock.unlock();
            }
        } else {
            if (values == null || values.length == 0) {
                return;
            }
            RList<String> list = redissonClient.getList(key);
            Collections.addAll(list, values);
        }
    }

    @Override
    public Object updateListInTransaction(String key, List<String> values) {
        // 不支持 list 事务操作
        // RTransaction transaction = redissonClient.createTransaction(TransactionOptions.defaults());
        // RSet<String> set = transaction.getSet(key);
        // set.delete();
        // set.addAll(values);
        // try {
        // transaction.commit();
        // } catch (TransactionException e) {
        // transaction.rollback();
        // }
        RList<String> list = redissonClient.getList(key);
        list.addAll(values);
        return null;
    }

    @Override
    public Long insertListIfNotExists(String key, String[] values) {
        RLock lock = redissonClient.getLock(key + "_lock");

        try {
            lock.lock(10, TimeUnit.SECONDS);
            RList<String> list1 = redissonClient.getList(key);
            if (list1.isExists()) {
                return (long)list1.size();
            }
            if (values == null || values.length == 0) {
                return (long)list1.size();
            }
            Collections.addAll(list1, values);
            return (long)list1.size();
        } finally {
            lock.unlock();
        }
    }

    @Override
    public List<String> listGetAll(String key) {
        return redissonClient.getList(key);
    }

    @Override
    public List<String> listRange(String key, long beginIndex, long endIndex) {
        RList<String> list = redissonClient.getList(key);
        return list.subList((int)beginIndex, (int)endIndex);
    }

    @Override
    public Map<String, List<String>> batchGetAllList(List<String> keys) {
        if (keys == null || keys.size() == 0) {
            return null;
        }
        Map<String, List<String>> hashMap = new HashMap<>();
        for (String key : keys) {
            RList<String> list = redissonClient.getList(key);
            hashMap.put(key, list);
        }
        return hashMap;
    }

    @Override
    public Long sortSetPush(String key, double score, String value) {
        RScoredSortedSet<String> scoredSortedSet = redissonClient.getScoredSortedSet(key);
        scoredSortedSet.add(score, value);
        return 1L;
    }

    @Override
    public Long sortRemove(String key, String value) {
        RScoredSortedSet<String> scoredSortedSet = redissonClient.getScoredSortedSet(key);
        if (!scoredSortedSet.isEmpty()) {
            scoredSortedSet.remove(value);
        }
        return 1L;
    }

    @Override
    public Set<String> sorSetRangeByScore(String key, double min, double max) {
        RScoredSortedSet<String> scoredSortedSet = redissonClient.getScoredSortedSet(key);
        Collection<String> strings = scoredSortedSet.valueRange(min, true, max, true);
        return new LinkedHashSet<>(strings);
    }

    @Override
    public Set<String> sorSetRangeByScore(String key, double min, double max, int offset, int count) {
        RScoredSortedSet<String> scoredSortedSet = redissonClient.getScoredSortedSet(key);
        Collection<String> strings = scoredSortedSet.valueRange(min, true, max, true, offset, count);
        return new LinkedHashSet<>(strings);
    }

    @Override
    public Long publish(String channel, String message) {
        RTopic topic = redissonClient.getTopic(channel);
        return topic.publish(message);
    }

    @Override
    public void publishAll(String channel, List<String> messages) {
        if (messages == null || messages.size() == 0) {
            return;
        }
        RTopic topic = redissonClient.getTopic(channel);
        for (String message : messages) {
            topic.publish(message);
        }
    }

    @Override
    public void subscribe(JedisPubSub jedisPubSub, String channel) {}

    @Override
    public void unSubscribe(JedisPubSub jedisPubSub) {}

    @Override
    public Long addWithSortedSet(String key, double score, String member) {
        RScoredSortedSet<String> scoredSortedSet = redissonClient.getScoredSortedSet(key);
        scoredSortedSet.add(score, member);
        return 1L;
    }

    @Override
    public Long addWithSortedSet(String key, Map<String, Double> scoreMembers) {
        RScoredSortedSet<String> scoredSortedSet = redissonClient.getScoredSortedSet(key);
        scoreMembers.forEach((k, v) -> {
            scoredSortedSet.add(v, k);
        });
        return 1L;
    }

    @Override
    public Set<String> revrangeByScoreWithSortedSet(String key, double max, double min) {
        RScoredSortedSet<String> scoredSortedSet = redissonClient.getScoredSortedSet(key);
        return (Set<String>)scoredSortedSet.valueRangeReversed(min, true, max, true);
    }

    @Override
    public <K, V, E> PairEx<K, V, E> makePairEx(K key, V value, E expire) {
        return new PairEx<K, V, E>(key, value, expire);
    }

    @Override
    public Object set(String key, Object value) {
        RBucket<String> bucket = redissonClient.getBucket(key);
        bucket.set(JSON.toJSONString(value));
        return "OK";
    }

    @Override
    public Object set(String key, Object value, int expire) {
        RBucket<String> bucket = redissonClient.getBucket(key);
        bucket.set(JSON.toJSONString(value), expire, TimeUnit.SECONDS);
        return "OK";
    }

    @Override
    public <T> T get(String key, Class<T> clazz) {
        try {
            RBucket<String> bucket = redissonClient.getBucket(key);
            String json = bucket.get();
            if (StringUtils.isBlank(json)) {
                return null;
            }
            return JSON.parseObject(json, clazz);
        } catch (Exception e) {
            e.printStackTrace();
            logger.info("key = {}", key);
            return null;
        }
    }
}
