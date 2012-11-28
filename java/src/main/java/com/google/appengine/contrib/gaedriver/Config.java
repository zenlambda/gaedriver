/*
 * Copyright (C) 2012 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package com.google.appengine.contrib.gaedriver;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

import com.google.common.base.Preconditions;

/**
 * {@code Config} is a container for all gaedriver-related configuration.
 *
 * @author schuppe@google.com (Robert Schuppenies)
 */
public class Config {

  /*
   * Name of the default file searched for configuration settings. It is assumed to be located in
   * the user's home directory.
   */
  public static final String DEFAULT_CONFIG_FILE = ".gaedriver.properties";

  /* The admin-console hostname. */
  private String acHostname;

  /* The identifier of the application to use (may include domain, partition or other prefixes). */
  private String appId;

  /* The app ID without any prefix. */
  private String displayAppId;

  /* The domain where the application is run. */
  private String domain;

  /* The partition where the app is hosted (for example "s~"). */
  private String partition;

  /* The full hostname of the app (eg "appid.appspot.com"). */
  private String appHostname;

  /* The backend identifier. */
  private String backendId = "";

  /* The number of available backend instances. */
  private int backendInstances = 0;

  /*
   * The hostname of the cluster where the app is hosted. Can also be "localhost" which indicates to
   * use devappserver.
   */
  private String clusterHostname;

  /* The local directory where the SDK can be found. */
  private String sdkDir;

  /* The local directory where the application can be found. */
  private String appDir;

  /* The username used for authentication (email address). */
  private String username;

  /* The password used for authentication. */
  private String password;

  /* The default devappserver port if no other port can be used. */
  static int DEFAULT_DEVAPPSERVER_PORT = 8080;

  /* A placeholder string assigned to acHostname when using devappserver. */
  public final static String NO_AC_ON_SDK = "AC_NOT_AVAILABLE_ON_DEVAPPSERVER";

  Utils utils = new Utils();

  /**
   * Initializes this configuration object.
   *
   * @param appId identifier of the application to use
   * @param backendId backend identifier
   * @param backendInstances number of available backend instances
   * @param clusterHostname hostname of the cluster where the app is hosted
   * @param sdkDir local directory where the SDK can be found
   * @param appDir local directory where the application can be found
   * @param acHostname admin-console hostname
   * @param username username used for authentication (email address)
   * @param password password used for authentication
   */
  public void init(String appId,
      String backendId,
      int backendInstances,
      String clusterHostname,
      String sdkDir,
      String appDir,
      String acHostname,
      String username,
      String password) {
    Preconditions.checkNotNull(appId);
    Preconditions.checkNotNull(backendId);
    Preconditions.checkNotNull(backendInstances);
    Preconditions.checkNotNull(clusterHostname);
    Preconditions.checkNotNull(sdkDir);
    Preconditions.checkNotNull(appDir);
    Preconditions.checkNotNull(acHostname);
    Preconditions.checkNotNull(username);
    Preconditions.checkNotNull(password);

    this.parseAppId(appId);
    this.backendId = backendId;
    this.backendInstances = backendInstances;

    if (clusterHostname.length() > 0) {
      this.clusterHostname = clusterHostname;
    } else {
      int port = utils.pickUnusedPort();
      if (port == -1) {
        port = DEFAULT_DEVAPPSERVER_PORT;
      }
      this.clusterHostname = "localhost:" + port;
    }
    this.acHostname = acHostname;
    if (utils.isClusterAppserver(this.clusterHostname)) {
      // If a backend is specified, point 'app_hostname' to this backend.
      if (this.backendId.length() != 0) {
        this.appHostname =
            String.format("%s.%s.%s", this.backendId, this.displayAppId, this.clusterHostname);
      } else {
        this.appHostname = String.format("%s.%s", this.displayAppId, this.clusterHostname);
      }
    } else {
      // We assume a cluster hostname that does not identify an
      // appserver to be an dev_appserver instance. This means the
      // application hostname is the given clustername and no admin
      // console is available.
      this.appHostname = this.clusterHostname;
      this.acHostname = Config.NO_AC_ON_SDK;
    }
    this.sdkDir = sdkDir;
    this.appDir = appDir;
    this.username = username;
    this.password = password;
  }

  /**
   * Parses the full application ID and sets partition, domain and display application ID.
   *
   * @param appId identifier of the application to use
   */
  void parseAppId(String appId) {
    if (appId.length() == 0) {
      this.appId = "";
      this.partition = "";
      this.domain = "";
      this.displayAppId = "";
      return;
    }
    this.appId = appId;
    this.displayAppId = new String(appId);
    if (this.displayAppId.indexOf("~") >= 0) {
      String[] subStrings = this.displayAppId.split("~");
      this.partition = subStrings[0];
      this.displayAppId = subStrings[1];
    }
    if (this.displayAppId.indexOf(":") >= 0) {
      String[] subStrings = this.displayAppId.split(":");
      this.domain = subStrings[0];
      this.displayAppId = subStrings[1];
    }
  }

