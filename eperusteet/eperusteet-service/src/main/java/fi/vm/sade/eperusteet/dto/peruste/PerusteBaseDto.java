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
public abstract class PerusteBaseDto implements Serializable, PerusteIdentifiable, Identifiable {

    private Long id;
    @JsonIgnore
    private Integer revision;

    @Schema(description = "Perusteen sisältöjen viimeisin päivitysaika")
    private PerusteVersionDto globalVersion;

    private LokalisoituTekstiDto nimi;
    private String koulutustyyppi;

    @Schema(description = "Perusteen sisäisen rakenteen toteutuksen ilmaiseva sisältö. Esimerkiksi vanhan ja uuden lukion toteutus (LOPS vs LOPS2019)")
    private KoulutustyyppiToteutus toteutus;
    private Set<KoulutusDto> koulutukset;
    private Set<Kieli> kielet;
    private LokalisoituTekstiDto kuvaus;
    private MaarayskirjeDto maarayskirje;
    private List<MuutosmaaraysDto> muutosmaaraykset = new ArrayList<>();

    private String diaarinumero;
    private Date voimassaoloAlkaa;

    @Schema(description = "Voimassaolon loppumisen jälkeinen perusteen päättymispäivämäärä.")
    private Date siirtymaPaattyy;
    private Date voimassaoloLoppuu;

    @Schema(description = "Perusteen määräyksen päätöspäivämäärä")
    private Date paatospvm;
    private Optional<Date> viimeisinJulkaisuAika;

    private Date luotu;
    @Schema(hidden = true)
    private Date muokattu;

    @Schema(description = "Perusteen sisäinen tila. Ei enää merkityksellinen julkaisujen käytönoton jälkeen")
    private PerusteTila tila;
    private PerusteTyyppi tyyppi;

    @Schema(description = "EU- ja ETA-maiden koulutusvientikokeiluun tarkoitettu peruste")
    private Boolean koulutusvienti;

    @Schema(description = "Perusteen vanhemmat määräykset. Eivät välttämättä ole toteutettu ePerusteisiin")
    private Set<String> korvattavatDiaarinumerot;

    @Schema(description = "Perusteeseen liittyvät osaamisalakoodit")
    private Set<KoodiDto> osaamisalat;

    // Tuodaan kvliitteestä
    @Schema(description = "kv-liitteen lisätieto")
    private LokalisoituTekstiDto tyotehtavatJoissaVoiToimia;

    @Schema(description = "kv-liitteen lisätieto")
    private LokalisoituTekstiDto suorittaneenOsaaminen;

    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    List<TutkintonimikeKoodiDto> tutkintonimikkeet;

    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    @Schema(description = "Perusteet joihin opas liittyy")
    private Set<PerusteKevytDto> oppaanPerusteet;

    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    @Schema(description = "Koulutustyypit joihin opas liittyy")
    private Set<KoulutusTyyppi> oppaanKoulutustyypit;

    @Schema(description = "Perusteprosessin päivämäärät")
    private Set<PerusteAikatauluDto> perusteenAikataulut;

    private LokalisoituTekstiDto poikkeamismaaraysTarkennus;
    private PoikkeamismaaraysTyyppi poikkeamismaaraysTyyppi;
    private OpasTyyppi opasTyyppi;
    private LokalisoituTekstiDto tietoapalvelustaKuvaus;

    @Override
    public KoulutustyyppiToteutus getToteutus() {
        return PerusteUtils.getToteutus(this.toteutus, this.koulutustyyppi, this.tyyppi);
    }
}
