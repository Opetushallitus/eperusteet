/*
 * Copyright (c) 2013 The Finnish Board of Education - Opetushallitus
 *
 * This program is free software: Licensed under the EUPL, Version 1.1 or - as
 * soon as they will be approved by the European Commission - subsequent versions
 * of the EUPL (the "Licence");
 *
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at: http://ec.europa.eu/idabc/eupl
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * European Union Public Licence for more details.
 */

package fi.vm.sade.eperusteet.service;

import fi.vm.sade.eperusteet.domain.*;
import fi.vm.sade.eperusteet.domain.tutkinnonosa.TutkinnonOsaTyyppi;
import fi.vm.sade.eperusteet.domain.tutkinnonrakenne.RakenneModuuli;
import fi.vm.sade.eperusteet.domain.tutkinnonrakenne.RakenneModuuliRooli;
import fi.vm.sade.eperusteet.dto.TiedoteDto;
import fi.vm.sade.eperusteet.dto.TilaUpdateStatus;
import fi.vm.sade.eperusteet.dto.peruste.PerusteDto;
import fi.vm.sade.eperusteet.dto.peruste.TutkintonimikeKoodiDto;
import fi.vm.sade.eperusteet.dto.perusteprojekti.PerusteprojektiDto;
import fi.vm.sade.eperusteet.dto.perusteprojekti.PerusteprojektiLuontiDto;
import fi.vm.sade.eperusteet.dto.tutkinnonosa.TutkinnonOsaDto;
import fi.vm.sade.eperusteet.dto.tutkinnonrakenne.*;
import fi.vm.sade.eperusteet.dto.util.LokalisoituTekstiDto;
import fi.vm.sade.eperusteet.repository.PerusteprojektiRepository;
import fi.vm.sade.eperusteet.service.exception.BusinessRuleViolationException;
import fi.vm.sade.eperusteet.service.mapping.Dto;
import fi.vm.sade.eperusteet.service.mapping.DtoMapper;
import fi.vm.sade.eperusteet.service.test.AbstractIntegrationTest;
import fi.vm.sade.eperusteet.service.test.util.PerusteprojektiTestUtils;
import fi.vm.sade.eperusteet.service.test.util.TestUtils;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.*;

@Transactional
@DirtiesContext
public class PerusteenRakenneIT extends AbstractIntegrationTest {

    @Autowired
    private PerusteprojektiService perusteprojektiService;

    @Autowired
    private PerusteprojektiTestUtils testUtils;

    @Autowired
    private PerusteService perusteService;

    @Autowired
    private PerusteprojektiRepository perusteprojektiRepository;

    @Autowired
    private EntityManager em;

    @Autowired
    @Dto
    private DtoMapper mapper;

    @Autowired
    @LockCtx(TutkinnonRakenneLockContext.class)
    private LockService<TutkinnonRakenneLockContext> lockService;

    private Perusteprojekti projekti;
    private Peruste peruste;
    private Suoritustapa suoritustapa;
    private RakenneModuuli rakenne;

    @Before
    public void setup() {
        PerusteprojektiDto projektiDto = testUtils.createPerusteprojekti();
        projekti = perusteprojektiRepository.findOne(projektiDto.getId());
        peruste = projekti.getPeruste();
        suoritustapa = peruste.getSuoritustapa(Suoritustapakoodi.REFORMI);
        rakenne = suoritustapa.getRakenne();
    }

    private RakenneModuuliDto getRakenneDto() {
        return perusteService.getTutkinnonRakenne(peruste.getId(), suoritustapa.getSuoritustapakoodi(), null);
    }

    private PerusteDto update(PerusteDto perusteDto) {
        return perusteService.update(perusteDto.getId(), perusteDto);
    }

    private RakenneModuuliDto update(RakenneModuuliDto rakenne) {
        final TutkinnonRakenneLockContext ctx = TutkinnonRakenneLockContext.of(peruste.getId(), Suoritustapakoodi.REFORMI);
        lockService.lock(ctx);
        RakenneModuuliDto updated = perusteService.updateTutkinnonRakenne(peruste.getId(), suoritustapa.getSuoritustapakoodi(), rakenne);
        return updated;
    }

    private TutkinnonOsaViiteDto uusiTutkinnonOsa() {
        TutkinnonOsaViiteDto result = perusteService.addTutkinnonOsa(peruste.getId(), suoritustapa.getSuoritustapakoodi(), TutkinnonOsaViiteDto.builder()
                .tyyppi(TutkinnonOsaTyyppi.NORMAALI)
                .tutkinnonOsaDto(TutkinnonOsaDto.builder()
                        .build())
                .build());
        return result;
    }

    @Test
    @Rollback
    public void testUusiRakenne() {
        assertThat(rakenne).isNotNull();
        assertThat(rakenne)
                .extracting(
                        "muodostumisSaanto.laajuus.minimi",
                        "muodostumisSaanto.laajuus.maksimi",
                        "muodostumisSaanto.koko.minimi",
                        "muodostumisSaanto.koko.maksimi")
                .contains(null, null, null, null);
    }

