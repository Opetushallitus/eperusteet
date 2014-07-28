create table idTeksti (id bigint);
insert into idTeksti values (nextval('hibernate_sequence'));
create table id (id bigint);
insert into id values (nextval('hibernate_sequence'));
create table perusteId (id bigint);
insert into perusteId values (nextval('hibernate_sequence'));
create table koulutusId (id bigint);
insert into koulutusId values (nextval('hibernate_sequence'));
create table suoritustapaId (id bigint);
insert into suoritustapaId values (nextval('hibernate_sequence'));
create table perusteenosaId (id bigint);
insert into perusteenosaId values (nextval('hibernate_sequence'));
create table suoritustapasisaltoId (id bigint);
insert into suoritustapasisaltoId values (nextval('hibernate_sequence'));
create table lapsetOrder (lapset_order integer);
insert into lapsetOrder values (0);


/*----- Luodaan peruste --------------*/
insert into tekstipalanen(id) select id from idTeksti;
insert into tekstipalanen_teksti(tekstipalanen_id, kieli, teksti) select id, 'FI' as kieli, 'Yleinen sisältö perustutkinto' as teksti from idTeksti;
insert into tekstipalanen_teksti(tekstipalanen_id, kieli, teksti) select id, 'SV' as kieli, '[Yleinen sisältö perustutkinto]' as teksti from idTeksti;
insert into koulutus(id, koulutus_koodi, koulutusala_koodi, opintoala_koodi) select id.id, nextval('hibernate_sequence'), 'koulutusalaoph2002_2', 'opintoalaoph2002_202' from koulutusId as id;
insert into peruste(nimi_id, tutkintokoodi, paivays, id, siirtyma, koodiuri) select idT.id, 'koulutustyyppi_1', current_timestamp, id.id, null, 'koodiUri' from idTeksti as idT, perusteId as id;
insert into peruste_koulutus(peruste_id, koulutus_id) select perusteId.id, koulutusId.id from perusteId, koulutusId;
insert into perusteenosaviite(id, perusteenosa_id, vanhempi_id, lapset_order) select id.id, null, null, null from suoritustapasisaltoId as id, perusteenosaId;
insert into suoritustapa(id, suoritustapakoodi, sisalto_perusteenosaviite_id) select id.id, 'OPS', suoritustapasisaltoId.id from suoritustapaId as id, suoritustapasisaltoId;
insert into peruste_suoritustapa(peruste_id, suoritustapa_id) select perusteId.id, suoritustapaId.id from perusteId, suoritustapaId;

delete from idTeksti;
delete from id;

/* -----Luodaan tekstikappale ---------------- */
insert into idTeksti values (nextval('hibernate_sequence'));
insert into id values (nextval('hibernate_sequence'));

insert into tekstipalanen(id) select id from idTeksti;
insert into tekstipalanen_teksti(tekstipalanen_id, kieli, teksti) select idTeksti.id, 'FI' as kieli, 'Elinikäisen oppimisen avaintaidot' as teksti from idTeksti;
insert into perusteenosa(id, luotu, muokattu, nimi_id, luoja, muokkaaja) select id.id, null as luotu, null as muokattu, idT.id, null as luoja, null as muokkaaja from id as id, idTeksti as idT;

delete from idTeksti;
insert into idTeksti values (nextval('hibernate_sequence'));
insert into tekstipalanen(id) select id from idTeksti;
insert into tekstipalanen_teksti(tekstipalanen_id, kieli, teksti) select idTeksti.id, 'FI' as kieli, 
'<p>Elinikäisen oppimisen avaintaidoilla tarkoitetaan osaamista, jota tarvitaan jatkuvassa oppimisessa, tulevaisuuden ja uusien tilanteiden
 haltuunotossa sekä työelämän muuttuvissa olosuhteissa selviytymisessä. Ne ovat tärkeä osa ammattitaitoa ja kuvastavat yksilön älyllistä 
 notkeutta ja erilaisista tilanteista selviytymistä. Ne lisäävät kaikilla aloilla tarvittavaa ammattisivistystä ja kansalaisvalmiuksia, 
 ja niiden avulla opiskelijat tai tutkinnon suorittajat pystyvät seuraamaan yhteiskunnassa ja työelämässä tapahtuvia muutoksia ja toimimaan muuttuvissa oloissa.
 Niillä on myös suuri merkitys yksilön elämän laatuun ja persoonallisuuden kehittymiseen.</p>

<p>Elinikäisen oppimisen avaintaidot sisältävät edellisen ammatillisen peruskoulutuksen opetussuunnitelman ja näyttötutkinnon perusteiden yhteisten
 painotusten ja kaikille aloille yhteisen ydinosaamisen lisäksi perusopetuksen ja lukion aihekokonaisuuksia sekä Euroopan parlamentin ja neuvoston
 suosituksia 2005/0221 (COD) elinikäisen oppimisen avaintaidoiksi.</p>

<p>Elinikäisen oppimisen avaintaidot sisältyvät ammattitaitoa täydentävien tutkinnon osien (yhteisten opintojen) tavoitteisiin ja ammatillisten tutkinnon osien
ammattitaitovaatimuksiin ja niiden arviointikriteereihin. Erikseen arvioitava elinikäisen oppimisen avaintaidon arvioinnin kohde sisältää seuraavat
 elinikäisen oppimisen avaintaidot: oppiminen ja ongelmanratkaisu, vuorovaikutus ja yhteistyö, ammattietiikka sekä terveys, turvallisuus ja toimintakyky.</p>

<b>Oppiminen ja ongelmanratkaisu</b>
<p>Opiskelija tai tutkinnon suorittaja suunnittelee toimintaansa sekä kehittää itseään ja työtään. Hän arvioi omaa osaamistaan. 
Hän ratkaisee työssään ongelmia sekä tekee valintoja ja päätöksiä. Hän toimii työssään joustavasti, innovatiivisesti ja uutta luovasti.
 Hän hankkii tietoa, jäsentää, arvioi ja soveltaa sitä.</p>

<b>Vuorovaikutus ja yhteistyö</b>
<p>Opiskelija tai tutkinnon suorittaja toimii tilanteen vaatimalla tavalla erilaisissa vuorovaikutustilanteissa sekä ilmaisee
 erilaisia näkökantoja selkeästi, rakentavasti ja luottamusta herättäen. Hän toimii yhteistyökykyisesti erilaisten ihmisten kanssa ja
 työryhmän jäsenenä sekä kohtelee erilaisia ihmisiä tasavertaisesti. Hän noudattaa yleisesti hyväksyttyjä käyttäytymissääntöjä ja toimintatapoja.
 Hän hyödyntää saamaansa palautetta toiminnassaan.</p>

<b>Ammattietiikka</b>
<p>Opiskelija tai tutkinnon suorittaja toimii työssään ammatin arvoperustan mukaisesti. Hän sitoutuu työhönsä ja toimii vastuullisesti noudattaen tehtyjä sopimuksia 
ja ammattiinsa kuuluvaa etiikkaa.</p>

<b>Terveys, turvallisuus ja toimintakyky</b>
<p>Opiskelija tai tutkinnon suorittaja toimii turvallisesti ja vastuullisesti työ- ja vapaa-aikana sekä liikenteessä ja ylläpitää
 terveellisiä elintapoja sekä toiminta- ja työkykyään. Hän työskentelee ergonomisesti ja hyödyntää alallaan tarvittavan terveysliikunnan 
