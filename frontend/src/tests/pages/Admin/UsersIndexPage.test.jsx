import { render, waitFor, screen } from "@testing-library/react";
import { QueryClient, QueryClientProvider } from "@tanstack/react-query";
import { MemoryRouter } from "react-router";
import UsersIndexPage from "main/pages/Admin/UsersIndexPage";
import usersFixtures from "fixtures/usersFixtures";
import { systemInfoFixtures } from "fixtures/systemInfoFixtures";
import mockConsole from "tests/testutils/mockConsole";

import axios from "axios";
import AxiosMockAdapter from "axios-mock-adapter";

describe("UsersIndexPage tests", () => {
  const axiosMock = new AxiosMockAdapter(axios);

  const testId = "UsersTable";

  beforeEach(() => {
    axiosMock.reset();
    axiosMock.resetHistory();
    axiosMock
      .onGet("/api/systemInfo")
      .reply(200, systemInfoFixtures.showingNeither);
  });

  test("renders without crashing on three users", async () => {
    const queryClient = new QueryClient();
    axiosMock.onGet("/api/admin/users").reply(200, usersFixtures.threeUsers);

    render(
      <QueryClientProvider client={queryClient}>
        <MemoryRouter>
          <UsersIndexPage />
        </MemoryRouter>
      </QueryClientProvider>,
    );
    await screen.findByText("Users");
  });

  test("renders empty table when backend unavailable", async () => {
    const queryClient = new QueryClient();
    axiosMock.onGet("/api/admin/users").timeout();

    const restoreConsole = mockConsole();

    render(
      <QueryClientProvider client={queryClient}>
        <MemoryRouter>
          <UsersIndexPage />
        </MemoryRouter>
      </QueryClientProvider>,
    );

    await waitFor(() => {
      expect(axiosMock.history.get.length).toBeGreaterThanOrEqual(1);
    });

    const errorMessage = console.error.mock.calls[0][0];
    expect(errorMessage).toMatch(
      "Error communicating with backend via GET on /api/admin/users",
    );
    restoreConsole();

    expect(
      screen.queryByTestId(`${testId}-cell-row-0-col-id`),
    ).not.toBeInTheDocument();
  });

  test("renders users table with data and toggle buttons", async () => {
    const queryClient = new QueryClient();
    axiosMock.onGet("/api/admin/users").reply(200, usersFixtures.threeUsers);

    render(
      <QueryClientProvider client={queryClient}>
        <MemoryRouter>
          <UsersIndexPage />
        </MemoryRouter>
      </QueryClientProvider>,
    );

    await screen.findByText("Users");

    await waitFor(() => {
      expect(
        screen.getByTestId(`${testId}-cell-row-0-col-id`),
      ).toHaveTextContent("1");
      expect(
        screen.getByTestId(`${testId}-cell-row-0-col-toggle-admin-button`),
      ).toHaveTextContent("Toggle Admin");
      expect(
        screen.getByTestId(
          `${testId}-cell-row-0-col-toggle-hostManager-button`,
        ),
      ).toHaveTextContent("Toggle Host Manager");
    });
  });
});
