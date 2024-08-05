/*
 * Copyright Camunda Services GmbH and/or licensed to Camunda Services GmbH under
 * one or more contributor license agreements. See the NOTICE file distributed
 * with this work for additional information regarding copyright ownership.
 * Licensed under the Camunda License 1.0. You may not use this file
 * except in compliance with the Camunda License 1.0.
 */
package io.camunda.search.es.transformers.query;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import io.camunda.search.clients.query.SearchQuery;
import io.camunda.search.clients.query.SearchQueryBuilders;
import io.camunda.search.es.transformers.ElasticsearchTransformers;
import io.camunda.search.transformers.SearchTransfomer;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

public class HasChildQueryTransformerTest {

  private final ElasticsearchTransformers transformers = new ElasticsearchTransformers();
  private SearchTransfomer<SearchQuery, Query> transformer;

  @BeforeEach
  public void before() {
    transformer = transformers.getTransformer(SearchQuery.class);
  }

  private static Stream<Arguments> provideQueries() {
    return Stream.of(
        Arguments.arguments(
            SearchQueryBuilders.hasChildQuery("fooType", SearchQueryBuilders.term("fooField", 123)),
            "Query: {'has_child':{'query':{'term':{'fooField':{'value':123}}},'score_mode':'none','type':'fooType'}}"),
        Arguments.arguments(
            SearchQueryBuilders.hasChild()
                .type("fooType")
                .query(SearchQueryBuilders.term("fooField", 123))
                .build()
                .toSearchQuery(),
            "Query: {'has_child':{'query':{'term':{'fooField':{'value':123}}},'score_mode':'none','type':'fooType'}}"));
  }

  @ParameterizedTest
  @MethodSource("provideQueries")
  public void shouldApplyTransformer(final SearchQuery query, final String expectedResultQuery) {
    // given
    final var expectedQuery = expectedResultQuery.replace("'", "\"");

    // when
    final var result = transformer.apply(query);

    // then
    assertThat(result).isNotNull();
    assertThat(result.toString()).isEqualTo(expectedQuery);
  }

  @Test
  public void shouldThrowErrorOnNullQuery() {
    // given

    // when - throw
    assertThatThrownBy(() -> SearchQueryBuilders.hasChild().type("fooType").build())
        .hasMessageContaining("Expected a non-null query parameter for the hasChild query.")
        .isInstanceOf(NullPointerException.class);
  }

  @Test
  public void shouldThrowErrorOnNullQueryFactoryMethod() {
    // given

    // when - throw
    assertThatThrownBy(() -> SearchQueryBuilders.hasChildQuery("fooType", null))
        .hasMessageContaining("Expected a non-null query parameter for the hasChild query.")
        .isInstanceOf(NullPointerException.class);
  }

  @Test
  public void shouldThrowErrorOnNullType() {
    // given

    // when - throw
    assertThatThrownBy(
            () -> SearchQueryBuilders.hasChild().query(SearchQueryBuilders.term("foo", 1)).build())
        .hasMessageContaining(
            "Expected a non-null type parameter for the hasChild query, with query:")
        .hasMessageContaining("field=foo")
        .hasMessageContaining("value=1")
        .isInstanceOf(NullPointerException.class);
  }

  @Test
  public void shouldThrowErrorOnNullTypeFactoryMethod() {
    // given

    // when - throw
    assertThatThrownBy(
            () -> SearchQueryBuilders.hasChildQuery(null, SearchQueryBuilders.term("foo", 1)))
        .hasMessageContaining(
            "Expected a non-null type parameter for the hasChild query, with query:")
        .hasMessageContaining("field=foo")
        .hasMessageContaining("value=1")
        .isInstanceOf(NullPointerException.class);
  }
}
