import BasicLayout from "main/layouts/BasicLayout/BasicLayout";
import { useCurrentUser } from "main/utils/currentUser";

const testIdPrefix = "UserProfilePage";

// Roles come back from the backend as e.g. "ROLE_ADMIN"; display them as "admin".
function friendlyRoleName(role: string): string {
  return role.replace(/^ROLE_/, "").toLowerCase();
}

export default function UserProfilePage(): React.JSX.Element {
  const currentUser = useCurrentUser();

  if (!currentUser.loggedIn) {
    return <BasicLayout>Loading...</BasicLayout>;
  }

  const { user, rolesList } = currentUser.root;

  return (
    <BasicLayout>
      <div className="pt-2">
        <h1 data-testid={`${testIdPrefix}-title`}>User Profile</h1>
        <table className="table table-striped" style={{ maxWidth: "600px" }}>
          <tbody>
            <tr>
              <th>Name</th>
              <td data-testid={`${testIdPrefix}-name`}>
                {user.fullName ?? "Not specified"}
              </td>
            </tr>
            <tr>
              <th>Email</th>
              <td data-testid={`${testIdPrefix}-email`}>{user.email}</td>
            </tr>
            <tr>
              <th>Roles</th>
              <td data-testid={`${testIdPrefix}-roles`}>
                {rolesList.map(friendlyRoleName).join(", ")}
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </BasicLayout>
  );
}
