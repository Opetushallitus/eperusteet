package fi.vm.sade.eperusteet.dto.peruste;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PageableQueryDto {
    private int sivu = 0;
    private int sivukoko = 25;
}
