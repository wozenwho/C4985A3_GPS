#!/bin/bash
# variables
name="AndroidServer"
src="server/tcp/*.java"
main="server.tcp.Main"
# commands
javac $src
echo -e "Main-Class: $main" > MANIFEST.MF
jar -cvmf MANIFEST.MF $name.jar server/tcp/*.class
cat stub.sh $name.jar > $name.run && chmod +x $name.run
