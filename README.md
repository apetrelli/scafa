# Scafa
Scafa is a universal non-caching roadwarrior repository.

At the moment it supports only direct and NTLM proxies.
It is similar to <a href="http://cntlm.sourceforge.net/">CNTLM</a> or <a href="http://www.squid-cache.org/">Squid</a>
with a configured upstream server.

## Prerequisites
Scafa needs Java 8 installed.

## Installing
Scafa can be with its Java-based installer (thanks to <a href="http://izpack.org/">IZPack</a>). See the <a href="https://github.com/apetrelli/scafa/releases">Releases</a> section and download "Scafa.Installer.zip", uncompress and launch the Jar file.

## Building
Scafa can be built using <a href="http://maven.apache.org/">Maven</a>.
After cloning the repository launch the command:

    mvn clean install

Under the directory:

    <gitclone>/scafa-assembly/target

There will be a zip file,


## Launching and configuring

Uncompress the zip file and launch:

    ./scafa.sh

or

    scafa.bat

Under your home directory there will be basic configuration files:

    <home>/.scafa/direct.ini
    <home>/.scafa/work.ini

Inside work.ini there is a basic configuration for NTLM (Microsoft ISA) proxies. Simply modify it with your
parameters, kill Scafa process and relaunch it with the "work" parameter:

    ./scafa.sh work

Then point your browser to your proxy at localhost:9000 and have fun!

More docs yet to come!
