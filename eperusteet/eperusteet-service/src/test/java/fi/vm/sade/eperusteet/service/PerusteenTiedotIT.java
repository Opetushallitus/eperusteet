package fi.vm.sade.eperusteet.service;

import fi.vm.sade.eperusteet.domain.*;
import fi.vm.sade.eperusteet.domain.arviointi.ArvioinninKohdealue;
import fi.vm.sade.eperusteet.domain.arviointi.Arviointi;
import fi.vm.sade.eperusteet.domain.tutkinnonosa.TutkinnonOsa;
import fi.vm.sade.eperusteet.domain.tutkinnonosa.TutkinnonOsaTyyppi;
import fi.vm.sade.eperusteet.domain.tutkinnonrakenne.TutkinnonOsaViite;
import fi.vm.sade.eperusteet.dto.TilaUpdateStatus;
import fi.vm.sade.eperusteet.dto.peruste.PerusteDto;
import fi.vm.sade.eperusteet.dto.peruste.TekstiKappaleDto;
import fi.vm.sade.eperusteet.dto.peruste.TutkintonimikeKoodiDto;
import fi.vm.sade.eperusteet.dto.tutkinnonrakenne.KoodiDto;
import fi.vm.sade.eperusteet.dto.util.LokalisoituTekstiDto;
import fi.vm.sade.eperusteet.repository.ArvioinninKohdealueRepository;
import fi.vm.sade.eperusteet.repository.PerusteenOsaRepository;
import fi.vm.sade.eperusteet.repository.SuoritustapaRepository;
import fi.vm.sade.eperusteet.repository.TutkinnonOsaRepository;
import fi.vm.sade.eperusteet.service.test.util.TestUtils;
import fi.vm.sade.eperusteet.service.util.Validointi;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.from;

@DirtiesContext
@Transactional
public class PerusteenTiedotIT extends AbstractPerusteprojektiTest {

    @Autowired
    private TutkinnonOsaRepository tutkinnonOsaRepository;

    @Autowired
    private PerusteenOsaRepository perusteenOsaRepository;

    @Autowired
    private PerusteprojektiService perusteprojektiService;

    @Autowired
    private PerusteService perusteService;

    @Autowired
    private AmmattitaitovaatimusService ammattitaitovaatimusService;

    @Autowired
    private ArvioinninKohdealueRepository arvioinninKohdealueRepository;

    @Autowired
    private SuoritustapaRepository suoritustapaRepository;

    @Autowired
    private TutkintonimikeKoodiService tutkintonimikeKoodiService;

    @Override
    @Before
    public void setup() {
        super.setup();
    }

    @Test
    @Rollback
    public void testPerusteprojektinMetatiedoissaTuleeOsaamisalat() {
        KoodiDto tutkinnonosat = KoodiDto.of("tutkinnonosat", "1234");
        KoodiDto osaamisala = KoodiDto.of("osaamisala", "1234");
        PerusteDto perusteDto = perusteService.get(this.peruste.getId());

        perusteDto.setOsaamisalat(new HashSet<>(Collections.singletonList(osaamisala)));
        PerusteDto updated = perusteService.update(this.peruste.getId(), perusteDto);
        this.uusiTekstiKappale(LokalisoituTekstiDto.of("oa nimi"), LokalisoituTekstiDto.of("oa teksti"), osaamisala, Arrays.asList(tutkinnonosat, osaamisala));

        Map<Suoritustapakoodi, Map<String, List<TekstiKappaleDto>>> kuvaukset = perusteService.getOsaamisalaKuvaukset(this.peruste.getId());
        TekstiKappaleDto tk = kuvaukset.values().iterator().next().values().iterator().next().get(0);

        assertThat(tk.getNimi().get(Kieli.FI)).isEqualTo("oa nimi");
        assertThat(tk.getTeksti().get(Kieli.FI)).isEqualTo("oa teksti");
        assertThat(tk)
                .extracting("osaamisala.uri", "osaamisala.koodisto")
                .containsExactly(
                        osaamisala.getUri(),
                        osaamisala.getKoodisto());
        assertThat(tk.getKoodit()).extracting("uri").containsExactlyInAnyOrder("tutkinnonosat_1234", "osaamisala_1234");
    }

