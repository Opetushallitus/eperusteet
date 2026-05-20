package fi.vm.sade.eperusteet.dto.peruste;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import fi.vm.sade.eperusteet.domain.Kieli;
import fi.vm.sade.eperusteet.domain.KoulutusTyyppi;
import fi.vm.sade.eperusteet.domain.KoulutustyyppiToteutus;
import fi.vm.sade.eperusteet.domain.OpasTyyppi;
import fi.vm.sade.eperusteet.domain.PerusteTila;
import fi.vm.sade.eperusteet.domain.PerusteTyyppi;
import fi.vm.sade.eperusteet.domain.PoikkeamismaaraysTyyppi;
import fi.vm.sade.eperusteet.domain.annotation.Identifiable;
import fi.vm.sade.eperusteet.dto.KoulutusDto;
import fi.vm.sade.eperusteet.dto.MuutosmaaraysDto;
import fi.vm.sade.eperusteet.dto.tutkinnonrakenne.KoodiDto;
import fi.vm.sade.eperusteet.dto.util.LokalisoituTekstiDto;
import fi.vm.sade.eperusteet.service.util.PerusteIdentifiable;
import fi.vm.sade.eperusteet.service.util.PerusteUtils;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Perusteen metatiedot ja yhteiset kentät. Yläluokka julkaistun perusteen täydelle sisällölle.")
public abstract class PerusteBaseDto implements Serializable, PerusteIdentifiable, Identifiable {

    @Schema(description = "Perusteen tunniste.")
    private Long id;

    @JsonIgnore
    @Schema(hidden = true)
    private Integer revision;

    @Schema(description = "Perusteen sisältöjen viimeisin päivitysaika.")
    private PerusteVersionDto globalVersion;

    @Schema(description = "Perusteen nimi.")
    private LokalisoituTekstiDto nimi;

    @Schema(description = "Perusteen koulutustyyppi.")
    private String koulutustyyppi;

    @Schema(description = "Perusteen sisäisen rakenteen toteutuksen ilmaiseva sisältö. "
            + "Esimerkiksi vanhan ja uuden lukion toteutus (LOPS vs LOPS2019).")
    private KoulutustyyppiToteutus toteutus;

    @Schema(description = "Perusteeseen liittyvät koulutukset ja niiden koulutuskoodit.")
    private Set<KoulutusDto> koulutukset;

    @Schema(description = "Perusteen sisältökielet.")
    private Set<Kieli> kielet;

    @Schema(description = "Perusteen kuvaus useilla kielillä.")
    private LokalisoituTekstiDto kuvaus;

    @Schema(description = "Perusteeseen liittyvän määräyskirjeen tiedot.")
    private MaarayskirjeDto maarayskirje;

    @Schema(description = "Perusteeseen liittyvät muutosmääräykset.")
    private List<MuutosmaaraysDto> muutosmaaraykset = new ArrayList<>();

    @Schema(description = "Opetushallituksen antama diaarinumero perusteelle.")
    private String diaarinumero;

    @Schema(description = "Perusteen voimaantulon alkamispäivä.")
    private Date voimassaoloAlkaa;

    @Schema(description = "Voimassaolon loppumisen jälkeinen siirtymäajan päättymispäivä. "
            + "Peruste on siirtymäajalla, kun virallinen voimassaolo on päättynyt mutta tämä päivämäärä ei ole vielä mennyt.")
    private Date siirtymaPaattyy;

    @Schema(description = "Perusteen virallisen voimassaolon päättymispäivä.")
    private Date voimassaoloLoppuu;

    @Schema(description = "Perusteen määräyksen päätöspäivämäärä.")
    private Date paatospvm;

    @Schema(description = "Viimeisimmän julkaisun ajankohta, jos peruste on julkaistu.")
    private Optional<Date> viimeisinJulkaisuAika;

    @Schema(description = "Perusteen luontiaika.")
    private Date luotu;

    @Schema(hidden = true)
    private Date muokattu;

    @Schema(description = "Perusteen sisäinen tila. Ei enää merkityksellinen julkaisujen käytönoton jälkeen.")
    private PerusteTila tila;

    @Schema(description = "Perusteen tyyppi (esim. normaali, opas, digitaalinen_osaaminen, amosaayhteinen).")
    private PerusteTyyppi tyyppi;

    @Schema(description = "Ilmaisee, onko peruste EU- ja ETA-maiden koulutusvientikokeiluun tarkoitettu.")
    private Boolean koulutusvienti;

    @Schema(description = "Perusteen vanhemmat määräykset diaarinumeroineen. "
            + "Eivät välttämättä ole toteutettu ePerusteisiin.")
    private Set<String> korvattavatDiaarinumerot;

    @Schema(description = "Perusteeseen liittyvät osaamisalakoodit.")
    private Set<KoodiDto> osaamisalat;

    @Schema(description = "KV-liitteestä tuotu kuvaus työtehtävistä, joissa tutkinnon suorittanut voi toimia.")
    private LokalisoituTekstiDto tyotehtavatJoissaVoiToimia;

    @Schema(description = "KV-liitteestä tuotu kuvaus tutkinnon suorittaneen osaamisesta.")
    private LokalisoituTekstiDto suorittaneenOsaaminen;

    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    @Schema(description = "Perusteeseen liittyvät tutkintonimikkeet.")
    List<TutkintonimikeKoodiDto> tutkintonimikkeet;

    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    @Schema(description = "Perusteet, joihin opas liittyy. Kenttä puuttuu, jos peruste ei ole opas.")
    private Set<PerusteKevytDto> oppaanPerusteet;

    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    @Schema(description = "Koulutustyypit, joihin opas liittyy. Kenttä puuttuu, jos peruste ei ole opas.")
    private Set<KoulutusTyyppi> oppaanKoulutustyypit;

    @Schema(description = "Perusteprosessin päivämäärät (esim. lausuntokierros, julkaisu).")
    private Set<PerusteAikatauluDto> perusteenAikataulut;

    @Schema(description = "Poikkeamismääräyksen tarkentava teksti.")
    private LokalisoituTekstiDto poikkeamismaaraysTarkennus;

    @Schema(description = "Poikkeamismääräyksen tyyppi.")
    private PoikkeamismaaraysTyyppi poikkeamismaaraysTyyppi;

    @Schema(description = "Opas-perusteen tyyppi (esim. normaali, tietoapalvelusta).")
    private OpasTyyppi opasTyyppi;

    @Schema(description = "Opas-perusteen kuvaus tietoapalvelusta-näkymää varten.")
    private LokalisoituTekstiDto tietoapalvelustaKuvaus;

    @Override
    public KoulutustyyppiToteutus getToteutus() {
        return PerusteUtils.getToteutus(this.toteutus, this.koulutustyyppi, this.tyyppi);
    }
}
