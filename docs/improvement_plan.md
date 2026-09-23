# eperusteet-service: parannussuunnitelma

Päivitetty: 2026-09-23
Kohde: `eperusteet/eperusteet-service`, Java-koodi (Spring Boot 4, Java 21, Hibernate 7). Noin 1000 päätiedostoa, 82 testitiedostoa.
Rajaus: `src/main/java`, `src/test/java` ja Java-migraatiot (`src/main/java/db/migration`). Käyttöliittymä, CI, Docker-skriptit ja `tools/` eivät kuulu tähän läpikäyntiin.

## 1. Lukuohje

Kaikki polut ovat suhteessa hakemistoon `eperusteet/eperusteet-service/src/main/java/fi/vm/sade/eperusteet/`, ellei toisin mainita. Rivinumerot viittaavat commitin `c5f6c440e` tilaan.

Vakavuusluokat:

- **Kriittinen**: hyödynnettävissä ilman kirjautumista, tai johtaa virheellisesti julkaistuun sisältöön. Korjataan heti.
- **Korkea**: todellinen bugi, tietovuoto kirjautuneille käyttäjille tai tuotantohäiriön riski.
- **Keskitaso**: virhetilanteet käsitellään väärin, piileviä vikoja tai suorituskykyongelma.
- **Matala**: ylläpidettävyys ja siisteys.

Työmäärä: S = alle päivä, M = 1–3 päivää, L = yli 3 päivää.

Merkintä **(V)** tarkoittaa, että löydös on tarkistettu käsin lähdekoodista tämän dokumentin kirjoittamisen yhteydessä. Muut löydökset ovat katselmoinnissa lähdekoodista vahvistettuja, mutta rivinumerot kannattaa tarkistaa ennen korjausta.

---

## 2. Yhteenveto ja korjausjärjestys

| # | Toimenpide | Vakavuus | Työ | Kohta |
|---|---|---|---|---|
| 1 | Suojaa `/api/eraajo/{nimi}/execute` | Kriittinen | S | 3.1 |
| 2 | Korjaa käänteinen oikeustarkistus `getKaikkiSisalto`-metodissa | Kriittinen | S | 3.2 |
| 3 | Palauta validointitulokset `ValidatorOpas`- ja `ValidatorDigitaalinenOsaaminen`-luokista | Kriittinen | S | 3.3 |
| 4 | Rajaa PDF-datan ja -tilan päivitys PDF-palvelulle | Kriittinen | S | 3.4 |
| 5 | Korjaa jsonpath-injektio julkisessa tutkinnon osan haussa | Korkea | S | 7.2 |
| 6 | Korjaa liitteiden oikeustarkistukset (`or isAuthenticated()`, `paivitaLisatieto`) | Korkea | S | 7.3 |
| 7 | Korjaa julkaisuvirta: uniikkiehto revisiolle ja projektin tila vasta onnistuneen julkaisun jälkeen | Korkea | M | 6.1, 6.2 |
| 8 | Korjaa `sort`-metodin readOnly-transaktio ja liitteiden tuonti | Korkea | S | 5.1, 5.2 |
| 9 | Korjaa logiikkavirheet (`structureEquals`, `AIPEVaihe.validateChange`, digitaalisen osaamisen kuvaustarkistus, koodiston välimuisti, CAS-asiakas) | Korkea | S–M | 4 |
| 10 | Ota CSRF ja tietoturvaotsakkeet käyttöön, rajaa CAS-proxyt, oletuksena kielletty suodatinketju | Korkea | M | 7.1 |
| 11 | Tee ajastettujen tehtävien lukituksesta klusteriturvallinen | Korkea | M | 8.1 |
| 12 | Aseta aikakatkaisut ulkoisille HTTP-kutsuille ja poista tuotanto-URL-oletukset | Korkea | M | 8.2 |
| 13 | Korjaa NPE-riskit (lista kohdassa 5.4) | Korkea/Keskitaso | M | 5.4 |
| 14 | Korjaa entiteettien equals/hashCode-toteutukset | Korkea | M | 9.1 |
| 15 | Rajaa sivukoko, kuvakoko ja tiedostonimet (palvelunestoriskit) | Keskitaso | S | 7.5 |
| 16 | Lopeta virheiden nieleminen ja virhetulosten välimuistitus | Keskitaso | M | 6.3, 6.4, 8.3 |
| 17 | Suorituskyky: EAGER-haut, koko taulun lataukset, tiedostot muistissa | Keskitaso | L | 10 |
| 18 | Testikattavuus kriittisille poluille (oikeudet, validaattorit, julkaisu) | Keskitaso | L | 11.2 |
| 19 | Refaktoroi suurimmat luokat, siirry pois Orikasta | Matala | L | 11.1, 12 |

---

## 3. Kriittiset löydökset

### 3.1 Ajastetut tehtävät käynnistettävissä ilman kirjautumista (V)

`resource/hallinta/EraAjoController.java:25-28`

```java
@RequestMapping(value = "/{nimi}/execute", method = RequestMethod.GET)
public void executeTask(@PathVariable String nimi) {
    tasks.get(nimi).executeAsync();
}
```

`config/WebSecurityConfiguration.java:143` sallii kaikki `GET /api/**`-pyynnöt (`permitAll()`). Ohjaimessa, `ScheduledTask`-rajapinnassa tai `AbstractScheduledTask`-luokassa ei ole `@PreAuthorize`-annotaatiota, joten kuka tahansa voi käynnistää minkä tahansa ajastetun tehtävän (esimerkiksi Lampi-viennin, julkaisujen välimuistin päivityksen tai koodistoon kirjoittavat tehtävät). `@Hidden` piilottaa reitin vain OpenAPI-dokumentaatiosta. Tuntematon `nimi` aiheuttaa NullPointerExceptionin ja HTTP 500 -vastauksen.

**Korjaus:**

- Lisää ylläpitäjän oikeus, esimerkiksi `@PreAuthorize("hasPermission(null, 'pohja', 'LUONTI')")`, samoin kuin muissa ylläpitotoiminnoissa.
- Vaihda HTTP-metodiksi POST.
- Palauta 404 tuntemattomalle tehtävälle: `Optional.ofNullable(tasks.get(nimi)).orElseThrow(NotExistsException::new)`.

### 3.2 Luonnosvaiheen perusteen koko sisältö julkisesti luettavissa (V)

`service/impl/PerusteServiceImpl.java:951-961`

```java
boolean hasPermission = permissionManager.hasPerustePermission(..., id, PermissionManager.Permission.LUKU);
...
if (!hasPermission && Objects.equals(peruste.getPerusteprojekti().getTila(), ProjektiTila.JULKAISTU)) {
    throw new AccessDeniedException("ei-riittavia-oikeuksia");
}
```

Ehto estää pääsyn vain, jos käyttäjältä puuttuu oikeus **ja** projekti on jo julkaistu. Julkaisemattomat luonnokset palautetaan siis kenelle tahansa. `PerusteService.getKaikkiSisalto` (`service/PerusteService.java:101-102`) on rajapinnassa ilman `@PreAuthorize`-annotaatiota ja on saatavilla julkisesti osoitteesta `GET /api/maintenance/export/{perusteId}/json` (`resource/MaintenanceController.java:115-118`). Saman ohjaimen ZIP-vienti (`exportPeruste`) sen sijaan vaatii LUKU-oikeuden, joten epäsymmetria on selvästi virhe.

**Korjaus:**

- Erota sisäinen ja ulkoinen käyttö:
  - `getKaikkiSisaltoInternal(id)` ilman oikeustarkistusta sisäisille kutsujille: `JulkaisutServiceImpl:307`, `MaintenanceServiceImpl`, `ExternalPdfServiceImpl` ja esikatselu.
  - Julkinen metodi, jossa `@PreAuthorize("hasPermission(#id, 'peruste', 'LUKU')")`.
- Suojaa `MaintenanceController.viePerusteJson` ylläpitäjän oikeudella.
- Lisää regressiotesti: kirjautumaton pyyntö luonnoksesta palauttaa 403.

### 3.3 Validaattorit hukkaavat kaikki virheet (V)

`service/impl/validators/ValidatorOpas.java:28-56` ja `service/impl/validators/ValidatorDigitaalinenOsaaminen.java:50-76`

