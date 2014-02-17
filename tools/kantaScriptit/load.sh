#!/bin/sh
USER=$1
DB=${2:-$USER}
cat opintoalat_V0_4.sql perusteOtsikotSuomiRuotsi_V0_4.sql arviointiasteikot.sql | psql -h localhost -U $USER -d $DB -f-

