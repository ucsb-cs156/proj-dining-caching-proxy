# OVERALL-DESIGN.md

This is a completely blank repo at the moment.

What I want to build is a caching proxy for the app https://github.com/ucsb-cs156/proj-dining

That app is a review app for UCSB Dining Commons menu items.

The app uses the UCSB API via several hard coded endpoints, e.g.

in: src/main/java/edu/ucsb/cs156/dining/services/UCSBDiningMenuService.java

```java
public static final String ALL_MEAL_TIMES_AT_A_DINING_COMMON_ENDPOINT =
      "https://api.ucsb.edu/dining/menu/v1/{date-time}/{dining-common-code}";
```

in src/main/java/edu/ucsb/cs156/dining/services/UCSBDiningMenuItemsService.java

```java
 public static final String ALL_MEAL_ITEMS_AT_A_DINING_COMMON_ENDPOINT =
      "https://api.ucsb.edu/dining/menu/v1/{date-time}/{dining-common-code}/{meal-code}";
```

and there may be others.

What I want you to do is a two step process:

First, find all of the places in https://github.com/ucsb-cs156/proj-dining where the host `https://api.ucsb.edu` is
hardcoded, and replace that with a variable that can be configured in application.properties, and changed
via an environment variable UCSB_DINING_API_HOST.   The default value of that variable shoudl be
`https://api.ucsb.edu`.  However, after we implement our caching proxy, it will no longer be the 
only possibility.

The second part is to build a Spring Boot based web server that uses the same code conventions
and structure as https://github.com/ucsb-cs156/proj-dining, but that has, as it's function,
to serve as a caching proxy for all endpoints that proj-dining uses.

This app will have a front end admin interface that serves mainly as way for admins to 
see statistics about the performance of the caching proxy.  

Initially it will simply allow the admin to see four statistics only:
* How many requests have been made, total, since the proxy was started up
* How many requests were not found in the local cache and had to be served by querying the actual server at `https://api.ucsb.edu`.
* How many requests were found in the local cache.
* The cache hit rate as a percentage.

Initially, the TTL in the cache is "infinity".  Eventually, there will be more interesting cache replacement policies.

Since the content of each API request comes back in JSON, it is likely that a MongoDB database is the best
solution.  Please consult these apps for patterns for setting up MongoDB that are familiar to our team:

* https://github.com/ucsb-cs156/proj-courses
* https://github.com/ucsb-cs156/proj-citelines

Please review the design above for any flaws you may see, or questions you may have before starting.

## Design review: questions and answers

### Scope / access

**1. This repo (`proj-dining-caching-proxy`) is separate from `proj-dining`. Step 1 requires editing *proj-dining*'s source. Do you want me to work against a local clone of that repo (if so, where?), or is step 1 actually out of scope for this repo and just documented here as a prerequisite someone else handles?**

I have a directory `~/github/ucsb-cs156/proj-dining` where that project is cloned. You may work in that directory. I'll ensure that no other agents are working in that directory while you are.

**2. The doc says "there may be others" for hardcoded `api.ucsb.edu` usages — should I do an exhaustive grep of proj-dining for `api.ucsb.edu` (services, tests, configs, docs) rather than trust just the two cited files?**

Yes.

### Proxy contract

**3. Should the proxy be a *transparent drop-in replacement* — i.e. it exposes the identical path shape (`/dining/menu/v1/{date-time}/{dining-common-code}[/{meal-code}]`) so proj-dining just points `UCSB_DINING_API_HOST` at the proxy's base URL with zero other changes?**

Yes. The client should not know or care whether it is talking directly to the `https://api.ucsb.edu` server or the proxy.

**4. Does the real UCSB dining API require auth (e.g. a subscription key header) on requests? If so the proxy needs to forward/attach that itself when it goes upstream on a cache miss.**

Yes. It requires an API key. The API key will be sent in the header of each request. You can see that pattern by reviewing the source code for proj-dining. The idea is that the API key can just be passed through.

One consequence of this that you may be wondering about is: what if client A with key keyA asks for a record, and that record is already in the cache because client B with keyB asked for it previously? Won't that be a security issue?

The answer is: All of the data that is being accessed through these API endpoints is public, non-sensitive data. The API keys are ONLY used to track access usage, and not for authentication or authorization. So it isn't really a concern.

**5. Are all these endpoints GET-only, and are responses cached keyed on the full path (including date-time), or is there normalization needed (e.g. treating date-times within the same day the same)?**

Yes, all of these endpoints are GET only. For now, we are assuming that the content never changes. That assumption is correct most of the time, and in our initial MVP implementation, we are making that simplifying assumption.

In a later iteration, we may take into account that some of the endpoints may change their responses (rarely) up to a certain point in time, after which they will not change. But for now, treat each API endpoint and set of parameters as a query that always and forever returns the same value; and will fix the (very rare) issues that arise from that at a later date.

**6. Should error/non-2xx upstream responses be cached, or only successful responses?**

Only cache successful responses for now. We may want to come back and do some rate limiting if/when a client is misbehaving and sending many queries that all result in errors, but that will be a later update.

### Stats

**7. "Since the proxy was started up" — are the four stats in-memory counters that reset on restart (while cached documents persist in Mongo), or should they also be durable?**

Let's make them in-memory since startup for now. Data persistence can come later.

**8. Global stats only, or should the admin page eventually break stats down per-endpoint?**

Just global for now. I'm building an MVP that students will build new features on top of. So I want the MVP to be fully functional, but have lots and lots of room for additional functionality to be conceived and built later.

### Admin auth / frontend

**9. proj-dining's admin UI presumably reuses UCSB's OAuth/Google login with role-based access (ADMIN role) — should this proxy's admin frontend replicate that same auth flow/dependency set, or is a simpler auth model acceptable since this is an internal ops tool?**

Mostly. Actually, I would suggest instead looking at https://github.com/ucsb-cs156/proj-citelines and https://github.com/ucsb-cs156/proj-scaffold for a better model of setting up Admin and Authorization access, with an "intermediate" layer having Instructor/Researcher. For our "intermediate" layer, call it "HostManager". We might, later on, allow Admins to set up certain users with access to more detailed statistics about requests from specific hosts, and that is what "HostManager" would be for.

For the time being though, the distinction between plain old logged in User, HostManager, and Admin is not very much: Admins can see the Users list and can update who is/is not an Admin or HostManager, and can see the Admin/Developer Info menu (please copy that from proj-dining also). But all three user levels will be able to see the same four statistics on the main page once they are logged in.

The distinction in access levels is mainly for later features, but it's useful to build it in right from the start.

### MongoDB / conventions

**10. For proj-courses/proj-citelines, is there a canonical doc/entity naming convention (e.g. `*Entity`, `*Repository` with Spring Data MongoDB) you want mirrored exactly, or just "similar spirit"?**

The naming conventions are mostly consistent across the Spring Boot projects mentioned below, as well as GitHub Actions, unit tests, integration tests, a React frontend that uses a particular way of doing proxying on localhost vs when deployed on Dokku. Variations tend to be minor. Please consult these projects for examples, and especially in places that they are consistent with one another, follow the established conventions:

* https://github.com/ucsb-cs156/proj-citelines
* https://github.com/ucsb-cs156/proj-courses
* https://github.com/ucsb-cs156/proj-dining
* https://github.com/ucsb-cs156/proj-frontiers
* https://github.com/ucsb-cs156/proj-happycows
* https://github.com/ucsb-cs156/proj-scaffold
