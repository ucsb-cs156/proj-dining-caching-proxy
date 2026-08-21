
# Deploying on Dokku

To deploy on dokku, see the instructions here:

* <https://ucsb-cs156.github.io/topics/dokku/deploying_an_app.html>

You will also need the environment variables documented in
`docs/oauth.md` (`GOOGLE_CLIENT_ID`, `GOOGLE_CLIENT_SECRET`, `ADMIN_EMAILS`)
and `docs/mongodb.md` (MongoDB setup).

You will also need the command:

* <tt>dokku git:set <i>appname</i> keep-git-dir true</tt>

# Short Version

This short version omits many details, but if you are already familiar with the process of deploying applications, you may be able to use this.

Note that you may need to modify:
* `diningcachingproxy` to `diningcachingproxy-qa` or `diningcachingproxy-dev-cgaucho` (where `cgaucho` is your github id)
* `https://github.com/ucsb-cs156/proj-dining-caching-proxy` to your team's repo url, if different
* `main` to `my-branch-name` for your feature branch
* `yourEmail@ucsb.edu` to your own email
* values for `GOOGLE_CLIENT_ID`, `GOOGLE_CLIENT_SECRET` from [docs/oauth.md](oauth.md)

### setup `diningcachingproxy`

```
dokku apps:create diningcachingproxy
dokku git:set diningcachingproxy keep-git-dir true

dokku postgres:create diningcachingproxy-db
dokku postgres:link diningcachingproxy-db diningcachingproxy --no-restart

dokku mongo:create diningcachingproxy-m-db
dokku mongo:link diningcachingproxy-m-db diningcachingproxy --no-restart

dokku config:set --no-restart diningcachingproxy PRODUCTION=true
dokku config:set --no-restart diningcachingproxy SOURCE_REPO=https://github.com/ucsb-cs156/proj-dining-caching-proxy
dokku config:set --no-restart diningcachingproxy GOOGLE_CLIENT_ID=get-value-from-google-developer-console
dokku config:set --no-restart diningcachingproxy GOOGLE_CLIENT_SECRET=get-value-from-google-developer-console
dokku config:set --no-restart diningcachingproxy ADMIN_EMAILS=list-of-admin-emails

dokku git:sync diningcachingproxy https://github.com/ucsb-cs156/proj-dining-caching-proxy main
dokku ps:rebuild diningcachingproxy

dokku letsencrypt:set diningcachingproxy email yourEmail@ucsb.edu
dokku letsencrypt:enable diningcachingproxy
```

### setup `diningcachingproxy-qa`

```
dokku apps:create diningcachingproxy-qa
dokku git:set diningcachingproxy-qa keep-git-dir true

dokku postgres:create diningcachingproxy-qa-db
dokku postgres:link diningcachingproxy-qa-db diningcachingproxy-qa --no-restart

dokku mongo:create diningcachingproxy-qa-m-db
dokku mongo:link diningcachingproxy-qa-m-db diningcachingproxy-qa --no-restart

dokku config:set --no-restart diningcachingproxy-qa PRODUCTION=true
dokku config:set --no-restart diningcachingproxy-qa SOURCE_REPO=https://github.com/ucsb-cs156/proj-dining-caching-proxy
dokku config:set --no-restart diningcachingproxy-qa GOOGLE_CLIENT_ID=get-value-from-google-developer-console
dokku config:set --no-restart diningcachingproxy-qa GOOGLE_CLIENT_SECRET=get-value-from-google-developer-console
dokku config:set --no-restart diningcachingproxy-qa ADMIN_EMAILS=list-of-admin-emails

dokku git:sync diningcachingproxy-qa https://github.com/ucsb-cs156/proj-dining-caching-proxy main
dokku ps:rebuild diningcachingproxy-qa

dokku letsencrypt:set diningcachingproxy-qa email yourEmail@ucsb.edu
dokku letsencrypt:enable diningcachingproxy-qa
```
