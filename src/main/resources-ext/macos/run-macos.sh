#!/bin/bash -x

echo "current directory:"
pwd

OOLITE_STARTER_HOME=`dirname $0`
JAVA_HOME="${OOLITE_STARTER_HOME}/../Resources/jvm"
# hints from https://www.oracle.com/technical-resources/articles/javase/javatomac.html
JAVA_OPTS="-Dcom.apple.macos.useScreenMenuBar=true -Dcom.apple.mrj.application.growbox.intrudes=false -Dcom.apple.mrj.application.live-resize=true"

pushd .
cd "${OOLITE_STARTER_HOME}"
${JAVA_HOME}/bin/java ${JAVA_OPTS} -jar ${OOLITE_STARTER_HOME}/../Resources/dist/@projectname@-@pomversion@.jar $@
RETVAL=$?
popd

exit $RETVAL