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

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.mockito.Mockito;

import java.util.Properties;

/**
 * Unit tests for Config.
 *
 * @author schuppe@google.com (Robert Schuppenies)
 */
@RunWith(JUnit4.class)
public class ConfigTest {

  @Test
  public void testInitCallsParseAppId() {
    String appId = "foo";
    Config config = Mockito.spy(new Config("", "", ""));
    config.init(appId, "", 0, "", "", "", "", "", "");
    Mockito.verify(config).parseAppId(appId);
  }

  @Test
  public void testInitBackendValuesSet() {
    String backendId = "";
    int backendInstances = 7;
    Config config = new Config("", "", "");
    config.init("", backendId, backendInstances, "", "", "", "", "", "");
    assertEquals(backendId, config.getBackendId());
    assertEquals(backendInstances, config.getBackendInstances());
  }

  @Test
  public void testInitNoClusterHostname() {
    int port = 42;
    Utils mockUtils = Mockito.mock(Utils.class);
    Mockito.when(mockUtils.pickUnusedPort()).thenReturn(port);
    Config config = new Config("", "", "");
    config.utils = mockUtils;
    config.init("", "", 0, "", "", "", "", "", "");
    assertEquals("localhost:" + String.valueOf(port), config.getClusterHostname());
  }

  @Test
  public void testInitAppHostnameWithoutBackend() {
    String clusterHostname = "appspot.com";
    Utils mockUtils = Mockito.mock(Utils.class);
    Mockito.when(mockUtils.isClusterAppserver(clusterHostname)).thenReturn(true);
    Config config = new Config("", "", "");
    config.utils = mockUtils;
    config.init("appid", "", 0, clusterHostname, "", "", "", "", "");
    assertEquals("appid." + clusterHostname, config.getAppHostname());
  }

  @Test
  public void testInitAppHostnameWithBackend() {
    String clusterHostname = "appspot.com";
    String backendId = "the-backend";
    Utils mockUtils = Mockito.mock(Utils.class);
    Mockito.when(mockUtils.isClusterAppserver(clusterHostname)).thenReturn(true);
    Config config = new Config("", "", "");
    config.utils = mockUtils;
    config.init("appid", backendId, 0, clusterHostname, "", "", "", "", "");
    assertEquals(backendId + "." + "appid." + clusterHostname, config.getAppHostname());
  }

  @Test
  public void testInitAppHostnameForDevappserver() {
    String clusterHostname = "localhost:89234";
    Utils mockUtils = Mockito.mock(Utils.class);
    Mockito.when(mockUtils.isClusterAppserver(clusterHostname)).thenReturn(false);
    Config config = new Config("", "", "");
    config.utils = mockUtils;
    config.init("appid", "", 0, clusterHostname, "", "", "", "", "");
    assertEquals(clusterHostname, config.getAppHostname());
    assertEquals(Config.NO_AC_ON_SDK, config.getAcHostname());
  }

  @Test
  public void testInitAppDir() {
    String appDir = "/the/app/dir";
    Config config = new Config("", "", "");
    config.init("appid", "", 0, "", "", appDir, "", "", "");
    assertEquals(appDir, config.getAppDir());
  }

  @Test
  public void testInitSdkDir() {
    String sdkDir = "/the/sdk/dir";
    Config config = new Config("", "", "");
    config.init("appid", "", 0, "", sdkDir, "", "", "", "");
    assertEquals(sdkDir, config.getSdkDir());
  }

  @Test
  public void testInitUsername() {
    String username = "alice@example.com";
    Config config = new Config("", "", "");
    config.init("appid", "", 0, "", "", "", "", username, "");
    assertEquals(username, config.getUsername());
  }

  @Test
  public void testInitPassword() {
    String password = "secret";
    Config config = new Config("", "", "");
    config.init("appid", "", 0, "", "", "", "", "", password);
    assertEquals(password, config.getPassword());
  }

  void checkParseAppId(String input, String expectedAppId, String expectedDisplayAppId,
      String expectedPartition, String expectedDomain) {
    Config config = new Config("cluster_hostname", "sdk_dir", "app_dir");
    config.parseAppId(input);
    assertEquals(expectedAppId, config.getAppId());
    assertEquals(expectedDisplayAppId, config.getDisplayAppId());
    assertEquals(expectedPartition, config.getPartition());
    assertEquals(expectedDomain, config.getDomain());

  }

  @Test
  public void testParseAppId() {
    checkParseAppId("", "", "", "", "");
    checkParseAppId("foo", "foo", "foo", "", "");
    checkParseAppId("s~foo", "s~foo", "foo", "s", "");
    checkParseAppId("a-domain.com:foo", "a-domain.com:foo", "foo", "", "a-domain.com");
    checkParseAppId("s~a-domain.com:foo", "s~a-domain.com:foo", "foo", "s", "a-domain.com");
  }

  @Test
  public void testConfigStringStringString() {
    Config config = TestUtils.getTestConfig();
    assertEquals("clusterHostname", config.getClusterHostname());
    assertEquals("sdkDir", config.getSdkDir());
    assertEquals("appDir", config.getAppDir());
  }

  /**
   * Test method for {@link com.google.appengine.contrib.gaedriver.Config#Config(java.lang.String,
   * java.lang.String, int, java.lang.String, java.lang.String, java.lang.String, java.lang.String,
   * java.lang.String, java.lang.String)}.
   */
  @Test
  public void testConfigStringStringIntStringStringStringStringStringString() {
    Config config = TestUtils.getTestConfig();
    assertEquals("appId", config.getAppId());
    assertEquals("backendId", config.getBackendId());
    assertEquals(0, config.getBackendInstances());
    assertEquals("clusterHostname", config.getClusterHostname());
    assertEquals("sdkDir", config.getSdkDir());
    assertEquals("appDir", config.getAppDir());
    assertEquals("acHostname", config.getAcHostname());
    assertEquals("username", config.getUsername());
    assertEquals("password", config.getPassword());
  }

  Properties getTestProperties() {
    Properties properties = new Properties();
    properties.setProperty("appId", "appIdValue");
    properties.setProperty("backendId", "backendIdValue");
    properties.setProperty("backendInstances", "17");
    properties.setProperty("clusterHostname", "clusterHostnameValue");
    properties.setProperty("sdkDir", "sdkDirValue");
    properties.setProperty("appDir", "appDirValue");
    properties.setProperty("acHostname", "acHostnameValue");
    properties.setProperty("username", "usernameValue");
    properties.setProperty("password", "passwordValue");
    return properties;
  }

  @Test
  public void testLoadFromProperties() throws InvalidConfigException {
    Properties properties = getTestProperties();
    Config config = Config.loadFromProperties(properties);
    assertEquals(properties.get("appId"), config.getAppId());
    assertEquals(properties.get("backendId"), config.getBackendId());
    assertEquals(17, config.getBackendInstances());
    assertEquals(properties.get("clusterHostname"), config.getClusterHostname());
    assertEquals(properties.get("sdkDir"), config.getSdkDir());
    assertEquals(properties.get("appDir"), config.getAppDir());
    assertEquals(properties.get("acHostname"), config.getAcHostname());
    assertEquals(properties.get("username"), config.getUsername());
    assertEquals(properties.get("password"), config.getPassword());
  }

  @Test(expected = InvalidConfigException.class)
  public void testLoadFromPropertiesWithInvalidBackendInstances() throws InvalidConfigException {
    Properties properties = getTestProperties();
    properties.setProperty("backendInstances", "no integer");
    Config config = Config.loadFromProperties(properties);
  }

}
