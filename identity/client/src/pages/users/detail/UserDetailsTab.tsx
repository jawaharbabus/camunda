/*
 * Copyright Camunda Services GmbH and/or licensed to Camunda Services GmbH under
 * one or more contributor license agreements. See the NOTICE file distributed
 * with this work for additional information regarding copyright ownership.
 * Licensed under the Camunda License 1.0. You may not use this file
 * except in compliance with the Camunda License 1.0.
 */
import { FC } from "react";
import EntityDetail from "src/components/entityDetail";
import useTranslate from "src/utility/localization";
import { User } from "src/utility/api/users";

type UserDetailsProps = {
  user: User;
  loading: boolean;
};

const UserDetails: FC<UserDetailsProps> = ({
  user: { username, email, id },
  loading,
}) => {
  const { t } = useTranslate();

  return (
    <EntityDetail
      label={t("User details")}
      data={[
        {
          label: t("Username"),
          value: username,
        },
        { label: t("Email"), value: email || "-" },
        {
          label: t("ID"),
          value: id,
        },
      ]}
      loading={loading}
    />
  );
};

export default UserDetails;