sekä ehkäisee työhön ja työympäristöön liittyviä vaaroja ja terveyshaittoja.</p>

<b>Aloitekyky ja yrittäjyys</b>
<p>Opiskelija tai tutkinnon suorittaja edistää toiminnallaan tavoitteiden saavuttamista. Hän toimii aloitteellisesti ja asiakaslähtöisesti 
työntekijänä ja/tai yrittäjänä. Hän suunnittelee toimintaansa ja työskentelee tavoitteiden saavuttamiseksi. Hän toimii taloudellisesti ja
 tuloksellisesti ja johtaa itseään. Hän mitoittaa oman työnsä tavoitteiden mukaan.</p>

<b>Kestävä kehitys</b>
Opiskelija tai tutkinnon suorittaja toimii ammattinsa kestävän kehityksen ekologisten, taloudellisten, sosiaalisten sekä kulttuuristen periaatteiden mukaisesti. Hän noudattaa alan työtehtävissä keskeisiä kestävän kehityksen säädöksiä, määräyksiä ja sopimuksia.

<b>Estetiikka</b>
<p>Opiskelija tai tutkinnon suorittaja ottaa toiminnassaan huomioon oman alansa esteettiset tekijät. Hän edistää 
tai ylläpitää työympäristönsä viihtyisyyttä ja muuta esteettisyyttä.</p>

<b>Viestintä ja mediaosaaminen</b>
<p>Opiskelija tai tutkinnon suorittaja viestii monimuotoisesti ja vuorovaikutteisesti tilanteeseen sopivalla tavalla hyödyntäen kielitaitoaan.
 Opiskelija tai tutkinnon suorittaja havainnoi, tulkitsee sekä arvioi kriittisesti erilaisia mediatuotteita.
 Hän käyttää mediaa ja viestintäteknologiaa sekä tuottaa media-aineistoja.</p>

<b>Matematiikka ja luonnontieteet</b>
<p>Opiskelija tai tutkinnon suorittaja käyttää peruslaskutoimituksia työssä vaadittavien ja arkipäivän laskutehtävien ratkaisemisessa.
 Hän käyttää esim. kaavoja, kuvaajia, kuvioita ja tilastoja ammattitehtävien ja -ongelmien ratkaisemisessa.
 Opiskelija tai tutkinnon suorittaja soveltaa fysiikan ja kemian lainalaisuuksiin perustuvia menetelmiä ja toimintatapoja työssään.</p>

<b>Teknologia ja tietotekniikka</b>
<p>Opiskelija tai tutkinnon suorittaja hyödyntää ammatissa käytettäviä teknologioita monipuolisesti. Hän ottaa työssään huomioon tekniikan hyödyt,
 rajoitukset ja riskit. Hän käyttää tietotekniikkaa monipuolisesti ammatissaan.</p>

<b>Aktiivinen kansalaisuus ja eri kulttuurit</b>
<p>Opiskelija tai tutkinnon suorittaja osallistuu rakentavalla tavalla yhteisön toimintaan ja päätöksentekoon.
 Hän toimii oikeuksiensa ja velvollisuuksiensa mukaisesti sekä työssä että arkielämässä. Hän noudattaa tasa-arvo- ja yhdenvertaisuuslakeja.
 Hän toimii asiallisesti ja työelämän vaatimusten mukaisesti eri kulttuuritaustan omaavien ihmisten kanssa kotimaassa ja kansainvälisissä toiminnoissa.</p>'
as teksti from idTeksti;

insert into tekstikappale(id, teksti_id) select id.id, idT.id from id as id, idTeksti as idT;
/*---- Laitetaan tekstikappale suoritustapasisällön lapseksi----*/
insert into perusteenosaviite(id, perusteenosa_id, vanhempi_id, lapset_order) select nextval('hibernate_sequence'), id.id, vanhempiId.id, lapsetOrder.lapset_order from suoritustapasisaltoId as vanhempiId, id as id, lapsetOrder;

update lapsetOrder set lapset_order = lapset_order + 1;

delete from idTeksti;
delete from id;

/*--------- Luodaan tekstikappale -----------*/
insert into idTeksti values (nextval('hibernate_sequence'));
insert into id values (nextval('hibernate_sequence'));

insert into tekstipalanen(id) select id from idTeksti;
insert into tekstipalanen_teksti(tekstipalanen_id, kieli, teksti) select idTeksti.id, 'FI' as kieli, 'Opetussuunnitelman laadinta ja sisältö' as teksti from idTeksti;
insert into perusteenosa(id, luotu, muokattu, nimi_id, luoja, muokkaaja) select id.id, null as luotu, null as muokattu, idT.id, null as luoja, null as muokkaaja from id as id, idTeksti as idT;

delete from idTeksti;
insert into idTeksti values (nextval('hibernate_sequence'));

insert into tekstipalanen(id) select id from idTeksti;
insert into tekstipalanen_teksti(tekstipalanen_id, kieli, teksti) select idTeksti.id, 'FI' as kieli, 
'<p>Ammatillisesta koulutuksesta annetun lain (630/1998, 14 §) mukaan koulutuksen järjestäjän tulee hyväksyä koulutusta varten opetussuunnitelma, 
jonka tulee perustua tässä asiakirjassa oleviin perustutkinnon perusteisiin. Sen tulee sisältää toimenpiteet koulutukselle asetettujen tehtävien ja 
tavoitteiden saavuttamiseksi (L 630/1998, 5 §). Opetussuunnitelma tulee hyväksyä erikseen suomen-, ruotsin- ja saamenkieliseen koulutukseen sekä tarvittaessa
muulla kielellä annettavaan koulutukseen. Koulutuksen järjestäjän opetussuunnitelma on julkinen asiakirja. Opetussuunnitelma säätelee ja ohjaa koulutuksen 
järjestäjän toteuttamaa koulutusta ja opetukseen läheisesti liittyvää muuta toimintaa. Opetussuunnitelma on laadittava siten, että se mahdollistaa 
opiskelijoille yksilölliset ammatillisten opintojen valinnat sekä lukio-opintojen ja ylioppilastutkinnon suorittamisen. Opetussuunnitelma toimii myös 
sisäisen ja ulkoisen arvioinnin pohjana ja antaa mahdollisuuden arvioida koulutuksen järjestäjän toteuttaman koulutuksen vaikuttavuutta.
</p>

<p>Opiskelijan oikeusturvan ja oppimisympäristön turvallisuuden varmistamiseksi opetussuunnitelman tulee antaa opiskelijalle
 riittävästi tietoa tutkintoon sisältyvistä tutkinnon osista, opinnoista, arvioinnista, opintojen suorittamista edistävistä
 opinto-ohjauksen ja opiskelijahuollon tuki- ja ohjausjärjestelyistä sekä tutkintokohtaisista terveydentilan ja toimintakyvyn 
vaatimuksista ja muista opintoihin liittyvistä edellytyksistä.</p>

