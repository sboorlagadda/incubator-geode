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
package org.apache.geode.internal.cache;

import java.util.Arrays;

import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;

import org.apache.geode.cache.CacheListener;
import org.apache.geode.cache.DataPolicy;
import org.apache.geode.cache.InterestPolicy;
import org.apache.geode.cache.PartitionAttributesFactory;
import org.apache.geode.cache.Region;
import org.apache.geode.cache.RegionFactory;
import org.apache.geode.cache.SubscriptionAttributes;
import org.apache.geode.test.junit.categories.DistributedTest;
import org.apache.geode.test.junit.runners.CategoryWithParameterizedRunnerFactory;

/**
 * This class tests event triggering and handling in partitioned regions.
 *
 * <p>
 * Converted from JUnit 3.
 *
 * @since GemFire 5.1
 */
@Category(DistributedTest.class)
@RunWith(Parameterized.class)
@Parameterized.UseParametersRunnerFactory(CategoryWithParameterizedRunnerFactory.class)
@SuppressWarnings("serial")
public class PRCacheListenerWithInterestPolicyAllInvocationTest
    extends ReplicateCacheListenerInvocationTest {

  @Parameters(name = "{index}: redundancy={0}")
  public static Iterable<Integer> data() {
    return Arrays.asList(0, 3);
  }

  @Parameter
  public int redundancy;

  @Override
  protected Region<String, Integer> createRegion(final String name,
      final CacheListener<String, Integer> listener) {
    PartitionAttributesFactory partitionFactory = new PartitionAttributesFactory();
    partitionFactory.setRedundantCopies(redundancy);

    RegionFactory<String, Integer> regionFactory = cacheRule.getCache().createRegionFactory();
    regionFactory.addCacheListener(listener);
    regionFactory.setDataPolicy(DataPolicy.PARTITION);
    regionFactory.setPartitionAttributes(partitionFactory.create());
    regionFactory.setSubscriptionAttributes(new SubscriptionAttributes(InterestPolicy.ALL));

    return regionFactory.create(name);
  }
}