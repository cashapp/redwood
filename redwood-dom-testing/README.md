# DOM Testing

Snapshot testing for HTML DOMs.


### How it Works

Kotlin/JS browser tests run via [Karma]. It runs a NodeJS service, which serves the HTML page,
and that hosts the in-browser tests.

We have a custom Karma middleware that implements these endpoints:

  * `GET /snapshots/{path}`: Reads a snapshot file from the local file system. Returns 200 if
    the file is found, and 404 if it does not.

  * `POST /snapshots/{path}`: Writes a snapshot file to the local file system.





[Karma]: https://karma-runner.github.io/0.13/config/configuration-file.html
