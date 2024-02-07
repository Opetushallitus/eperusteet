package fi.vm.sade.eperusteet.service.impl.yl;

import fi.vm.sade.eperusteet.domain.Kieli;
import fi.vm.sade.eperusteet.domain.Peruste;
import fi.vm.sade.eperusteet.domain.PerusteenOsaTunniste;
import fi.vm.sade.eperusteet.domain.TekstiPalanen;
import fi.vm.sade.eperusteet.domain.yl.lukio.Aihekokonaisuudet;
import fi.vm.sade.eperusteet.domain.yl.lukio.Aihekokonaisuus;
import fi.vm.sade.eperusteet.domain.yl.lukio.LukiokoulutuksenPerusteenSisalto;
import fi.vm.sade.eperusteet.dto.yl.lukio.AihekokonaisuudetYleiskuvausDto;
import fi.vm.sade.eperusteet.dto.yl.lukio.AihekokonaisuusListausDto;
import fi.vm.sade.eperusteet.dto.yl.lukio.LukioAihekokonaisuusLuontiDto;
import fi.vm.sade.eperusteet.dto.yl.lukio.LukioAihekokonaisuusMuokkausDto;
import fi.vm.sade.eperusteet.repository.LukioAihekokonaisuudetRepository;
import fi.vm.sade.eperusteet.repository.LukioAihekokonaisuusRepository;
import fi.vm.sade.eperusteet.repository.LukiokoulutuksenPerusteenSisaltoRepository;
import fi.vm.sade.eperusteet.repository.PerusteRepository;
import fi.vm.sade.eperusteet.repository.version.Revision;
import fi.vm.sade.eperusteet.service.LokalisointiService;
import fi.vm.sade.eperusteet.service.exception.BusinessRuleViolationException;
import fi.vm.sade.eperusteet.service.exception.NotExistsException;
import fi.vm.sade.eperusteet.service.mapping.Dto;
import fi.vm.sade.eperusteet.service.mapping.DtoMapper;
import fi.vm.sade.eperusteet.service.yl.AihekokonaisuudetService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static fi.vm.sade.eperusteet.domain.yl.lukio.Aihekokonaisuus.inPeruste;
import static fi.vm.sade.eperusteet.service.util.OptionalUtil.found;

@Service
public class AihekokonaisuudetServiceImpl implements AihekokonaisuudetService {

    @Dto
    @Autowired
    private DtoMapper mapper;

    @Autowired
    private LukiokoulutuksenPerusteenSisaltoRepository lukioSisaltoRepository;

    @Autowired
    private LukioAihekokonaisuudetRepository lukioAihekokonaisuudetRepository;

    @Autowired
    private LukioAihekokonaisuusRepository lukioAihekokonaisuusRepository;

    @Autowired
    private PerusteRepository perusteet;

    @Autowired
    private LokalisointiService lokalisointiService;

    @Override
    @Transactional(readOnly = true)
    public List<AihekokonaisuusListausDto> getAihekokonaisuudet(Long perusteId) {
        return lokalisointiService.lokalisoi(
                lukioAihekokonaisuusRepository.findAihekokonaisuudetByPerusteId(perusteId));
    }


    @Override
    @Transactional(readOnly = true)
    public AihekokonaisuudetYleiskuvausDto getAihekokonaisuudetYleiskuvaus(Long perusteId) {
        Peruste peruste = perusteet.getOne(perusteId);
        Aihekokonaisuudet aihekokonaisuudet = peruste.getLukiokoulutuksenPerusteenSisalto().getAihekokonaisuudet();
        if (aihekokonaisuudet != null) {
            return mapper.map(aihekokonaisuudet, AihekokonaisuudetYleiskuvausDto.class);
        } else {
            return new AihekokonaisuudetYleiskuvausDto();
        }
    }

    @Override
    @Transactional(readOnly = true)
    public LukioAihekokonaisuusMuokkausDto getLukioAihekokobaisuusMuokkausById(long perusteId, long aihekokonaisuusId)
            throws NotExistsException {
        Aihekokonaisuus aihekokonaisuus = found(lukioAihekokonaisuusRepository
                .findOne(aihekokonaisuusId), inPeruste(perusteId));
        return mapper.map(aihekokonaisuus, new LukioAihekokonaisuusMuokkausDto());
    }

