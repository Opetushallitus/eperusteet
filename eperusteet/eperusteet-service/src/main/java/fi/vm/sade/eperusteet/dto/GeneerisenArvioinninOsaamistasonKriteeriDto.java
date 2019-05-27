package fi.vm.sade.eperusteet.dto;

import fi.vm.sade.eperusteet.dto.util.LokalisoituTekstiDto;
import lombok.*;

import java.util.List;

@Getter
@Setter
@EqualsAndHashCode(of = "osaamistaso")
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GeneerisenArvioinninOsaamistasonKriteeriDto {
    private Reference osaamistaso;
    private List<LokalisoituTekstiDto> kriteerit;
}
