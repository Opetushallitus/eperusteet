package fi.vm.sade.eperusteet.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AmmattitaitovaatimusQueryDto {
    private int sivu = 0;
    private int sivukoko = 25;
    private String uri;
    private boolean kaikki = false;
}