```java
// ValidatorOpas
List<Validointi> validoinnit = new ArrayList<>();
Validointi validointi = new Validointi(ValidointiKategoria.PERUSTE);
... validointi.virhe(...) ...
return validoinnit;          // validointi-oliota ei koskaan lisätä listaan

// ValidatorDigitaalinenOsaaminen
... perusteValidointi.virhe(...); tarkistaPerusteenSisaltoTekstipalaset(..., sisaltoValidointi);
return validoinnit;          // tyhjä lista
```

Julkaisu estetään vain, jos jokin palautettu `Validointi` on virheellinen (`service/impl/JulkaisutServiceImpl.java:292-296`). Oppaat ja digitaalisen osaamisen perusteet voidaan siksi julkaista ilman voimassaolon alkua, ilman nimeä kaikilla kielillä ja puutteellisilla kieliversioilla. Digitaalisen osaamisen perusteen voi myös julkaista toiseen kertaan, vaikka tarkistus `digitaalinen-osaaminen-jo-julkaistu` on olemassa.

**Korjaus:**

```java
// ValidatorOpas
validoinnit.add(validointi);
return validoinnit;

// ValidatorDigitaalinenOsaaminen
return List.of(perusteValidointi, sisaltoValidointi);
```

Korjaa samalla kohdat 4.3 ja 4.4, koska ne tulevat esiin heti, kun validaattorit alkavat palauttaa virheitä. Lisää jokaiselle validaattorille yksikkötesti, joka varmistaa virheen näkyvän palautetussa listassa. Pitkän aikavälin korjaus on kohdassa 12.

### 3.4 PDF-datan ja -tilan ylikirjoitus kenelle tahansa kirjautuneelle (V)

`service/dokumentti/DokumenttiService.java:47-49` ja `resource/DokumenttiController.java:195-208`

```java
void updateDokumenttiPdfData(byte[] pdfData, Long dokumenttiId);
void updateDokumenttiTila(DokumenttiTila tila, Long dokumenttiId);
```

Metodeilta puuttuu `@PreAuthorize`, ja suodatinketju vaatii POST-pyynnöille vain kirjautumisen. Kuka tahansa kirjautunut virkailija voi siis korvata minkä tahansa julkaistun perusteen PDF-tiedoston omallaan tai muuttaa sen tilan. `config/WebSecurityConfigurationDev.java:41` sallii polun `/api/dokumentit/pdf/**` ilman kirjautumista.

**Korjaus:**

- Rajaa kutsut PDF-palvelun palvelutunnukselle (oma rooli tai jaettu salaisuus otsakkeessa).
- Tarkista, että `dokumenttiId` on tilassa `JONOSSA` tai `LUODAAN`, eli että PDF-palvelu todella generoi sitä.
- Varmista, ettei Dev-konfiguraatiota voi aktivoida jaetuissa ympäristöissä (ks. 7.6).

---

## 4. Logiikkavirheet

### 4.1 `TutkinnonOsa.structureEquals` vertaa kenttää itseensä (V), korkea

`domain/tutkinnonosa/TutkinnonOsa.java:193-195`

```java
if (result && that.getAmmattitaitovaatimukset2019() != null && getAmmattitaitovaatimukset2019() != null) {
    result &= that.getAmmattitaitovaatimukset2019().structureEquals(that.getAmmattitaitovaatimukset2019());
}
```

Vertailu on aina tosi, joten ammattitaitovaatimusten (2019) rakennemuutoksia ei tunnisteta. Rakenteen muutosvalidointi (esimerkiksi julkaistun perusteen korjaustilassa) päästää siksi läpi muutokset, jotka sen pitäisi estää.

**Korjaus:** `getAmmattitaitovaatimukset2019().structureEquals(that.getAmmattitaitovaatimukset2019())`. Lisää yksikkötesti, jossa ammattitaitovaatimuksia muutetaan.

### 4.2 `AIPEVaihe.validateChange` vertaa vääriä kenttiä (V), korkea

`domain/yl/AIPEVaihe.java:140-154`

```java
TekstiOsa.validateChange(a.getSiirtymaEdellisesta(), b.getSiirtymaEdellisesta());
TekstiOsa.validateChange(a.getTehtava(), b.getSiirtymaEdellisesta());
TekstiOsa.validateChange(a.getSiirtymaSeuraavaan(), b.getSiirtymaEdellisesta());
...
if (!Objects.equals(a.getJarjestys(), b.getJarjestys())) { ... }
if (!Objects.equals(a.getJarjestys(), b.getJarjestys())) { ... }   // sama tarkistus kahdesti
```

Kaksi riviä vertaa `a`:n kenttää `b`:n `siirtymaEdellisesta`-kenttään. Seurauksena on sekä vääriä hylkäyksiä (rakennetta-ei-voi-muuttaa, vaikka mitään ei muutettu) että tunnistamattomia muutoksia.

**Korjaus:** `b.getTehtava()` ja `b.getSiirtymaSeuraavaan()`. Poista toistuva järjestystarkistus.

### 4.3 Digitaalisen osaamisen validaattori tarkistaa nimen kuvauksen sijaan (V), korkea

`service/impl/validators/ValidatorDigitaalinenOsaaminen.java:126-137`

```java
tasokuvaus.getEdelleenKehittyvatOsaamiset().forEach(kuvaus -> {
    tarkistaTekstipalanen("...-osa-alue-kuvaus", osaAlue.getNimi(), pakolliset, virheellisetKielet, true);
});
```

Sama virhe toistuu kolmessa silmukassa (`getEdelleenKehittyvatOsaamiset`, `getOsaamiset`, `getEdistynytOsaaminenKuvaukset`). Tasokuvausten puuttuvia kieliversioita ei tunnisteta, ja nimen puuttuminen raportoidaan virheellisesti kuvauksen virheenä. Virhe ei tällä hetkellä näy, koska validaattori palauttaa tyhjän listan (kohta 3.3).

**Korjaus:** välitä `kuvaus` (tai sen tekstipalanen) `tarkistaTekstipalanen`-kutsulle.

### 4.4 Oppaan validointi sidottu nykyiseen tilaan eikä kohdetilaan (V), keskitaso

`service/impl/validators/ValidatorOpas.java:38`

```java
if (ProjektiTila.JULKAISTU.equals(projekti.getTila())) { ... koulutustyyppi, nimi, voimassaolon päättyminen ... }
```

`validate(perusteprojektiId, targetTila)` ei käytä `targetTila`-parametria. Ensimmäisellä julkaisulla projektin tila on vielä `VALMIS` tai `LAADINTA`, joten nämä tarkistukset eivät aja. Ne ajetaan vasta seuraavilla julkaisuilla.

**Korjaus:** käytä ehtoa `ProjektiTila.JULKAISTU.equals(targetTila)`. Varmista tarkoitus tuoteomistajalta.

### 4.5 Koodiston välimuistia ei tyhjennetä rinnasteisille relaatioille, keskitaso

`service/impl/KoodistoClientImpl.java:427-431`

```java
if (koodiRelaatioTyyppi.equals(KoodiRelaatioTyyppi.SISALTYY)) { ... alarelaatio/ylarelaatio ... }
else if (koodiRelaatioTyyppi.equals(KoodiRelaatioTyyppi.SISALTYY)) {   // sama ehto
    cacheManager.getCache("koodistot").evict("rinnasteiset:" + parentKoodi);
}
```

Toinen haara ei toteudu koskaan, joten rinnasteiset koodit näkyvät vanhentuneina välimuistin elinajan loppuun asti.

**Korjaus:** `else if (koodiRelaatioTyyppi.equals(KoodiRelaatioTyyppi.RINNASTEINEN))`.

### 4.6 Väärä CAS-asiakas käyttöoikeuspalvelun kutsuissa, korkea

`service/impl/KayttajanTietoServiceImpl.java:149-150, 196-197`

```java
OphHttpClient client = restClientFactory.get(onrServiceUrl, true);
String url = koServiceUrl + HENKILO_API + oid + "/organisaatiohenkilo";
```

CAS-istunto muodostetaan oppijanumerorekisterille, mutta kutsu menee käyttöoikeuspalveluun. `getOrganisaatioVirkailijat` (noin rivi 226) tekee saman oikein. 401-vastaus käsitellään hiljaa tyhjänä tuloksena (rivit 159-161 ja 204-205), ja tulos välimuistitetaan. Käyttäjän projektit tai organisaatiot voivat näkyä tyhjinä.

**Korjaus:** `restClientFactory.get(koServiceUrl, true)`. Kirjaa 401- ja 403-vastaukset varoituksena, äläkä välimuistita niistä syntyvää tyhjää tulosta.