    @Test
    @Rollback
    public void testTekstikappaleKoodeilla_paivitys() {
        this.uusiTekstiKappale(LokalisoituTekstiDto.of("oa nimi"), LokalisoituTekstiDto.of("oa teksti"), null);

        Peruste peruste = perusteRepository.findOne(this.peruste.getId());
        TekstiKappale tekstikappale = (TekstiKappale) peruste.getSuoritustavat().iterator().next().getSisalto().getLapset().get(1).getPerusteenOsa();
        TekstiKappaleDto tk = mapper.map(tekstikappale, TekstiKappaleDto.class);

        assertThat(tk.getKoodit()).isNull();

        KoodiDto tutkinnonosat = KoodiDto.of("tutkinnonosat", "1234");
        KoodiDto osaamisala = KoodiDto.of("osaamisala", "1234");
        tk.setKoodit(Arrays.asList(tutkinnonosat, osaamisala));

        tk = perusteenOsaService.update(tk);
        assertThat(tk.getKoodit()).extracting("uri").containsExactlyInAnyOrder("tutkinnonosat_1234", "osaamisala_1234");
    }

    private Koodi getFirstAmmattitaitovaatimuskoodi() {
        Arviointi arviointi = peruste.getSuoritustavat().iterator().next()
                .getTutkinnonOsat().iterator().next()
                .getTutkinnonOsa().getArviointi();
        if (arviointi != null) {
            return arviointi.getArvioinninKohdealueet().iterator().next()
                    .getKoodi();
        }
        else {
            return null;
        }
    }

    @Test
    @Rollback
    public void testPerusteDiaarinumeronPaivitys() {
        peruste.setDiaarinumero(new Diaarinumero("OPH-12345-1234"));
        projekti = perusteprojektiRepository.save(projekti);
        peruste = perusteRepository.save(peruste);
        em.flush();
        assertThat(peruste.getDiaarinumero().toString()).isEqualTo("OPH-12345-1234");

        PerusteDto perusteDto = mapper.map(this.peruste, PerusteDto.class);
        perusteDto.setDiaarinumero("OPH-12345-1233");
        perusteDto.setNimi(TestUtils.lt("nimi"));
        perusteDto.setVoimassaoloAlkaa(new Date());
        PerusteDto updated = perusteService.update(peruste.getId(), perusteDto);
        assertThat(updated.getDiaarinumero()).isEqualTo("OPH-12345-1233");

        projekti.setTila(ProjektiTila.JULKAISTU);
        peruste.asetaTila(PerusteTila.VALMIS);
        peruste = perusteRepository.save(peruste);
        em.flush();

        perusteDto.setDiaarinumero("OPH-12345-1234");
        updated = perusteService.update(peruste.getId(), perusteDto);
        assertThat(updated.getDiaarinumero()).isEqualTo("OPH-12345-1233");

    }


