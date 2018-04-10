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

import java.util.List;
import java.util.Set;

import org.springframework.shell.core.annotation.CliCommand;
import org.springframework.shell.core.annotation.CliOption;

import org.apache.geode.annotations.Experimental;
import org.apache.geode.connectors.jdbc.internal.configuration.ConnectorService;
import org.apache.geode.distributed.ClusterConfigurationService;
import org.apache.geode.distributed.DistributedMember;
import org.apache.geode.management.cli.CliMetaData;
import org.apache.geode.management.cli.GfshCommand;
import org.apache.geode.management.cli.Result;
import org.apache.geode.management.internal.cli.exceptions.EntityNotFoundException;
import org.apache.geode.management.internal.cli.functions.CliFunctionResult;
import org.apache.geode.management.internal.cli.i18n.CliStrings;
import org.apache.geode.management.internal.cli.result.CommandResult;
import org.apache.geode.management.internal.cli.result.ResultBuilder;
import org.apache.geode.management.internal.security.ResourceOperation;
import org.apache.geode.security.ResourcePermission;

@Experimental
public class AlterConnectionCommand extends GfshCommand {
  static final String ALTER_JDBC_CONNECTION = "alter jdbc-connection";
  static final String ALTER_JDBC_CONNECTION__HELP =
      EXPERIMENTAL + "Alter properties for an existing jdbc connection.";

  static final String ALTER_CONNECTION__NAME = "name";
  static final String ALTER_CONNECTION__NAME__HELP = "Name of the connection to be altered.";
  static final String ALTER_CONNECTION__URL = "url";
  static final String ALTER_CONNECTION__URL__HELP = "New URL location for the database.";
  static final String ALTER_CONNECTION__USER = "user";
  static final String ALTER_CONNECTION__USER__HELP =
      "New user name to use when connecting to database.";
  static final String ALTER_CONNECTION__PASSWORD = "password";
  static final String ALTER_CONNECTION__PASSWORD__HELP =
      "New password to use when connecting to database.";
  static final String ALTER_CONNECTION__PARAMS = "params";
  static final String ALTER_CONNECTION__PARAMS__HELP =
      "New additional parameters to use when connecting to the database. This replaces all previously existing parameters.";

  private static final String ERROR_PREFIX = "ERROR: ";

  @CliCommand(value = ALTER_JDBC_CONNECTION, help = ALTER_JDBC_CONNECTION__HELP)
  @CliMetaData(relatedTopic = CliStrings.DEFAULT_TOPIC_GEODE)
  @ResourceOperation(resource = ResourcePermission.Resource.CLUSTER,
      operation = ResourcePermission.Operation.MANAGE)
  public Result alterConnection(
      @CliOption(key = ALTER_CONNECTION__NAME, mandatory = true,
          help = ALTER_CONNECTION__NAME__HELP) String name,
      @CliOption(key = ALTER_CONNECTION__URL, specifiedDefaultValue = "",
          help = ALTER_CONNECTION__URL__HELP) String url,
      @CliOption(key = ALTER_CONNECTION__USER, specifiedDefaultValue = "",
          help = ALTER_CONNECTION__USER__HELP) String user,
      @CliOption(key = ALTER_CONNECTION__PASSWORD, specifiedDefaultValue = "",
          help = ALTER_CONNECTION__PASSWORD__HELP) String password,
      @CliOption(key = ALTER_CONNECTION__PARAMS, specifiedDefaultValue = "",
          help = ALTER_CONNECTION__PARAMS__HELP) String[] params) {
    // input
    Set<DistributedMember> targetMembers = getMembers(null, null);
    ConnectorService.Connection connection = new ConnectorService.Connection();
    connection.setUser(user);
    connection.setPassword(password);
    connection.setUrl(url);
    connection.setName(name);
    connection.setParameters(params);

    // action
    List<CliFunctionResult> results = executeAndGetFunctionResult(new AlterConnectionFunction(), connection, targetMembers);

    boolean persisted = false;
    ClusterConfigurationService ccService = getConfigurationService();

    if(ccService != null && results.stream().filter(CliFunctionResult::isSuccessful).count() > 0) {
      ConnectorService service = ccService.getCustomCacheElement("cluster", "connector-service", ConnectorService.class);
      if (service == null) {
        service = new ConnectorService();
      }
      ConnectorService.Connection conn = ccService.findIdentifiable(service.getConnection(), name);
      service.getConnection().remove(conn);
      service.getConnection().add(connection);
      ccService.saveCustomCacheElement("cluster", service);
      persisted = true;
    }

    CommandResult commandResult = ResultBuilder.buildResult(results, EXPERIMENTAL, null);
    commandResult.setCommandPersisted(persisted);
    return commandResult;
  }
}
