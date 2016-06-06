#!/bin/python

import subprocess
import json
import sys
import http.client

if len(sys.argv) < 3:
    print('Usage: ' + sys.argv[0] + ' <JSESSIONID> <category>')
    sys.exit(2)

LOCALIZATION_SERVICE='itest-virkailija.oph.ware.fi'
conn = http.client.HTTPSConnection(LOCALIZATION_SERVICE)
conn.request('GET', '/lokalisointi/cxf/rest/v1/localisation?category=' + sys.argv[2])
response = json.JSONDecoder().decode(conn.getresponse().read().decode())

for loc in response:
    id = str(loc['id'])
    if len(id) > 0 and loc.get('category') == 'eperusteet':
        subprocess.call(['curl',
                        '-X', 'DELETE',
                        '-H', 'Content-Type: application/json',
                        '-b', 'JSESSIONID=' + sys.argv[1],
                        'https://' + LOCALIZATION_SERVICE + '/lokalisointi/cxf/rest/v1/localisation/' + id])
