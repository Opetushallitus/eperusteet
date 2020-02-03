package fi.vm.sade.eperusteet.service.impl;

import fi.vm.sade.eperusteet.domain.Peruste;
import fi.vm.sade.eperusteet.domain.Perusteprojekti;
import fi.vm.sade.eperusteet.domain.Tiedote;
import fi.vm.sade.eperusteet.dto.TiedoteDto;
import fi.vm.sade.eperusteet.dto.kayttaja.KayttajanTietoDto;
import fi.vm.sade.eperusteet.dto.peruste.TiedoteQuery;
import fi.vm.sade.eperusteet.dto.util.PageDto;
import fi.vm.sade.eperusteet.repository.PerusteRepository;
import fi.vm.sade.eperusteet.repository.TiedoteRepository;
import fi.vm.sade.eperusteet.repository.TiedoteRepositoryCustom;
import fi.vm.sade.eperusteet.service.KayttajanTietoService;
import fi.vm.sade.eperusteet.service.TiedoteService;
import fi.vm.sade.eperusteet.service.exception.NotExistsException;
import fi.vm.sade.eperusteet.service.mapping.Dto;
import fi.vm.sade.eperusteet.service.mapping.DtoMapper;
import fi.vm.sade.eperusteet.service.util.SecurityUtil;
import java.util.Date;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.method.P;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 *
 * @author mikkom
 */
@Service
@Transactional
public class TiedoteServiceImpl implements TiedoteService {

    @Dto
    @Autowired
    private DtoMapper mapper;

    @Autowired
    private TiedoteRepository repository;

    @Autowired
    private TiedoteRepositoryCustom tiedoteRepositoryCustom;

    @Autowired
    private KayttajanTietoService kayttajat;

    @Autowired
    private PerusteRepository perusteRepository;

    @Override
    public Page<TiedoteDto> findBy(TiedoteQuery tquery) {
        // Sisäiset tiedotteet vaativat kirjautumisen
        if (!SecurityUtil.isAuthenticated()) {
            tquery.setJulkinen(true);
        }

        PageRequest pageRequest = new PageRequest(
                tquery.getSivu(),
                tquery.getSivukoko(),
                Sort.Direction.DESC,
                "luotu"
        );

        Page<Tiedote> tiedotteet = tiedoteRepositoryCustom.findBy(pageRequest, tquery);

        return new PageDto<>(tiedotteet, TiedoteDto.class, pageRequest, mapper);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TiedoteDto> getAll(boolean vainJulkiset, Long alkaen) {
        return getAll(vainJulkiset, alkaen, null);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TiedoteDto> getAll(boolean vainJulkiset, Long alkaen, Long perusteId) {
        if (!SecurityUtil.isAuthenticated()) {
            vainJulkiset = true;
        }

        List<Tiedote> tiedotteet;
        if (perusteId == null) {
            tiedotteet = repository.findAll(vainJulkiset, new Date(alkaen));
        }
        else {
            Peruste peruste = perusteRepository.findOne(perusteId);
            Perusteprojekti perusteprojekti = peruste.getPerusteprojekti();
            tiedotteet = repository.findAllByPerusteprojekti(vainJulkiset, new Date(alkaen), perusteprojekti);

        }
        return mapper.mapAsList(tiedotteet, TiedoteDto.class);
    }

    @Override
    @Transactional(readOnly = true)
    public TiedoteDto getTiedote(Long tiedoteId) {
        Tiedote tiedote = repository.findOne(tiedoteId);
        TiedoteDto tdto = mapper.map(tiedote, TiedoteDto.class);
        if (tdto != null && SecurityUtil.isAuthenticated()) {
            KayttajanTietoDto ktd = kayttajat.hae(tiedote.getLuoja());
            if (ktd != null) {
                tdto.setNimi(ktd.getKutsumanimi() + " " + ktd.getSukunimi());
            }
        }
        return tdto;
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
        tiedote.preupdate();
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
            throw new NotExistsException(msg);
        }
    }
}
