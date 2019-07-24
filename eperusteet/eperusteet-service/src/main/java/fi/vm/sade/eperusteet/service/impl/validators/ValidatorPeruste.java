package fi.vm.sade.eperusteet.service.impl.validators;

import fi.vm.sade.eperusteet.domain.*;
import fi.vm.sade.eperusteet.domain.tutkinnonosa.OsaAlue;
import fi.vm.sade.eperusteet.domain.tutkinnonosa.TutkinnonOsa;
import fi.vm.sade.eperusteet.domain.tutkinnonosa.TutkinnonOsaTyyppi;
import fi.vm.sade.eperusteet.domain.tutkinnonrakenne.AbstractRakenneOsa;
import fi.vm.sade.eperusteet.domain.tutkinnonrakenne.RakenneModuuli;
import fi.vm.sade.eperusteet.domain.tutkinnonrakenne.TutkinnonOsaViite;
import fi.vm.sade.eperusteet.domain.yl.*;
import fi.vm.sade.eperusteet.domain.yl.lukio.LukioOpetussuunnitelmaRakenne;
import fi.vm.sade.eperusteet.domain.yl.lukio.LukiokoulutuksenPerusteenSisalto;
import fi.vm.sade.eperusteet.domain.yl.lukio.Lukiokurssi;
import fi.vm.sade.eperusteet.dto.TilaUpdateStatus;
import fi.vm.sade.eperusteet.dto.koodisto.KoodistoKoodiDto;
import fi.vm.sade.eperusteet.dto.peruste.KVLiiteJulkinenDto;
import fi.vm.sade.eperusteet.dto.peruste.TutkintonimikeKoodiDto;
import fi.vm.sade.eperusteet.dto.tutkinnonosa.OsaAlueDto;
import fi.vm.sade.eperusteet.dto.tutkinnonosa.TutkinnonOsaDto;
import fi.vm.sade.eperusteet.dto.tutkinnonrakenne.KoodiDto;
import fi.vm.sade.eperusteet.dto.util.LokalisoituTekstiDto;
import fi.vm.sade.eperusteet.repository.PerusteprojektiRepository;
import fi.vm.sade.eperusteet.service.KoodistoClient;
import fi.vm.sade.eperusteet.service.PerusteService;
import fi.vm.sade.eperusteet.service.TutkintonimikeKoodiService;
import fi.vm.sade.eperusteet.service.Validator;
import fi.vm.sade.eperusteet.service.exception.BusinessRuleViolationException;
import fi.vm.sade.eperusteet.service.mapping.Dto;
import fi.vm.sade.eperusteet.service.mapping.DtoMapper;
import fi.vm.sade.eperusteet.service.util.PerusteenRakenne;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.stream.Collectors;

import static fi.vm.sade.eperusteet.domain.ProjektiTila.*;
import static fi.vm.sade.eperusteet.domain.TekstiPalanen.tarkistaTekstipalanen;
import static fi.vm.sade.eperusteet.dto.util.LokalisoituTekstiDto.localized;
import static fi.vm.sade.eperusteet.service.util.Util.*;
import static java.util.stream.Collectors.toMap;

@Component
@Transactional
@Slf4j
public class ValidatorPeruste implements Validator {

    @Autowired
    @Dto
    private DtoMapper mapper;

    @Autowired
    private PerusteprojektiRepository repository;

    @Autowired
    private PerusteService perusteService;

    @Autowired
    private TutkintonimikeKoodiService tutkintonimikeKoodiService;

    @Autowired
    private KoodistoClient koodistoService;

    @Override
    public boolean applicableToteutus(KoulutustyyppiToteutus toteutus) {
        return true;
    }

