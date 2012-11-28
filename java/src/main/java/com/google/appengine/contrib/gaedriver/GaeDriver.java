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

/**
 * Allows to drive App Engine application deployments and runs with devappserver.
 *
 * @author schuppe@google.com (Robert Schuppenies)
 */
public class GaeDriver {

  /* The configuration to use. */
  final Config config;

  /* The client thread object used. */
  ClientThreadBase thread;

  Utils utils = new Utils();

  /**
   * Creates a new {@code GaeDriver} instance.
   *
   * @param config the configuration to use
   */
  public GaeDriver(Config config) {
    this.config = config;
  }

  /**
   * Sets up an application. Depending on the configuration this will either start the application
   * with devappserver or deploy it.
   *
   * @throws InvalidConfigException if the passed configuration is invalid.
   * @throws ClientException if the client could not be run as expected
   * @throws InterruptedException if waiting for the client to finish failed.
   */
  public void setUpApp() throws InvalidConfigException, ClientException, InterruptedException {
    if (utils.isClusterAppserver(config.getClusterHostname())) {
      AppcfgThread.updateApp(this.config);
    } else {
      this.thread = new DevAppserverThread(config);
      this.thread.start();
    }
  }

  /**
   * Tears down a previously set up application. If the application was deployed or hasn't been
   * started with devappserver yet, this is a NOP.
   *
   * @throws InterruptedException
   */
  public void tearDownApp() throws InterruptedException {
    if (this.thread != null) {
      this.thread.stop();
    }
  }

}
