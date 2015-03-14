# Python Tutorial #


## Test Configuration ##

Before we can use gaedriver, we need to set up the test configuration. For this tutorial, create a configuration file in '$HOME/.gaedriver' and make it readable only to you.

If you want your tests to be run against dev\_appserver, use a configuration like the following:
```
[config]
app_id: my-test-app-id
cluster_hostname: localhost:8080
sdk_dir: /PATH/TO/SDK/
app_dir: /PATH/TO/gaedriver/py/demo/app
```

If you want to test against production a few more parameters are needed, for example your username and password to deploy the app:
```
[config]
app_id: my-test-app-id
cluster_hostname: appspot.com
sdk_dir: /PATH/TO/SDK/
app_dir: /PATH/TO/gaedriver/py/demo/app
username: alice@example.com
password: secret
```


## Write test module ##

At first some imports:
```
import urllib2
import os
# We are using unittest2 because it has module-level fixtures.
import unittest2
# And of course import gaedriver.
import gaedriver
```
Next, we define the path to the configuration file and a global token which will be needed later on:
```
# Path to the configuration file.
TEST_CONFIG_FILE = '%s/.gaedriver' % os.environ['HOME']
# Token returned by gaedriver for started apps.
GAEDRIVER_APP_TOKEN = None
```

Because setting up an application for testing is expensive, it makes sense to do it only once in most cases. With unittest2, we can do it at the test module level. Also note how gaedriver.setup\_app() returns a token that is stored.
```
def setUpModule():
    global GAEDRIVER_APP_TOKEN
    config = gaedriver.load_config_from_file(TEST_CONFIG_FILE)
    # gaedriver.setup_app() will either deploy the app referenced in
    # config or start it with dev_appserver. The particular action
    # depends on the cluster_hostname attribute. If it points to
    # localhost (e.g., localhost:8080), dev_appserver is used. Any other
    # value will trigger a deployment.
    GAEDRIVER_APP_TOKEN = gaedriver.setup_app(config)
```

This token is neded for the test fixture tear-down. It allows us to stop dev\_appserver.
```
def tearDownModule():
    config = gaedriver.load_config_from_file(TEST_CONFIG_FILE)
    # Once the tests are completed, use gaedriver.teardown_app() to
    # clean up. For apps started with dev_appserver, this will stop
    # the dev_appserver. For deployed apps, this is currently a NOP.
    gaedriver.teardown_app(config, GAEDRIVER_APP_TOKEN)
```

With all the meta-work done, we can do some testing. Here is a simple example that just checks if the homepage serves the right content:
```
class AppTest(unittest2.TestCase):
    """Basic application tests."""

    def setUp(self):
        self.config = gaedriver.load_config_from_file(TEST_CONFIG_FILE)

    def test_homepage(self):
        url = 'http://%s/' % self.config.app_hostname
        response = urllib2.urlopen(url)
        self.assertEqual('Hello World', response.read())
```

A more advanced test, but based on the same setup, is whether App Engine serves ETag headers as you expect it:
```
class ETagsTest(unittest2.TestCase):
    """Test that ETag headers are served correctly."""

    def setUp(self):
        self.config = gaedriver.load_config_from_file(TEST_CONFIG_FILE)

    def test_dynamic_content(self):
        url = 'http://%s/' % (self.config.app_hostname)
        response = urllib2.urlopen(url)
        self.assertFalse('ETag' in response.headers)

    def test_static_content(self):
        url = 'http://%s/static/static.txt' % self.config.app_hostname
        response = urllib2.urlopen(url)
        self.assertTrue('ETag' in response.headers)
```

You can find the full example at http://code.google.com/p/gaedriver/source/browse/py/demo/test_e2e.py

## gaedriver.run\_appcfg\_with\_auth() ##

In the example above we only used gaedriver to bring up an app for testing. But sometimes it makes sense to use appcfg, for example to configure task queues or the DoS settings. That's where gaedriver.run\_appcfg\_with\_auth() comes in handy. It allows you to use appcfg from within your test. Passing in a gaedriver.Config object and the action you want to perform, it will return the output of appcfg to you:
```
def run_appcfg_with_auth(config, action, options=None, args=None):
    """Run an appcfg command with the given options, action, and arguments.

    If 'config' has a non-empty 'backend_id' attribute, "appcfg backend"
    will be used instead of "appcfg".

    By default, app ID and server are set as given in config.

    Args:
      config: A Config instance.
      action: The action to be performed.
      options: A list of options to be used.
      args: A list of arguments passed to appcfg, e.g., a directory location.

    Returns:
      An (stdout, stderr) tuple.

    Raises:
      ValueError: If one of the configuration attributes is not set.
    """
```
So much for know. As usual, you can always find more details in the source code: http://code.google.com/p/gaedriver/source/browse/py/src/gaedriver/gaedriver.py :P