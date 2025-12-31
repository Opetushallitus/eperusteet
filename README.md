# Eperusteet

[![Build Status](https://github.com/Opetushallitus/eperusteet/actions/workflows/build.yml/badge.svg)](https://github.com/Opetushallitus/eperusteet/actions)

## 1. Palvelun tehtävä

Opetushallituksen ePerusteet-palvelu tutkintojen ja yleissivistävän koulutuksen opetussuunnitelmien perusteiden laadintaan ja julkaisuun.

### Palvelukortti
<https://wiki.eduuni.fi/display/ophpolku/ePerusteet+palvelukokonaisuus>

## 2. Arkkitehtuuri

Javalla ja Spring Boot -viitekehyksellä toteutettu REST API -palvelu (`eperusteet-service` kansio). 

**Teknologiat:**
- Spring Boot 3.x
- Hibernate JPA
- PostgreSQL
- Flyway migraatiot
- Maven build

**Integraatiot:**
- Tarjoaa REST-rajapinnan [eperusteet-ui](https://github.com/Opetushallitus/eperusteet-ui) käyttöliittymälle
- Ulkoinen API muille OPH-palveluille
- CAS-autentikaatio
- Käyttäjähallinta ja organisaatiopalvelu
- Koodistopalvelu

Tiedot tallennetaan PostgreSQL-tietokantaan.

## 3. Kehitysympäristö

### 3.1. Esivaatimukset

Asenna haluammallasi tavalla

- Amazon Corretto JDK 17 tai uudempi
- Maven 3.8 tai uudempi
- Docker ja Docker Compose
- luo [dev-settingsin](/dev-settings.md) mukaiset käyttäjäkohtaisten asetusten tiedostot annettuihin polkuihin ja täytä omilla tiedoilla

**Huomioitavaa riippuvuuksista:**

Käännösaikana tarvitaan pääsy OPH:n sisäiseen pakettien hallintaan, koska osa paketeista (esim. build-parent) ei ole julkisissa Maven-repoissa. Konfiguroi Maven settings.xml tiedosto dev-settings.md ohjeiden mukaisesti.

Ajoaikana palvelu riippuu seuraavista OPH-palveluista:
- **CAS** - keskitetty autentikaatio
- **Käyttäjähallinta** - käyttäjätietojen hallinta
- **Organisaatiopalvelu** - organisaatiotietojen hallinta
- **Koodistopalvelu** - koodistojen hallinta

### 3.2. Testien ajaminen

Integraatiotestit vaativat Docker-ympäristön (testit käynnistävät PostgreSQL-kontin automaattisesti).

Aja testit komennolla:

```bash
cd eperusteet/eperusteet-service
mvn clean install
```

Vain yksikkötestit ilman integraatiotestejä:

```bash
mvn test
```
### 3.3. Migraatiot

Tietokantamigraatiot on toteutettu [Flyway](https://flywaydb.org/)-työkalulla ja ajetaan automaattisesti sovelluksen käynnistyksen yhteydessä.

**Migraatiotiedostojen sijainnit:**
- SQL-migraatiot: `eperusteet/eperusteet-service/src/main/resources/db/migration`
- Java-migraatiot: `eperusteet/eperusteet-service/src/main/java/db/migration`

**Nimeämiskäytäntö:**
- `V[versio]__[kuvaus].sql` (esim. `V1_0__initial_schema.sql`)
- Java-migraatiot: `V[versio]__[Kuvaus].java`

Migraatiohistoria tallennetaan `flyway_schema_history` -tauluun.

### 3.4. Ajaminen lokaalisti

#### 3.4.1. Tietokantojen käynnistys

Tietokantojen lokaalia pyöritystä varten luo koneellesi projektin juureen docker-compose.yml tiedosto jonka sisältö on alla:

```yaml
version: "3.1"
services:
  eperusteet:
    image: postgres:15
    environment:
      POSTGRES_USER: oph
      POSTGRES_PASSWORD: test
      POSTGRES_DB: eperusteet
    ports:
      - "127.0.0.1:5432:5432"
    volumes:
      - eperusteet_data:/var/lib/postgresql/data
  eperusteet-amosaa:
    image: postgres:15
    environment:
      POSTGRES_USER: oph
      POSTGRES_PASSWORD: test
      POSTGRES_DB: amosaa
    ports:
      - "127.0.0.1:5433:5432"
    volumes:
      - amosaa_data:/var/lib/postgresql/data
  eperusteet-ylops:
    image: postgres:15
    environment:
      POSTGRES_USER: oph
      POSTGRES_PASSWORD: test
      POSTGRES_DB: ylops
    ports:
      - "127.0.0.1:5434:5432"
    volumes:
      - ylops_data:/var/lib/postgresql/data

volumes:
  eperusteet_data:
  amosaa_data:
  ylops_data:
```

Käynnistä tietokannat komennolla:

```bash
docker compose up -d
```

#### 3.4.2. Palvelun käynnistys

Tämän jälkeen palvelun saa käyntiin seuraavilla komennoilla:

```bash
cd eperusteet/eperusteet-service
mvn spring-boot:run -Dspring-boot.run.profiles=default,dev
```

Palvelu käynnistyy oletuksena porttiin 8080. API on käytettävissä osoitteessa `http://localhost:8080/eperusteet-service/api`

#### 3.4.3. API-dokumentaation generointi

Jos muutat tietomallia tai rajapintoja, generoi OpenAPI-dokumentaatio uudelleen.

Päivitä OpenAPI-spesifikaatio:
```bash
./generate-openapi.sh openapi
```

ExternalController-rajapintojen muutoksien jälkeen:
```bash
./generate-openapi.sh openapi_ext
```

Generoitu dokumentaatio löytyy `generated/` kansiosta.

### 3.5. IDE setup

IDEAssa saattaa olla helpompi avata vain `eperusteet/eperusteet-service` kansio koko repon juuren sijaan, sillä
joillakin on tullut IDE:n sekoilua koko repon avauksen tapauksessa.

Suositeltavat asetukset:
- Aseta Maven automaattinen import päälle
- Käytä projektiin asetettua Java-versiota
- Aseta koodiformatointi käyttämään projektin määrittelemiä sääntöjä

### 3.6. Versiohallinta

Git käytäntönä projektissa on suosittu kehityshaaran squashausta päähaaraan
mergettäessä. Pre-push hook löytyy kansiosta `tools/git-hooks/`.

### 3.7. Yleisiä ongelmatilanteita

**Tietokanta ei käynnisty:**
```bash
# Tarkista Docker-konttien tila
docker ps -a

# Käynnistä kontit uudelleen
docker compose restart
```

**Migraatiovirheet:**
- Tarkista että tietokanta on tyhjä tai migraatiohistoria on oikein
- Tarvittaessa poista tietokanta ja luo uudelleen:
```bash
docker compose down -v
docker compose up -d
```

**Maven build epäonnistuu:**
- Varmista että Maven settings.xml on konfiguroitu oikein (dev-settings.md)
- Tarkista internet-yhteys OPH:n repoihin
- Tyhjennä Maven cache: `mvn dependency:purge-local-repository`

**Palvelu ei käynnisty:**
- Tarkista että portti 8080 on vapaana
- Tarkista että tietokanta on käynnissä ja saavutettavissa
- Tarkista lokitiedostosta (`app.log`) virheilmoitukset

## 4. Ympäristöt

### 4.1. Testiympäristöt

Testiympäristön rajapintojen swaggerit löytyvät osoitteesta [virkailija.testiopintopolku.fi/eperusteet-service/swagger](https://virkailija.testiopintopolku.fi/eperusteet-service/swagger/index.html)

External-rajapintojen swaggerit löytyvät osoitteesta [opetushallitus.github.io/eperusteet](https://opetushallitus.github.io/eperusteet/api/eperusteet)

### 4.2. Tuotantoympäristö

Tuotantoympäristö löytyy osoitteesta [virkailija.opintopolku.fi](https://virkailija.opintopolku.fi/eperusteet-service/)

### 4.3. Lokit

Lokit löytyvät AWS:n CloudWatch-palvelusta.

### 4.4. Continuous Integration

Buildipalveluna käytetään GitHub Actionsia ([build.yml](/.github/workflows/build.yml)). 

Pushaaminen remoteen käynnistää:
1. Testien ajamisen
2. Sovelluksen buildauksen
3. Kontti-imagen luonnin OPH:n deploytyökaluja varten
4. Imagen pushaus AWS ECR:ään

## ePerusteet-projektit

|Projekti | Build status |
|-----|-----|
|[ePerusteet](https://github.com/Opetushallitus/eperusteet)|[![Build Status](https://github.com/Opetushallitus/eperusteet/actions/workflows/build.yml/badge.svg)](https://github.com/Opetushallitus/eperusteet/actions)|
|[ePerusteet-amosaa](https://github.com/Opetushallitus/eperusteet-amosaa) | [![Build Status](https://github.com/Opetushallitus/eperusteet-amosaa/actions/workflows/build.yml/badge.svg)](https://github.com/Opetushallitus/eperusteet-amosaa/actions)|
|[ePerusteet-ylops](https://github.com/Opetushallitus/eperusteet-ylops) | [![Build Status](https://github.com/Opetushallitus/eperusteet-ylops/actions/workflows/build.yml/badge.svg)](https://github.com/Opetushallitus/eperusteet-ylops/actions)|
|[ePerusteet-ui](https://github.com/Opetushallitus/eperusteet-ui) | [![Build Status](https://github.com/Opetushallitus/eperusteet-ui/actions/workflows/build.yml/badge.svg)](https://github.com/Opetushallitus/eperusteet-ui/actions)|
|[eperusteet-ylops-ui](https://github.com/Opetushallitus/eperusteet-ylops-ui) | [![Build Status](https://github.com/Opetushallitus/eperusteet-ylops-ui/actions/workflows/build.yml/badge.svg)](https://github.com/Opetushallitus/eperusteet-ylops-ui/actions) |
|[ePerusteet-amosaa-ui](https://github.com/Opetushallitus/eperusteet-amosaa-ui) | [![Build Status](https://github.com/Opetushallitus/eperusteet-amosaa-ui/actions/workflows/build.yml/badge.svg)](https://github.com/Opetushallitus/eperusteet-amosaa-ui/actions)|
|[ePerusteet-opintopolku](https://github.com/Opetushallitus/eperusteet-opintopolku) | [![Build Status](https://github.com/Opetushallitus/eperusteet-opintopolku/actions/workflows/build.yml/badge.svg)](https://github.com/Opetushallitus/eperusteet-opintopolku/actions) |
|[ePerusteet-backend-utils](https://github.com/Opetushallitus/eperusteet-backend-utils) | [![Build Status](https://github.com/Opetushallitus/eperusteet-backend-utils/actions/workflows/build.yml/badge.svg)](https://github.com/Opetushallitus/eperusteet-backend-utils/actions)|
|[ePerusteet-frontend-utils](https://github.com/Opetushallitus/eperusteet-frontend-utils) | [![Build Status](https://github.com/Opetushallitus/eperusteet-frontend-utils/actions/workflows/build.yml/badge.svg)](https://github.com/Opetushallitus/eperusteet-frontend-utils/actions) |
|[ePerusteet-pdf](https://github.com/Opetushallitus/eperusteet-pdf) | [![Build Status](https://github.com/Opetushallitus/eperusteet-pdf/actions/workflows/build.yml/badge.svg)](https://github.com/Opetushallitus/eperusteet-pdf/actions) |
|[eperusteet-e2e-smoke-test](https://github.com/Opetushallitus/eperusteet-e2e-smoke-test) | [![Build Status](https://github.com/Opetushallitus/eperusteet-e2e-smoke-test/actions/workflows/playwright.yml/badge.svg)](https://github.com/Opetushallitus/eperusteet-e2e-smoke-test/actions)|

## 5. Lisätiedot

### 5.1. Työkalut ja apuskriptit

Projektin `tools/` kansiosta löytyy useita hyödyllisiä työkaluja:

- **kantaScriptit/** - Tietokantaan liittyviä apuskriptejä
- **lokalisointi/** - Lokalisointityökaluja (käännösten hallinta)
- **git-hooks/** - Git-hookit kehitykseen

### 5.2. Dokumentaatio

- [Swagger UI (testi)](https://virkailija.testiopintopolku.fi/eperusteet-service/swagger/index.html) - Interaktiivinen API-dokumentaatio
- [External API docs](https://opetushallitus.github.io/eperusteet/api/eperusteet) - Ulkoinen API-dokumentaatio
- [Palvelukortti](https://wiki.eduuni.fi/display/ophpolku/ePerusteet+palvelukokonaisuus) - Yleiskatsaus palveluun

### 5.3. Lisenssi

Katso [LICENSE.txt](LICENSE.txt)

### 5.4. Yhteystiedot

Opetushallitus / ePerusteet-tiimi
