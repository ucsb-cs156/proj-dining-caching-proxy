import BasicLayout from "main/layouts/BasicLayout/BasicLayout";
import RoleEmailForm, {
  type RoleEmailFormFields,
} from "main/components/Users/RoleEmailForm";
import { useNavigate } from "react-router";
import { useBackendMutation } from "main/utils/useBackend";
import { toast } from "react-toastify";
import type { AxiosRequestConfig } from "axios";

type HostManagersCreatePageProps = {
  storybook?: boolean;
};

export default function HostManagersCreatePage({
  storybook = false,
}: HostManagersCreatePageProps): React.JSX.Element {
  const navigation = useNavigate();
  const objectToAxiosParams = (
    hostManager: RoleEmailFormFields,
  ): AxiosRequestConfig => ({
    url: "/api/admin/hostmanagers/post",
    method: "POST",
    params: {
      email: hostManager.email,
    },
  });

  const onSuccess = (hostManager: RoleEmailFormFields) => {
    toast(`New host manager added - email: ${hostManager.email}`);
    if (!storybook) navigation("/admin/hostmanagers");
  };

  const mutation = useBackendMutation(
    objectToAxiosParams,
    { onSuccess },
    ["/api/admin/hostmanagers/all"], // mutation makes this key stale so that pages relying on it reload
  );

  const onSubmit = async (data: RoleEmailFormFields) => {
    mutation.mutate(data);
  };

  return (
    <BasicLayout>
      <div className="pt-2">
        <h1>Add New Host Manager</h1>
        <RoleEmailForm submitAction={onSubmit} />
      </div>
    </BasicLayout>
  );
}
