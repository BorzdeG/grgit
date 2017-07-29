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

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Singular;
import lombok.ToString;
import lombok.Value;

@AllArgsConstructor
@Builder
@ToString(includeFieldNames=true)
@Value
public class CommitDiff {
  Commit commit;

  @Singular("added")
  Set<String> added;

  @Singular("copied")
  Set<String> copied;

  @Singular("modified")
  Set<String> modified;

  @Singular("removed")
  Set<String> removed;

  @Singular("renamed")
  Set<String> renamed;

  /**
   * Gets all changed files.
   * @return all changed files
   */
  public Set<String> getAllChanges() {
	  return Stream.of(added, copied, modified, removed, renamed)
	  	.flatMap(Set::stream)
	  	.collect(Collectors.toSet());
  }
}
