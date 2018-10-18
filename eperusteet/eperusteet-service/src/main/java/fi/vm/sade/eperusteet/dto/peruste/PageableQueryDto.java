package fi.vm.sade.eperusteet.dto.peruste;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
@EqualsAndHashCode(of = { "sivu", "sivukoko" })
public class PageableQueryDto implements Serializable {
    private int sivu = 0;
    private int sivukoko = 25;
}
