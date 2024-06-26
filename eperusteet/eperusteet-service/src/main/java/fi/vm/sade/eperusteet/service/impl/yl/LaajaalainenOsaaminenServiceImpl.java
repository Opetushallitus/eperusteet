package fi.vm.sade.eperusteet.service.impl.yl;

import fi.vm.sade.eperusteet.domain.MuokkausTapahtuma;
import fi.vm.sade.eperusteet.domain.yl.LaajaalainenOsaaminen;
import fi.vm.sade.eperusteet.domain.yl.PerusopetuksenPerusteenSisalto;
import fi.vm.sade.eperusteet.dto.yl.LaajaalainenOsaaminenDto;
import fi.vm.sade.eperusteet.repository.LaajaalainenOsaaminenRepository;
import fi.vm.sade.eperusteet.repository.PerusopetuksenPerusteenSisaltoRepository;
import fi.vm.sade.eperusteet.repository.version.Revision;
import fi.vm.sade.eperusteet.service.LockCtx;
import fi.vm.sade.eperusteet.service.LockService;
import fi.vm.sade.eperusteet.service.PerusteenMuokkaustietoService;
import fi.vm.sade.eperusteet.service.exception.NotExistsException;
import fi.vm.sade.eperusteet.service.mapping.Dto;
import fi.vm.sade.eperusteet.service.mapping.DtoMapper;
import fi.vm.sade.eperusteet.service.yl.LaajaalainenOsaaminenContext;
import fi.vm.sade.eperusteet.service.yl.LaajaalainenOsaaminenService;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class LaajaalainenOsaaminenServiceImpl implements LaajaalainenOsaaminenService {

    @Autowired
    private PerusopetuksenPerusteenSisaltoRepository sisaltoRepository;
    @Autowired
    private LaajaalainenOsaaminenRepository osaaminenRepository;

    @Autowired
    @LockCtx(LaajaalainenOsaaminenContext.class)
    private LockService<LaajaalainenOsaaminenContext> lockService;

    @Autowired
    @Dto
    private DtoMapper mapper;

    @Autowired
    private PerusteenMuokkaustietoService muokkausTietoService;

    @Override
    public LaajaalainenOsaaminenDto addLaajaalainenOsaaminen(Long perusteId, LaajaalainenOsaaminenDto dto) {
        PerusopetuksenPerusteenSisalto sisalto = sisaltoRepository.findByPerusteId(perusteId);
        notNull(sisalto, "Päivitettävää tietoa ei ole olemassa");
        LaajaalainenOsaaminen tmp = mapper.map(dto, LaajaalainenOsaaminen.class);
        sisaltoRepository.lock(sisalto);
        tmp = osaaminenRepository.save(tmp);
        sisalto.addLaajaalainenosaaminen(tmp);

        muokkausTietoService.addMuokkaustieto(perusteId, tmp, MuokkausTapahtuma.LUONTI);
        return mapper.map(tmp, LaajaalainenOsaaminenDto.class);
    }

    @Override
    public LaajaalainenOsaaminenDto updateLaajaalainenOsaaminen(Long perusteId, LaajaalainenOsaaminenDto dto) {
        LaajaalainenOsaaminen current = osaaminenRepository.findBy(perusteId, dto.getId());
        notNull(current, "Päivitettävää tietoa ei ole olemassa");
        lockService.assertLock(LaajaalainenOsaaminenContext.of(perusteId, dto.getId()));
        osaaminenRepository.lock(current);
        mapper.map(dto, current);
        osaaminenRepository.save(current);
        muokkausTietoService.addMuokkaustieto(perusteId, current, MuokkausTapahtuma.PAIVITYS);
        return mapper.map(current, LaajaalainenOsaaminenDto.class);
    }

    @Override
    @Transactional(readOnly = true)
    public LaajaalainenOsaaminenDto getLaajaalainenOsaaminen(Long perusteId, Long id) {
        LaajaalainenOsaaminen osaaminen = osaaminenRepository.findBy(perusteId, id);
        return mapper.map(osaaminen, LaajaalainenOsaaminenDto.class);
    }

    @Override
    @Transactional(readOnly = true)
    public LaajaalainenOsaaminenDto getLaajaalainenOsaaminen(Long perusteId, Long id, int revisio) {
        PerusopetuksenPerusteenSisalto sisalto = sisaltoRepository.findRevision(id, revisio);
        notNull(sisalto, "Perustetta ei ole olemassa");
        return mapper.map(sisalto.getLaajaalainenosaaminen(id), LaajaalainenOsaaminenDto.class);
    }

    @Override
    public List<Revision> getLaajaalainenOsaaminenVersiot(Long perusteId, Long id) {
        PerusopetuksenPerusteenSisalto sisalto = sisaltoRepository.findByPerusteId(perusteId);
        notNull(notNull(sisalto, "Perustetta ei ole olemassa").getLaajaalainenosaaminen(id), "Laaja-alaista osaamista ei ole olemassa");
        return osaaminenRepository.getRevisions(id);
    }

    @Override
    public void deleteLaajaalainenOsaaminen(Long perusteId, Long id) {
        PerusopetuksenPerusteenSisalto sisalto = sisaltoRepository.findByPerusteId(perusteId);
        LaajaalainenOsaaminen lo = notNull(sisalto, "Perustetta ei ole olemassa").getLaajaalainenosaaminen(id);
        notNull(lo, "Laaja-alaista osaamista ei ole olemassa");
        final LaajaalainenOsaaminenContext ctx = LaajaalainenOsaaminenContext.of(perusteId, id);
        lockService.assertLock(ctx);
        //lukko täytyy poistaa ennen varsinaista poistoa (turvallista koska poisto ei näy transaktion ulkopuolelle)
        lockService.unlock(ctx);
        //lukitus tarvitaan koska sisällön versio muuttuu ja yhtäaikainen versioiden teko rikkoo enversin auditoinnin
        sisaltoRepository.lock(sisalto, false);
        sisalto.removeLaajaalainenosaaminen(lo);

        muokkausTietoService.addMuokkaustieto(perusteId, lo, MuokkausTapahtuma.POISTO);
        // Poista laaja-alainen osaamisen jos siihen ei ole enää viittauksia
        osaaminenRepository.delete(lo);
    }

    private static <T> T notNull(T o, String msg) {
        if (o == null) {
            throw new NotExistsException(msg);
        }
        return o;
    }
}
