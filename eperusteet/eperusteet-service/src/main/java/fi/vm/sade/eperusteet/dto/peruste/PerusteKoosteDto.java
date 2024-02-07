package fi.vm.sade.eperusteet.dto.peruste;

import fi.vm.sade.eperusteet.domain.Kieli;
import fi.vm.sade.eperusteet.dto.tutkinnonrakenne.KoodiDto;
import fi.vm.sade.eperusteet.dto.util.LokalisoituTekstiDto;
import java.util.List;
import java.util.Set;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PerusteKoosteDto {
    private LokalisoituTekstiDto nimi;
    private String diaarinumero;
    private String koulutustyyppi;
    private Set<Kieli> kielet;
    List<KoodiDto> tutkinnonOsat;
    List<KoosteenOsaamisalaDto> osaamisalat;
}
