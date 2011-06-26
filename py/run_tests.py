#!/usr/bin/env python
#
# Copyright 2011 Google Inc.
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#     http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.


"""Script to run tests."""

__author__ = 'schuppe@google.com (Robert Schuppenies)'

import optparse
import os
import sys

# Note that you have to install the unittest2 package, first.
try:
    import unittest2
    from pyfakefs import fake_filesystem
except ImportError:
    print "Could not import unittest2 and/or pyfakefs."
    print "Please make sure you have add the lib/ directory to your PYTHONPATH."
    sys.exit(1)

USAGE = """%prog [unit|e2e|all]"""


ROOT_PATH = os.path.abspath(os.path.dirname(__file__))
TEST_SIZE_DIRS = {'unit': 'src',
                  'e2e': 'demo'}


def run_unit_tests():
    suite = unittest2.TestSuite()
    absolute_test_dir = os.path.join(ROOT_PATH, TEST_SIZE_DIRS['unit'])
    suite = unittest2.loader.TestLoader().discover(absolute_test_dir)
    unittest2.TextTestRunner(verbosity=2).run(suite)


def run_e2e_tests():
    # add gaedriver directory to sys.path
    sys.path.insert(0, os.path.join(ROOT_PATH, 'src'))
    absolute_test_dir = os.path.join(ROOT_PATH, TEST_SIZE_DIRS['e2e'])
    suite = unittest2.loader.TestLoader().discover(absolute_test_dir)
    unittest2.TextTestRunner(verbosity=2).run(suite)


def main(test_size):
    if test_size == 'unit':
      run_unit_tests()
    elif test_size == 'e2e':
      run_e2e_tests()
    elif test_size == 'all':
      run_e2e_tests()
      run_unit_tests()
    else:
      print 'Say what? Expected test size is either "unit", "e2e", or "all".'
      sys.exit(1)


if __name__ == '__main__':
    parser = optparse.OptionParser(USAGE)
    options, args = parser.parse_args()
    if len(args) != 1:
        print 'Error: Exactly 2 arguments required.'
        parser.print_help()
        sys.exit(1)
    TEST_SIZE = args[0]
    main(TEST_SIZE)
