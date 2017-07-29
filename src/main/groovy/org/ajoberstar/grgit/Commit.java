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

import java.util.Date;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Singular;
import lombok.ToString;
import lombok.Value;

/**
 * A commit.
 * @since 0.1.0
 */
@AllArgsConstructor
@Builder
@ToString(includeFieldNames=true)
@Value
public class Commit {
  /**
   * The full hash of the commit.
   */
  String id;

  /**
   * Hashes of any parent commits.
   */
  @Singular
  List<String> parentIds;

  /**
   * The author of the changes in the commit.
   */
  Person author;

  /**
   * The committer of the changes in the commit.
   */
  Person committer;

  /**
   * The time the commit was created in seconds since "the epoch".
   */
  int time;

  /**
   * The full commit message.
   */
  String fullMessage;

  /**
   * The shortened commit message.
   */
  String shortMessage;

  /**
   * The time the commit was created.
   * @return the date
   */
  public Date getDate() {
    long seconds = Integer.valueOf(time).longValue();
    return new Date(seconds * 1000);
  }
  
  /**
   * The first {@code length} characters of the commit hash.
   * @param length the number of characters to abbreviate the
   * hash to (defaults to 7)
   */
  public String getAbbreviatedId() {
	  return getAbbreviatedId(7);
  }

  /**
   * The first {@code length} characters of the commit hash.
   * @param length the number of characters to abbreviate the
   * hash to (defaults to 7)
   */
  public String getAbbreviatedId(int length) {
	  return id.substring(0, length);
  }
}