    @Test
    @Rollback
    public void testTestDeprekoitunutRakenteenKoko() {
        RakenneModuuliDto rakenneDto = getRakenneDto();
        rakenneDto.setMuodostumisSaanto(MuodostumisSaantoDto.builder()
                .koko(MuodostumisSaantoDto.Koko.builder()
                        .maksimi(180)
                        .minimi(180)
                        .build())
                .build());
        RakenneModuuliDto updated = update(rakenneDto);
        assertThat(updated.getMuodostumisSaanto().getKoko()).isNull();
    }

    @Test
    @Rollback
    public void testRakenteenTunnisteidenSailyminen() {
        assertThat(rakenne).isNotNull();
        RakenneModuuliDto rakenneDto = getRakenneDto();

        final UUID rootTunniste = rakenneDto.getTunniste();
        rakenneDto.getOsat()
                .addAll(Arrays.asList(
                        RakenneOsaDto.of(uusiTutkinnonOsa()),
                        RakenneModuuliDto.builder()
                                .osat(Arrays.asList(
                                        RakenneOsaDto.of(uusiTutkinnonOsa())))
                                .build()));

        rakenneDto = update(rakenneDto);

        assertThat(rakenneDto.getTunniste()).isEqualTo(rootTunniste);

        { // Tunnisteet säilyvät muokkauksilla
            final UUID a = rakenneDto.getOsat().get(0).getTunniste();
            final UUID b = rakenneDto.getOsat().get(1).getTunniste();
            final UUID c = ((RakenneModuuliDto)rakenneDto.getOsat().get(1)).getOsat().get(0).getTunniste();
            assertThat(Arrays.asList(a, b, c)).doesNotContainNull();

            ((RakenneModuuliDto)rakenneDto.getOsat().get(1)).getOsat().add(RakenneModuuliDto.builder().build());
            ((RakenneModuuliDto)rakenneDto.getOsat().get(1)).getOsat().add(0, RakenneModuuliDto.builder().build());
            rakenneDto.getOsat().add(0, RakenneOsaDto.of(uusiTutkinnonOsa()));
            rakenneDto.getOsat().add(RakenneOsaDto.of(uusiTutkinnonOsa()));
            rakenneDto = update(rakenneDto);

            assertThat(Stream.of(
                        rakenneDto,
                        rakenneDto.getOsat().get(1),
                        rakenneDto.getOsat().get(2),
                        ((RakenneModuuliDto)rakenneDto.getOsat().get(2)).getOsat().get(1))
                    .map(AbstractRakenneOsaDto::getTunniste))
                    .containsExactly(rootTunniste, a, b, c);
        }
    }

    @Test
    @Rollback
    public void testEiSallitaSamaaTunnistetta() {
        RakenneModuuliDto rakenneDto = getRakenneDto();
        rakenneDto.getOsat().add(RakenneOsaDto.of(uusiTutkinnonOsa()));
        rakenneDto.getOsat().add(RakenneOsaDto.of(uusiTutkinnonOsa()));
        final RakenneModuuliDto updated = update(rakenneDto);
        updated.getOsat().get(0).setTunniste(updated.getOsat().get(1).getTunniste());
        assertThatThrownBy(() -> update(updated));
    }

    @Test
    @Rollback
    public void testRegressionRakenneMuuttunutJarjestys() {
        RakenneModuuliDto rakenneDto = getRakenneDto();
        rakenneDto.setOsat(Arrays.asList(
                RakenneModuuliDto.builder().build(),
                RakenneModuuliDto.builder().build()));

        rakenneDto = update(rakenneDto);

        List<UUID> tunnisteet = rakenneDto.getOsat().stream().map(AbstractRakenneOsaDto::getTunniste)
                .collect(Collectors.toList());
        assertThat(tunnisteet).doesNotContainNull().hasSize(2);
        UUID at = tunnisteet.get(0);
        UUID bt = tunnisteet.get(1);

        Collections.swap(rakenneDto.getOsat(), 0, 1);

        rakenneDto = update(rakenneDto);
        assertThat(rakenneDto.getOsat().stream().map(AbstractRakenneOsaDto::getTunniste))
                .containsExactly(bt, at);
    }

    @Test
    @Rollback
    public void testTutkintonimikekoodeihinPerusteAutomaattisesti() {
        TutkintonimikeKoodiDto koodi = perusteService.addTutkintonimikeKoodi(peruste.getId(), TutkintonimikeKoodiDto.builder()
                .tutkintonimikeArvo("1001")
                .tutkintonimikeUri("tutkintonimike_1001").build());

        assertThat(koodi.getPeruste().getIdLong())
                .isEqualTo(peruste.getId());
    }

