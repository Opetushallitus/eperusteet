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
@Dto
public class TekstiPalanenConverter extends BidirectionalConverter<TekstiPalanen, LokalisoituTekstiDto> {

    private static final Logger LOG = LoggerFactory.getLogger(TekstiPalanenConverter.class);

    @Autowired
    private TekstiPalanenRepository repository;

    @Override
    public LokalisoituTekstiDto convertTo(TekstiPalanen source, Type<LokalisoituTekstiDto> destinationType, MappingContext mappingContext) {
        return new LokalisoituTekstiDto(source.getId(), source.getTunniste(), source.getTeksti());
    }

    @Override
    public TekstiPalanen convertFrom(LokalisoituTekstiDto source, Type<TekstiPalanen> destinationType, MappingContext mappingContext) {
        if (source.getId() != null) {
            /*
            Jos id on mukana, yritä yhdistää olemassa olevaan tekstipalaseen
            Koska tekstipalanen on muuttumaton ja cachetettu, niin oletustapaus on että
            tekstipalanen on jo cachessa (luettu aikaisemmin) ja tietokantahaku vältetään.
            Huom! vihamielinen/virheellinen client voisi keksiä id:n aiheuttaen turhia tietokantahakuja.
            */
            TekstiPalanen current = repository.findOne(source.getId());
            if (current != null) {
                TekstiPalanen tekstiPalanen = TekstiPalanen.of(source.getTekstit(), current.getTunniste());
                if ( current.equals(tekstiPalanen) ) {
                    return current;
                }
                return tekstiPalanen;
            }
        }
        return TekstiPalanen.of(source.getTekstit());
    }
}
