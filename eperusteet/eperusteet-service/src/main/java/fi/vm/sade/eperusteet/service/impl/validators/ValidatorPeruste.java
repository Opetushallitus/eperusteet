package fi.vm.sade.eperusteet.service.impl.validators;

import fi.vm.sade.eperusteet.domain.Diaarinumero;
import fi.vm.sade.eperusteet.domain.Kieli;
import fi.vm.sade.eperusteet.domain.Koodi;
import fi.vm.sade.eperusteet.domain.Koulutus;
import fi.vm.sade.eperusteet.domain.KoulutusTyyppi;
import fi.vm.sade.eperusteet.domain.KoulutustyyppiToteutus;
import fi.vm.sade.eperusteet.domain.Peruste;
import fi.vm.sade.eperusteet.domain.PerusteTila;
import fi.vm.sade.eperusteet.domain.PerusteTyyppi;
import fi.vm.sade.eperusteet.domain.PerusteenOsa;
import fi.vm.sade.eperusteet.domain.PerusteenOsaTunniste;
import fi.vm.sade.eperusteet.domain.PerusteenOsaViite;
import fi.vm.sade.eperusteet.domain.Perusteprojekti;
import fi.vm.sade.eperusteet.domain.ProjektiTila;
import fi.vm.sade.eperusteet.domain.Suoritustapa;
import fi.vm.sade.eperusteet.domain.Suoritustapakoodi;
import fi.vm.sade.eperusteet.domain.TekstiKappale;
import fi.vm.sade.eperusteet.domain.TekstiPalanen;
import fi.vm.sade.eperusteet.domain.maarays.Maarays;
import fi.vm.sade.eperusteet.domain.maarays.MaaraysLiiteTyyppi;
import fi.vm.sade.eperusteet.domain.tutkinnonosa.OsaAlue;
import fi.vm.sade.eperusteet.domain.tutkinnonosa.TutkinnonOsa;
import fi.vm.sade.eperusteet.domain.tutkinnonosa.TutkinnonOsaTyyppi;
import fi.vm.sade.eperusteet.domain.tutkinnonrakenne.AbstractRakenneOsa;
import fi.vm.sade.eperusteet.domain.tutkinnonrakenne.RakenneModuuli;
import fi.vm.sade.eperusteet.domain.tutkinnonrakenne.TutkinnonOsaViite;
import fi.vm.sade.eperusteet.domain.yl.KeskeinenSisaltoalue;
import fi.vm.sade.eperusteet.domain.yl.Koodillinen;
import fi.vm.sade.eperusteet.domain.yl.LaajaalainenOsaaminen;
import fi.vm.sade.eperusteet.domain.yl.Nimetty;
import fi.vm.sade.eperusteet.domain.yl.OpetuksenKohdealue;
import fi.vm.sade.eperusteet.domain.yl.OpetuksenTavoite;
import fi.vm.sade.eperusteet.domain.yl.Oppiaine;
import fi.vm.sade.eperusteet.domain.yl.OppiaineenVuosiluokkaKokonaisuus;
import fi.vm.sade.eperusteet.domain.yl.PerusopetuksenPerusteenSisalto;
import fi.vm.sade.eperusteet.domain.yl.TekstiOsa;
import fi.vm.sade.eperusteet.domain.yl.VuosiluokkaKokonaisuudenLaajaalainenOsaaminen;
import fi.vm.sade.eperusteet.domain.yl.VuosiluokkaKokonaisuus;
import fi.vm.sade.eperusteet.domain.yl.lukio.LukioOpetussuunnitelmaRakenne;
import fi.vm.sade.eperusteet.domain.yl.lukio.LukiokoulutuksenPerusteenSisalto;
import fi.vm.sade.eperusteet.domain.yl.lukio.Lukiokurssi;
import fi.vm.sade.eperusteet.dto.TilaUpdateStatus;
import fi.vm.sade.eperusteet.dto.ValidointiKategoria;
import fi.vm.sade.eperusteet.dto.ValidointiStatusType;
import fi.vm.sade.eperusteet.dto.koodisto.KoodistoKoodiDto;
import fi.vm.sade.eperusteet.dto.maarays.MaaraysDto;
import fi.vm.sade.eperusteet.dto.peruste.KVLiiteJulkinenDto;
import fi.vm.sade.eperusteet.dto.peruste.NavigationNodeDto;
import fi.vm.sade.eperusteet.dto.peruste.NavigationType;
import fi.vm.sade.eperusteet.dto.peruste.PerusteenOsaDto;
import fi.vm.sade.eperusteet.dto.peruste.TutkintonimikeKoodiDto;
import fi.vm.sade.eperusteet.dto.tutkinnonosa.OsaAlueDto;
import fi.vm.sade.eperusteet.dto.tutkinnonosa.TutkinnonOsaDto;
import fi.vm.sade.eperusteet.dto.tutkinnonrakenne.KoodiDto;
import fi.vm.sade.eperusteet.dto.util.LokalisoituTekstiDto;
import fi.vm.sade.eperusteet.dto.util.NavigableLokalisoituTekstiDto;
import fi.vm.sade.eperusteet.dto.yl.OppiaineSuppeaDto;
import fi.vm.sade.eperusteet.repository.PerusteprojektiRepository;
import fi.vm.sade.eperusteet.service.KoodistoClient;
import fi.vm.sade.eperusteet.service.MaaraysService;
import fi.vm.sade.eperusteet.service.PerusteService;
import fi.vm.sade.eperusteet.service.TutkintonimikeKoodiService;
import fi.vm.sade.eperusteet.service.Validator;
import fi.vm.sade.eperusteet.service.exception.BusinessRuleViolationException;
import fi.vm.sade.eperusteet.service.mapping.Dto;
import fi.vm.sade.eperusteet.service.mapping.DtoMapper;
import fi.vm.sade.eperusteet.service.util.PerusteenRakenne;
import fi.vm.sade.eperusteet.service.util.ValidatorUtil;
import fi.vm.sade.eperusteet.service.util.Validointi;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.Stack;
import java.util.stream.Collectors;

