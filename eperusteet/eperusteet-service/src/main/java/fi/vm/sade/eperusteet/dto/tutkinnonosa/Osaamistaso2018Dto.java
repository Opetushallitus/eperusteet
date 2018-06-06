package fi.vm.sade.eperusteet.dto.tutkinnonosa;

import fi.vm.sade.eperusteet.dto.OsaamistasonKriteeriDto;
import fi.vm.sade.eperusteet.dto.util.EntityReference;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.Singular;

import java.util.List;

@Getter
@Setter
@Builder
public class Osaamistaso2018Dto {
    private Long id;
    private EntityReference osaamistaso_id;

    @Singular("kriteeri")
    private List<OsaamistasonKriteeri2018Dto> kriteerit;
}
