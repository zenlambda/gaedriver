/*
 * Copyright (C) 2012 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package com.google.appengine.contrib.gaedriver;

import com.google.common.base.Joiner;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * {@code AppcfgThread} runs appcfg in a separate thread.
 *
 * @author schuppe@google.com (Robert Schuppenies)
 */
public class AppcfgThread extends ClientThreadBase {

  /*
   * Applications are not always available right away after an update. It is recommended to wait a
   * few seconds before continuing.
   */
  public static int APP_DEPLOY_WAIT = 2000;

  /* maximum number of retries if appcfg deployment gets a rollback response. */
  public static int MAX_ROLLBACK_RETRIES = 2;

  private final String action;

  /**
   * Creates a new AppcfgThread object.
   *
   * @param config the gaedriver configuration to use
   * @param action the action appcfg should invoke
   * @param options a list of options passed to appcfg
   */
  public AppcfgThread(Config config, String action, List<String> options) {
    super(config, options);
    this.action = action;
  }

  /**
   * Creates a new AppcfgThread object.
   *
   * @param config the gaedriver configuration to use
   * @param action the action appcfg should invoke
   */
  public AppcfgThread(Config config, String action) {
    super(config, new ArrayList<String>());
    this.action = action;
  }

  @Override
  protected List<String> buildArgumentList() {
    List<String> command = new ArrayList<String>();
    Joiner joiner = Joiner.on(File.separator);
    String javaBinary = joiner.join(System.getProperty("java.home"), "bin", "java");
    String toolsJar = joiner.join(config.getSdkDir(), "lib", "appengine-tools-api.jar");
    command.add(javaBinary);
    command.add("-cp");
    command.add(toolsJar);
    command.add("com.google.appengine.tools.admin.AppCfg");
    command.add("--application=" + config.getAppId());
    command.add("--sdk_root=" + config.getSdkDir());
    command.add("--server=" + config.getAcHostname());
    command.add("--email=" + config.getUsername());
    command.add("--passin");
    command.addAll(options);
    command.add(this.action);
    command.add(config.getAppDir());
    return command;
  }

  @Override
  public void start() throws ClientException {
    super.start();
    // Wait for AppCfg
    try {
      // Send the password to AppCfg
      OutputStream out = process.getOutputStream();
      out.write((config.getPassword() + "\n").getBytes());
      out.flush();
      // Check exit status
      if (process.waitFor() != 0) {
        throw new ClientException("appcfg call failed: " + this.getStdout());
      }
    } catch (InterruptedException e) {
      process.destroy();
      throw new ClientException("Interrupted while calling appcfg: " + e.getMessage());
    } catch (IOException e) {
      process.destroy();
      throw new ClientException("Failed to use appcfg:" + e.getMessage());
    }
  }

  /**
   * Updates the application aka pushes the application to appserver.
   *
   * @param options a list of options to pass to the appcfg
   * @throws ClientException if appcfg could not be run as expected
   * @throws InterruptedException if waiting for appcfg to finish failed.
   */
  public static void updateApp(Config config, List<String> options)
      throws ClientException, InterruptedException {
    Pattern pattern = Pattern.compile(
        ".*Another transaction by user \\D+ is already in progress.*", Pattern.DOTALL);
    for (int retries = 0; retries < MAX_ROLLBACK_RETRIES; retries++) {
      AppcfgThread thread = new AppcfgThread(config, "update");
      try {
        thread.start();
        Thread.sleep(AppcfgThread.APP_DEPLOY_WAIT);
        break;
      } catch (ClientException e) {
        // Note that appcfg errors are returned on stdout - duh!
        String stdout = thread.getStdout();
        if (stdout.length() > 0) {
          Matcher matcher = pattern.matcher(stdout);
          if (matcher.matches()) {
            AppcfgThread rollbackThread = new AppcfgThread(config, "rollback");
            try {
              rollbackThread.start();
            } finally {
              rollbackThread.stop();
            }
            String rollbackStdout = rollbackThread.getStdout();
            String rollbackStderr = rollbackThread.getStderr();
            if ((rollbackStdout.toLowerCase().indexOf("error") >= 0)
                || (rollbackStderr.length() >= 0)) {
              throw new ClientException("Deployment failed and subsequent rollback failed:"
                  + rollbackStdout + rollbackStdout);
            }
            rollbackThread.stop();
          }
        }
      } finally {
        thread.stop();
      }
    }
  }

  /**
   * Updates the application aka pushes the application to appserver.
   *
   * @throws ClientException if appcfg could not be run as expected
   * @throws InterruptedException if waiting for appcfg to finish failed.
   */
  public static void updateApp(Config config) throws ClientException, InterruptedException {
    updateApp(config, new ArrayList<String>());
  }


}