### 4.7 Oppiaineen kohdealueen päivitys palauttaa tyhjän ja ohittaa omistajatarkistuksen (V), korkea

`service/impl/yl/OppiaineServiceImpl.java:440-455`

```java
OpetuksenKohdealue kohde = null;
if (kohdealue.getId() != null) {
    OpetuksenKohdealue vanhaKohde = kohdeAlueRepository.findById(kohdealue.getId()).orElse(null);
    mapper.map(kohdealue, vanhaKohde);
} else { ... kohde = ... }
...
return mapper.map(kohde, OpetuksenKohdealueDto.class);
```

- Päivityshaarassa `kohde` jää `null`-arvoon, joten metodi palauttaa tyhjän vastauksen.
- Jos id:tä ei löydy, `mapper.map(..., null)` kaatuu.
- Kohdealueen kuulumista oppiaineeseen `aine` ei tarkisteta, joten id:n avulla voi muokata toisen perusteen kohdealuetta, jos käyttäjällä on muokkausoikeus johonkin perusteeseen.

**Korjaus:** hae kohdealue oppiaineen `kohdealueet`-joukosta, heitä `NotExistsException` jos sitä ei löydy, ja aseta `kohde = vanhaKohde`.

### 4.8 Oppiaineiden järjestyksen päivitys kaatuu puutteelliseen listaan (V), keskitaso

`service/impl/yl/OppiaineServiceImpl.java:373-375`

```java
OppiaineSuppeaDto oppiaineDto = oppiaineet.stream().filter(...).findFirst().get();
sisallonOppiaine.setJnro((long)oppiaineet.indexOf(oppiaineDto));
```

Jos asiakkaan lista ei sisällä kaikkia oppiaineita (esimerkiksi rinnakkainen muokkaus lisäsi uuden), koko päivitys kaatuu `NoSuchElementException`-poikkeukseen ja HTTP 500 -vastaukseen. `sisalto` voi lisäksi olla `null`.

**Korjaus:** palauta selkeä `BusinessRuleViolationException`, tai sijoita puuttuvat oppiaineet listan loppuun.

### 4.9 Toimimattomat ja keskeneräiset toiminnot, matala

- `service/impl/PerusteServiceImpl.java:1636-1651` (`updateAllTutkinnonOsaJarjestys`): järjestysnumerot asetetaan DTO:ista mapattuihin olioihin, joita JPA ei hallinnoi eikä niitä tallenneta. Metodi ei tee mitään. **Korjaus:** poista endpoint tai toteuta hallinnoiduilla entiteeteillä.
- `service/impl/PerusteServiceImpl.java:1449-1474` (`revertRakenneVersio`, `//TODO korjaa palautus` rivillä 1460): rakenteen palautus on keskeneräinen, ja `peruste` voi olla `null`. **Korjaus:** viimeistele tai estä toiminto käyttöliittymästä.
- `service/impl/Lops2019ServiceImpl.java:334`: FIXME, jolla on toiminnallinen vaikutus. Kirjaa tiketiksi.

### 4.10 Konfiguraatioon liittyvät logiikkavirheet, keskitaso

- `service/impl/OpintoalaServiceImpl.java:29-30`: `@Value` final-kenttään ei injektoi mitään, joten kutsut menevät aina tuotannon koodistoon myös testiympäristöissä. Oletusarvon ympärillä olevat heittomerkit päätyisivät osaksi URL:ia.
- `service/impl/YlopsClientImpl.java:41`: `getOpetussuunnitelmatEtusivu` käyttää julkista `ylopsServiceUrl`-osoitetta, kun muut kutsut käyttävät `ylopsServiceUrl_internal`-osoitetta. Varmista, onko tämä tarkoituksellista.

**Korjaus:** konstruktori-injektio samasta asetuksesta kuin `KoodistoClientImpl` käyttää (`koodisto.service.url`), ilman heittomerkkejä ja ilman tuotanto-oletusta.

---

## 5. Bugit

### 5.1 Tutkinnon osien järjestys ei tallennu (V), korkea

`service/impl/PerusteenOsaViiteServiceImpl.java:33, 258-266`

Luokalla on `@Transactional(readOnly = true)`, eikä `sort`-metodilla tai rajapinnalla ole omaa transaktiomäärittelyä. `tutkinnonOsaViiteRepository.saveAll(viitteet)` ja `muokkausTietoService.addMuokkaustieto` ajetaan read-only-transaktiossa, jolloin Hibernate ei tee flushia ja Postgres-yhteys on read-only-tilassa. Järjestyksen muutos katoaa tai kaatuu. `peruste` haetaan lisäksi `orElse(null)`-kutsulla.

**Korjaus:** lisää metodiin `@Transactional` ilman `readOnly`-asetusta. Tarkista samalla luokan muut kirjoittavat metodit.

### 5.2 Liitteiden tuonti kaatuu uusilla liitteillä (V), korkea

`service/impl/PerusteImportLops2019.java:127-139`

```java
Liite liite = liiteRepository.findById(uuid).orElseThrow();
if (liite == null) { ... liiteRepository.add(...) ... }
```

`orElseThrow()` heittää poikkeuksen ennen kuin `null`-haaraan päästään. Tuonti epäonnistuu aina, kun liitettä ei ole jo kannassa, eli juuri siinä tilanteessa, jota varten haara on kirjoitettu.

**Korjaus:**

```java
Liite liite = liiteRepository.findById(uuid)
        .orElseGet(() -> liiteRepository.add(uuid, liiteDto.getTyyppi(), liiteDto.getMime(),
                liiteDto.getNimi(), projektiImport.getLiitetiedostot().get(uuid)));
peruste.attachLiite(liite);
```

### 5.3 Transaktiovirheet, keskitaso

- `service/dokumentti/impl/DokumenttiServiceImpl.java:242-259` (`query`): `@Transactional(readOnly = true)`, mutta aikakatkaisun tila tallennetaan kutsulla `dokumenttiStateService.save(dto)`. `peruste` voi olla `null` (rivi 248-250), toisin kuin `get`-metodissa. **Korjaus:** tallenna tila erillisessä `REQUIRES_NEW`-transaktiossa ja lisää `null`-tarkistus.
- `service/impl/PerusteenOsaServiceImpl.java:536-550` (`delete`): ei transaktiota, ja kommentit poistetaan erikseen ennen perusteen osaa. Virhe kesken jättää tiedot osittain poistetuiksi. **Korjaus:** `@Transactional`.

### 5.4 NullPointerException-riskit, korkea/keskitaso

Yleinen malli: `findById(...).orElse(null)` tai `findOne(...)` ja sen jälkeen suora käyttö. Asiakas saa HTTP 500 -vastauksen hallitun 404:n tai 400:n sijaan.

