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

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

import org.ajoberstar.grgit.Repository;
import org.ajoberstar.grgit.exception.GrgitException;
import org.ajoberstar.grgit.service.ResolveService;
import org.eclipse.jgit.api.DeleteBranchCommand;
import org.eclipse.jgit.api.errors.GitAPIException;

/**
 * Removes one or more branches from the repository. Returns a list of
 * the fully qualified branch names that were removed.
 *
 * <p>Remove branches that have been merged.</p>
 *
 * <pre>
 * def removedBranches = grgit.branch.remove(names: ['the-branch'])
 * def removedBranches = grgit.branch.remove(names: ['the-branch', 'other-branch'], force: false)
 * </pre>
 *
 * <p>Remove branches, even if they haven't been merged.</p>
 *
 * <pre>
 * def removedBranches = grgit.branch.remove(names: ['the-branch'], force: true)
 * </pre>
 *
 * See <a href="http://git-scm.com/docs/git-branch">git-branch Manual Page</a>.
 *
 * @since 0.2.0
 * @see <a href="http://git-scm.com/docs/git-branch">git-branch Manual Page</a>
 */
public class BranchRemoveOp implements Callable<List<String>> {
  private final Repository repo;
  private List<?> names = new ArrayList<>();
  private boolean force = false;

  public BranchRemoveOp(Repository repo) {
    this.repo = repo;
  }

  public List<String> call() {
    DeleteBranchCommand cmd = repo.getJgit().branchDelete();
    cmd.setBranchNames(names.stream().map(x -> new ResolveService(repo).toBranchName(x)).toArray(size -> new String[size]));
    cmd.setForce(force);

    try {
      return cmd.call();
    } catch (GitAPIException e) {
      throw new GrgitException("Problem deleting branch(es).", e);
    }
  }

  /**
   * List of all branche names to remove.
   * @see {@link ResolveService#toBranchName(Object)}
   */
public List<?> getNames() {
	return names;
}

public void setNames(List<?> names) {
	this.names = names;
}

/**
 * If {@code false} (the default), only remove branches that
 * are merged into another branch. If {@code true} will delete
 * regardless.
 */
public boolean isForce() {
	return force;
}

public void setForce(boolean force) {
	this.force = force;
}
}
