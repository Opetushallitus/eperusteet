package fi.vm.sade.eperusteet.service.impl;

import fi.vm.sade.eperusteet.domain.Tiedote;
import fi.vm.sade.eperusteet.dto.TiedoteDto;
import fi.vm.sade.eperusteet.repository.TiedoteRepository;
import fi.vm.sade.eperusteet.service.TiedoteService;
import fi.vm.sade.eperusteet.service.exception.BusinessRuleViolationException;
import fi.vm.sade.eperusteet.service.mapping.Dto;
import fi.vm.sade.eperusteet.service.mapping.DtoMapper;
import fi.vm.sade.eperusteet.service.security.PermissionHelper;
import fi.vm.sade.eperusteet.service.util.SecurityUtil;
import java.util.Date;
import java.util.List;
import org.opensaml.xml.security.SecurityHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.method.P;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 *
 * @author mikkom
 */
@Service
@Transactional
public class TiedoteServiceImpl implements TiedoteService {

    @Autowired
    @Dto
    private DtoMapper mapper;

    @Autowired
    private TiedoteRepository repository;

    @Override
    @Transactional(readOnly = true)
    public List<TiedoteDto> getAll(boolean vainJulkiset, Long alkaen) {
        if (!SecurityUtil.isAuthenticated()) {
            vainJulkiset = true;
        }
        List<Tiedote> tiedotteet = repository.findAll(vainJulkiset, new Date(alkaen));
        return mapper.mapAsList(tiedotteet, TiedoteDto.class);
    }

    @Override
    @Transactional(readOnly = true)
    public TiedoteDto getTiedote(@P("tiedoteId") Long tiedoteId) {
        Tiedote tiedote = repository.findOne(tiedoteId);
        assertExists(tiedote, "Pyydettyä tiedotetta ei ole olemassa");
        if (!tiedote.isJulkinen() && !SecurityUtil.isAuthenticated()) {
            throw new BusinessRuleViolationException("Autentikoimaton käyttäjä voi lukea vain julkisia tiedotteita");
        }
        return mapper.map(tiedote, TiedoteDto.class);
    }

    @Override
    public TiedoteDto addTiedote(TiedoteDto tiedoteDto) {
        Tiedote tiedote = mapper.map(tiedoteDto, Tiedote.class);
        tiedote = repository.save(tiedote);
        return mapper.map(tiedote, TiedoteDto.class);
    }

    @Override
    public TiedoteDto updateTiedote(TiedoteDto tiedoteDto) {
        Tiedote tiedote = repository.findOne(tiedoteDto.getId());
        assertExists(tiedote, "Päivitettävää tietoa ei ole olemassa");
        mapper.map(tiedoteDto, tiedote);
        tiedote = repository.save(tiedote);
        return mapper.map(tiedote, TiedoteDto.class);
    }

    @Override
    public void removeTiedote(Long tiedoteId) {
        Tiedote tiedote = repository.findOne(tiedoteId);
        repository.delete(tiedote);
    }

    private static void assertExists(Object o, String msg) {
        if (o == null) {
            throw new BusinessRuleViolationException(msg);
        }
    }
}
