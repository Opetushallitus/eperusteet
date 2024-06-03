# Eperusteet

[![Build Status](https://travis-ci.org/Opetushallitus/eperusteet.svg?branch=master)](https://travis-ci.org/Opetushallitus/eperusteet)

## 1. Palvelun tehtävä

Opetushallituksen ePerusteet-palvelu tutkintojen ja yleissivistävän koulutuksen opetussuunnitelmien perusteiden
laadintaan ja julkaisuun.

### Palvelukortti
<https://wiki.eduuni.fi/display/ophpolku/ePerusteet+palvelukokonaisuus>

## 2. Arkkitehtuuri

Javalla ja Springillä toteutettu web service (eperusteet-service kansio). Tarjoaa rajapinnan eperusteet-ui:lle ja ulkoisille palveluille. Tallentaa
tiedot postgreSQL-kantaan.

Sisältää myös vanhan eperusteet käyttöliittymän (eperusteet-app kansio) mutta tämän on jo korvannut
uusi [eperusteet-ui.](https://github.com/Opetushallitus/eperusteet-ui)

## 3. Kehitysympäristö

### 3.1. Esivaatimukset

Asenna haluammallasi tavalla

- Amazon Corretto JDK 11
- Maven 3
- Docker
- luo [dev-settingsin](/dev-settings.md) mukaiset käyttäjäkohtaisten asetusten tiedostot annettuihin polkuihin ja täytä omilla tiedoilla

Riippuvuuksien takia käännösaikana tarvitaan pääsy sisäiseen pakettien hallintaan, koska osa paketeista (lähinnä build-parent) ei ole julkisissa repoissa.

Ajoaikana riippuu mm. keskitetystä autentikaatiosta (CAS), käyttäjähallinnasta, organisaatiopalvelusta ja koodistosta joihin täytyy olla ajoympäristöstä pääsy.

### 3.2. Testien ajaminen

  ```
  cd eperusteet/eperusteet-service
  mvn clean install
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
    image: postgres:12.10
    environment:
      POSTGRES_USER: oph
      POSTGRES_PASSWORD: test
      POSTGRES_DB: eperusteet
    ports:
      - "127.0.0.1:5432:5432"
    #volumes:
    #  - "./eperusteet:/var/lib/postgresql/data"
  eperusteet-amosaa:
    image: postgres:12.10
    environment:
      POSTGRES_USER: oph
      POSTGRES_PASSWORD: test
      POSTGRES_DB: amosaa
    ports:
      - "127.0.0.1:5433:5432"
    #volumes:
    #  - "./eperusteet:/var/lib/postgresql/data"
  eperusteet-ylops:
    image: postgres:12.10
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
mvn spring-boot:run -Dspring-boot.run.profiles=default,dev
```

#### &nbsp;&nbsp;API-generointi

Jos muutat tietomallia tai rajapintoja aja tämä (vaatii https://github.com/casey/just):

```
just gen_openapi
```

ExternalController-rajapintojen muutoksien jälkeen aja tämä:
```
just gen_openapi_ext
```

### 3.4.1. Kikkoja lokaaliin kehitykseen


### 3.5. IDE setup

IDEAssa saattaa olla helpompi avata vain eperusteet-service koko repon juuren sijaan, sillä
joillakin on tullut ide:n sekoilua koko repon avauksen tapauksessa.


### 3.6. Versiohallinta

Git käytäntönä projektissa on suosittu kehityshaaran squashausta päähaaraan
mergettäessä.

## 4. Ympäristöt

### 4.1. Testiympäristöt

Testiympäristön rajapintojen swaggerit löytyvät osoitteesta [virkailija.testiopintopolku.fi/eperusteet-service/swagger](https://virkailija.testiopintopolku.fi/eperusteet-service/swagger/index.html)
External-rajapintojen swaggerit löytyvät osoitteesta [opetushallitus.github.io/eperusteet](https://opetushallitus.github.io/eperusteet/api/eperusteet)


### 4.3. Lokit

Lokit löytyvät AWS:n cloudwatchista

### 4.4. Continuous integration

Buildipalveluna käytetään Github Actionsia ([build.yml](/.github/workflows/build.yml)). Pushaaminen remoteen aiheuttaa sen että
eperusteet-ui ja eperusteet-service buildataan. Tämän jälkeen luodaan kontti-image OPH:n deploytyökaluja varten.

## ePerusteet-projektit

|Projekti | Build status | Maintainability | Test Coverage | Known Vulnerabilities|
|-----|-----|-----|-----|-----|
|[ePerusteet](https://github.com/Opetushallitus/eperusteet)|[![Build Status](https://github.com/Opetushallitus/eperusteet/actions/workflows/build.yml/badge.svg)](https://github.com/Opetushallitus/eperusteet/actions)|[![Maintainability](https://api.codeclimate.com/v1/badges/39796a1c7290d5286fb9/maintainability)](https://codeclimate.com/github/Opetushallitus/eperusteet/maintainability)|[![Test Coverage](https://api.codeclimate.com/v1/badges/39796a1c7290d5286fb9/test_coverage)](https://codeclimate.com/github/Opetushallitus/eperusteet/test_coverage)|     |
|[ePerusteet-amosaa](https://github.com/Opetushallitus/eperusteet-amosaa) | [![Build Status](https://github.com/Opetushallitus/eperusteet-amosaa/actions/workflows/build.yml/badge.svg)](https://github.com/Opetushallitus/eperusteet-amosaa/actions)|[![Maintainability](https://api.codeclimate.com/v1/badges/f4874f6e7c0b3253a72c/maintainability)](https://codeclimate.com/github/Opetushallitus/eperusteet-amosaa/maintainability)|[![Test Coverage](https://api.codeclimate.com/v1/badges/f4874f6e7c0b3253a72c/test_coverage)](https://codeclimate.com/github/Opetushallitus/eperusteet-amosaa/test_coverage)|     |
|[ePerusteet-ylops](https://github.com/Opetushallitus/eperusteet-ylops) | [![Build Status](https://github.com/Opetushallitus/eperusteet-ylops/actions/workflows/build.yml/badge.svg)](https://github.com/Opetushallitus/eperusteet-ylops/actions)|[![Maintainability](https://api.codeclimate.com/v1/badges/0d726dbe19fb50cd2372/maintainability)](https://codeclimate.com/github/Opetushallitus/eperusteet-ylops/maintainability)|[![Test Coverage](https://api.codeclimate.com/v1/badges/0d726dbe19fb50cd2372/test_coverage)](https://codeclimate.com/github/Opetushallitus/eperusteet-ylops/test_coverage)|     |
|[ePerusteet-ui](https://github.com/Opetushallitus/eperusteet-ui) | [![Build Status](https://github.com/Opetushallitus/eperusteet-ui/actions/workflows/build.yml/badge.svg)](https://github.com/Opetushallitus/eperusteet-ui/actions)|[![Maintainability](https://api.codeclimate.com/v1/badges/08a12ebfa585ba5bd7e4/maintainability)](https://codeclimate.com/github/Opetushallitus/eperusteet-ui/maintainability)|[![Test Coverage](https://api.codeclimate.com/v1/badges/08a12ebfa585ba5bd7e4/test_coverage)](https://codeclimate.com/github/Opetushallitus/eperusteet-ui/test_coverage)|     |
|[eperusteet-ylops-ui](https://github.com/Opetushallitus/eperusteet-ylops-ui) | [![Build Status](https://github.com/Opetushallitus/eperusteet-ylops-ui/actions/workflows/build.yml/badge.svg)](https://github.com/Opetushallitus/eperusteet-ylops-ui/actions) |[![Maintainability](https://api.codeclimate.com/v1/badges/75658db76fec914e5a64/maintainability)](https://codeclimate.com/github/Opetushallitus/eperusteet-ylops-ui/maintainability)|[![Test Coverage](https://api.codeclimate.com/v1/badges/75658db76fec914e5a64/test_coverage)](https://codeclimate.com/github/Opetushallitus/eperusteet-ylops-ui/test_coverage)|     |
|[ePerusteet-amosaa-ui](https://github.com/Opetushallitus/eperusteet-amosaa-ui) | [![Build Status](https://github.com/Opetushallitus/eperusteet-amosaa-ui/actions/workflows/build.yml/badge.svg)](https://github.com/Opetushallitus/eperusteet-amosaa-ui/actions)|[![Maintainability](https://api.codeclimate.com/v1/badges/e76c6bcc2fbe83e98f43/maintainability)](https://codeclimate.com/github/Opetushallitus/eperusteet-amosaa-ui/maintainability)|[![Test Coverage](https://api.codeclimate.com/v1/badges/e76c6bcc2fbe83e98f43/test_coverage)](https://codeclimate.com/github/Opetushallitus/eperusteet-amosaa-ui/test_coverage)|     |
|[ePerusteet-opintopolku](https://github.com/Opetushallitus/eperusteet-opintopolku) | [![Build Status](https://github.com/Opetushallitus/eperusteet-opintopolku/actions/workflows/build.yml/badge.svg)](https://github.com/Opetushallitus/eperusteet-opintopolku/actions) | [![Maintainability](https://api.codeclimate.com/v1/badges/24fc0c3e2b968b432319/maintainability)](https://codeclimate.com/github/Opetushallitus/eperusteet-opintopolku/maintainability) | [![Test Coverage](https://api.codeclimate.com/v1/badges/24fc0c3e2b968b432319/test_coverage)](https://codeclimate.com/github/Opetushallitus/eperusteet-opintopolku/test_coverage)|     |
|[ePerusteet-backend-utils](https://github.com/Opetushallitus/eperusteet-backend-utils) | [![Build Status](https://github.com/Opetushallitus/eperusteet-backend-utils/actions/workflows/build.yml/badge.svg)](https://github.com/Opetushallitus/eperusteet-backend-utils/actions)|[![Maintainability](https://api.codeclimate.com/v1/badges/0b134dc49bbed795915b/maintainability)](https://codeclimate.com/github/Opetushallitus/eperusteet-backend-utils/maintainability)|[![Test Coverage](https://api.codeclimate.com/v1/badges/0b134dc49bbed795915b/test_coverage)](https://codeclimate.com/github/Opetushallitus/eperusteet-backend-utils/test_coverage)|     |
|[ePerusteet-frontend-utils](https://github.com/Opetushallitus/eperusteet-frontend-utils) | [![Build Status](https://github.com/Opetushallitus/eperusteet-frontend-utils/actions/workflows/build.yml/badge.svg)](https://github.com/Opetushallitus/eperusteet-frontend-utils/actions) | [![Maintainability](https://api.codeclimate.com/v1/badges/f782a4a50622ae34a2bd/maintainability)](https://codeclimate.com/github/Opetushallitus/eperusteet-frontend-utils/maintainability) | [![Test Coverage](https://api.codeclimate.com/v1/badges/f782a4a50622ae34a2bd/test_coverage)](https://codeclimate.com/github/Opetushallitus/eperusteet-frontend-utils/test_coverage)|     |
|[ePerusteet-pdf](https://github.com/Opetushallitus/eperusteet-pdf) | [![Build Status](https://github.com/Opetushallitus/eperusteet-pdf/actions/workflows/build.yml/badge.svg)](https://github.com/Opetushallitus/eperusteet-pdf/actions) |[![Maintainability](https://api.codeclimate.com/v1/badges/b5b1675b68a0b935952c/maintainability)](https://codeclimate.com/github/Opetushallitus/eperusteet-pdf/maintainability)|[![Test Coverage](https://api.codeclimate.com/v1/badges/b5b1675b68a0b935952c/test_coverage)](https://codeclimate.com/github/Opetushallitus/eperusteet-pdf/test_coverage)|     |
|[eperusteet-e2e-smoke-test](https://github.com/Opetushallitus/eperusteet-e2e-smoke-test) | [![Build Status](https://github.com/Opetushallitus/eperusteet-e2e-smoke-test/actions/workflows/build.yml/badge.svg)](https://github.com/Opetushallitus/eperusteet-e2e-smoke-test/actions)|[![Maintainability](https://api.codeclimate.com/v1/badges/b83286846538dc62bb29/maintainability)](https://codeclimate.com/github/Opetushallitus/eperusteet-e2e-smoke-test/maintainability)|[![Test Coverage](https://api.codeclimate.com/v1/badges/b83286846538dc62bb29/test_coverage)](https://codeclimate.com/github/Opetushallitus/eperusteet-e2e-smoke-test/test_coverage)|     |
