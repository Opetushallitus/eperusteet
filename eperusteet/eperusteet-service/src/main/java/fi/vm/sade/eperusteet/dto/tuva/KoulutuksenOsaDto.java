package fi.vm.sade.eperusteet.dto.tuva;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonTypeName;
import fi.vm.sade.eperusteet.domain.KoulutusOsanTyyppi;
import fi.vm.sade.eperusteet.domain.tuva.KoulutusOsanKoulutustyyppi;
import fi.vm.sade.eperusteet.dto.peruste.NavigationType;
import fi.vm.sade.eperusteet.dto.peruste.PerusteenOsaDto;
import fi.vm.sade.eperusteet.dto.tutkinnonrakenne.KoodiDto;
import fi.vm.sade.eperusteet.dto.util.LokalisoituTekstiDto;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@JsonTypeName("koulutuksenosa")
@JsonIgnoreProperties(ignoreUnknown = true)
public class KoulutuksenOsaDto extends PerusteenOsaDto.Laaja {

    private KoodiDto nimiKoodi;
    private Integer laajuusMinimi;
    private Integer laajuusMaksimi;
    private KoulutusOsanKoulutustyyppi koulutusOsanKoulutustyyppi;
    private KoulutusOsanTyyppi koulutusOsanTyyppi;
    private LokalisoituTekstiDto kuvaus;
    private LokalisoituTekstiDto tavoitteenKuvaus;
    private List<LokalisoituTekstiDto> tavoitteet;
    private LokalisoituTekstiDto keskeinenSisalto;
    private LokalisoituTekstiDto laajaAlaisenOsaamisenKuvaus;
    private LokalisoituTekstiDto arvioinninKuvaus;

    @Override
    public String getOsanTyyppi() {
        return "koulutuksenosa";
    }

    @Override
    public NavigationType getNavigationType() {
        return NavigationType.koulutuksenosa;
    }
}
