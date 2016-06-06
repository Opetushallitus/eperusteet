#!/bin/python3

### excel2locale.py
### Converts localisation excel to json

import json
import openpyxl
import sys
from collections import OrderedDict

if len(sys.argv) < 2:
    print('Usage: ' + sys.argv[0] + '<from.xlsx>')
    sys.exit(2)

def create_key_value_pair_to_json(key, langs):
	for lang in langs:
		if langs[lang]:
			entry = {
				key: langs[lang]
			}
		if lang is 'fi':
				dict_fi.update(entry)
		if lang is 'sv':
				dict_sv.update(entry)


# Open worksheet
workbook = openpyxl.load_workbook(sys.argv[1])
worksheet = workbook.active

dict_fi = OrderedDict()
dict_sv = OrderedDict()

# create_key_value_pair_to_json
for row in worksheet.rows[1:]:
    if not row[0].value:
        break
    else:
        create_key_value_pair_to_json(row[0].value, {
			"fi": row[1].value, 
            "sv": row[2].value
        })

with open('locale-fi.json', 'w', encoding="utf8") as outfile:
	json.dump(dict_fi, outfile,ensure_ascii=False, indent=2)
with open('locale-sv.json', 'w', encoding="utf8") as outfile:
	json.dump(dict_sv, outfile,ensure_ascii=False, indent=2)

