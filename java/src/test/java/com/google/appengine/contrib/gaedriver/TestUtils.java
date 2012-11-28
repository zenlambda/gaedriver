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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

/**
 * A class that holds helper functionality for tests.
 *
 * @author schuppe@google.com (Robert Schuppenies)
 */
public class TestUtils {

  public static Config getTestConfig() {
    return new Config("appId",
        "backendId",
        0,
        "clusterHostname",
        "sdkDir",
        "appDir",
        "acHostname",
        "username",
        "password");
  }

  public static String getResponseContent(String url) throws MalformedURLException, IOException {
    StringBuffer responseString = new StringBuffer();
    URLConnection connection = new URL(url).openConnection();
    BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
    String aux;
    while ((aux = in.readLine()) != null) {
      responseString.append(aux);
    }
    in.close();
    return responseString.toString();
  }



}
