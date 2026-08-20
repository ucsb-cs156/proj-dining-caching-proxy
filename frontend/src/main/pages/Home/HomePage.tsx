import BasicLayout from "main/layouts/BasicLayout/BasicLayout";

// Placeholder landing page for logged-in users. A future update will replace
// this with the four-stat cache dashboard (total requests, cache hits, cache
// misses, hit rate) described in docs/design/OVERALL-DESIGN.md.
export default function HomePage(): React.JSX.Element {
  return (
    <BasicLayout>
      <div className="pt-2">
        <h1>Dining Caching Proxy</h1>
        <p>Cache statistics coming soon.</p>
      </div>
    </BasicLayout>
  );
}
