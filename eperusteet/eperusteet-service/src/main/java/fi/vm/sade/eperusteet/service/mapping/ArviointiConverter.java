package fi.vm.sade.eperusteet.service.mapping;

import fi.ratamaa.dtoconverter.annotation.DtoMap;
import fi.vm.sade.eperusteet.domain.GeneerinenArviointiasteikko;
import fi.vm.sade.eperusteet.domain.GeneerisenOsaamistasonKriteeri;
import fi.vm.sade.eperusteet.domain.Osaamistaso;
import fi.vm.sade.eperusteet.domain.TekstiPalanen;
import fi.vm.sade.eperusteet.domain.arviointi.ArviointiAsteikko;
import fi.vm.sade.eperusteet.dto.Arviointi2020Dto;
import fi.vm.sade.eperusteet.dto.GeneerisenArvioinninOsaamistasonKriteeriDto;
import fi.vm.sade.eperusteet.dto.OsaamistasoDto;
import fi.vm.sade.eperusteet.dto.OsaamistasonKriteerit2020Dto;
import fi.vm.sade.eperusteet.dto.util.LokalisoituTekstiDto;
import fi.vm.sade.eperusteet.repository.GeneerinenArviointiasteikkoRepository;
import fi.vm.sade.eperusteet.repository.TekstiPalanenRepository;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import ma.glasnost.orika.MappingContext;
import ma.glasnost.orika.converter.BidirectionalConverter;
import ma.glasnost.orika.metadata.Type;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;


@Component
public class ArviointiConverter extends BidirectionalConverter<GeneerinenArviointiasteikko, Arviointi2020Dto> {

    @Override
    public Arviointi2020Dto convertTo(GeneerinenArviointiasteikko geneerinenArviointiasteikko, Type<Arviointi2020Dto> type, MappingContext mappingContext) {
        if (geneerinenArviointiasteikko == null) {
            return null;
        }

        Arviointi2020Dto result = new Arviointi2020Dto();
        ArviointiAsteikko asteikko = geneerinenArviointiasteikko.getArviointiAsteikko();
        result.setArviointiAsteikko(geneerinenArviointiasteikko.getArviointiAsteikko().getReference());
        result.setKohde(mapperFacade.map(geneerinenArviointiasteikko.getKohde(), LokalisoituTekstiDto.class));

        final Map<Osaamistaso, List<TekstiPalanen>> tasot = geneerinenArviointiasteikko.getOsaamistasonKriteerit().stream()
                .collect(Collectors.toMap(GeneerisenOsaamistasonKriteeri::getOsaamistaso, GeneerisenOsaamistasonKriteeri::getKriteerit));
        result.setOsaamistasonKriteerit(asteikko.getOsaamistasot().stream()
                .map(ot -> {
                    List<TekstiPalanen> kriteerit = tasot.getOrDefault(ot, Collections.emptyList());
                    OsaamistasonKriteerit2020Dto taso = new OsaamistasonKriteerit2020Dto();
                    taso.setOsaamistaso(mapperFacade.map(ot, OsaamistasoDto.class));
                    taso.setKriteerit(mapperFacade.mapAsList(kriteerit, LokalisoituTekstiDto.class));
                    return taso;
                })
                .collect(Collectors.toList()));
        return result;
    }

    @Override
    public GeneerinenArviointiasteikko convertFrom(Arviointi2020Dto arviointi2020Dto, Type<GeneerinenArviointiasteikko> type, MappingContext mappingContext) {
        return null;
    }
}
