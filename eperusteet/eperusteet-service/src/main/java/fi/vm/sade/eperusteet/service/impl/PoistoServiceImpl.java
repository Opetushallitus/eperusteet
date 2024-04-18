package fi.vm.sade.eperusteet.service.impl;

import fi.vm.sade.eperusteet.domain.MuokkausTapahtuma;
import fi.vm.sade.eperusteet.domain.Peruste;
import fi.vm.sade.eperusteet.domain.Poistettava;
import fi.vm.sade.eperusteet.domain.PoistettuSisalto;
import fi.vm.sade.eperusteet.domain.Suoritustapa;
import fi.vm.sade.eperusteet.domain.Suoritustapakoodi;
import fi.vm.sade.eperusteet.domain.TekstiKappale;
import fi.vm.sade.eperusteet.dto.PoistettuSisaltoDto;
import fi.vm.sade.eperusteet.dto.peruste.PerusteenOsaViiteDto;
import fi.vm.sade.eperusteet.dto.peruste.TekstiKappaleDto;
import fi.vm.sade.eperusteet.repository.PerusteRepository;
import fi.vm.sade.eperusteet.repository.PerusteenOsaRepository;
import fi.vm.sade.eperusteet.repository.PerusteenOsaViiteRepository;
import fi.vm.sade.eperusteet.repository.PoistettuSisaltoRepository;
import fi.vm.sade.eperusteet.repository.TekstikappaleRepository;
import fi.vm.sade.eperusteet.service.PerusteService;
import fi.vm.sade.eperusteet.service.PerusteenMuokkaustietoService;
import fi.vm.sade.eperusteet.service.PoistoService;
import fi.vm.sade.eperusteet.service.exception.BusinessRuleViolationException;
import fi.vm.sade.eperusteet.service.mapping.Dto;
import fi.vm.sade.eperusteet.service.mapping.DtoMapper;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@Slf4j
public class PoistoServiceImpl implements PoistoService {

    @Autowired
    @Dto
    private DtoMapper mapper;

    @Autowired
    private PoistettuSisaltoRepository poistettuSisaltoRepository;

    @Autowired
    private PerusteRepository perusteRepository;

    @Autowired
    private PerusteenOsaRepository perusteenOsaRepository;

    @Autowired
    private PerusteenOsaViiteRepository perusteenOsaViiteRepository;

    @Autowired
    private PerusteService perusteService;

    @Autowired
    private PerusteenMuokkaustietoService perusteenMuokkaustietoService;

    @Autowired
    private TekstikappaleRepository tekstikappaleRepository;

    @Override
    public void restore(Long perusteId, Long poistoId) {
        PoistettuSisalto poistettuSisalto = poistettuSisaltoRepository.getOne(poistoId);
        if (!perusteId.equals(poistettuSisalto.getPeruste().getId())) {
            throw new BusinessRuleViolationException("vain-oman-voi-palauttaa");
        }

        PerusteenOsaViiteDto.Matala viite = null;

        switch (poistettuSisalto.getTyyppi()) {
            case TEKSTIKAPPALE:
                viite = palautaTekstikappale(perusteId, poistettuSisalto);
                break;
            default:
                throw new BusinessRuleViolationException("tunnistamaton-poistotyyppi");
        }

        if (viite != null) {
            perusteenMuokkaustietoService.addMuokkaustieto(perusteId, perusteenOsaViiteRepository.findOne(viite.getId()), MuokkausTapahtuma.PALAUTUS);
        }

        poistettuSisaltoRepository.deleteById(poistoId);
    }


    @Override
    public List<PoistettuSisaltoDto> getRemoved(Long perusteId) {
        List<PoistettuSisalto> poistetut = poistettuSisaltoRepository.findAllByPerusteId(perusteId);
        return mapper.mapAsList(poistetut, PoistettuSisaltoDto.class);
    }

    @Override
    public PoistettuSisaltoDto remove(Long perusteId, Poistettava poistettava) {
        if (poistettava.getPoistetunTyyppi() == null) {
            return null;
        }

        Peruste peruste = perusteRepository.findOne(perusteId);
        PoistettuSisalto poistettu = new PoistettuSisalto();
        poistettu.setNimi(poistettava.getNimi());
        poistettu.setPeruste(peruste);
        poistettu.setPoistettuId(poistettava.getId());
        poistettu.setTyyppi(poistettava.getPoistetunTyyppi());
        return mapper.map(poistettuSisaltoRepository.save(poistettu), PoistettuSisaltoDto.class);
    }

    private PerusteenOsaViiteDto.Matala palautaTekstikappale(Long perusteId, PoistettuSisalto poistettuSisalto) {
        TekstiKappale latest = tekstikappaleRepository.getLatestNotNull(poistettuSisalto.getPoistettuId());
        Peruste peruste = perusteRepository.findOne(perusteId);
        TekstiKappale tekstikappale = new TekstiKappale(latest);
        TekstiKappaleDto tekstikappaleDto = mapper.map(tekstikappale, TekstiKappaleDto.class);
        PerusteenOsaViiteDto.Matala viite = new PerusteenOsaViiteDto.Matala(tekstikappaleDto);
        return perusteService.addSisaltoUUSI(
                perusteId,
                peruste.getSuoritustavat().stream().map(Suoritustapa::getSuoritustapakoodi).findFirst().orElse(Suoritustapakoodi.REFORMI),
                viite);
    }

}
