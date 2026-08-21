import { useBackend } from "main/utils/useBackend";
import BasicLayout from "main/layouts/BasicLayout/BasicLayout";

type Stats = {
  totalRequests: number;
  cacheHits: number;
  cacheMisses: number;
  hitRatePercentage: number;
};

const defaultStats: Stats = {
  totalRequests: 0,
  cacheHits: 0,
  cacheMisses: 0,
  hitRatePercentage: 0,
};

export default function HomePage(): React.JSX.Element {
  const { data } = useBackend<Stats>(
    ["/api/stats"],
    { method: "GET", url: "/api/stats" },
    // Stryker disable next-line all : don't test default value of empty stats
    defaultStats,
  );
  const stats = data ?? defaultStats;

  return (
    <BasicLayout>
      <div className="pt-2">
        <h1>Dining Caching Proxy</h1>
        <table className="table" data-testid="HomePage-stats-table">
          <tbody>
            <tr>
              <th scope="row">Total Requests</th>
              <td data-testid="HomePage-totalRequests">
                {stats.totalRequests}
              </td>
            </tr>
            <tr>
              <th scope="row">Cache Hits</th>
              <td data-testid="HomePage-cacheHits">{stats.cacheHits}</td>
            </tr>
            <tr>
              <th scope="row">Cache Misses</th>
              <td data-testid="HomePage-cacheMisses">{stats.cacheMisses}</td>
            </tr>
            <tr>
              <th scope="row">Hit Rate</th>
              <td data-testid="HomePage-hitRate">
                {stats.hitRatePercentage.toFixed(1)}%
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </BasicLayout>
  );
}
