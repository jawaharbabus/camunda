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
package io.camunda.process.test.impl.client;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.Optional;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.cookie.BasicCookieStore;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClientBuilder;
import org.apache.hc.core5.http.ClassicHttpResponse;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.http.io.entity.HttpEntities;

public class OperateApiClient {

  private static final String LOGIN_ENDPOINT = "/api/login?username=%s&password=%s";
  private static final String LOGIN_USERNAME = "demo";
  private static final String LOGIN_PASSWORD = "demo";

  private static final String PROCESS_INSTANCE_GET_ENDPOINT = "/v1/process-instances/%d";
  private static final String FLOW_NODE_INSTANCES_SEARCH_ENDPOINT = "/v1/flownode-instances/search";

  private final ObjectMapper objectMapper =
      new ObjectMapper().disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);

  private boolean isLoggedIn = false;

  private final String operateRestApi;

  private final CloseableHttpClient httpClient;

  public OperateApiClient(final String operateRestApi) {
    this.operateRestApi = operateRestApi;

    final BasicCookieStore cookieStore = new BasicCookieStore();
    httpClient = HttpClientBuilder.create().setDefaultCookieStore(cookieStore).build();
  }

  private void ensureAuthenticated() throws IOException {
    if (!isLoggedIn) {
      sendLoginRequest();
      isLoggedIn = true;
    }
  }

  private void sendLoginRequest() throws IOException {
    httpClient.execute(
        new HttpPost(
            String.format(operateRestApi + LOGIN_ENDPOINT, LOGIN_USERNAME, LOGIN_PASSWORD)),
        response -> {
          if (response.getCode() != 204) {
            throw new IllegalStateException(
                String.format(
                    "Failed to login. [code: %d, message: %s]",
                    response.getCode(), getReponseAsString(response)));
          }
          return null;
        });
  }

  private static void verifyStatusCode(final ClassicHttpResponse response) {
    if (response.getCode() == 404) {
      throw new ZeebeClientNotFoundException(
          String.format(
              "Failed send request. Object not found. [code: %d, message: %s]",
              response.getCode(), getReponseAsString(response)));
    }
    if (response.getCode() != 200) {
      throw new RuntimeException(
          String.format(
              "Failed send request. [code: %d, message: %s]",
              response.getCode(), getReponseAsString(response)));
    }
  }

  public ProcessInstanceDto getProcessInstanceByKey(final long processInstanceKey)
      throws IOException {
    ensureAuthenticated();

    final String body =
        sendGetRequest(String.format(PROCESS_INSTANCE_GET_ENDPOINT, processInstanceKey));
    return objectMapper.readValue(body, ProcessInstanceDto.class);
  }

  public FlowNodeInstancesResponseDto findFlowNodeInstancesByProcessInstanceKey(
      final long processInstanceKey) throws IOException {
    ensureAuthenticated();

    final String requestBody =
        String.format("{\"filter\": {\"processInstanceKey\":%d}}", processInstanceKey);
    final String responseBody = sendPostRequest(FLOW_NODE_INSTANCES_SEARCH_ENDPOINT, requestBody);
    return objectMapper.readValue(responseBody, FlowNodeInstancesResponseDto.class);
  }

  private static String getReponseAsString(final ClassicHttpResponse response) {
    return Optional.ofNullable(response.getEntity())
        .map(
            entity -> {
              try {
                return EntityUtils.toString(entity);
              } catch (final Exception e) {
                throw new RuntimeException(e);
              }
            })
        .orElse("?");
  }

  private String sendGetRequest(final String endpoint) throws IOException {
    return httpClient.execute(
        new HttpGet(operateRestApi + endpoint),
        response -> {
          verifyStatusCode(response);
          return getReponseAsString(response);
        });
  }

  private String sendPostRequest(final String endpoint, final String body) throws IOException {

    final HttpPost request = new HttpPost(operateRestApi + endpoint);
    request.setEntity(HttpEntities.create(body, ContentType.APPLICATION_JSON));

    return httpClient.execute(
        request,
        response -> {
          verifyStatusCode(response);
          return getReponseAsString(response);
        });
  }
}
