import OurTable from "main/components/Common/OurTable";
import { Button } from "react-bootstrap";
import { toast } from "react-toastify";
import { useBackendMutation } from "main/utils/useBackend";
import type { Cell } from "@tanstack/react-table";
import type { LegacyColumn } from "main/components/Common/OurTableUtils";

export type User = {
  id: number;
  givenName: string;
  familyName: string;
  email: string;
  admin: boolean;
  hostManager: boolean;
};

const columns: LegacyColumn[] = [
  {
    Header: "id",
    accessor: "id", // accessor is the "key" in the data
  },
  {
    header: "First Name",
    accessorKey: "givenName",
  },
  {
    header: "Last Name",
    accessorKey: "familyName",
  },
  {
    header: "Email",
    accessorKey: "email",
  },
  {
    header: "Admin",
    id: "admin",
    accessorKey: "admin",
    cell: ({ cell }: { cell: Cell<User, unknown> }) => {
      return String(cell.getValue());
    }, // convert boolean to string for display
  },
  {
    header: "Host Manager",
    id: "hostManager",
    accessorKey: "hostManager",
    cell: ({ cell }: { cell: Cell<User, unknown> }) => {
      return String(cell.getValue());
    }, // convert boolean to string for display
  },
];

type UsersTableProps = {
  users: User[];
  showToggleButtons?: boolean;
};

export default function UsersTable({
  users,
  showToggleButtons = false,
}: UsersTableProps): React.JSX.Element {
  const toggleAdminMutation = useBackendMutation<Cell<User, unknown>>(
    (cell) => ({
      url: "/api/admin/toggleAdmin",
      method: "PUT",
      params: { id: cell.row.original.id },
    }),
    { onSuccess: () => toast("Admin status toggled") },
    ["/api/admin/users"],
  );

  const toggleHostManagerMutation = useBackendMutation<Cell<User, unknown>>(
    (cell) => ({
      url: "/api/admin/toggleHostManager",
      method: "PUT",
      params: { id: cell.row.original.id },
    }),
    { onSuccess: () => toast("Host Manager status toggled") },
    ["/api/admin/users"],
  );

  const toggleAdminColumn = {
    header: "Toggle Admin",
    id: "toggle-admin",
    cell: ({ cell }: { cell: Cell<User, unknown> }) => (
      <Button
        variant="primary"
        onClick={() => toggleAdminMutation.mutate(cell)}
        data-testid={`UsersTable-cell-row-${cell.row.index}-col-toggle-admin-button`}
      >
        Toggle Admin
      </Button>
    ),
  };

  const toggleHostManagerColumn = {
    header: "Toggle Host Manager",
    id: "toggle-hostManager",
    cell: ({ cell }: { cell: Cell<User, unknown> }) => (
      <Button
        variant="primary"
        onClick={() => toggleHostManagerMutation.mutate(cell)}
        data-testid={`UsersTable-cell-row-${cell.row.index}-col-toggle-hostManager-button`}
      >
        Toggle Host Manager
      </Button>
    ),
  };

  return (
    <OurTable
      data={users}
      columns={
        showToggleButtons
          ? [...columns, toggleAdminColumn, toggleHostManagerColumn]
          : columns
      }
      testid={"UsersTable"}
    />
  );
}
