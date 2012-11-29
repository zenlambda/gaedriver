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

package com.google.appengine.contrib.gaedriver.e2e;

import static org.junit.Assert.assertTrue;

import com.google.appengine.contrib.gaedriver.ClientException;
import com.google.appengine.contrib.gaedriver.Config;
import com.google.appengine.contrib.gaedriver.InvalidConfigException;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.io.IOException;
import java.net.MalformedURLException;

/**
 * End-to-end test that starts an application with devappserver.
 *
 * @author schuppe@google.com (Robert Schuppenies)
 */
@RunWith(JUnit4.class)
public class DevappserverE2ETest extends AbstractE2ETest {

  @Test
  public void testDevappserver()
      throws InvalidConfigException,
      ClientException,
      InterruptedException,
      MalformedURLException,
      IOException {
    Config config = new Config("", sdkDir, appDir);
    checkE2E(config);
  }

  @Test
  public void testAppserver()
      throws InvalidConfigException,
      ClientException,
      InterruptedException,
      MalformedURLException,
      IOException {
    // Make sure the global config actually points at something else than localhost.
    assertTrue(userConfig.getClusterHostname().indexOf("localhost") == -1);
    checkE2E(userConfig);
  }
}
