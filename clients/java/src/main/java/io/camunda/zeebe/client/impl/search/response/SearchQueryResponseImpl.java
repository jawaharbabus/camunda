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

import io.camunda.zeebe.client.api.search.response.SearchQueryResponse;
import io.camunda.zeebe.client.api.search.response.SearchResponsePage;
import java.util.List;

public final class SearchQueryResponseImpl<T> implements SearchQueryResponse<T> {

  private final List<T> items;
  private final SearchResponsePage page;

  public SearchQueryResponseImpl(final List<T> items, final SearchResponsePage page) {
    this.items = items;
    this.page = page;
  }

  @Override
  public List<T> items() {
    return items;
  }

  @Override
  public SearchResponsePage page() {
    return page;
  }
}
