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

import org.apache.geode.cache.configuration.CacheConfig;
import org.apache.geode.cache.configuration.CacheElement;
import org.apache.geode.cache.execute.Function;
import org.apache.geode.connectors.jdbc.internal.configuration.ConnectorService;
import org.apache.geode.management.cli.CliMetaData;
import org.apache.geode.management.cli.SingleGfshCommand;
import org.apache.geode.management.internal.cli.i18n.CliStrings;
import org.apache.geode.management.internal.security.ResourceOperation;
import org.apache.geode.security.ResourcePermission;

public class DestroyMappingCommand extends SingleGfshCommand {
  static final String DESTROY_MAPPING = "destroy jdbc-mapping";
  static final String DESTROY_MAPPING__HELP = "Destroy the specified mapping.";
  static final String DESTROY_MAPPING__REGION_NAME = "region";
  static final String DESTROY_MAPPING__REGION_NAME__HELP = "Name of the region mapping to destroy.";

  private static final String ERROR_PREFIX = "ERROR: ";

  @CliCommand(value = DESTROY_MAPPING, help = DESTROY_MAPPING__HELP)
  @CliMetaData(relatedTopic = CliStrings.DEFAULT_TOPIC_GEODE)
  @ResourceOperation(resource = ResourcePermission.Resource.CLUSTER,
      operation = ResourcePermission.Operation.MANAGE)
  public Object destroyMapping(@CliOption(key = DESTROY_MAPPING__REGION_NAME, mandatory = true,
      help = DESTROY_MAPPING__REGION_NAME__HELP) String regionName) {
    return regionName;
  }

  @Override
  public Function getFunction() {
    return new DestroyMappingFunction();
  }

  @Override
  public void updateClusterConfig(String group, CacheConfig config, Object element) {
    ConnectorService service =
        config.findCustomCacheElement("connector-service", ConnectorService.class);
    if (service != null) {
      CacheElement.removeElement(service.getRegionMapping(), (String) element);
    }
  }
}
