package fi.vm.sade.eperusteet.dto;

import fi.vm.sade.eperusteet.dto.util.LokalisoituTekstiDto;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@EqualsAndHashCode(of = "osaamistaso")
public class GeneerisenArvioinninOsaamistasonKriteeriDto {
    private Reference osaamistaso;
    private List<LokalisoituTekstiDto> kriteerit;
}
