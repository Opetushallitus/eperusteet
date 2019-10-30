package fi.vm.sade.eperusteet.service.impl;

import com.google.common.collect.Sets;
import fi.vm.sade.eperusteet.domain.*;
import fi.vm.sade.eperusteet.domain.lops2019.oppiaineet.Lops2019Oppiaine;
import fi.vm.sade.eperusteet.domain.lops2019.oppiaineet.moduuli.Lops2019Moduuli;
import fi.vm.sade.eperusteet.dto.lops2019.Lops2019OppiaineKaikkiDto;
import fi.vm.sade.eperusteet.dto.lops2019.oppiaineet.moduuli.Lops2019ModuuliDto;
import fi.vm.sade.eperusteet.dto.peruste.PerusteKaikkiDto;
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
import java.util.stream.Collectors;

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

    private Lops2019Oppiaine mapOppiaine(Lops2019OppiaineKaikkiDto oa) {
        Lops2019Oppiaine result = mapper.map(oa, Lops2019Oppiaine.class);
        result.setOppimaarat(oa.getOppimaarat().stream()
            .map(this::mapOppiaine)
            .collect(Collectors.toList()));
        result.setModuulit(oa.getModuulit().stream()
            .map(moduuli -> mapper.map(moduuli, Lops2019Moduuli.class))
            .collect(Collectors.toList()));
        return result;
    }

    @Override
    public PerusteprojektiDto tuoPerusteprojekti(final PerusteprojektiImportDto projektiImport) {
        PerusteKaikkiDto perusteDto = projektiImport.getPeruste();
        perusteDto.setKoulutustyyppi(KoulutusTyyppi.LUKIOKOULUTUS.toString());
        Perusteprojekti projekti = mapper.map(projektiImport.getProjekti(), Perusteprojekti.class);
        projekti.setId(null);
        projekti.setTila(ProjektiTila.LAADINTA);
        projekti.setNimi(projektiImport.getProjekti().getNimi());

        final Peruste peruste = mapper.map(perusteDto, Peruste.class);
        peruste.getLops2019Sisalto().setOppiaineet(perusteDto.getLops2019Sisalto().getOppiaineet().stream()
            .map(this::mapOppiaine)
            .collect(Collectors.toList()));
        peruste.setNimi(TekstiPalanen.of(perusteDto.getNimi() != null ? perusteDto.getNimi().getTekstit() : null));
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
