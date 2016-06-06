#!/bin/python3

### excel2locale.py
### Uploads a locale excel to localization service

import subprocess
import json
import openpyxl
import sys
import codecs

if len(sys.argv) < 4:
    print('Usage: ' + sys.argv[0] + '<JSESSIONID> <from.xlsx> <SERVICE_ROOT>')
    sys.exit(2)


LOCALIZATION_SERVICE=sys.argv[3] + '/lokalisointi/cxf/rest/v1/localisation'

# Parse localizations
data = json.loads(subprocess.check_output(['curl',
                '-X', 'GET',
                '-H', 'Content-Type: application/json',
                LOCALIZATION_SERVICE]).decode("utf-8"))


categories = {
    'eperusteet': {},
    'eperusteet-ylops': {},
    'eperusteet-opintopolku': {},
    'eperusteet-amosaa': {},
}


for val in data:
    if val['category'] in categories:
        category = categories[val['category']]
        if val['key'] not in categories:
            category[val['key']] = {}

        category[val['key']][val['locale']] = {
            'id': val['id'],
            'value': val['value']
        }


def update(id, data):
    print(subprocess.check_output(['curl',
                    '-X', 'PUT',
                    '-H', 'Content-Type: application/json',
                    '-b', 'JSESSIONID=' + sys.argv[1],
                    '-d', json.JSONEncoder().encode(data),
                    LOCALIZATION_SERVICE + "/" + id]))


def add(data):
    print(subprocess.check_output(['curl',
                    '-X', 'POST',
                    '-H', 'Content-Type: application/json',
                    '-b', 'JSESSIONID=' + sys.argv[1],
                    '-d', json.JSONEncoder().encode(data),
                    LOCALIZATION_SERVICE]))


def send_to_localization_service(locale):
    for lang in locale['langs']:
        if locale['langs'][lang] and len(locale['langs'][lang]) > 0 and locale['category'].startswith('eperusteet'):
            entry = {
                'category': locale['category'],
                'key': locale['key'],
                'locale': lang,
                'value': locale['langs'][lang]
            }

            try:
                id = categories[entry['category']][entry['key']][entry['locale']]['id']
                update(str(id), entry)
            except KeyError:
                add(entry)


# Open worksheet
workbook = openpyxl.load_workbook(sys.argv[2])
worksheet = workbook.active

# Send into localization service
idx = 2
for row in worksheet.rows[1:]:
    if not row[0].value:
        break
    else:
        send_to_localization_service({
            'category': row[0].value.split(":")[0],
            'key': row[0].value.split(":")[1],
            'langs': {
                'fi': row[1].value, 
                'sv': row[2].value
            }
        })
    idx += 1
