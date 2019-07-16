package fi.vm.sade.eperusteet.service.impl;

import fi.vm.sade.eperusteet.domain.GeneerinenArviointiasteikko;
import fi.vm.sade.eperusteet.domain.GeneerisenOsaamistasonKriteeri;
import fi.vm.sade.eperusteet.domain.Osaamistaso;
import fi.vm.sade.eperusteet.domain.arviointi.ArviointiAsteikko;
import fi.vm.sade.eperusteet.dto.GeneerinenArviointiasteikkoDto;
import fi.vm.sade.eperusteet.repository.GeneerinenArviointiasteikkoRepository;
import fi.vm.sade.eperusteet.service.GeneerinenArviointiasteikkoService;
import fi.vm.sade.eperusteet.service.exception.BusinessRuleViolationException;
import fi.vm.sade.eperusteet.service.mapping.Dto;
import fi.vm.sade.eperusteet.service.mapping.DtoMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Transactional
@Slf4j
public class GeneerinenArviointiasteikkoServiceImpl implements GeneerinenArviointiasteikkoService {

    @Autowired
    private GeneerinenArviointiasteikkoRepository geneerinenArviointiasteikkoRepository;

    @Autowired
    @Dto
    private DtoMapper mapper;

    @Override
    public List<GeneerinenArviointiasteikkoDto> getAll() {
        List<GeneerinenArviointiasteikko> asteikot = geneerinenArviointiasteikkoRepository.findAll();
        return mapper.mapAsList(asteikot, GeneerinenArviointiasteikkoDto.class);
    }

    private void tarkistaArviointiAsteikot(GeneerinenArviointiasteikko geneerinen) {
        Set<Long> asteikonTasot = geneerinen.getArviointiAsteikko().getOsaamistasot().stream()
                .map(Osaamistaso::getId)
                .collect(Collectors.toSet());
        Set<Long> geneerisenKriteerienOsaamistasot = geneerinen.getOsaamistasonKriteerit().stream()
                .map(GeneerisenOsaamistasonKriteeri::getOsaamistaso)
                .map(Osaamistaso::getId)
                .collect(Collectors.toSet());
        if (!asteikonTasot.equals(geneerisenKriteerienOsaamistasot)) {
            throw new BusinessRuleViolationException("kriteerien-osaamistasot-oltava-saman-arviointiasteikon");
        }
    }

    @Override
    public GeneerinenArviointiasteikkoDto getOne(Long id) {
        GeneerinenArviointiasteikko asteikko = geneerinenArviointiasteikkoRepository.findOne(id);
        if (asteikko == null) {
            throw new BusinessRuleViolationException("geneerinen-arivointiasteikko-ei-loytynyt");
        }
        return mapper.map(asteikko, GeneerinenArviointiasteikkoDto.class);
    }

    @Override
    public GeneerinenArviointiasteikkoDto add(GeneerinenArviointiasteikkoDto asteikkoDto) {
        GeneerinenArviointiasteikko asteikko = mapper.map(asteikkoDto, GeneerinenArviointiasteikko.class);
        asteikko.setId(null);
        Set<GeneerisenOsaamistasonKriteeri> osaamistasot = asteikko.getArviointiAsteikko().getOsaamistasot().stream()
                .map(taso -> {
                    GeneerisenOsaamistasonKriteeri uusi = new GeneerisenOsaamistasonKriteeri();
                    uusi.setOsaamistaso(taso);
                    return uusi;
                })
                .collect(Collectors.toSet());
        asteikko.setOsaamistasonKriteerit(osaamistasot);
        tarkistaArviointiAsteikot(asteikko);
        GeneerinenArviointiasteikko result = geneerinenArviointiasteikkoRepository.save(asteikko);
        return mapper.map(result, GeneerinenArviointiasteikkoDto.class);
    }

    @Override
    public GeneerinenArviointiasteikkoDto update(Long id, GeneerinenArviointiasteikkoDto asteikkoDto) {
        GeneerinenArviointiasteikko asteikko = geneerinenArviointiasteikkoRepository.findOne(id);
        if (asteikko.isJulkaistu()) {
            throw new BusinessRuleViolationException("julkaistua-ei-voi-muokata");
        }

        asteikkoDto.setId(id);
        ArviointiAsteikko arviointiAsteikko = asteikko.getArviointiAsteikko();
        mapper.map(asteikkoDto, asteikko);
        asteikko.setArviointiAsteikko(arviointiAsteikko);
        tarkistaArviointiAsteikot(asteikko);
        asteikko = geneerinenArviointiasteikkoRepository.save(asteikko);
        return mapper.map(asteikko, GeneerinenArviointiasteikkoDto.class);
    }

    @Override
    public void remove(Long id) {
        GeneerinenArviointiasteikko vanha = geneerinenArviointiasteikkoRepository.findOne(id);
        if (vanha.isJulkaistu()) {
            throw new BusinessRuleViolationException("julkaistua-ei-voi-muokata");
        }
        geneerinenArviointiasteikkoRepository.delete(id);
    }

    @Override
    public GeneerinenArviointiasteikkoDto kopioi(Long id) {
        GeneerinenArviointiasteikko vanha = geneerinenArviointiasteikkoRepository.findOne(id);
        if (!vanha.isJulkaistu()) {
            throw new BusinessRuleViolationException("vain-julkaistun-voi-kopioida");
        }
        GeneerinenArviointiasteikko uusi = vanha.copy();
        return mapper.map(uusi, GeneerinenArviointiasteikkoDto.class);
    }
}
