package fi.vm.sade.eperusteet.service.test.util;

import fi.vm.sade.eperusteet.domain.*;
import fi.vm.sade.eperusteet.domain.arviointi.ArviointiAsteikko;
import fi.vm.sade.eperusteet.domain.tutkinnonosa.TutkinnonOsaTyyppi;
import fi.vm.sade.eperusteet.domain.tutkinnonrakenne.MuodostumisSaanto;
import fi.vm.sade.eperusteet.dto.KoulutusDto;
import fi.vm.sade.eperusteet.dto.TiedoteDto;
import fi.vm.sade.eperusteet.dto.TilaUpdateStatus;
import fi.vm.sade.eperusteet.dto.peruste.KVLiiteDto;
import fi.vm.sade.eperusteet.dto.peruste.PerusteDto;
import fi.vm.sade.eperusteet.dto.perusteprojekti.PerusteprojektiDto;
import fi.vm.sade.eperusteet.dto.perusteprojekti.PerusteprojektiLuontiDto;
import fi.vm.sade.eperusteet.dto.tutkinnonosa.TutkinnonOsaDto;
import fi.vm.sade.eperusteet.dto.tutkinnonrakenne.KoodiDto;
import fi.vm.sade.eperusteet.dto.tutkinnonrakenne.TutkinnonOsaViiteDto;
import fi.vm.sade.eperusteet.dto.Reference;
import fi.vm.sade.eperusteet.dto.util.LokalisoituTekstiDto;
import fi.vm.sade.eperusteet.repository.ArviointiAsteikkoRepository;
import fi.vm.sade.eperusteet.repository.PerusteRepository;
import fi.vm.sade.eperusteet.service.ArviointiAsteikkoService;
import fi.vm.sade.eperusteet.service.PerusteService;
import fi.vm.sade.eperusteet.service.PerusteprojektiService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;
import java.util.function.Consumer;

import static fi.vm.sade.eperusteet.service.test.util.TestUtils.uniikkiLokalisoituString;
import static org.assertj.core.api.Assertions.assertThat;

@Service
@Transactional
public class PerusteprojektiTestUtils {
    @Autowired
    private PerusteprojektiService perusteprojektiService;

    @Autowired
    private PerusteService perusteService;

    @Autowired
    private PerusteRepository perusteRepository;

    @Autowired
    ArviointiAsteikkoService arviointiAsteikkoService;

    @Autowired
    ArviointiAsteikkoRepository arviointiAsteikkoRepository;

    public PerusteprojektiDto createPerusteprojekti() {
        return createPerusteprojekti((PerusteprojektiLuontiDto pp) -> {});
    }

    public PerusteprojektiDto createPerusteprojekti(Consumer<PerusteprojektiLuontiDto> withPerusteprojekti) {
        PerusteprojektiLuontiDto result = new PerusteprojektiLuontiDto();
        result.setReforminMukainen(false);
        result.setNimi(TestUtils.uniikkiString());
        result.setKoulutustyyppi(KoulutusTyyppi.ERIKOISAMMATTITUTKINTO.toString());
        result.setLaajuusYksikko(LaajuusYksikko.OSAAMISPISTE);
        result.setReforminMukainen(true);
        result.setTyyppi(PerusteTyyppi.NORMAALI);
        result.setRyhmaOid("000");
        result.setDiaarinumero(TestUtils.uniikkiString());
        withPerusteprojekti.accept(result);
        PerusteprojektiDto pp = perusteprojektiService.save(result);
        return pp;
    }

    public PerusteDto initPeruste(Long perusteId, Consumer<PerusteDto> perusteFn) {
        return editPeruste(perusteId, p -> {
            HashSet<Kieli> kielet = new HashSet<>();
            kielet.add(Kieli.FI);
            p.setVoimassaoloAlkaa((new GregorianCalendar(2017, 5, 4)).getTime());
            p.setNimi(TestUtils.lt("x"));
            p.getNimi().getTekstit().put(Kieli.FI, "ap_fi");
            p.getNimi().getTekstit().put(Kieli.SV, "ap_sv");
            p.setKielet(kielet);
            p.setDiaarinumero("OPH-" + Long.toString(TestUtils.uniikkiId()) + "-1234");
            perusteFn.accept(p);
        });
    }

