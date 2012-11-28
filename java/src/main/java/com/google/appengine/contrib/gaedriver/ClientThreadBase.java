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

import com.google.common.base.Preconditions;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.util.List;

/**
 * {@code ClientThreadBase} is a base class for for supported SDK client tools.
 *
 * @author schuppe@google.com (Robert Schuppenies)
 */
public abstract class ClientThreadBase {


  /* The process that will contain the executed client. */
  protected Process process;

  /* The object that will fetch output of the started process. */
  private ClientMonitor processMonitor = null;

  /* A thread in which the client monitor will be run. */
  private Thread processMonitorThread = null;

  /* The configuration to use for the client. */
  protected final Config config;

  /* A list of optional arguments to pass to the client. */
  protected final List<String> options;

  /* The captured output from stdout. */
  protected final StringBuffer stdout;

  /* The captured output from stderr. */
  protected final StringBuffer stderr;

  /**
   * Create a client thread object.
   *
   * @param config the gaedriver configuration to use
   * @param options options to be passed to the invoked client
   */
  public ClientThreadBase(Config config, List<String> options) {
    Preconditions.checkNotNull(config);
    this.config = config;
    this.options = options;
    stdout = new StringBuffer();
    stderr = new StringBuffer();
  }

  /**
   * Monitors the output of the client.
   */
  private class ClientMonitor implements Runnable {
    private BufferedReader stdoutReader;
    private BufferedReader stderrReader;
    private StringBuffer stdoutBuffer;
    private StringBuffer stderrBuffer;

    private volatile boolean shouldStop = false;

    /**
     * Creates a DevAppServerMonitor that copies output to a {@link PrintStream}.
     *
     * @param process the process to monitor
     * @param stdout the buffer stdout output should be written to
     * @param stderr the buffer stderr output should be written to
     */
    public ClientMonitor(Process process, StringBuffer stdout, StringBuffer stderr) {
      stdoutReader = new BufferedReader(new InputStreamReader(process.getInputStream()));
      stderrReader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
      this.stdoutBuffer = stdout;
      this.stderrBuffer = stderr;
    }

    @Override
    public void run() {
      String lineSep = System.getProperty("line.separator");
      try {
        while (!shouldStop) {
          String line;
          while ((line = stdoutReader.readLine()) != null) {
            stdoutBuffer.append(line + lineSep);
          }
          while ((line = stderrReader.readLine()) != null) {
            stderrBuffer.append(line + lineSep);
          }
          try {
            Thread.sleep(500);
          } catch (InterruptedException e) {
            throw new RuntimeException(e);
          }
        }
      } catch (IOException e) {
      }
    }
  }

  /**
   * Builds argument list used to start the client (including the client itself).
   *
   * @return A list of arguments that are used to invoke a child process.
   */
  protected abstract List<String> buildArgumentList();

  /**
   * Runs the client tool.
   *
   * @throws ClientException if the client could not be run as expected
   */
  public void start() throws ClientException {
    List<String> command = buildArgumentList();
    try {
      process = new ProcessBuilder(command).redirectErrorStream(true).start();
    } catch (IOException e) {
      throw new ClientException(e);
    }
    processMonitor = new ClientMonitor(process, stdout, stderr);
    processMonitorThread = new Thread(processMonitor);
    processMonitorThread.start();

    // Kill the subprocess at JVM shutdown.
    Runtime.getRuntime().addShutdownHook(new Thread() {
      @Override
      public void run() {
        process.destroy();
        processMonitor.shouldStop = true;
      }
    });
  }

  /**
   * Stops the client tool.
   *
   * @throws InterruptedException if the client could not be stopped as expected
   */
  public void stop() throws InterruptedException {
    processMonitor.shouldStop = true;
    process.destroy();
  }

  /**
   * @return The stdout stream of the client.
   */
  public String getStdout() {
    return stdout.toString();
  }

  /**
   * @return The stderr stream of the client.
   */
  public String getStderr() {
    return stderr.toString();
  }

}