    @Test
    @Rollback
    public void testRegressionRakenteessaUsampiTutkintonimikeIlmanOsaamisaloja() {
        perusteService.addTutkintonimikeKoodi(peruste.getId(), TutkintonimikeKoodiDto.builder()
                    .tutkintonimikeArvo("1001")
                    .tutkintonimikeUri("tutkintonimike_1001").build());

        perusteService.addTutkintonimikeKoodi(peruste.getId(), TutkintonimikeKoodiDto.builder()
                .tutkintonimikeArvo("1002")
                .tutkintonimikeUri("tutkintonimike_1002").build());

        RakenneModuuliDto rakenneDto = getRakenneDto();
        rakenneDto.setOsat(Arrays.asList(
                RakenneModuuliDto.builder()
                        .rooli(RakenneModuuliRooli.TUTKINTONIMIKE)
                        .tutkintonimike(KoodiDto.of("tutkintonimike", "1001"))
                        .build(),
                RakenneModuuliDto.builder()
                        .rooli(RakenneModuuliRooli.TUTKINTONIMIKE)
                        .tutkintonimike(KoodiDto.of("tutkintonimike", "1002"))
                        .build()));
        RakenneModuuliDto rakenne = update(rakenneDto);
        TilaUpdateStatus status = perusteprojektiService.validoiProjekti(projekti.getId(), ProjektiTila.JULKAISTU);
        List<TilaUpdateStatus.Status> infot = status.getInfot();
        assertThat(status.getInfot().stream().map(TilaUpdateStatus.Status::getViesti))
            .doesNotContain("tutkintonimikkeen-osaamisala-puuttuu-perusteesta");
    }

    @Test
    @Rollback
    public void testValidoiTutkinnossaMaariteltavatRyhmat() {
        PerusteprojektiLuontiDto ppldto = new PerusteprojektiLuontiDto(KoulutusTyyppi.PERUSTUTKINTO.toString(),
                LaajuusYksikko.OSAAMISPISTE, null, null, PerusteTyyppi.NORMAALI, "1.2.246.562.28.11287634288");
        ppldto.setReforminMukainen(false);
        ppldto.setNimi(TestUtils.uniikkiString());
        ppldto.setDiaarinumero(TestUtils.uniikkiString());
        ppldto.setReforminMukainen(true);
        PerusteprojektiDto perusteprojektiDto = perusteprojektiService.save(ppldto);

        Perusteprojekti pp = perusteprojektiRepository.findOne(perusteprojektiDto.getId());
        Peruste peruste = pp.getPeruste();

        final TutkinnonRakenneLockContext ctx = TutkinnonRakenneLockContext.of(peruste.getId(), Suoritustapakoodi.REFORMI);
        lockService.lock(ctx);

        perusteService.addSisalto(pp.getPeruste().getId(), Suoritustapakoodi.REFORMI, null);


        peruste.getSuoritustavat().forEach(suoritustapa -> {
            assertThatThrownBy(() -> {
                RakenneModuuliDto rakenne = mapper.map(suoritustapa.getRakenne(), RakenneModuuliDto.class);
                RakenneModuuli virtuaaliryhma = TestUtils.rakenneModuuli()
                        .laajuus(5)
                        .rooli(RakenneModuuliRooli.VIRTUAALINEN)
                        .tayta()
                        .build();

                RakenneModuuliDto moduuli = mapper.map(virtuaaliryhma, RakenneModuuliDto.class);
                rakenne.getOsat().add(moduuli);

                perusteService.updateTutkinnonRakenne(peruste.getId(), suoritustapa.getSuoritustapakoodi(), rakenne);
            }).isInstanceOf(BusinessRuleViolationException.class)
            .hasMessage("ryhman-rooli-ei-salli-sisaltoa");
        });

        lockService.unlock(ctx);
    }

    @Test
    public void testLisaaRuotsinnosJulkaistuunPerusteeseen() {
        TutkinnonOsaViiteDto dto = testUtils.addTutkinnonOsa(peruste.getId());
        RakenneModuuliDto rakenne = perusteService.getTutkinnonRakenne(peruste.getId(),
                Suoritustapakoodi.REFORMI, 0);
        rakenne.getOsat().add(RakenneOsaDto.of(dto));

        final TutkinnonRakenneLockContext ctx = TutkinnonRakenneLockContext.of(peruste.getId(),
                Suoritustapakoodi.REFORMI);

        lockService.lock(ctx);
        rakenne = perusteService.updateTutkinnonRakenne(peruste.getId(), Suoritustapakoodi.REFORMI, rakenne);
        lockService.unlock(ctx);

        perusteprojektiService.updateTila(projekti.getId(), ProjektiTila.VIIMEISTELY, null);
        perusteprojektiService.updateTila(projekti.getId(), ProjektiTila.VALMIS, null);
        perusteprojektiService.updateTila(projekti.getId(), ProjektiTila.JULKAISTU, new TiedoteDto());

        // Muutetaan muodostumisen kuvaus
        rakenne.setKuvaus(LokalisoituTekstiDto.of("kuvaus"));

        lockService.lock(ctx);
        perusteService.updateTutkinnonRakenne(peruste.getId(), Suoritustapakoodi.REFORMI, rakenne);
        lockService.unlock(ctx);

    }

}