import static fi.vm.sade.eperusteet.domain.ProjektiTila.KOMMENTOINTI;
import static fi.vm.sade.eperusteet.domain.ProjektiTila.LAADINTA;
import static fi.vm.sade.eperusteet.domain.ProjektiTila.POISTETTU;
import static fi.vm.sade.eperusteet.domain.ProjektiTila.VALMIS;
import static fi.vm.sade.eperusteet.domain.ProjektiTila.jalkeen;
import static fi.vm.sade.eperusteet.domain.TekstiPalanen.tarkistaTekstipalanen;
import static fi.vm.sade.eperusteet.dto.util.LokalisoituTekstiDto.localized;
import static fi.vm.sade.eperusteet.service.util.Util.and;
import static fi.vm.sade.eperusteet.service.util.Util.empty;
import static fi.vm.sade.eperusteet.service.util.Util.emptyString;
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

    @Autowired
    private MaaraysService maaraysService;

    @Override
    public boolean applicableToteutus(KoulutustyyppiToteutus toteutus) {
        return true;
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

    private List<TutkinnonOsaViite> koodittomatTutkinnonosat(Suoritustapa suoritustapa) {
        List<TutkinnonOsaViite> koodittomatTutkinnonOsat = new ArrayList<>();

        if (suoritustapa.getTutkinnonOsat() != null) {
            for (TutkinnonOsaViite viite : getViitteet(suoritustapa)) {
                TutkinnonOsaDto osaDto = mapper.map(viite.getTutkinnonOsa(), TutkinnonOsaDto.class);
                if (osaDto.getKoodi() == null && StringUtils.isEmpty(osaDto.getKoodiUri())) {
                    koodittomatTutkinnonOsat.add(viite);
                }
            }
        }
        return koodittomatTutkinnonOsat;
    }

    private List<TutkinnonOsaViite> arvioinnitIlmanOtsikoita(Suoritustapa suoritustapa) {
        return suoritustapa.getTutkinnonOsat().stream()
                .filter(viite -> viite.getTutkinnonOsa().getArviointi() != null
                        && viite.getTutkinnonOsa().getArviointi().getArvioinninKohdealueet() != null
                        && viite.getTutkinnonOsa().getArviointi().getArvioinninKohdealueet().stream().anyMatch(arvkd -> Objects.isNull(arvkd.getKoodi()) && arvkd.getOtsikko() == null)
                        )
                .collect(Collectors.toList());
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
    public void tarkistaSisalto(final PerusteenOsaViite viite, final Set<Kieli> pakolliset, Validointi validointi) {
        PerusteenOsa perusteenOsa = viite.getPerusteenOsa();
        Map<String, String> virheellisetKielet = new HashMap<>();
        if (perusteenOsa instanceof TekstiKappale && (perusteenOsa.getTunniste() == PerusteenOsaTunniste.NORMAALI || perusteenOsa.getTunniste() == null)) {
            TekstiKappale tekstikappale = (TekstiKappale) perusteenOsa;
            tarkistaTekstipalanen("peruste-validointi-tekstikappale-nimi", tekstikappale.getNimi(), pakolliset, virheellisetKielet, true);
            tarkistaTekstipalanen("peruste-validointi-tekstikappale-teksti", tekstikappale.getTeksti(), pakolliset, virheellisetKielet);
        }

        for (Map.Entry<String, String> entry : virheellisetKielet.entrySet()) {
            validointi.virhe(entry.getKey(), NavigationNodeDto.of(NavigationType.viite, mapper.map(perusteenOsa, PerusteenOsaDto.class).getNimi(), viite.getId()));
        }

        for (PerusteenOsaViite lapsi : viite.getLapset()) {
            tarkistaSisalto(lapsi, pakolliset, validointi);
        }
    }


    @Transactional(readOnly = true)
    private void tarkistaPerusopetuksenOppiaine(
            Oppiaine oa,
            final Set<Kieli> vaaditutKielet,
            Validointi validointi
    ) {
        Map<String, String> virheellisetKielet = new HashMap<>();
        OppiaineSuppeaDto oaDto = mapper.map(oa, OppiaineSuppeaDto.class);

        if (oa.getKoodiArvo() == null && oa.getKoodiUri() == null) {
            validointi.virhe("peruste-validointi-oppiaine-koodi", NavigationNodeDto.of(NavigationType.perusopetusoppiaine, oaDto.getNimiOrDefault(null), oa.getId()));
        }

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

        virheellisetKielet.entrySet().forEach(entry -> validointi.virhe(entry.getKey(), NavigationNodeDto.of(NavigationType.perusopetusoppiaine, oaDto.getNimiOrDefault(null), oa.getId())));

        if (oa.getOppimaarat() != null) {
            for (Oppiaine oppimaara : oa.getOppimaarat()) {
                tarkistaPerusopetuksenOppiaine(oppimaara, vaaditutKielet, validointi);
            }
        }
    }

    @Transactional(readOnly = true)
    private void tarkistaPerusopetuksenPeruste(Peruste peruste, Validointi validointi) {
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

        virheellisetKielet.entrySet().forEach(entry -> validointi.virhe(entry.getKey(), null, null));

        for (Oppiaine oa : oppiaineet) {
            tarkistaPerusopetuksenOppiaine(oa, vaaditutKielet, validointi);
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
        tarkistaTekstipalanen("peruste-validointi-nimi", peruste.getNimi(), vaaditutKielet, virheellisetKielet);

        if (peruste.getKoulutukset() != null) {
            for (Koulutus koulutus : peruste.getKoulutukset()) {
                tarkistaTekstipalanen("peruste-validointi-koulutus-nimi", koulutus.getNimi(), vaaditutKielet, virheellisetKielet);
            }
        }

        return virheellisetKielet;
    }

    @Transactional(readOnly = true)
    public Validointi tarkistaPerusteenSisaltoTekstipalaset(Peruste peruste) {
        Validointi validointi = new Validointi(ValidointiKategoria.KIELISISALTO);

        if (peruste.getTyyppi() == PerusteTyyppi.POHJA) {
            return validointi;
        }

        Set<Kieli> vaaditutKielet = peruste.getKielet();

        // Esiopetus
        if (peruste.getEsiopetuksenPerusteenSisalto() != null) {
            for (PerusteenOsaViite lapsi : peruste.getEsiopetuksenPerusteenSisalto().getSisalto().getLapset()) {
                tarkistaSisalto(lapsi, vaaditutKielet, validointi);
            }
        }

        // TPO
        if (peruste.getTpoOpetuksenSisalto() != null) {
            for (PerusteenOsaViite lapsi : peruste.getTpoOpetuksenSisalto().getSisalto().getLapset()) {
                tarkistaSisalto(lapsi, vaaditutKielet, validointi);
            }
        }

        // VST
        if (peruste.getVstSisalto() != null) {
            for (PerusteenOsaViite lapsi : peruste.getVstSisalto().getSisalto().getLapset()) {
                tarkistaSisalto(lapsi, vaaditutKielet, validointi);
            }
        }

        // TUVA
        if (peruste.getTuvasisalto() != null) {
            for (PerusteenOsaViite lapsi : peruste.getTuvasisalto().getSisalto().getLapset()) {
                tarkistaSisalto(lapsi, vaaditutKielet, validointi);
            }
        }

        // Perusopetus
        if (peruste.getPerusopetuksenPerusteenSisalto() != null) {
            for (PerusteenOsaViite lapsi : peruste.getPerusopetuksenPerusteenSisalto().getSisalto().getLapset()) {
                tarkistaSisalto(lapsi, vaaditutKielet, validointi);
            }
        }

        // Muut
        for (Suoritustapa st : peruste.getSuoritustavat()) {
            PerusteenOsaViite sisalto = st.getSisalto();
            if (sisalto != null) {
                for (PerusteenOsaViite lapsi : sisalto.getLapset()) {
                    tarkistaSisalto(lapsi, vaaditutKielet, validointi);
                }
            }

            RakenneModuuli rakenne = st.getRakenne();
            if (rakenne != null) {
                Map<String, String> virheellisetKielet = new HashMap<>();
                tarkistaRakenne(st.getRakenne(), vaaditutKielet, virheellisetKielet);

                for (Map.Entry<String, String> entry : virheellisetKielet.entrySet()) {
                    validointi.virhe(entry.getKey(), NavigationNodeDto.of(NavigationType.muodostuminen));
                }
            }

            for (TutkinnonOsaViite tov : st.getTutkinnonOsat()) {
                TutkinnonOsa tosa = tov.getTutkinnonOsa();
                Map<String, String> virheellisetKielet = new HashMap<>();
                tarkistaTekstipalanen("peruste-validointi-tutkinnonosa-ammattitaidon-osoittamistavat",
                        tosa.getAmmattitaidonOsoittamistavat(), vaaditutKielet, virheellisetKielet);
                tarkistaTekstipalanen("peruste-validointi-tutkinnonosa-ammattitaitovaatimukset",
                        tosa.getAmmattitaitovaatimukset(), vaaditutKielet, virheellisetKielet);
                tarkistaTekstipalanen("peruste-validointi-tutkinnonosa-kuvaus", tosa.getKuvaus(),
                        vaaditutKielet, virheellisetKielet);
                tarkistaTekstipalanen("peruste-validointi-tutkinnonosa-nimi", tosa.getNimi(),
                        vaaditutKielet, virheellisetKielet, true);

                for (Map.Entry<String, String> entry : virheellisetKielet.entrySet()) {
                    validointi.virhe(entry.getKey(), NavigationNodeDto.of(NavigationType.tutkinnonosaviite, mapper.map(tosa, TutkinnonOsaDto.class).getNimi(), tov.getId()));
                }
            }
        }

        return validointi;
    }

    @Override
    public List<Validointi> validate(Long id, ProjektiTila tila) {
        List<Validointi> validoinnit = new ArrayList<>();
        Validointi perusteValidointi = new Validointi(ValidointiKategoria.PERUSTE);
        validoinnit.add(perusteValidointi);

        Perusteprojekti projekti = repository.findOne(id);

        if (projekti == null) {
            throw new BusinessRuleViolationException("Projektia ei ole olemassa id:llä: " + id);
        }

        if (projekti.getPeruste().getTyyppi() == PerusteTyyppi.OPAS) {
            return validoinnit;
        }

        // Tarkistetaan että perusteelle on asetettu nimi perusteeseen asetetuilla kielillä
        if (tila != ProjektiTila.POISTETTU && tila != LAADINTA && tila != KOMMENTOINTI) {
            TekstiPalanen nimi = projekti.getPeruste().getNimi();
            for (Kieli kieli : projekti.getPeruste().getKielet()) {
                if (nimi == null || !nimi.getTeksti().containsKey(kieli)
                        || nimi.getTeksti().get(kieli).isEmpty()) {
                    perusteValidointi.virhe("perusteen-nimea-ei-ole-kaikilla-kielilla", NavigationNodeDto.of(NavigationType.tiedot));
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

                if (!ValidatorUtil.hasValidTutkintonimikkeet(peruste, tutkintonimikkeet)) {
                    perusteValidointi.virhe("tyhja-tutkintonimike-ei-sallittu", NavigationNodeDto.of(NavigationType.muodostuminen));
                }

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
                                    .collect(Collectors.toList());
                            perusteValidointi.virhe("tutkintonimikkeen-osaamisala-puuttuu-perusteesta", NavigationNodeDto.of(NavigationType.muodostuminen));
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
                                    .map(koodiDto -> koodiDto.getNimi())
                                    .collect(Collectors.toList());

                            puuttuvat.forEach(puuttuva -> perusteValidointi.virhe("osaamisalan-kuvauksia-puuttuu-sisallosta", NavigationNodeDto.of(NavigationType.muodostuminen), puuttuva.getTekstit()));
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
                                perusteValidointi.virhe("rakenteen-validointi-virhe", NavigationNodeDto.of(NavigationType.muodostuminen));
                            }
                        }

                        // Vapaiden tutkinnon osien tarkistus
                        List<TutkinnonOsaViite> vapaatOsat = vapaatTutkinnonosat(suoritustapa);
                        if (!vapaatOsat.isEmpty()) {
                            List<NavigableLokalisoituTekstiDto> nimet = new ArrayList<>();
                            for (TutkinnonOsaViite viite : vapaatOsat) {
                                if (viite.getTutkinnonOsa().getNimi() != null) {
                                    nimet.add(new NavigableLokalisoituTekstiDto(viite));
                                }
                            }
                            nimet.forEach(puuttuva -> perusteValidointi.virhe("liittamattomia-tutkinnon-osia", puuttuva.getNavigationNode()));
                        }

                        List<TutkinnonOsaViite> arvioinnitIlmanOtsikoita = arvioinnitIlmanOtsikoita(suoritustapa);
                        if (!arvioinnitIlmanOtsikoita.isEmpty()) {
                            List<NavigableLokalisoituTekstiDto> nimet = new ArrayList<>();
                            for (TutkinnonOsaViite viite : arvioinnitIlmanOtsikoita) {
                                if (viite.getTutkinnonOsa().getNimi() != null) {
                                    nimet.add(new NavigableLokalisoituTekstiDto(viite));
                                }
                            }
                            nimet.forEach(puuttuva -> perusteValidointi.virhe("tutkinnon-osan-arvioinnin-kohdealueelta-puuttuu-otsikko", puuttuva.getNavigationNode()));
                        }
                    }

                    // Tarkistetaan koodittomien tutkinnon osien nimet
                    List<TutkinnonOsaViite> koodittomatTutkinnonOsat = koodittomatTutkinnonosat(suoritustapa);
                    if (!koodittomatTutkinnonOsat.isEmpty()) {
                        List<NavigableLokalisoituTekstiDto> nimet = new ArrayList<>();
                        for (TutkinnonOsaViite viite : koodittomatTutkinnonOsat) {
                            if (!viite.getTutkinnonOsa().hasRequiredKielet()) {
                                nimet.add(new NavigableLokalisoituTekstiDto(viite));
                            }
                        }

                        nimet.forEach(puuttuva -> perusteValidointi.virhe("koodistoon-lisattavan-tutkinnon-osan-nimi-tulee-olla-kaannettyna-suomeksi-ja-ruotsiksi", puuttuva.getNavigationNode()));
                    }

                    // Tarkista tutke2-osien osa-alueiden koodit
                    List<NavigableLokalisoituTekstiDto> koodittomatOsaalueet = new ArrayList<>();
                    for (TutkinnonOsaViite tov : suoritustapa.getTutkinnonOsat()) {
                        TutkinnonOsa tosa = tov.getTutkinnonOsa();
                        if (TutkinnonOsaTyyppi.isTutke(tosa.getTyyppi())) {
                            for (OsaAlue oa : tosa.getOsaAlueet()) {
                                OsaAlueDto alueDto = mapper.map(oa, OsaAlueDto.class);
                                if (alueDto.getKoodiArvo() == null || alueDto.getKoodiArvo().isEmpty() ||
                                        alueDto.getKoodiUri() == null || alueDto.getKoodiUri().isEmpty()) {
                                    koodittomatOsaalueet.add(new NavigableLokalisoituTekstiDto(tov));
                                    break;
                                }
                            }
                        }
                    }

                    // Tarkistetaan osa-alueiden kooditukset
                    if (!koodittomatOsaalueet.isEmpty()) {
                        koodittomatOsaalueet.forEach(puuttuva -> perusteValidointi.virhe("tutke2-osalta-puuttuu-osa-alue-koodi", puuttuva.getNavigationNode()));
                    }

                    // Kerätään tutkinnon osien koodit
                    List<NavigableLokalisoituTekstiDto> virheellisetKoodistonimet = new ArrayList<>();
                    List<NavigableLokalisoituTekstiDto> uniikitKooditTosat = new ArrayList<>();
                    Set<String> uniikitKoodit = new HashSet<>();
                    for (TutkinnonOsaViite tov : getViitteet(suoritustapa)) {
                        TutkinnonOsa tosa = tov.getTutkinnonOsa();
                        TutkinnonOsaDto osaDto = mapper.map(tosa, TutkinnonOsaDto.class);

                        String uri = osaDto.getKoodiUri();
                        String arvo = osaDto.getKoodiArvo();

                        // Tarkistetaan onko sama koodi useammassa tutkinnon osassa
                        if (!ObjectUtils.isEmpty(uri)) {
                            if (tosa.getNimi() != null && uniikitKoodit.contains(uri)) {
                                uniikitKooditTosat.add(new NavigableLokalisoituTekstiDto(tov));
                            } else {
                                uniikitKoodit.add(uri);
                            }
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
                                virheellisetKoodistonimet.add(new NavigableLokalisoituTekstiDto(tov));
                            }
                        }
                    }

                    if (!virheellisetKoodistonimet.isEmpty()) {
                        virheellisetKoodistonimet.forEach(puuttuva -> perusteValidointi.virhe("tutkinnon-osan-asetettua-koodia-ei-koodistossa", puuttuva.getNavigationNode()));
                    }

                    if (!uniikitKooditTosat.isEmpty()) {
                        uniikitKooditTosat.forEach(puuttuva -> perusteValidointi.virhe("tutkinnon-osien-koodit-kaytossa-muissa-tutkinnon-osissa", puuttuva.getNavigationNode()));
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
                    perusteValidointi.virhe("tutkintonimikkeen-vaatimaa-tutkinnonosakoodia-ei-loytynyt-tutkinnon-osilta", null, null);
                }

            }

            if (tila == ProjektiTila.JULKAISTU || tila == ProjektiTila.VALMIS) {
                tarkistaPerusopetuksenPeruste(peruste, perusteValidointi);

                // Tarkista että kaikki vaadittu kielisisältö on asetettu
                Map<String, String> perusteenTiedotLokalisointiVirheet = tarkistaPerusteenTekstipalaset(projekti.getPeruste());
                for (Map.Entry<String, String> entry : perusteenTiedotLokalisointiVirheet.entrySet()) {
                    perusteValidointi.virhe(entry.getKey(), NavigationNodeDto.of(NavigationType.tiedot));
                }

                // Tarkista että kaikki vaadittu kielisisältö on asetettu
                validoinnit.add(tarkistaPerusteenSisaltoTekstipalaset(projekti.getPeruste()));

                validoinnit.add(tarkistaMaarays(projekti.getPeruste()));
            }

            if (tila == ProjektiTila.JULKAISTU) {
                if (!projekti.getPeruste().getTyyppi().equals(PerusteTyyppi.OPAS)) {
                    Diaarinumero diaarinumero = projekti.getPeruste().getDiaarinumero();
                    if (diaarinumero == null || StringUtils.isBlank(diaarinumero.getDiaarinumero())) {
                        perusteValidointi.virhe("peruste-ei-diaarinumeroa", NavigationNodeDto.of(NavigationType.tiedot));
                    }

                    if (projekti.getPeruste().getVoimassaoloAlkaa() == null) {
                        perusteValidointi.virhe("peruste-ei-voimassaolon-alkamisaikaa", NavigationNodeDto.of(NavigationType.tiedot));
                    }
                }
            }
        }

        return validoinnit;
    }

    private Validointi tarkistaMaarays(Peruste peruste) {
        Validointi validointi = new Validointi(ValidointiKategoria.PERUSTE);
        Maarays maarays = mapper.map(maaraysService.getPerusteenMaarays(peruste.getId()), Maarays.class);

        if (maarays != null) {
            Set<Kieli> vaaditutKielet = peruste.getKielet();
            Map<String, String> virheellisetKielet = new HashMap<>();
            tarkistaTekstipalanen("peruste-validointi-maarays-kuvaus", maarays.getKuvaus(), vaaditutKielet, virheellisetKielet, true);
            for (Map.Entry<String, String> entry : virheellisetKielet.entrySet()) {
                validointi.virhe(entry.getKey(), NavigationNodeDto.of(NavigationType.tiedot));
            }

            vaaditutKielet.forEach(kieli -> {
                if (peruste.getMaarayskirje() == null || !peruste.getMaarayskirje().getLiitteet().containsKey(kieli)) {
                    validointi.virhe("peruste-validointi-maarays-dokumentti", NavigationNodeDto.of(NavigationType.tiedot));
                }
            });
        }

        return validointi;
    }

    @Override
    public boolean applicableKoulutustyyppi(KoulutusTyyppi tyyppi) {
        return true;
    }

    @Override
    public boolean applicablePerustetyyppi(PerusteTyyppi tyyppi) {
        return !tyyppi.equals(PerusteTyyppi.DIGITAALINEN_OSAAMINEN);
    }

    @Override
    public boolean applicableTila(ProjektiTila tila) {
        return true;
    }
}
