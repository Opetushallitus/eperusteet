// Kirjoittaa käännökset json-tiedostoihin.
//
// Käännökset tulevat kääntäjän tiedostosta, lokalisaatiopalvelusta ja palvelukohtaisista json-tiedostoista.

import * as XLSX from 'xlsx';
import { readFileSync, writeFileSync } from 'fs';
import { join } from 'path';
import * as _ from 'lodash';
import axios from 'axios';


if (process.argv.length < 4) {
  console.log('yarn ./synckaannokset.ts <käännökset.xlsx> <src/locales>');
  process.exit(1);
}

const KaannoksetApi = "https://virkailija.testiopintopolku.fi/lokalisointi/cxf/rest/v1/localisation"
const locfile = process.argv[2];
const targetDir = process.argv[3];


function readXlsx(filename: string) {
  const wb: XLSX.WorkBook = XLSX.readFile(locfile);
  const sheet = XLSX.utils.sheet_to_json(wb.Sheets[wb.SheetNames[0]], { header: 1, raw: false });

  const result = {
    fi: {},
    sv: {},
    en: {},
  };

  for (let idx = 1; idx < sheet.length; ++idx) {
    const palvelu = sheet[idx][0];
    const avain = sheet[idx][1];
    const fi = sheet[idx][2];
    const sv = sheet[idx][3];
    const en = sheet[idx][4];
    if (fi) {
      result.fi[avain] = fi;
    }
    if (sv) {
      result.sv[avain] = sv;
    }
    if (en) {
      result.en[avain] = en;
    }
  }
  return result;
}


function readJsonFile(lang: string, targetRootDir: string) {
  try {
    const file = join(targetRootDir, `locale-${lang}.json`);
    const data = String(readFileSync(file));
    const json = JSON.parse(data);
    return json;
  }
  catch (err) {
    console.error(err);
    return {};
  }
}


function lokalisaatioJsons(targetRootDir: string) {
  return {
    fi: readJsonFile('fi', targetRootDir),
    sv: readJsonFile('sv', targetRootDir),
    en: readJsonFile('en', targetRootDir),
  };
}


async function lokalisaatio() {
  const res = await axios.get(KaannoksetApi, {
    headers: {
      'Caller-Id': '1.2.246.562.10.00000000001.eperusteet_lokalisointi'
    },
  });

  const result = {
    fi: {},
    sv: {},
    en: {},
  };

  _(res.data)
    .filter(k => _.startsWith(k.category, 'eperusteet-ylops'))
    .filter(k => result[k.locale])
    .forEach(k => result[k.locale][k.key] = k.value);

  return result;
}


function mergeLang(lang: string, ...blobs: object[]) {
  const keys = _(blobs)
    .map(b => _.keys(b[lang]))
    .flatten()
    .sortBy()
    .uniq()
    .value();

  const result = {};
  for (const key of keys) {
    for (const blob of blobs) {
      const val = blob[lang][key];
      if (val) {
        result[key] = val;
        // console.log('setting locale', key, lang, val)
      }
    }
  }
  return result;
}


/**
 * merge
 *
 * Yhdistetään eri lähteiden käännösavaimet yhdeksi. Viimeisenä ilmennyt käännösavain valitaan
 * lopputulokseen.
 *
 * @param {object[]} ...blobs eri lähteiden käännösolioita
 * @returns {object} Yhdistetty käännösolio
 */
function merge(...blobs: object[]) {
  const result = {
    fi: mergeLang('fi', ...blobs),
    sv: mergeLang('sv', ...blobs),
    en: mergeLang('en', ...blobs),
  };

  return result;
}


function writeLocales(locales, targetRootDir: string) {
  _.forEach(locales, (v, lang) => {
    const targetFile = `./locale-${lang}.json`;
    writeFileSync(targetFile, JSON.stringify(v, null, 2));
  });
}


async function main() {
  const excel = readXlsx(locfile);
  const devs = lokalisaatioJsons(targetDir);
  const palvelu = await lokalisaatio();
  const newLocales = merge(devs, palvelu, excel);
  writeLocales(newLocales, targetDir);
}

main();
