#!/usr/bin/python3
# -*- coding: UTF-8 -*-

# Nouda käännökset Opintopolun Käytönhallinnan Käännösten ylläpidosta Lataa (Save Link As...) -linkistä ja tallenna .jsoniksi.
# Anna tämä json ensimmäisenä parametrina.
# Anna muina parametreina lokalisointi-hakemistoja projekteista.
# Skripti parsii kaikki avaimet kaikista lokalisointilähteistä ja etsii niille suomen-, ruotsin-
# ja englanninkieliset käännökset priorisoiden ensimmäisenä annetusta lokalisointilähdeparametrista löytynyttä käännöstä.

import glob
import json
import openpyxl
import sys

if len(sys.argv) < 2:
    print('Dependencies: sudo install pip openpyxl')
    print('Usage: ' + sys.argv[0] + ' <from.json> [+ <locale_dir_1> + <locale_dir_2> + ...]')
    print('where <from.json> is saved from the localization service and locale_dirs contain locale-fi/sv/en-files.')
    sys.exit(2)

locales = []
locales_lang = []
try:
    for i in range(2, len(sys.argv)):
        for loc_file in glob.glob(sys.argv[i] + '/locale-??.json'):
            data = open(loc_file, encoding="utf8")
            locales.append(json.load(data))
            locales_lang.append(loc_file[-7:][:2]) # strip lang
except:
    print('Invalid directory or data in directory: ' + sys.argv[i])
    sys.exit(2)

data = open(sys.argv[1], encoding="utf8")
data_json = json.load(data)
data.close()

workbook = openpyxl.Workbook()
worksheet = workbook.active
worksheet.title = "Lokalisoinnit"

worksheet.cell('A1').value = 'Avain'
worksheet.cell('B1').value = 'Suomi'
worksheet.cell('C1').value = 'Ruotsi'
worksheet.cell('D1').value = 'Englanti'

filtered = list(filter(lambda l: 'eperusteet' in l['category'], data_json))
keys_from_service = set(map(lambda m: m['key'], filtered))
keys_from_loc_files = set()

for locale in locales:
    keys_from_loc_files = keys_from_loc_files.union(set(locale.keys()))
keys = keys_from_service.union(keys_from_loc_files)

def localize(lang, key):
    values_from_service = list(filter(lambda l: l['key'] == key, filtered))
    translation_from_service = (next(filter(lambda l: l['locale'] == lang, values), {'value':''}))['value']
    if translation_from_service: return translation_from_service

    lang_indices = [i for i in range(len(locales_lang)) if locales_lang[i] == lang]
    for i in lang_indices:
        if key in locales[i].keys(): return locales[i][key]
    return ""

idx = 2
for key in sorted(keys):
    values = list(filter(lambda l: l['key'] == key, filtered))
    worksheet.cell('A' + str(idx)).value = key
    worksheet.cell('B' + str(idx)).value = localize('fi', key)
    worksheet.cell('C' + str(idx)).value = localize('sv', key)
    worksheet.cell('D' + str(idx)).value = localize('en', key)
    idx += 1

workbook.save('lokaalit.xlsx')
