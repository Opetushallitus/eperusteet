package fi.vm.sade.eperusteet.dto;

import fi.vm.sade.eperusteet.dto.util.PageDto;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AmmattitaitovaatimusQueryDto {
    private int sivu = 0;
    private int sivukoko = 25;
    private String uri;
}
