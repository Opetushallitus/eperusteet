package fi.vm.sade.eperusteet.service;

import fi.vm.sade.eperusteet.dto.peruste.NavigationNodeDto;
import fi.vm.sade.eperusteet.dto.peruste.PerusteenOsaDto;
import fi.vm.sade.eperusteet.dto.tutkinnonrakenne.KoodiDto;
import fi.vm.sade.eperusteet.dto.util.LokalisoituTekstiDto;
import org.springframework.security.core.parameters.P;
import org.springframework.security.access.prepost.PreAuthorize;

public interface NavigationBuilderPublic extends NavigationBuilder {
    @Override
    default Class getImpl() {
        return NavigationBuilderPublic.class;
    }

    @PreAuthorize("hasPermission(#perusteId, 'peruste', 'LUKU')")
    NavigationNodeDto buildNavigation(@P("perusteId") Long perusteId, String kieli, boolean esikatselu, Integer julkaisuRevisio);

    default LokalisoituTekstiDto getPerusteenOsaNimi(PerusteenOsaDto perusteenOsaDto) {
        return perusteenOsaDto != null ? perusteenOsaDto.getNimi() : null;
    }

    default KoodiDto getPerusteenosaMetaKoodi(PerusteenOsaDto perusteenOsaDto) {
        return null;
    }
}
