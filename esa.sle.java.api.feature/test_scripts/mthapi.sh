#!/bin/sh

#before running this script the SLE_JAR and CLASSPATH should be set. 
export SLE_JAR=../dist/jar/
export CLASSPATH=../extlib/

CP=$SLE_JAR/sleapi-1.1.4.jar:$SLE_JAR/sleapi-impl-1.1.4.jar:$SLE_JAR/sleapi-harness-1.1.4.jar:$CLASSPATH/jasn1-compiler-1.5.0.jar:$CLASSPATH/antlr-2.7.7.jar:$CLASSPATH/jasn1-1.5.0.jar
echo "Call multi-instance test harness with arguments $*"
java -cp $CP esa.sle.impl.tst.systst.multi.MTHApiexe $*
