package fi.vm.sade.eperusteet.service.mapping;

import fi.vm.sade.eperusteet.domain.TekstiPalanen;
import fi.vm.sade.eperusteet.dto.util.LokalisoituTekstiDto;
import fi.vm.sade.eperusteet.repository.TekstiPalanenRepository;
import ma.glasnost.orika.MappingContext;
import ma.glasnost.orika.converter.BidirectionalConverter;
import ma.glasnost.orika.metadata.Type;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@UncachedDto
public class TekstiPalanenUncachedConverter extends BidirectionalConverter<TekstiPalanen, LokalisoituTekstiDto> {
    private static final Logger LOG = LoggerFactory.getLogger(TekstiPalanenUncachedConverter.class);

    @Autowired
    private TekstiPalanenRepository repository;

    @Override
    public LokalisoituTekstiDto convertTo(TekstiPalanen source, Type<LokalisoituTekstiDto> destinationType, MappingContext mappingContext) {
        return new LokalisoituTekstiDto(source.getId(), source.getTunniste(), source.getTeksti());
    }

    @Override
    public TekstiPalanen convertFrom(LokalisoituTekstiDto source, Type<TekstiPalanen> destinationType, MappingContext mappingContext) {
        return TekstiPalanen.of(source.getTekstit());
    }
}