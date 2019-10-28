package fi.vm.sade.eperusteet.service.impl;

import com.google.common.collect.Sets;
import fi.vm.sade.eperusteet.domain.*;
import fi.vm.sade.eperusteet.dto.perusteprojekti.PerusteprojektiDto;
import fi.vm.sade.eperusteet.dto.perusteprojekti.PerusteprojektiImportDto;
import fi.vm.sade.eperusteet.repository.PerusteRepository;
import fi.vm.sade.eperusteet.repository.PerusteprojektiRepository;
import fi.vm.sade.eperusteet.repository.lops2019.Lops2019OppiaineRepository;
import fi.vm.sade.eperusteet.service.PerusteImport;
import fi.vm.sade.eperusteet.service.mapping.Dto;
import fi.vm.sade.eperusteet.service.mapping.DtoMapper;
import fi.vm.sade.eperusteet.service.mapping.UncachedDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import java.util.Set;
import java.util.Stack;

@Component
@Transactional
public class PerusteImportLops2019 implements PerusteImport {
    @Autowired
    private PerusteprojektiRepository perusteprojektiRepository;

    @Autowired
    private PerusteRepository perusteRepository;

    @Autowired
    private Lops2019OppiaineRepository lops2019OppiaineRepository;

    @Autowired
    private EntityManager em;

    @Autowired
    @UncachedDto
    private DtoMapper mapper;

    @Override
    public Set<KoulutustyyppiToteutus> getTyypit() {
        return Sets.newHashSet(KoulutustyyppiToteutus.LOPS2019);
    }

    private void resaveTekstikappaleet(final PerusteenOsaViite root) {
        final Stack<PerusteenOsaViite> stack = new Stack<>();
        stack.push(root);
        while (stack.size() > 0) {
            final PerusteenOsaViite head = stack.pop();
            if (head.getPerusteenOsa() != null) {
                head.getPerusteenOsa().setId(null);
                em.persist(head.getPerusteenOsa());
            }
            if (head.getLapset() != null) {
                stack.addAll(head.getLapset());
            }
        }
    }

    @Override
    public PerusteprojektiDto tuoPerusteprojekti(final PerusteprojektiImportDto projektiImport) {
        projektiImport.getProjekti().setKoulutustyyppi(KoulutusTyyppi.LUKIOKOULUTUS.toString());
        Perusteprojekti projekti = mapper.map(projektiImport.getProjekti(), Perusteprojekti.class);
        projekti.setId(null);
        projekti.setTila(ProjektiTila.LAADINTA);

        final Peruste peruste = mapper.map(projektiImport.getPeruste(), Peruste.class);
        peruste.asetaTila(PerusteTila.LUONNOS);
        peruste.setMaarayskirje(null);
        peruste.setMuutosmaaraykset(null);
        peruste.setId(null);
        peruste.setPerusteprojekti(projekti);
        peruste.setSisalto(peruste.getLops2019Sisalto().copy(true));
        this.resaveTekstikappaleet(peruste.getLops2019Sisalto().getSisalto());

        projekti.setPeruste(peruste);
        projekti = perusteprojektiRepository.save(projekti);
        return mapper.map(projekti, PerusteprojektiDto.class);
    }
}
