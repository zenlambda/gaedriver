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
import java.util.ArrayList;
import java.util.List;

/**
 * {@code DevAppserverThread} runs devappserver in a separate thread.
 *
 * @author schuppe@google.com (Robert Schuppenies)
 */
public class DevAppserverThread extends ClientThreadBase {

  /* Maximum time in milliseconds to wait for devappserver to launch. */
  static final long MAX_TIMEOUT = 60000L;

  /* Time to sleep between checks if devappserver has started the app. */
  static final long SLEEP_TIME = 500L;

  /* The hostname where the app will be listening. */
  String host;

  /* The port where the app will be listening. */
  int port;

  /**
   * Creates a new {@code DevAppserverThread} instance.
   *
   * @param config the gaedriver configuration to use
   * @param options a list of options passed to appcfg
   * @throws InvalidConfigException if the passed configuration is invalid.
   */
  public DevAppserverThread(Config config, List<String> options) throws InvalidConfigException {
    super(config, options);
    init(config);
  }

  /**
   * Creates a new {@code DevAppserverThread} instance.
   *
   * @param config the gaedriver configuration to use
   * @throws InvalidConfigException if the passed configuration is invalid.
   */
  public DevAppserverThread(Config config) throws InvalidConfigException {
    super(config, new ArrayList<String>());
    init(config);

  }

  /**
   * Initializes this instance.
   *
   * @param config the gaedriver configuration to use
   */
  void init(Config config) throws InvalidConfigException {
    int portSep = config.getAppHostname().indexOf(":");
    if (portSep == -1) {
      this.host = config.getAppHostname();
      this.port = Config.DEFAULT_DEVAPPSERVER_PORT;
    } else {
      this.host = config.getAppHostname().substring(0, portSep);
      String portString =
          config.getAppHostname().substring(portSep + 1, config.getAppHostname().length());
      try {
        this.port = Integer.valueOf(portString);
      } catch (NumberFormatException e) {
        throw new InvalidConfigException("\"" + portString + "\" is not a valid port.");
      }
    }
  }

  @Override
  protected List<String> buildArgumentList() {

    String sep = File.separator;
    String javaBinary = System.getProperty("java.home") + sep + "bin" + sep + "java";
    String toolsJar = config.getSdkDir() + sep + "lib" + sep + "appengine-tools-api.jar";

    List<String> command = new ArrayList<String>();
    command.add(javaBinary);
    command.add("-ea");
    command.add("-cp");
    command.add(toolsJar);
    command.add("com.google.appengine.tools.KickStart");
    command.add("com.google.appengine.tools.development.DevAppServerMain");
    command.add("--sdk_root=" + config.getSdkDir());
    command.add("--address=" + host);
    command.add("--port=" + port);
    command.addAll(options);
    command.add(config.getAppDir());
    return command;
  }

  @Override
  public void start() throws ClientException {
    super.start();
    String lineSep = System.getProperty("line.separator");
    long timeWaited = 0;
    while (timeWaited < MAX_TIMEOUT) {
      if (this.stdout.indexOf("The server is running at") >= 0) {
        break;
      }
      // Check if the process has ended with an unexpected return code;
      try {
        if (this.process.exitValue() != 0) {
          String msg = String.format("Could not start DevAppServer:%s%s%s%s",
              this.stdout, lineSep, this.stderr, lineSep);
          throw new ClientException(msg);
        }
      } catch (IllegalThreadStateException e) {
        // Ignore exception if the process has not finished, yet.
      }
      try {
        Thread.sleep(SLEEP_TIME);
      } catch (InterruptedException e) {
        throw new ClientException("Oh, oh! Waiting for subprocess thread failed.");
      }
      if (timeWaited == MAX_TIMEOUT) {
        String msg = "Failed to launch DevAppServer after " + MAX_TIMEOUT + "ms";
        throw new ClientException(msg);
      }
      timeWaited += SLEEP_TIME;
    }
  }

}
