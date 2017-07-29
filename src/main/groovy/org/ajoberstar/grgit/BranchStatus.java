/*
 * Copyright 2012-2015 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.ajoberstar.grgit;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.ToString;
import lombok.Value;

/**
 * The tracking status of a branch.
 * @since 0.2.0
 */
@AllArgsConstructor
@Builder
@ToString(includeFieldNames=true)
@Value
public class BranchStatus {
  /**
   * The branch this object is for.
   */
  Branch branch;

  /**
   * The number of commits this branch is ahead of its upstream.
   */
  int aheadCount;

  /**
   * The number of commits this branch is behind its upstream.
   */
  int behindCount;
}
