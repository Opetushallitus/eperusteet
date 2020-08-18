# ePerusteet 

Opetushallituksen ePerusteet-palvelu tutkintojen ja yleissivistävän koulutuksen opetussuunnitelmien perusteiden laadintaan ja julkaisuun.

[![Build Status](https://travis-ci.org/Opetushallitus/eperusteet.svg?branch=master)](https://travis-ci.org/Opetushallitus/eperusteet)


## Palvelukortti
<https://confluence.csc.fi/display/oppija/ePerusteet>

## Kehitysympäristön vaatimukset

- JDK 8
- Maven 3
- [käyttäjäkohtaisien asetuksien pohjat - dev-settings](/dev-settings.md)
- Nodejs sekä yo front-end-kehitystä varten
  - <http://nodejs.org/download/>
  - (sudo) npm -g install yo
  - Asenna riippupvuudet, jos puuttuvat
    - (sudo) npm -g install bower
    - (sudo) npm -g install grunt-cli

Riippuvuuksien takia käännösaikana tarvitaan pääsy sisäiseen pakettien hallintaan, koska osa paketeista (lähinnä build-parent) ei ole julkisissa repoissa.

Ajoaikana riippuu mm. keskitetystä autentikaatiosta (CAS), käyttäjähallinnasta, organisaatiopalvelusta ja koodistosta joihin täytyy olla ajoympäristöstä pääsy.

## Ajaminen paikallisesti

### eperusteet-service

  #### &nbsp;&nbsp;Käynnistys

  ```
  cd eperusteet/eperusteet-service
  mvn tomcat7:run
  ```

  #### &nbsp;&nbsp;Testaus

  ```
  cd eperusteet/eperusteet-service
  mvn clean install -Poph
  ```

  #### &nbsp;&nbsp;API-generointi

  ```
  cd eperusteet/eperusteet-service  
  mvn clean compile -Pgenerate-openapi
  specfile="$EPERUSTEET_SERVICE_DIR/target/openapi/eperusteet.spec.json"
  npx openapi-generator generate -c ../../generator.config.json -i "$specfile" -g typescript-axios
  ```

### eperusteet-app

  #### &nbsp;&nbsp;Käynnistys

  ```
  cd eperusteet/eperusteet-app/yo
  npm install
  bower install
  npm run dev
  ```

  #### &nbsp;&nbsp;Testaus

  ```
  cd eperusteet/eperusteet-app/yo
  npm run unit
  ```

### Tietokannat (vaihtoehtoinen)
  
  #### &nbsp;&nbsp;Käynnistys

  docker-compose.yml tiedosto talteen ( [käyttäjäkohtaisien asetuksien pohjat - dev-settings](/dev-settings.md) )

  ```
  docker-compose up
  ```

## ePerusteet-projektit

  Projekti | Build status | Maintainability | Test Coverage | Known Vulnerabilities
  -------- | ------------ | --------------- | ------------- | ----------------------
  [ePerusteet](https://github.com/Opetushallitus/eperusteet) | [![Build Status](https://travis-ci.org/Opetushallitus/eperusteet.svg?branch=master)](https://travis-ci.org/Opetushallitus/eperusteet)
  [ePerusteet-amosaa](https://github.com/Opetushallitus/eperusteet-amosaa) | [![Build Status](https://travis-ci.org/Opetushallitus/eperusteet-amosaa.svg?branch=master)](https://travis-ci.org/Opetushallitus/eperusteet-amosaa)
  [ePerusteet-ylops](https://github.com/Opetushallitus/eperusteet-ylops) | [![Build Status](https://travis-ci.org/Opetushallitus/eperusteet-ylops.svg?branch=master)](https://travis-ci.org/Opetushallitus/eperusteet-ylops)
  [ePerusteet-ui](https://github.com/Opetushallitus/eperusteet-ui) | [![Build Status](https://travis-ci.org/Opetushallitus/eperusteet-ui.svg?branch=master)](https://travis-ci.org/Opetushallitus/eperusteet-ui) |  |  | 
  [eperusteet-ylops-ui](https://github.com/Opetushallitus/eperusteet-ylops-ui) | [![Build Status](https://travis-ci.org/Opetushallitus/eperusteet-ylops-ui.svg?branch=master)](https://travis-ci.org/Opetushallitus/eperusteet-ylops-ui) | [![Maintainability](https://api.codeclimate.com/v1/badges/eea9e59302df6e343d57/maintainability)](https://codeclimate.com/github/Opetushallitus/eperusteet-ylops-ui/maintainability) | [![Test Coverage](https://api.codeclimate.com/v1/badges/eea9e59302df6e343d57/test_coverage)](https://codeclimate.com/github/Opetushallitus/eperusteet-ylops-ui/test_coverage) | 
  [ePerusteet-amosaa-ui](https://github.com/Opetushallitus/eperusteet-amosaa-ui) | [![Build Status](https://travis-ci.org/Opetushallitus/eperusteet-amosaa-ui.svg?branch=master)](https://travis-ci.org/Opetushallitus/eperusteet-amosaa-ui) |  |  | 
  [ePerusteet-opintopolku](https://github.com/Opetushallitus/eperusteet-opintopolku) | [![Build Status](https://travis-ci.org/Opetushallitus/eperusteet-opintopolku.svg?branch=master)](https://travis-ci.org/Opetushallitus/eperusteet-opintopolku) | [![Maintainability](https://api.codeclimate.com/v1/badges/24fc0c3e2b968b432319/maintainability)](https://codeclimate.com/github/Opetushallitus/eperusteet-opintopolku/maintainability) | [![Test Coverage](https://api.codeclimate.com/v1/badges/24fc0c3e2b968b432319/test_coverage)](https://codeclimate.com/github/Opetushallitus/eperusteet-opintopolku/test_coverage)
  [ePerusteet-backend-utils](https://github.com/Opetushallitus/eperusteet-backend-utils) | [![Build Status](https://travis-ci.org/Opetushallitus/eperusteet-backend-utils.svg?branch=master)](https://travis-ci.org/Opetushallitus/eperusteet-backend-utils)
  [ePerusteet-frontend-utils](https://github.com/Opetushallitus/eperusteet-frontend-utils) | [![Build Status](https://travis-ci.org/Opetushallitus/eperusteet-frontend-utils.svg?branch=master)](https://travis-ci.org/Opetushallitus/eperusteet-frontend-utils) | [![Maintainability](https://api.codeclimate.com/v1/badges/f782a4a50622ae34a2bd/maintainability)](https://codeclimate.com/github/Opetushallitus/eperusteet-frontend-utils/maintainability) | [![Test Coverage](https://api.codeclimate.com/v1/badges/f782a4a50622ae34a2bd/test_coverage)](https://codeclimate.com/github/Opetushallitus/eperusteet-frontend-utils/test_coverage)