| Sijainti | Ongelma | Korjaus |
|---|---|---|
| `service/impl/JulkaisutServiceImpl.java:233-239` (V) | `perusteprojekti` voi olla `null` ennen `getPeruste()`-kutsua. Async-metodi (277-280) tarkistaa, synkroninen ei | `NotExistsException` |
| `service/impl/JulkaisutServiceImpl.java:227` (V) | `.filter(JulkaisuBaseDto::getJulkinen)`: `Boolean` voi olla `null` | `Boolean.TRUE.equals(j.getJulkinen())` |
| `service/impl/JulkaisutServiceImpl.java:377` | `peruste.getDiaarinumero().getDiaarinumero()` | `Optional.ofNullable(...)` |
| `service/impl/JulkaisutServiceImpl.java:480-484, 710-715` | `findFirstByPerusteAndRevision...` voi palauttaa `null` (`aktivoiJulkaisu`, `updateJulkaisu`) | Tarkista ja heitä `NotExistsException` |
| `service/impl/JulkaisutServiceImpl.java:545` (V) | `getSisaltotyyppi().equals(...)` | Vertaa vakiosta käsin |
| `service/impl/JulkaisutServiceImpl.java:608-609` | `kooditaValiaikaisetKoodit`: `peruste` voi olla `null` | `null`-tarkistus |
| `service/impl/PerusteServiceImpl.java:938-946` | `getSisallot().stream().findFirst().get()` ja `getTutkinnonOsat()` voi olla `null` | `findFirst().map(...)` |
| `service/impl/PerusteServiceImpl.java:713, 721-723, 752-755` | Osaamisalakuvaukset: `peruste`, `suoritustavat` tai DTO:n `getLapset()` voi olla `null` | `CollectionUtils.emptyIfNull` |
| `service/impl/PerusteServiceImpl.java:1480-1481` | `getTutkinnonOsat`: `peruste` voi olla `null` | `null`-tarkistus |
| `service/impl/PerusteServiceImpl.java:1560-1568` | `updateTutkinnonRakenne`: suoritustapa tai peruste voi olla `null` | Eksplisiittiset tarkistukset |
| `service/impl/PerusteServiceImpl.java:2404-2409` | Kloonaus: `vanha.getToteutus().equals(...)` | `Objects.equals` |
| `service/impl/PerusteprojektiServiceImpl.java:412, 428-429` | `koulutustyyppi` voi olla `null` ennen `isOneOf(...)`-kutsua | `null`-tarkistus |
| `service/impl/PerusteprojektiServiceImpl.java:694-700` (V) | `updateProjektiTila`: `projekti` voi olla `null`. Metodi ohittaa myös sallittujen tilasiirtymien tarkistuksen (`mahdollisetTilat`) | `NotExistsException` ja tilasiirtymän validointi |
| `service/impl/PerusteenOsaServiceImpl.java:696` | `getPerusteet().iterator().next()` tyhjälle joukolle | `stream().findFirst().orElseThrow(...)` |
| `service/impl/PerusteenOsaServiceImpl.java:307-318` | `orElse(null)` `assertExists`-kutsun jälkeen | `orElseThrow` |
| `service/impl/TiedoteServiceImpl.java:93-94, 134-135` | `peruste` tai `tiedote` voi olla `null` | `assertExists` |
| `service/impl/TutkinnonOsaViiteServiceImpl.java:62-63` | `getLatestRevisionId(id).getNumero()`, kun revisioita ei ole | `null`-tarkistus |
| `service/impl/KommenttiServiceImpl.java:51-53, 151-163` (V) | `get`, `update`, `delete`: `kommentti` voi olla `null` | `NotExistsException` |
| `service/impl/OsaamismerkkiServiceImpl.java:159-160` | `deleteOsaamismerkki`: `null` | `orElseThrow` |
| `service/impl/validators/ValidatorLops2019.java:156` | `.orElse(null).forEach(...)` | `.orElseGet(Collections::emptyList)` |
| `service/impl/validators/ValidatorKvliiteTaso.java:44-46` | `koodiUri.startsWith(...)`, kun `koodiUri` voi olla `null` | Suodata `null`-arvot |
| `ProjektiValidatorImpl:34-35`, `ValidatorOpas:31` (V), `ValidatorDigitaalinenOsaaminen:53` (V), `ValidatorPerusteTiedot`, `ValidatorKieliJaKaantajaTutkinto` | `findById(...).orElse(null)` ja sen jälkeen `projekti.getPeruste()` | Sama malli kuin `ValidatorPeruste`, ks. kohta 12 |
| `service/impl/AmosaaClientImpl.java:46-48` | `tilastot.getData()`, kun vastaus voi olla `null`. Sivutuksella ei ole ylärajaa | `null`-tarkistus ja sivumäärän yläraja |

Pitkän aikavälin korjaus on kohdassa 12 (repository-kerroksen `findByIdOrThrow`).

---

## 6. Ongelmatapaukset

### 6.1 Samanaikaiset julkaisut voivat tuottaa saman revision (V), korkea

`service/impl/JulkaisutServiceImpl.java:699-703`

```java
public int seuraavaVapaaJulkaisuNumero(long perusteId) {
    ...
    return vanhatJulkaisut.stream().mapToInt(JulkaistuPeruste::getRevision).max().orElse(0) + 1;
}
```

Revisio lasketaan max + 1 -periaatteella ilman lukitusta, eikä taulussa `julkaistu_peruste` ole uniikkiehtoa `(peruste_id, revision)` (`src/main/resources/db/migration/V0_20190704145035__julkaistu_peruste.sql`). `teeJulkaisu` (232-244) ei myöskään tarkista, onko julkaisu jo tilassa `KESKEN`. `julkaisuTaskExecutor` (`config/AsyncConfig.java:17-22`, `ThreadPoolTaskExecutor` oletusasetuksilla eli yksi säie) sarjallistaa julkaisut vain yhden solmun sisällä. Kahdella solmulla, tai kun `teeJulkaisu` ja `aktivoiJulkaisu` ajetaan rinnakkain, samalle perusteelle voi syntyä kaksi samaa revisiota. Tämän jälkeen `findFirstByPerusteAndRevision` palauttaa satunnaisen niistä.

**Korjaus:**

- Flyway-migraatio: `ALTER TABLE julkaistu_peruste ADD CONSTRAINT julkaistu_peruste_peruste_revision_uk UNIQUE (peruste_id, revision)`. Tarkista ensin tuotantodata duplikaattien varalta.
- Hylkää uusi julkaisu, jos `JulkaisuPerusteTila` on `KESKEN` (ja alle `JULKAISUN_ODOTUSAIKA_SEKUNNEISSA` vanha), tai lukitse rivi `SELECT ... FOR UPDATE` -kyselyllä.
- Määritä `julkaisuTaskExecutor`-poolin koko ja jonon pituus eksplisiittisesti.

### 6.2 Projekti merkitään julkaistuksi ennen kuin julkaisu on onnistunut (V), korkea

`service/impl/PerusteprojektiServiceImpl.java:642-689` ja `service/impl/JulkaisutServiceImpl.java:232-354`

Tilasiirtymä `VALMIS -> JULKAISTU` metodissa `updateTila`:

1. luo tiedotteen (`tiedoteService.addTiedote`),
2. kutsuu `julkaisutService.teeJulkaisu`, joka käynnistää `teeJulkaisuAsync`-metodin toisessa säikeessä,
3. asettaa `projekti.setTila(tila)` ja tallentaa (686-687) riippumatta julkaisun tuloksesta.

Seuraukset:

- Asynkroninen säie käynnistyy ennen kuin ulompi transaktio on commitoitu. Se ei välttämättä näe juuri luotua tiedotetta tai perusteen tilamuutosta.
- Jos asynkroninen julkaisu epäonnistuu (validointi, PDF, tallennus), `JulkaisuPerusteTila` saa arvon `VIRHE`, mutta projekti on silti tilassa `JULKAISTU` ilman yhtään `JulkaistuPeruste`-riviä.
- Molemmat transaktiot muokkaavat samaa `Peruste`- ja `Perusteprojekti`-riviä, mikä voi johtaa kadonneeseen päivitykseen tai optimistisen lukituksen virheeseen.

**Korjaus:**

- Käynnistä asynkroninen työ vasta commitin jälkeen: `TransactionSynchronizationManager.registerSynchronization(... afterCommit ...)` tai `@TransactionalEventListener(phase = AFTER_COMMIT)`.
- Älä aseta projektin tilaa `updateTila`-metodissa, kun kyse on julkaisusta. `teeJulkaisuAsync` asettaa sen jo onnistuneen julkaisun yhteydessä (rivi 301).

### 6.3 Julkaisun virheiden käsittely, keskitaso

- `service/impl/JulkaisutServiceImpl.java:343-347` (V): kaikki poikkeukset, myös `BusinessRuleViolationException("projekti-ei-validi")` ja `("vain-oman-perusteen-voi-julkaista")`, kääritään geneeriseksi `julkaisun-tallennus-epaonnistui`-virheeksi. Käyttöliittymä ei pysty kertomaan syytä. **Korjaus:** heitä `BusinessRuleViolationException` sellaisenaan ja tallenna virhekoodi `JulkaisuPerusteTila`-olioon, jotta käyttöliittymä voi näyttää sen.
- `service/impl/JulkaisutServiceImpl.java:431-458`: PDF- ja KV-liitteen generoinnin virhe lokitetaan, ja julkaisu jatkuu ilman dokumentteja. **Korjaus:** päätä tuoteomistajan kanssa, pitääkö julkaisun epäonnistua. Vähintään julkaisuun pitää merkitä puuttuvat dokumentit ja lähettää hälytys.
- `service/impl/JulkaisutServiceImpl.java:579-580` (V): `e.printStackTrace()` lokituksen sijaan.

### 6.4 Nielaistut virheet, keskitaso

