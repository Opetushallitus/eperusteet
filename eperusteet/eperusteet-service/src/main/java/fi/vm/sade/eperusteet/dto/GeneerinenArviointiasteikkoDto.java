package fi.vm.sade.eperusteet.dto;

import fi.vm.sade.eperusteet.domain.KoulutusTyyppi;
import fi.vm.sade.eperusteet.dto.util.LokalisoituTekstiDto;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GeneerinenArviointiasteikkoDto {
    private Long id;
    private LokalisoituTekstiDto nimi;
    private LokalisoituTekstiDto kohde;
    private Reference arviointiAsteikko;
    private boolean julkaistu;
    private boolean valittavissa;
    private boolean oletusvalinta;
    private Set<KoulutusTyyppi> koulutustyypit;
    private Set<GeneerisenArvioinninOsaamistasonKriteeriDto> osaamistasonKriteerit = new HashSet<>();
}
