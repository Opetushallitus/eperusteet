#!/bin/bash
protractor="./node_modules/protractor"
[[ ! -d $protractor ]] && npm install protractor
tsc conf.ts
tsc --outFile e2e/result.js
$protractor/bin/webdriver-manager update --standalone
$protractor/bin/webdriver-manager start 2> /dev/null &
sleep 2
$protractor/bin/protractor conf.js
kill $(lsof -i :4444|awk '{print $2}'|tail -n 1)
wait