    public PerusteDto initPeruste(Long perusteId) {
        return initPeruste(perusteId, p -> {});
    }

    public PerusteDto editPeruste(Long perusteId) {
        return editPeruste(perusteId, (p) -> {});
    }

    public PerusteDto editPeruste(Long perusteId, Consumer<PerusteDto> perusteFn) {
        PerusteDto p = perusteService.get(perusteId);
        perusteFn.accept(p);
        return perusteService.update(p.getId(), p);
    }

    public KoodiDto createKoodiDto(String arvo) {
        return createKoodiDto(arvo, "tutkinnonosat");
    }

    public KoodiDto createKoodiDto(String arvo, String koodisto) {
        KoodiDto result = new KoodiDto();
        result.setArvo(arvo);
        result.setKoodisto(koodisto);
        result.setUri(koodisto + "_" + arvo);
        return result;
    }

    public TutkinnonOsaViiteDto addTutkinnonOsa(Long perusteId) {
        return addTutkinnonOsa(perusteId, (t) -> {});
    }

    public TutkinnonOsaViiteDto addTutkinnonOsa(Long perusteId, Consumer<TutkinnonOsaViiteDto> tosaFn) {
        HashSet<Kieli> kielet = new HashSet<>();
        kielet.add(Kieli.FI);
        TutkinnonOsaDto tosa = new TutkinnonOsaDto();
        tosa.setKoodi(createKoodiDto("200530"));
        tosa.setNimi(TestUtils.lt("x"));
        tosa.getNimi().getTekstit().put(Kieli.FI, "ap_fi");
        tosa.getNimi().getTekstit().put(Kieli.SV, "ap_sv");
        TutkinnonOsaViiteDto result = new TutkinnonOsaViiteDto();
        result.setTutkinnonOsaDto(tosa);
        result.setTyyppi(TutkinnonOsaTyyppi.NORMAALI);
        result.setLaajuus(new BigDecimal(5L));
        tosaFn.accept(result);
        return perusteService.addTutkinnonOsa(perusteId, Suoritustapakoodi.REFORMI, result);
    }

    public TutkinnonOsaViiteDto editTutkinnonOsa(Long perusteId, Suoritustapakoodi st, Long tovId, Consumer<TutkinnonOsaViiteDto> tosaFn) {
        TutkinnonOsaViiteDto tosa = perusteService.getTutkinnonOsaViite(perusteId, st, tovId);
        tosaFn.accept(tosa);
        return perusteService.updateTutkinnonOsa(perusteId, Suoritustapakoodi.REFORMI, tosa);
    }

    public void asetaTila(Long projektiId, ProjektiTila tila) {
        TiedoteDto tiedote = null;
        if (tila.equals(ProjektiTila.JULKAISTU)) {
            tiedote = TestUtils.createTiedote();
        }
        TilaUpdateStatus status = perusteprojektiService.updateTila(projektiId, tila, tiedote);
        assertThat(status.isVaihtoOk()).isTrue();
    }

    public void julkaise(Long projektiId) {
        TilaUpdateStatus status = perusteprojektiService.updateTila(projektiId, ProjektiTila.VIIMEISTELY, null);
        assertThat(status.isVaihtoOk()).isTrue();
        status = perusteprojektiService.updateTila(projektiId, ProjektiTila.VALMIS, null);
        assertThat(status.isVaihtoOk()).isTrue();
        status = perusteprojektiService.updateTila(projektiId, ProjektiTila.JULKAISTU, TestUtils.createTiedote());
        assertThat(status.isVaihtoOk()).isTrue();
    }