    @Test
    @Rollback
    public void testPerusteAmmattitaitovaatimuskoodit() {
        TutkinnonOsa tosa = new TutkinnonOsa();
        tosa.setTyyppi(TutkinnonOsaTyyppi.NORMAALI);
        tosa = perusteenOsaRepository.save(tosa);

        Arviointi arviointi = new Arviointi();
        arviointi.setArvioinninKohdealueet(new ArrayList<>());
        ArvioinninKohdealue kohdealue = new ArvioinninKohdealue();
        kohdealue.setOtsikko(TekstiPalanen.of(Kieli.FI, "ammattitaitovaatimus"));
        arviointi.getArvioinninKohdealueet().add(kohdealue);
        tosa.setArviointi(arviointi);
        perusteenOsaRepository.saveAndFlush(tosa);

        TutkinnonOsaViite tov = new TutkinnonOsaViite();
        tov.setTutkinnonOsa(tosa);
        tov.setSuoritustapa(suoritustapa);
        suoritustapa.getTutkinnonOsat().add(tov);
        suoritustapa = suoritustapaRepository.saveAndFlush(suoritustapa);

        ammattitaitovaatimusService.addAmmattitaitovaatimuskoodit();
        em.flush();
        assertThat(arvioinninKohdealueRepository.koodillisetCount()).isEqualTo(0);
        assertThat(getFirstAmmattitaitovaatimuskoodi()).isNull();

        projekti.setTila(ProjektiTila.JULKAISTU);
        peruste.asetaTila(PerusteTila.VALMIS);
        projekti = perusteprojektiRepository.save(projekti);
        peruste = perusteRepository.save(peruste);
        ammattitaitovaatimusService.addAmmattitaitovaatimuskoodit();
        em.flush();
        assertThat(arvioinninKohdealueRepository.koodillisetCount()).isEqualTo(1);
        assertThat(getFirstAmmattitaitovaatimuskoodi())
                .isNotNull()
                .returns("ammattitaitovaatimukset_1", Koodi::getUri)
                .returns("ammattitaitovaatimukset", Koodi::getKoodisto)
                .returns(null, Koodi::getVersio);
    }

    @Test
    @Rollback
    public void testPerusteprojektiValidators() {
        TilaUpdateStatus status = new TilaUpdateStatus(perusteprojektiService.validoiProjekti(projekti.getId(), ProjektiTila.LAADINTA));
        assertThat(status.isVaihtoOk()).isTrue();

        status = new TilaUpdateStatus(perusteprojektiService.validoiProjekti(projekti.getId(), ProjektiTila.JULKAISTU));
        assertThat(status)
                .returns(false, from(TilaUpdateStatus::isVaihtoOk));

        assertThat(status.getVirheet())
                .extracting(Validointi.Virhe::getKuvaus)
                .contains(
                        "koulutuskoodi-puuttuu");
        assertThat(status.getHuomautukset())
                .extracting(Validointi.Virhe::getKuvaus)
                .contains(
                        "kvliite-validointi-suorittaneen-osaaminen",
                        "kvliite-validointi-tyotehtavat-joissa-voi-toimia",
                        "kvliite-validointi-arvosana-asteikko",
                        "kvliite-validointi-jatkoopinto-kelpoisuus",
                        "kvliite-validointi-saados-perusta",
                        "kvliite-validointi-pohjakoulutusvaatimukset",
                        "kvliite-validointi-lisatietoja",
                        "kvliite-validointi-tutkintotodistuksen-saaminen",
                        "kvliite-validointi-tutkinnosta-paattava-viranomainen",
                        "kvliite-validointi-nimi",
                        "kvliite-validointi-tasot");


        status = new TilaUpdateStatus(perusteprojektiService.validoiProjekti(projekti.getId(), ProjektiTila.JULKAISTU));
        assertThat(status)
                .returns(false, from(TilaUpdateStatus::isVaihtoOk));
        status = new TilaUpdateStatus(perusteprojektiService.validoiProjekti(projekti.getId(), ProjektiTila.LAADINTA));
        assertThat(status)
                .returns(true, from(TilaUpdateStatus::isVaihtoOk));
        status = new TilaUpdateStatus(perusteprojektiService.validoiProjekti(projekti.getId(), ProjektiTila.POISTETTU));
        assertThat(status)
                .returns(true, from(TilaUpdateStatus::isVaihtoOk));
    }

    @Test
    public void testTutkintonimikkeet() {
        List<TutkintonimikeKoodiDto> tutkintonimikekoodit = tutkintonimikeKoodiService.getTutkintonimikekoodit(peruste.getId());
        assertThat(tutkintonimikekoodit).isEmpty();
    }

}
