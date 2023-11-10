SHDIR=$(
    cd "$(dirname "$0")"
    pwd
)
echo current path:$SHDIR
PIDFILE=$SHDIR/process.pid
if [ -f $PIDFILE ]; then
    if kill -0 $(cat $PIDFILE) >/dev/null 2>&1; then
        echo server already running as process $(cat $PIDFILE).
        exit 0
    fi
fi
for d in "$SHDIR"/../lib/*.jar; do
    CLASSPATH="$CLASSPATH:$d"
done
CLASSPATH="${CLASSPATH#:}"

LOGFILE="nohup.out"
nohup java -server -Xms2g -Xmx2g -XX:MaxMetaspaceSize=128m \
    -Dsun.net.inetaddr.ttl=300 -XX:+HeapDumpOnOutOfMemoryError -XX:HeapDumpPath=. \
    -cp .:$SHDIR/../conf:$CLASSPATH $@ >$LOGFILE &

if [ $? -eq 0 ]; then
    if /bin/echo -n $! >"$PIDFILE"; then
        tail -n 100 $LOGFILE
    else
        echo FAILED TO WRITE PID
        exit 1
    fi
else
    echo "server nohup failed!"
    exit 1
fi
