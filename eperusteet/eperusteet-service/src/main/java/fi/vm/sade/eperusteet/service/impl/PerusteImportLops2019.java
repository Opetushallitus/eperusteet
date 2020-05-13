package fi.vm.sade.eperusteet.service.impl;

import com.google.common.collect.Sets;
import fi.vm.sade.eperusteet.domain.*;
import fi.vm.sade.eperusteet.domain.liite.Liite;
import fi.vm.sade.eperusteet.domain.lops2019.oppiaineet.Lops2019Oppiaine;
import fi.vm.sade.eperusteet.domain.lops2019.oppiaineet.moduuli.Lops2019Moduuli;
import fi.vm.sade.eperusteet.dto.MuutosmaaraysDto;
import fi.vm.sade.eperusteet.dto.liite.LiiteBaseDto;
import fi.vm.sade.eperusteet.dto.liite.LiiteDto;
import fi.vm.sade.eperusteet.dto.lops2019.Lops2019OppiaineKaikkiDto;
import fi.vm.sade.eperusteet.dto.peruste.PerusteKaikkiDto;
import fi.vm.sade.eperusteet.dto.peruste.TermiDto;
import fi.vm.sade.eperusteet.dto.perusteprojekti.PerusteprojektiDto;
import fi.vm.sade.eperusteet.dto.perusteprojekti.PerusteprojektiImportDto;
import fi.vm.sade.eperusteet.repository.PerusteRepository;
import fi.vm.sade.eperusteet.repository.PerusteprojektiRepository;
import fi.vm.sade.eperusteet.repository.TermistoRepository;
import fi.vm.sade.eperusteet.repository.liite.LiiteRepository;
import fi.vm.sade.eperusteet.repository.lops2019.Lops2019OppiaineRepository;
import fi.vm.sade.eperusteet.service.PerusteImport;
import fi.vm.sade.eperusteet.service.mapping.DtoMapper;
import fi.vm.sade.eperusteet.service.mapping.UncachedDto;
import java.util.List;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;

import javax.persistence.EntityManager;
import java.util.*;
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
    private LiiteRepository liiteRepository;

    @Autowired
    private TermistoRepository termistoRepository;

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
        if (!CollectionUtils.isEmpty(peruste.getPerusteenAikataulut())) {
            List<PerusteAikataulu> aikataulut = mapper.mapAsList(peruste.getPerusteenAikataulut(), PerusteAikataulu.class).stream().map(aikataulu -> {
                aikataulu.setPeruste(peruste);
                return aikataulu;
            }).collect(Collectors.toList());
            peruste.setPerusteenAikataulut(aikataulut);
        }
        this.resaveTekstikappaleet(peruste.getLops2019Sisalto().getSisalto());

        projekti.setPeruste(peruste);
        projekti = perusteprojektiRepository.save(projekti);

        // Liitteet
        for (LiiteDto liiteDto : projektiImport.getLiitteet()) {
            UUID uuid = liiteDto.getId();
            // Jos liite löytyy jo, käytetään olemassa olevaa
            Liite liite = liiteRepository.findOne(uuid);
            if (liite == null) {
                Liite newLiite = liiteRepository.add(
                        uuid,
                        liiteDto.getTyyppi(),
                        liiteDto.getMime(),
                        liiteDto.getNimi(),
                        projektiImport.getLiitetiedostot().get(uuid)
                );
                peruste.attachLiite(newLiite);
            } else {
                peruste.attachLiite(liite);
            }
        }

        // Määräyskirje
        if (perusteDto.getMaarayskirje() != null) {
            Maarayskirje maarayskirje = mapper.map(perusteDto.getMaarayskirje(), Maarayskirje.class);
            maarayskirje.setId(null);
            maarayskirje.getLiitteet().clear();
            for (Map.Entry<Kieli, LiiteBaseDto> entry : perusteDto.getMaarayskirje().getLiitteet().entrySet()) {
                Kieli kieli = entry.getKey();
                LiiteBaseDto liiteDto = entry.getValue();
                if (liiteDto.getId() != null) {
                    Liite liite = liiteRepository.findOne(peruste.getId(), liiteDto.getId());
                    maarayskirje.getLiitteet().put(kieli, liite);
                }
            }
            peruste.setMaarayskirje(maarayskirje);
        }

        // Muutosmääräykset
        if (!ObjectUtils.isEmpty(perusteDto.getMuutosmaaraykset())) {
            ArrayList<Muutosmaarays> muutosmaaraykset = new ArrayList<>();
            for (MuutosmaaraysDto muutosmaaraysDto : perusteDto.getMuutosmaaraykset()) {
                Muutosmaarays muutosmaarays = mapper.map(muutosmaaraysDto, Muutosmaarays.class);
                muutosmaarays.setId(null);
                muutosmaarays.setPeruste(peruste);
                muutosmaarays.getLiitteet().clear();
                for (Map.Entry<Kieli, LiiteBaseDto> entry : muutosmaaraysDto.getLiitteet().entrySet()) {
                    Kieli kieli = entry.getKey();
                    LiiteBaseDto liiteDto = entry.getValue();
                    if (liiteDto.getId() != null) {
                        Liite liite = liiteRepository.findOne(peruste.getId(), liiteDto.getId());
                        muutosmaarays.getLiitteet().put(kieli, liite);
                    }
                }
                muutosmaaraykset.add(muutosmaarays);
            }
            peruste.setMuutosmaaraykset(muutosmaaraykset);
        }

        // Termit
        for (TermiDto termiDto : projektiImport.getTermit()) {
            Termi termi = mapper.map(termiDto, Termi.class);
            termi.setId(null);
            termi.setPeruste(peruste);
            termistoRepository.save(termi);
        }


        return mapper.map(projekti, PerusteprojektiDto.class);
    }
}
