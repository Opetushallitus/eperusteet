package fi.vm.sade.eperusteet.dto.peruste;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TutkinnonOsaQueryDto {
    private int sivu = 0;
    private int sivukoko = 25;
    private String nimi;
    private String koodiUri;
    private boolean kaikki = false;
    private Long perusteId;
    private String kieli = "fi";
    private boolean vanhentuneet;

}
