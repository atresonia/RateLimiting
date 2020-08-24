package com.sonia.ratelimiter;

public interface RateLimiter {
    void increment(String guid);
    boolean isAllowed(String guid);
    int getTotalUserCount();
    int getMaxGuidCount();
}
