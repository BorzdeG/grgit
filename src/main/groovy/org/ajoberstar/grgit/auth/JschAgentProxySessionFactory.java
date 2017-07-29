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

import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Stream;

import org.eclipse.jgit.transport.JschConfigSessionFactory;
import org.eclipse.jgit.transport.OpenSshConfig.Host;
import org.eclipse.jgit.util.FS;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jcraft.jsch.IdentityRepository;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.agentproxy.Connector;
import com.jcraft.jsch.agentproxy.RemoteIdentityRepository;
import com.jcraft.jsch.agentproxy.USocketFactory;
import com.jcraft.jsch.agentproxy.connector.PageantConnector;
import com.jcraft.jsch.agentproxy.connector.SSHAgentConnector;
import com.jcraft.jsch.agentproxy.usocket.JNAUSocketFactory;
import com.jcraft.jsch.agentproxy.usocket.NCUSocketFactory;

/**
 * A session factory that supports use of ssh-agent and Pageant SSH authentication.
 *
 * @since 0.1.0
 */
class JschAgentProxySessionFactory extends JschConfigSessionFactory {
  private static final Logger logger = LoggerFactory.getLogger(JschAgentProxySessionFactory.class);
  private final AuthConfig config;

  public JschAgentProxySessionFactory(AuthConfig config) {
    this.config = config;
  }

  /** Customize session */
  @Override
  protected void configure(Host hc, Session session) {
    config.getSessionConfig().forEach(session::setConfig);
  }

  /**
   * Obtains a JSch used for creating sessions, with the addition of ssh-agent and Pageant agents,
   * if available.
   *
   * @return the JSch instance
   */
  @Override
  protected JSch getJSch(Host hc, FS fs) throws JSchException {
    JSch jsch;
    try {
      jsch = super.getJSch(hc, fs);
    } catch (JSchException e) {
      jsch = super.createDefaultJSch(fs);
    }

    if (config.getSshPrivateKeyPath() != null) {
      if (config.getSshPassphrase() != null) {
        jsch.addIdentity(config.getSshPrivateKeyPath(), config.getSshPassphrase());
      } else {
        jsch.addIdentity(config.getSshPrivateKeyPath());
      }
    }

    Optional<Connector> con = determineConnector();
    if (con.isPresent()) {
      IdentityRepository remoteRepo = new RemoteIdentityRepository(con.get());
      if (remoteRepo.getIdentities().isEmpty()) {
        logger.info("not using agent proxy: no identities found");
      } else {
        logger.info("using agent proxy");
        jsch.setIdentityRepository(remoteRepo);
      }
    } else {
      logger.info("jsch agent proxy not available");
    }
    return jsch;
  }

  /**
   * Chooses which agent proxy connector is used.
   *
   * @return the connector available at this time
   */
  private Optional<Connector> determineConnector() {
    return Stream.<Supplier<Optional<Connector>>>of(this::sshAgentSelector, this::pageantSelector)
        .map(Supplier::get)
        .filter(Optional::isPresent)
        .map(Optional::get)
        .findFirst();
  }

  private Optional<Connector> sshAgentSelector() {
    try {
      if (!config.allows(AuthConfig.Option.SSHAGENT)) {
        logger.info("ssh-agent option disabled");
        return Optional.empty();
      } else if (SSHAgentConnector.isConnectorAvailable()) {
        Optional<USocketFactory> usf = determineUSocketFactory();
        if (usf.isPresent()) {
          logger.info("ssh-agent available");
          return Optional.of(new SSHAgentConnector(usf.get()));
        } else {
          logger.info("ssh-agent not available");
          return Optional.empty();
        }
      } else {
        logger.info("ssh-agent not available");
        return Optional.empty();
      }
    } catch (Throwable e) {
      logger.info("ssh-agent could not be configured: {}", e.getMessage());
      logger.debug("ssh-agent failure details.", e);
      return Optional.empty();
    }
  }

  private Optional<Connector> pageantSelector() {
    try {
      if (!config.allows(AuthConfig.Option.PAGEANT)) {
        logger.info("pageant option disabled");
        return Optional.empty();
      } else if (PageantConnector.isConnectorAvailable()) {
        logger.info("pageant available");
        return Optional.of(new PageantConnector());
      } else {
        logger.info("pageant not available");
        return Optional.empty();
      }
    } catch (Throwable e) {
      logger.info("ssh-agent could not be configured: {}", e.getMessage());
      logger.debug("ssh-agent failure details.", e);
      return Optional.empty();
    }
  }

  /**
   * Choose which socket factory to use.
   *
   * @return a working socket factory or {@code null} if none is available
   */
  private Optional<USocketFactory> determineUSocketFactory() {
    return Stream.<Supplier<Optional<USocketFactory>>>of(this::ncSelector, this::jnaSelector)
        .map(Supplier::get)
        .filter(Optional::isPresent)
        .map(Optional::get)
        .findFirst();
  }

  private Optional<USocketFactory> jnaSelector() {
    try {
      return Optional.of(new JNAUSocketFactory());
    } catch (Throwable e) {
      logger.info("JNA USocketFactory could not be configured: {}", e.getMessage());
      logger.debug("JNA USocketFactory failure details.", e);
      return Optional.empty();
    }
  }

  private Optional<USocketFactory> ncSelector() {
    try {
      return Optional.of(new NCUSocketFactory());
    } catch (Throwable e) {
      logger.info("NetCat USocketFactory could not be configured: {}", e.getMessage());
      logger.debug("NetCat USocketFactory failure details.", e);
      return Optional.empty();
    }
  }
}
