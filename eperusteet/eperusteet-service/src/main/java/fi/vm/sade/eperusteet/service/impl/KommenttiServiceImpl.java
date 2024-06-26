package fi.vm.sade.eperusteet.service.impl;

import fi.vm.sade.eperusteet.domain.Kommentti;
import fi.vm.sade.eperusteet.domain.PerusteenOsaViite;
import fi.vm.sade.eperusteet.domain.tutkinnonrakenne.TutkinnonOsaViite;
import fi.vm.sade.eperusteet.dto.KommenttiDto;
import fi.vm.sade.eperusteet.dto.kayttaja.KayttajanTietoDto;
import fi.vm.sade.eperusteet.repository.KommenttiRepository;
import fi.vm.sade.eperusteet.repository.PerusteenOsaViiteRepository;
import fi.vm.sade.eperusteet.repository.PerusteprojektiRepository;
import fi.vm.sade.eperusteet.repository.TutkinnonOsaViiteRepository;
import fi.vm.sade.eperusteet.service.KayttajanTietoService;
import fi.vm.sade.eperusteet.service.KommenttiService;
import fi.vm.sade.eperusteet.service.mapping.Dto;
import fi.vm.sade.eperusteet.service.mapping.DtoMapper;
import fi.vm.sade.eperusteet.service.security.PermissionChecker;
import fi.vm.sade.eperusteet.service.security.PermissionManager;
import fi.vm.sade.eperusteet.service.util.SecurityUtil;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class KommenttiServiceImpl implements KommenttiService {

    @Autowired
    KommenttiRepository kommentit;

    @Autowired
    private PermissionChecker permissionChecker;

    @Autowired
    @Dto
    private DtoMapper mapper;

    @Autowired
    private KayttajanTietoService kayttajat;

    @Autowired
    private PerusteprojektiRepository perusteprojektiRepository;

    @Autowired
    private PerusteenOsaViiteRepository povRepository;

    @Autowired
    private TutkinnonOsaViiteRepository tovRepository;

    @Override
    @Transactional(readOnly = true)
    public KommenttiDto get(Long kommenttiId) {
        Kommentti kommentti = kommentit.findById(kommenttiId).orElse(null);
        return mapper.map(kommentti, KommenttiDto.class);
    }

    @Override
    @Transactional(readOnly = true)
    public List<KommenttiDto> getAllByPerusteenOsa(Long perusteenOsaId) {
        List<Kommentti> re = kommentit.findAllByPerusteenOsa(perusteenOsaId);
        return mapper.mapAsList(re, KommenttiDto.class);
    }

    @Override
    @Transactional(readOnly = true)
    public List<KommenttiDto> getAllByPerusteenOsa(Long id, Long perusteenOsaId) {
        List<Kommentti> re = kommentit.findAllByPerusteenOsa(id, perusteenOsaId);
        return mapper.mapAsList(re, KommenttiDto.class);
    }

    @Override
    @Transactional(readOnly = true)
    public List<KommenttiDto> getAllBySuoritustapa(Long id, String suoritustapa) {
        List<Kommentti> re = kommentit.findAllBySuoritustapa(id, suoritustapa);
        return mapper.mapAsList(re, KommenttiDto.class);
    }

    @Override
    @Transactional(readOnly = true)
    public List<KommenttiDto> getAllByPerusteprojekti(Long id) {
        List<Kommentti> re = kommentit.findAllByPerusteprojekti(id);
        return mapper.mapAsList(re, KommenttiDto.class);
    }

    @Override
    @Transactional(readOnly = true)
    public List<KommenttiDto> getAllByParent(Long id) {
        List<Kommentti> re = kommentit.findAllByParent(id);
        return mapper.mapAsList(re, KommenttiDto.class);
    }

    @Override
    @Transactional(readOnly = true)
    public List<KommenttiDto> getAllByYlin(Long id) {
        List<Kommentti> re = kommentit.findAllByYlin(id);
        return mapper.mapAsList(re, KommenttiDto.class);
    }

    @Transactional
    private void addName(Kommentti k) {
        KayttajanTietoDto ktd = kayttajat.hae(k.getLuoja());
        if (ktd != null) {
            k.setNimi(ktd.getKutsumanimi() + " " + ktd.getSukunimi());
        }
    }

    private String clip(String kommentti) {
        if (kommentti != null) {
            int length = kommentti.length();
            return kommentti.substring(0, length < 1024 ? length : 1024);
        }
        else {
            return "";
        }
    }

    @Override
    @Transactional
    public KommenttiDto add(final KommenttiDto kommenttidto) {
        Kommentti kommentti = new Kommentti();
        kommentti.setSisalto(clip(kommenttidto.getSisalto()));
        kommentti.setPerusteprojektiId(kommenttidto.getPerusteprojektiId());
        kommentti.setSuoritustapa(kommenttidto.getSuoritustapa());
        kommentti.setPerusteenOsaId(kommenttidto.getPerusteenOsaId());

        if (kommentti.getSuoritustapa() == null && kommentti.getPerusteenOsaId() != null) {
            PerusteenOsaViite pov = povRepository.findOne(kommentti.getPerusteenOsaId());
            if (pov != null) {
//                kommentti.setSuoritustapa(pov.getSuoritustapa().getSuoritustapakoodi().name());
            }
            else {
                TutkinnonOsaViite tov = tovRepository.findOne(kommentti.getPerusteenOsaId());
                if (tov != null) {
                    kommentti.setSuoritustapa(tov.getSuoritustapa().getSuoritustapakoodi().name());
                }
            }
        }

        if (kommenttidto.getParentId() != null) {
            Kommentti parent = kommentit.findById(kommenttidto.getParentId()).orElse(null);
            kommentti.setParentId(parent.getId());
            kommentti.setYlinId(parent.getYlinId() == null ? parent.getId() : parent.getYlinId());
        }
        kommentti = kommentit.save(kommentti);
        addName(kommentti);
        return mapper.map(kommentti, KommenttiDto.class);
    }

    @Override
    @Transactional
    public KommenttiDto update(Long kommenttiId, final KommenttiDto kommenttidto) {
        Kommentti kommentti = kommentit.findById(kommenttiId).orElse(null);
        SecurityUtil.allow(kommentti.getLuoja());
        permissionChecker.checkPermission(kommentti.getPerusteprojektiId(), PermissionManager.Target.PERUSTEPROJEKTI, PermissionManager.Permission.LUKU);
        kommentti.setSisalto(clip(kommenttidto.getSisalto()));
        return mapper.map(kommentit.save(kommentti), KommenttiDto.class);
    }

    @Override
    @Transactional
    public void delete(Long kommenttiId) {
        Kommentti kommentti = kommentit.findById(kommenttiId).orElse(null);
        SecurityUtil.allow(kommentti.getLuoja());
        permissionChecker.checkPermission(kommentti.getPerusteprojektiId(),PermissionManager.Target.PERUSTEPROJEKTI, PermissionManager.Permission.LUKU);
        kommentti.setSisalto(null);
        kommentti.setPoistettu(true);
    }

    @Override
    @Transactional
    public void deleteReally(Long kommenttiId) {
        kommentit.deleteById(kommenttiId);
    }
}
