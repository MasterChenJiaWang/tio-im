package com.daren.chen.im.server.springboot.http;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import com.daren.chen.im.core.cache.redis.JedisClusterTemplate;
import com.daren.chen.im.core.cache.redis.Pair;
import com.daren.chen.im.core.cache.redis.RedissonTemplate;
import com.daren.chen.im.core.packets.Group;

/**
 * @Description:
 * @author: chendaren
 * @CreateDate: 2021/1/14 10:31
 */
@SpringBootTest
public class RedisCulsTest {

    @Test
    public void keys() throws Exception {
        Set<String> test1 = JedisClusterTemplate.me().keys("group");
        System.out.println(test1.size());
        Set<String> group = RedissonTemplate.me().keys("group");
        System.out.println(group.size());
    }

    @Test
    public void delKeysLike() throws Exception {
        long group1 = JedisClusterTemplate.me().delKeysLike("lastId:group");
        System.out.println(group1);
        long group = RedissonTemplate.me().delKeysLike("lastId:user");
        System.out.println(group);
    }

    @Test
    public void delKey() throws Exception {
        long group1 = JedisClusterTemplate.me().delKey("userToken:13888800001");
        System.out.println(group1);
        long group = RedissonTemplate.me().delKey("userToken:13888800006");
        System.out.println(group);
    }

    @Test
    public void delKeys() throws Exception {
        long group1 =
            JedisClusterTemplate.me().delKeys(new String[] {"user:13888800001:info", "user:13888800002:info"});
        System.out.println(group1);
        long group = RedissonTemplate.me().delKeys(new String[] {"user:13888800003:info", "user:13888800004:info"});
        System.out.println(group);
    }

    @Test
    public void expire() throws Exception {
        long group1 = JedisClusterTemplate.me().expire("user:13888800001:group", 111);
        System.out.println(group1);
        long group = RedissonTemplate.me().expire("user:13888800002:group", 222);
        System.out.println(group);
    }

    @Test
    public void makeId() throws Exception {
        long group1 = JedisClusterTemplate.me().makeId("makeId:test1");
        System.out.println(group1);
        long group = RedissonTemplate.me().makeId("makeId:test1");
        System.out.println(group);
    }

