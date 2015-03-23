#!/bin/python3

import json
import sys
import os
import codecs

if len(sys.argv) < 3:
    print('Usage: ' + sys.argv[0] + ' <from.json>' + sys.argv[1] + ' <from.dir>, ...')
    sys.exit(2)

# Read json to tuple
data = open(sys.argv[1], encoding="utf8")
data_json = json.load(data)
data.close()

unusedFile=open('./unusedLocalizationKeys.txt', 'w', encoding="utf8")

# Go through given directories and try to find localization key
for key in sorted(data_json.keys()):
	keyNotFound = True
	indx = 2
	for indx in range(len(sys.argv)):
		for subdir, dirs, files in os.walk(sys.argv[indx]):
			for file in files:
				datafile = open(os.path.join(subdir, file), encoding="utf8")
				for line in datafile:
					if key in line:
						keyNotFound = False
	if keyNotFound:
		print (key)
		unusedFile.write("%s\n" % key)

unusedFile.close()