| Sijainti | Ongelma |
|---|---|
| `service/impl/PerusteServiceImpl.java:467-468` | Tyhjä `catch (Exception ex) {}` laajuuden laskennassa. Laajuus puuttuu ilman lokimerkintää |
| `service/impl/LiiteServiceImpl.java:57-62` | Tyhjä `catch` ja `// FIXME` liitteen viennissä. Asiakas saa tyhjän vastauksen HTTP-koodilla 200 |
| `service/impl/LockManagerImpl.java:116-117` | Tyhjä `catch` myös `InterruptedException`-poikkeukselle. Keskeytyslippu katoaa |
| `service/impl/OpintoalaServiceImpl.java:48-50` | Tyhjä `catch`, tyhjä lista tallentuu `@Cacheable`-välimuistiin |
| `service/impl/LokalisointiServiceImpl.java:53-63` | Virheen sattuessa tyhjä lista tallentuu välimuistiin |
| `service/impl/KayttajanTietoServiceImpl.java:109-135, 159-161` | 401/403 ja jäsennysvirhe palauttavat `null` tai tyhjän, joka välimuistitetaan (`kayttajat`, `kayttajan_projekti`) |
| `service/impl/KoodistoClientImpl.java` (koodistorelaatioiden luonti) | Virhe vain lokitetaan, joten ajastettu tehtävä näyttää onnistuneen |
| `service/mapping/DtoMapperConfig.java` | RestClient-virheet lokitetaan, ja DTO jää ilman koodien nimiä |
| `Etags`, `VirheServlet` | Tyhjät `catch`-lohkot |

**Korjaus:**

- Kirjaa virheet aina vähintään tasolla `warn`.
- Lisää välimuistiannotaatioihin `unless = "#result == null || #result.isEmpty()"`.
- Palauta keskeytyslippu kutsulla `Thread.currentThread().interrupt()`.

---

## 7. Tietoturva

### 7.1 Suodatinketjun asetukset (V), korkea

`config/WebSecurityConfiguration.java`

- Rivit 138-139: `.headers(AbstractHttpConfigurer::disable)` ja `.csrf(AbstractHttpConfigurer::disable)`. Sovellus käyttää evästepohjaista istuntoa, joten ilman CSRF-suojausta toinen sivusto voi tehdä kirjautuneen käyttäjän nimissä tilaa muuttavia pyyntöjä. Otsakkeiden poisto kytkee pois myös `X-Frame-Options`-, `X-Content-Type-Options`- ja HSTS-oletukset.
- Rivi 97: `ticketValidator.setAcceptAnyProxy(true)`. Mikä tahansa CAS-asiakas voi hankkia proxy-tiketin tähän palveluun käyttäjän nimissä.
- Rivi 143: `GET /api/**` on `permitAll()`. Kaikki GET-reittien suojaus nojaa metoditason annotaatioihin, joten yksikin unohtunut annotaatio avaa reitin (kohdat 3.1, 3.2 ja 7.4). `@InternalApi` (`config/InternalApi.java`) vaikuttaa vain OpenAPI-dokumentaatioon.

**Korjaus:**

- Ota CSRF käyttöön `CookieCsrfTokenRepository.withHttpOnlyFalse()`-toteutuksella. Vaatii käyttöliittymään `X-XSRF-TOKEN`-otsakkeen. Rajaa poikkeukset palvelu-palvelu-kutsuihin.
- Palauta otsakkeiden oletukset. Jos upotusta tarvitaan, käytä `frameOptions().sameOrigin()`.
- Määritä luotetut proxy-ketjut (`setAllowedProxyChains`).
- Oletuksena kaikki kielletään, ja julkiset reitit avataan eksplisiittisellä listalla (esimerkiksi `/api/external/**`, `/api/julkinen/**`, julkaistun sisällön reitit). Vaadi vähintään kirjautuminen polkuihin `/api/maintenance/**`, `/api/eraajo/**` ja `/api/lampi/**`.

### 7.2 Jsonpath-injektio julkisessa endpointissa (V), korkea

`repository/JulkaisutRepository.java:227-243` ja `resource/julkinen/ExternalController.java` (tutkinnon osan haku koodilla)

```sql
concat('$.tutkinnonOsat[*] ? (@.koodi.uri == "', :tutkinnonOsaKoodiUri, '")')::jsonpath
```

Polkuparametri liitetään merkkijonona jsonpath-lausekkeeseen. Lainausmerkin sisältävä syöte muuttaa lausekkeen rakennetta. Data on jo julkaistua, joten tietovuodon riski on pieni, mutta kalliit lausekkeet (esimerkiksi `like_regex`) mahdollistavat palvelunestohyökkäyksen, ja virheellinen syöte aiheuttaa 500-virheen.

**Korjaus:** käytä jsonpath-muuttujia ja validoi parametri (esimerkiksi `^tutkinnonosat_\d+$`):

```sql
jsonb_path_query(data, '$.tutkinnonOsat[*] ? (@.koodi.uri == $uri)', jsonb_build_object('uri', :tutkinnonOsaKoodiUri))
```

Siirrä ohjaimen suora repository-kutsu palvelukerrokseen.

### 7.3 Liitteiden oikeudet ovat liian laajat (V), korkea

`service/LiiteService.java`

- Rivit 30-43: `hasPermission(#perusteId, 'peruste', 'LUKU') or isAuthenticated()`. Kuka tahansa kirjautunut voi listata ja ladata minkä tahansa perusteen liitteet, myös luonnosten.
- Rivi 45-46: `paivitaLisatieto` kirjoittaa tietoa, mutta vaatii vain `LUKU`-oikeuden.
- Rivit 18-22: `get(UUID)`, `addJulkaisuLiite` ja `addOsaamismerkkiLiite` ilman annotaatiota. `resource/LiitetiedostoController.java:160-169` hakee julkaisun liitteen pelkällä UUID:llä sitomatta sitä polun `perusteId`-arvoon (IDOR).

**Korjaus:** poista `or isAuthenticated()`-ehdot, vaadi `paivitaLisatieto`-metodille `MUOKKAUS`- tai `KORJAUS`-oikeus, ja hae liite aina vanhemman (peruste tai julkaisu) kautta.

### 7.4 Liian heikot oikeustarkistukset, keskitaso

- **Kommentit (V):** `service/KommenttiService.java:11-30`. Kaikki lukumetodit (`get`, `getAllByPerusteprojekti`, `getAllByParent`, `getAllByYlin`, `getAllByPerusteenOsa`, `getAllBySuoritustapa`) vaativat vain kirjautumisen, joten kuka tahansa virkailija voi lukea minkä tahansa projektin sisäiset kommentit. Vaadi `hasPermission(..., 'perusteprojekti', 'LUKU')`. `deleteReally` (rivi 41-42) poistaa kommentin ilman omistajatarkistusta. Sitä kutsutaan nyt vain sisäisesti (`PerusteenOsaServiceImpl:545`), mutta rajapinnan annotaatio pitää silti tiukentaa tai metodi siirtää sisäiseen palveluun.
- **Tiedotteet (V):** `service/TiedoteService.java:26`

  ```java
  @PostAuthorize("returnObject == null or returnObject.julkinen or isAuthenticated() or returnObject.julkaisupaikat.size() > 0 or ...")
  ```

  Ei-julkinen tiedote palautetaan kirjautumattomalle, jos sillä on yksikin julkaisupaikka, koulutustyyppi tai peruste. Jokainen kirjautunut näkee kaikki tiedotteet. Varmista tarkoitus ja rajaa ehto `julkinen`-kenttään ja projektikohtaiseen oikeuteen.
- **Ylläpito:** `service/MaintenanceService.java:21-22` (`getYllapitoValue`) ja `service/AmmattitaitovaatimusService.java:56-57` (`virheellisetAmmattitaitovaatimukset`) ovat `permitAll()`. Tee sallittujen avainten lista tai vaadi ylläpitäjän oikeus.
- **PDF-generointi:** `service/dokumentti/DokumenttiService.java:12-27`. Generointi vaatii vain `LUKU`-oikeuden, joten raskaita PDF-töitä voi jonottaa pelkällä lukuoikeudella. Vaadi `MUOKKAUS`, tai rajoita pyyntöjen määrää.
- **Käyttäjätiedot:** `KayttajanTietoController GET /{oid}`. Kuka tahansa kirjautunut voi hakea minkä tahansa käyttäjän tiedot. `KayttajanTietoDto` sisältää `yhteystiedot`- ja `oikeudet`-kentät. Rajaa omiin tietoihin tai poista henkilötiedot vastauksesta.

