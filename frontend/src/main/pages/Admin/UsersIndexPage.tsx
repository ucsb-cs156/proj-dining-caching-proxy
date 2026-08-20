import BasicLayout from "main/layouts/BasicLayout/BasicLayout";
import UsersTable, { type User } from "main/components/Users/UsersTable";
import { useBackend } from "main/utils/useBackend";

export default function UsersIndexPage(): React.JSX.Element {
  const { data: users } = useBackend<User[]>(
    // Stryker disable next-line all : don't test internal caching of React Query
    ["/api/admin/users"],
    { method: "GET", url: "/api/admin/users" },
    // Stryker disable next-line all : don't test default value of empty list
    [],
  );

  return (
    <BasicLayout>
      <div className="pt-2">
        <h1>Users</h1>
        <UsersTable users={users ?? []} showToggleButtons={true} />
      </div>
    </BasicLayout>
  );
}
