#!/bin/python3

import json
import openpyxl
import sys

if len(sys.argv) < 2:
    print('Dependencies: sudo install pip openpyxl')
    print('Usage: ' + sys.argv[0] + ' <from.json>')
    sys.exit(2)

# Read json to tuple
data = open(sys.argv[1])
data_json = json.load(data)
data.close()

# Open worksheet
workbook = openpyxl.Workbook()
worksheet = workbook.active
worksheet.title = "Lokalisoinnit"

# Write json to worksheet
worksheet.cell('A1').value = 'Avain'
worksheet.cell('B1').value = 'Suomi'
worksheet.cell('C1').value = 'Ruotsi'
worksheet.cell('D1').value = 'Englanti'

idx = 2
for key in sorted(data_json.keys()):
    worksheet.cell('A' + str(idx)).value = key
    worksheet.cell('B' + str(idx)).value = data_json[key]
    idx += 1

workbook.save('lokaalit.xlsx')
