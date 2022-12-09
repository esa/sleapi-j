#!/bin/sh

#before running this script the SLE_JAR and CLASSPATH should be set. 
export SLE_JAR=../../esa.sle.java.api.core/target/
export CLASSPATH=../../esa.sle.java.api.core/extlib/
CP=$SLE_JAR/esa.sle.java.api.core-5.1.0.jar:$CLASSPATH/jasn1-compiler-1.11.2.jar:$CLASSPATH/antlr-2.7.7.jar:$CLASSPATH/jasn1-1.11.2.jar
echo "Call test communication server with arguments $*"
java -cp $CP esa.sle.impl.api.apipx.pxcs.Slecsexe $*
