package fi.vm.sade.eperusteet.dto;

import fi.vm.sade.eperusteet.dto.util.LokalisoituTekstiDto;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GeneerinenArviointiasteikkoDto {
    private Long id;
    private LokalisoituTekstiDto nimi;
    private LokalisoituTekstiDto kohde;
    private Reference arviointiAsteikko;
    private boolean julkaistu;
    private Set<GeneerisenArvioinninOsaamistasonKriteeriDto> osaamistasonKriteerit = new HashSet<>();
}
