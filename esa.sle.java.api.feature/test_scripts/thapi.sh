#!/bin/bash

RL=readlink

if [ `uname` != Linux ] ; then
	RL=greadlink
fi

SCRIPT=$(${RL} -f "$0")
# Absolute path this script is in, thus /home/user/bin
SCRIPTPATH=$(dirname "$SCRIPT")

echo "SCRIPTPATH: '${SCRIPTPATH}'"

#before running this script the SLE_JAR and CLASSPATH should be set. 
export SLEAPI_HOME=${SCRIPTPATH}/../..
export CLASSPATH=${SLEAPI_HOME}/esa.sle.java.api.core/extlib

CP=${SLEAPI_HOME}/esa.sle.java.api.core/target/esa.sle.java.api.core-5.1.0.jar:${SLEAPI_HOME}/esa.sle.java.api.core.test.harness/target/esa.sle.java.api.core.test.harness-5.1.0.jar:$CLASSPATH/jasn1-compiler-1.11.2.jar\:$CLASSPATH/antlr-2.7.7.jar\:$CLASSPATH/jasn1-1.11.2.jar
		
#echo "Call test harness with arguments: '$*' (CP: ${CP})"
java -cp "$CP" esa.sle.impl.tst.systst.THApiexe $*
