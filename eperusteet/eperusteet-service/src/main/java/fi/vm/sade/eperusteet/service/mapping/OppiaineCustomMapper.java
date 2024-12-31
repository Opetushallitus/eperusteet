package fi.vm.sade.eperusteet.service.mapping;

import fi.vm.sade.eperusteet.domain.yl.Oppiaine;
import fi.vm.sade.eperusteet.dto.yl.OppiaineBaseDto;
import fi.vm.sade.eperusteet.service.KoodistoClient;
import ma.glasnost.orika.CustomMapper;
import ma.glasnost.orika.MappingContext;

public class OppiaineCustomMapper {

    public static <T extends OppiaineBaseDto> CustomMapper<T, Oppiaine> create(KoodistoClient koodistoClient) {
        return new CustomMapper<T, Oppiaine>() {

            @Override
            public void mapBtoA(Oppiaine oppiaine, T oppiaineDto, MappingContext context) {
                super.mapBtoA(oppiaine, oppiaineDto, context);
                if (oppiaine.getKoodiUri() != null) {
                    oppiaineDto.setKoodi(koodistoClient.getKoodi(oppiaine.getKoodiUri().split("_")[0], oppiaine.getKoodiUri()));
                }
            }

            @Override
            public void mapAtoB(T oppiaineDto, Oppiaine oppiaine, MappingContext context) {
                super.mapAtoB(oppiaineDto, oppiaine, context);
                if (oppiaineDto.getKoodi() != null) {
                    oppiaine.setKoodiUri(oppiaineDto.getKoodi().getUri());
                    oppiaine.setKoodiArvo(oppiaineDto.getKoodi().getArvo());
                }
            }
        };
    }
}