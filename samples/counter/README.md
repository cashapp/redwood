Counter
=======

This is a very basic demo of Redwood.


Prerequisites
-------------

In order to build and run these applications you'll need to have the following installed:
- gradle
- git-lfs


Running Counter on Web
----------------------

To simply view the sample app, open: https://cashapp.github.io/redwood/latest/counter/

To build and modify the app locally, run this:
```
./gradlew samples:counter:browser:browserRun
```

If successful, the command will load the app at http://localhost:8080/ in your default web browser.

Note: only one app instance can be loaded at a time, so if you already have another app instance open, this will clobber that.
