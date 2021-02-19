package fi.vm.sade.eperusteet.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import fi.vm.sade.eperusteet.domain.*;
import fi.vm.sade.eperusteet.dto.TilaUpdateStatus;
import fi.vm.sade.eperusteet.dto.kayttaja.KayttajanTietoDto;
import fi.vm.sade.eperusteet.dto.peruste.JulkaisuBaseDto;
import fi.vm.sade.eperusteet.dto.peruste.PerusteKaikkiDto;
import fi.vm.sade.eperusteet.repository.JulkaisutRepository;
import fi.vm.sade.eperusteet.repository.PerusteRepository;
import fi.vm.sade.eperusteet.repository.PerusteprojektiRepository;
import fi.vm.sade.eperusteet.resource.config.InitJacksonConverter;
import fi.vm.sade.eperusteet.service.JulkaisutService;
import fi.vm.sade.eperusteet.service.KayttajanTietoService;
import fi.vm.sade.eperusteet.service.PerusteService;
import fi.vm.sade.eperusteet.service.PerusteenMuokkaustietoService;
import fi.vm.sade.eperusteet.service.PerusteprojektiService;
import fi.vm.sade.eperusteet.service.exception.BusinessRuleViolationException;
import fi.vm.sade.eperusteet.service.mapping.Dto;
import fi.vm.sade.eperusteet.service.mapping.DtoMapper;
import fi.vm.sade.eperusteet.service.util.JsonMapper;
import java.io.IOException;
import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;
import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

@Service
@Transactional
public class JulkaisutServiceImpl implements JulkaisutService {

    @Value("${fi.vm.sade.eperusteet.salli_virheelliset:false}")
    private boolean salliVirheelliset;

    @Dto
    @Autowired
    private DtoMapper mapper;

    @Autowired
    private PerusteRepository perusteRepository;

    @Autowired
    private PerusteprojektiRepository perusteprojektiRepository;

    @Autowired
    private JulkaisutRepository julkaisutRepository;

    @Autowired
    private PerusteprojektiService perusteprojektiService;

    @Autowired
    private PerusteService perusteService;

    @Autowired
    private PerusteenMuokkaustietoService muokkausTietoService;

    @Autowired
    private KayttajanTietoService kayttajanTietoService;

    private final ObjectMapper objectMapper = InitJacksonConverter.createMapper();

    @Override
    public List<JulkaisuBaseDto> getJulkaisut(long id) {
        Peruste peruste = perusteRepository.findOne(id);
        if (peruste == null) {
            throw new BusinessRuleViolationException("perustetta-ei-loytynyt");
        }

        List<JulkaistuPeruste> one = julkaisutRepository.findAllByPeruste(peruste);
        List<JulkaisuBaseDto> julkaisut = mapper.mapAsList(one, JulkaisuBaseDto.class);
        return taytaKayttajaTiedot(julkaisut);
    }

    @Override
    public JulkaisuBaseDto teeJulkaisu(long projektiId, JulkaisuBaseDto julkaisuBaseDto) {
        Perusteprojekti perusteprojekti = perusteprojektiRepository.findOne(projektiId);

        if (perusteprojekti == null) {
            throw new BusinessRuleViolationException("projektia-ei-ole");
        }

        Peruste peruste = perusteprojekti.getPeruste();

        if (peruste == null) {
            throw new BusinessRuleViolationException("perustetta-ei-ole");
        }

        if (julkaisuBaseDto.getPeruste() != null && !Objects.equals(peruste.getId(), julkaisuBaseDto.getPeruste().getId())) {
            throw new BusinessRuleViolationException("vain-oman-perusteen-voi-julkaista");
        }

        // Validoinnit
        PerusteKaikkiDto sisalto = perusteService.getKaikkiSisalto(peruste.getId());
        ObjectNode perusteDataJson = objectMapper.valueToTree(sisalto);
        List<JulkaistuPeruste> julkaisut = julkaisutRepository.findAllByPerusteOrderByRevisionDesc(perusteprojekti.getPeruste());
        if (julkaisut != null && julkaisut.size() > 0) {
            JulkaistuPeruste last = julkaisut.get(0);
            if (last.getData().getHash() == perusteDataJson.hashCode()) {
                throw new BusinessRuleViolationException("ei-muuttunut-viime-julkaisun-jalkeen");
            }
        }

        TilaUpdateStatus status = perusteprojektiService.validoiProjekti(projektiId, ProjektiTila.JULKAISTU);

        if (!salliVirheelliset && !status.isVaihtoOk()) {
            throw new BusinessRuleViolationException("projekti-ei-validi");
        }

        PerusteVersion version = peruste.getGlobalVersion();
        long julkaisutCount = julkaisutRepository.countByPeruste(peruste);

        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        JulkaistuPeruste julkaisu = new JulkaistuPeruste();
        julkaisu.setRevision((int) julkaisutCount);
        julkaisu.setTiedote(TekstiPalanen.of(julkaisuBaseDto.getTiedote().getTekstit()));
        julkaisu.setLuoja(username);
        julkaisu.setLuotu(version.getAikaleima());
        julkaisu.setPeruste(peruste);

        julkaisu.setData(new JulkaistuPerusteData(perusteDataJson));
        julkaisu = julkaisutRepository.save(julkaisu);
        {
            Cache amosaaperusteet = CacheManager.getInstance().getCache("amosaaperusteet");
            if (amosaaperusteet != null) {
                amosaaperusteet.removeAll();
            }
        }

        muokkausTietoService.addMuokkaustieto(peruste.getId(), peruste, MuokkausTapahtuma.JULKAISU);

        return taytaKayttajaTiedot(mapper.map(julkaisu, JulkaisuBaseDto.class));
    }

    private List<JulkaisuBaseDto> taytaKayttajaTiedot(List<JulkaisuBaseDto> julkaisut) {
        Map<String, KayttajanTietoDto> kayttajatiedot = kayttajanTietoService
                .haeKayttajatiedot(julkaisut.stream().map(JulkaisuBaseDto::getLuoja).collect(Collectors.toList()))
                .stream().collect(Collectors.toMap(kayttajanTieto -> kayttajanTieto.getOidHenkilo(), kayttajanTieto -> kayttajanTieto));
        julkaisut.forEach(julkaisu -> julkaisu.setKayttajanTieto(kayttajatiedot.get(julkaisu.getLuoja())));
        return julkaisut;
    }

    private JulkaisuBaseDto taytaKayttajaTiedot(JulkaisuBaseDto julkaisu) {
        return taytaKayttajaTiedot(Arrays.asList(julkaisu)).get(0);
    }

}
