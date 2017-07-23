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
package org.ajoberstar.grgit.auth;

import java.awt.GraphicsEnvironment;
import java.util.Optional;
import java.util.function.Predicate;

import org.ajoberstar.grgit.Credentials;
import org.eclipse.jgit.api.TransportCommand;
import org.eclipse.jgit.awtui.AwtCredentialsProvider;
import org.eclipse.jgit.transport.CredentialsProvider;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility class that allows a JGit {@code TransportCommand} to be configured to use additional
 * authentication options.
 */
public final class TransportOpUtil {
  private static final Logger logger = LoggerFactory.getLogger(TransportOpUtil.class);

  private TransportOpUtil() {
    throw new AssertionError("This class cannot be instantiated.");
  }

  /**
   * Configures the given transport command with the given credentials.
   *
   * @param cmd the command to configure
   * @param creds the hardcoded credentials to use, if not {@code null}
   */
  public static void configure(TransportCommand<?, ?> cmd, Credentials creds) {
    AuthConfig config = AuthConfig.fromSystem();
    logger.info(
        "The following authentication options are allowed (though they may not be available): {}",
        config.getAllowed());
    cmd.setCredentialsProvider(determineCredentialsProvider(config, creds));
    cmd.setTransportConfigCallback(new JschAgentProxyConfigCallback(config));
  }

  private static CredentialsProvider determineCredentialsProvider(
      AuthConfig config, Credentials creds) {
    Predicate<Credentials> populated =
        c -> Optional.ofNullable(c).map(Credentials::isPopulated).orElse(false);
    Credentials systemCreds = config.getHardcodedCreds();

    if (config.allows(AuthConfig.Option.HARDCODED) && populated.test(creds)) {
      logger.info("using hardcoded credentials provided programmatically");
      return new UsernamePasswordCredentialsProvider(creds.getUsername(), creds.getPassword());
    } else if (config.allows(AuthConfig.Option.HARDCODED) && populated.test(systemCreds)) {
      logger.info("using hardcoded credentials from system properties");
      return new UsernamePasswordCredentialsProvider(
          systemCreds.getUsername(), systemCreds.getPassword());
    } else if (config.allows(AuthConfig.Option.INTERACTIVE) && !GraphicsEnvironment.isHeadless()) {
      logger.info("using interactive credentials, if needed");
      return new AwtCredentialsProvider();
    } else {
      return null;
    }
  }
}