<p>Koulutuksen järjestäjä varaa koulutukseen tarvittavat voimavarat. Koulutuksen järjestäjä huolehtii opetussuunnitelmassa siitä,
 että opiskelija voi saavuttaa tutkinnolle asetetut tavoitteet, saa riittävästi opetusta ja tarvitsemaansa ohjausta koulutuksen 
järjestämistavasta riippumatta oppilaitoksen kaikkina työpäivinä, myös työssäoppimisen ja ammattiosaamisen näyttöjen aikana.</p>

<p>Opetussuunnitelma sisältää kaikkia koulutusaloja ja tutkintoja varten yhteisen osan ja tutkintokohtaisesti eriytyvät osat.</p>

<b>Opetussuunnitelman yhteinen osa</b>
<p>Opetussuunnitelman yhteisessä osassa määritellään kaikille perustutkinnoille yhteiset periaatteet ja menettelytavat sekä koulutuksen järjestäjän keskeiset arvot.</p>

<p>Opetussuunnitelman yhteinen osa sisältää ainakin
<ul>
<li>koulutuksen järjestämisen ammatillisena peruskoulutuksena, työpaikalla käytännön työtehtävien yhteydessä järjestettävänä koulutuksena ja oppisopimuskoulutuksena (L 630/1998, 3 §, 15 §, 17 §)</li>
<li>koulutuksen järjestämisen lähi-, etä-, monimuoto-opetuksena (L 630/1998, 15 §) ja verkko-opetuksena</li>
<li>suunnitelmat ja toimintatavat tutkinnon osan tai osien suorittamiseksi sekä opiskelijoiden mahdollisuudet täydentää opintojaan ja suorittaa koko tutkinto</li>
<li>opintojen tarjonnan yhteistyössä muiden koulutuksen järjestäjien ja työelämän kanssa (L 630/1998, 14 §, 10 §)</li>
<li>toimenpiteet opetukseen liittyvästä yhteisöllisyyttä vahvistavasta toiminnasta, joka tarjoaa mahdollisuuden arvopohdintaan ja kulttuuriperintöön perehtymiseen (A 811/1998, 9 §)</li>
<li>yhteiset toimintatavat opiskelijan arvioinnin (L 601/2005, 25a §) toteuttamisesta luvun 7 mukaisesti</li>
<li>toimintatavat, miten tutkintokohtaisista terveydentilan ja toimintakyvyn vaatimuksista ja muista opintoihin liittyvistä edellytyksistä tiedotetaan opiskelijoiksi hakeutuville (L 951/2011, 27 a §)</li>
<li>periaatteet, miten koulutuksen järjestäjä toimii opiskeluoikeuden peruuttamista, opiskeluoikeuteen liittyvää tiedonsaantia ja opiskeluoikeuden palauttamista koskevissa asioissa (L 951/2011, 32 §, 32 a § ja 32 b §) valtioneuvoston asetuksen (1032/2011) mukaisten koulutusalojen ja tutkintojen erityispiirteet huomioon ottaen, ja miten se ohjeistaa opiskeluoikeuden peruuttamisesta ja palauttamisesta päättävän koulutuksen järjestäjän asettaman toimielimen toimintaa ja päätöksentekoa sekä mahdollisesti muiden koulutuksen järjestäjien kanssa tehtävää toimielinyhteistyötä (L 951/2011, 35 a §)</li>
<li>toimintatavat, miten tiedotetaan opiskelijaksi hakeutuville koulutusaloista ja tutkinnoista, joissa voidaan edellyttää rikosrekisteriotteen pyytämistä (L 951/2011, 32 a § 4 mom) sekä ohjeet, miten ja milloin opiskelija toimittaa rikosrekisteriotteen koulutuksen järjestäjälle nähtäväksi (L 955/2011, 6 §)</li>
<li>toimintatavat, miten ja milloin tiedotetaan opiskelijoille muutoksenhaun tekemisestä opiskelijan oikeusturvalautakuntaan (L 956/2011)  opiskeluoikeuden peruuttamista tai palauttamista koskevissa asioissa valtioneuvoston asetuksessa (1032/2011, 16 §) määriteltyjen koulutusalojen tutkinnoissa</li>
<li>toimintatavat, miten ja milloin tiedotetaan opiskelijoille muutoksenhaun tekemisestä opiskelijan oikeusturvalautakuntaan (L 956/2011) opiskeluoikeuden peruuttamista tai palauttamista koskevissa asioissa valtioneuvoston asetuksessa (1032/2011, 16 §) määriteltyjen koulutusalojen tutkinnoissa</li>
<li>luvun 8 Muut määräykset ammatillisessa peruskoulutuksessa toteuttamisen edellyttämät seuraavat kohdat:
-	8.1 Opinto-ohjaus ja henkilökohtainen opiskelusuunnitelma (Opinto-ohjauksen tavoitteet,</li>
<li>opiskelijan oikeus opinto-ohjaukseen, Opinto-ohjauksen järjestäminen ja Henkilökohtainen opiskelusuunnitelma)</li>
<li>henkilöstön kehittämissuunnitelman.</li>
</ul>
</p>
<b>Opetussuunnitelman tutkintokohtainen osa</b>
<p>Opetussuunnitelman tutkintokohtaisessa osassa määrätään ammatillisten tutkinnon osien ja ammattitaitoa täydentävien tutkinnon osien
 (yhteisten opintojen) järjestäminen yhteistyössä muiden koulutuksen järjestäjien ja työelämän kanssa. Lisäksi määrätään opetuksen ajoitus,
 oppimisympäristöt ja opetusmenetelmät, joiden avulla opiskelija voi saavuttaa tutkinnon ammattitaitovaatimukset ja tavoitteet.</p>

<p>Tutkintokohtaisessa osassa määrätään myös koulutuksen järjestäjän tarjoamat opinnot muista tutkinnoista sekä opiskelijan mahdollisuudet
 suorittaa useampia kuin yksi tutkinto. Siinä päätetään ammatillisten ja ammattitaitoa täydentävien (yhteisten opintojen) arviointisuunnitelma,
 joka sisältää ammattiosaamisen näytöt ja muun osaamisen arvioinnin.</p>

<p>Opetussuunnitelman tutkintokohtainen osa sisältää ainakin
<ul>
<li>tutkinnon muodostumisen pakollisista ja valinnaisista ammatillisista tutkinnon osista ja ammattitaitoa täydentävistä tutkinnon osista</li>
<li>opintojen etenemisen, ajoituksen ja järjestämisen</li>
<li>vapaasti valittavien tutkinnon osien tarjonnan</li>
<li>suunnitelman ammatillista osaamista yksilöllisesti syventävien tutkinnon osien (perustutkintoa laajentavien tutkinnon osien) järjestämisestä</li>
<li>suunnitelman tutkinnon osien arvioinnista ja osaamisen arviointimenetelmistä</li>
<li>suunnitelman ammatillisten tutkinnon osien arvioinnista siten, että se sisältää toimielimen hyväksymän suunnitelman ammattiosaamisen näyttöjen toteuttamisesta ja arvioinnista</li>
<li>paikallisesti tarjottavat tutkinnon osat, niiden ammattitaitovaatimukset, arvioinnin kohteet ja arviointikriteerit sekä ammattitaitoa täydentävien pakollisten tutkinnon osien valinnaisten lisäosien tavoitteet, arvioinnin kohteet ja arviointikriteerit.</li>
</ul>
</p>
<b>Henkilökohtainen opiskelusuunnitelma</b>
<p>Laissa ammatillisesta koulutuksesta (L 630/1998, 14 §) on säädetty opiskelijan mahdollisuudesta yksilöllisiin opintojen valintoihin
 ja asetuksessa ammatillisesta koulutuksesta (A 811/1998, 3 §, 4 §, 12a §) opinnoista tiedottamisesta ja opinto-ohjauksesta ja osaamisen
 tunnustamisesta. Jotta opiskelijan yksilöllinen valinnaisuus toteutuu, koulutuksen järjestäjän tulee laatia opiskelijan yksilöllisten lähtökohtien
 pohjalta henkilökohtainen opiskelusuunnitelma (HOPS), jota päivitetään koko koulutuksen ajan.</p>
