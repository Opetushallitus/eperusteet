package fi.vm.sade.eperusteet.service;

import fi.vm.sade.eperusteet.domain.*;
import fi.vm.sade.eperusteet.domain.arviointi.ArvioinninKohdealue;
import fi.vm.sade.eperusteet.domain.arviointi.Arviointi;
import fi.vm.sade.eperusteet.domain.tutkinnonosa.Ammattitaitovaatimukset2019;
import fi.vm.sade.eperusteet.domain.tutkinnonosa.TutkinnonOsa;
import fi.vm.sade.eperusteet.domain.tutkinnonosa.TutkinnonOsaTyyppi;
import fi.vm.sade.eperusteet.domain.tutkinnonrakenne.TutkinnonOsaViite;
import fi.vm.sade.eperusteet.dto.peruste.TutkinnonOsaQueryDto;
import fi.vm.sade.eperusteet.dto.tutkinnonosa.Ammattitaitovaatimukset2019Dto;
import fi.vm.sade.eperusteet.dto.tutkinnonosa.Ammattitaitovaatimus2019Dto;
import fi.vm.sade.eperusteet.dto.tutkinnonosa.TutkinnonOsaDto;
import fi.vm.sade.eperusteet.dto.tutkinnonrakenne.KoodiDto;
import fi.vm.sade.eperusteet.dto.tutkinnonrakenne.TutkinnonOsaViiteDto;
import fi.vm.sade.eperusteet.dto.tutkinnonrakenne.TutkinnonOsaViiteKontekstiDto;
import fi.vm.sade.eperusteet.dto.util.LokalisoituTekstiDto;
import fi.vm.sade.eperusteet.repository.ArvioinninKohdealueRepository;
import fi.vm.sade.eperusteet.repository.PerusteenOsaRepository;
import fi.vm.sade.eperusteet.repository.SuoritustapaRepository;
import fi.vm.sade.eperusteet.repository.TutkinnonOsaRepository;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.from;


@DirtiesContext
@Transactional
public class TutkinnonOsaJulkisetIT extends AbstractPerusteprojektiTest {

    @Autowired
    private TutkinnonOsaRepository tutkinnonOsaRepository;

    @Autowired
    private PerusteenOsaRepository perusteenOsaRepository;

    @Autowired
    private AmmattitaitovaatimusService ammattitaitovaatimusService;

    @Autowired
    private ArvioinninKohdealueRepository arvioinninKohdealueRepository;

    @Autowired
    private SuoritustapaRepository suoritustapaRepository;

    @Autowired
    private TutkintonimikeKoodiService tutkintonimikeKoodiService;

    @Autowired
    private PerusteenOsaService perusteenOsaService;

    @Before
    public void setup() {
        super.setup();
    }

    @Test
    @Rollback
    public void testVirheellinenHaku() {
        assertThatCode(() -> {
            Page<TutkinnonOsaDto> osat = perusteenOsaService.findTutkinnonOsatBy(TutkinnonOsaQueryDto.builder()
                    .build());
        }).hasMessage("koodiUri on pakollinen");
    }

    @Test
    @Rollback
    public void testTutkinnonOsienHaku() {
        TutkinnonOsa tosa = new TutkinnonOsa();
        tosa.setTyyppi(TutkinnonOsaTyyppi.NORMAALI);
        tosa.asetaTila(PerusteTila.VALMIS);
        tosa.setKoodi(new Koodi("tutkinnonosat_123456", "tutkinnonosat"));
        tosa = perusteenOsaRepository.save(tosa);

        perusteenOsaRepository.saveAndFlush(tosa);

        TutkinnonOsaViite tov = new TutkinnonOsaViite();
        tov.setTutkinnonOsa(tosa);
        tov.setSuoritustapa(suoritustapa);
        suoritustapa.getTutkinnonOsat().add(tov);
        suoritustapa = suoritustapaRepository.saveAndFlush(suoritustapa);

        projekti.setTila(ProjektiTila.JULKAISTU);
        peruste.asetaTila(PerusteTila.VALMIS);
        projekti = perusteprojektiRepository.save(projekti);
        peruste = perusteRepository.save(peruste);
        ammattitaitovaatimusService.addAmmattitaitovaatimuskoodit();
        em.flush();

        Page<TutkinnonOsaDto> osat = perusteenOsaService.findTutkinnonOsatBy(TutkinnonOsaQueryDto.builder()
                .sivukoko(10)
                .koodiUri("tutkinnonosat_123456")
                .build());

        assertThat(osat.getTotalElements()).isEqualTo(1);
        assertThat(osat.getContent().get(0).getKoodi().getUri())
                .isEqualTo("tutkinnonosat_123456");

    }

