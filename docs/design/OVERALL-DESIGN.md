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