'
 as teksti from idTeksti;

insert into tekstikappale(id, teksti_id) select id.id, idT.id from id as id, idTeksti as idT;
/*---- Laitetaan tekstikappale suoritustapasisällön lapseksi----*/

insert into perusteenosaviite(id, perusteenosa_id, vanhempi_id, lapset_order) select nextval('hibernate_sequence'), id.id, vanhempiId.id, lapsetOrder.lapset_order from suoritustapasisaltoId as vanhempiId, id as id, lapsetOrder;

update lapsetOrder set lapset_order = lapset_order + 1;

delete from idTeksti;
delete from id;


/*--------- Luodaan tekstikappale -----------*/
insert into idTeksti values (nextval('hibernate_sequence'));
insert into id values (nextval('hibernate_sequence'));

insert into tekstipalanen(id) select id from idTeksti;
insert into tekstipalanen_teksti(tekstipalanen_id, kieli, teksti) select idTeksti.id, 'FI' as kieli, 'Opinto-ohjaus ja henkilökohtainen opiskelusuunnitelma' as teksti from idTeksti;
insert into perusteenosa(id, luotu, muokattu, nimi_id, luoja, muokkaaja) select id.id, null as luotu, null as muokattu, idT.id, null as luoja, null as muokkaaja from id as id, idTeksti as idT;

delete from idTeksti;
insert into idTeksti values (nextval('hibernate_sequence'));

insert into tekstipalanen(id) select id from idTeksti;
insert into tekstipalanen_teksti(tekstipalanen_id, kieli, teksti) select idTeksti.id, 'FI' as kieli, 
'
<b>Opinto-ohjauksen tavoitteet</b>
<p>Opinto-ohjauksen tavoitteena on, että opiskelija saa riittävästi tietoa koulutuksestaan ennen sen aloitusta ja sen aikana.
 Opiskelijan tulee tietää tutkintoonsa sisältyvät tutkinnon osat ja opinnot ja niiden valinnan mahdollisuudet. 
Lisäksi tavoitteena on, että opiskelija osaa toimia oppilaitosyhteisössään, osaa kehittää opiskelu- ja vuorovaikutustaitojaan ja 
itsetuntemustaan sekä arvioida omaa toimintaansa ja tuotoksiaan.
Hän osaa suunnitella opintonsa, laatia henkilökohtaisen opiskelusuunnitelmansa yhteistyössä opettajan kanssa ja ottaa vastuun opinnoistaan.
 Hän osaa seurata opintosuoritusten kertymistä ja hakea tukea opintojensa suunnitteluun.
</p>

<p>Opiskelija osaa tehdä koulutusta ja elämänuraa koskevia valintoja ja ratkaisuja. Hän tunnistaa opiskeluunsa ja
 elämäntilanteisiinsa mahdollisesti liittyviä ongelmia ja osaa hakea niihin tukea. Hän osaa hakea ja saa hyvissä ajoin ja
 riittävästi tietoa opiskelemaansa tutkintoon liittyvistä terveydentilan ja toimintakyvyn vaatimuksista, muista opintoihin 
liittyvistä edellytyksistä ja tarjolla olevista ohjaus- ja tukimuodoista.
</p>

<b>Opiskelijan oikeus opinto-ohjaukseen</b>
<p>Tutkintoon sisältyy opinto-ohjausta vähintään 1,5 opintoviikkoa (VN:n päätös 213/1999).
 Ohjaustoiminnalla tulee tukea opiskelijaa kokonaisvaltaisesti opintojen eri vaiheissa. Jokaisella opiskelijalla on 
oikeus saada henkilökohtaista ja muuta opintojen ohjausta. Opiskelijalla on oikeus saada opiskelussa tai
 elämäntilanteiden muutoksissa tarvitsemiaan tukipalveluita.</p>

<b>Opinto-ohjauksen järjestäminen</b>
<p>Opinto-ohjauksen järjestämisen tavoitteena on edistää koulutuksellista, etnistä ja sukupuolten välistä tasa-arvoa. 
Tavoitteena on lisäksi lisätä opiskelijoiden hyvinvointia, ehkäistä opintojen keskeyttämistä, edistää työllistymistä 
ja tukea jatko-opintoihin hakeutumista. Koulutuksen järjestäjän on kiinnitettävä huomiota erityisesti niiden 
opiskelijoiden ohjaukseen, joilla on opiskelu- tai oppimisvaikeuksia (esimerkiksi luki-häiriö), poissaoloja koulutuksesta 
tai elämänhallintaan liittyviä vaikeuksia.</p>

<p>Koulutuksen järjestäjä laatii opetussuunnitelmaansa opinto-ohjaussuunnitelman, jossa kuvataan ohjaukseen 
osallistuvien tehtävät ja työnjako. Suunnitelma toimii koko oppilaitoksen ohjaustyön kehittämisen välineenä. 
Siinä määritellään, miten ja minkälaista tukea opiskelija saa ohjaustoimintaan osallistuvilta. Suunnitelmassa
 esitetään, miten yhteistyö eri koulutuksen järjestäjien kanssa on järjestetty, jotta opiskelija voi valita tutkinnon 
osia ja opintoja eri koulutusohjelmista ja tutkinnoista sekä suunnitella useamman kuin yhden tutkinnon suorittamista.
 Siinä määritellään myös yhteistyö oppilaitoksen ulkopuolisten asiantuntijoiden ja huoltajien kanssa. Opiskelijahuoltosuunnitelma on osa ohjaussuunnitelmaa.
</p>

<p>Ohjaustoimintaan osallistuvat kaikki oppilaitoksen opettajat sekä muut ohjauksesta vastuulliset. 
Opinto-ohjaajalla on päävastuu opinto-ohjauksen järjestämisestä sekä ohjauksen kokonaisuuden suunnittelusta
 ja toteutuksesta. Opettajan tehtävänä on ohjata ja motivoida opiskelijaa tutkinnon suorittamisessa ja
 opintojen suunnittelussa. Hänen tehtävänsä on myös auttaa opiskelijaa löytämään vahvuuksiaan ja kehittämään oppimisen valmiuksiaan.</p>

<p>Opinto-ohjausta järjestetään opintoihin liittyvänä, henkilökohtaisena, ryhmäohjauksena ja muuna ohjauksena.
 Opiskelija saa opinto-ohjausta opiskelunsa tueksi ja valintojen tekemiseen, jotta hän kykenee suunnittelemaan opiskelujensa 
