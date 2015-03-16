#!/bin/python3

### excel2locale.py
### Uploads a locale excel to localization service

import subprocess
import json
import openpyxl
import sys

LOCALIZATION_SERVICE='https://itest-virkailija.oph.ware.fi/lokalisointi/cxf/rest/v1/localisation'

if len(sys.argv) < 3:
    print('Usage: ' + sys.argv[0] + '<JSESSIONID> <from.xlsx>')
    sys.exit(2)

def send_to_localization_service(key, langs):
    for lang in langs:
        if langs[lang]:
            entry = {
                'category': 'eperusteet',
                'key': key,
                'locale': lang,
                'value': langs[lang]
            }
            subprocess.call(['curl',
                            '-X', 'POST',
                            '-H', 'Content-Type: application/json',
                            '-b', 'JSESSIONID=' + sys.argv[1],
                            '-d', json.JSONEncoder().encode(entry),
                            LOCALIZATION_SERVICE])

# Open worksheet
workbook = openpyxl.load_workbook(sys.argv[2])
worksheet = workbook.active

# Send into localization service
idx = 2
for row in worksheet.rows[1:]:
    if not row[0].value:
        break
    else:
        send_to_localization_service(row[0].value, {
            "fi": row[1].value, 
            "sv": row[2].value, 
            "en": row[3].value
        })
    idx += 1
