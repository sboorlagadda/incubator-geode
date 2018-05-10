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


import org.springframework.shell.core.annotation.CliCommand;
import org.springframework.shell.core.annotation.CliOption;

import org.apache.geode.cache.configuration.ClusterCacheElement;
import org.apache.geode.connectors.jdbc.internal.configuration.ClusterRegionMapping;
import org.apache.geode.management.cli.CliMetaData;
import org.apache.geode.management.cli.GfshCommand;
import org.apache.geode.management.internal.cli.i18n.CliStrings;
import org.apache.geode.management.internal.cli.result.model.ResultModel;
import org.apache.geode.management.internal.security.ResourceOperation;
import org.apache.geode.security.ResourcePermission;

public class AlterMappingCommand extends GfshCommand {
  static final String ALTER_MAPPING = "alter jdbc-mapping";
  static final String ALTER_MAPPING__HELP = "Alter properties for an existing jdbc mapping.";

  static final String ALTER_MAPPING__REGION_NAME = "region";
  static final String ALTER_MAPPING__REGION_NAME__HELP =
      "Name of the region the mapping to be altered.";
  static final String ALTER_MAPPING__PDX_CLASS_NAME = "pdx-class-name";
  static final String ALTER_MAPPING__PDX_CLASS_NAME__HELP =
      "Name of new pdx class for which values with be written to the database.";
  static final String ALTER_MAPPING__TABLE_NAME = "table";
  static final String ALTER_MAPPING__TABLE_NAME__HELP =
      "Name of new database table for values to be written to.";
  static final String ALTER_MAPPING__CONNECTION_NAME = "connection";
  static final String ALTER_MAPPING__CONNECTION_NAME__HELP = "Name of new JDBC connection to use.";
  static final String ALTER_MAPPING__PRIMARY_KEY_IN_VALUE = "primary-key-in-value";
  static final String ALTER_MAPPING__PRIMARY_KEY_IN_VALUE__HELP =
      "If false, the entry value does not contain the data used for the database table's primary key, instead the entry key will be used for the primary key column value.";
  static final String ALTER_MAPPING__FIELD_MAPPING = "field-mapping";
  static final String ALTER_MAPPING__FIELD_MAPPING__HELP =
      "New key value pairs of entry value fields to database columns.";

  @CliCommand(value = ALTER_MAPPING, help = ALTER_MAPPING__HELP)
  @CliMetaData(relatedTopic = CliStrings.DEFAULT_TOPIC_GEODE)
  @ResourceOperation(resource = ResourcePermission.Resource.CLUSTER,
      operation = ResourcePermission.Operation.MANAGE)
  public ResultModel alterMapping(
      @CliOption(key = ALTER_MAPPING__REGION_NAME, mandatory = true,
          help = ALTER_MAPPING__REGION_NAME__HELP) String regionName,
      @CliOption(key = ALTER_MAPPING__CONNECTION_NAME, specifiedDefaultValue = "",
          help = ALTER_MAPPING__CONNECTION_NAME__HELP) String connectionName,
      @CliOption(key = ALTER_MAPPING__TABLE_NAME, help = ALTER_MAPPING__TABLE_NAME__HELP,
          specifiedDefaultValue = "") String table,
      @CliOption(key = ALTER_MAPPING__PDX_CLASS_NAME, help = ALTER_MAPPING__PDX_CLASS_NAME__HELP,
          specifiedDefaultValue = "") String pdxClassName,
      @CliOption(key = ALTER_MAPPING__PRIMARY_KEY_IN_VALUE,
          help = ALTER_MAPPING__PRIMARY_KEY_IN_VALUE__HELP,
          specifiedDefaultValue = "true") Boolean keyInValue,
      @CliOption(key = ALTER_MAPPING__FIELD_MAPPING, help = ALTER_MAPPING__FIELD_MAPPING__HELP,
          specifiedDefaultValue = "") String[] fieldMappings) {
    // input
    ClusterRegionMapping newMapping =
        new ClusterRegionMapping(regionName, pdxClassName, table, connectionName, keyInValue);
    newMapping.setFieldMapping(fieldMappings);
    return persistCacheElement(newMapping, null, null, ClusterCacheElement.Operation.UPDATE);
  }
}
