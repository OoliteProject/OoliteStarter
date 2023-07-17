#!/bin/bash

OOLITE_STARTER_HOME=`dirname $0`
JAVA_OPTS=""

if [ ! -f "configuration.properties" ]
then
	pushd .
	cd "${OOLITE_STARTER_HOME}"
	javaw ${JAVA_OPTS} -jar dist/@projectname@-@pomversion@.jar $@
	RETVAL=$?
	popd
else
	javaw ${JAVA_OPTS} -jar ${OOLITE_STARTER_HOME}/dist/@projectname@-@pomversion@.jar $@
	RETVAL=$?
fi

exit $RETVAL