package fi.vm.sade.eperusteet.dto.yl.lukio.osaviitteet;

import com.fasterxml.jackson.annotation.JsonTypeName;
import fi.vm.sade.eperusteet.domain.PerusteTila;
import fi.vm.sade.eperusteet.domain.PerusteenOsaTunniste;
import fi.vm.sade.eperusteet.dto.peruste.NavigationType;
import fi.vm.sade.eperusteet.dto.peruste.PerusteenOsaDto;
import fi.vm.sade.eperusteet.dto.util.LokalisoituTekstiDto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@JsonTypeName("opetuksenyleisettavoitteet")
public class OpetuksenYleisetTavoitteetLaajaDto extends PerusteenOsaDto.Laaja {
    private UUID uuidTunniste;
    private LokalisoituTekstiDto otsikko;
    private LokalisoituTekstiDto kuvaus;

    public OpetuksenYleisetTavoitteetLaajaDto(LokalisoituTekstiDto nimi, PerusteTila tila, PerusteenOsaTunniste tunniste) {
        super(nimi, tila, tunniste);
    }

    @Override
    public String getOsanTyyppi() {
        return "opetuksenyleisettavoitteet";
    }

    @Override
    public NavigationType getNavigationType() {
        return NavigationType.opetuksenyleisettavoitteet;
    }
}
