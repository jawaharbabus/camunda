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
package io.camunda.zeebe.client.impl.search;

import io.camunda.zeebe.client.protocol.rest.SearchQuerySortRequest;
import java.util.ArrayList;
import java.util.List;

public abstract class SearchQuerySortBase<T>
    extends TypedSearchRequestPropertyProvider<List<SearchQuerySortRequest>> {

  private final List<SearchQuerySortRequest> sorting = new ArrayList<>();
  private SearchQuerySortRequest current;

  protected T field(final String value) {
    current = new SearchQuerySortRequest();
    current.field(value);
    return self();
  }

  protected T order(final String order) {
    if (current != null) {
      current.order(order);
      sorting.add(current);
      current = null;
    }
    return self();
  }

  public T asc() {
    return order("asc");
  }

  public T desc() {
    return order("desc");
  }

  protected abstract T self();

  @Override
  protected List<SearchQuerySortRequest> getSearchRequestProperty() {
    return sorting;
  }
}
