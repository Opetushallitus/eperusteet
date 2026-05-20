package fi.vm.sade.eperusteet.dto.peruste;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import fi.vm.sade.eperusteet.domain.KoulutustyyppiToteutus;
import fi.vm.sade.eperusteet.domain.PerusteTyyppi;
import fi.vm.sade.eperusteet.dto.KVLiiteJulkinenDto;
import fi.vm.sade.eperusteet.dto.lops2019.Lops2019SisaltoDto;
import fi.vm.sade.eperusteet.dto.tutkinnonosa.TutkinnonOsaKaikkiDto;
import fi.vm.sade.eperusteet.dto.tutkinnonrakenne.TutkinnonOsaViiteSuppeaDto;
import fi.vm.sade.eperusteet.dto.tuva.KoulutuksenOsaDto;
import fi.vm.sade.eperusteet.dto.tuva.TutkintoonvalmentavaSisaltoDto;
import fi.vm.sade.eperusteet.dto.vst.VapaasivistystyoSisaltoDto;
import fi.vm.sade.eperusteet.dto.kios.KieliJaKaantajaTutkintoSisaltoDto;
import fi.vm.sade.eperusteet.dto.yl.AIPEOpetuksenSisaltoDto;
import fi.vm.sade.eperusteet.dto.yl.EsiopetuksenPerusteenSisaltoDto;
import fi.vm.sade.eperusteet.dto.yl.PerusopetuksenPerusteenSisaltoDto;
import fi.vm.sade.eperusteet.dto.yl.TPOOpetuksenSisaltoDto;
import fi.vm.sade.eperusteet.dto.yl.lukio.julkinen.LukiokoulutuksenPerusteenSisaltoDto;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Julkaistun perusteen täydet sisältötiedot. "
        + "Sisältää perusteen metatiedot sekä koulutustyypin mukaisen sisältörakenteen.")
public class PerusteKaikkiDto extends PerusteBaseDto {

    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    @Schema(description = "Ammatillisten perusteiden suoritustavat: tutkinnon muodostuminen, rakenne "
            + "ja tutkinnon osaviitteet laajuuksineen.")
    Set<SuoritustapaLaajaDto> suoritustavat;

    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    @Schema(description = "Tutkinnon osien täydet tiedot. Laajuudet täydennetään suoritustapojen "
            + "tutkinnon osaviitteistä vastauksessa.")
    List<TutkinnonOsaKaikkiDto> tutkinnonOsat;

    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    @Schema(description = "Tutkintoon valmentavan koulutuksen (TUVA) koulutuksen osat.")
    List<KoulutuksenOsaDto> koulutuksenOsat;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonProperty("perusopetus")
    @Schema(description = "Perusopetuksen perusteen sisältörakenne. "
            + "Kenttä puuttuu, jos peruste ei ole perusopetuksen peruste.")
    private PerusopetuksenPerusteenSisaltoDto perusopetuksenPerusteenSisalto;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonProperty("lukiokoulutus")
    @Schema(description = "Vanhan lukion (LOPS) perusteen sisältörakenne. "
            + "Kenttä puuttuu, jos peruste käyttää LOPS 2019 -rakennetta.")
    private LukiokoulutuksenPerusteenSisaltoDto lukiokoulutuksenPerusteenSisalto;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonProperty("lops2019")
    @Schema(description = "Uuden lukion (LOPS 2019) perusteen sisältörakenne.")
    private Lops2019SisaltoDto lops2019Sisalto;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonProperty("esiopetus")
    @Schema(description = "Esiopetuksen perusteen sisältörakenne.")
    private EsiopetuksenPerusteenSisaltoDto esiopetuksenPerusteenSisalto;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonProperty("aipe")
    @Schema(description = "Aikuisten perusopetuksen perusteen sisältörakenne.")
    private AIPEOpetuksenSisaltoDto aipeOpetuksenPerusteenSisalto;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonProperty("tpo")
    @Schema(description = "Taiteen perusopetuksen perusteen sisältörakenne.")
    private TPOOpetuksenSisaltoDto tpoOpetuksenSisalto;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonProperty("vapaasivistystyo")
    @Schema(description = "Vapaan sivistystyön perusteen sisältörakenne.")
    private VapaasivistystyoSisaltoDto vstSisalto;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonProperty("tutkintoonvalmentava")
    @Schema(description = "Tutkintoon valmentavan koulutuksen perusteen sisältörakenne.")
    private TutkintoonvalmentavaSisaltoDto tuvasisalto;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonProperty("opas")
    @Schema(description = "Opas-perusteen sisältörakenne.")
    private OpasSisaltoDto oppaanSisalto;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonProperty("digitaalinenosaaminen")
    @Schema(description = "Digitaalisen osaamisen perusteen sisältörakenne.")
    private DigitaalisenOsaamisenSisaltoDto digitaalinenOsaaminenSisalto;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonProperty("kielijakaantajatutkinto")
    @Schema(description = "Kieli- ja kääntäjätutkinnon perusteen sisältörakenne.")
    private KieliJaKaantajaTutkintoSisaltoDto kieliJaKaantajaTutkintoPerusteenSisalto;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @Schema(description = "Perusteen viimeisimmän muutosmääräyksen voimaantulon alkamispäivä. "
            + "Kenttä puuttuu, jos perusteella ei ole voimassa olevaa muutosmääräystä.")
    private Date muutosmaarayksenVoimassaoloAlkaa;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @Schema(description = "Perusteen kansainvälisen tutkinnon viitekehyksen (KV) liite. "
            + "Kenttä puuttuu, jos perusteella ei ole KV-liitettä.")
    private KVLiiteJulkinenDto kvLiite;