    private void validoiLukio(Peruste peruste, ProjektiTila tila, TilaUpdateStatus updateStatus) {
        LukiokoulutuksenPerusteenSisalto sisalto = peruste.getLukiokoulutuksenPerusteenSisalto();
        LukioOpetussuunnitelmaRakenne rakenne = sisalto.getOpetussuunnitelma();
        updateStatus.forSuoritustapa(Suoritustapakoodi.LUKIOKOULUTUS).toTila(tila)
                .forTilat(jalkeen(LAADINTA))
                .addErrorGiven("peruste-lukio-ei-oppiaineita", rakenne.getOppiaineet().isEmpty())
                .addErrorGiven("peruste-lukio-ei-aihekokonaisuuksia", KoulutusTyyppi.of(peruste.getKoulutustyyppi()) != KoulutusTyyppi.LUKIOVALMISTAVAKOULUTUS
                        && (sisalto.getAihekokonaisuudet() == null || sisalto.getAihekokonaisuudet().getAihekokonaisuudet().isEmpty()))
                .addErrorGiven("peruste-lukio-ei-opetuksen-yleisia-tavoitteita",
                        sisalto.getOpetuksenYleisetTavoitteet() == null)
                .forTilat(jalkeen(KOMMENTOINTI))
                .addErrorStatusForAll("peruste-lukio-liittamaton-kurssi", () ->
                        rakenne.kurssit()
                                .filter(empty(Lukiokurssi::getOppiaineet))
                                .map(localized(Nimetty::getNimi)))
                /*
                .addErrorStatusForAll("peruste-lukio-oppiaineessa-ei-kursseja", () -> {
                    // EP-1143
                    // EP-1183
                    return rakenne.oppiaineetMaarineen()
                            .filter(not(Oppiaine::isKoosteinen)
                                    .and(not(Oppiaine::isAbstraktiBool))
                                    .and(empty(Oppiaine::getLukiokurssit)))
                            .map(localized(Nimetty::getNimi));
                })
                */
                .addErrorStatusForAll("peruste-lukio-oppiaineessa-ei-oppimaaria", () ->
                        rakenne.oppiaineet()
                                .filter(and(Oppiaine::isKoosteinen, empty(Oppiaine::getOppimaarat)))
                                .map(localized(Nimetty::getNimi)))
                .addErrorStatusForAll("peruste-lukio-kooodi-puuttuu", () ->
                        rakenne.koodilliset()
                                .filter(emptyString(Koodillinen::getKoodiArvo).or(emptyString(Koodillinen::getKoodiUri)))
                                .map(localized(Nimetty::getNimi)))
                .addErrorStatusForAll("peruste-lukio-sama-koodi", () -> {
                    List<LokalisoituTekstiDto> duplikaatit = new ArrayList<>();
                    rakenne.koodilliset()
                            .filter(emptyString(Koodillinen::getKoodiArvo).negate())
                            .collect(toMap(Koodillinen::getKoodiArvo, k -> k, (a, b) -> {
                                duplikaatit.add(localized(a.getNimi())
                                        .concat(" - ")
                                        .concat(localized(b.getNimi()))
                                        .concat(" (" + a.getKoodiArvo() + ")"));
                                return a;
                            }));
                    return duplikaatit.stream();
                });
    }

    public List<PerusteenOsaViite> flattenSisalto(PerusteenOsaViite root) {
        List<PerusteenOsaViite> result = new ArrayList<>();
        Stack<PerusteenOsaViite> stack = new Stack<>();
        stack.push(root);
        while (!stack.empty()) {
            PerusteenOsaViite head = stack.pop();
            result.add(head);
            if (head.getLapset() != null) {
                head.getLapset().forEach(stack::push);
            }
        }
        return result;
    }

    private List<TutkinnonOsaViite> vapaatTutkinnonosat(Suoritustapa suoritustapa) {
        List<TutkinnonOsaViite> viiteList = new ArrayList<>();
        RakenneModuuli rakenne = suoritustapa.getRakenne();
        if (rakenne != null) {
            for (TutkinnonOsaViite viite : getViitteet(suoritustapa)) {
                if (!rakenne.isInRakenne(viite, true)) {
                    viiteList.add(viite);
                }
            }
        }
        return viiteList;
    }

    private Collection<TutkinnonOsaViite> getViitteet(Suoritustapa suoritustapa) {
        return suoritustapa.getTutkinnonOsat();
    }

    private List<TutkinnonOsa> koodittomatTutkinnonosat(Suoritustapa suoritustapa) {
        List<TutkinnonOsa> koodittomatTutkinnonOsat = new ArrayList<>();

        if (suoritustapa.getTutkinnonOsat() != null) {
            for (TutkinnonOsaViite viite : getViitteet(suoritustapa)) {
                TutkinnonOsaDto osaDto = mapper.map(viite.getTutkinnonOsa(), TutkinnonOsaDto.class);
                if (osaDto.getKoodi() == null && StringUtils.isEmpty(osaDto.getKoodiUri())) {
                    koodittomatTutkinnonOsat.add(viite.getTutkinnonOsa());
                }
            }
        }
        return koodittomatTutkinnonOsat;
    }

    @SuppressWarnings("ServiceMethodEntity")
    @Transactional(readOnly = true)
    public void tarkistaRakenne(final AbstractRakenneOsa aosa, final Set<Kieli> pakolliset,
                                Map<String, String> virheellisetKielet) {
        tarkistaTekstipalanen("peruste-validointi-rakenneosa-kuvaus", aosa.getKuvaus(), pakolliset, virheellisetKielet);
        if (aosa instanceof RakenneModuuli) {
            RakenneModuuli osa = (RakenneModuuli) aosa;
            for (AbstractRakenneOsa lapsi : osa.getOsat()) {
                tarkistaRakenne(lapsi, pakolliset, virheellisetKielet);
            }
        }
    }

