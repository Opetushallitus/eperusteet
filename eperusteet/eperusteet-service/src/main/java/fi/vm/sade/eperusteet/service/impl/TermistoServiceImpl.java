package fi.vm.sade.eperusteet.service.impl;

import fi.vm.sade.eperusteet.domain.HistoriaTapahtumaAuditointitiedoilla;
import fi.vm.sade.eperusteet.domain.MuokkausTapahtuma;
import fi.vm.sade.eperusteet.domain.Peruste;
import fi.vm.sade.eperusteet.domain.Termi;
import fi.vm.sade.eperusteet.dto.peruste.NavigationType;
import fi.vm.sade.eperusteet.dto.peruste.TermiDto;
import fi.vm.sade.eperusteet.repository.PerusteRepository;
import fi.vm.sade.eperusteet.repository.TermistoRepository;
import fi.vm.sade.eperusteet.service.PerusteenMuokkaustietoService;
import fi.vm.sade.eperusteet.service.TermistoService;
import fi.vm.sade.eperusteet.service.exception.NotExistsException;
import fi.vm.sade.eperusteet.service.mapping.Dto;
import fi.vm.sade.eperusteet.service.mapping.DtoMapper;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class TermistoServiceImpl implements TermistoService {

    @Autowired
    @Dto
    private DtoMapper mapper;

    @Autowired
    private TermistoRepository termisto;

    @Autowired
    private PerusteRepository perusteet;

    @Autowired
    private PerusteenMuokkaustietoService muokkausTietoService;

    @Override
    @Transactional(readOnly = true)
    public List<TermiDto> getTermit(Long perusteId) {
        List<Termi> termit = termisto.findByPerusteId(perusteId);
        return mapper.mapAsList(termit, TermiDto.class);
    }

    @Override
    public TermiDto getTermi(Long perusteId, String avain) {
        return mapper.map(termisto.findByPerusteIdAndAvain(perusteId, avain), TermiDto.class);
    }

    @Override
    public TermiDto addTermi(Long perusteId, TermiDto dto) {
        Peruste peruste = perusteet.findOne(perusteId);
        assertExists(peruste, "Perustetta ei ole olemassa");
        Termi tmp = mapper.map(dto, Termi.class);
        tmp.setPeruste(peruste);
        tmp = termisto.save(tmp);

        muokkausTietoService.addMuokkaustieto(perusteId, new HistoriaTapahtumaAuditointitiedoilla(tmp.getId(), tmp.getTermi(), NavigationType.termi), MuokkausTapahtuma.LUONTI);
        return mapper.map(tmp, TermiDto.class);
    }

    @Override
    public TermiDto updateTermi(Long perusteId, TermiDto dto) {
        Peruste peruste = perusteet.findOne(perusteId);
        assertExists(peruste, "Perustetta ei ole olemassa");
        Termi current = termisto.findById(dto.getId()).orElse(null);
        assertExists(current, "Päivitettävää tietoa ei ole olemassa");
        mapper.map(dto, current);
        termisto.save(current);

        muokkausTietoService.addMuokkaustieto(perusteId, new HistoriaTapahtumaAuditointitiedoilla(current.getId(), current.getTermi(), NavigationType.termi), MuokkausTapahtuma.PAIVITYS);
        return mapper.map(current, TermiDto.class);
    }

    @Override
    public void deleteTermi(Long perusteId, Long id) {
        Termi termi = termisto.findById(id).orElse(null);
        termisto.delete(termi);
        muokkausTietoService.addMuokkaustieto(perusteId, new HistoriaTapahtumaAuditointitiedoilla(termi.getId(), termi.getTermi(), NavigationType.termi), MuokkausTapahtuma.POISTO);

    }

    private static void assertExists(Object o, String msg) {
        if (o == null) {
            throw new NotExistsException(msg);
        }
    }
}