### 7.5 Palvelunestoriskit ja syötteiden validointi, keskitaso

- **Rajaamaton sivukoko (V):** `resource/peruste/JulkaisuController.java:77-81` (`PageRequest.of(sivu, sivukoko)`), `resource/JulkinenController.java:28-29` ja `resource/peruste/PerusteController.java:445-448`. Vertailukohta: `resource/julkinen/ExternalController.java:115` käyttää `Math.min(sivukoko, 50)`. Aseta yhteinen yläraja, esimerkiksi 100.
- **Kuvien skaalaus:** `service/impl/LiiteTiedostoServiceImpl.java:61-80`. Asiakkaan antamille `width`- ja `height`-arvoille ei ole ylärajaa, ja `new BufferedImage(width, height, ...)` voi kuluttaa koko keon. Aseta yläraja (esimerkiksi 4000 px).
- **Lampi-latauksen tiedostonimi:** `service/export/LampiExportService.java:196-201` ja `resource/hallinta/LampiController.java:50-54`. `csvFileName` liitetään suoraan S3-avaimeen ja `Content-Disposition`-otsakkeeseen. Validoi nimi (`^[\w.\-]+$`) ja vertaa sitä listattuihin avaimiin.
- **Tilaa muuttavat GET-reitit:** `resource/peruste/JulkaisuController.java:120-129` (`/koodita`, `/nollaajulkaisutila`) ja useat `MaintenanceController`-reitit, esimerkiksi `/lisaaAmmattitaitovaatimukset2019/{perusteId}` (rivit 125-128). GET-pyyntöjä voi laukaista esihaku, välimuisti tai upotettu kuva. Vaihda POST-metodiin.

### 7.6 Muut tietoturvahuomiot, matala

- **HTML-sanitointi:** `domain/validation/ValidHtml.java:45-58`. `href`- ja `src`-attribuuteille ei rajata protokollia, ja `style` on sallittu. Lisää `addProtocols("a", "href", "http", "https", "mailto")` ja `addProtocols("img", "src", "http", "https", "data")`, ja rajoita `style`-attribuuttia.
- **`developmentPermissionOverride`:** `service/security/PermissionManager.java:406-408` ohittaa kaikki oikeustarkistukset. Kaada käynnistys, jos asetus on päällä muussa kuin `local`-profiilissa.
- **Dev-konfiguraatio:** `config/WebSecurityConfigurationDev.java:56-100` käyttää HTTP Basicia, laajoja rooleja ja `withDefaultPasswordEncoder`-toteutusta. Hyväksyttävää vain paikallisesti.
- **Virhevastaukset ja lokit:** `config/ExceptionHandlingConfig.java` palauttaa validointivirheiden `propertyPath`-tiedot asiakkaalle ja kirjaa pyynnön URI:n kyselyparametreineen (noin rivit 219-228). Palauta vakioidut virheavaimet ja poista kyselyparametrit lokista.

---

## 8. Integraatiot, välimuisti ja taustatyöt

### 8.1 Ajastettujen tehtävien lukitus ei ole klusteriturvallinen, korkea

`service/tasks/AbstractScheduledTask.java:27-42`, `service/impl/SkeduloituajoServiceImpl.java` ja `service/ScheduledConfiguration.java`

- Tila luetaan ja päivitetään erillisinä operaatioina (hae tai lisää ajo, tarkista tila, käynnistä ajo). Kaksi solmua tai kaksi samanaikaista pyyntöä voi käynnistää saman ajon.
- `AtomicBoolean` (`ScheduledConfiguration.java:33, 74-75`) suojaa vain yhtä JVM:ää.
- Jos JVM kaatuu kesken ajon, tila jää arvoon `AJOSSA`, ja kaikki myöhemmät ajot heittävät `SkeduloituAjoAlreadyRunningException`-poikkeuksen pysyvästi.
- `ScheduledConfiguration.java:98-101`: vakava virhe kirjataan tasolla `log.debug` ilman pinoa.

**Korjaus:** ota käyttöön ShedLock (JDBC) tai tee varaus atomisella päivityksellä (`UPDATE ... SET status='AJOSSA' WHERE nimi=? AND status<>'AJOSSA'`). Lisää vanhentuneen ajon palautus (esimerkiksi yli N tuntia `AJOSSA`-tilassa). Muuta lokitaso `error`-tasoksi.

### 8.2 HTTP-kutsut ilman aikakatkaisuja ja kovakoodatut osoitteet, korkea

`new RestTemplate()` luodaan kutsukohtaisesti ilman connect- ja read-aikakatkaisuja:

- `service/impl/KoodistoClientImpl.java` (useita kohtia)
- `service/impl/LokalisointiServiceImpl.java:55, 69`
- `service/impl/OpintoalaServiceImpl.java:42`
- `service/impl/KoulutusalaServiceImpl.java:37`
- `service/impl/PerusteprojektiServiceImpl.java` (määräyskirjeen lataus, noin rivi 1031)

Jumiutunut ulkoinen palvelu sitoo pyyntösäikeet. Vertailukohta: `RestClientFactoryImpl` asettaa `OphHttpClient`-asiakkaalle 60 sekunnin aikakatkaisun.

Kovakoodatut tuotanto-URL:t oletusarvoina: `KoodistoClientImpl:68`, `LokalisointiServiceImpl:37`, `KoulutusalaServiceImpl:22` ja `OpintoalaServiceImpl:30`. Väärin konfiguroitu testiympäristö kutsuu silloin hiljaa tuotantoa. `KoulutusalaServiceImpl.java:37-44` tekee lisäksi yhden HTTP-kutsun jokaista koulutusalaa kohden (N+1).

URL:t muodostetaan merkkijonoja yhdistämällä ilman koodausta, esimerkiksi `LokalisointiServiceImpl.java:67-70` (`"category=" + category + "&locale=" + locale`) ja `KoodistoClientImpl` (`koodistoUri + "/koodi/" + koodiUri`).

**Korjaus:**

- Luo yksi jaettu `RestClient`-bean, jolle on määritetty aikakatkaisut, ja käytä sitä kaikkialla.
- Poista tuotanto-oletukset ja kaada käynnistys, jos asetus puuttuu.
- Käytä `UriComponentsBuilder`-luokkaa.
- Hae koulutusalojen relaatiot yhdellä kutsulla tai rinnakkain.

### 8.3 Välimuisti, keskitaso

- `src/main/resources/jcache.xml:44-51`: `koodistot`-välimuistin heap-koko on 20 entryä. Samaa välimuistia käyttävät `getAll` ja relaatioavaimet (`alarelaatio:`, `ylarelaatio:`, `rinnasteiset:`), joten entryt vaihtuvat jatkuvasti ja koodistoon tulee turhia kutsuja. Kasvata kokoa tai jaa välimuisti useaan.
- `service/impl/AmosaaClientImpl.java:46-48`: `@Cacheable` voi tallentaa rajattoman sivutuksen tuloksen. Lisää sivumäärän yläraja ja `unless`-ehto.
- Virhetulosten välimuistitus: ks. kohta 6.4.

### 8.4 Dokumentit ja vienti, keskitaso

- `service/export/LampiExportService.java` (noin rivit 137, 190, 208): `queryForList` koko taululle ja S3-objekti `asByteArray()`-kutsulla muistiin. Käytä `JdbcTemplate.query` ja `RowCallbackHandler` -yhdistelmää sekä virtautettua latausta.
- `service/impl/ExternalPdfServiceImpl.java`: koko `PerusteKaikkiDto` sarjallistetaan merkkijonoksi ennen lähetystä. Virtauta, jos perusteiden koko kasvaa.

---

## 9. Domain-malli ja persistointi

### 9.1 Rikkinäiset equals/hashCode-toteutukset, korkea

| Luokka | Ongelma |
|---|---|
| `domain/yl/Oppiaine.java:314-365` | `equals` vertaa `tunniste`-kenttää, `hashCode` käyttää `System.identityHashCode`-arvoa. Yhtä suuret oliot päätyvät eri hash-lokeroihin (myös `oppimaarat`-joukossa) |
| `domain/arviointi/ArvioinninKohdealue.java:96-116` | `equals` vertaa `id`-kenttää, `hashCode` ei. `hashCode` laskee arvon muuttuvasta kokoelmasta |
| `domain/ammattitaitovaatimukset/AmmattitaitovaatimuksenKohdealue.java:62-77` | `equals` perustuu `id`-kenttään, `hashCode`-metodia ei ole. Kaikki tallentamattomat oliot (`id == null`) ovat keskenään yhtä suuria |
| `domain/tutkinnonrakenne/TutkinnonOsaViite.java:70-90` | `hashCode` laskee arvon muuttuvista viitteistä, vaikka olio on `Suoritustapa.tutkinnonOsat`-joukossa (`LinkedHashSet`). Kentän muutos rikkoo joukon |
| `domain/arviointi/Arviointi.java:90-107`, `domain/OsaamistasonKriteeri.java:91-105` | Syvä sisältövertailu ja kokoelmiin perustuva `hashCode` muuttuvalle entiteettiverkolle |

