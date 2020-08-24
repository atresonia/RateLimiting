package com.sonia.ratelimiter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RateLimiterFixedWindow implements RateLimiter {
    private RateLimitData rateLimitData = new RateLimitData();

    private long intervalSec;
    private long budget;
    private int maxGuidCount;

    private final Logger logger = LoggerFactory.getLogger(RateLimiterFixedWindow.class);

    public RateLimiterFixedWindow(long intervalSec, long budget, int maxGuidCount) {
        this.intervalSec = intervalSec;
        this.budget = budget;
        // max number of guids we'll track. Beyond this number, we'll reset the rate limiter data
        this.maxGuidCount = maxGuidCount;
    }

    /**
     * increment rate limit counter for the given user
     * @param guid the user's guid
     */
    public void increment(String guid) {
        rateLimitData.increment(guid);
    }

    /**
     *
     * @param guid the users's guid
     * @return true if request allowed, false if requested should be rate-limited
     */
    public boolean isAllowed(String guid) {
        // If the interval is reached or past or if we've reached the limit on the number of users
        // we're tracking, clear the data
        long currentTime = System.currentTimeMillis();
        long lastUpdatedTime = rateLimitData.getLastUpdatedTime().longValue();

        // went past interval
        if (currentTime >= lastUpdatedTime  + (intervalSec * 1000) ||
            rateLimitData.getTotalUserCount() >= maxGuidCount) {

            if (rateLimitData.getTotalUserCount() >= maxGuidCount) {
                logger.debug("Clearing rate limit data as max user count reached ({})", maxGuidCount);
            }
            // Clear
            rateLimitData.resetData();
        }

        // check if we should rate limit
        long currentCount = rateLimitData.getUserCount(guid);
        // check for currentCount + 1 to check if current request fits in budget
        boolean allowed = currentCount + 1 <= budget;
        if (!allowed) {
            logger.debug("User {} is rate limited", guid);
        }

        return allowed;
    }

    public int getTotalUserCount() {
        return rateLimitData.getTotalUserCount();
    }

    public int getMaxGuidCount() {
        return maxGuidCount;
    }

}