    @Test
    public void decr() throws Exception {
        long group1 = 0;
        try {
            group1 = JedisClusterTemplate.me().decr("makeId:test2", 5);
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println(group1);

        long group = 0;
        try {
            group = RedissonTemplate.me().decr("makeId:test2", 10);
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println(group);

    }

    @Test
    public void incr() throws Exception {
        long group = 0;
        try {
            group = RedissonTemplate.me().incr("makeId:test4", 10);
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println(group);
        long group1 = 0;
        try {
            group1 = JedisClusterTemplate.me().incr("makeId:test4", 5);
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println(group1);

    }

    @Test
    public void setString() throws Exception {
        String test1 = JedisClusterTemplate.me().setString("setString:test1", "test1");
        System.out.println(test1);
        String test2 = RedissonTemplate.me().setString("setString:test2", "test2");
        System.out.println(test2);
    }

    @Test
    public void setString2Boolean() throws Exception {
        boolean test1 = JedisClusterTemplate.me().setString2Boolean("setString:test3", "test4", 100);
        System.out.println(test1);
        boolean test2 = RedissonTemplate.me().setString2Boolean("setString:test4", "test4", 100);
        System.out.println(test2);
    }

    @Test
    public void setString2() throws Exception {
        String test1 = JedisClusterTemplate.me().setString("setString:test5", "test5", 100);
        System.out.println(test1);
        String test2 = RedissonTemplate.me().setString("setString:test6", "test6", 100);
        System.out.println(test2);
    }

    @Test
    public void setStringIfNotExists() throws Exception {
        Long test1 = JedisClusterTemplate.me().setStringIfNotExists("setString:test5", "test5");
        System.out.println(test1);
        Long test2 = RedissonTemplate.me().setStringIfNotExists("setString:test6", "test6");
        System.out.println(test2);
        System.out.println("......");

        Long test3 = JedisClusterTemplate.me().setStringIfNotExists("setString:test7", "test7");
        System.out.println(test3);
        Long test4 = RedissonTemplate.me().setStringIfNotExists("setString:test8", "test8");
        System.out.println(test4);
    }

    @Test
    public void getString() throws Exception {
        String test1 = JedisClusterTemplate.me().getString("setString:test1");
        System.out.println(test1);
        String test2 = RedissonTemplate.me().getString("setString:test2");
        System.out.println(test2);
    }

    @Test
    public void batchSetString() throws Exception {
        List<Pair<String, String>> pairs = new ArrayList<>();
        pairs.add(new Pair<>("setString:test11", "11"));
        pairs.add(new Pair<>("setString:test12", "12"));
        List<Object> list = JedisClusterTemplate.me().batchSetString(pairs);
        System.out.println(list.size());
        List<Pair<String, String>> pairs2 = new ArrayList<>();
        pairs2.add(new Pair<>("setString:test13", "13"));
        pairs2.add(new Pair<>("setString:test14", "14"));
        List<Object> list2 = RedissonTemplate.me().batchSetString(pairs2);
        System.out.println(list2);
    }

    @Test
    public void hashSet() throws Exception {
        Long test1 = JedisClusterTemplate.me().hashSet("hashSet5", "test2", "2");
        System.out.println(test1);
        Long test2 = RedissonTemplate.me().hashSet("hashSet5", "test1", "1");
        System.out.println(test2);
    }

    @Test
    public void hashGet() throws Exception {
        Long test1 = JedisClusterTemplate.me().hashSet("hashSet5", "test3", "3");
        System.out.println(test1);
        String s = RedissonTemplate.me().hashGet("hashSet5", "test3");
        String s2 = RedissonTemplate.me().hashGet("hashSet5", "test1", 100);
        System.out.println(s);
        System.out.println(s2);
    }

    @Test
    public void hashMultipleSet() throws Exception {
        Map<String, String> hash = new HashMap<>();
        hash.put("test1", "1");
        hash.put("test2", "2");
        String test1 = JedisClusterTemplate.me().hashMultipleSet("hashSet6", hash);
        String test2 = JedisClusterTemplate.me().hashMultipleSet("hashSet7", hash, 100);
        System.out.println(test1);
        System.out.println(test2);
        Map<String, String> hash2 = new HashMap<>();
        hash2.put("test1", "1");
        hash2.put("test2", "2");
        String s = RedissonTemplate.me().hashMultipleSet("hashSet8", hash2);
        String s2 = RedissonTemplate.me().hashMultipleSet("hashSet9", hash2, 100);
        System.out.println(s);
        System.out.println(s2);
        System.out.println("....");

        List<String> list = JedisClusterTemplate.me().hashMultipleGet("hashSet7", "test1", "test2");
        for (String s1 : list) {
            System.out.println(s1);
        }
        System.out.println("<><><><>");
        List<String> list2 = JedisClusterTemplate.me().hashMultipleGet("hashSet6", 1000, "test1", "test2");
        for (String s1 : list2) {
            System.out.println(s1);
        }
        System.out.println("<><><><>");

        List<String> list3 = RedissonTemplate.me().hashMultipleGet("hashSet9", "test1", "test2");
        for (String s1 : list3) {
            System.out.println(s1);
        }
        System.out.println("<><><><>");
        List<String> list4 = RedissonTemplate.me().hashMultipleGet("hashSet8", 1000, "test1", "test2");
        for (String s1 : list4) {
            System.out.println(s1);
        }
    }

    @Test
    public void hashGetAll() throws Exception {
        Map<String, String> hashSet8 = JedisClusterTemplate.me().hashGetAll("hashSet8");
        hashSet8.forEach((k, v) -> {
            System.out.printf("%s ,  %s  \n", k, v);
        });
        System.out.println("...............");
        Map<String, String> hashSet6 = RedissonTemplate.me().hashGetAll("hashSet6");
        hashSet6.forEach((k, v) -> {
            System.out.printf("%s ,  %s  \n", k, v);
        });
    }

    @Test
    public void hashDel() throws Exception {
        JedisClusterTemplate.me().hashDel("hashSet8", new String[] {"test1"});
        System.out.println("...............");
        RedissonTemplate.me().hashDel("hashSet6", new String[] {"test2"});
    }

    @Test
    public void listPushTail() throws Exception {
        // for (int i = 1; i < 4; i++) {
        // Long test1 = JedisClusterTemplate.me().listPushTail("listPushTail" + i, "test1", "test2");
        // System.out.println(test1);
        // Long test2 = RedissonTemplate.me().listPushTail("listPushTail" + i, "test3", "test4");
        // System.out.println(test2);
        // Long test12 = JedisClusterTemplate.me().listPushTail("listPushTail" + i, "test1", "test2");
        // System.out.println(test12);
        // Long test22 = RedissonTemplate.me().listPushTail("listPushTail" + i, "test3", "test4");
        // System.out.println(test22);
        // }

        for (int i = 0; i < 10000; i++) {
            // Long test1 = JedisClusterTemplate.me().listPushTail("listPushTail1", "test1", "test2");
            // System.out.println(test1);
            Long test2 = RedissonTemplate.me().listPushTail("listPushTail1", "test" + i);
            System.out.println(test2);
        }

    }

    @Test
    public void listPushHead() throws Exception {
        Long test1 = JedisClusterTemplate.me().listPushHead("listPushTail1", "test1-new1");
        System.out.println(test1);
        Long test2 = RedissonTemplate.me().listPushHead("listPushTail1", "test1-new2");
        System.out.println(test2);

    }

    @Test
    public void listRemove() throws Exception {
        Long test1 = JedisClusterTemplate.me().listRemove("listPushTail1", 0, "test1");
        System.out.println(test1);
        Long test2 = RedissonTemplate.me().listRemove("listPushTail1", 0, "test2");
        System.out.println(test2);
        Long test3 = JedisClusterTemplate.me().listRemove("listPushTail2", 1, "test1");
        System.out.println(test3);
        Long test4 = RedissonTemplate.me().listRemove("listPushTail2", 1, "test2");
        System.out.println(test4);

        Long test5 = JedisClusterTemplate.me().listRemove("listPushTail3", 1, "test1");
        System.out.println(test5);
        Long test6 = RedissonTemplate.me().listRemove("listPushTail3", 1, "test2");
        System.out.println(test6);
    }

    @Test
    public void listPushHeadAndTrim() throws Exception {
        Long test1 = JedisClusterTemplate.me().listPushHeadAndTrim("listPushTail2", "test1-new1", 3);
        System.out.println(test1);
        Long test2 = RedissonTemplate.me().listPushHeadAndTrim("listPushTail3", "test1-new2", 4);
        System.out.println(test2);

    }

    @Test
    public void batchListPushTail() throws Exception {
        new Thread(() -> {
            try {
                JedisClusterTemplate.me().batchListPushTail("listPushTail3", new String[] {"test1", "test2", "test3"},
                    true);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
        new Thread(() -> {
            try {
                RedissonTemplate.me().batchListPushTail("listPushTail3", new String[] {"test7", "test8", "test9"},
                    true);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();

    }

    @Test
    public void updateListInTransaction() throws Exception {
        List<String> list1 = new ArrayList<>();
        list1.add("test1");
        list1.add("test2");
        list1.add("test3");
        JedisClusterTemplate.me().updateListInTransaction("listPushTail2", list1);
        List<String> list2 = new ArrayList<>();
        list2.add("test4");
        list2.add("test5");
        list2.add("test6");
        RedissonTemplate.me().updateListInTransaction("listPushTail3", list2);

    }

    @Test
    public void insertListIfNotExists() throws Exception {
        new Thread(() -> {
            try {
                JedisClusterTemplate.me().insertListIfNotExists("listPushTail5",
                    new String[] {"test1", "test2", "test3"});
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
        new Thread(() -> {
            try {
                RedissonTemplate.me().insertListIfNotExists("listPushTail6", new String[] {"test7", "test8", "test9"});
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();

    }

    @Test
    public void listGetAll() throws Exception {
        List<String> list1 = JedisClusterTemplate.me().listGetAll("listPushTail2");
        for (String s : list1) {
            System.out.println(s);
        }
        System.out.println(".....");
        List<String> list2 = RedissonTemplate.me().listGetAll("listPushTail2");
        for (String s : list2) {
            System.out.println(s);
        }
        System.out.println(".....");

    }

    @Test
    public void listRange() throws Exception {
        List<String> list1 = JedisClusterTemplate.me().listRange("listPushTail1", 0, 5);
        for (String s : list1) {
            System.out.println(s);
        }
        System.out.println(".....");
        List<String> list2 = RedissonTemplate.me().listRange("listPushTail1", 0, 4);
        for (String s : list2) {
            System.out.println(s);
        }
        System.out.println(".....");

    }

    @Test
    public void sortSetPush() throws Exception {
        Long test1 = JedisClusterTemplate.me().sortSetPush("sortSetPush1", 2, "test1");
        System.out.println(test1);
        Long test2 = RedissonTemplate.me().sortSetPush("sortSetPush1", 1, "test2");
        System.out.println(test2);
        JedisClusterTemplate.me().sortRemove("sortSetPush1", "test2");
        JedisClusterTemplate.me().sortRemove("sortSetPush1", "test1");

        for (int i = 1; i <= 10000; i++) {
            JedisClusterTemplate.me().sortSetPush("sortSetPush1", i, "test" + i);
        }
        for (int i = 10001; i <= 20000; i++) {
            RedissonTemplate.me().sortSetPush("sortSetPush1", i, "test" + i);
        }
    }

    @Test
    public void sorSetRangeByScore() throws Exception {
        Set<String> list1 = JedisClusterTemplate.me().sorSetRangeByScore("sortSetPush1", 1, 5);
        for (String s : list1) {
            System.out.println(s);
        }
        System.out.println(".....");
        Set<String> list2 = RedissonTemplate.me().sorSetRangeByScore("sortSetPush1", 1, 4);
        for (String s : list2) {
            System.out.println(s);
        }
        System.out.println(".....");

        Set<String> list3 = JedisClusterTemplate.me().sorSetRangeByScore("sortSetPush1", 100, 5000, 100, 10);
        for (String s : list3) {
            System.out.println(s);
        }
        System.out.println(".....");
        Set<String> list4 = RedissonTemplate.me().sorSetRangeByScore("sortSetPush1", 100, 5000, 100, 10);
        for (String s : list4) {
            System.out.println(s);
        }
        System.out.println(".....");
    }

    @Test
    public void addWithSortedSet() throws Exception {
        Long test1 = JedisClusterTemplate.me().addWithSortedSet("sortSetPush2", 2, "test1");
        System.out.println(test1);
        Long test2 = RedissonTemplate.me().addWithSortedSet("sortSetPush2", 1, "test2");
        System.out.println(test2);

    }

    @Test
    public void revrangeByScoreWithSortedSet() throws Exception {
        Set<String> list1 = JedisClusterTemplate.me().revrangeByScoreWithSortedSet("sortSetPush1", 5, 1);
        for (String s : list1) {
            System.out.println(s);
        }
        System.out.println(".....");
        Set<String> list2 = RedissonTemplate.me().revrangeByScoreWithSortedSet("sortSetPush1", 4, 1);
        for (String s : list2) {
            System.out.println(s);
        }
        System.out.println(".....");

    }

    @Test
    public void publish() throws Exception {
        Long test1 = JedisClusterTemplate.me().publish("dev_1111_JIM_CLUSTER", "test1");
        System.out.println(test1);
        Long test2 = RedissonTemplate.me().publish("dev_1111_JIM_CLUSTER", "test2");
        System.out.println(test2);

    }

    @Test
    public void get() throws Exception {
        Group group = JedisClusterTemplate.me().get("group:100:info", Group.class);
        System.out.println(group.getGroupId());
        Group group2 = RedissonTemplate.me().get("group:100:info", Group.class);
        System.out.println(group2.getGroupId());

        String user = JedisClusterTemplate.me().get("user:13888800001:terminal:tcp", String.class);
        System.out.println(user);
        String user2 = RedissonTemplate.me().get("user:13888800001:terminal:tcp", String.class);
        System.out.println(user2);

    }
}
