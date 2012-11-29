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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Unit tests for AppcfgThread.
 *
 * @author schuppe@google.com (Robert Schuppenies)
 */
@RunWith(JUnit4.class)
public class AppcfgThreadTest {

  String action;
  List<String> options;
  Config config;

  @Before
  public void setUp() {
    action = "the_action";
    options = new ArrayList<String>();
    config = TestUtils.getTestConfig();
  }

  void checkBuildArgumentList(List<String> options) {
    String sep = File.separator;
    String javaBinary = System.getProperty("java.home") + sep + "bin" + sep + "java";
    String toolsJar = config.getSdkDir() + sep + "lib" + sep + "appengine-tools-api.jar";
    AppcfgThread appcfgThread;
    if (options.size() == 0) {
      appcfgThread = new AppcfgThread(config, action);
    } else {
      appcfgThread = new AppcfgThread(config, action, options);
    }
    List<String> argList = appcfgThread.buildArgumentList();
    assertEquals(javaBinary, argList.get(0));
    assertEquals("-cp", argList.get(1));
    assertEquals(toolsJar, argList.get(2));
    assertTrue(argList.contains("com.google.appengine.tools.admin.AppCfg"));
    assertTrue(argList.contains("--application=" + config.getAppId()));
    assertTrue(argList.contains("--sdk_root=" + config.getSdkDir()));
    assertTrue(argList.contains("--server=" + config.getAcHostname()));
    assertTrue(argList.contains("--email=" + config.getUsername()));
    for (String option : options) {
      assertTrue(argList.contains(option));
    }
    assertEquals(action, argList.get(argList.size() - 2));
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
