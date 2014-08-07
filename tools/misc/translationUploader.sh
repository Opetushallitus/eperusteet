#!/bin/bash
if [[ ! -e $FILE ]]; then
    echo "Usage: ./exe <SESSIONID> <LOCALE> <FILE>"
    exit 1
fi

URL="https://itest-virkailija.oph.ware.fi/lokalisointi/cxf/rest/v1/localisation"
JSESSIONID=$1
KIELI=$2
FILE=$3

send() {
    curl -X POST -H "Content-Type: application/json" -b "JSESSIONID=$JSESSIONID"\
         -d "{ \"category\": \"eperusteet\", \"locale\": \"$1\", \"key\": \"$2\", \"value\": \"$3\" }" "$URL"

lines=$(cat "$FILE" | json_pp | tail -n +2 | head -n -1 | sed 's/\"//g' | sed 's/,$//' | sed 's/ : /:/' | sed -e 's/^[ \t]*//' | sort)

for line in ${lines[@]}; do
    key=$(echo $line | cut -f1 -d':')
    value=$(echo $line | cut -f2- -d':')
    send "$KIELI" "$key" "$value"
done
