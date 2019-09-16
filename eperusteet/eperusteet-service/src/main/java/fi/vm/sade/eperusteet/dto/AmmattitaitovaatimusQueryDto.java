package fi.vm.sade.eperusteet.dto;

import fi.vm.sade.eperusteet.dto.util.PageDto;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class AmmattitaitovaatimusQueryDto {
    private int sivu = 0;
    private int sivukoko = 25;
    private String uri;
    private boolean kaikki = false;
}
