#!/bin/bash

javac -d . src/ece454p1/*.java
echo "Main-Class: ece454p1.TestMain" > MANIFEST.MF
jar cfm ece.jar MANIFEST.MF ece454p1/*.class

