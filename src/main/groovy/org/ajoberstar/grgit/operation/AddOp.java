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
package org.ajoberstar.grgit.operation;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Callable;

import org.ajoberstar.grgit.Repository;
import org.ajoberstar.grgit.exception.GrgitException;
import org.eclipse.jgit.api.AddCommand;
import org.eclipse.jgit.api.errors.GitAPIException;

/**
 * Adds files to the index.
 *
 * <p>
 *   To add specific files or directories to the path. Wildcards are not
 *   supported.
 * </p>
 *
 * <pre>
 * grgit.add(patterns: ['1.txt', 'some/dir'])
 * </pre>
 *
 * <p>To add changes to all currently tracked files.</p>
 *
 * <pre>
 * grgit.add(update: true)
 * </pre>
 *
 * See <a href="http://git-scm.com/docs/git-add">git-add Manual Page</a>.
 *
 * @since 0.1.0
 * @see <a href="http://git-scm.com/docs/git-add">git-add Manual Page</a>
 */
public class AddOp implements Callable<Void> {
  private final Repository repo;
  private Set<String> patterns = new HashSet<>();
  private boolean update = false;

  public AddOp(Repository repo) {
    this.repo = repo;
  }

  public Void call() {
    AddCommand cmd = repo.getJgit().add();
    patterns.forEach(cmd::addFilepattern);
    cmd.setUpdate(update);
    try {
      cmd.call();
      return null;
    } catch (GitAPIException e) {
      throw new GrgitException("Problem adding changes to index.", e);
    }
  }
  
  /**
   * Patterns of files to add to the index.
   * @return patterns
   */
  public Set<String> getPatterns() {
	  return patterns;
  }
  
  public void setPatterns(Set<String> patterns) {
	  this.patterns = patterns;
  }
  
  /**
   * {@code true} if changes to all currently tracked files should be added
   * to the index, {@code false} otherwise.
   * @return whether to update currently tracked files
   */
  public boolean isUpdate() {
	  return update;
  }
  
  public void setUpdate(boolean update) {
	  this.update = update;
  }
}