    @SuppressWarnings("ServiceMethodEntity")
    @Transactional(readOnly = true)
    public void tarkistaSisalto(final PerusteenOsaViite viite, final Set<Kieli> pakolliset,
                                Map<String, String> virheellisetKielet) {
        PerusteenOsa perusteenOsa = viite.getPerusteenOsa();
        if (perusteenOsa instanceof TekstiKappale && (perusteenOsa.getTunniste() == PerusteenOsaTunniste.NORMAALI || perusteenOsa.getTunniste() == null)) {
            TekstiKappale tekstikappale = (TekstiKappale) perusteenOsa;
            tarkistaTekstipalanen("peruste-validointi-tekstikappale-nimi", tekstikappale.getNimi(), pakolliset, virheellisetKielet, true);
            tarkistaTekstipalanen("peruste-validointi-tekstikappale-teksti", tekstikappale.getTeksti(), pakolliset, virheellisetKielet);
        }
        for (PerusteenOsaViite lapsi : viite.getLapset()) {
            tarkistaSisalto(lapsi, pakolliset, virheellisetKielet);
        }
    }


    @Transactional(readOnly = true)
    private void tarkistaPerusopetuksenOppiaine(
            Oppiaine oa,
            final Set<Kieli> vaaditutKielet,
            Map<String, String> virheellisetKielet
    ) {
        tarkistaTekstipalanen("peruste-validointi-oppiaine-nimi", oa.getNimi(), vaaditutKielet, virheellisetKielet);

        if (oa.getTehtava() != null) {
            tarkistaTekstipalanen("peruste-validointi-oppiaine-sisalto", oa.getTehtava().getOtsikko(),
                    vaaditutKielet, virheellisetKielet);
            tarkistaTekstipalanen("peruste-validointi-oppiaine-sisalto", oa.getTehtava().getTeksti(),
                    vaaditutKielet, virheellisetKielet);
        }

        Set<OpetuksenKohdealue> kohdealueet = oa.getKohdealueet();
        for (OpetuksenKohdealue ka : kohdealueet) {
            tarkistaTekstipalanen("peruste-validointi-oppiaine-kohdealue", ka.getNimi(), vaaditutKielet, virheellisetKielet);
        }

        Set<OppiaineenVuosiluokkaKokonaisuus> oavlks = oa.getVuosiluokkakokonaisuudet();
        for (OppiaineenVuosiluokkaKokonaisuus oavlk : oavlks) {
            TekstiOsa arviointi = oavlk.getArviointi();
            if (arviointi != null) {
                tarkistaTekstipalanen("peruste-validointi-oppiaine-vlk-sisalto", arviointi.getOtsikko(),
                        vaaditutKielet, virheellisetKielet);
                tarkistaTekstipalanen("peruste-validointi-oppiaine-vlk-sisalto", arviointi.getTeksti(),
                        vaaditutKielet, virheellisetKielet);
            }

            TekstiOsa ohjaus = oavlk.getOhjaus();
            if (ohjaus != null) {
                tarkistaTekstipalanen("peruste-validointi-oppiaine-vlk-sisalto", ohjaus.getOtsikko(),
                        vaaditutKielet, virheellisetKielet);
                tarkistaTekstipalanen("peruste-validointi-oppiaine-vlk-sisalto", ohjaus.getTeksti(),
                        vaaditutKielet, virheellisetKielet);
            }

            TekstiOsa tehtava = oavlk.getTehtava();
            if (tehtava != null) {
                tarkistaTekstipalanen("peruste-validointi-oppiaine-vlk-sisalto", tehtava.getOtsikko(),
                        vaaditutKielet, virheellisetKielet);
                tarkistaTekstipalanen("peruste-validointi-oppiaine-vlk-sisalto", tehtava.getTeksti(),
                        vaaditutKielet, virheellisetKielet);
            }

            TekstiOsa tyotavat = oavlk.getTyotavat();
            if (tyotavat != null) {
                tarkistaTekstipalanen("peruste-validointi-oppiaine-vlk-sisalto", tyotavat.getOtsikko(),
                        vaaditutKielet, virheellisetKielet);
                tarkistaTekstipalanen("peruste-validointi-oppiaine-vlk-sisalto", tyotavat.getTeksti(),
                        vaaditutKielet, virheellisetKielet);
            }

            List<KeskeinenSisaltoalue> sisaltoalueet = oavlk.getSisaltoalueet();
            for (KeskeinenSisaltoalue sa : sisaltoalueet) {
                tarkistaTekstipalanen("peruste-validointi-oppiaine-sisaltoalue", sa.getNimi(),
                        vaaditutKielet, virheellisetKielet);
                tarkistaTekstipalanen("peruste-validointi-oppiaine-sisaltoalue", sa.getKuvaus(),
                        vaaditutKielet, virheellisetKielet);
            }

            List<OpetuksenTavoite> tavoitteet = oavlk.getTavoitteet();
            for (OpetuksenTavoite tavoite : tavoitteet) {
                tarkistaTekstipalanen("peruste-validointi-oppiaine-vlk-tavoite-tavoite-teksti",
                        tavoite.getTavoite(), vaaditutKielet, virheellisetKielet);
            }
        }

        if (oa.getOppimaarat() != null) {
            for (Oppiaine oppimaara : oa.getOppimaarat()) {
                tarkistaPerusopetuksenOppiaine(oppimaara, vaaditutKielet, virheellisetKielet);
            }
        }
    }

