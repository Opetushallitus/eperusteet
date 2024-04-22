package fi.vm.sade.eperusteet.service.impl;

import com.google.common.collect.Sets;
import fi.vm.sade.eperusteet.domain.OpasSisalto;
import fi.vm.sade.eperusteet.domain.Peruste;
import fi.vm.sade.eperusteet.domain.PerusteTila;
import fi.vm.sade.eperusteet.domain.PerusteTyyppi;
import fi.vm.sade.eperusteet.domain.Perusteprojekti;
import fi.vm.sade.eperusteet.domain.TekstiPalanen;
import fi.vm.sade.eperusteet.dto.opas.OpasDto;
import fi.vm.sade.eperusteet.dto.opas.OpasLuontiDto;
import fi.vm.sade.eperusteet.dto.peruste.PerusteHakuDto;
import fi.vm.sade.eperusteet.dto.peruste.PerusteQuery;
import fi.vm.sade.eperusteet.dto.peruste.PerusteprojektiQueryDto;
import fi.vm.sade.eperusteet.dto.perusteprojekti.PerusteprojektiKevytDto;
import fi.vm.sade.eperusteet.dto.util.PageDto;
import fi.vm.sade.eperusteet.repository.PerusteRepository;
import fi.vm.sade.eperusteet.repository.PerusteprojektiRepository;
import fi.vm.sade.eperusteet.service.LiiteService;
import fi.vm.sade.eperusteet.service.OpasService;
import fi.vm.sade.eperusteet.service.PerusteService;
import fi.vm.sade.eperusteet.service.PerusteprojektiService;
import fi.vm.sade.eperusteet.service.exception.BusinessRuleViolationException;
import fi.vm.sade.eperusteet.service.mapping.Dto;
import fi.vm.sade.eperusteet.service.mapping.DtoMapper;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;

import static fi.vm.sade.eperusteet.domain.ProjektiTila.LAADINTA;

@Service
@Transactional
public class OpasServiceImpl implements OpasService {

    @Autowired
    private PerusteprojektiService perusteprojektiService;

    @Autowired
    private PerusteprojektiRepository repository;

    @Autowired
    private PerusteRepository perusteRepository;

    @Autowired
    private PerusteRepository perusteet;

    @Autowired
    private PerusteService perusteService;

    @Autowired
    @Dto
    private DtoMapper mapper;

    @Autowired
    private LiiteService liiteService;

    @Override
    public OpasDto get(Long id) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public OpasDto save(OpasLuontiDto opasDto) {
        Perusteprojekti perusteprojekti = mapper.map(opasDto, Perusteprojekti.class);
        if (opasDto.getRyhmaOid() == null) {
            throw new BusinessRuleViolationException("Opastyöryhmää ei ole asetettu");
        }

        perusteprojekti.setTila(LAADINTA);
        perusteprojekti.setRyhmaOid(opasDto.getRyhmaOid());

        Peruste peruste = new Peruste();
        peruste.setTyyppi(PerusteTyyppi.OPAS);
        peruste.setSisalto(new OpasSisalto());
        peruste.setNimi(mapper.map(opasDto.getLokalisoituNimi(), TekstiPalanen.class));

        if (opasDto.getPohjaId() != null) {
            Peruste vanha = perusteRepository.getOne(opasDto.getPohjaId());
            peruste.setSisalto(vanha.getOppaanSisalto().kloonaa(peruste));
        }

        peruste.setOppaanKoulutustyypit(opasDto.getOppaanKoulutustyypit());
        if (!CollectionUtils.isEmpty(opasDto.getOppaanPerusteet())) {
            peruste.setOppaanPerusteet(Sets.newHashSet(mapper.mapAsList(opasDto.getOppaanPerusteet(), Peruste.class)));
        }

        perusteRepository.save(peruste);

        perusteprojekti.setPeruste(peruste);
        perusteprojekti = repository.saveAndFlush(perusteprojekti);

        if (opasDto.getPohjaId() != null) {
            liiteService.copyLiitteetForPeruste(peruste.getId(), opasDto.getPohjaId());
        }

        return mapper.map(perusteprojekti, OpasDto.class);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PerusteHakuDto> findBy(PageRequest page, PerusteQuery pquery) {
        pquery.setTila(PerusteTila.VALMIS.toString());
        pquery.setJulkaistu(true);
        pquery.setPerusteTyyppi(PerusteTyyppi.OPAS.toString());
        Page<Peruste> result = perusteRepository.findBy(page, pquery);
        PageDto<Peruste, PerusteHakuDto> resultDto = new PageDto<>(result, PerusteHakuDto.class, page, mapper);
        return resultDto;
    }

    @Override
    public Page<PerusteprojektiKevytDto> findProjektiBy(PageRequest p, PerusteprojektiQueryDto pquery) {
        pquery.setTyyppi(Arrays.asList(PerusteTyyppi.OPAS));
        Page<Perusteprojekti> projektit = repository.findBy(p, pquery);
        Page<PerusteprojektiKevytDto> result = projektit.map(pp -> {
            PerusteprojektiKevytDto ppk = mapper.map(pp, PerusteprojektiKevytDto.class);
            return ppk;
        });
        return result;
    }

}
