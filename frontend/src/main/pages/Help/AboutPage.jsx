import BasicLayout from "main/layouts/BasicLayout/BasicLayout";

export default function AboutPage() {
  return (
    <BasicLayout>
      <h1>About the Dining Caching Proxy</h1>

      <p>
        This app is a caching proxy for the UCSB Dining API calls made by{" "}
        <a href="https://github.com/ucsb-cs156/proj-dining">proj-dining</a>.
        Since dining menu data rarely changes, this proxy caches responses so
        that repeated requests for the same menu don&apos;t need to hit the real
        UCSB API every time.
      </p>

      <p>
        The admin interface shows statistics about the performance of the cache:
        how many requests have been made, how many were served from the cache,
        and how many had to be fetched from the UCSB API.
      </p>

      <p>
        It is open source, and the source code is available here:{" "}
        <a href="https://github.com/ucsb-cs156/proj-dining-caching-proxy">
          https://github.com/ucsb-cs156/proj-dining-caching-proxy
        </a>
      </p>
    </BasicLayout>
  );
}
