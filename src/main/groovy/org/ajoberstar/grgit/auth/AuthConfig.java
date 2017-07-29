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

import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.ajoberstar.grgit.Credentials;
import org.ajoberstar.grgit.exception.GrgitException;

/**
 * Stores configuration options for how to authenticate with remote repositories.
 *
 * <p>The following system properties can be set to configure how authentication is performed with
 * remote repositories. All "allow" properties default to {@code true}.
 *
 * <ul>
 *   <li>{@code org.ajoberstar.grgit.auth.force={hardcoded|interactive|sshagent|pageant}}
 *   <li>{@code org.ajoberstar.grgit.auth.hardcoded.allow={true|false}}
 *   <li>{@code org.ajoberstar.grgit.auth.interactive.allow={true|false}}
 *   <li>{@code org.ajoberstar.grgit.auth.sshagent.allow={true|false}}
 *   <li>{@code org.ajoberstar.grgit.auth.pageant.allow={true|false}}
 * </ul>
 *
 * <p>In order to set default hardocded credentials, use the following properties. Note that unless
 * hardcoded credentials are disabled, using these properties will supersede the use of interactive
 * creds, ssh-agent, or Pageant. However, they will not take precedence over credentials provided
 * directly to a repository during the clone, init, or open.
 *
 * <ul>
 *   <li>{@code org.ajoberstar.grgit.auth.username=<username>}
 *   <li>{@code org.ajoberstar.grgit.auth.password=<password>}
 * </ul>
 *
 * <p>Hardcoded credentials can alternately be provided with environment variables. These take a
 * lower precedence than the system properties, but all other considerations are the same.
 *
 * <ul>
 *   <li>{@code GRGIT_USER=<username>}
 *   <li>{@code GRGIT_PASS=<password>}
 * </ul>
 *
 * <p>To customize SSH credentials use the following properties. In order to add a non-standard SSH
 * key to use as your credentials, use the first following property. In case your private key is
 * protected by a passphrase, use the second property.
 *
 * <ul>
 *   <li>{@code org.ajoberstar.grgit.auth.ssh.private=<path.to.private.key>}
 *   <li>{@code org.ajoberstar.grgit.auth.ssh.passphrase=<passphrase>}
 * </ul>
 *
 * <p>In order to customize the JSch session config use a property of the following format (possible
 * values <a
 * href="http://epaul.github.io/jsch-documentation/javadoc/com/jcraft/jsch/JSch.html#setConfig(java.lang.String,
 * java.lang.String)">JSch documentation</a>):
 *
 * <ul>
 *   <li>{@code org.ajoberstar.grgit.auth.session.config.<key>=<value>}
 * </ul>
 *
 * <p>The following order is used to determine which authentication option is used.
 *
 * <ol>
 *   <li>Hardcoded credentials, if provided.
 *   <li>Ssh-Agent, if available.
 *   <li>Pageant, if available.
 *   <li>Interactive credentials, if needed.
 * </ol>
 *
 * @since 0.2.0
 */
class AuthConfig {
  /** System property name used to force a specific authentication option. */
  public static final String FORCE_OPTION = "org.ajoberstar.grgit.auth.force";

  public static final String USERNAME_OPTION = "org.ajoberstar.grgit.auth.username";
  public static final String PASSWORD_OPTION = "org.ajoberstar.grgit.auth.password";
  public static final String SSH_PRIVATE_KEY_OPTION = "org.ajoberstar.grgit.auth.ssh.private";
  public static final String SSH_PASSPHRASE_OPTION = "org.ajoberstar.grgit.auth.ssh.passphrase";
  public static final String SSH_SESSION_CONFIG_OPTION_PREFIX =
      "org.ajoberstar.grgit.auth.session.config.";

  static final String USERNAME_ENV_VAR = "GRGIT_USER";
  static final String PASSWORD_ENV_VAR = "GRGIT_PASS";

  private final Map<String, String> props;
  private final Map<String, String> env;

  private AuthConfig(Map<String, String> props, Map<String, String> env) {
    this.props = props;
    this.env = env;
  }

  /** Set of all authentication options that are allowed in this configuration. */
  public Set<Option> getAllowed() {
    String forceSetting = props.get(FORCE_OPTION);
    if (forceSetting != null) {
      try {
        return Stream.of(Option.valueOf(forceSetting.toUpperCase())).collect(Collectors.toSet());
      } catch (IllegalArgumentException e) {
        throw new GrgitException(
            "${FORCE_OPTION} must be set to one of ${Option.values() as List}. Currently set to: ${forceSetting}",
            e);
      }
    } else {
      return Arrays.stream(Option.values())
          .filter(
              opt -> {
                String setting = props.getOrDefault(opt.getSystemPropertyName(), "true");
                return Boolean.valueOf(setting);
              })
          .collect(Collectors.toSet());
    }
  }

