package fi.vm.sade.eperusteet.dto.peruste;

import fi.vm.sade.eperusteet.domain.Kieli;
import fi.vm.sade.eperusteet.domain.PerusteTyyppi;
import java.util.Date;
import java.util.Optional;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PerusteDokumenttiDto {
    private Long id;
    private PerusteTyyppi tyyppi;
    private Set<Kieli> kielet;
    private Set<SuoritustapaDto> suoritustavat;
    private PerusteVersionDto globalVersion;
    private Optional<Date> viimeisinJulkaisuAika;

}
