package com.sonia.ratelimiter;

import org.junit.Assert;
import org.junit.Test;

import java.util.concurrent.TimeUnit;

public class RateLimiterTest {

    @Test
    public void testRateLimitData() throws InterruptedException {
        // TODO: add multi-threaded test
        RateLimitData rateLimitData = new RateLimitData();
        Assert.assertEquals("Initially, the total user count should be zero, but is not",
                0, rateLimitData.getTotalUserCount());

        int numGuids = 3;
        String[] guids = new String[numGuids];

        for (int i = 0; i < numGuids; i++) {
            guids[i] = String.valueOf(1001 + i);
        }
        Thread thread1 = new Thread();
        Thread thread2 = new Thread();
        thread1.start();
        thread2.start();
        long updateTimeBeforeReset = rateLimitData.getLastUpdatedTime().longValue();
        // System.out.println("Time before reset=" + updateTimeBeforeReset);

        for (int i = 0; i < 5; i++) {
            int j = 0;
            for (String guid: guids) {
                rateLimitData.increment(guid);
                Assert.assertEquals("User count for guid " + guid + "should be 1",
                        1, rateLimitData.getUserCount(guid));
                Assert.assertEquals("Total user count is not updating properly",
                        ++j, rateLimitData.getTotalUserCount());
            }

            rateLimitData.resetData();

            // precision not that accurate for milliseconds, wait a little to be sure
            TimeUnit.MILLISECONDS.sleep(100);

            long updateTimeAfterReset = rateLimitData.getLastUpdatedTime().longValue();
            // System.out.println("Time after reset=" + updateTimeAfterReset);

            Assert.assertEquals("After resetting rate limit data, total user count should be zero, but isn't",
                    0, rateLimitData.getTotalUserCount());

            Assert.assertTrue("Last Updated Time should change after resetting the rate limit data," +
                    "but they are the same", updateTimeBeforeReset < updateTimeAfterReset);

        }
    }

    @Test
    public void testRateLimiter() throws InterruptedException {
        int numGuids = 100;
        String[] guids = new String[numGuids];
        for (int i = 0; i < numGuids; i++) {
            guids[i] = String.valueOf(1001 + i);
        }

        long intervalSec = 2;
        long budget = 2;
        int maxGuidCount = 150;
        RateLimiterFixedWindow rateLimiter = new RateLimiterFixedWindow(intervalSec, budget, maxGuidCount);

        for (int i = 0; i < 5; i++) {
            for (String guid: guids) {
                boolean isAllowed = rateLimiter.isAllowed(guid);
                if (i < 2) {
                    Assert.assertTrue("guid=" + guid +", i=" + i + ".  isAllowed should be true, but is false", isAllowed);
                } else {
                    Assert.assertFalse("guid=" + guid +", i=" + i + ". isAllowed should be false, but is true", isAllowed);
                }
                rateLimiter.increment(guid);
            }
        }

        // Wait past the rate limit interval, and then verify that the rate limit is lifted
        TimeUnit.MILLISECONDS.sleep(intervalSec * 1000 + 100);

        for (int i = 0; i < 2; i++) {
            for (String guid: guids) {
                boolean isAllowed = rateLimiter.isAllowed(guid);
                Assert.assertTrue("guid=" + guid +", i=" + i + ".  isAllowed should be true, but is false", isAllowed);
                rateLimiter.increment(guid);
            }
        }

    }

    @Test
    public void testRateLimiterMaxUsers() throws InterruptedException {
        int numGuids = 200;
        String[] guids = new String[numGuids];
        for (int i = 0; i < numGuids; i++) {
            guids[i] = String.valueOf(1001 + i);
        }

        int refCount = 0;
        long intervalSec = 10;
        long budget = 2;
        int maxGuidCount = 150;
        RateLimiterFixedWindow rateLimiter = new RateLimiterFixedWindow(intervalSec, budget, maxGuidCount);
        for (int i = 0; i < 5; i++) {
            for (String guid: guids) {

                Assert.assertTrue("Max user count is not being set properly", rateLimiter.getTotalUserCount() <= maxGuidCount);

                if (rateLimiter.getTotalUserCount() >= rateLimiter.getMaxGuidCount()) {
                    refCount = 0;
                }
                boolean isAllowed = rateLimiter.isAllowed(guid);

                if (refCount < 2) {
                    Assert.assertTrue("guid=" + guid + ", i=" + i + ".  isAllowed should be true, but is false", isAllowed);
                } else {
                    Assert.assertFalse("guid=" + guid + ", i=" + i + ". isAllowed should be false, but is true", isAllowed);
                }
                rateLimiter.increment(guid);
            }
            // System.out.println("refCount=" + refCount);
            refCount++;
        }
        // TODO: make sure rate limiter is enforcing max size

    }
}
