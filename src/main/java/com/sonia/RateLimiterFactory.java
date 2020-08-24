package com.sonia;

import com.sonia.ratelimiter.RateLimiter;
import com.sonia.ratelimiter.RateLimiterFixedWindow;
import org.glassfish.hk2.api.Factory;

public class RateLimiterFactory implements Factory<RateLimiter> {

    @Override
    public RateLimiter provide() {
        // Get settings from config file
        long budget = 2;
        long intervalSec = 10;
        int maxUserEntries = 100_000;
        // TODO: use the builder pattern for rate limiter
        return new RateLimiterFixedWindow(intervalSec, budget, maxUserEntries);
    }

    @Override
    public void dispose(RateLimiter rateLimiter) {
        //Noop
    }
}
