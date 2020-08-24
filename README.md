# RateLimiting
**Rate Limiting Fixed Window Algorithm**

A Jersey REST API program that designs a rate limiter will limit the number of requests sent by a specific user within a particular time window for a web service. 

Depending on the number of requests made by a user in a specific time frame, the rate limiter returns a boolean on whether or not the user should be rate limited.

**Inputs**

`budget`: the maximum number of requests allowed by a user

`intervalSec`: the specific time frame in seconds that the user is allowed to make a `budget` number of requests, if time interval is passed, we will reset the rate limiter data

`maxUserEntries`: the maximum users that will be tracked. Beyond this number, we will reset the rate limiter data



