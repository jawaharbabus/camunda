import { FC } from "react";
import { useNavigate, useParams } from "react-router";
import useTranslate from "src/utility/localization";
import { useApi } from "src/utility/api/hooks";
import NotFound from "src/pages/not-found";
import { OverflowMenu, OverflowMenuItem, Section } from "@carbon/react";
import { StackPage } from "src/components/layout/Page";
import PageHeadline from "src/components/layout/PageHeadline";
import { getRole } from "src/utility/api/roles";
import RoleDetails from "./RoleDetails";
import { useEntityModal } from "src/components/modal";
import DeleteModal from "src/pages/roles/modals/DeleteModal";
import Flex from "src/components/layout/Flex";
import { DetailPageHeaderFallback } from "src/components/fallbacks";
import Tabs from "src/components/tabs";
import EditModal from "src/pages/roles/modals/EditModal";
import RolePermissions from "src/pages/roles/detail/RolePermissions";

const Details: FC = () => {
  const navigate = useNavigate();
  const { t } = useTranslate();
  const { id = "", tab = "details" } = useParams<{
    id: string;
    tab: string;
  }>();

  const { data: role, loading } = useApi(getRole, {
    id,
  });

  const [deleteRole, deleteModal] = useEntityModal(DeleteModal, () =>
    navigate("..", { replace: true }),
  );

  const [editRole, editRoleModal] = useEntityModal(EditModal, () =>
    navigate("..", { replace: true }),
  );

  if (!loading && !role) return <NotFound />;

  return (
    <StackPage>
      <>
        {loading && !role ? (
          <DetailPageHeaderFallback />
        ) : (
          <Flex>
            {role && (
              <>
                <PageHeadline>{role.name}</PageHeadline>
                <OverflowMenu ariaLabel={t("Open role context menu")}>
                  <OverflowMenuItem
                    itemText={t("Delete")}
                    isDelete
                    onClick={() => {
                      deleteRole(role);
                    }}
                  />
                  <OverflowMenuItem
                    itemText={t("Edit")}
                    onClick={() => {
                      editRole(role);
                    }}
                  />
                </OverflowMenu>
              </>
            )}
          </Flex>
        )}
        <Section>
          <Tabs
            tabs={[
              {
                key: "details",
                label: t("Role details"),
                content: <RoleDetails role={role} loading={loading} />,
              },
              {
                key: "permissions",
                label: t("Permissions"),
                content: <RolePermissions role={role} loading={loading} />,
              },
            ]}
            selectedTabKey={tab}
            path={`../${id}`}
          />
        </Section>
      </>
      {deleteModal}
      {editRoleModal}
    </StackPage>
  );
};

export default Details;
