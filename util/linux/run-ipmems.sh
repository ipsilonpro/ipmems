#!/bin/sh

BASEDIR="$(dirname $0)"
cd "$BASEDIR"
PIDFILE="ipmems.pid"

if [ -e "$PIDFILE" ]
then
	if ! kill $(cat $PIDFILE) > /dev/null 2>&1; then
		rm "$PIDFILE"
	fi
fi
while [ -e "$PIDFILE" ]
do
	sleep 1
done
java -jar ipmems.jar $* &