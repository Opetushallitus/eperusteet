package fi.vm.sade.eperusteet.dto.tutkinnonosa;

import fi.vm.sade.eperusteet.dto.util.EntityReference;
import fi.vm.sade.eperusteet.dto.util.LokalisoituTekstiDto;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.Singular;

import java.util.List;

@Getter
@Setter
@Builder
public class Ammattitaitovaatimus2018Dto {
    private Long id;
    private EntityReference arviointiasteikko_id;
    private LokalisoituTekstiDto nimi;
    private Long koodi;

    @Singular("osaamistaso")
    private List<Osaamistaso2018Dto> osaamistasot;
}