  /**
   * Test whether the given authentication option is allowed by this configuration.
   *
   * @param option the authentication option to test for
   * @return {@code true} if the given option is allowed, {@code false} otherwise
   */
  public boolean allows(Option option) {
    return getAllowed().contains(option);
  }

  /**
   * Constructs and returns a {@link Credentials} instance reflecting the settings in the system
   * properties.
   *
   * @return a credentials instance reflecting the settings in the system properties, or, if the
   *     username isn"t set, {@code null}
   */
  public Credentials getHardcodedCreds() {
    if (allows(Option.HARDCODED)) {
      String username = props.getOrDefault(USERNAME_OPTION, env.get(USERNAME_ENV_VAR));
      String password = props.getOrDefault(PASSWORD_OPTION, env.get(PASSWORD_ENV_VAR));
      return new Credentials(username, password);
    } else {
      return null;
    }
  }

  /**
   * Gets the path to your SSH private key to use during authentication reflecting the value set in
   * the system properties.
   *
   * @return the path to the SSH key, if set, otherwise {@code null}
   */
  public String getSshPrivateKeyPath() {
    return props.get(SSH_PRIVATE_KEY_OPTION);
  }

  /**
   * Gets the passphrase for your SSH private key to use during authentication reflecting the value
   * set in the system properties.
   *
   * @return the passphrase of the SSH key, if set, otherwise {@code null}
   */
  public String getSshPassphrase() {
    return props.get(SSH_PASSPHRASE_OPTION);
  }

  /**
   * Gets session config override for SSH session that is used underneath by JGit
   *
   * @return map with configuration or empty if nothing was specified in system property
   */
  public Map<String, String> getSessionConfig() {
    return props
        .entrySet()
        .stream()
        .filter(entry -> entry.getKey().startsWith(SSH_SESSION_CONFIG_OPTION_PREFIX))
        .collect(
            Collectors.toMap(
                entry -> entry.getKey().substring(SSH_SESSION_CONFIG_OPTION_PREFIX.length()),
                Map.Entry::getValue));
  }

  /**
   * Factory method to construct an authentication configuration from the given properties and
   * environment.
   *
   * @param properties the properties to use in this configuration
   * @param env the environment vars to use in this configuration
   * @return the constructed configuration
   * @throws GrgitException if force is set to an invalid option
   */
  public static AuthConfig fromMap(Map<String, String> props) {
    return new AuthConfig(props, Collections.emptyMap());
  }

  /**
   * Factory method to construct an authentication configuration from the given properties and
   * environment.
   *
   * @param properties the properties to use in this configuration
   * @param env the environment vars to use in this configuration
   * @return the constructed configuration
   * @throws GrgitException if force is set to an invalid option
   */
  public static AuthConfig fromMap(Map<String, String> props, Map<String, String> env) {
    return new AuthConfig(props, env);
  }

  /**
   * Factory method to construct an authentication configuration from the current system properties
   * and environment variables.
   *
   * @return the constructed configuration
   * @throws GrgitException if force is set to an invalid option
   */
  public static AuthConfig fromSystem() {
    Map<String, String> sysProps =
        System.getProperties()
            .entrySet()
            .stream()
            .collect(
                Collectors.toMap(
                    entry -> entry.getKey().toString(), entry -> entry.getValue().toString()));
    return fromMap(sysProps, System.getenv());
  }

  /** Available authentication options. */
  public static enum Option {
    /** Use credentials provided directly to Grgit. */
    HARDCODED,

    /** Will prompt for credentials using an AWT window, if needed. */
    INTERACTIVE,

    /** Use SSH keys in the system's sshagent process. */
    SSHAGENT,

    /** Use SSH keys in the system's pageant process. */
    PAGEANT;

    /**
     * Gets the system property name used to configure whether this option is allowed or not. By
     * default, all are allowed. The system properties are of the form {@code
     * org.ajoberstar.grgit.auth.<lowercase option name>.allow} Can be set to {@code true} or {@code
     * false}.
     *
     * @return the system property name
     */
    String getSystemPropertyName() {
      return String.format("org.ajoberstar.grgit.auth.%s.allow", name().toLowerCase());
    }
  }
}
