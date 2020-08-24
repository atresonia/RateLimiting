package com.sonia.ratelimiter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

class RateLimitData {

    //guid->count
    private final ConcurrentHashMap<String, AtomicLong> userCountMap = new ConcurrentHashMap<>();
    // TODO: why do we need this
    private final AtomicReference<ConcurrentHashMap<String, AtomicLong>> userCount =
            new AtomicReference<>(userCountMap);

    //Current counting window start time in milliseconds
    private final AtomicLong lastUpdatedTime = new AtomicLong(System.currentTimeMillis());

    //Lock used for multiple threads to lock other threads out when reseting one of the counts
    private final Lock lock = new ReentrantLock();

    // TODO: why do we need this?
    //Empty map to be swapped for current user rate limit map (userCountMap)
    private final ConcurrentHashMap<String, AtomicLong> userCountMapCopy = new ConcurrentHashMap<>();

    private static final Logger logger = LoggerFactory.getLogger(RateLimitData.class);

    /**
     * increment rate limit counter for the given user
     * @param guid the user's guid
     * @return the new count
     */
    public long increment(String guid) {
        // TODO: check if this function is thread-safe
        AtomicLong count = userCount.get().get(guid);
        if (count == null) {
            AtomicLong newCount = new AtomicLong(0L);
            count = userCount.get().putIfAbsent(guid, newCount);
            if (count == null) { // TODO: why this check?
                count = newCount;
            }
        }
        return count.incrementAndGet();
    }

    /**
     * Reset the rate limit data:
     * 1. Set this.lastUpdatedTime to current time
     * 2. Clear this.userCount
     */
    public void resetData() {
        if (lock.tryLock()) {
            try {
                lastUpdatedTime.set(System.currentTimeMillis());

                if (userCount.get() == userCountMap) {
                    userCount.set(userCountMapCopy);
                    userCountMap.clear();
                } else {
                    userCount.set(userCountMap);
                    userCountMapCopy.clear();
                }
            } finally {
                lock.unlock();
            }
        }
    }

    /**
     * return the rate limit count for the specified user
     * @param guid the user's guid
     * @return the rate limit count
     */
    public long getUserCount(String guid) {
        logger.debug("Number of users in map={}", userCount.get().size());
        long count = 0;
        if (userCount.get() != null) {
            AtomicLong refCount = userCount.get().get(guid);
            if (refCount != null) {
                return refCount.longValue();
            }
        }
        return count;
    }

    public AtomicLong getLastUpdatedTime() {
        return lastUpdatedTime;
    }

    int getTotalUserCount() {
        return userCount.get().size();
    }


}
