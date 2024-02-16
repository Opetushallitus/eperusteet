package fi.vm.sade.eperusteet.dto.util;

import com.fasterxml.jackson.annotation.JsonValue;
import fi.vm.sade.eperusteet.domain.Kieli;
import fi.vm.sade.eperusteet.domain.PerusteenOsa;
import fi.vm.sade.eperusteet.domain.tutkinnonrakenne.TutkinnonOsaViite;
import fi.vm.sade.eperusteet.dto.peruste.NavigationNodeDto;
import fi.vm.sade.eperusteet.dto.peruste.NavigationType;
import java.util.Map;
import java.util.UUID;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@EqualsAndHashCode(callSuper = true)
public class NavigableLokalisoituTekstiDto extends LokalisoituTekstiDto {

    @Setter
    @Getter
    private NavigationNodeDto navigationNode;

    public NavigableLokalisoituTekstiDto(Long id, Map<Kieli, String> values, NavigationNodeDto navigationNode) {
        super(id, values);
        this.navigationNode = navigationNode;
    }

    public NavigableLokalisoituTekstiDto(TutkinnonOsaViite viite) {
        this(
                viite.getTutkinnonOsa().getNimi() != null ? viite.getTutkinnonOsa().getNimi().getId() : null,
                viite.getTutkinnonOsa().getNimi() != null ? viite.getTutkinnonOsa().getNimi().getTeksti() : null,
                NavigationNodeDto.of(
                        NavigationType.tutkinnonosaviite,
                        viite.getNimi() != null ? LokalisoituTekstiDto.of(viite.getNimi().getTeksti()) : LokalisoituTekstiDto.of("nimeton-tutkinnon-osa"),
                        viite.getId()));
    }

    public NavigableLokalisoituTekstiDto(PerusteenOsa perusteenOsa) {
        this(
                perusteenOsa.getNimi() != null ? perusteenOsa.getNimi().getId() : null,
                perusteenOsa.getNimi() != null ? perusteenOsa.getNimi().getTeksti() : null,
                NavigationNodeDto.of(
                        perusteenOsa.getNavigationType(),
                        perusteenOsa.getNimi() != null ? LokalisoituTekstiDto.of(perusteenOsa.getNimi().getTeksti()) : null,
                        perusteenOsa.getViitteet().stream().findFirst().get().getId()));
    }

    public NavigableLokalisoituTekstiDto(Long id, UUID tunniste, Map<Kieli, String> values) {
        super(id, tunniste, values);
    }

    public NavigableLokalisoituTekstiDto(Map<String, String> values) {
        super(values);
    }

    public NavigableLokalisoituTekstiDto(Long id, Map<Kieli, String> values) {
        super(id, values);
    }

    @Override
    @JsonValue
    public Map<String, Object> asMap() {
        Map<String, Object> map = super.asMap();
        if(navigationNode != null) {
                map.put("navigationNode", navigationNode);
        }

        return map;
    }
}
