package fi.vm.sade.eperusteet.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import fi.vm.sade.eperusteet.domain.*;
import fi.vm.sade.eperusteet.domain.tutkinnonosa.*;
import fi.vm.sade.eperusteet.domain.tutkinnonrakenne.TutkinnonOsaViite;
import fi.vm.sade.eperusteet.dto.peruste.TutkinnonOsaQueryDto;
import fi.vm.sade.eperusteet.dto.tutkinnonosa.*;
import fi.vm.sade.eperusteet.dto.tutkinnonrakenne.KoodiDto;
import fi.vm.sade.eperusteet.dto.tutkinnonrakenne.TutkinnonOsaViiteKontekstiDto;
import fi.vm.sade.eperusteet.dto.util.LokalisoituTekstiDto;
import fi.vm.sade.eperusteet.repository.ArvioinninKohdealueRepository;
import fi.vm.sade.eperusteet.repository.PerusteenOsaRepository;
import fi.vm.sade.eperusteet.repository.SuoritustapaRepository;
import fi.vm.sade.eperusteet.repository.TutkinnonOsaRepository;
import fi.vm.sade.eperusteet.resource.config.InitJacksonConverter;
import org.joda.time.DateTime;
import org.jsoup.Jsoup;
import org.jsoup.safety.Whitelist;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

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

    @Override
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
        lisaaTutkinnonosaPerusteeseen();

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
    public void testfindAllTutkinnonOsatBy() {
        lisaaTutkinnonosaPerusteeseen();

        {
            Page<TutkinnonOsaViiteKontekstiDto> osat = perusteenOsaService.findAllTutkinnonOsatBy(TutkinnonOsaQueryDto.builder()
                    .sivukoko(10)
                    .kieli("fi")
                    .build());
            assertThat(osat.getTotalElements()).isEqualTo(1);
        }

        {
            Page<TutkinnonOsaViiteKontekstiDto> osat = perusteenOsaService.findAllTutkinnonOsatBy(TutkinnonOsaQueryDto.builder()
                    .sivukoko(10)
                    .kieli("fi")
                    .nimi("")
                    .build());
            assertThat(osat.getTotalElements()).isEqualTo(1);
        }

        {
            Page<TutkinnonOsaViiteKontekstiDto> osat = perusteenOsaService.findAllTutkinnonOsatBy(TutkinnonOsaQueryDto.builder()
                    .sivukoko(10)
                    .kieli("fi")
                    .nimi("nimi")
                    .build());
            assertThat(osat.getTotalElements()).isEqualTo(1);
        }

        {
            Page<TutkinnonOsaViiteKontekstiDto> osat = perusteenOsaService.findAllTutkinnonOsatBy(TutkinnonOsaQueryDto.builder()
                    .sivukoko(10)
                    .nimi("nimi")
                    .kieli("sv")
                    .build());
            assertThat(osat.getTotalElements()).isEqualTo(0);
        }

        {
            Page<TutkinnonOsaViiteKontekstiDto> osat = perusteenOsaService.findAllTutkinnonOsatBy(TutkinnonOsaQueryDto.builder()
                    .sivukoko(10)
                    .kieli("fi")
                    .nimi("XXX")
                    .build());
            assertThat(osat.getTotalElements()).isEqualTo(0);
        }

        {
            Page<TutkinnonOsaViiteKontekstiDto> osat = perusteenOsaService.findAllTutkinnonOsatBy(TutkinnonOsaQueryDto.builder()
                    .sivukoko(10)
                    .kieli("fi")
                    .perusteId(peruste.getId())
                    .build());
            assertThat(osat.getTotalElements()).isEqualTo(1);
        }

        {
            peruste.setVoimassaoloLoppuu(DateTime.now().minusYears(1).toDate());
            peruste = perusteRepository.save(peruste);

            Page<TutkinnonOsaViiteKontekstiDto> osat = perusteenOsaService.findAllTutkinnonOsatBy(TutkinnonOsaQueryDto.builder()
                    .sivukoko(10)
                    .kieli("fi")
                    .perusteId(peruste.getId())
                    .build());
            assertThat(osat.getTotalElements()).isEqualTo(0);

            osat = perusteenOsaService.findAllTutkinnonOsatBy(TutkinnonOsaQueryDto.builder()
                    .sivukoko(10)
                    .kieli("fi")
                    .perusteId(peruste.getId())
                    .vanhentuneet(true)
                    .build());
            assertThat(osat.getTotalElements()).isEqualTo(1);
        }

    }

    private void lisaaTutkinnonosaPerusteeseen() {
        TutkinnonOsa tosa = new TutkinnonOsa();
        tosa.setTyyppi(TutkinnonOsaTyyppi.NORMAALI);
        tosa.asetaTila(PerusteTila.VALMIS);
        tosa.setNimi(TekstiPalanen.of(Kieli.FI, "nimi"));
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
        peruste.setPerusteprojekti(projekti);
        projekti = perusteprojektiRepository.save(projekti);
        peruste = perusteRepository.save(peruste);
        ammattitaitovaatimusService.addAmmattitaitovaatimuskoodit();
        em.flush();
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
    public void testAmmattiatitovaatimustenSiirtoVanhaanKenttaan() {
        TutkinnonOsa tutkinnonOsa = new TutkinnonOsa();
        Ammattitaitovaatimukset2019 av2019 = new Ammattitaitovaatimukset2019();
        av2019.setKohde(TekstiPalanen.of("kohdeSuomi", "kohdeRuotsi"));
        av2019.setVaatimukset(Collections.singletonList(Ammattitaitovaatimus2019.of(TekstiPalanen.of("kohdealueetonSuomi", "kohdealueetonRuotsi"))));

        { // Kohdealue
            Ammattitaitovaatimus2019Kohdealue kohdealue = new Ammattitaitovaatimus2019Kohdealue();
            kohdealue.setKuvaus(TekstiPalanen.of("kohdealueSuomi", "kohdealueRuotsi"));
            kohdealue.setVaatimukset(Collections.singletonList(Ammattitaitovaatimus2019.of(TekstiPalanen.of("kohdealueellinenSuomi", "kohdealueellinenRuotsi"))));
            av2019.getKohdealueet().add(kohdealue);
        }

        tutkinnonOsa.setAmmattitaitovaatimukset2019(av2019);
        TutkinnonOsaKaikkiDto tutkinnonOsaKaikkiDto = mapper.map(tutkinnonOsa, TutkinnonOsaKaikkiDto.class);
        LokalisoituTekstiDto teksti = tutkinnonOsaKaikkiDto.getAmmattitaitovaatimukset();
        String suomi = teksti.getTekstit().get(Kieli.FI);
        String ruotsi = teksti.getTekstit().get(Kieli.SV);
        Jsoup.isValid(suomi, Whitelist.relaxed());
        Jsoup.isValid(ruotsi, Whitelist.relaxed());
        assertThat(suomi).isEqualTo("<dl><dt><i>kohdeSuomi</i></dt><dd style=\"display: list-item;\">kohdealueetonSuomi</dd></dl><b>kohdealueSuomi</b><dl><dt><i>kohdeSuomi</i></dt><dd style=\"display: list-item;\">kohdealueellinenSuomi</dd></dl>");
        assertThat(ruotsi).isEqualTo("<dl><dt><i>kohdeRuotsi</i></dt><dd style=\"display: list-item;\">kohdealueetonRuotsi</dd></dl><b>kohdealueRuotsi</b><dl><dt><i>kohdeRuotsi</i></dt><dd style=\"display: list-item;\">kohdealueellinenRuotsi</dd></dl>");

    }

    @Test
    @Rollback
    public void testOsaAlueetExport() throws JsonProcessingException {
        ObjectMapper objectMapper = InitJacksonConverter.createMapper();
        {
            OsaAlue oa = new OsaAlue();
            oa.setTyyppi(OsaAlueTyyppi.OSAALUE2014);
            oa.setOsaamistavoitteet(new ArrayList<>());
            oa.getOsaamistavoitteet().add(new Osaamistavoite());
            oa.getOsaamistavoitteet().add(new Osaamistavoite());
            OsaAlueKokonaanDto oaDto = mapper.map(oa, OsaAlueKokonaanDto.class);
            OsaAlueKokonaanDto parsed = objectMapper.readValue(objectMapper.writeValueAsString(oa), OsaAlueKokonaanDto.class);
            assertThat(parsed.getPakollisetOsaamistavoitteet()).isNull();
            assertThat(parsed.getValinnaisetOsaamistavoitteet()).isNull();
            assertThat(parsed.getOsaamistavoitteet()).hasSize(2);
        }

        {
            OsaAlue oa = new OsaAlue();
            oa.setTyyppi(OsaAlueTyyppi.OSAALUE2020);
            oa.setPakollisetOsaamistavoitteet(new Osaamistavoite());
            oa.setValinnaisetOsaamistavoitteet(new Osaamistavoite());
            OsaAlueKokonaanDto parsed = objectMapper.readValue(objectMapper.writeValueAsString(oa), OsaAlueKokonaanDto.class);
            assertThat(parsed.getPakollisetOsaamistavoitteet()).isNotNull();
            assertThat(parsed.getValinnaisetOsaamistavoitteet()).isNotNull();
            assertThat(parsed.getOsaamistavoitteet()).isNull();
        }
    }

    @Test
    @Rollback
    public void testAmmattiatitovaatimustenSiirtoVanhaanKenttaanVanhaRakenne() {
        TutkinnonOsa tutkinnonOsa = new TutkinnonOsa();
        tutkinnonOsa.setAmmattitaitovaatimukset(TekstiPalanen.of(Kieli.FI, "xyz"));
        TutkinnonOsaKaikkiDto tutkinnonOsaKaikkiDto = mapper.map(tutkinnonOsa, TutkinnonOsaKaikkiDto.class);
        assertThat(tutkinnonOsaKaikkiDto.getAmmattitaitovaatimukset().getTekstit().get(Kieli.FI)).isEqualTo("xyz");
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