    public void asetaMuodostumiset(Long perusteId) {
        Peruste peruste = perusteRepository.findOne(perusteId);
        peruste.getSuoritustavat().stream().forEach(st -> {
            st.getRakenne().setMuodostumisSaanto(new MuodostumisSaanto(new MuodostumisSaanto.Laajuus(0, 180, LaajuusYksikko.OSAAMISPISTE), null));
        });
    }

    public void luoValidiKVLiite(Long perusteId) {
        PerusteDto p = perusteService.get(perusteId);

        // KV-liitteen kielet
        Set<Kieli> kielet = new HashSet<>();
        kielet.add(Kieli.FI);
        kielet.add(Kieli.SV);
        kielet.add(Kieli.EN);

        // Luodaan perusteelle lokalisoitu nimi ja muodostumisen kuvaus
        p.setNimi(TestUtils.uniikkiLokalisoituTekstiDto(kielet));

        Peruste peruste = perusteRepository.findOne(perusteId);
        peruste.getSuoritustavat().forEach(suoritustapa -> suoritustapa.getRakenne()
                .setKuvaus(TekstiPalanen.of(uniikkiLokalisoituString())));
        perusteRepository.save(peruste);

        // Luodaan varsinainen KV-liite
        KVLiiteDto kvLiiteDto = new KVLiiteDto();

        kvLiiteDto.setSuorittaneenOsaaminen(TestUtils.uniikkiLokalisoituTekstiDto(kielet));
        kvLiiteDto.setTyotehtavatJoissaVoiToimia(TestUtils.uniikkiLokalisoituTekstiDto(kielet));

        ArviointiAsteikko arviointiAsteikko = new ArviointiAsteikko();
        arviointiAsteikko.setId(1L);
        arviointiAsteikko = arviointiAsteikkoRepository.save(arviointiAsteikko);
        kvLiiteDto.setArvosanaAsteikko(new Reference(arviointiAsteikko.getId()));

        kvLiiteDto.setJatkoopintoKelpoisuus(TestUtils.uniikkiLokalisoituTekstiDto(kielet));
        kvLiiteDto.setKansainvalisetSopimukset(TestUtils.uniikkiLokalisoituTekstiDto(kielet));
        kvLiiteDto.setSaadosPerusta(TestUtils.uniikkiLokalisoituTekstiDto(kielet));
        kvLiiteDto.setPohjakoulutusvaatimukset(TestUtils.uniikkiLokalisoituTekstiDto(kielet));
        kvLiiteDto.setLisatietoja(TestUtils.uniikkiLokalisoituTekstiDto(kielet));
        kvLiiteDto.setTutkintotodistuksenSaaminen(TestUtils.uniikkiLokalisoituTekstiDto(kielet));
        kvLiiteDto.setTutkinnostaPaattavaViranomainen(TestUtils.uniikkiLokalisoituTekstiDto(kielet));

        p.setKvliite(kvLiiteDto);

        // Lisätään Sirkusalan perustutkinto koulutuskoodi
        Set<KoulutusDto> koulutukset = new HashSet<>();
        KoulutusDto koulutus = new KoulutusDto();
        koulutus.setKoulutusalakoodi("koulutusalaoph2002_2");
        koulutus.setKoulutuskoodiArvo("321902");
        koulutus.setKoulutuskoodiUri("koulutus_321902");
        Map<String, String> tekstit = new HashMap<>();
        tekstit.put("fi", "Sirkusalan perustutkinto");
        tekstit.put("sv", "Grundexamen inom cirkusbranschen");
        tekstit.put("en", "Circus Artist, FQ");
        koulutus.setNimi(new LokalisoituTekstiDto(tekstit));
        koulutus.setOpintoalakoodi("opintoalaoph2002_204");
        koulutukset.add(koulutus);
        p.setKoulutukset(koulutukset);

        perusteService.updateFull(perusteId, p);
    }
}
