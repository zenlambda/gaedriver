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

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Unit tests for DevAppserverThread.
 *
 * @author schuppe@google.com (Robert Schuppenies)
 */
public class DevAppserverThreadTest {

  String action;
  List<String> options;
  Config config;

  @Before
  public void setUp() {
    action = "the_action";
    options = new ArrayList<String>();
    config = TestUtils.getTestConfig();
  }

  @Test
  public void testInitNoPort() throws InvalidConfigException {
    DevAppserverThread devappThread = new DevAppserverThread(config);
    Config mockConfig= Mockito.mock(Config.class);
    Mockito.when(mockConfig.getAppHostname()).thenReturn("localhost");
    devappThread.init(mockConfig);
    assertEquals("localhost", devappThread.host);
    assertEquals(Config.DEFAULT_DEVAPPSERVER_PORT, devappThread.port);
  }

  @Test
  public void testInitWithPort() throws InvalidConfigException {
    DevAppserverThread devappThread = new DevAppserverThread(config);
    Config mockConfig= Mockito.mock(Config.class);
    Mockito.when(mockConfig.getAppHostname()).thenReturn("localhost:1742");
    devappThread.init(mockConfig);
    assertEquals("localhost", devappThread.host);
    assertEquals(1742, devappThread.port);
  }

  @Test(expected=InvalidConfigException.class)
  public void testInitWithInvalidPort() throws InvalidConfigException {
    DevAppserverThread devappThread = new DevAppserverThread(config);
    Config mockConfig= Mockito.mock(Config.class);
    Mockito.when(mockConfig.getAppHostname()).thenReturn("localhost:invalidPort");
    devappThread.init(mockConfig);
  }

  void checkBuildArgumentList(List<String> options) {
    String sep = File.separator;
    String javaBinary = System.getProperty("java.home") + sep + "bin" + sep + "java";
    String toolsJar = config.getSdkDir() + sep + "lib" + sep + "appengine-tools-api.jar";
    DevAppserverThread devappThread = null;
    try {
      if (options.size() == 0) {
        devappThread = new DevAppserverThread(config);
      } else {
        devappThread = new DevAppserverThread(config, options);
      }
    } catch (InvalidConfigException e) {
    }
    List<String> argList = devappThread.buildArgumentList();
    assertEquals(javaBinary, argList.get(0));
    assertEquals("-ea", argList.get(1));
    assertEquals("-cp", argList.get(2));
    assertEquals(toolsJar, argList.get(3));
    assertTrue(argList.contains("com.google.appengine.tools.KickStart"));
    assertTrue(argList.contains("com.google.appengine.tools.development.DevAppServerMain"));
    assertTrue(argList.contains("--sdk_root=" + config.getSdkDir()));
    assertTrue(argList.contains("--address=" + devappThread.host));
    assertTrue(argList.contains("--port=" + devappThread.port));
    for (String option : options) {
      assertTrue(argList.contains(option));
    }
    assertEquals(config.getAppDir(), argList.get(argList.size() - 1));
  }

  @Test
  public void testBuildArgumentList() {
    checkBuildArgumentList(new ArrayList<String>());
  }

  @Test
  public void testBuildArgumentListWithOptions() {
    ArrayList<String> options = new ArrayList<String>();
    options.add("--option1");
    options.add("--option2");
    checkBuildArgumentList(options);
  }

}