  /**
   * Creates a new configuration object.
   *
   * @param clusterHostname hostname of the cluster where the app is hosted
   * @param sdkDir local directory where the SDK can be found
   * @param appDir local directory where the application can be found
   */
  public Config(String clusterHostname, String sdkDir, String appDir) {
    init("", "", 0, clusterHostname, sdkDir, appDir, "", "", "");
  }

  /**
   * Creates a new configuration object.
   *
   * @param appId identifier of the application to use
   * @param backendId backend identifier
   * @param backendInstances number of available backend instances
   * @param clusterHostname hostname of the cluster where the app is hosted
   * @param sdkDir local directory where the SDK can be found
   * @param appDir local directory where the application can be found
   * @param acHostname admin-console hostname
   * @param username username used for authentication (email address)
   * @param password password used for authentication
   */
  public Config(String appId,
      String backendId,
      int backendInstances,
      String clusterHostname,
      String sdkDir,
      String appDir,
      String acHostname,
      String username,
      String password) {
    init(appId,
        backendId,
        backendInstances,
        clusterHostname,
        sdkDir,
        appDir,
        acHostname,
        username,
        password);
  }

  /**
   * Creates a configuration instance from a {@code Properties} object.
   *
   * @param properties properties container for relevant information
   * @return a configuration object.
   * @throws InvalidConfigException if data in properties is invalid.
   */
  protected static Config loadFromProperties(Properties properties) throws InvalidConfigException {
    String appId = properties.getProperty("appId", "");
    String backendId = properties.getProperty("backendId", "");
    String backendInstancesStr = properties.getProperty("backendInstances", "");
    int backendInstances;
    try {
      if (backendInstancesStr.length() == 0) {
        backendInstances = 0;
      } else {
        backendInstances = Integer.valueOf(backendInstancesStr);
      }
    } catch (NumberFormatException e) {
      throw new InvalidConfigException("\"" + backendInstancesStr + "\" is not a valid "
          + "backendInstances value. Expected an integer.");
    }
    String clusterHostname = properties.getProperty("clusterHostname", "");
    String sdkDir = properties.getProperty("sdkDir", "");
    String appDir = properties.getProperty("appDir", "");
    String acHostname = properties.getProperty("acHostname", "");
    String username = properties.getProperty("username", "");
    String password = properties.getProperty("password", "");

    Config config = new Config(appId,
        backendId,
        backendInstances,
        clusterHostname,
        sdkDir,
        appDir,
        acHostname,
        username,
        password);
    return config;
  }

  /**
   * Load Config object from a properties file.
   *
   * @param filePath path to the properties file
   * @return a configuration object.
   * @throws FileNotFoundException if file could not be found
   * @throws IOException if file could not be read
   * @throws InvalidConfigException if data in properties is invalid.
   */
  public static Config loadFromFile(String filePath)
      throws FileNotFoundException, IOException, InvalidConfigException {
    Properties properties = new Properties();
    properties.load(new FileInputStream(filePath));
    return loadFromProperties(properties);
  }

  /**
   * Load Config object from the default properties file.
   *
   * @return a configuration object.
   * @throws FileNotFoundException if file could not be found
   * @throws IOException if file could not be read
   * @throws InvalidConfigException if data in properties is invalid.
   */
  public static Config loadFromFile()
      throws FileNotFoundException, IOException, InvalidConfigException {
    String filePath = System.getProperty("user.home") + File.separator + DEFAULT_CONFIG_FILE;
    return loadFromFile(filePath);
  }

  /**
   * @return the admin-console hostname
   */
  public String getAcHostname() {
    return acHostname;
  }

  /**
   * @return the full application ID
   */
  public String getAppId() {
    return appId;
  }

  /**
   * @return the display application ID
   */
  public String getDisplayAppId() {
    return displayAppId;
  }

  /**
   * @return the application hostname
   */
  public String getAppHostname() {
    return appHostname;
  }

  /**
   * @return the backend Id
   */
  public String getBackendId() {
    return backendId;
  }

  /**
   * @return the number of available backend instances
   */
  public int getBackendInstances() {
    return backendInstances;
  }

  /**
   * @return the cluster hostname
   */
  public String getClusterHostname() {
    return clusterHostname;
  }

  /**
   * @return the domain of the app
   */
  public String getDomain() {
    return domain;
  }

  /**
   * @return the partition of the app
   */
  public String getPartition() {
    return partition;
  }

  /**
   * @return the local sdk directory used
   */
  public String getSdkDir() {
    return sdkDir;
  }

  /**
   * @param sdkDir the local sdk directory to set
   */
  public void setSdkDir(String sdkDir) {
    this.sdkDir = sdkDir;
  }

  /**
   * @return the local application directory
   */
  public String getAppDir() {
    return appDir;
  }

  /**
   * @param appDir the local application directory to set
   */
  public void setAppDir(String appDir) {
    this.appDir = appDir;
  }

  /**
   * @return the username
   */
  public String getUsername() {
    return username;
  }

  /**
   * @return the password
   */
  public String getPassword() {
    return password;
  }

  /**
   * @param password the password to set
   */
  public void setPassword(String password) {
    this.password = password;
  }

  /**
   * @param username the username to set
   */
  public void setUsername(String username) {
    this.username = username;
  }

}
