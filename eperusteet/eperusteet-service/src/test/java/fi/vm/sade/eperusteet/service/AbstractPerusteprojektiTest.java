package fi.vm.sade.eperusteet.service;


import fi.vm.sade.eperusteet.domain.*;
import fi.vm.sade.eperusteet.domain.arviointi.ArviointiAsteikko;
import fi.vm.sade.eperusteet.domain.tutkinnonosa.TutkinnonOsaTyyppi;
import fi.vm.sade.eperusteet.domain.tutkinnonrakenne.RakenneModuuli;
import fi.vm.sade.eperusteet.dto.arviointi.ArviointiAsteikkoDto;
import fi.vm.sade.eperusteet.dto.peruste.PerusteDto;
import fi.vm.sade.eperusteet.dto.peruste.PerusteenOsaViiteDto;
import fi.vm.sade.eperusteet.dto.peruste.TekstiKappaleDto;
import fi.vm.sade.eperusteet.dto.perusteprojekti.PerusteprojektiDto;
import fi.vm.sade.eperusteet.dto.tutkinnonosa.TutkinnonOsaDto;
import fi.vm.sade.eperusteet.dto.tutkinnonrakenne.KoodiDto;
import fi.vm.sade.eperusteet.dto.tutkinnonrakenne.RakenneModuuliDto;
import fi.vm.sade.eperusteet.dto.tutkinnonrakenne.TutkinnonOsaViiteDto;
import fi.vm.sade.eperusteet.dto.util.LokalisoituTekstiDto;
import fi.vm.sade.eperusteet.repository.PerusteRepository;
import fi.vm.sade.eperusteet.repository.PerusteenOsaRepository;
import fi.vm.sade.eperusteet.repository.PerusteprojektiRepository;
import fi.vm.sade.eperusteet.service.mapping.Dto;
import fi.vm.sade.eperusteet.service.mapping.DtoMapper;
import fi.vm.sade.eperusteet.service.test.AbstractIntegrationTest;
import fi.vm.sade.eperusteet.service.test.util.PerusteprojektiTestUtils;
import java.util.Collection;
import java.util.Set;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@DirtiesContext
@Transactional
abstract public class AbstractPerusteprojektiTest extends AbstractIntegrationTest {

    @Autowired
    protected PerusteprojektiService perusteprojektiService;

    @Autowired
    protected PerusteprojektiTestUtils testUtils;

    @Autowired
    protected PerusteService perusteService;

    @Autowired
    protected PerusteenOsaService perusteenOsaService;

    @Autowired
    private PerusteenOsaRepository perusteenOsaRepo;

    @Autowired
    protected PerusteprojektiRepository perusteprojektiRepository;

    @Autowired
    protected PerusteRepository perusteRepository;

    @Autowired
    protected EntityManager em;

    @Autowired
    @Dto
    protected DtoMapper mapper;

    @Autowired
    @LockCtx(TutkinnonRakenneLockContext.class)
    protected LockService<TutkinnonRakenneLockContext> lockService;

    protected Perusteprojekti projekti;
    protected Peruste peruste;
    protected Suoritustapa suoritustapa;
    protected RakenneModuuli rakenne;

    protected void setup() {
        PerusteprojektiDto projektiDto = testUtils.createPerusteprojekti();
        projekti = perusteprojektiRepository.findOne(projektiDto.getId());
        peruste = projekti.getPeruste();
        suoritustapa = peruste.getSuoritustapa(Suoritustapakoodi.REFORMI);
        rakenne = suoritustapa.getRakenne();
    }


    protected ArviointiAsteikkoDto addArviointiasteikot() {
        TekstiPalanen osaamistasoOtsikko = TekstiPalanen.of(Collections.singletonMap(Kieli.FI, "otsikko"));
        em.persist(osaamistasoOtsikko);

        List<Osaamistaso> osaamistasot = Stream.of(
                Osaamistaso.builder()
                        .id(2L)
                        .otsikko(TekstiPalanen.of(Kieli.FI, "Taso 1")).build(),
                Osaamistaso.builder()
                        .id(3L)
                        .otsikko(TekstiPalanen.of(Kieli.FI, "Taso 2")).build(),
                Osaamistaso.builder()
                        .id(4L)
                        .otsikko(TekstiPalanen.of(Kieli.FI, "Taso 3")).build())
                .peek(em::persist)
                .collect(Collectors.toList());


        ArviointiAsteikko asteikko = ArviointiAsteikko.builder()
                .id(1L)
                .osaamistasot(osaamistasot)
                .build();

        em.persist(asteikko);
        em.flush();
        return mapper.map(asteikko, ArviointiAsteikkoDto.class);
    }

    protected RakenneModuuliDto getRakenneDto() {
        return perusteService.getTutkinnonRakenne(peruste.getId(), suoritustapa.getSuoritustapakoodi(), null);
    }

    protected PerusteDto update(PerusteDto perusteDto) {
        return perusteService.update(perusteDto.getId(), perusteDto);
    }

    protected RakenneModuuliDto update(RakenneModuuliDto rakenne) {
        final TutkinnonRakenneLockContext ctx = TutkinnonRakenneLockContext.of(peruste.getId(), Suoritustapakoodi.REFORMI);
        lockService.lock(ctx);
        RakenneModuuliDto updated = perusteService.updateTutkinnonRakenne(peruste.getId(), suoritustapa.getSuoritustapakoodi(), rakenne);
        return updated;
    }

    protected TutkinnonOsaViiteDto uusiTutkinnonOsa() {
        TutkinnonOsaViiteDto result = perusteService.addTutkinnonOsa(peruste.getId(), suoritustapa.getSuoritustapakoodi(), TutkinnonOsaViiteDto.builder()
                .tyyppi(TutkinnonOsaTyyppi.NORMAALI)
                .tutkinnonOsaDto(TutkinnonOsaDto.builder()
                        .build())
                .build());
        return result;
    }

    protected PerusteenOsaViiteDto.Matala uusiTekstiKappale(LokalisoituTekstiDto nimi, LokalisoituTekstiDto teksti, KoodiDto osaamisala) {
        PerusteenOsaViiteDto.Matala pov = new PerusteenOsaViiteDto.Matala();
        TekstiKappaleDto tk = new TekstiKappaleDto();
        pov.setPerusteenOsa(tk);
        PerusteenOsaViiteDto.Matala result = perusteService.addSisaltoUUSI(peruste.getId(), suoritustapa.getSuoritustapakoodi(), pov);

        tk = (TekstiKappaleDto) result.getPerusteenOsa();
        tk.setNimi(nimi);
        tk.setTeksti(teksti);
        tk.setOsaamisala(osaamisala);

        perusteenOsaService.lock(tk.getId());
        perusteenOsaService.update(tk);

        return result;
    }

    @Test
    public void noop() {

    }

    protected void updateKaikkienPerusteenOsienTilat(PerusteTila tila) {
        perusteenOsaRepo.findAll().forEach((perusteenOsa -> {
            perusteenOsa.asetaTila(tila);
            perusteenOsaRepo.save(perusteenOsa);
        }));
    }

    protected void lisaaKoulutukset(Long id, Collection<String> koodiUris) {
        Peruste peruste = perusteRepository.getOne(id);

        Set<Koulutus> koulutukset = koodiUris.stream().map(uri -> {
            Koulutus koulutus = new Koulutus();
            koulutus.setKoulutuskoodiUri(uri);
            koulutus.setKoulutuskoodiArvo(uri);
            return koulutus;
        }).collect(Collectors.toSet());

        peruste.setKoulutukset(koulutukset);
        perusteRepository.save(peruste);
    }

}
