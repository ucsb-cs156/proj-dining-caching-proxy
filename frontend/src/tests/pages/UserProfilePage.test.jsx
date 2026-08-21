import { render, screen } from "@testing-library/react";
import { QueryClient, QueryClientProvider } from "@tanstack/react-query";
import { MemoryRouter } from "react-router";
import axios from "axios";
import AxiosMockAdapter from "axios-mock-adapter";

import UserProfilePage from "main/pages/UserProfilePage";
import { systemInfoFixtures } from "fixtures/systemInfoFixtures";

const axiosMock = new AxiosMockAdapter(axios);

function renderPage() {
  const queryClient = new QueryClient({
    defaultOptions: { queries: { retry: false } },
  });
  return render(
    <QueryClientProvider client={queryClient}>
      <MemoryRouter>
        <UserProfilePage />
      </MemoryRouter>
    </QueryClientProvider>,
  );
}

describe("UserProfilePage tests", () => {
  beforeEach(() => {
    axiosMock.reset();
    axiosMock.resetHistory();
    axiosMock
      .onGet("/api/systemInfo")
      .reply(200, systemInfoFixtures.showingNeither);
  });

  test("renders name, email, and roles for a regular user", async () => {
    axiosMock.onGet("/api/currentUser").reply(200, {
      user: { email: "cgaucho@ucsb.edu", fullName: "Chris Gaucho" },
      roles: [{ authority: "ROLE_USER" }],
    });

    renderPage();

    expect(
      await screen.findByTestId("UserProfilePage-title"),
    ).toHaveTextContent("User Profile");
    expect(screen.getByTestId("UserProfilePage-name")).toHaveTextContent(
      "Chris Gaucho",
    );
    expect(screen.getByTestId("UserProfilePage-email")).toHaveTextContent(
      "cgaucho@ucsb.edu",
    );
    expect(screen.getByTestId("UserProfilePage-roles")).toHaveTextContent(
      "user",
    );
  });

  test("shows 'Not specified' when the user has no fullName", async () => {
    axiosMock.onGet("/api/currentUser").reply(200, {
      user: { email: "cgaucho@ucsb.edu" },
      roles: [{ authority: "ROLE_USER" }],
    });

    renderPage();

    expect(await screen.findByTestId("UserProfilePage-name")).toHaveTextContent(
      "Not specified",
    );
  });

  test("renders roles for an admin/hostmanager user", async () => {
    axiosMock.onGet("/api/currentUser").reply(200, {
      user: { email: "admin@ucsb.edu", fullName: "Admin User" },
      roles: [
        { authority: "ROLE_USER" },
        { authority: "ROLE_ADMIN" },
        { authority: "ROLE_HOSTMANAGER" },
      ],
    });

    renderPage();

    expect(
      await screen.findByTestId("UserProfilePage-roles"),
    ).toHaveTextContent("user, admin, hostmanager");
  });
});
