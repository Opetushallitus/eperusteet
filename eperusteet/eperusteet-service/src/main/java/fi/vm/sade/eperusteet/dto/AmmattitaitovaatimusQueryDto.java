package fi.vm.sade.eperusteet.dto;

import fi.vm.sade.eperusteet.dto.util.PageDto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AmmattitaitovaatimusQueryDto {
    private int sivu = 0;
    private int sivukoko = 25;
    private String uri;
    private boolean kaikki = false;
}
