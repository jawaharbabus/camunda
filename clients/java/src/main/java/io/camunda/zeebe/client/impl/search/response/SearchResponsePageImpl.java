/*
 * Copyright © 2017 camunda services GmbH (info@camunda.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.camunda.zeebe.client.impl.search.response;

import io.camunda.zeebe.client.api.search.response.SearchResponsePage;
import java.util.List;

public class SearchResponsePageImpl implements SearchResponsePage {

  private final long totalItems;
  private final List<Object> firstSortValues;
  private final List<Object> lastSortValues;

  public SearchResponsePageImpl(
      final long totalItems,
      final List<Object> firstSortValues,
      final List<Object> lastSortValues) {
    this.totalItems = totalItems;
    this.firstSortValues = firstSortValues;
    this.lastSortValues = lastSortValues;
  }

  @Override
  public Long totalItems() {
    return totalItems;
  }

  @Override
  public List<Object> firstSortValues() {
    return firstSortValues;
  }

  @Override
  public List<Object> lastSortValues() {
    return lastSortValues;
  }
}
