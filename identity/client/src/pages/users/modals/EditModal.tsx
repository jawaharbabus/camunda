/*
 * Copyright Camunda Services GmbH and/or licensed to Camunda Services GmbH under
 * one or more contributor license agreements. See the NOTICE file distributed
 * with this work for additional information regarding copyright ownership.
 * Licensed under the Camunda License 1.0. You may not use this file
 * except in compliance with the Camunda License 1.0.
 */
import { FC, useState } from "react";
import TextField from "src/components/form/TextField";
import { useApiCall } from "src/utility/api";
import useTranslate from "src/utility/localization";
import { FormModal, UseEntityModalProps } from "src/components/modal";
import { updateUser, User } from "src/utility/api/users";

const EditModal: FC<UseEntityModalProps<User>> = ({
  open,
  onClose,
  onSuccess,
  entity: { id, email: currentEmail, name: currentName, username },
}) => {
  const { t } = useTranslate();
  const [apiCall, { loading, namedErrors }] = useApiCall(updateUser);
  const [name, setName] = useState(currentName);
  const [email, setEmail] = useState(currentEmail);
  const [password, setPassword] = useState("");

  const handleSubmit = async () => {
    const { success } = await apiCall({
      id,
      name,
      email,
      username,
      password,
    });

    if (success) {
      onSuccess();
    }
  };

  return (
    <FormModal
      open={open}
      headline={t("Edit user")}
      onClose={onClose}
      onSubmit={handleSubmit}
      loading={loading}
      loadingDescription={t("Updating user")}
      confirmLabel={t("Update user")}
    >
      <TextField
        label={t("Name")}
        value={name}
        placeholder={t("Name")}
        onChange={setName}
        errors={namedErrors?.name}
        autoFocus
      />
      <TextField
        label={t("Email")}
        value={email}
        placeholder={t("Email")}
        onChange={setEmail}
        errors={namedErrors?.email}
      />
      <TextField
        label={t("Username")}
        value={username}
        placeholder={t("Username")}
        errors={namedErrors?.username}
        disabled
      />
      <TextField
        label={t("Password")}
        value={password}
        placeholder={t("Password")}
        onChange={setPassword}
        errors={namedErrors?.password}
        type="password"
        helperText={t("Leave empty to keep current password")}
      />
    </FormModal>
  );
};

export default EditModal;
