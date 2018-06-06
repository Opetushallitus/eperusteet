package fi.vm.sade.eperusteet.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AbstractQueryDto {
    private int sivu = 0;
    private int sivukoko = 25;
    private String nimi;
}