**Korjaus:** käytä entiteeteissä identiteetti- tai id-pohjaista vertailua (`id != null && id.equals(o.id)` ja luokkakohtainen vakio-`hashCode`). Sisältövertailuun on jo `structureEquals`. Kirjoita testit, jotka lisäävät entiteetin joukkoon, muuttavat sitä ja poistavat sen.

### 9.2 Kaskadit ja liitokset, keskitaso

- `domain/tutkinnonosa/TutkinnonOsa.java:67-68`: `koodi` on EAGER ja `CascadeType.ALL`. Koodi-entiteetti voi olla jaettu, jolloin poisto tai merge kaskadoituu väärään paikkaan. Käytä LAZY-hakua ja vain `PERSIST`/`MERGE`-kaskadia.
- `domain/Peruste.java:145-150`: `osaamisalat` `CascadeType.ALL` ilman `orphanRemoval`-asetusta, ja EAGER. Poistetut koodit voivat jäädä orvoiksi riveiksi.
- Muita `CascadeType.ALL` ilman `orphanRemoval`-asetusta: `GeneerinenArviointiasteikko:56-59`, `ArviointiAsteikko:32`, `PerusteenOsaViite.lapset:154`, `TutkinnonOsa`-luokan listat (rivit 70, 85, 94, 104).
- 36 EAGER-liitosta `domain/`-hakemistossa. Pahimmat: `Peruste.java:126-178` (`koulutukset`, `osaamisalat`, `suoritustavat`) ja `TekstiPalanen.java:28`.
- Kokoelmat palautetaan muokattavina Lombokin `@Getter`-metodeilla (esimerkiksi `ArvioinninKohdealue.getArvioinninKohteet`, rivit 78-79), jolloin käänteinen puoli ei synkronoidu. `OsaamistasonKriteeri.getKriteerit` palauttaa jo muokkauskelvottoman näkymän; käytä samaa mallia.

### 9.3 Kloonaus jakaa viitteitä, keskitaso

- `service/impl/PerusteServiceImpl.java:2375-2388`: perusteen kloonaus jakaa `Koulutus`- ja osaamisalaentiteetit alkuperäisen kanssa.
- `domain/yl/Oppiaine.java` (`kloonaa`, noin rivi 378) jakaa `nimi`- ja `tehtava`-tekstipalaset.
- `domain/ammattitaitovaatimukset/AmmattitaitovaatimuksenKohdealue.java:54-55` jakaa `otsikko`-kentän.

Jos jaettu olio muuttuu (tai jos se ei ole muuttumaton `TekstiPalanen`), muutos näkyy molemmissa perusteissa. Varmista, ovatko kyseiset tyypit käytännössä muuttumattomia. Jos eivät, tee syväkopio.

### 9.4 Repository-kerros, keskitaso/matala

- Noin 32 `getOne()`-kutsua. Metodi on vanhentunut, eikä se koskaan palauta `null`-arvoa, joten puuttuva entiteetti huomataan vasta laiskan latauksen yhteydessä `EntityNotFoundException`-poikkeuksena. Esimerkiksi `service/impl/OsaAlueServiceImpl.java:51-54` tarkistaa `getOne`-tuloksen `null`-arvon turhaan. Korvaa kutsulla `findById` tai tietoisella `getReferenceById`-kutsulla.
- `repository/version/JpaWithVersioningRepository.java:68-70`: `findOne` palauttaa `null`, mikä on NPE-riskien (kohta 5.4) päälähde.
- `repository/liite/impl/LiiteRepositoryImpl.java:22-28`: `IOUtils.toByteArray(is)` lukee koko tiedoston muistiin, eikä `length`-parametria käytetä. Käytä `BlobProxy.generateProxy(is, length)`.
- `repository/dialect/JsonBType.java`: ei käytössä. `nullSafeGet` palauttaa aina `null`, `hashCode` aina 0, ja luokassa on kaksi `printStackTrace`-kutsua. Poista luokka.
- `repository/custom/TekstiPalanenRepositoryCustomImpl.java`: `@Deprecated`, ja siinä on huomautuksia Hibernate 6 -ongelmista. Kirjoita uudelleen JPQL:llä tai poista.

### 9.5 Muut, matala

- `domain/JulkaistuPerusteData.java:40-42`: `@PrePersist` asettaa `hash = data.hashCode()`. `JsonNode.hashCode()` on 32-bittinen eikä ole tarkoitettu sisällön tunnisteeksi. Jos hashia käytetään muuttumattoman sisällön tunnistamiseen, törmäys voi johtaa väärään päätelmään. Käytä SHA-256-tiivistettä.
- `java.util.Date` 34 entiteettikentässä. Siirry pitkällä aikavälillä `java.time`-tyyppeihin.
- Java-migraatio `db/migration/V0_20230503101700__julkaisu_dokumentit_fix.java:56` upottaa kannasta luetun aikaleiman SQL-merkkijonoon. Migraatio on jo ajettu, eikä sitä pidä muuttaa. Käytä uusissa migraatioissa aina sidottuja parametreja.
- Java-migraatiot `V0_98`, `V0_104` ja `V0_161` kirjoittavat kaikki rivit uudelleen ilman ehtoa. Flyway ajaa ne vain kerran, mutta lisää uusiin vastaaviin `WHERE ... IS NULL` -ehto.

---

## 10. Suorituskyky

- **Koko taulun lataukset:** `PerusteRepository.findAllPerusteet` ja `findJulkaistutPerusteet` sekä niiden käyttö kohdissa `PerusteServiceImpl.java:329-341` (`getKooste`), `PerusteServiceImpl.java:371-372` (`getAllInfo`), `AmmattitaitovaatimusServiceImpl.java:111-117` ja `MaintenanceServiceImpl.java:108`. Kaikki perusteet ladataan muistiin EAGER-kokoelmineen. Käytä projektiokyselyjä ja sivutusta.
- **EAGER-kokoelmat:** ks. kohta 9.2. Vaihda oletukseksi LAZY ja käytä `@EntityGraph`-annotaatiota tai DTO-projektioita. Tee muutos vaiheittain ja mittaa, koska LAZY voi paljastaa `LazyInitializationException`-virheitä.
- **Tiedostot muistissa:** `LiiteRepositoryImpl:22-28`, `DokumenttiServiceImpl.get` (koko PDF `byte[]`-taulukkona), `ExternalPdfServiceImpl`, `LampiExportService`. Käsittele virtoina (`StreamingResponseBody`, `BlobProxy`, `RowCallbackHandler`).
- **N+1-HTTP-kutsut:** `KoulutusalaServiceImpl:37-44`, ja koodiston relaatiohaut pienen välimuistin kanssa (kohta 8.3).
- **Indeksit:** migraatio `V0_20260329153400__tutkinnonosaviite_index.sql` lisäsi puuttuvan vierasavainindeksin. Käy läpi muutkin usein käytetyt vierasavaimet (`peruste_id`, liitostaulut) `EXPLAIN ANALYZE` -komennolla. Kohdan 6.1 uniikkiehto toimii samalla indeksinä hauille `(peruste_id, revision)`.

---

## 11. Koodin laatu ja testaus

### 11.1 Koodin laatu, matala

Suurimmat luokat (riviä):

| Luokka | Rivit |
|---|---:|
| `service/impl/PerusteServiceImpl` | 2842 |
| `service/impl/PerusteprojektiServiceImpl` | 1075 |
| `service/mapping/DtoMapperConfig` | 971 |
| `service/impl/JulkaisutServiceImpl` | 805 |
| `service/impl/PerusteenOsaServiceImpl` | 752 |
| `service/impl/validators/ValidatorPeruste` | 736 |
| `domain/Peruste` | 704 |
| `service/security/PermissionManager` | 646 |

Muut mittarit:

