import { useBackend } from "main/utils/useBackend";
import BasicLayout from "main/layouts/BasicLayout/BasicLayout";
import RoleEmailTable, {
  type RoleEmail,
} from "main/components/Users/RoleEmailTable";
import { Link } from "react-router";

export default function HostManagersIndexPage(): React.JSX.Element {
  const { data: hostManagers } = useBackend<RoleEmail[]>(
    ["/api/admin/hostmanagers/all"],
    { method: "GET", url: "/api/admin/hostmanagers/all" },
    // Stryker disable next-line all : don't test default value of empty list
    [],
  );

  const createButton = () => {
    return (
      <Link
        className="btn btn-primary"
        to="/admin/hostmanagers/create"
        style={{ float: "right" }}
      >
        New Host Manager
      </Link>
    );
  };

  return (
    <BasicLayout>
      <div className="pt-2">
        {createButton()}
        <h1>Host Managers</h1>
        <RoleEmailTable
          data={hostManagers ?? []}
          deleteEndpoint="/api/admin/hostmanagers/delete"
          getEndpoint="/api/admin/hostmanagers/all"
          testIdPrefix="HostManagersIndexPage"
        />
      </div>
    </BasicLayout>
  );
}
