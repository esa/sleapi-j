#!/bin/sh

#before running this script the SLE_JAR and CLASSPATH should be set. 
export SLE_JAR=../dist/jar/
export CLASSPATH=../extlib/

CP=$SLE_JAR/sleapi-5.1.0.jar:$SLE_JAR/sleapi-impl-5.1.0.jar:$SLE_JAR/sleapi-harness-5.1.0.jar:$CLASSPATH/jasn1-compiler-1.11.2.jar:$CLASSPATH/antlr-2.7.7.jar:$CLASSPATH/jasn1-1.11.2.jar
echo "Call test harness with arguments $*"
#java -cp $CP esa.sle.impl.tst.systst.THApiexe $*
java -Xdebug -Xnoagent -Djava.compiler=NONE -Xrunjdwp:transport=dt_socket,server=y,suspend=y,address=5005 -cp $CP esa.sle.impl.tst.systst.THApiexe $*

