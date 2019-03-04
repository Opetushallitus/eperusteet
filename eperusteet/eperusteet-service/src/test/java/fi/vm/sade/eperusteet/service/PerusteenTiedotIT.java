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

    @Before
    public void setup() {
        super.setup();
    }

    @Test
    @Rollback
    public void testPerusteprojektinMetatiedoissaTuleeOsaamisalat() {
        KoodiDto osaamisala = KoodiDto.of("osaamisalat", "1234");
        PerusteDto perusteDto = perusteService.get(this.peruste.getId());

        perusteDto.setOsaamisalat(new HashSet<>(Collections.singletonList(osaamisala)));
        PerusteDto updated = perusteService.update(this.peruste.getId(), perusteDto);
        this.uusiTekstiKappale(LokalisoituTekstiDto.of("oa nimi"), LokalisoituTekstiDto.of("oa teksti"), osaamisala);

        Map<Suoritustapakoodi, Map<String, List<TekstiKappaleDto>>> kuvaukset = perusteService.getOsaamisalaKuvaukset(this.peruste.getId());
        TekstiKappaleDto tk = kuvaukset.values().iterator().next().values().iterator().next().get(0);

        assertThat(tk.getNimi().get(Kieli.FI)).isEqualTo("oa nimi");
        assertThat(tk.getTeksti().get(Kieli.FI)).isEqualTo("oa teksti");
        assertThat(tk)
                .extracting("osaamisala.uri", "osaamisala.koodisto")
                .containsExactly(
                        osaamisala.getUri(),
                        osaamisala.getKoodisto());
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
        TilaUpdateStatus status = perusteprojektiService.validoiProjekti(projekti.getId(), ProjektiTila.LAADINTA);
        assertThat(status.isVaihtoOk()).isTrue();

        status = perusteprojektiService.validoiProjekti(projekti.getId(), ProjektiTila.JULKAISTU);
        assertThat(status)
                .returns(false, from(TilaUpdateStatus::isVaihtoOk));
        assertThat(status.getInfot())
                .extracting(TilaUpdateStatus.Status::getViesti)
                .contains(
                        "koulutuskoodi-puuttuu",
                        "kvliite-validointi-tyotehtavat-joissa-voi-toimia");
        status = perusteprojektiService.validoiProjekti(projekti.getId(), ProjektiTila.VIIMEISTELY);
        assertThat(status)
                .returns(false, from(TilaUpdateStatus::isVaihtoOk));
        status = perusteprojektiService.validoiProjekti(projekti.getId(), ProjektiTila.KOMMENTOINTI);
        assertThat(status)
                .returns(true, from(TilaUpdateStatus::isVaihtoOk));
        status = perusteprojektiService.validoiProjekti(projekti.getId(), ProjektiTila.POISTETTU);
        assertThat(status)
                .returns(true, from(TilaUpdateStatus::isVaihtoOk));
    }

    @Test
    public void testTutkintonimikkeet() {
        List<TutkintonimikeKoodiDto> tutkintonimikekoodit = tutkintonimikeKoodiService.getTutkintonimikekoodit(peruste.getId());
        assertThat(tutkintonimikekoodit).isEmpty();
    }

}