    @Transactional(readOnly = true)
    private void tarkistaPerusopetuksenPeruste(Peruste peruste, TilaUpdateStatus status) {
        if (peruste == null) {
            return;
        }

        PerusopetuksenPerusteenSisalto sisalto = peruste.getPerusopetuksenPerusteenSisalto();
        if (sisalto == null) {
            return;
        }

        Set<LaajaalainenOsaaminen> osaamiset = sisalto.getLaajaalaisetosaamiset();
        Set<VuosiluokkaKokonaisuus> vlks = sisalto.getVuosiluokkakokonaisuudet();
        Set<Oppiaine> oppiaineet = sisalto.getOppiaineetCopy();
        Set<Kieli> vaaditutKielet = peruste.getKielet();
        Map<String, String> virheellisetKielet = new HashMap<>();

        // Laajaalaiset osaamiset
        for (LaajaalainenOsaaminen osaaminen : osaamiset) {
            tarkistaTekstipalanen("peruste-validointi-laajaalainen-osaaminen-nimi", osaaminen.getNimi(),
                    vaaditutKielet, virheellisetKielet);
            tarkistaTekstipalanen("peruste-validointi-laajaalainen-osaaminen-kuvaus", osaaminen.getKuvaus(),
                    vaaditutKielet, virheellisetKielet);
        }

        // Vuosiluokkakokonaisuudet
        for (VuosiluokkaKokonaisuus vlk : vlks) {
            tarkistaTekstipalanen("peruste-validointi-vlk-nimi", vlk.getNimi(), vaaditutKielet, virheellisetKielet);

            if (vlk.getTehtava() != null) {
                tarkistaTekstipalanen("peruste-validointi-vlk-tehtava-otsikko", vlk.getTehtava().getOtsikko(),
                        vaaditutKielet, virheellisetKielet);
                tarkistaTekstipalanen("peruste-validointi-vlk-tehtava-teksti", vlk.getTehtava().getTeksti(),
                        vaaditutKielet, virheellisetKielet);
            }

            Set<VuosiluokkaKokonaisuudenLaajaalainenOsaaminen> vlklos = vlk.getLaajaalaisetOsaamiset();
            for (VuosiluokkaKokonaisuudenLaajaalainenOsaaminen vlklo : vlklos) {
                tarkistaTekstipalanen("peruste-validointi-vlk-lo", vlklo.getKuvaus(),
                        vaaditutKielet, virheellisetKielet);
            }

            TekstiOsa paikallisestiPaatettavatAsiat = vlk.getPaikallisestiPaatettavatAsiat();
            if (paikallisestiPaatettavatAsiat != null) {
                tarkistaTekstipalanen("peruste-validointi-vlk-sisalto", paikallisestiPaatettavatAsiat.getOtsikko(),
                        vaaditutKielet, virheellisetKielet);
                tarkistaTekstipalanen("peruste-validointi-vlk-sisalto", paikallisestiPaatettavatAsiat.getTeksti(),
                        vaaditutKielet, virheellisetKielet);
            }

            TekstiOsa siirtymaEdellisesta = vlk.getSiirtymaEdellisesta();
            if (siirtymaEdellisesta != null) {
                tarkistaTekstipalanen("peruste-validointi-vlk-sisalto", siirtymaEdellisesta.getOtsikko(),
                        vaaditutKielet, virheellisetKielet);
                tarkistaTekstipalanen("peruste-validointi-vlk-sisalto", siirtymaEdellisesta.getTeksti(),
                        vaaditutKielet, virheellisetKielet);
            }

            TekstiOsa siirtymaSeuraavaan = vlk.getSiirtymaSeuraavaan();
            if (siirtymaSeuraavaan != null) {
                tarkistaTekstipalanen("peruste-validointi-vlk-sisalto", siirtymaSeuraavaan.getOtsikko(),
                        vaaditutKielet, virheellisetKielet);
                tarkistaTekstipalanen("peruste-validointi-vlk-sisalto", siirtymaSeuraavaan.getTeksti(),
                        vaaditutKielet, virheellisetKielet);
            }

            TekstiOsa laajaalainenOsaaminen = vlk.getLaajaalainenOsaaminen();
            if (laajaalainenOsaaminen != null) {
                tarkistaTekstipalanen("peruste-validointi-vlk-sisalto", laajaalainenOsaaminen.getOtsikko(),
                        vaaditutKielet, virheellisetKielet);
                tarkistaTekstipalanen("peruste-validointi-vlk-sisalto", laajaalainenOsaaminen.getTeksti(),
                        vaaditutKielet, virheellisetKielet);
            }
        }

        for (Oppiaine oa : oppiaineet) {
            tarkistaPerusopetuksenOppiaine(oa, vaaditutKielet, virheellisetKielet);
        }

        for (Map.Entry<String, String> entry : virheellisetKielet.entrySet()) {
            status.setVaihtoOk(false);
            status.addStatus(entry.getKey());
        }
    }

