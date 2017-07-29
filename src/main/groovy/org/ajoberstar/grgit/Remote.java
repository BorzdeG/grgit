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

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Singular;
import lombok.ToString;
import lombok.Value;

/**
 * Remote repository.
 * @since 0.4.0
 */
@AllArgsConstructor
@Builder
@ToString(includeFieldNames=true)
@Value
public class Remote {
  /**
   * Name of the remote.
   */
	String name;

  /**
   * URL to fetch from.
   */
	String url;

  /**
   * URL to push to.
   */
	String pushUrl;

  /**
   * Specs to fetch from the remote.
   */
	@Singular
	List<String> fetchRefSpecs;

  /**
   * Specs to push to the remote.
   */
	@Singular
			List<String> pushRefSpecs;

  /**
   * Whether or not pushes will mirror the repository.
   */
					boolean mirror;
}
