package fi.vm.sade.eperusteet.dto.peruste;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeInfo.As;
import com.fasterxml.jackson.annotation.JsonTypeInfo.Id;
import fi.vm.sade.eperusteet.domain.PerusteTila;
import fi.vm.sade.eperusteet.domain.PerusteenOsaTunniste;
import fi.vm.sade.eperusteet.dto.ReferenceableDto;
import fi.vm.sade.eperusteet.dto.digi.OsaamiskokonaisuusDto;
import fi.vm.sade.eperusteet.dto.digi.OsaamiskokonaisuusPaaAlueDto;
import fi.vm.sade.eperusteet.dto.tutkinnonosa.TutkinnonOsaDto;
import fi.vm.sade.eperusteet.dto.tuva.KoulutuksenOsaDto;
import fi.vm.sade.eperusteet.dto.tuva.TuvaLaajaAlainenOsaaminenDto;
import fi.vm.sade.eperusteet.dto.util.LokalisoituTekstiDto;
import fi.vm.sade.eperusteet.dto.vst.KotoKielitaitotasoDto;
import fi.vm.sade.eperusteet.dto.vst.KotoLaajaAlainenOsaaminenDto;
import fi.vm.sade.eperusteet.dto.vst.KotoOpintoDto;
import fi.vm.sade.eperusteet.dto.vst.OpintokokonaisuusDto;
import fi.vm.sade.eperusteet.dto.vst.TavoitesisaltoalueDto;
import fi.vm.sade.eperusteet.dto.yl.TaiteenalaDto;
import fi.vm.sade.eperusteet.dto.yl.lukio.osaviitteet.AihekokonaisuudetLaajaDto;
import fi.vm.sade.eperusteet.dto.yl.lukio.osaviitteet.LukioOpetussuunnitelmaRakenneLaajaDto;
import fi.vm.sade.eperusteet.dto.yl.lukio.osaviitteet.OpetuksenYleisetTavoitteetLaajaDto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
public abstract class PerusteenOsaDto implements ReferenceableDto {
    private Long id;
    private Date luotu;
    private Date muokattu;
    private String muokkaaja;
    private LokalisoituTekstiDto nimi;
    private PerusteTila tila;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private PerusteenOsaTunniste tunniste;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Boolean valmis;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Boolean kaannettava;

    public PerusteenOsaDto( LokalisoituTekstiDto nimi, PerusteTila tila, PerusteenOsaTunniste tunniste) {
        this.nimi = nimi;
        this.tila = tila;
        this.tunniste = tunniste;
    }

    @Data
    @EqualsAndHashCode(callSuper = true)
    @JsonTypeInfo(use = Id.NAME, include = As.EXISTING_PROPERTY, property = "osanTyyppi")
    @JsonSubTypes(value = {
            @JsonSubTypes.Type(value = TekstiKappaleDto.class),
            @JsonSubTypes.Type(value = TutkinnonOsaDto.class),
            @JsonSubTypes.Type(value = TaiteenalaDto.class),
            @JsonSubTypes.Type(value = AihekokonaisuudetLaajaDto.class),
            @JsonSubTypes.Type(value = OpetuksenYleisetTavoitteetLaajaDto.class),
            @JsonSubTypes.Type(value = LukioOpetussuunnitelmaRakenneLaajaDto.class),
            @JsonSubTypes.Type(value = OpintokokonaisuusDto.class),
            @JsonSubTypes.Type(value = TavoitesisaltoalueDto.class),
            @JsonSubTypes.Type(value = KoulutuksenOsaDto.class),
            @JsonSubTypes.Type(value = KotoKielitaitotasoDto.class),
            @JsonSubTypes.Type(value = KotoOpintoDto.class),
            @JsonSubTypes.Type(value = KotoLaajaAlainenOsaaminenDto.class),
            @JsonSubTypes.Type(value = TuvaLaajaAlainenOsaaminenDto.class),
            @JsonSubTypes.Type(value = OsaamiskokonaisuusDto.class),
            @JsonSubTypes.Type(value = OsaamiskokonaisuusPaaAlueDto.class)
    })
    public static abstract class Laaja extends PerusteenOsaDto implements Navigable {
        public abstract String getOsanTyyppi();

        public Laaja() {
        }

        public Laaja(LokalisoituTekstiDto nimi, PerusteTila tila, PerusteenOsaTunniste tunniste) {
            super(nimi, tila, tunniste);
        }
    }

    @Data
    @EqualsAndHashCode(callSuper = true)
    public static class Suppea extends PerusteenOsaDto {
        private String osanTyyppi;

        public Suppea() {
        }

        public Suppea(LokalisoituTekstiDto nimi, PerusteTila tila, PerusteenOsaTunniste tunniste) {
            super(nimi, tila, tunniste);
        }
    }

}
