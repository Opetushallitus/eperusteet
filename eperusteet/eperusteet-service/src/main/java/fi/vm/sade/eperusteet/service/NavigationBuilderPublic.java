package fi.vm.sade.eperusteet.service;


import fi.vm.sade.eperusteet.dto.peruste.PerusteenOsaDto;
import fi.vm.sade.eperusteet.dto.util.LokalisoituTekstiDto;

public interface NavigationBuilderPublic extends NavigationBuilder {
    @Override
    default Class getImpl() {
        return NavigationBuilderPublic.class;
    }

    default LokalisoituTekstiDto getPerusteenOsaNimi(PerusteenOsaDto perusteenOsaDto) {
        return perusteenOsaDto != null ? perusteenOsaDto.getNimi() : null;
    }
}
