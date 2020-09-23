package fi.vm.sade.eperusteet.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import fi.vm.sade.eperusteet.domain.*;
import fi.vm.sade.eperusteet.dto.TilaUpdateStatus;
import fi.vm.sade.eperusteet.dto.peruste.JulkaisuBaseDto;
import fi.vm.sade.eperusteet.dto.peruste.PerusteKaikkiDto;
import fi.vm.sade.eperusteet.repository.JulkaisutRepository;
import fi.vm.sade.eperusteet.repository.PerusteRepository;
import fi.vm.sade.eperusteet.repository.PerusteprojektiRepository;
import fi.vm.sade.eperusteet.resource.config.InitJacksonConverter;
import fi.vm.sade.eperusteet.service.JulkaisutService;
import fi.vm.sade.eperusteet.service.PerusteService;
import fi.vm.sade.eperusteet.service.PerusteenMuokkaustietoService;
import fi.vm.sade.eperusteet.service.PerusteprojektiService;
import fi.vm.sade.eperusteet.service.exception.BusinessRuleViolationException;
import fi.vm.sade.eperusteet.service.mapping.Dto;
import fi.vm.sade.eperusteet.service.mapping.DtoMapper;
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

    private final ObjectMapper objectMapper = InitJacksonConverter.createMapper();

    @Override
    public List<JulkaisuBaseDto> getJulkaisut(long id) {
        Peruste peruste = perusteRepository.findOne(id);
        if (peruste == null) {
            throw new BusinessRuleViolationException("perustetta-ei-loytynyt");
        }

        List<JulkaistuPeruste> one = julkaisutRepository.findAllByPeruste(peruste);
        List<JulkaisuBaseDto> julkaisut = mapper.mapAsList(one, JulkaisuBaseDto.class);
        return julkaisut;
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

        { // Validoinnit
            List<JulkaistuPeruste> julkaisut = julkaisutRepository.findAllByPerusteOrderByRevisionDesc(perusteprojekti.getPeruste());
            if (julkaisut != null && julkaisut.size() > 0) {
                JulkaistuPeruste last = julkaisut.get(julkaisut.size() - 1);
                if (last.getLuotu().compareTo(perusteprojekti.getPeruste().getGlobalVersion().getAikaleima()) == 0) {
                    throw new BusinessRuleViolationException("versiosta-jo-julkaisu");
                }
            }

            TilaUpdateStatus status = perusteprojektiService.validoiProjekti(projektiId, ProjektiTila.JULKAISTU);

            if (!salliVirheelliset && !status.isVaihtoOk()) {
                throw new BusinessRuleViolationException("projekti-ei-validi");
            }
        }

        PerusteVersion version = peruste.getGlobalVersion();
        long julkaisutCount = julkaisutRepository.countByPeruste(peruste);

        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        PerusteKaikkiDto sisalto = perusteService.getJulkaistuSisalto(peruste.getId());
        JulkaistuPeruste julkaisu = new JulkaistuPeruste();
        julkaisu.setRevision((int)julkaisutCount);
        julkaisu.setTiedote(TekstiPalanen.of(Kieli.FI, "Julkaisu"));
        julkaisu.setLuoja(username);
        julkaisu.setLuotu(version.getAikaleima());
        julkaisu.setPeruste(peruste);

        ObjectNode data = objectMapper.valueToTree(sisalto);
        julkaisu.setData(new JulkaistuPerusteData(data));
        julkaisu = julkaisutRepository.save(julkaisu);
        {
            Cache amosaaperusteet = CacheManager.getInstance().getCache("amosaaperusteet");
            if (amosaaperusteet != null) {
                amosaaperusteet.removeAll();
            }
        }

        muokkausTietoService.addMuokkaustieto(peruste.getId(), peruste, MuokkausTapahtuma.JULKAISU);

        return mapper.map(julkaisu, JulkaisuBaseDto.class);
    }

}
