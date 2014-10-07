ePerusteet
======================
Opetushallituksen ePerusteet-palvelu tutkintojen ja yleissivistävän koulutuksen opetussuunnitelmien perusteiden laadintaan ja julkaisuun.

Palvelukortti
-------------
<https://confluence.csc.fi/display/oppija/ePerusteet>

Kehitysympäristön pystytys
--------------------------

- JDK 7
- Maven
- Nodejs sekä yo front-end-kehitystä varten
  - <http://nodejs.org/download/>
  - (sudo) npm -g install yo
- PostgreSQL 9.3 (luo tietokanta paikallista kehitystä varten)
- Tomcat [7.0.42,8)

Riippuvuuksien takia käännösaikana tarvitaan pääsy sisäiseen pakettien hallintaan, koska osa paketeista (lähinnä build-parent) ei ole julkisissa repoissa.

Ajoaikana riippuu mm. keskitetystä autentikaatiosta (CAS), käyttäjähallinnasta, organisaatiopalvelusta ja koodistosta joihin täytyy olla ajoympäristöstä pääsy.

Ajaminen paikallisesti
----------------------

eperusteet-app: 

    cd eperusteet/eperusteet-app/yo
    npm install
    bower install
    grunt server

eperusteet-service: 

    cd eperusteet/eperusteet-service
    mvn tomcat7:run -Deperusteet.devdb.user=<user> -Deperusteet.devdb.password=<password> -Deperusteet.devdb.jdbcurl=<jdbcurl>

Sovelluksen voi myös kääntää kahdeksi eri war-paketiksi joita voi aja erillisessä Tomcatissa. 

- Kehitysympäristön tomcatin konfiguraatioon tarvitaan seuraavat muutokset:
  - URIEncoding="UTF-8": `<Connector port="8080" protocol="HTTP/1.1" (...) URIEncoding="UTF-8"/>`
  - PostgreSQL 9.3 JDBC-ajuri lib-hakemistoon
  - Kehityskannan resurssi:
    `<GlobalNamingResources>...
    <Resource name="jdbc/eperusteet" auth="Container" type="javax.sql.DataSource"
                 maxActive="100" maxIdle="30" maxWait="10000"
                 username="..." password="..." driverClassName="org.postgresql.Driver"
                 url="jdbc:postgresql://localhost:5432/..."/>
    ...</GlobalNamingResources>`

Web-sovelluksen (app) osalta maven käyttää yeoman-maven-pluginia joka tarvitsee nodejs:n ja yo:n toimiakseen.

Windows-kehittäjät huom! ko. plugin yrittää ajaa npm, bower, ja grunt -komentoja jotka Windowsissa ovat komentosarjatiedostoja (esim. npm.cmd). Näiden pitää olla polussa, lisäksi vastaava shell-skripti ilman lopuketta kannattaa muuttaa vaikka .sh päätteiseksi, muuten maven-pluginin suoritus päättyy virheeseen.

