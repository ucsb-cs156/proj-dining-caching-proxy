# Configuring for MongoDB

This app uses MongoDB in addition to PostgreSQL: PostgreSQL holds relational
data (`User`, `Admin`, `HostManager`), while MongoDB will hold the cached UCSB
Dining API responses (added in a follow-up PR — see
`docs/design/OVERALL-DESIGN.md`).

On localhost, and for unit/integration/end-to-end tests, you do not need to do
any special configuration for MongoDB; the app uses an embedded, in-memory
instance of MongoDB, similar to how H2 is used as an in-memory instance of a
SQL database.

## Configuring MongoDB on Dokku

On dokku, you set up the MongoDB database in a similar way to how the Postgres
database is set up, with these commands.

Note that `appname` should be replaced with the name of your app, e.g.
`diningcachingproxy`, `diningcachingproxy-qa`, `diningcachingproxy-dev-cgaucho`, etc.

Append `-m-db` to distinguish this from the app itself.

```
dokku mongo:create appname-m-db
dokku mongo:link appname-m-db appname --no-restart
```

For example, for a `diningcachingproxy-qa` app, you'd use:

```
dokku mongo:create diningcachingproxy-qa-m-db
dokku mongo:link diningcachingproxy-qa-m-db diningcachingproxy-qa --no-restart
```

The `dokku mongo:link` command sets the `MONGO_URL` config var on the app,
which is read by `spring.data.mongodb.uri` in
`application-production.properties`.

## Accessing the Mongo Command on Dokku

If you want to list records in the mongo collections, you can access a mongo
command line on dokku with the following command (substitute the name of your
mongo db database in place of `diningcachingproxy-m-db`):

```
dokku mongo:connect diningcachingproxy-m-db
```

That gives you a `mongosh` prompt where `show collections` lists the
collections in the database.
