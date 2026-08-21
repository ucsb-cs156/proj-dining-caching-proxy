import { render, screen, waitFor } from "@testing-library/react";
import HomePage from "main/pages/Home/HomePage";
import { QueryClient, QueryClientProvider } from "@tanstack/react-query";
import { MemoryRouter } from "react-router";

import { apiCurrentUserFixtures } from "fixtures/currentUserFixtures";
import { systemInfoFixtures } from "fixtures/systemInfoFixtures";
import axios from "axios";
import AxiosMockAdapter from "axios-mock-adapter";
import * as useBackendModule from "main/utils/useBackend";
import { describe, test, expect, afterEach, vi } from "vitest";

const useBackendSpy = vi.spyOn(useBackendModule, "useBackend");

describe("HomePage tests", () => {
  const axiosMock = new AxiosMockAdapter(axios);
  const statsEndpoint = "/api/stats";

  const setupUser = () => {
    axiosMock.reset();
    axiosMock.resetHistory();
    axiosMock
      .onGet("/api/currentUser")
      .reply(200, apiCurrentUserFixtures.userOnly);
    axiosMock
      .onGet("/api/systemInfo")
      .reply(200, systemInfoFixtures.showingNeither);
  };

  const queryClient = new QueryClient();

  afterEach(() => {
    useBackendSpy.mockClear();
  });

  test("renders the four stats from the backend", async () => {
    setupUser();
    axiosMock.onGet(statsEndpoint).reply(200, {
      totalRequests: 10,
      cacheHits: 7,
      cacheMisses: 3,
      hitRatePercentage: 70,
    });

    render(
      <QueryClientProvider client={queryClient}>
        <MemoryRouter>
          <HomePage />
        </MemoryRouter>
      </QueryClientProvider>,
    );

    await waitFor(() => {
      expect(screen.getByTestId("HomePage-totalRequests")).toHaveTextContent(
        "10",
      );
    });
    expect(screen.getByTestId("HomePage-cacheHits")).toHaveTextContent("7");
    expect(screen.getByTestId("HomePage-cacheMisses")).toHaveTextContent("3");
    expect(screen.getByTestId("HomePage-hitRate")).toHaveTextContent("70.0%");
  });

  test("renders zeroes before the backend responds", async () => {
    setupUser();
    axiosMock.onGet(statsEndpoint).reply(200, {
      totalRequests: 0,
      cacheHits: 0,
      cacheMisses: 0,
      hitRatePercentage: 0,
    });

    render(
      <QueryClientProvider client={queryClient}>
        <MemoryRouter>
          <HomePage />
        </MemoryRouter>
      </QueryClientProvider>,
    );

    expect(screen.getByTestId("HomePage-totalRequests")).toHaveTextContent("0");
    expect(screen.getByTestId("HomePage-hitRate")).toHaveTextContent("0.0%");
  });

  test("useBackend is called with the correct cache query key", async () => {
    setupUser();
    axiosMock.onGet(statsEndpoint).reply(200, {
      totalRequests: 0,
      cacheHits: 0,
      cacheMisses: 0,
      hitRatePercentage: 0,
    });

    render(
      <QueryClientProvider client={queryClient}>
        <MemoryRouter>
          <HomePage />
        </MemoryRouter>
      </QueryClientProvider>,
    );

    expect(useBackendSpy).toHaveBeenCalledWith(
      [statsEndpoint],
      { method: "GET", url: statsEndpoint },
      { totalRequests: 0, cacheHits: 0, cacheMisses: 0, hitRatePercentage: 0 },
    );
  });
});