    @Override
    @Transactional
    public long luoAihekokonaisuus(long perusteId, LukioAihekokonaisuusLuontiDto aihekokonaisuusLuontiDto)
            throws BusinessRuleViolationException {
        LukiokoulutuksenPerusteenSisalto sisalto = found(lukioSisaltoRepository.findByPerusteId(perusteId),
                () -> new BusinessRuleViolationException("Perustetta ei ole."));
        lukioSisaltoRepository.lock(sisalto, false);

        Aihekokonaisuudet aihekokonaisuudet = sisalto.getAihekokonaisuudet();
        if (aihekokonaisuudet == null) {
            aihekokonaisuudet = initAihekokonaisuudet(sisalto);
            lukioAihekokonaisuudetRepository.saveAndFlush(aihekokonaisuudet);
        }

        Aihekokonaisuus aihekokonaisuus = mapper.map(aihekokonaisuusLuontiDto, new Aihekokonaisuus());
        aihekokonaisuus.setAihekokonaisuudet(aihekokonaisuudet);
        lukioAihekokonaisuusRepository.saveAndFlush(aihekokonaisuus);
        lukioAihekokonaisuusRepository.setRevisioKommentti(aihekokonaisuusLuontiDto.getMetadataOrEmpty().getKommentti());
        return aihekokonaisuus.getId();
    }

    @Override
    @SuppressWarnings({"TransactionalAnnotations", "ServiceMethodEntity"})
    public Aihekokonaisuudet initAihekokonaisuudet(LukiokoulutuksenPerusteenSisalto sisalto) {
        Aihekokonaisuudet aihekokonaisuudet;
        aihekokonaisuudet = new Aihekokonaisuudet();
        //Asetetaan oletusotsikko
        HashMap<Kieli, String> hm = new HashMap<>();
        hm.put(Kieli.FI, "Aihekokonaisuudet");
        aihekokonaisuudet.setOtsikko(TekstiPalanen.of(hm));
        aihekokonaisuudet.setSisalto(sisalto);
        aihekokonaisuudet.setNimi(TekstiPalanen.of(Kieli.FI, "Aihekokonaisuudet"));
        aihekokonaisuudet.setTunniste(PerusteenOsaTunniste.NORMAALI);
        aihekokonaisuudet.getViite().setPerusteenOsa(aihekokonaisuudet);
        aihekokonaisuudet.getViite().setVanhempi(sisalto.getSisalto());
        sisalto.getSisalto().getLapset().add(aihekokonaisuudet.getViite());
        sisalto.setAihekokonaisuudet(aihekokonaisuudet);
        return aihekokonaisuudet;
    }

    @Override
    @Transactional
    public void muokkaaAihekokonaisuutta(long perusteId, LukioAihekokonaisuusMuokkausDto lukioAihekokonaisuusMuokkausDto)
                    throws NotExistsException {
        Aihekokonaisuus aihekokonaisuus = found(lukioAihekokonaisuusRepository
                .findOne(lukioAihekokonaisuusMuokkausDto.getId()), inPeruste(perusteId));
        lukioAihekokonaisuusRepository.lock(aihekokonaisuus, false);
        mapper.map(lukioAihekokonaisuusMuokkausDto, aihekokonaisuus);
        lukioAihekokonaisuusRepository.setRevisioKommentti(lukioAihekokonaisuusMuokkausDto.getMetadataOrEmpty().getKommentti());
    }

    @Override
    @Transactional
    public void tallennaYleiskuvaus(Long perusteId, AihekokonaisuudetYleiskuvausDto aihekokonaisuudetYleiskuvausDto) {
        Peruste peruste = perusteet.getOne(perusteId);
        LukiokoulutuksenPerusteenSisalto sisalto = peruste.getLukiokoulutuksenPerusteenSisalto();
        Aihekokonaisuudet aihekokonaisuudet = sisalto.getAihekokonaisuudet();
        if (aihekokonaisuudet == null) {
            aihekokonaisuudet = initAihekokonaisuudet(sisalto);
        }
        mapper.map(aihekokonaisuudetYleiskuvausDto, aihekokonaisuudet);

        // Uusi, tallennetaan.
        if (aihekokonaisuudet.getId() == null) {
            aihekokonaisuudet.setSisalto(peruste.getLukiokoulutuksenPerusteenSisalto());
            peruste.getLukiokoulutuksenPerusteenSisalto().setAihekokonaisuudet(aihekokonaisuudet);
            lukioAihekokonaisuudetRepository.saveAndFlush(aihekokonaisuudet);
        }

        lukioAihekokonaisuudetRepository.setRevisioKommentti(aihekokonaisuudetYleiskuvausDto.getMetadataOrEmpty().getKommentti());
    }

