const http = require("https");
const { readFile, writeFile } = require("fs");
const path = require("path");
const { execSync } = require("child_process");


const [x, y, LocalefilePath] = process.argv;


async function getLocalefile(lang) {
  return new Promise((resolve, reject) => readFile(path.join(LocalefilePath, "locale-" + lang + ".json"), (err, val) => {
    if (err) {
      reject(err);
    }
    else {
      resolve(JSON.parse(val));
    }
  }));
}

async function writeLocalefile(data, lang) {
  return new Promise((resolve, reject) => writeFile(path.join(LocalefilePath, "locale-" + lang + ".json"), JSON.stringify(data, null, 4), "utf8", (err) => {
    if (err) {
      reject(err);
    }
    else {
      resolve();
    }
  }));
}

if (!LocalefilePath) {
  console.log("Give me the path for locales, please!");
  process.exit(42);
}

function getTranslations() {
  return new Promise((resolve, reject) => {
    http.get("https://virkailija.testiopintopolku.fi/lokalisointi/cxf/rest/v1/localisation?category=eperusteet", (res) => {
      let data = "";
      res.setEncoding("utf8");
      res.on("data", blob => data += blob);
      res.on("end", () => resolve(JSON.parse(data)
        .map(({ id, category, locale, key, value }) => ({ id, category, locale, key, value }))));
    })
    .on("error", reject);
  });
}

function construct(translations, category) {
  const result = {};
  translations
    .filter(translation => translation.category === category)
    .forEach(({ key, locale, value }) => {
      result[key] = { ...result[key], [locale]: value };
    });
}

function constructForLang(translations, category, lang = "fi") {
  return translations
    .filter(translation => translation.category === category)
    .filter(translation => translation.locale === lang)
    .reduce((acc, { key, value }) => ({ ...acc, [key]: value }), {})
}

async function updateDirectory(translations, lang = "fi") {
  const data = constructForLang(translations, "eperusteet", lang);
  const original = await getLocalefile(lang);
  const combined = { ...original, ...data };
  return writeLocalefile(combined, lang)
}

async function run() {
  try {
    const translations = await getTranslations();
    await updateDirectory(translations, "fi");
    await updateDirectory(translations, "sv");
    await updateDirectory(translations, "en");
  }
  catch (err) {
    console.log(err);
  }
}

run();
