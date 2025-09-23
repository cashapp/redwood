//
// Copyright (C) 2025 Square, Inc.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//      http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
//
let fs = require('node:fs');
let path = require('node:path');

function installSnapshotsStore(config) {
  /**
   * Given a path like '/Development/redwood/build/js/packages/redwood-redwood-dom-testing-test',
   * this returns the original Kotlin path like '/Development/redwood/redwood-dom-testing'.
   *
   * This is clumsy! We'd prefer to be able to read that path directly from somewhere.
   */
  function jsDirectoryToModuleDirectory(jsDirectory) {
    let parts = jsDirectory.split('/');

    let fullyQualifiedDirectory = parts.pop();
    let moduleDirectory = fullyQualifiedDirectory.split('-').slice(1, -1).join('-')

    parts.pop(); // Discard 'packages'
    parts.pop(); // Discard 'js'
    parts.pop(); // Discard 'build'
    parts.push(moduleDirectory);

    return parts.join('/')
  }

  let moduleDirectory = jsDirectoryToModuleDirectory(config.basePath);

  function isSnapshotRequest(method, urlPath) {
    return urlPath.startsWith('/snapshots/') && (method === 'GET' || method === 'POST');
  }

  function SnapshotStoreMiddlewareFactory(config) {
    return function (request, response, next) {
      let url = new URL(request.originalUrl, "https://example.com/");
      let urlPath = url.pathname;
      let writeToBuildDir = url.searchParams.get('dir') === 'build';

      if (!isSnapshotRequest(request.method, urlPath)) {
        return next();
      }

      let filePath = writeToBuildDir
        ? path.join(moduleDirectory, 'build', urlPath)
        : path.join(moduleDirectory, 'src', 'test', urlPath);

      if (!filePath.startsWith(`${moduleDirectory}/`)) {
        return next(); // Directory traversal attack? Don't touch the file system.
      }

      if (request.method === 'GET') {
        fs.readFile(filePath, (err, data) => {
          if (err) {
            response.writeHead(404);
            response.end('no such file');
          } else {
            response.end(data);
          }
        });
      } else {
        try {
          fs.mkdirSync(path.dirname(filePath), {recursive: true});
        } catch (err) {
          // Already exists?
        }

        let writeStream = fs.createWriteStream(filePath);
        request.on('data', function (chunk) {
          writeStream.write(chunk);
        });

        request.on('end', function () {
          writeStream.end();
          response.end('accepted');
        });
      }
    }
  }

  config.plugins = config.plugins || [];
  config.plugins.push({'middleware:snapshot-store': ['factory', SnapshotStoreMiddlewareFactory]});

  config.middleware = config.middleware || [];
  config.middleware.push('snapshot-store');
}

/**
 * Karma runs a web server that hosts test code. Mocha is a JavaScript test framework. We configure
 * timeouts in Karma's config under client.mocha.timeout.
 */
function configureMochaTimeout(config, timeout) {
  config.client = config.client || {};
  config.client.mocha = config.client.mocha || {};
  config.client.mocha.timeout = timeout;
}

installSnapshotsStore(config);
configureMochaTimeout(config, "10s");
