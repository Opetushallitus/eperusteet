package fi.vm.sade.eperusteet.dto.yl.lukio;

import fi.vm.sade.eperusteet.dto.yl.OppiaineBaseDto;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

@Getter
@EqualsAndHashCode
public class LukioOpetussuunnitelmaRakenneRevisionDto<OppiaineType extends OppiaineBaseDto> {
    private final Long perusteId;
    private final Integer rakenneRevision;
    private final List<LukiokurssiListausDto> kurssit = new ArrayList<>();
    private final List<OppiaineType> oppiaineet = new ArrayList<>();

    public LukioOpetussuunnitelmaRakenneRevisionDto(Long perusteId, Integer rakenneRevision) {
        this.perusteId = perusteId;
        this.rakenneRevision = rakenneRevision;
    }
}
