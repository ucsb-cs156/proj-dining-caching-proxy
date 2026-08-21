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


# Question: Should we pivot in name and scope?

When you are done with that, before we pivot to the next step in building the dining proxy:

Here's a question.  We also have another app that accesses various https://api.ucsb.edu endpoints.

Maybe we should rename the repo and the app to 

proj-ucsb-api-caching-proxy

And expand the scope to include proj-courses as well.

Both proj-dining and proj-courses also do some of their own internal caching, but the point of this caching proxy is that when you have an entire classroom where sometimes 48 different students are doing development implementations and all hitting up these api endpoints, it can get a bit noisy for the server.

So the caching inside each of these apps is helpful, but for the sum total across all of them, we still want some caching at the network level.

What do you think?

Can you look over https://github.com/ucsb-cs156/proj-courses and assess whether, perhaps, we might just treat the two apps as equivalent for purposes of the proxy?

We might want to do a similar PR on proj-courses where we factor out an environment variable and application.properties value for the API prefix.

Let me know your thoughts.

## Response from Claude

Short version: mechanically compatible, but not semantically equivalent — I'd hold off on both the rename and the scope expansion until the proxy's design grows a TTL concept, or scope any early proj-courses integration to only the endpoints that share dining's "never changes once written" assumption.

### What I found in proj-courses

proj-courses calls several `api.ucsb.edu` paths (`/academics/curriculums/v1/classes/search`, `/academics/curriculums/v3/classes/{quarter}/{enrollcode}`, `/academics/curriculums/v3/finals`, `/academics/quartercalendar/v1/quarters/current`, `/academics/quartercalendar/v1/quarters`, `/students/lookups/v1/subjects`, `/students/lookups/v1/requirements`), all from `UCSBCurriculumService`/`UCSBAPIQuarterService`/`UCSBSubjectsService`. Mechanically, it looks a lot like proj-dining: every call is a GET, and every call sends the same `ucsb-api-key`/`ucsb-api-version` header pair proj-dining uses. So the transport layer, the admin UI, and the `CachedResponse` schema this proxy is built around would all reuse cleanly — that part of "treat them as equivalent" holds up.

Where it breaks down is the caching *semantics*, and this proxy's current design (cache-by-URL-and-headers, no expiry, one row per unique request forever) is built entirely around an assumption that's true for dining but not for courses: "today's menu, once posted, never changes." Two things in proj-courses violate that:

- `quarters/current` has **no distinguishing query parameters at all** — it's a "what quarter is it right now" call whose answer changes at every quarter rollover. Cache it forever and it's wrong forever, the very next quarter.
- Course-search results (basicsearch/sectionsearch/primaries, for the current or an upcoming quarter) reflect **live enrollment, seat count, and waitlist state that changes continuously during registration**. Caching those forever wouldn't just be stale — it'd hand every student after the first request bad seat-availability data, which is a correctness bug, not just a missed optimization.

That's not a hypothetical concern on proj-courses' end, either — they've already had to solve it. proj-courses only caches `getCurrentQuarter()` in memory for 24 hours, and everything else the frontend actually hits (`/api/public/primaries`, `/basicsearch`, `/sectionsearch`, etc.) goes straight to the UCSB API live. For their own background-job-populated data they built a separate, DB-backed `IsStaleService` with a configurable staleness threshold — i.e., they already needed real TTL semantics and built their own rather than a cache-forever model. They've also got their own IP-based rate limiter (`RateLimitFilter`), which suggests the "48 students hammering the API at once" problem you're solving for here is one they've already felt too.

On the host-config PR: proj-courses doesn't have the `UCSB_DINING_API_HOST`-style env var yet in the upstream repo — I found it as an uncommitted local change on this machine, which I'm reading as exactly the PR you're about to prepare yourself (matching #135's pattern). That's a genuine prerequisite for any shared proxy regardless of what we decide below, so it's worth doing either way.

### Recommendation

Don't rename or expand scope yet. Two options, in order of preference:

1. **Ship the dining-only proxy as planned, revisit proj-courses later.** Once the MVP is out and (if it's ever needed) the design grows a per-cache-entry TTL/expiry concept — even a simple "cache for N minutes" default per route — proj-courses becomes a much more natural fit, and at that point the rename to `proj-ucsb-api-caching-proxy` would actually describe what the tool does rather than get ahead of it.
2. **If you want to fold proj-courses in now anyway,** don't treat all its endpoints as equivalent to dining's — explicitly scope it to the genuinely immutable ones (past-quarter classes, finals info for a completed quarter) and exclude `quarters/current` and any current/future-quarter search from caching entirely. That's more integration work than "just point it at the same proxy," and it means the proxy needs an explicit non-cacheable/short-TTL escape hatch from day one instead of later — which is really the same conclusion as option 1, just pulled forward.

### Addendum: it's not just cache/don't-cache, it's cache-for-how-long

Following up on the recommendation above, in conversation the point was sharpened further: the proxy doesn't just need to know *whether* an endpoint is cacheable, it needs to know *for how long*. `quarters/current` is the clearest example — caching it for 24 hours is safe (worst case, a student sees yesterday's quarter for a few hours around a rollover, which is merely stale and self-corrects on the next refresh), but caching it for days or weeks is not merely stale, it's actively wrong: every downstream call that trusts "current quarter" to pick a quarter would silently operate on the wrong one long after the real rollover happened.

So the design has (at least) three tiers per endpoint, not two:

- **Cache forever** — dining menus once posted; nothing ever un-happens.
- **Cache with a bounded TTL** — `quarters/current`-style data that changes rarely but must expire on a schedule tied to how often it actually changes.
- **Never cache** — live seat/waitlist/enrollment search during registration, where any caching window produces wrong answers.

[Issue #2](https://github.com/ucsb-cs156/proj-dining-caching-proxy/issues/2) is the first concrete step toward this: before TTLs can be assigned per endpoint, the proxy first needs to know *what* endpoints it has actually seen (independent of query parameters) and how often each has been hit. That's deliberately scoped as observation-only for now — a controller endpoint listing seen API paths with hit counts — with the TTL configuration (global default, per-host override, per-endpoint override, `0` meaning never-cache) layered on top once that inventory exists. That sequencing lines up with the recommendation above: build the visibility and TTL-policy layer against dining's own endpoints first, then proj-courses becomes an integration question rather than a redesign.

Either way, I'd treat the rename as a decision to make *after* scope is settled, not before — `proj-ucsb-api-caching-proxy` as a name commits to "this proxy is generic across UCSB apps," and right now that's only true for the transport layer, not for the caching logic that's the actual point of the project.