    @SuppressWarnings("ServiceMethodEntity")
    @Transactional(readOnly = true)
    public Map<String, String> tarkistaPerusteenTekstipalaset(Peruste peruste) {
        if (peruste.getTyyppi() == PerusteTyyppi.POHJA) {
            return new HashMap<>();
        }

        Set<Kieli> vaaditutKielet = peruste.getKielet();
        Map<String, String> virheellisetKielet = new HashMap<>();
        tarkistaTekstipalanen("peruste-validointi-kuvaus", peruste.getKuvaus(), vaaditutKielet, virheellisetKielet);
        tarkistaTekstipalanen("peruste-validointi-nimi", peruste.getNimi(), vaaditutKielet, virheellisetKielet);

        if (peruste.getKoulutukset() != null) {
            for (Koulutus koulutus : peruste.getKoulutukset()) {
                tarkistaTekstipalanen("peruste-validointi-koulutus-nimi", koulutus.getNimi(), vaaditutKielet, virheellisetKielet);
            }
        }

        // Esiopetus
        if (peruste.getEsiopetuksenPerusteenSisalto() != null) {
            for (PerusteenOsaViite lapsi : peruste.getEsiopetuksenPerusteenSisalto().getSisalto().getLapset()) {
                tarkistaSisalto(lapsi, vaaditutKielet, virheellisetKielet);
            }
        }

        // TPO
        if (peruste.getTpoOpetuksenSisalto() != null) {
            for (PerusteenOsaViite lapsi : peruste.getTpoOpetuksenSisalto().getSisalto().getLapset()) {
                tarkistaSisalto(lapsi, vaaditutKielet, virheellisetKielet);
            }
        }

        // Perusopetus
        if (peruste.getPerusopetuksenPerusteenSisalto() != null) {
            for (PerusteenOsaViite lapsi : peruste.getPerusopetuksenPerusteenSisalto().getSisalto().getLapset()) {
                tarkistaSisalto(lapsi, vaaditutKielet, virheellisetKielet);
            }
        }

        // Muut
        for (Suoritustapa st : peruste.getSuoritustavat()) {
            PerusteenOsaViite sisalto = st.getSisalto();
            if (sisalto != null) {
                for (PerusteenOsaViite lapsi : sisalto.getLapset()) {
                    tarkistaSisalto(lapsi, vaaditutKielet, virheellisetKielet);
                }
            }

            RakenneModuuli rakenne = st.getRakenne();
            if (rakenne != null) {
                tarkistaRakenne(st.getRakenne(), vaaditutKielet, virheellisetKielet);
            }

            for (TutkinnonOsaViite tov : st.getTutkinnonOsat()) {
                TutkinnonOsa tosa = tov.getTutkinnonOsa();
                tarkistaTekstipalanen("peruste-validointi-tutkinnonosa-ammattitaidon-osoittamistavat",
                        tosa.getAmmattitaidonOsoittamistavat(), vaaditutKielet, virheellisetKielet);
                tarkistaTekstipalanen("peruste-validointi-tutkinnonosa-ammattitaitovaatimukset",
                        tosa.getAmmattitaitovaatimukset(), vaaditutKielet, virheellisetKielet);
                tarkistaTekstipalanen("peruste-validointi-tutkinnonosa-kuvaus", tosa.getKuvaus(),
                        vaaditutKielet, virheellisetKielet);
                tarkistaTekstipalanen("peruste-validointi-tutkinnonosa-nimi", tosa.getNimi(),
                        vaaditutKielet, virheellisetKielet, true);
                tarkistaTekstipalanen("peruste-validointi-tutkinnonosa-tavoitteet", tosa.getTavoitteet(),
                        vaaditutKielet, virheellisetKielet);
            }
        }

        return virheellisetKielet;
    }