    @JsonIgnore
    public Set<PerusteenSisaltoDto> getSisallot() {
        if (PerusteTyyppi.OPAS.equals(this.getTyyppi())) {
            return Collections.singleton(this.getOppaanSisalto());
        } else {
            if (KoulutustyyppiToteutus.AMMATILLINEN.equals(this.getToteutus())) {
                return new HashSet<>(this.getSuoritustavat());
            } else if (this.getPerusopetuksenPerusteenSisalto() != null) {
                return Collections.singleton(this.getPerusopetuksenPerusteenSisalto());
            } else if (this.getLops2019Sisalto() != null) {
                return Collections.singleton(this.getLops2019Sisalto());
            } else if (this.getEsiopetuksenPerusteenSisalto() != null) {
                return Collections.singleton(this.getEsiopetuksenPerusteenSisalto());
            } else if (this.getLukiokoulutuksenPerusteenSisalto() != null) {
                return Collections.singleton(this.getLukiokoulutuksenPerusteenSisalto());
            } else if (this.getAipeOpetuksenPerusteenSisalto() != null) {
                return Collections.singleton(this.getAipeOpetuksenPerusteenSisalto());
            } else if (this.getTpoOpetuksenSisalto() != null) {
                return Collections.singleton(this.getTpoOpetuksenSisalto());
            } else if (this.getVstSisalto() != null) {
                return Collections.singleton(this.getVstSisalto());
            } else if (this.getTuvasisalto() != null) {
                return Collections.singleton(this.getTuvasisalto());
            } else if (this.getDigitaalinenOsaaminenSisalto() != null) {
                return Collections.singleton(this.getDigitaalinenOsaaminenSisalto());
            } else if (this.getKieliJaKaantajaTutkintoPerusteenSisalto() != null) {
                return Collections.singleton(this.getKieliJaKaantajaTutkintoPerusteenSisalto());
            }
        }
        return new HashSet<>();
    }

    public List<TutkinnonOsaKaikkiDto> getTutkinnonOsat() {
        if (tutkinnonOsat == null) {
            return null;
        }

        return tutkinnonOsat.stream().peek(tutkinnonosa -> {
            Map<Long, TutkinnonOsaViiteSuppeaDto> viitteetLaajuusMap = suoritustavat.stream()
                    .map(SuoritustapaLaajaDto::getTutkinnonOsat)
                    .flatMap(Collection::stream)
                    .collect(Collectors.toMap((viite -> viite.getTutkinnonOsa().getIdLong()), viite -> viite, (first, second) -> first));
            tutkinnonosa.setLaajuus(Optional.ofNullable(viitteetLaajuusMap.get(tutkinnonosa.getId())).map(TutkinnonOsaViiteSuppeaDto::getLaajuus).orElse(null));
            tutkinnonosa.setLaajuusMaksimi(Optional.ofNullable(viitteetLaajuusMap.get(tutkinnonosa.getId())).map(TutkinnonOsaViiteSuppeaDto::getLaajuusMaksimi).orElse(null));
        }).collect(Collectors.toList());
    }
}