- TODO- ja FIXME-merkintöjä 14. Kirjaa tiketeiksi tai poista.
- `printStackTrace`-kutsuja 3 (`JsonBType` kaksi, `JulkaisutServiceImpl:580`). `System.out`-kutsuja ei ole.
- Tyhjiä `catch`-lohkoja vähintään 6 (kohta 6.4).
- Ohjaimet kutsuvat repositoryja suoraan, esimerkiksi `ExternalController` tutkinnon osan haussa. Siirrä kutsut palvelukerrokseen, jotta oikeustarkistus ja transaktiot hoituvat yhdessä paikassa.
- `aop/PerformanceMonitorAspect` kirjaa aina INFO-tasolla. Lisää kynnysarvo (esimerkiksi yli 500 ms) ja käytä oletuksena DEBUG-tasoa.
- `service/impl/PerusteServiceImpl.java:1342-1357`: vanhentunut `checkIfKoulutuksetAlreadyExists`. Poista, jos sitä ei käytetä.

### 11.2 Testaus, keskitaso

Nykytila: 82 testitiedostoa. `@Disabled`- tai `@Ignore`-merkittyjä testejä ei ole, eikä testeissä käytetä `Thread.sleep`-kutsuja.

Puutteet ja toimenpiteet:

1. **Oikeustestit:** `ControllerPermissionIT` testaa nyt yhden julkisen GET-reitin sekä POST-pyyntöjen 401/403-vastaukset. Laajenna testi käymään läpi kaikki `RequestMappingHandlerMapping`-rekisterin reitit kirjautumattomana ja varmistamaan, että vain eksplisiittisesti julkisiksi merkityt palauttavat 200. Tämä estää kohtien 3.1, 3.2, 7.3 ja 7.4 kaltaiset regressiot.
2. **Validaattorit:** jokaiselle `Validator`-toteutukselle testi, joka varmistaa virheen palautuvan (kohdat 3.3, 4.3 ja 4.4).
3. **Domain-logiikka:** yksikkötestit `TutkinnonOsa.structureEquals`- ja `AIPEVaihe.validateChange`-metodeille (kohdat 4.1 ja 4.2) sekä equals/hashCode-sopimukselle (kohta 9.1).
4. **Julkaisu:** integraatiotestit `JulkaisutServiceImpl.teeJulkaisu`- ja `PerusteprojektiServiceImpl.updateTila`-poluille, mukaan lukien virhepolut: projektia ei ole, validointi epäonnistuu, PDF epäonnistuu, diaarinumero puuttuu ja samanaikainen julkaisu.
5. **Testaamattomat palvelut:** `JulkaisutService`, `SkeduloituajoService` ja ajastetut tehtävät, `LiiteService`/`LiiteTiedostoService`, `KommenttiService`, `LokalisointiService`, `GeneerinenArviointiasteikkoService` ja lukitus.
6. **Siivous:** käyttämätön `import org.junit.Ignore` seitsemässä tiedostossa: `ControllerPermissionIT`, `Lops2019ServiceIT`, `MappingTestIT`, `PerusteenOsaServiceIT`, `PerusteenOsaViiteIT`, `PerusteenRakenneIT` ja `PerusteprojektiServiceTilaIT`.
7. **Kattavuusraportti:** lisää JaCoCo CI-putkeen ja aseta vähimmäisraja uudelle koodille.

---

## 12. Parannusideat

- **Oletuksena kielletty suodatinketju.** Julkiset reitit yhteen eksplisiittiseen listaan (tai omaan annotaatioonsa, jonka perusteella suodatinketju konfiguroidaan), ja kohdan 11.2 automaattinen oikeustesti varmistamaan, ettei uusia avoimia reittejä synny huomaamatta.
- **`findByIdOrThrow`-apumetodi** repository-kerrokseen (esimerkiksi oletusmetodi yhteisessä perusrajapinnassa), joka heittää `NotExistsException`-poikkeuksen. Korvaa sillä `findById(...).orElse(null)`- ja `findOne`-kutsut vaiheittain. Tämä poistaa suurimman osan kohdan 5.4 riskeistä.
- **Validaattoreiden yhteinen pohjaluokka**, joka hakee projektin (heittää, jos sitä ei ole), luo `Validointi`-oliot ja palauttaa ne. Aliluokat toteuttavat vain tarkistukset. Näin kohdan 3.3 virhe ei voi toistua.
- **Julkaisun tilakone.** Julkaisun tila (`JULKAISEMATON`, `KESKEN`, `JULKAISTU`, `VIRHE`) ja projektin tila päivitetään yhdessä paikassa, asynkroninen työ käynnistetään commitin jälkeen, ja revisiolle on uniikkiehto (kohdat 6.1 ja 6.2).
- **Jaettu HTTP-asiakas** aikakatkaisuilla ja uudelleenyrityksillä, ja välimuistiannotaatioihin yhtenäinen `unless`-käytäntö.
- **ShedLock** ajastettuihin tehtäviin (kohta 8.1).
- **Koodityylisäännöt:** `Boolean.TRUE.equals(...)` ja `Objects.equals(...)` mahdollisesti `null`-arvoisille kentille, ei tyhjiä `catch`-lohkoja, ei `printStackTrace`-kutsuja. Valvo esimerkiksi Error Prone- tai SpotBugs-työkalulla CI:ssä.
- **Mappaus:** Orika (`service/mapping/DtoMapperConfig.java`, versio 1.5.4) ei ole enää ylläpidossa, ja siinä on tunnettuja ongelmia Java 21:n ja Hibernate-proxyjen kanssa (`OptionalSupport.java:25` TODO). Siirry vaiheittain MapStructiin, aloittaen julkaisun ja PDF:n poluista.
- **Suurten luokkien jako:** jaa `PerusteServiceImpl` vastuualueittain (haku ja listaus, rakenne, kloonaus, tuonti ja vienti, julkaistun sisällön luku).
- **Aikatyypit:** siirtymä `java.util.Date` -tyypistä `java.time`-tyyppeihin, ja Joda-Timen (`DateTime.now()`) poisto samalla.

---

## 13. Tarkistetut väitteet, jotka eivät ole ongelmia

Nämä nousivat esiin katselmoinnissa, mutta käsin tarkistettaessa ne todettiin toimiviksi. Ne kirjataan, jotta niitä ei selvitetä uudelleen.

- **`JulkaisutServiceImpl.taytaPerusteet` hylkää `julkaisut.map(...)`-tuloksen** (`service/impl/JulkaisutServiceImpl.java:562-573`). Spring Datan `Page.map` suoritetaan heti, ja lambda muokkaa samoja olioita, jotka palautetaan. Perusteet siis täyttyvät oikein. Koodi on kuitenkin harhaanjohtavaa: vaihda `map` `getContent().forEach(...)`-kutsuksi (matala).
- **Kommenttien muokkaus ja poisto ilman omistajatarkistusta.** `KommenttiServiceImpl.update` ja `delete` (rivit 150-166) kutsuvat `SecurityUtil.allow(kommentti.getLuoja())` ja tarkistavat projektin lukuoikeuden. Vain kommentin kirjoittaja voi muokata sitä. Lukumetodien heikko suojaus (kohta 7.4) on silti todellinen ongelma.
- **Julkaisun virhetila katoaa rollbackissa.** `JulkaisuPerusteTilaServiceImpl.saveJulkaisuPerusteTila` (rivi 24) käyttää `Propagation.REQUIRES_NEW`-asetusta, joten `VIRHE`-tila tallentuu, vaikka julkaisun transaktio perutaan. `teeJulkaisuAsync` ajetaan luokkatason `@Transactional`-annotaation ansiosta omassa transaktiossaan, joten sen sisäiset muutokset (esimerkiksi projektin tila rivillä 301) perutaan virhetilanteessa. Ongelma on vain kutsujan `updateTila`-metodissa (kohta 6.2).
- **Tietoturvakonteksti asynkronisissa julkaisuissa.** `julkaisuTaskExecutor` (`config/AsyncConfig.java:21`) on kääritty `DelegatingSecurityContextAsyncTaskExecutor`-luokkaan, joten `SecurityContextHolder`-kutsu (`JulkaisutServiceImpl:313`) saa oikean käyttäjän.
- **SQL-injektio natiivikyselyissä.** Kohtaa 7.2 lukuun ottamatta tarkistetut natiivikyselyt (muun muassa `JulkaistuPerusteDataStore`) käyttävät sidottuja parametreja.
