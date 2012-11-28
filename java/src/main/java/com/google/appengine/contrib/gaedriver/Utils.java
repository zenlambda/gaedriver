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

import java.io.IOException;
import java.net.ServerSocket;
import java.security.SecureRandom;

/**
 * {@code Utils} provides helper functions for gaedriver.
 *
 * @author schuppe@google.com (Robert Schuppenies)
 */
class Utils {

  /* The start for the random port range. */
  static int RANDOM_PORT_BASE = 32768;

  /* The total size of the random port range. */
  static int RANDOM_PORT_RANGE = 60000 - 32768 + 1;

  /**
   * Returns @ code true} if the given cluster hostname identifies an appserver and {@code false} if
   * it identifies an devappserver.
   *
   *  The implementation is plain and everything that does not start with "localhost" will be
   * assumed to identify appserver.
   *
   * @param clusterHostname
   */
  boolean isClusterAppserver(String clusterHostname) {
    return !clusterHostname.startsWith("localhost");
  }

  /**
   * Returns a free port that can be used.
   *
   *  Note that this is a naive implementation and prone to race conditions if the identified port
   * is used by any other instance or application after being returned.
   *
   * @return A port or -1 if no free port could be found.
   */
  int pickUnusedPort() {
    int startingPoint = RANDOM_PORT_BASE + new SecureRandom().nextInt(RANDOM_PORT_RANGE);
    // Try to find a free port between the random starting point and the end of the range.
    for (int port = startingPoint; port < RANDOM_PORT_BASE + RANDOM_PORT_RANGE; port++) {
      try {
        ServerSocket socket = new ServerSocket(port);
        socket.close();
        return port;
      } catch (IOException ex) {
        continue; // try next port
      }
    }
    // If that didn't find a port, try from the start of the range to the original starting point.
    for (int port = RANDOM_PORT_BASE; port < startingPoint; port++) {
      try {
        ServerSocket socket = new ServerSocket(port);
        socket.close();
        return port;
      } catch (IOException ex) {
        continue; // try next port
      }
    }
    // Give up.
    return -1;
  }

}
