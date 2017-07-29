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

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.Callable;

import org.ajoberstar.grgit.Repository;
import org.ajoberstar.grgit.exception.GrgitException;
import org.ajoberstar.grgit.util.CoercionUtil;
import org.eclipse.jgit.api.ApplyCommand;
import org.eclipse.jgit.api.errors.GitAPIException;

/**
 * Apply a patch to the index.
 *
 * <p>To apply a patch to the index.</p>
 *
 * <pre>
 * grgit.apply(path: patchFile)
 * </pre>
 *
 * See <a href="http://git-scm.com/docs/git-apply">git-apply Manual Page</a>.
 *
 * @since 0.1.0
 * @see <a href="http://git-scm.com/docs/git-apply">git-apply Manual Page</a>
 */
public class ApplyOp implements Callable<Void> {
  private final Repository repo;
  private Object patch;

  public ApplyOp(Repository repo) {
    this.repo = repo;
  }

  public Void call() {
    ApplyCommand cmd = repo.getJgit().apply();
    if (patch == null) {
      throw new IllegalStateException("Must set a patch file.");
    }
    Path patchFile = CoercionUtil.toFile(patch).toPath();
    try (InputStream patchStream = Files.newInputStream(patchFile)) {
    	cmd.setPatch(patchStream);
    	cmd.call();
    	return null;
  } catch (GitAPIException e) {
    throw new GrgitException("Problem applying patch.", e);
  } catch (IOException e) {
	throw new UncheckedIOException(e);
}
    }

  /**
   * The patch file to apply to the index.
   * @see {@link CoercionUtil#toFile(Object)}
   */
public Object getPatch() {
	return patch;
}

public void setPatch(Object patch) {
	this.patch = patch;
}
  }