    @Override
    @Transactional
    public void poistaAihekokonaisuus(long perusteId, long aihekokonaisuusId) throws NotExistsException {
        Aihekokonaisuus aihekokonaisuus = found(lukioAihekokonaisuusRepository
                .findOne(aihekokonaisuusId), inPeruste(perusteId));
        lukioAihekokonaisuusRepository.delete(aihekokonaisuus);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Revision> getAihekokonaisuudetYleiskuvausVersiot(long perusteId) {
        Peruste peruste = perusteet.getOne(perusteId);
        LukiokoulutuksenPerusteenSisalto sisalto = peruste.getLukiokoulutuksenPerusteenSisalto();
        Aihekokonaisuudet aihekokonaisuudet = sisalto.getAihekokonaisuudet();
        if (aihekokonaisuudet != null) {
            return lukioAihekokonaisuudetRepository.getRevisions(aihekokonaisuudet.getId());
        } else {
            return new ArrayList<Revision>();
        }
    }

    @Override
    @Transactional(readOnly = true)
    public AihekokonaisuudetYleiskuvausDto getAihekokonaisuudetYleiskuvausByVersion(long perusteId, int revisio) {
        Peruste peruste = perusteet.getOne(perusteId);
        Aihekokonaisuudet aihekokonaisuudet = peruste.getLukiokoulutuksenPerusteenSisalto().getAihekokonaisuudet();
        if (aihekokonaisuudet != null) {
            Aihekokonaisuudet rev = lukioAihekokonaisuudetRepository.findRevision(aihekokonaisuudet.getId(), revisio);
            return mapper.map(rev, AihekokonaisuudetYleiskuvausDto.class);
        } else {
            return new AihekokonaisuudetYleiskuvausDto();
        }
    }

    @Override
    @Transactional
    public AihekokonaisuudetYleiskuvausDto palautaAihekokonaisuudetYleiskuvaus(long perusteId, int revisio) {
        AihekokonaisuudetYleiskuvausDto dto = getAihekokonaisuudetYleiskuvausByVersion(perusteId, revisio);
        tallennaYleiskuvaus(perusteId, dto);
        return dto;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Revision> getAihekokonaisuusVersiot(long perusteId, long aihekokonaisuusId) {
        Aihekokonaisuus aihekokonaisuus = found(lukioAihekokonaisuusRepository
                .findOne(aihekokonaisuusId), inPeruste(perusteId));
        return lukioAihekokonaisuusRepository.getRevisions(aihekokonaisuus.getId());
    }

    @Override
    @Transactional(readOnly = true)
    public LukioAihekokonaisuusMuokkausDto getAihekokonaisuusByVersion(long perusteId, long aihekokonaisuusId, int revisio) {
        Aihekokonaisuus aihekokonaisuus = found(lukioAihekokonaisuusRepository
                .findOne(aihekokonaisuusId), inPeruste(perusteId));
        Aihekokonaisuus rev = lukioAihekokonaisuusRepository.findRevision(aihekokonaisuus.getId(), revisio);
        LukioAihekokonaisuusMuokkausDto dto = mapper.map(rev, new LukioAihekokonaisuusMuokkausDto());
        return dto;
    }

    @Override
    @Transactional
    public LukioAihekokonaisuusMuokkausDto palautaAihekokonaisuus(long perusteId, long aihekokonaisuusId, int revisio) {
        LukioAihekokonaisuusMuokkausDto dto = getAihekokonaisuusByVersion(perusteId, aihekokonaisuusId, revisio);
        muokkaaAihekokonaisuutta(perusteId, dto);
        return dto;
    }
}
