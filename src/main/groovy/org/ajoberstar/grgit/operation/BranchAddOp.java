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

import java.util.concurrent.Callable;

import org.ajoberstar.grgit.Branch;
import org.ajoberstar.grgit.Repository;
import org.ajoberstar.grgit.exception.GrgitException;
import org.ajoberstar.grgit.service.ResolveService;
import org.ajoberstar.grgit.util.JGitUtil;

import org.eclipse.jgit.api.CreateBranchCommand;
import org.eclipse.jgit.api.CreateBranchCommand.SetupUpstreamMode;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Ref;

/**
 * Adds a branch to the repository. Returns the newly created {@link Branch}.
 *
 * <p>To add a branch starting at the current HEAD.</p>
 *
 * <pre>
 * grgit.branch.add(name: 'new-branch')
 * </pre>
 *
 * <p>To add a branch starting at, but not tracking, a local start point.</p>
 *
 * <pre>
 * grgit.branch.add(name: 'new-branch', startPoint: 'local-branch')
 * grgit.branch.add(name: 'new-branch', startPoint: 'local-branch', mode: BranchAddOp.Mode.NO_TRACK)
 * </pre>
 *
 * <p>To add a branch starting at and tracking a local start point.</p>
 *
 * <pre>
 * grgit.branch.add(name: 'new-branch', startPoint: 'local-branch', mode: BranchAddOp.Mode.TRACK)
 * </pre>
 *
 * <p>To add a branch starting from and tracking a remote branch.</p>
 *
 * <pre>
 * grgit.branch.add(name: 'new-branch', startPoint: 'origin/remote-branch')
 * grgit.branch.add(name: 'new-branch', startPoint: 'origin/remote-branch', mode: BranchAddOp.Mode.TRACK)
 * </pre>
 *
 * <p>To add a branch starting from, but not tracking, a remote branch.</p>
 *
 * <pre>
 * grgit.branch.add(name: 'new-branch', startPoint: 'origin/remote-branch', mode: BranchAddOp.Mode.NO_TRACK)
 * </pre>
 *
 * See <a href="http://git-scm.com/docs/git-branch">git-branch Manual Page</a>.
 *
 * @since 0.2.0
 * @see <a href="http://git-scm.com/docs/git-branch">git-branch Manual Page</a>
 */
public class BranchAddOp implements Callable<Branch> {
  private final Repository repo;
  private String name;
  private Object startPoint;
  private Mode mode;

  public BranchAddOp(Repository repo) {
    this.repo = repo;
  }

  public Branch call() {
    if (mode != null && startPoint == null) {
      throw new IllegalStateException("Cannot set mode if no start point.");
    }
    CreateBranchCommand cmd = repo.getJgit().branchCreate();
    cmd.setName(name);
    cmd.setForce(false);
    if (startPoint != null) {
      String rev = new ResolveService(repo).toRevisionString(startPoint);
      cmd.setStartPoint(rev);
    }
    if (mode != null) {
    	cmd.setUpstreamMode(mode.jgit);
    }

    try {
      Ref ref = cmd.call();
      return JGitUtil.resolveBranch(repo, ref);
    } catch (GitAPIException e) {
      throw new GrgitException("Problem creating branch.", e);
    }
  }
  /**
   * The name of the branch to add.
   */
  public String getName() {
	return name;
}

public void setName(String name) {
	this.name = name;
}

/**
 * The commit the branch should start at. If this is a remote branch
 * it will be automatically tracked.
 * @see {@link ResolveService#toRevisionString(Object)}
 */
public Object getStartPoint() {
	return startPoint;
}

public void setStartPoint(Object startPoint) {
	this.startPoint = startPoint;
}

/**
 * The tracking mode to use. If {@code null}, will use the default
 * behavior.
 */
public Mode getMode() {
	return mode;
}

public void setMode(Mode mode) {
	this.mode = mode;
}

public static enum Mode {
    TRACK(SetupUpstreamMode.TRACK),
    NO_TRACK(SetupUpstreamMode.NOTRACK);

    private final SetupUpstreamMode jgit;

    Mode(SetupUpstreamMode jgit) {
      this.jgit = jgit;
    }
  }
}
