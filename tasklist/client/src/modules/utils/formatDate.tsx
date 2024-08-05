/*
 * Copyright Camunda Services GmbH and/or licensed to Camunda Services GmbH under
 * one or more contributor license agreements. See the NOTICE file distributed
 * with this work for additional information regarding copyright ownership.
 * Licensed under the Camunda License 1.0. You may not use this file
 * except in compliance with the Camunda License 1.0.
 */

import {format, parseISO} from 'date-fns';
import {logger} from './logger';
import {getCurrentDateLocale} from 'modules/internationalization';

const formatDate = (dateString: string, showTime = true) => {
  try {
    return format(
      parseISO(dateString),
      showTime ? 'dd MMM yyyy - p' : 'dd MMM yyyy',
      {
        locale: getCurrentDateLocale(),
      },
    );
  } catch (error) {
    logger.error(error);
    return '';
  }
};

export {formatDate};