    @Override
    public TilaUpdateStatus validate(Long id, ProjektiTila tila) {
        TilaUpdateStatus updateStatus = new TilaUpdateStatus();
        updateStatus.setVaihtoOk(true);

        Perusteprojekti projekti = repository.findOne(id);

        if (projekti == null) {
            throw new BusinessRuleViolationException("Projektia ei ole olemassa id:llä: " + id);
        }

        if (projekti.getPeruste().getTyyppi() == PerusteTyyppi.OPAS) {
            return updateStatus;
        }

        // Tarkistetaan että perusteelle on asetettu nimi perusteeseen asetetuilla kielillä
        if (tila != ProjektiTila.POISTETTU && tila != LAADINTA && tila != KOMMENTOINTI) {
            TekstiPalanen nimi = projekti.getPeruste().getNimi();
            for (Kieli kieli : projekti.getPeruste().getKielet()) {
                if (nimi == null || !nimi.getTeksti().containsKey(kieli)
                        || nimi.getTeksti().get(kieli).isEmpty()) {
                    updateStatus.addStatus("perusteen-nimea-ei-ole-kaikilla-kielilla");
                    updateStatus.setVaihtoOk(false);
                    break;
                }
            }
        }

        Set<String> tutkinnonOsienKoodit = new HashSet<>();
        Peruste peruste = projekti.getPeruste();
        boolean isValmisPohja = PerusteTyyppi.POHJA == peruste.getTyyppi() && (VALMIS == projekti.getTila() || PerusteTila.VALMIS == peruste.getTila());

        // Perusteen validointi
        if (!isValmisPohja
                && peruste.getSuoritustavat() != null
                && tila != LAADINTA && tila != KOMMENTOINTI && tila != POISTETTU) {
            if (KoulutusTyyppi.of(peruste.getKoulutustyyppi()).isAmmatillinen()) {
                Set<String> osaamisalat = peruste.getOsaamisalat()
                        .stream()
                        .map(Koodi::getUri)
                        .collect(Collectors.toSet());
                List<TutkintonimikeKoodiDto> tutkintonimikkeet = tutkintonimikeKoodiService.getTutkintonimikekoodit(peruste.getId());

                { // Tutkintonimikkeiden osaamisalat täytyvät olla perusteessa
                    Set<String> tutkintonimikkeidenOsaamisalat = tutkintonimikkeet.stream()
                            .map(TutkintonimikeKoodiDto::getOsaamisalaUri)
                            .filter(Objects::nonNull)
                            .collect(Collectors.toSet());

                    for (String nimikkeenOsaamisala : tutkintonimikkeidenOsaamisalat) {
                        if (!osaamisalat.contains(nimikkeenOsaamisala)) {
                            List<LokalisoituTekstiDto> puuttuvatOsaamisalat = tutkintonimikkeidenOsaamisalat.stream()
                                    .map(uri -> {
                                        Koodi koodi = new Koodi();
                                        koodi.setUri(uri);
                                        koodi.setKoodisto("osaamisalat");
                                        return koodi;
                                    })
                                    .map(k -> mapper.map(k, KoodiDto.class))
                                    .map(KoodiDto::getNimi)
                                    .map(LokalisoituTekstiDto::new)
                                    .collect(Collectors.toList());
                            updateStatus.addStatus("tutkintonimikkeen-osaamisala-puuttuu-perusteesta", null, puuttuvatOsaamisalat);
                            updateStatus.setVaihtoOk(false);
                            break;
                        }
                    }
                }
            }

            if (!(peruste.getLukiokoulutuksenPerusteenSisalto() != null || peruste.getLops2019Sisalto() != null)) {
                PerusteenRakenne.Validointi validointi;

                // Osaamisaloilla täytyy olla tekstikuvaukset
                if (peruste.getOsaamisalat() != null && peruste.getOsaamisalat().size() > 0) {
                    PerusteenOsaViite sisalto = peruste.getSisallot(null);
                    if (sisalto != null) {
                        Set<Koodi> kuvaukselliset = flattenSisalto(sisalto).stream()
                                .filter(osa -> osa.getPerusteenOsa() instanceof TekstiKappale)
                                .map(osa -> (TekstiKappale) osa.getPerusteenOsa())
                                .map(TekstiKappale::getOsaamisala)
                                .filter(Objects::nonNull)
                                .collect(Collectors.toSet());

                        Set<Koodi> oalat = new HashSet<>(peruste.getOsaamisalat());

                        if (!Objects.equals(oalat, kuvaukselliset)) {
                            oalat.removeAll(kuvaukselliset);
                            List<LokalisoituTekstiDto> puuttuvat = oalat.stream()
                                    .map(koodi -> mapper.map(koodi, KoodiDto.class))
                                    .map(koodiDto -> new LokalisoituTekstiDto(koodiDto.getNimi()))
                                    .collect(Collectors.toList());
                            updateStatus.addStatus("osaamisalan-kuvauksia-puuttuu-sisallosta", null, puuttuvat);
                            updateStatus.setVaihtoOk(false);
                        }
                    }
                }

                // Rakenteiden validointi
                for (Suoritustapa suoritustapa : peruste.getSuoritustavat()) {
                    // Amosaa jaetun rakennetta ei tarkisteta
                    if (PerusteTyyppi.NORMAALI.equals(peruste.getTyyppi())) {
                        if (suoritustapa.getRakenne() != null && PerusteTyyppi.NORMAALI.equals(peruste.getTyyppi())) {
                            validointi = PerusteenRakenne.validoiRyhma(
                                    new PerusteenRakenne.Context(peruste.getOsaamisalat(), tutkintonimikeKoodiService.getTutkintonimikekoodit(peruste.getId())),
                                    suoritustapa.getRakenne(),
                                    KoulutusTyyppi.of(peruste.getKoulutustyyppi()).isValmaTelma());
                            if (!validointi.ongelmat.isEmpty()) {
                                updateStatus.addStatus("rakenteen-validointi-virhe",
                                        suoritustapa.getSuoritustapakoodi(),
                                        validointi);
                                updateStatus.setVaihtoOk(false);
                            }
                        }

                        // FIXME (Ilmeisesti pitää pystyä)
                        // Ammatitaitovaatimuksia ei voi julkaista enää tekstimuodossa
//                        if (suoritustapa.getSuoritustapakoodi().equals(Suoritustapakoodi.REFORMI)) {
//                            for (TutkinnonOsaViite tutkinnonOsaViite : suoritustapa.getTutkinnonOsat()) {
//                                LokalisoituTekstiDto nimi = mapper.map(tutkinnonOsaViite.getTutkinnonOsa().getNimi(), LokalisoituTekstiDto.class);
//                                TekstiPalanen avTekstina = tutkinnonOsaViite.getTutkinnonOsa().getAmmattitaitovaatimukset();
//                                List<AmmattitaitovaatimuksenKohdealue> avTaulukkona = tutkinnonOsaViite.getTutkinnonOsa().getAmmattitaitovaatimuksetLista();
//                                if (avTekstina != null && (avTaulukkona == null || avTaulukkona.isEmpty())) {
//                                    updateStatus.addErrorStatus("tutkinnon-osan-ammattitaitovaatukset-tekstina", suoritustapa.getSuoritustapakoodi(), nimi);
//                                }
//                            }
//                        }

                        // Vapaiden tutkinnon osien tarkistus
                        List<TutkinnonOsaViite> vapaatOsat = vapaatTutkinnonosat(suoritustapa);
                        if (!vapaatOsat.isEmpty()) {
                            List<LokalisoituTekstiDto> nimet = new ArrayList<>();
                            for (TutkinnonOsaViite viite : vapaatOsat) {
                                if (viite.getTutkinnonOsa().getNimi() != null) {
                                    nimet.add(new LokalisoituTekstiDto(viite.getTutkinnonOsa().getNimi().getId(),
                                            viite.getTutkinnonOsa().getNimi().getTeksti()));
                                }
                            }
                            updateStatus.addStatus("liittamattomia-tutkinnon-osia", suoritustapa.getSuoritustapakoodi(), nimet);
                            updateStatus.setVaihtoOk(false);
                        }
                    }

                    // Tarkistetaan koodittomat tutkinnon osat
                    List<TutkinnonOsa> koodittomatTutkinnonOsat = koodittomatTutkinnonosat(suoritustapa);
                    if (!koodittomatTutkinnonOsat.isEmpty()) {
                        List<LokalisoituTekstiDto> nimet = new ArrayList<>();
                        for (TutkinnonOsa tutkinnonOsa : koodittomatTutkinnonOsat) {
                            if (tutkinnonOsa.getNimi() != null) {
                                nimet.add(new LokalisoituTekstiDto(tutkinnonOsa.getNimi().getId(),
                                        tutkinnonOsa.getNimi().getTeksti()));
                            }
                        }
                        updateStatus.addStatus("koodittomia-tutkinnon-osia", suoritustapa.getSuoritustapakoodi(), nimet);
                        updateStatus.setVaihtoOk(false);
                    }

                    // Tarkista tutke2-osien osa-alueiden koodit
                    List<LokalisoituTekstiDto> koodittomatOsaalueet = new ArrayList<>();
                    for (TutkinnonOsaViite tov : suoritustapa.getTutkinnonOsat()) {
                        TutkinnonOsa tosa = tov.getTutkinnonOsa();
                        if (TutkinnonOsaTyyppi.isTutke(tosa.getTyyppi())) {
                            for (OsaAlue oa : tosa.getOsaAlueet()) {
                                OsaAlueDto alueDto = mapper.map(oa, OsaAlueDto.class);
                                if (alueDto.getKoodiArvo() == null || alueDto.getKoodiArvo().isEmpty() ||
                                        alueDto.getKoodiUri() == null || alueDto.getKoodiUri().isEmpty()) {
                                    koodittomatOsaalueet.add(new LokalisoituTekstiDto(tosa.getId(),
                                            tosa.getNimi().getTeksti()));
                                    break;
                                }
                            }
                        }
                    }

                    // Tarkistetaan osa-alueiden kooditukset
                    if (!koodittomatOsaalueet.isEmpty()) {
                        updateStatus.addStatus("tutke2-osalta-puuttuu-osa-alue-koodi",
                                suoritustapa.getSuoritustapakoodi(), koodittomatOsaalueet);
                    }

                    // Kerätään tutkinnon osien koodit
                    List<LokalisoituTekstiDto> virheellisetKoodistonimet = new ArrayList<>();
                    List<LokalisoituTekstiDto> uniikitKooditTosat = new ArrayList<>();
                    Set<String> uniikitKoodit = new HashSet<>();
                    for (TutkinnonOsaViite tov : getViitteet(suoritustapa)) {
                        TutkinnonOsa tosa = tov.getTutkinnonOsa();
                        TutkinnonOsaDto osaDto = mapper.map(tosa, TutkinnonOsaDto.class);

                        String uri = osaDto.getKoodiUri();
                        String arvo = osaDto.getKoodiArvo();

                        // Tarkistetaan onko sama koodi useammassa tutkinnon osassa
                        if (uniikitKoodit.contains(uri)) {
                            uniikitKooditTosat.add(new LokalisoituTekstiDto(tosa.getNimi().getId(),
                                    tosa.getNimi().getTeksti()));
                        } else {
                            uniikitKoodit.add(uri);
                        }

                        if (tosa.getNimi() != null
                                && (uri != null && !uri.isEmpty()
                                && arvo != null && !arvo.isEmpty())) {
                            KoodistoKoodiDto koodi = null;
                            try {
                                koodi = koodistoService.get("tutkinnonosat", uri);
                            } catch (Exception e) {
                                log.error(e.getMessage(), e);
                            }

                            if (koodi != null && koodi.getKoodiUri().equals(uri)) {
                                tutkinnonOsienKoodit.add(osaDto.getKoodiArvo());
                            } else {
                                virheellisetKoodistonimet.add(new LokalisoituTekstiDto(tosa.getNimi().getId(),
                                        tosa.getNimi().getTeksti()));
                            }
                        }
                    }

                    if (!virheellisetKoodistonimet.isEmpty()) {
                        updateStatus.addStatus("tutkinnon-osan-asetettua-koodia-ei-koodistossa",
                                suoritustapa.getSuoritustapakoodi(), virheellisetKoodistonimet);
                        updateStatus.setVaihtoOk(false);
                    }

                    if (!uniikitKooditTosat.isEmpty()) {
                        updateStatus.addStatus("tutkinnon-osien-koodit-kaytossa-muissa-tutkinnon-osissa",
                                suoritustapa.getSuoritustapakoodi(), uniikitKooditTosat);
                        updateStatus.setVaihtoOk(false);
                    }
                }

                // Tarkistetaan perusteen tutkinnon osien koodien ja tutkintonimikkeiden yhteys
                List<TutkintonimikeKoodiDto> tutkintonimikeKoodit = tutkintonimikeKoodiService.getTutkintonimikekoodit(projekti.getPeruste().getId());
                List<String> koodit = new ArrayList<>();
                for (TutkintonimikeKoodiDto tnk : tutkintonimikeKoodit) {
                    if (tnk.getTutkinnonOsaArvo() != null) {
                        koodit.add(tnk.getTutkinnonOsaArvo());
                    }
                }
                if (!tutkinnonOsienKoodit.containsAll(koodit)) {
                    updateStatus.addStatus("tutkintonimikkeen-vaatimaa-tutkinnonosakoodia-ei-loytynyt-tutkinnon-osilta");
                    updateStatus.setVaihtoOk(false);
                }
            }

            if (tila == ProjektiTila.JULKAISTU || tila == ProjektiTila.VALMIS) {
                tarkistaPerusopetuksenPeruste(peruste, updateStatus);
                // Tarkista että kaikki vaadittu kielisisältö on asetettu
                Map<String, String> lokalisointivirheet = tarkistaPerusteenTekstipalaset(projekti.getPeruste());
                for (Map.Entry<String, String> entry : lokalisointivirheet.entrySet()) {
                    updateStatus.setVaihtoOk(false);
                    updateStatus.addStatus(entry.getKey());
                }

                // Tarkista KV-liite
                if (KoulutusTyyppi.of(peruste.getKoulutustyyppi()).isAmmatillinen()) {
                    KVLiiteJulkinenDto julkinenKVLiite = perusteService.getJulkinenKVLiite(peruste.getId());
                    Set<Kieli> vaaditutKielet = new HashSet<Kieli>() {{
                        add(Kieli.FI);
                        add(Kieli.SV);
                        add(Kieli.EN);
                    }};
                }
            }

            if (tila == ProjektiTila.JULKAISTU) {
                if (!projekti.getPeruste().getTyyppi().equals(PerusteTyyppi.OPAS)) {
                    Diaarinumero diaarinumero = projekti.getPeruste().getDiaarinumero();
                    if (diaarinumero == null) {
                        updateStatus.addStatus("peruste-ei-diaarinumeroa");
                        updateStatus.setVaihtoOk(false);
                    }

                    if (projekti.getPeruste().getVoimassaoloAlkaa() == null) {
                        updateStatus.addStatus("peruste-ei-voimassaolon-alkamisaikaa");
                        updateStatus.setVaihtoOk(false);
                    }
                }
            }

            if (peruste.getLukiokoulutuksenPerusteenSisalto() != null) {
                validoiLukio(peruste, tila, updateStatus);
            }
        }

        return updateStatus;
    }

    @Override
    public boolean applicableKoulutustyyppi(KoulutusTyyppi tyyppi) {
        return true;
    }

    @Override
    public boolean applicablePerustetyyppi(PerusteTyyppi tyyppi) {
        return true;
    }

    @Override
    public boolean applicableTila(ProjektiTila tila) {
        return true;
    }
}
