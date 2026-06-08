package fi.vm.sade.eperusteet.dto.peruste;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import fi.vm.sade.eperusteet.domain.Kieli;
import fi.vm.sade.eperusteet.dto.KoulutusDto;
import fi.vm.sade.eperusteet.dto.tutkinnonosa.TutkinnonOsaKaikkiDto;
import fi.vm.sade.eperusteet.dto.tutkinnonrakenne.KoodiDto;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@Schema(description = "Julkaistun perusteen tai tutkinnon osan yhteenvetotiedot julkisesta perustehakurajapinnasta.")
public class PerusteenJulkaisuData {

    @Schema(description = "Perusteen tai tutkinnon osan tunniste. Kun sisältötyyppi on peruste, kyseessä on perusteen id; "
            + "kun sisältötyyppi on tutkinnonosa, kyseessä on tutkinnon osan id.")
    private Long id;

    @Schema(description = "Perusteen tai tutkinnon osan nimi kielikohtaisena karttana (avain on kielikoodi, esim. fi, sv, en). "
            + "Haussa valittu kieli ohjaa, millä kielellä nimi näytetään ja millä kielellä nimivertailu tehdään.")
    private Map<String, String> nimi;

    @Schema(description = "Perusteen alkuperäinen voimaantulon alkamispäivä. Tulevat perusteet ovat sellaisia, "
            + "joiden voimaantuloajankohta on hakuhetkeä myöhempi.")
    private Date voimassaoloAlkaa;

    @Schema(description = "Perusteen virallisen voimassaolon päättymispäivä. Voimassaolon päättymisen jälkeen peruste "
            + "voi olla vielä siirtymäajalla, kunnes siirtymäpäättymispäivä on mennyt.")
    private Date voimassaoloLoppuu;

    @Schema(description = "Voimassaolon loppumisen jälkeinen siirtymäajan päättymispäivä. Peruste on siirtymäajalla, "
            + "kun virallinen voimassaolo on päättynyt mutta tämä päivämäärä ei ole vielä mennyt.")
    private Date siirtymaPaattyy;

    @Schema(description = "Perusteen määräyksen päätöspäivämäärä.")
    private Date paatospvm;

    @Schema(description = "Opetushallituksen antama diaarinumero perusteelle.")
    private String diaarinumero;

    @Schema(description = "Perusteeseen liittyvät osaamisalakoodit.")
    private Set<KoodiDto> osaamisalat;

    @Schema(description = "Perusteeseen liittyvät tutkintonimikkeet.")
    private List<TutkintonimikeKoodiDto> tutkintonimikkeet;

    @Schema(description = "Perusteen koulutustyyppi (esim. ammatillinen, lukiokoulutus). "
            + "Digitaalisen osaamisen perusteissa koulutustyyppisuodatusta ei sovelleta haussa.")
    private String koulutustyyppi;

    @Schema(description = "Tutkinnon vähimmäislaajuus ensimmäisen suoritustavan muodostumissäännöstä.")
    private Integer laajuus;

    @Schema(description = "Perusteeseen liittyvät koulutukset ja niiden koulutuskoodit.")
    private List<KoulutusDto> koulutukset;

    @Schema(description = "Perusteen suoritustavat ja niihin liittyvät laajuusyksiköt.")
    private Set<SuoritustapaDto> suoritustavat;

    @Schema(description = "Perusteeseen tai tutkinnon osaan liittyvien koodien URI:t (koulutus-, tutkinnon osa-, "
            + "osaamisala- ja tutkintonimikekoodit). Käytetään koodi-hauparametrin täsmäosumalla suodatuksessa.")
    private List<String> koodit;

    @Schema(description = "Viimeisimmän julkaisun luontiaika.")
    private Date julkaistu;

    @Schema(description = "Perusteen luontiaika (epoch-millis).")
    private Long luotu;

    @Schema(description = "Julkaistun perusteen tyyppi (esim. normaali, opas, digitaalinen_osaaminen, amosaayhteinen).")
    private String tyyppi;

    @Schema(description = "Perusteen sisältökielet.")
    private Set<Kieli> kielet;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @Schema(description = "Tutkinnon osan tiedot, kun sisältötyyppi on tutkinnonosa. Muuten kenttä puuttuu vastauksesta.")
    private TutkinnonOsaKaikkiDto tutkinnonosa;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @Schema(description = "Perusteet, joissa tutkinnon osa esiintyy. Täytetään tutkinnon osa -hakutuloksissa; "
            + "muuten kenttä puuttuu vastauksesta.")
    private List<PerusteenJulkaisuData> perusteet;

    @Schema(description = "Palautetun tietueen tyyppi: peruste (kokonainen peruste) tai tutkinnonosa (yksittäinen tutkinnon osa).")
    private String sisaltotyyppi;

}
