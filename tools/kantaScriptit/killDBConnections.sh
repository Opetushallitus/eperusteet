#!/usr/bin/env bash
# https://coderwall.com/p/xhaz_a
if [ -n "$1" ] ; then
  where="where pg_stat_activity.datname = '$1'"
  echo "killing all connections to database '$1'"
else
  echo "killing all connections to database"
fi

sudo cat <<-EOF | psql -U test -d test
SELECT pg_terminate_backend(pg_stat_activity.pid)
FROM pg_stat_activity
${where}
EOF