sisällön ja rakenteen omien voimavarojensa mukaisesti. Opinto-ohjauksella edistetään opiskelijoiden yhteisöllisyyttä koko koulutuksen
 ajan. Opiskelijoiden opiskelua ja hyvinvointia seurataan ja tuetaan yhteistyössä huoltajien kanssa.</p>

<p>Koulutuksen järjestäjän tulee tiedottaa ammatillisesta koulutuksesta ja siihen hakeutumisesta perusopetuksen oppilaille, 
heidän huoltajilleen, oppilaanohjaajille ja opettajille. Koulutuksen järjestäjän tulee suunnata tiedotusta ja ohjausta erityisesti
 perusopetuksen päättymisen jälkeen koulutuksen ulkopuolelle jääneille nuorille ja heidän huoltajilleen.</p>

<p>Koulutuksen järjestäjän tulee kehittää ura- ja rekrytointipalvelujaan yhteistyössä elinkeinoelämän ja työvoimapalveluiden
 kanssa sekä edistää ja tukea opiskelijoiden työllistymistä ja jatkokoulutukseen pääsemistä.</p>

<b>Henkilökohtainen opiskelusuunnitelma</b>
<p>Henkilökohtainen opiskelusuunnitelma tukee opiskelijan urasuunnittelua ja kehittää hänen valmiuksiaan itsearvioinnissa. 
Se perustuu opiskelijan oman opiskelun suunnitteluun, yksilöllisiin valintoihin, opinnoissa etenemiseen ja oppimisen arviointiin. 
Opiskelijaa ohjataan henkilökohtaisen opiskelusuunnitelman laadinnassa ja sen toteutumisen seurannassa. Se on suunnitelma, 
jonka toteuttamiseen opiskelija sitoutuu ja motivoituu koko koulutuksen ajaksi.</p>

<p>Henkilökohtaisen opiskelusuunnitelman laadintaan osallistuvat opettaja tai opettajat ja tarvittaessa opinto-ohjaaja 
opiskelijan kanssa yhdessä neuvotellen. Suunnitelmassa määritellään oppimisen tavoitteet, opintojen suorittaminen, suoritustavat
 ja ajoitus sekä opintojen arviointi. Laadinnassa otetaan huomioon opiskelijoiden erilaiset oppimistyylit, terveydentila,
 toimintakyky, yksilölliset voimavarat ja mahdollisen erityisen tuen tai apuvälineiden tarve. Mahdolliset oppimista vaikeuttavat 
seikat tunnistetaan ja opiskelijan itseohjautuvuutta ja ammatillista kasvua ohjataan ja tuetaan.</p>

<p>Henkilökohtaisen opiskelusuunnitelman toteutumista ja opintojen edistymistä seurataan, ja tarvittaessa opiskelijalle annetaan tukiopetusta.
 Opiskelija ja opettajat arvioivat henkilökohtaisen opiskelusuunnitelman toteuttamisen mahdolliset esteet. Opiskelijaa ohjataan
 tekemään omaa oppimistaan koskevia päätöksiä sekä tarvittaessa tarkentamaan ja muuttamaan suunnitelmaa opintojen edetessä.</p>

<p>Henkilökohtainen opiskelusuunnitelma sisältää opiskelijan yksilölliset valinnat, opinnoissa etenemisen,
 oppimisen arvioinnin, opiskelijan osaamisen tunnistamisen ja tunnustamisen, työssäoppimisen paikat ja ajat sekä ammattiosaamisen näytöt.
</p>
'
 as teksti from idTeksti;

insert into tekstikappale(id, teksti_id) select id.id, idT.id from id as id, idTeksti as idT;
/*---- Laitetaan tekstikappale suoritustapasisällön lapseksi----*/

insert into perusteenosaviite(id, perusteenosa_id, vanhempi_id, lapset_order) select nextval('hibernate_sequence'), id.id, vanhempiId.id, lapsetOrder.lapset_order from suoritustapasisaltoId as vanhempiId, id as id, lapsetOrder;

update lapsetOrder set lapset_order = lapset_order + 1;

delete from idTeksti;
delete from id;


/*--------- Luodaan tekstikappale -----------*/
insert into idTeksti values (nextval('hibernate_sequence'));
insert into id values (nextval('hibernate_sequence'));

insert into tekstipalanen(id) select id from idTeksti;
insert into tekstipalanen_teksti(tekstipalanen_id, kieli, teksti) select idTeksti.id, 'FI' as kieli, 'Työssäoppiminen ja työturvallisuus' as teksti from idTeksti;
insert into perusteenosa(id, luotu, muokattu, nimi_id, luoja, muokkaaja) select id.id, null as luotu, null as muokattu, idT.id, null as luoja, null as muokkaaja from id as id, idTeksti as idT;

delete from idTeksti;
insert into idTeksti values (nextval('hibernate_sequence'));

insert into tekstipalanen(id) select id from idTeksti;
insert into tekstipalanen_teksti(tekstipalanen_id, kieli, teksti) select idTeksti.id, 'FI' as kieli, 
'
<p>Sen lisäksi, mitä asetuksessa ammatillisesta koulutuksesta (A 811/1998, 5 §, VNA muutos 603/2005, 3 ja 5 §) 
on säädetty, tulee työssäoppimisessa noudattaa seuraavaa:</p>

<p>Työssäoppiminen on osa ammatillista koulutusta. Se on koulutuksen järjestämismuoto, jossa osa 
tutkinnon tavoitteista opitaan työpaikalla työtä tehden.</p>

<p>Työssäoppiminen on aidossa työympäristössä tapahtuvaa tavoitteellista, ohjattua ja arvioitua opiskelua. 
Työssäoppimisjaksojen tulee olla ammatinhallinnan kannalta riittävän pitkiä ja monipuolisia. Vain poikkeustapauksessa 
opiskelija voi suorittaa työssäoppimisen oppilaitoksen harjoitusyrityksessä tai vastaavin järjestelyin.</p>

<p>Työpaikkojen ja koulutuksen järjestäjien yhteistyöllä varmistetaan työssäoppimisen ja muun ammatillisen
 koulutuksen työelämävastaavuus, laatu ja ajantasaisuus. Koulutuksen järjestäjän vastuulla on huolehtia,
 että kaikilla alueen toimijoilla on yhteinen käsitys työssäoppimisen järjestämisestä. Koulutuksen järjestäjän 
tulee huolehtia, että opiskelija saa riittävästi ohjausta ja opetusta työssäoppimisen aikana ja että opettajilla 
ja muulla henkilöstöllä on edellytykset yhteistyölle työelämän kanssa. Koulutuksen järjestäjän ja opettajien tulee
 yhdessä työ- ja elinkeinoelämän kanssa varmistaa työssäoppimisen laatu, jotta opiskelija saavuttaa tutkinnon perusteiden ammattitaitovaatimukset.</p>

<p>Työssäoppimisen toteutuksesta vastaa koulutuksen järjestäjä. Toteutukseen sisältyy suunnittelua, opiskelijan ohjausta
 ja arviointia. Lisäksi koulutuksen järjestäjän tehtävänä on huolehtia opettajien työelämäosaamisesta ja kouluttamisesta
 sekä työpaikkaohjaajien kouluttamisesta. Työpaikalla kiinnitetään erityistä huomiota opiskelijan ohjaukseen ja palautteen antamiseen.</p>

