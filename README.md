# Eperusteet

[![Build Status](https://travis-ci.org/Opetushallitus/eperusteet.svg?branch=master)](https://travis-ci.org/Opetushallitus/eperusteet)

## 1. Palvelun tehtävä

Opetushallituksen ePerusteet-palvelu tutkintojen ja yleissivistävän koulutuksen opetussuunnitelmien perusteiden 
laadintaan ja julkaisuun.

### Palvelukortti
<https://wiki.eduuni.fi/display/ophpolku/ePerusteet+palvelukokonaisuus>

## 2. Arkkitehtuuri

Javalla ja Springillä toteutettu web service. Tarjoaa rajapinnan eperusteet-ui:lle ja ulkoisille palveluille. Tallentaa 
tiedot postgreSQL-kantaan. 

Sisältää myös vanhan eperusteet käyttöliittymän (eperusteet-app kansio) mutta tämän on korvannut
uusi eperusteet-ui.

## 3. Kehitysympäristö

### 3.1. Esivaatimukset

Asenna haluammallasi tavalla

- JDK 8
- Maven 3
- Docker
- luo [dev-settingsin](/dev-settings.md) mukaiset käyttäjäkohtaisten asetusten tiedostot annettuihin polkuihin ja täytä omilla tiedoilla 

Riippuvuuksien takia käännösaikana tarvitaan pääsy sisäiseen pakettien hallintaan, koska osa paketeista (lähinnä build-parent) ei ole julkisissa repoissa.

Ajoaikana riippuu mm. keskitetystä autentikaatiosta (CAS), käyttäjähallinnasta, organisaatiopalvelusta ja koodistosta joihin täytyy olla ajoympäristöstä pääsy.

Jos on tarve kehittää vanhaa käyttöliittymää asenna:

- Nodejs, yo, bower, grunt-cli
  - <http://nodejs.org/download/>
  - (sudo) npm -g install yo
  - (sudo) npm -g install bower
  - (sudo) npm -g install grunt-cli

### 3.2. Testien ajaminen

  ```
  cd eperusteet/eperusteet-service
  mvn clean install -Poph
  ```
### 3.3. Migraatiot

Tietokantamigraatiot on toteutettu [flywaylla](https://flywaydb.org/) ja ajetaan automaattisesti kännistyksen
yhteydessä. Migraatiotiedostot löytyvät kansioista 

`eperusteet/eperusteet-service/src/main/resources/db/migration`

`eperusteet/eperusteet-service/src/main/java/db/migration`

### 3.4. Ajaminen lokaalisti

Tietokantojen lokaalia pyöritystä varten luo koneellesi esim projektin juureen docker-compose.yml tiedosto jonka sisältö on alla:

```yaml
version: "3.1"
services:
  eperusteet:
    image: postgres:10.7
    environment:
      POSTGRES_USER: oph
      POSTGRES_PASSWORD: test
      POSTGRES_DB: eperusteet
    ports:
      - "127.0.0.1:5432:5432"
    #volumes:
    #  - "./eperusteet:/var/lib/postgresql/data"
  eperusteet-amosaa:
    image: postgres:10.7
    environment:
      POSTGRES_USER: oph
      POSTGRES_PASSWORD: test
      POSTGRES_DB: amosaa
    ports:
      - "127.0.0.1:5433:5432"
    #volumes:
    #  - "./eperusteet:/var/lib/postgresql/data"
  eperusteet-ylops:
    image: postgres:10.7
    environment:
      POSTGRES_USER: oph
      POSTGRES_PASSWORD: test
      POSTGRES_DB: ylops
    ports:
      - "127.0.0.1:5434:5432"
    #volumes:
    #  - "./eperusteet:/var/lib/postgresql/data"   
```

aja tiedoston kanssa samassa kansiossa komento `docker compose up`

Tämän jälkeen palvelun saa käyntiin seuraavilla komennoilla:

```bash
cd eperusteet/eperusteet-service
mvn jetty:run -Djetty.port=8080
```

#### &nbsp;&nbsp;API-generointi

Jos muutat tietomallia tai rajapintoja aja tämä:

```
cd eperusteet/eperusteet-service  
mvn clean compile -Pgenerate-openapi
```
Tämän jälkeen kopio `eperusteet/eperusteet-service/target/openapi/eperusteet.spec.json`-tiedoston sisältö tiedostoon `eperusteet/generated/eperusteet.spec.json`


### eperusteet-app (vanha käyttöliittymä)

Jos jostain syystä tulee tarvetta tätä 

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


### 3.4.1. Kikkoja lokaaliin kehitykseen


### 3.5. IDE setup

IDEAssa saattaa olla helpompi avata vain eperusteet-service koko repon juuren sijaan, sillä 
joillakin on tullut ide:n sekoilua koko repon avauksen tapauksessa.


### 3.6. Versiohallinta

## 4. Ympäristöt

### 4.1. Testiympäristöt 

Testiympäristöjen swaggerit löytyvät seuraavista osoitteista

- [untuva](https://virkailija.untuvaopintopolku.fi/eperusteet-service/)
- [hahtuva](https://virkailija.hahtuvaopintopolku.fi/eperusteet-service/)
- [QA eli pallero](https://virkailija.testiopintopolku.fi/eperusteet-service/)

### 4.3. Lokit



### 4.4. Continuous integration

https://app.travis-ci.com/github/Opetushallitus/eperusteet

## ePerusteet-projektit

|Projekti | Build status | Maintainability | Test Coverage | Known Vulnerabilities|
|-----|-----|-----|-----|-----|
|[ePerusteet](https://github.com/Opetushallitus/eperusteet)|[![Build Status](https://travis-ci.org/Opetushallitus/eperusteet.svg?branch=master)](https://travis-ci.org/Opetushallitus/eperusteet)|     |     |     |
|[ePerusteet-amosaa](https://github.com/Opetushallitus/eperusteet-amosaa) | [![Build Status](https://travis-ci.org/Opetushallitus/eperusteet-amosaa.svg?branch=master)](https://travis-ci.org/Opetushallitus/eperusteet-amosaa)|     |     |     |
|[ePerusteet-ylops](https://github.com/Opetushallitus/eperusteet-ylops) | [![Build Status](https://travis-ci.org/Opetushallitus/eperusteet-ylops.svg?branch=master)](https://travis-ci.org/Opetushallitus/eperusteet-ylops)|     |     |     |
|[ePerusteet-ui](https://github.com/Opetushallitus/eperusteet-ui) | [![Build Status](https://travis-ci.org/Opetushallitus/eperusteet-ui.svg?branch=master)](https://travis-ci.org/Opetushallitus/eperusteet-ui)|     |     |     |
|[eperusteet-ylops-ui](https://github.com/Opetushallitus/eperusteet-ylops-ui) | [![Build Status](https://travis-ci.org/Opetushallitus/eperusteet-ylops-ui.svg?branch=master)](https://travis-ci.org/Opetushallitus/eperusteet-ylops-ui) | [![Maintainability](https://api.codeclimate.com/v1/badges/eea9e59302df6e343d57/maintainability)](https://codeclimate.com/github/Opetushallitus/eperusteet-ylops-ui/maintainability) | [![Test Coverage](https://api.codeclimate.com/v1/badges/eea9e59302df6e343d57/test_coverage)](https://codeclimate.com/github/Opetushallitus/eperusteet-ylops-ui/test_coverage)|     |
|[ePerusteet-amosaa-ui](https://github.com/Opetushallitus/eperusteet-amosaa-ui) | [![Build Status](https://travis-ci.org/Opetushallitus/eperusteet-amosaa-ui.svg?branch=master)](https://travis-ci.org/Opetushallitus/eperusteet-amosaa-ui)|     |     |     |
|[ePerusteet-opintopolku](https://github.com/Opetushallitus/eperusteet-opintopolku) | [![Build Status](https://travis-ci.org/Opetushallitus/eperusteet-opintopolku.svg?branch=master)](https://travis-ci.org/Opetushallitus/eperusteet-opintopolku) | [![Maintainability](https://api.codeclimate.com/v1/badges/24fc0c3e2b968b432319/maintainability)](https://codeclimate.com/github/Opetushallitus/eperusteet-opintopolku/maintainability) | [![Test Coverage](https://api.codeclimate.com/v1/badges/24fc0c3e2b968b432319/test_coverage)](https://codeclimate.com/github/Opetushallitus/eperusteet-opintopolku/test_coverage)|     |
|[ePerusteet-backend-utils](https://github.com/Opetushallitus/eperusteet-backend-utils) | [![Build Status](https://travis-ci.org/Opetushallitus/eperusteet-backend-utils.svg?branch=master)](https://travis-ci.org/Opetushallitus/eperusteet-backend-utils)|     |     |     |
|[ePerusteet-frontend-utils](https://github.com/Opetushallitus/eperusteet-frontend-utils) | [![Build Status](https://travis-ci.org/Opetushallitus/eperusteet-frontend-utils.svg?branch=master)](https://travis-ci.org/Opetushallitus/eperusteet-frontend-utils) | [![Maintainability](https://api.codeclimate.com/v1/badges/f782a4a50622ae34a2bd/maintainability)](https://codeclimate.com/github/Opetushallitus/eperusteet-frontend-utils/maintainability) | [![Test Coverage](https://api.codeclimate.com/v1/badges/f782a4a50622ae34a2bd/test_coverage)](https://codeclimate.com/github/Opetushallitus/eperusteet-frontend-utils/test_coverage)|     |
