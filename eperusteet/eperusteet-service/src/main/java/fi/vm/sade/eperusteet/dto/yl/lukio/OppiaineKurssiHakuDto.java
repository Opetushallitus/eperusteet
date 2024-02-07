package fi.vm.sade.eperusteet.dto.yl.lukio;

import lombok.EqualsAndHashCode;
import lombok.Getter;

@Getter
@EqualsAndHashCode
public class OppiaineKurssiHakuDto {
    private final Long oppiaineId;
    private final Long kurssiId;
    private final Long oppiaineNimiId;
    private final Integer jarjestys;

    public OppiaineKurssiHakuDto(Long oppiaineId, Long kurssiId, Integer jarjestys, Long oppiaineNimiId) {
        this.oppiaineId = oppiaineId;
        this.kurssiId = kurssiId;
        this.jarjestys = jarjestys;
        this.oppiaineNimiId = oppiaineNimiId;
    }
}
