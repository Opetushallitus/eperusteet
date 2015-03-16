#!/bin/python

import subprocess
import json
import sys
import http.client

LOCALIZATION_SERVICE='itest-virkailija.oph.ware.fi'
conn = http.client.HTTPSConnection(LOCALIZATION_SERVICE)
conn.request('GET', '/lokalisointi/cxf/rest/v1/localisation?category=eperusteet')
response = json.JSONDecoder().decode(conn.getresponse().read().decode())

if len(sys.argv) < 2:
    print('Usage: ' + sys.argv[0] + ' <JSESSIONID>')
    sys.exit(2)

for loc in response:
    id = str(loc['id'])
    if len(id) > 0 and loc.get('category') == 'eperusteet':
        subprocess.call(['curl',
                        '-X', 'DELETE',
                        '-H', 'Content-Type: application/json',
                        '-b', 'JSESSIONID=' + sys.argv[1],
                        'https://' + LOCALIZATION_SERVICE + '/lokalisointi/cxf/rest/v1/localisation/' + id])
