#!/bin/bash

javac -d . -Xlint src/ece454p1/*.java
echo "Main-Class: ece454p1.TestMain" > MANIFEST.MF
jar cfm ece.jar MANIFEST.MF ece454p1/*.class

