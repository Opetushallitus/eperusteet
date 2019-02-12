package fi.vm.sade.eperusteet.dto.peruste;

import lombok.*;

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
}