<p>Työssäoppimisen aikana opiskelija ei yleensä ole työsuhteessa työnantajaan, eikä hänelle makseta palkkaa
 työssäoppimisjaksojen aikana. Työssäoppimisjakson aikana opiskelija on oikeutettu saamaan opintotuen ja
 opintososiaaliset edut niistä erikseen annettujen ohjeiden mukaan. Kun työssäoppiminen toteutetaan ulkomailla,
 järjestämisessä otetaan huomioon myös paikalliset määräykset.</p>

<p>Sen lisäksi, mitä laissa ammatillisesta koulutuksesta (L 630/1998, 19 §, 28 §) ja voimassa olevissa työturvallisuussäädöksissä 
on säädetty, työturvallisuusasioissa noudatetaan seuraavaa:</p>

<p>Sopimuksessa työpaikalla käytännön työtehtävien yhteydessä järjestettävästä koulutuksesta ja 
ammattiosaamisen näytöistä on kirjattava turvallisuuteen, tapaturmiin ja vahingonkorvauksiin liittyvät 
vastuut ja vakuutukset. Ennen työn aloittamista työnantaja ja koulutuksen järjestäjä varmistavat yhdessä,
 että opiskelijalla on edellytykset tehdä ko. työtä turvallisesti ja terveyttään vaarantamatta sekä ohjeita noudattaen.</p>

<p>Koulutuksen järjestäjän tulee ottaa edellä mainitussa sopimuksessa huomioon viranomaisten
 toiminnan julkisuudesta annetun lain (621/1999) säännökset. Ammatillisesta koulutuksesta annetun 
lain mukaan tiedon haltijalla on oikeus salassapitosäännösten estämättä antaa opiskelijan terveydentilaa 
ja toimintakykyä koskevia ja tehtävien hoidon kannalta välttämättömiä tietoja työssäoppimisesta vastaaville
 henkilöille opiskelijan sekä työssäoppimispaikan henkilöstön ja asiakkaiden turvallisuuden varmistamiseksi (L 951/2011, 43 §).</p>

<p>Koulutuksen järjestäjään sovelletaan työturvallisuuslain 4 §:n 1 momentin mukaan työantajaa
 koskevia säännöksiä silloin, kun työ tapahtuu oppilaitoksessa tai muutoin koulutuksen järjestäjän 
osoittamalla tavalla.</p>

<p>Opiskelijan arvioinnista työssäoppimisjaksoilla on määrätty luvussa 7.</p>
'
 as teksti from idTeksti;

insert into tekstikappale(id, teksti_id) select id.id, idT.id from id as id, idTeksti as idT;
/*---- Laitetaan tekstikappale suoritustapasisällön lapseksi----*/

insert into perusteenosaviite(id, perusteenosa_id, vanhempi_id, lapset_order) select nextval('hibernate_sequence'), id.id, vanhempiId.id, lapsetOrder.lapset_order from suoritustapasisaltoId as vanhempiId, id as id, lapsetOrder;

update lapsetOrder set lapset_order = lapset_order + 1;

delete from idTeksti;
delete from id;


/*--------- Luodaan tekstikappale -----------*/
insert into idTeksti values (nextval('hibernate_sequence'));
insert into id values (nextval('hibernate_sequence'));

insert into tekstipalanen(id) select id from idTeksti;
insert into tekstipalanen_teksti(tekstipalanen_id, kieli, teksti) select idTeksti.id, 'FI' as kieli, 'Ammatillinen erityisopetus' as teksti from idTeksti;
insert into perusteenosa(id, luotu, muokattu, nimi_id, luoja, muokkaaja) select id.id, null as luotu, null as muokattu, idT.id, null as luoja, null as muokkaaja from id as id, idTeksti as idT;

delete from idTeksti;
insert into idTeksti values (nextval('hibernate_sequence'));

insert into tekstipalanen(id) select id from idTeksti;
insert into tekstipalanen_teksti(tekstipalanen_id, kieli, teksti) select idTeksti.id, 'FI' as kieli, 
'
<p>Koulutuksellisen tasa-arvon toteutumiseksi jokaisella opiskelijalla tulee olla erilaisista 
oppimisedellytyksistä riippumatta yhdenvertaiset mahdollisuudet osallistua ammatilliseen koulutukseen
 sekä sijoittua koulutuksen jälkeen työhön ja yhteiskuntaan täysivaltaisena kansalaisena. Ammatillisen 
erityisopetuksen tulee tarjota opiskelijoille mahdollisimman esteetön ja saavutettava ammatillinen koulutus
 ja edellytykset koulutuksen jälkeiseen työllistymiseen. Tutkintokohtaisista terveydentilan ja toimintakyvyn 
vaatimuksista ja muista opintoihin liittyvistä edellytyksistä tulee tiedottaa opiskelijaksi hakeutuvalle ja 
hänen huoltajalleen.</p>

<p>Opiskelijaksi hakeutuvan terveydentilaan tai toimintakykyyn liittyvä seikka ei saa olla esteenä 
opiskelijaksi ottamiselle. Ammatillisessa erityisopetuksessa tulee opiskelijan terveydentilaa ja 
toimintakykyä tukea ja edistää opiskelijan yksilöllisten tarpeiden mukaisilla tuki- ja ohjaustoimilla,
 monipuolisilla ja vaihtelevilla oppimisen ja osaamisen arviointimenetelmillä sekä muilla toimintatavoilla
 ja erityisjärjestelyillä, jotka edistävät opiskelijan opintojen onnistumista. Opiskelijaksi ei kuitenkaan 
voida ottaa henkilöä, joka ei ole terveydentilaltaan tai toimintakyvyltään kykenevä opintoihin liittyviin 
käytännön tehtäviin tai työssäoppimiseen, jos valtioneuvoston asetuksessa (1032/2011) määrättyjen humanistisen
 ja kasvatusalan, tekniikan ja liikenteen alan, luonnonvara- ja ympäristöalan sekä sosiaali-, terveys- ja
 liikunta-alan tutkintojen opintoihin liittyvät turvallisuusvaatimukset sitä edellyttävät, ja jos estettä ei
 voida kohtuullisin toimin poistaa. Edellä mainittujen tutkintojen osalta on terveydentilan ja toimintakyvyn
 vaatimukset sisällytetty ammatillisten perustutkintojen perusteisiin.</p>

<p>Erityistä tukea tarvitsevan opiskelijan ammatillinen koulutus tulee toteuttaa yhdenvertaisuusperiaatteen 
mukaan ensisijaisesti tavallisessa ammatillisessa oppilaitoksessa samoissa opetusryhmissä muiden opiskelijoiden 
kanssa. Opetus voidaan järjestää myös osittain tai kokonaan erityisryhmässä. Ammatilliset erityisoppilaitokset
 huolehtivat ensisijaisesti vaikeavammaisten koulutuksesta sekä valmentavasta ja kuntouttavasta opetuksesta ja
 ohjauksesta. Lisäksi niiden tulee tarjota asiantuntija-apua muille oppilaitoksille. Ammatillista erityisopetusta
 voidaan järjestää myös oppisopimuskoulutuksena.</p>

