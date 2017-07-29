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

/**
 * Status of the current working tree and index.
 */
@AllArgsConstructor
@Builder
@ToString(includeFieldNames=true)
@Value
public class Status {
  Changes staged;
  Changes unstaged;
  @Singular
  Set<String> conflicts;

  @AllArgsConstructor
  @Builder
  @ToString(includeFieldNames=true)
  @Value
  public static class Changes {
	  @Singular("added")
    Set<String> added;
	  @Singular("modified")
    private     final Set<String> modified;
	  @Singular("removed")
    Set<String> removed;


    /**
     * Gets all changed files.
     * @return all changed files
     */
    public Set<String> getAllChanges() {
    	return Stream.of(added, modified, removed)
    		.flatMap(Set::stream)
    		.collect(Collectors.toSet());
    }
  }

  /**
   * Whether the repository has any changes or conflicts.
   * @return {@code true} if there are no changes either staged or unstaged or
   * any conflicts, {@code false} otherwise
   */
  public boolean isClean() {
	  return staged.getAllChanges().isEmpty() && unstaged.getAllChanges().isEmpty() && conflicts.isEmpty();
  }
}
