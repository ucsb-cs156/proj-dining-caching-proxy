import {
  fireEvent,
  render,
  screen,
  waitFor,
  within,
} from "@testing-library/react";
import { QueryClient, QueryClientProvider } from "@tanstack/react-query";
import axios from "axios";
import AxiosMockAdapter from "axios-mock-adapter";
import { vi } from "vitest";
import { toast } from "react-toastify";
import usersFixtures from "fixtures/usersFixtures";
import UsersTable from "main/components/Users/UsersTable";
import { useBackendMutation } from "main/utils/useBackend";

vi.mock("react-toastify", async (importOriginal) => {
  const actual = await importOriginal();
  return {
    ...actual,
    toast: vi.fn(),
  };
});

vi.mock("main/utils/useBackend", async (importOriginal) => {
  const actual = await importOriginal();
  return {
    ...actual,
    useBackendMutation: vi.fn(actual.useBackendMutation),
  };
});

const createQueryClient = () =>
  new QueryClient({
    defaultOptions: {
      queries: { retry: false },
      mutations: { retry: false },
    },
  });

const renderWithQueryClient = (ui) => {
  const queryClient = createQueryClient();

  return {
    queryClient,
    ...render(
      <QueryClientProvider client={queryClient}>{ui}</QueryClientProvider>,
    ),
  };
};