    @Test
    @Rollback
    public void testTutkinnonOsienToteutustenHaku() {
        TutkinnonOsa tosa = new TutkinnonOsa();
        tosa.setTyyppi(TutkinnonOsaTyyppi.NORMAALI);
        tosa.asetaTila(PerusteTila.VALMIS);
        tosa.setKoodi(new Koodi("tutkinnonosat_123456", "tutkinnonosat"));
        tosa = perusteenOsaRepository.save(tosa);

        perusteenOsaRepository.saveAndFlush(tosa);

        TutkinnonOsaViite tov = new TutkinnonOsaViite();
        tov.setTutkinnonOsa(tosa);
        tov.setSuoritustapa(suoritustapa);
        suoritustapa.getTutkinnonOsat().add(tov);
        suoritustapa = suoritustapaRepository.saveAndFlush(suoritustapa);
        tov = suoritustapa.getTutkinnonOsat().iterator().next();

        projekti.setTila(ProjektiTila.JULKAISTU);
        peruste.asetaTila(PerusteTila.VALMIS);
        projekti = perusteprojektiRepository.save(projekti);
        peruste = perusteRepository.save(peruste);
        ammattitaitovaatimusService.addAmmattitaitovaatimuskoodit();
        em.flush();

        List<TutkinnonOsaViiteKontekstiDto> viitteet = perusteenOsaService.findTutkinnonOsaViitteetByTutkinnonOsa(tosa.getId());
        assertThat(viitteet).hasSize(1);
        assertThat(viitteet.get(0))
                .extracting("id", "peruste.id", "suoritustapa.suoritustapakoodi")
                .contains(tov.getId(), peruste.getId(), suoritustapa.getSuoritustapakoodi());
    }

    @Test
    @Rollback
    public void testAmmattitaitovaatimukset2019() {
        TutkinnonOsa tosa = new TutkinnonOsa();
        tosa.setTyyppi(TutkinnonOsaTyyppi.NORMAALI);
        tosa.asetaTila(PerusteTila.VALMIS);
        tosa.setKoodi(new Koodi("tutkinnonosat_123456", "tutkinnonosat"));
        Ammattitaitovaatimukset2019Dto vaatimuksetDto = Ammattitaitovaatimukset2019Dto.builder()
                .kohde(LokalisoituTekstiDto.of("kohde"))
                .vaatimukset(Stream.of(
                        Ammattitaitovaatimus2019Dto.builder()
                                .koodi(KoodiDto.of("ammatitaitovaatimukset", "1234"))
                                .vaatimus(LokalisoituTekstiDto.of("1234"))
                            .build())
                        .collect(Collectors.toList()))
                .build();
        Ammattitaitovaatimukset2019 vaatimukset = mapper.map(vaatimuksetDto, Ammattitaitovaatimukset2019.class);
        tosa.setAmmattitaitovaatimukset2019(vaatimukset);
        tosa = perusteenOsaRepository.save(tosa);

//        perusteenOsaRepository.saveAndFlush(tosa);
//
//        TutkinnonOsaViite tov = new TutkinnonOsaViite();
//        tov.setTutkinnonOsa(tosa);
//        tov.setSuoritustapa(suoritustapa);
//        suoritustapa.getTutkinnonOsat().add(tov);
//        suoritustapa = suoritustapaRepository.saveAndFlush(suoritustapa);
//        tov = suoritustapa.getTutkinnonOsat().iterator().next();
//
//        projekti.setTila(ProjektiTila.JULKAISTU);
//        peruste.asetaTila(PerusteTila.VALMIS);
//        projekti = perusteprojektiRepository.save(projekti);
//        peruste = perusteRepository.save(peruste);
//        ammattitaitovaatimusService.addAmmattitaitovaatimuskoodit();
//        em.flush();
//
//        List<TutkinnonOsaViiteKontekstiDto> viitteet = perusteenOsaService.findTutkinnonOsaViitteetByTutkinnonOsa(tosa.getId());
//        assertThat(viitteet).hasSize(1);
//        assertThat(viitteet.get(0))
//                .extracting("id", "peruste.id", "suoritustapa.suoritustapakoodi")
//                .contains(tov.getId(), peruste.getId(), suoritustapa.getSuoritustapakoodi());
    }

}