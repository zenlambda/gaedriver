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


"""gaedriver - a tool for end-to-end testing of App Engine apps."""

__author__ = 'schuppe@google.com (Robert Schuppenies)'

__author__ = 'schuppe@google.com (Robert Schuppenies)'

import optparse
import os
import shutil
import sys
import tempfile
import time
import zipfile


USAGE = """%prog

Create a distributable."""


def get_version():
    """Get the version of the current release.

    Returns:
      The version string.

    Raises:
      ValueError: If no valid VERSION file could be found.
    """
    python_dir = os.path.abspath(os.path.dirname(__file__))
    version_file = os.path.join(python_dir, 'VERSION')
    version_fh = open(version_file)
    lines = version_fh.readlines()
    version_fh.close()
    for line in lines:
        if line.startswith('release:'):
            _, version = line.split(':')
            version = version.strip()
            version_parts = version.split(' ')
            return version_parts[0]
    raise ValueError('no line "release: ..." found in %s' % version_file)


def create_dist_dir():
    """Create the distributable in a temporary directory.

    Returns:
      The name of the temporary directory.
    """
    python_dir = os.path.abspath(os.path.dirname(__file__))
    temp_dir = tempfile.mkdtemp()
    for rel_dir in ['demo', 'lib', 'src']:
        src = os.path.join(python_dir, rel_dir)
        dst = os.path.join(temp_dir, rel_dir)
        shutil.copytree(src, dst)
    license_file = os.path.join(os.path.dirname(python_dir), 'COPYING')
    shutil.copy(license_file, temp_dir)
    run_test_file = os.path.join(python_dir, 'run_tests.py')
    shutil.copy(run_test_file, temp_dir)
    version_file = os.path.join(python_dir, 'VERSION')
    shutil.copy(version_file, temp_dir)
    version_fh = open(os.path.join(temp_dir, 'VERSION'), 'a')
    epoch_time = int(time.time())
    version_fh.write('timestamp: %s' % epoch_time)
    version_fh.close()
    return temp_dir


def create_zip(input_dir, output_file):
    """Create a zip file with the content of input_dir.

    Args:
      input_dir: Name of the directory holding the content for the zip.
      output_file: name of the output file.
    """
    z_file = zipfile.ZipFile(output_file, mode='w')
    for root, _, files in os.walk(input_dir):
        for input_file in files:
            _, ext = os.path.splitext(input_file)
            if ext in ('pyc', 'pyo'):
                continue
            src = os.path.join(root, input_file)
            arcname = src[len(input_dir) + 1:]
            z_file.write(src, arcname)
    z_file.close()


def main():
    """Main function."""
    version = get_version()
    output_filename = 'gaedriver-%s.zip' % version
    temp_dir = create_dist_dir()
    create_zip(temp_dir, output_filename)


if __name__ == '__main__':
    PARSER = optparse.OptionParser(USAGE)
    OPTIONS, ARGS = PARSER.parse_args()
    if len(ARGS) != 0:
        print 'Error: No arguments required.'
        PARSER.print_help()
        sys.exit(1)
    main()