describe("UserTable tests", () => {
  let axiosMock;

  beforeEach(() => {
    axiosMock = new AxiosMockAdapter(axios);
    vi.clearAllMocks();
  });

  afterEach(() => {
    axiosMock.restore();
  });

  test("renders without crashing for empty table", () => {
    renderWithQueryClient(<UsersTable users={[]} />);
  });

  test("renders without crashing for three users", () => {
    renderWithQueryClient(<UsersTable users={usersFixtures.threeUsers} />);
  });

  test("Has the expected colum headers and content", () => {
    renderWithQueryClient(<UsersTable users={usersFixtures.threeUsers} />);

    const expectedHeaders = [
      "id",
      "First Name",
      "Last Name",
      "Email",
      "Admin",
      "Host Manager",
    ];
    const expectedFields = [
      "id",
      "givenName",
      "familyName",
      "email",
      "admin",
      "hostManager",
    ];
    const testId = "UsersTable";

    expectedHeaders.forEach((headerText) => {
      const header = screen.getByText(headerText);
      expect(header).toBeInTheDocument();
    });

    expectedFields.forEach((field) => {
      const header = screen.getByTestId(`${testId}-cell-row-0-col-${field}`);
      expect(header).toBeInTheDocument();
    });

    expect(screen.getByTestId(`${testId}-cell-row-0-col-id`)).toHaveTextContent(
      "1",
    );
    expect(
      screen.getByTestId(`${testId}-cell-row-0-col-admin`),
    ).toHaveTextContent("true");
    expect(screen.getByTestId(`${testId}-cell-row-1-col-id`)).toHaveTextContent(
      "2",
    );
    expect(
      screen.getByTestId(`${testId}-cell-row-1-col-admin`),
    ).toHaveTextContent("false");

    expect(
      screen.getByTestId(`${testId}-cell-row-2-col-hostManager`),
    ).toHaveTextContent("true");
  });

  test("does not show toggle buttons by default", () => {
    renderWithQueryClient(<UsersTable users={usersFixtures.threeUsers} />);

    expect(
      screen.queryByRole("columnheader", { name: "Toggle Admin" }),
    ).not.toBeInTheDocument();
    expect(
      screen.queryByRole("columnheader", { name: "Toggle Host Manager" }),
    ).not.toBeInTheDocument();
    expect(
      screen.queryByTestId("UsersTable-cell-row-0-col-toggle-admin-button"),
    ).not.toBeInTheDocument();
    expect(
      screen.queryByTestId(
        "UsersTable-cell-row-0-col-toggle-hostManager-button",
      ),
    ).not.toBeInTheDocument();
  });

  test("shows toggle columns and buttons when enabled", () => {
    renderWithQueryClient(
      <UsersTable users={usersFixtures.threeUsers} showToggleButtons={true} />,
    );

    expect(
      screen.getByRole("columnheader", { name: "Toggle Admin" }),
    ).toBeInTheDocument();
    expect(
      screen.getByRole("columnheader", { name: "Toggle Host Manager" }),
    ).toBeInTheDocument();

    const toggleAdminCell = screen.getByTestId(
      "UsersTable-cell-row-0-col-toggle-admin",
    );
    const toggleHostManagerCell = screen.getByTestId(
      "UsersTable-cell-row-0-col-toggle-hostManager",
    );

    expect(toggleAdminCell).toBeInTheDocument();
    expect(toggleHostManagerCell).toBeInTheDocument();

    expect(
      within(toggleAdminCell).getByRole("button", { name: "Toggle Admin" }),
    ).toBeInTheDocument();
    expect(
      within(toggleHostManagerCell).getByRole("button", {
        name: "Toggle Host Manager",
      }),
    ).toBeInTheDocument();
  });

  test("useBackendMutation is configured to refresh admin users after toggles", () => {
    renderWithQueryClient(
      <UsersTable users={usersFixtures.threeUsers} showToggleButtons={true} />,
    );

    expect(useBackendMutation).toHaveBeenNthCalledWith(
      1,
      expect.any(Function),
      expect.objectContaining({ onSuccess: expect.any(Function) }),
      ["/api/admin/users"],
    );

    expect(useBackendMutation).toHaveBeenNthCalledWith(
      2,
      expect.any(Function),
      expect.objectContaining({ onSuccess: expect.any(Function) }),
      ["/api/admin/users"],
    );
  });

  test("Clicking Toggle Admin calls the toggleAdmin endpoint", async () => {
    const { queryClient } = renderWithQueryClient(
      <UsersTable users={usersFixtures.threeUsers} showToggleButtons={true} />,
    );

    const invalidateQueriesSpy = vi.spyOn(queryClient, "invalidateQueries");

    axiosMock.onPut("/api/admin/toggleAdmin").reply((config) => {
      expect(config.params).toEqual({ id: 1 });
      return [200, { ...usersFixtures.threeUsers[0], admin: false }];
    });

    fireEvent.click(
      screen.getByTestId("UsersTable-cell-row-0-col-toggle-admin-button"),
    );

    await waitFor(() => {
      expect(axiosMock.history.put.length).toBe(1);
    });

    expect(axiosMock.history.put[0].url).toBe("/api/admin/toggleAdmin");
    expect(axiosMock.history.put[0].params).toEqual({ id: 1 });

    await waitFor(() => {
      expect(toast).toHaveBeenCalledWith("Admin status toggled");
    });

    await waitFor(() => {
      expect(invalidateQueriesSpy).toHaveBeenCalledWith({
        queryKey: ["/api/admin/users"],
      });
    });
  });

  test("Clicking Toggle Host Manager calls the toggleHostManager endpoint", async () => {
    const { queryClient } = renderWithQueryClient(
      <UsersTable users={usersFixtures.threeUsers} showToggleButtons={true} />,
    );

    const invalidateQueriesSpy = vi.spyOn(queryClient, "invalidateQueries");

    axiosMock.onPut("/api/admin/toggleHostManager").reply((config) => {
      expect(config.params).toEqual({ id: 1 });
      return [200, { ...usersFixtures.threeUsers[0], hostManager: true }];
    });

    fireEvent.click(
      screen.getByTestId("UsersTable-cell-row-0-col-toggle-hostManager-button"),
    );

    await waitFor(() => {
      expect(axiosMock.history.put.length).toBe(1);
    });

    expect(axiosMock.history.put[0].url).toBe("/api/admin/toggleHostManager");
    expect(axiosMock.history.put[0].params).toEqual({ id: 1 });

    await waitFor(() => {
      expect(toast).toHaveBeenCalledWith("Host Manager status toggled");
    });

    await waitFor(() => {
      expect(invalidateQueriesSpy).toHaveBeenCalledWith({
        queryKey: ["/api/admin/users"],
      });
    });
  });
});