<p>Vammaisuuden, sairauden, kehityksessä viivästymisen, tunne-elämän häiriön tai muun syyn vuoksi erityisiä
 opetus- tai opiskelijahuoltopalveluja tarvitsevien opiskelijoiden tulee saada erityisopetusta. Sen avulla
 turvataan henkilökohtaisiin edellytyksiin perustuva oppiminen, itsensä kehittäminen ja ihmisenä kasvaminen.
 Erityisopetukseen liitetään tarvittaessa muita tukitoimia ja kuntoutusta yhteistyössä kuntoutuspalvelujen tuottajien kanssa.</p>

<p>Koulutuksen järjestäjä määrittelee erityisopetuksen periaatteet: tavoitteet, toteutuksen, opetusmenetelmät,
 tuki- ja erityispalvelut, asiantuntijapalvelut, yhteistyötahot ja vastuut. Oppilaitoksen tulee varata erityisopetukseen
 riittävät voimavarat. Erityistä tukea tarvitsevien opiskelijoiden oppimisen edistäminen on koko oppilaitosyhteisön tehtävä.</p>

<p>Erityisopetuksen tarve on määriteltävä ammatillisen koulutuksen lain 20 §:n ja opetussuunnitelman perusteiden 
pohjalta jokaiselle opiskelijalle yksilöllisesti. Tavoitteiden saavuttamista on tuettava yksilöllisesti suunnitellun
 ja ohjatun oppimisprosessin sekä erilaisten tukitoimien avulla.</p>

<b>Henkilökohtainen opetuksen järjestämistä koskeva suunnitelma (HOJKS)</b>
<p>
Erityisopetusta tarvitsevalle opiskelijalle on laadittava aina kirjallinen henkilökohtainen opetuksen järjestämistä koskeva suunnitelma (HOJKS) (L 630/1998, 20 §). Suunnitelman tulee sisältää (A 811/1998, 8 § mukaan)
<ul>
<li>suoritettava tutkinto</li>
<li>opetuksessa noudatettavat tutkinnon perusteet</li>
<li>tutkinnon laajuus</li>
<li>opiskelijalle laadittu henkilökohtainen opetussuunnitelma</li>
<li>opiskelijan saamat erityiset opetus- ja opiskelijahuollon palvelut</li>
<li>muut henkilökohtaiset palvelu- ja tukitoimet sekä</li>
<li>erityisopetuksen perustelut.</li>
</ul>
HOJKS tulee laatia opiskelijan, tarvittaessa hänen huoltajansa, aikaisemman koulun edustajien sekä opettajien ja opiskelijahuollon asiantuntijoiden kanssa.
</p>
<p>
Mikäli ammattitaitovaatimuksia on mukautettu, henkilökohtaiseen opetuksen järjestämistä koskevaan
 suunnitelmaan sisältyy henkilökohtainen opetussuunnitelma, jossa määritellään opiskelijan yksilölliset 
oppimisen tavoitteet. Ne perustuvat hänen opiskelemansa tutkinnon perusteisiin. Ammatillisessa erityisopetuksessa 
opetus on suunniteltava siten, että opiskelija mahdollisimman suuressa määrin saavuttaa saman pätevyyden kuin muussa
 ammatillisessa koulutuksessa. Tavoitteita voidaan mukauttaa opiskelijan edellytysten mukaan joko niin, että kaiken 
opetuksen tavoitteet on mukautettu, tai mukauttamalla vain yhden tai sitä useamman tutkinnon osan tavoitteet. Opetuksessa
 tulee keskittyä opiskelijan vahvojen osaamisalueiden tukemiseen, jotta hänelle taataan hyvät mahdollisuudet
 sijoittua työhön. Erityistä huomiota tulee kiinnittää työssä harjaantumiseen työssäoppimisen jaksojen
aikana. Opiskelijalle tulee selvittää, miten hän voi koulutuksen jälkeen saada tarvitsemiaan erityispalveluja.</p>

<p>Opiskelijan edistymistä tulee seurata koulutuksen aikana, ja henkilökohtaisia tavoitteita ja tukitoimia 
on muutettava tarpeen mukaan. Määräykset erityisopiskelijoiden arvioinnista ovat luvussa 7.10.</p>
'
 as teksti from idTeksti;

insert into tekstikappale(id, teksti_id) select id.id, idT.id from id as id, idTeksti as idT;
/*---- Laitetaan tekstikappale suoritustapasisällön lapseksi----*/

insert into perusteenosaviite(id, perusteenosa_id, vanhempi_id, lapset_order) select nextval('hibernate_sequence'), id.id, vanhempiId.id, lapsetOrder.lapset_order from suoritustapasisaltoId as vanhempiId, id as id, lapsetOrder;

update lapsetOrder set lapset_order = lapset_order + 1;

delete from idTeksti;
delete from id;


/*--------- Luodaan tekstikappale -----------*/
insert into idTeksti values (nextval('hibernate_sequence'));
insert into id values (nextval('hibernate_sequence'));

insert into tekstipalanen(id) select id from idTeksti;
insert into tekstipalanen_teksti(tekstipalanen_id, kieli, teksti) select idTeksti.id, 'FI' as kieli, 'Maahanmuuttajien ja eri kieli- ja kulttuuriryhmien opetus' as teksti from idTeksti;
insert into perusteenosa(id, luotu, muokattu, nimi_id, luoja, muokkaaja) select id.id, null as luotu, null as muokattu, idT.id, null as luoja, null as muokkaaja from id as id, idTeksti as idT;

delete from idTeksti;
insert into idTeksti values (nextval('hibernate_sequence'));

insert into tekstipalanen(id) select id from idTeksti;
insert into tekstipalanen_teksti(tekstipalanen_id, kieli, teksti) select idTeksti.id, 'FI' as kieli, 
'
<p>Maahanmuuttajaopiskelijoiden ja muiden kieli- ja kulttuuriryhmien opiskelijoiden, kuten saamenkielisten,
 romanien ja viittomakielisten opiskelijoiden ammattitaitovaatimukset ovat pääsääntöisesti samat kuin muidenkin
 opiskelijoiden. Opetuksessa noudatetaan ammatillisen perustutkinnon perusteita.</p>

<p>Opiskelijoita, joiden äidinkieli on muu kuin oppilaitoksen opetuskieli, tulee tukea etenkin kielten
 opinnoissa ja erityisin opetusjärjestelyin. Opetuksessa otetaan tarvittaessa huomioon opiskelijoiden tausta, 
kuten äidinkieli, kulttuuri ja koulutuksen aikana kehittyvä kielitaito. Opetusjärjestelyillä tuetaan opiskelijoiden 
omaa kielellistä identiteettiä enemmistökielen ja -kulttuurin rinnalla. Koulutuksen järjestäjän opetussuunnitelmaan
 sisältyy maahanmuuttajien ja eri kieli- ja kulttuuriryhmien opiskelijoiden opetusjärjestelyjen toteuttaminen.</p>

<b>Maahanmuuttajat</b>
<p>Maahanmuuttajilla tarkoitetaan tässä yhteydessä Suomeen muuttaneita ja Suomessa syntyneitä maahanmuuttajataustaisia 
opiskelijoita. Opetuksessa otetaan tarvittaessa huomioon maahanmuuton syy, maassaoloaika sekä kehittyvä suomen kielen taito.
 Opinnot tukevat opiskelijan kasvua sekä suomalaisen että hänen oman kieli- ja kulttuuriyhteisönsä aktiiviseksi ja tasapainoiseksi jäseneksi. </p>

