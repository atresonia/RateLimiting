package com.sonia;

import com.sonia.ratelimiter.RateLimiter;
import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.glassfish.jersey.server.ResourceConfig;

import javax.inject.Singleton;

public class AppConfig extends ResourceConfig {
    // class allows you to inject dependencies instead of hardcoding them
    // takes Rate Limiter class and creates as singleton (instantiate only once)
    // without requiring class to be a singleton itself
    // using an interface more useful for dependency injection
    // dependency injection library: hk2
    public AppConfig() {
        register(RateLimiter.class);

        register(new AbstractBinder() {
            @Override
            protected void configure() {
                bindFactory(RateLimiterFactory.class)
                        .to(RateLimiter.class)
                        .in(Singleton.class);
            }
        });
    }
}
