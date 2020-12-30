package fi.vm.sade.eperusteet.dto.peruste;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
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
