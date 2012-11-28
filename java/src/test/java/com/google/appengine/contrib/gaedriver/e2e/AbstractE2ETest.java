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

import static org.junit.Assert.assertEquals;

import com.google.appengine.contrib.gaedriver.ClientException;
import com.google.appengine.contrib.gaedriver.Config;
import com.google.appengine.contrib.gaedriver.GaeDriver;
import com.google.appengine.contrib.gaedriver.InvalidConfigException;
import com.google.appengine.contrib.gaedriver.TestUtils;
import com.google.common.base.Joiner;

import org.junit.Before;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Base class for end-to-end tests.
 *
 * @author schuppe@google.com (Robert Schuppenies)
 */
public class AbstractE2ETest {

  String appDir;
  String sdkDir;
  Config userConfig;

  @Before
  public void setUp() throws FileNotFoundException, IOException, InvalidConfigException {
    userConfig = Config.loadFromFile();
    sdkDir = userConfig.getSdkDir();
    if (!new File(sdkDir).exists()) {
      throw new AssertionError(
          String.format("SDK directory defined in %s does not exist.", Config.DEFAULT_CONFIG_FILE));
    }

    URL resourceUrl = getClass().getResource(".");
    File resourceFile = new File(resourceUrl.getFile());
    File tmpDir = resourceFile;
    for (int levels = 0; levels < 6; levels++) {
      tmpDir = tmpDir.getParentFile();
    }
    Joiner joiner = Joiner.on(File.separator);
    appDir = joiner.join(tmpDir.getAbsolutePath(), "testapp", "target", "testapp-1.0-SNAPSHOT");
  }

  void checkE2E(Config config)
      throws InvalidConfigException,
      ClientException,
      InterruptedException,
      MalformedURLException,
      IOException {
    config.setAppDir(appDir);
    GaeDriver gaedriver = new GaeDriver(config);
    String url = "http://" + config.getAppHostname();
    gaedriver.setUpApp();
    String response = TestUtils.getResponseContent(url);
    assertEquals("Hello, World!", response.toString());
    gaedriver.tearDownApp();
  }

}
