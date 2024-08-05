/*
 * Copyright Camunda Services GmbH and/or licensed to Camunda Services GmbH under
 * one or more contributor license agreements. See the NOTICE file distributed
 * with this work for additional information regarding copyright ownership.
 * Licensed under the Camunda License 1.0. You may not use this file
 * except in compliance with the Camunda License 1.0.
 */
import { FC } from "react";
import { useNavigate, useParams } from "react-router";
import useTranslate from "src/utility/localization";
import { useApi } from "src/utility/api/hooks";
import NotFound from "src/pages/not-found";
import { OverflowMenu, OverflowMenuItem, Section } from "@carbon/react";
import { StackPage } from "src/components/layout/Page";
import PageHeadline from "src/components/layout/PageHeadline";
import { getUserDetails } from "src/utility/api/users";
import UserDetails from "./UserDetailsTab";
import Tabs from "src/components/tabs";
import { DetailPageHeaderFallback } from "src/components/fallbacks";
import Flex from "src/components/layout/Flex";
import { useEntityModal } from "src/components/modal";
import EditModal from "src/pages/users/modals/EditModal";
import DeleteModal from "src/pages/users/modals/DeleteModal";
import List from "src/pages/users/detail/role/List";

const Details: FC = () => {
  const { t } = useTranslate();
  const { id = "", tab = "details" } = useParams<{ id: string; tab: string }>();
  const navigate = useNavigate();
  const {
    data: user,
    loading,
    reload,
  } = useApi(getUserDetails, {
    id,
  });
  const [editUser, editUserModal] = useEntityModal(EditModal, reload);
  const [deleteUser, deleteUserModal] = useEntityModal(DeleteModal, () =>
    navigate("..", { replace: true }),
  );

  if (!loading && !user) return <NotFound />;

  return (
    <StackPage>
      <>
        {loading && !user ? (
          <DetailPageHeaderFallback hasOverflowMenu={false} />
        ) : (
          <Flex>
            {user && (
              <>
                <PageHeadline>{user.username}</PageHeadline>
                <OverflowMenu ariaLabel={t("Open users context menu")}>
                  <OverflowMenuItem
                    itemText={t("Update")}
                    onClick={() => {
                      editUser(user);
                    }}
                  />
                  <OverflowMenuItem
                    itemText={t("Delete")}
                    onClick={() => {
                      deleteUser(user);
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
                label: t("User details"),
                content: user && <UserDetails user={user} loading={loading} />,
              },
              {
                key: "roles",
                label: t("Assigned roles"),
                content: user && <List user={user} loadingUser={loading} />,
              },
            ]}
            selectedTabKey={tab}
            path={`../${id}`}
          />
        </Section>
      </>
      {editUserModal}
      {deleteUserModal}
    </StackPage>
  );
};

export default Details;
