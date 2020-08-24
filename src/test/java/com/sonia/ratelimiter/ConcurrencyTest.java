package com.sonia.ratelimiter;

import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class ConcurrencyTest {

    @Test
    public void givenConcurrentMap_whenSumParallel_thenCorrect()
            throws Exception {
        // thread safe: uses CAS instruction
        Map<String, Integer> map = new ConcurrentHashMap<>();
        List<Integer> sumList = parallelSum100(map, 1000);
        System.out.println(sumList.toString());
        System.out.println(map.toString());
        Assert.assertEquals(1, sumList
                .stream()
                .distinct()
                .count());
        long wrongResultCount = sumList
                .stream()
                .filter(num -> num != 100)
                .count();

        Assert.assertEquals(0, wrongResultCount);
    }

    @Test
    public void givenHashMap_whenSumParallel_thenError() throws Exception {
        // not thread safe: multiple threads access and increment value at same time without
        // storing one after the other (>= 100)
        Map<String, Integer> map = new HashMap<>();
        List<Integer> sumList = parallelSum100(map, 100);
        System.out.println(sumList.toString());
        System.out.println(map.toString());
        Assert.assertNotEquals(1, sumList
                .stream()
                .distinct()
                .count());
        long wrongResultCount = sumList
                .stream()
                .filter(num -> num != 100)
                .count();

        Assert.assertTrue(wrongResultCount > 0);
    }

    private List<Integer> parallelSum100(Map<String, Integer> map,
                                         int executionTimes) throws InterruptedException {
        List<Integer> sumList = new ArrayList<>(1000);
         for (int i = 0; i < executionTimes; i++) {
            map.put("test", 0);
            ExecutorService executorService =
                    Executors.newFixedThreadPool(4);
            for (int j = 0; j < 10; j++) {
                executorService.execute(() -> {
                    for (int k = 0; k < 10; k++)
                        map.computeIfPresent(
                                "test",
                                (key, value) -> value + 1
                        );
                });
            }
            executorService.shutdown();
            executorService.awaitTermination(5, TimeUnit.SECONDS);
            sumList.add(map.get("test"));
        }
        return sumList;
    }
}