<b>Saamenkieliset</b>
<p>Perusopetuksessa aloitettua saamenkielistä opetusta jatketaan mahdollisuuksien mukaan ammatillisessa koulutuksessa.
 Saamenkielistä opetusta voidaan antaa pohjois-, inarin- ja koltansaamen kielillä. Saamen kieltä voi opiskella äidinkielenä,
 vaikka opetusta ei muutoin tarjottaisi saamen kielellä.</p>

<b>Romanit</b>
<p>Romanien opetus toteutetaan ottamalla huomioon Suomen romanien asema etnisenä ja kulttuurisena vähemmistönä.
 Opetusjärjestelyissä otetaan huomioon romanien kulttuuri. Romanikielen opetusta järjestetään mahdollisuuksien
 mukaan yhteistyössä muiden koulutuksen järjestäjien kanssa.</p>

<b>Viittomakieliset</b>
<p>Viittomakielisten opiskelijoiden opetuksessa ja opiskelussa sovelletaan ammatillisen perustutkinnon perusteita
 viittomakieliseen kulttuuriin ja viestintään. Viittomakielen rinnalla käytetään suomea tai ruotsia luku- ja kirjoituskielenä.
 Viittomakielisten opetus suunnitellaan niin, että opiskelijan on mahdollista toimia tulkin kanssa. Oppimisympäristöissä otetaan 
huomioon viittomakielisen tai huonokuuloisen mahdollisuus kommunikoida luontevasti.</p>

<b>Kieltenopetuksen järjestelyt</b>
<p>Seuraavia valtioneuvoston päätöksen (VnP 213/1999) mukaisia opetusjärjestelyjä voidaan 
soveltaa maahanmuuttajien, saamenkielisten, romanien ja viittomakielisten kielten opinnoissa 
(äidinkieli, toinen kotimainen kieli, vieras kieli), jos heidän äidinkielensä on muu kuin suomi tai ruotsi.</p>

<b>Äidinkieli</b>
<p>Jos opiskelijan äidinkieli on muu kuin suomi tai ruotsi, voi koulutuksen järjestäjä jakaa
 ammatillisesta koulutuksesta annetun lain 12 § 2. momentissa säädetyt äidinkielen ja toisen kotimaisen 
kielen pakolliset tutkinnon osat säädetystä poikkeavalla tavalla.</p>

<p>Äidinkielen ja toisen kotimaisen kielen tutkinnon osiin varatut opintoviikot (4 + 1 = 5 ov) 
voidaan yhdistää ja jakaa joustavasti mahdollisiin opiskelijan oman äidinkielen opintoihin, suomi toisena 
kielenä -opintoihin ja toisen kotimaisen kielen opintoihin. Suomi toisena kielenä tarkoittaa kieltä, joka on
 opittu äidinkielen jälkeen suomenkielisessä ympäristössä. Viittomakielisiä opiskelijoita varten on 
laadittu erikseen suomi viittomakielisille -tutkinnon osa (luku 5).</p>

<p>Opiskelijat voivat opiskella suomen kieltä joko
<ol>
<li>suomi toisena kielenä -tavoitteiden mukaisesti (luku 5) tai</li>
<li>äidinkieli, suomi -tavoitteiden mukaisesti (luku 5), jos opiskelijan suomen kielen
taidon arvioidaan olevan äidinkielisen tasoinen.</li>
</ol>
</p>

<p>Opiskelija, jonka suomen kielen taito ei ole äidinkielisen tasoista kaikilla kielitaidon osa-alueilla,
 opiskelee suomi toisena kielenä -tavoitteiden ja sisältöjen mukaisesti. Hänen suomen kielen osaamisensa 
arvioidaan näiden tavoitteiden mukaisesti huolimatta siitä, onko koulutuksen järjestäjä tarjonnut suomi
 toisena kielenä -opetusta. Koulutuksen järjestäjä voi tarjota ja opiskelijan opiskelusuunnitelmaan voi
kuulua molempia edellä mainituista opinnoista. Opiskelija voi siirtyä kesken suomi toisena kielenä -opintojen 
opiskelemaan suomen kieltä äidinkieli, suomi -tavoitteiden mukaisesti.</p>

<p>Opiskelijoille tulee mahdollisuuksien mukaan järjestää myös hänen oman äidinkielensä opetusta.
 Äidinkielenä voidaan opiskelijan valinnan mukaan (L 630/1998, 12 § 3 mom.) opettaa myös romanikieltä,
 viittomakieltä tai muuta opiskelijan äidinkieltä. Maahanmuuttajien oman äidinkielen tavoitteet ovat luvussa 5.
 Opiskelija voi opiskella omaa äidinkieltään joko äidinkieli, oma äidinkieli vieraskielisille opiskelijoille (4 ov) 
tai vieras kieli (2 ov) -tavoitteiden mukaisesti tai vapaasti valittavina tutkinnon osina.</p>

<p>Jos opiskelija opiskelee äidinkieltä oma äidinkieli vieraskielisille opiskelijoille -tavoitteiden mukaisesti,
 on hänen opintoihinsa kuuluttava suomen kielen opintoja.</p>

<b>Toinen kotimainen kieli</b>
<p>Opiskelijoiden toisen kotimaisen kielen (ruotsi) opetus järjestetään toisen kotimaisen kielen tavoitteiden
 mukaisesti ottaen huomioon opiskelijoiden kielitaidon taso.</p>

<p>Toisen kotimaisen kielen opinnot voidaan myös korvata oman äidinkielen ja suomen kielen opinnoilla 
vieraskielisillä opiskelijoilla (5 ov), luku 5. Tarvittaessa opetus voidaan järjestää toisen kotimaisen 
kielen alkeisopetuksena opiskelijan ja alan tarpeista riippuen.</p>

<b>Vieras kieli</b>
<p>Opiskelijan opintoihin on kuuluttava myös vieraan kielen opintoja. Muuta kuin suomea tai ruotsia 
äidinkielenään puhuvan opiskelijan vieras kieli voi olla myös hänen äidinkieltään.</p>
'
 as teksti from idTeksti;

insert into tekstikappale(id, teksti_id) select id.id, idT.id from id as id, idTeksti as idT;
/*---- Laitetaan tekstikappale suoritustapasisällön lapseksi----*/

insert into perusteenosaviite(id, perusteenosa_id, vanhempi_id, lapset_order) select nextval('hibernate_sequence'), id.id, vanhempiId.id, lapsetOrder.lapset_order from suoritustapasisaltoId as vanhempiId, id as id, lapsetOrder;

update lapsetOrder set lapset_order = lapset_order + 1;

delete from idTeksti;
delete from id;

/*--------putsaus-----------------*/
DROP TABLE idTeksti;
DROP TABLE id;
DROP TABLE perusteId;
DROP TABLE suoritustapaId;
DROP TABLE perusteenosaId;
DROP TABLE suoritustapasisaltoId;
DROP TABLE koulutusId;
DROP TABLE lapsetOrder;
