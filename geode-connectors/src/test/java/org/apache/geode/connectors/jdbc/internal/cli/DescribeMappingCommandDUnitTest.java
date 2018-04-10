/*
 * Licensed to the Apache Software Foundation (ASF) under one or more contributor license
 * agreements. See the NOTICE file distributed with this work for additional information regarding
 * copyright ownership. The ASF licenses this file to You under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance with the License. You may obtain a
 * copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package org.apache.geode.connectors.jdbc.internal.cli;

import static org.apache.geode.connectors.jdbc.internal.cli.CreateMappingCommand.CREATE_MAPPING__CONNECTION_NAME;
import static org.apache.geode.connectors.jdbc.internal.cli.CreateMappingCommand.CREATE_MAPPING__PDX_CLASS_NAME;
import static org.apache.geode.connectors.jdbc.internal.cli.CreateMappingCommand.CREATE_MAPPING__REGION_NAME;
import static org.apache.geode.connectors.jdbc.internal.cli.CreateMappingCommand.CREATE_MAPPING__TABLE_NAME;
import static org.apache.geode.connectors.jdbc.internal.cli.CreateMappingCommand.CREATE_MAPPING__VALUE_CONTAINS_PRIMARY_KEY;
import static org.apache.geode.connectors.jdbc.internal.cli.DescribeMappingCommand.DESCRIBE_MAPPING;
import static org.apache.geode.connectors.jdbc.internal.cli.DescribeMappingCommand.DESCRIBE_MAPPING__REGION_NAME;
import static org.assertj.core.api.Assertions.assertThat;

import java.io.Serializable;

import org.apache.geode.connectors.jdbc.internal.configuration.ConnectorService;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import org.apache.geode.connectors.jdbc.internal.JdbcConnectorService;
import org.apache.geode.connectors.jdbc.internal.RegionMappingBuilder;
import org.apache.geode.connectors.jdbc.internal.RegionMappingExistsException;
import org.apache.geode.internal.cache.InternalCache;
import org.apache.geode.management.internal.cli.util.CommandStringBuilder;
import org.apache.geode.test.dunit.rules.ClusterStartupRule;
import org.apache.geode.test.dunit.rules.MemberVM;
import org.apache.geode.test.junit.assertions.CommandResultAssert;
import org.apache.geode.test.junit.categories.DistributedTest;
import org.apache.geode.test.junit.rules.GfshCommandRule;
import org.apache.geode.test.junit.rules.serializable.SerializableTestName;

@Category(DistributedTest.class)
public class DescribeMappingCommandDUnitTest implements Serializable {

  private static final String REGION_NAME = "testRegion";

  @Rule
  public transient GfshCommandRule gfsh = new GfshCommandRule();

  @Rule
  public ClusterStartupRule startupRule = new ClusterStartupRule();

  @Rule
  public SerializableTestName testName = new SerializableTestName();

  private MemberVM server;

  @Before
  public void before() throws Exception {
    MemberVM locator = startupRule.startLocatorVM(0);
    server = startupRule.startServerVM(1, locator.getPort());

    gfsh.connectAndVerify(locator);
  }

  @Test
  public void describesExistingMapping() {
    server.invoke(this::createMapping);
    CommandStringBuilder csb = new CommandStringBuilder(DESCRIBE_MAPPING)
        .addOption(DESCRIBE_MAPPING__REGION_NAME, REGION_NAME);

    CommandResultAssert commandResultAssert = gfsh.executeAndAssertThat(csb.toString());

    commandResultAssert.statusIsSuccess();
    commandResultAssert.containsKeyValuePair(CREATE_MAPPING__REGION_NAME, REGION_NAME);
    commandResultAssert.containsKeyValuePair(CREATE_MAPPING__CONNECTION_NAME, "connection");
    commandResultAssert.containsKeyValuePair(CREATE_MAPPING__TABLE_NAME, "testTable");
    commandResultAssert.containsKeyValuePair(CREATE_MAPPING__PDX_CLASS_NAME, "myPdxClass");
    commandResultAssert.containsKeyValuePair(CREATE_MAPPING__VALUE_CONTAINS_PRIMARY_KEY, "true");
    commandResultAssert.containsOutput("field1");
    commandResultAssert.containsOutput("field2");
    commandResultAssert.containsOutput("column1");
    commandResultAssert.containsOutput("column2");
  }

  @Test
  public void reportsNoMappingFound() {
    CommandStringBuilder csb = new CommandStringBuilder(DESCRIBE_MAPPING)
        .addOption(DESCRIBE_MAPPING__REGION_NAME, "nonExisting");

    CommandResultAssert commandResultAssert = gfsh.executeAndAssertThat(csb.toString());

    commandResultAssert.statusIsSuccess();
    commandResultAssert
        .containsOutput(String.format("Mapping for region '%s' not found", "nonExisting"));
  }

  private void createMapping() throws RegionMappingExistsException {
    InternalCache cache = ClusterStartupRule.getCache();
    JdbcConnectorService service = cache.getService(JdbcConnectorService.class);

    String[] fieldMappings = new String[] {"field1:column1", "field2:column2"};
    ConnectorService.RegionMapping regionMapping = new RegionMappingBuilder().withRegionName(REGION_NAME)
        .withConnectionConfigName("connection").withTableName("testTable")
        .withPdxClassName("myPdxClass").withPrimaryKeyInValue(true)
        .withFieldToColumnMappings(fieldMappings).build();
    service.createRegionMapping(regionMapping);

    assertThat(service.getMappingForRegion(REGION_NAME)).isNotNull();
  }


}
