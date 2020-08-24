package fi.vm.sade.eperusteet.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import fi.vm.sade.eperusteet.domain.*;
import fi.vm.sade.eperusteet.dto.TilaUpdateStatus;
import fi.vm.sade.eperusteet.dto.peruste.JulkaisuBaseDto;
import fi.vm.sade.eperusteet.dto.peruste.JulkaisuDto;
import fi.vm.sade.eperusteet.dto.peruste.PerusteKaikkiDto;
import fi.vm.sade.eperusteet.repository.JulkaisutRepository;
import fi.vm.sade.eperusteet.repository.PerusteRepository;
import fi.vm.sade.eperusteet.repository.PerusteprojektiRepository;
import fi.vm.sade.eperusteet.resource.config.InitJacksonConverter;
import fi.vm.sade.eperusteet.service.JulkaisutService;
import fi.vm.sade.eperusteet.service.PerusteService;
import fi.vm.sade.eperusteet.service.PerusteprojektiService;
import fi.vm.sade.eperusteet.service.exception.BusinessRuleViolationException;
import fi.vm.sade.eperusteet.service.mapping.Dto;
import fi.vm.sade.eperusteet.service.mapping.DtoMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.List;
import java.util.Objects;

@Service
@Transactional
public class JulkaisutServiceImpl implements JulkaisutService {

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
    public JulkaisuBaseDto teeJulkaisu(long perusteId, JulkaisuBaseDto julkaisuBaseDto) {
        Peruste peruste = perusteRepository.findOne(perusteId);

        if (peruste == null) {
            throw new BusinessRuleViolationException("perustetta-ei-ole");
        }

        Perusteprojekti perusteprojekti = peruste.getPerusteprojekti();

        if (perusteprojekti == null) {
            throw new BusinessRuleViolationException("projektia-ei-ole");
        }

        { // Validoinnit
            List<JulkaistuPeruste> julkaisut = julkaisutRepository.findAllByPerusteOrderByRevisionDesc(perusteprojekti.getPeruste());
            if (julkaisut != null && julkaisut.size() > 0) {
                JulkaistuPeruste last = julkaisut.get(julkaisut.size() - 1);
                if (last.getLuotu().compareTo(perusteprojekti.getPeruste().getGlobalVersion().getAikaleima()) == 0) {
                    throw new BusinessRuleViolationException("versiosta-jo-julkaisu");
                }
            }

            TilaUpdateStatus status = perusteprojektiService.validoiProjekti(perusteId, ProjektiTila.JULKAISTU);

            if (!status.isVaihtoOk()) {
                throw new BusinessRuleViolationException("projekti-ei-validi");
            }
        }

        PerusteVersion version = peruste.getGlobalVersion();
        long julkaisutCount = julkaisutRepository.countByPeruste(peruste);

        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        PerusteKaikkiDto sisalto = perusteService.getKokoSisalto(peruste.getId());
        JulkaistuPeruste julkaisu = new JulkaistuPeruste();
        julkaisu.setRevision((int)julkaisutCount);
        julkaisu.setTiedote(TekstiPalanen.of(Kieli.FI, "Julkaisu"));
        julkaisu.setLuoja(username);
        julkaisu.setLuotu(version.getAikaleima());
        julkaisu.setPeruste(peruste);

        ObjectNode data = objectMapper.valueToTree(sisalto);
        julkaisu.setData(new JulkaistuPerusteData(data));
        julkaisu = julkaisutRepository.save(julkaisu);
        return mapper.map(julkaisu, JulkaisuBaseDto.class);
    }

}
