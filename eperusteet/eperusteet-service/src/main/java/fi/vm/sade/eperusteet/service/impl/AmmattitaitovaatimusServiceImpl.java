package fi.vm.sade.eperusteet.service.impl;

import fi.vm.sade.eperusteet.domain.Kieli;
import fi.vm.sade.eperusteet.domain.Koodi;
import fi.vm.sade.eperusteet.domain.KoodiRelaatioTyyppi;
import fi.vm.sade.eperusteet.domain.KoulutusTyyppi;
import fi.vm.sade.eperusteet.domain.Peruste;
import fi.vm.sade.eperusteet.domain.PerusteTila;
import fi.vm.sade.eperusteet.domain.PerusteTyyppi;
import fi.vm.sade.eperusteet.domain.ProjektiTila;
import fi.vm.sade.eperusteet.domain.Suoritustapa;
import fi.vm.sade.eperusteet.domain.Suoritustapakoodi;
import fi.vm.sade.eperusteet.domain.TekstiPalanen;
import fi.vm.sade.eperusteet.domain.arviointi.ArvioinninKohdealue;
import fi.vm.sade.eperusteet.domain.arviointi.Arviointi;
import fi.vm.sade.eperusteet.domain.tutkinnonosa.Ammattitaitovaatimukset2019;
import fi.vm.sade.eperusteet.domain.tutkinnonosa.Ammattitaitovaatimus2019;
import fi.vm.sade.eperusteet.domain.tutkinnonosa.Ammattitaitovaatimus2019Kohdealue;
import fi.vm.sade.eperusteet.domain.tutkinnonosa.OsaAlue;
import fi.vm.sade.eperusteet.domain.tutkinnonosa.Osaamistavoite;
import fi.vm.sade.eperusteet.domain.tutkinnonosa.TutkinnonOsa;
import fi.vm.sade.eperusteet.domain.tutkinnonrakenne.TutkinnonOsaViite;
import fi.vm.sade.eperusteet.dto.AmmattitaitovaatimusQueryDto;
import fi.vm.sade.eperusteet.dto.ParsitutAmmattitaitovaatimukset;
import fi.vm.sade.eperusteet.dto.koodisto.KoodistoKoodiDto;
import fi.vm.sade.eperusteet.dto.koodisto.KoodistoUriArvo;
import fi.vm.sade.eperusteet.dto.peruste.PerusteBaseDto;
import fi.vm.sade.eperusteet.dto.peruste.PerusteInfoDto;
import fi.vm.sade.eperusteet.dto.peruste.SuoritustapaDto;
import fi.vm.sade.eperusteet.dto.peruste.TutkintonimikeKoodiDto;
import fi.vm.sade.eperusteet.dto.perusteprojekti.PerusteprojektiKevytDto;
import fi.vm.sade.eperusteet.dto.tutkinnonosa.Ammattitaitovaatimus2019Dto;
import fi.vm.sade.eperusteet.dto.tutkinnonosa.TutkinnonOsaDto;
import fi.vm.sade.eperusteet.dto.tutkinnonrakenne.KoodiDto;
import fi.vm.sade.eperusteet.dto.tutkinnonrakenne.TutkinnonOsaViiteKontekstiDto;
import fi.vm.sade.eperusteet.dto.util.LokalisoituTekstiDto;
import fi.vm.sade.eperusteet.repository.AmmattitaitovaatimusRepository;
import fi.vm.sade.eperusteet.repository.ArvioinninKohdealueRepository;
import fi.vm.sade.eperusteet.repository.KoodiRepository;
import fi.vm.sade.eperusteet.repository.PerusteRepository;
import fi.vm.sade.eperusteet.service.AmmattitaitovaatimusService;
import fi.vm.sade.eperusteet.service.KoodistoClient;
import fi.vm.sade.eperusteet.service.PerusteService;
import fi.vm.sade.eperusteet.service.TutkinnonOsaViiteService;
import fi.vm.sade.eperusteet.service.event.aop.IgnorePerusteUpdateCheck;
import fi.vm.sade.eperusteet.service.mapping.Dto;
import fi.vm.sade.eperusteet.service.mapping.DtoMapper;
import fi.vm.sade.eperusteet.service.security.PermissionManager;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Stack;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.extern.slf4j.Slf4j;
import org.joda.time.DateTime;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@Transactional
public class AmmattitaitovaatimusServiceImpl implements AmmattitaitovaatimusService {

    @Autowired
    @Dto
    private DtoMapper mapper;

    @Autowired
    private PerusteRepository perusteRepository;

    @Autowired
    private KoodiRepository koodiRepository;

    @Autowired
    private ArvioinninKohdealueRepository arvioinninKohdealueRepository;

    @Autowired
    private AmmattitaitovaatimusRepository ammattitaitovaatimusRepository;

    @Autowired
    private KoodistoClient koodistoClient;

    @Autowired
    private PermissionManager permissionManager;

    @Autowired
    private TutkinnonOsaViiteService tutkinnonOsaViiteService;

    @Autowired
    private PerusteService perusteService;

    @Override
    public void addAmmattitaitovaatimuskoodit() {
        List<ArvioinninKohdealue> kohdealueet = perusteRepository.findAllPerusteet().stream()
                .filter(peruste -> Objects.equals(PerusteTila.VALMIS, peruste.getTila()))
                .filter(peruste -> peruste.getSuoritustavat().stream()
                        .map(Suoritustapa::getSuoritustapakoodi)
                        .anyMatch(koodi -> Objects.equals(Suoritustapakoodi.REFORMI, koodi)))
                .map(peruste -> peruste.getSuoritustavat().iterator().next().getTutkinnonOsat())
                .flatMap(Collection::stream)

                .map(TutkinnonOsaViite::getTutkinnonOsa)
                .map(TutkinnonOsa::getArviointi)
                .filter(Objects::nonNull)
                .map(Arviointi::getArvioinninKohdealueet)
                .flatMap(Collection::stream)
                .filter(arvioinninKohdealue -> Objects.isNull(arvioinninKohdealue.getKoodi()))
                .collect(Collectors.toList());

        long nextArvo = arvioinninKohdealueRepository.koodillisetCount() + 1L;
        log.info("Seuraava ammattitaitovaatimuksen arvo:" + String.valueOf(nextArvo));
        log.info("Koodittomia arvioinnin kohdealueita:  " + String.valueOf(kohdealueet.size()));

        for (ArvioinninKohdealue kohdealue : kohdealueet) {
            Koodi koodi = new Koodi();
            koodi.setKoodisto("ammattitaitovaatimukset");
            koodi.setUri("ammattitaitovaatimukset_" + String.valueOf(nextArvo));
            kohdealue.setKoodi(koodiRepository.save(koodi));
            ++nextArvo;
            arvioinninKohdealueRepository.save(kohdealue);
        }

        log.info("Arvioinnin kohdealueiden ammattitaitovaatimuskoodit lisätty");
    }

    @Override
    public Page<TutkinnonOsaViiteKontekstiDto> findTutkinnonOsat(PageRequest p, AmmattitaitovaatimusQueryDto pquery) {

        if(pquery.isKaikki()) {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if(!permissionManager.hasPermission(authentication, null, PermissionManager.Target.PERUSTEPROJEKTI, PermissionManager.Permission.LUKU)) {
                throw new AccessDeniedException("Pääsy evätty");
            }
        }

        Page<TutkinnonOsaViite> result = ammattitaitovaatimusRepository.findTutkinnonOsatBy(p, pquery);
        Page<TutkinnonOsaViiteKontekstiDto> resultDto = result.map(tov -> {
            TutkinnonOsaViiteKontekstiDto tovkDto = mapper.map(tov, TutkinnonOsaViiteKontekstiDto.class);
            TutkinnonOsa tosa = tov.getTutkinnonOsa();
            Peruste peruste = tov.getSuoritustapa().getPerusteet().iterator().next();
            tovkDto.setPeruste(mapper.map(peruste, PerusteInfoDto.class));
            tovkDto.setTutkinnonOsaDto(mapper.map(tosa, TutkinnonOsaDto.class));
            tovkDto.setSuoritustapa(mapper.map(tov.getSuoritustapa(), SuoritustapaDto.class));

            PerusteprojektiKevytDto perusteprojekti = new PerusteprojektiKevytDto();
            perusteprojekti.setId(peruste.getPerusteprojekti().getId());
            tovkDto.setPerusteProjekti(perusteprojekti);
            return tovkDto;
        });
        return resultDto;
    }

    @Override
    public Page<PerusteBaseDto> findPerusteet(PageRequest p, AmmattitaitovaatimusQueryDto pquery) {
        Page<Peruste> result = ammattitaitovaatimusRepository.findPerusteetBy(p, pquery);
        Page<PerusteBaseDto> resultDto = result.map(peruste -> mapper.map(peruste, PerusteBaseDto.class));
        return resultDto;
    }

    @Override
    public List<Ammattitaitovaatimus2019> getVaatimukset(Long perusteId) {
        Peruste peruste = perusteRepository.findOne(perusteId);
        return getVaatimukset(peruste);
    }

    @Override
    public List<ParsitutAmmattitaitovaatimukset> virheellisetAmmattitaitovaatimukset() {
        return perusteRepository.findAllAmosaa().stream()
                .filter(p -> p.getSuoritustavat().size() == 1
                        && Suoritustapakoodi.REFORMI.equals(p.getSuoritustavat().iterator().next().getSuoritustapakoodi()))
                .flatMap(peruste -> getTutkinnonOsaViitteet(peruste).stream()
                    .filter(tov -> tov.getTutkinnonOsa().getAmmattitaitovaatimukset2019() == null)
                    .filter(tov -> tov.getTutkinnonOsa().getAmmattitaitovaatimukset() != null)
                    .map(tov -> parsiVanhatAmmattitaitovaatimukset(peruste, tov)))
                .filter(x -> {
                    int fi = x.getVaatimukset().getOrDefault(Kieli.FI, new ArrayList<>()).size();
                    int sv = x.getVaatimukset().getOrDefault(Kieli.SV, new ArrayList<>()).size();
                    return fi > 0 && sv > 0 && fi != sv;
                })
                .collect(Collectors.toList());
    }

    private List<Ammattitaitovaatimus2019> getVaatimukset(Peruste peruste) {
        return getTutkinnonOsaViitteet(peruste).stream()
                .map(TutkinnonOsaViite::getTutkinnonOsa)
                .map(TutkinnonOsa::getAmmattitaitovaatimukset2019)
                .filter(Objects::nonNull)
                .map(av -> {
                    List<Ammattitaitovaatimus2019> v = new ArrayList<>(av.getVaatimukset());
                    v.addAll(av.getKohdealueet().stream()
                            .map(Ammattitaitovaatimus2019Kohdealue::getVaatimukset)
                            .flatMap(Collection::stream)
                            .collect(Collectors.toList()));
                    return v.stream();
                })
                .flatMap(x -> x)
                .collect(Collectors.toList());
    }

    private List<Ammattitaitovaatimus2019> getVaatimuksetFromOsaAlueet(Peruste peruste) {
        return getTutkinnonOsaViitteet(peruste).stream()
                .map(TutkinnonOsaViite::getTutkinnonOsa)
                .map(TutkinnonOsa::getOsaAlueet)
                .filter(Objects::nonNull)
                .flatMap(Collection::stream)
                .map(OsaAlue::getAllOsaamistavoitteet)
                .flatMap(Collection::stream)
                .map(Osaamistavoite::getTavoitteet2020)
                .filter(Objects::nonNull)
                .map(av -> {
                    List<Ammattitaitovaatimus2019> v = new ArrayList<>(av.getVaatimukset());
                    v.addAll(av.getKohdealueet().stream()
                            .map(Ammattitaitovaatimus2019Kohdealue::getVaatimukset)
                            .flatMap(Collection::stream)
                            .collect(Collectors.toList()));
                    return v.stream();
                })
                .flatMap(x -> x)
                .collect(Collectors.toList());
    }

    private List<ArvioinninKohdealue> getArvioinninKohdealueet(Peruste peruste) {
        return peruste.getSuoritustavat().stream()
                .map(Suoritustapa::getTutkinnonOsat)
                .flatMap(Collection::stream)
                .map(TutkinnonOsaViite::getTutkinnonOsa)
                .map(TutkinnonOsa::getArviointi)
                .filter(Objects::nonNull)
                .map(Arviointi::getArvioinninKohdealueet)
                .flatMap(Collection::stream)
                .filter(arvioinninKohdealue -> Objects.isNull(arvioinninKohdealue.getKoodi()))
                .collect(Collectors.toList());
    }

    private List<TutkinnonOsaViite> getTutkinnonOsaViitteet(Peruste peruste) {
        return peruste.getSuoritustavat().stream()
                .map(Suoritustapa::getTutkinnonOsat)
                .flatMap(Collection::stream)
                .collect(Collectors.toList());
    }

    private ParsitutAmmattitaitovaatimukset parsiVanhatAmmattitaitovaatimukset(Peruste peruste, TutkinnonOsaViite tov) {
        if (tov.getTutkinnonOsa().getAmmattitaitovaatimukset() == null) {
            return null;
        }
        ParsitutAmmattitaitovaatimukset result = new ParsitutAmmattitaitovaatimukset();
        TekstiPalanen tekstit = tov.getTutkinnonOsa().getAmmattitaitovaatimukset();
        tekstit.getTeksti().forEach((key, value) -> {
            Document doc = Jsoup.parse(value);
            List<String> items = doc.select("li").stream()
                    .map(Element::text)
                    .collect(Collectors.toList());
            result.getKohde().put(key, doc.select("p").text());
            result.getVaatimukset().put(key, items);
        });
        result.setProjektiId(peruste.getPerusteprojekti().getId());
        result.setPerusteId(peruste.getId());
        result.setTutkinnonOsa(tov.getTutkinnonOsa().getId());
        result.setTutkinnonOsaViite(tov.getId());
        return result;
    }

    @Override
    public void updateAmmattitaitovaatimukset(Long perusteId) {
        Peruste peruste = perusteRepository.findOne(perusteId);
        List<TutkinnonOsaViite> tovs = getTutkinnonOsaViitteet(peruste);
        for (TutkinnonOsaViite tov : tovs) {
            if (tov.getTutkinnonOsa().getAmmattitaitovaatimukset2019() != null) {
                continue;
            }
            ParsitutAmmattitaitovaatimukset parsitut = parsiVanhatAmmattitaitovaatimukset(peruste, tov);
            if (parsitut != null) {
                List<String> fi = parsitut.getVaatimukset().getOrDefault(Kieli.FI, new ArrayList<>());
                List<String> sv = parsitut.getVaatimukset().getOrDefault(Kieli.SV, new ArrayList<>());
                boolean lisaaRuotsi = fi.size() == sv.size();

                Ammattitaitovaatimukset2019 av = new Ammattitaitovaatimukset2019();
                av.setKohde(TekstiPalanen.of(parsitut.getKohde()));
                av.setVaatimukset(new ArrayList<>());

                for (int idx = 0; idx < fi.size(); ++idx) {
                    TekstiPalanen tp = TekstiPalanen.of(Kieli.FI, fi.get(idx));
                    if (lisaaRuotsi) {
                        tp.getTeksti().put(Kieli.SV, sv.get(idx));
                    }
                    av.getVaatimukset().add(Ammattitaitovaatimus2019.of(tp));
                }

                log.debug("Lisätty tutkinnon osan ammattitaitovaatimus");
                tov.getTutkinnonOsa().setAmmattitaitovaatimukset2019(av);
            }
        }
    }

    @Override
    @Async
    @IgnorePerusteUpdateCheck
    public void addAmmattitaitovaatimuskooditToKoodisto() {
        perusteRepository.findAmmattitaitovaatimusPerusteelliset(ProjektiTila.JULKAISTU,
                PerusteTyyppi.NORMAALI, KoulutusTyyppi.ammatilliset(), Suoritustapakoodi.REFORMI)
                .forEach(peruste -> {
                    addAmmattitaitovaatimuskooditToKoodisto(peruste.getId());
                    addArvioinninKohdealueetToKoodisto(peruste.getId());
                });
    }

    @Override
    @IgnorePerusteUpdateCheck
    public List<KoodiDto> addAmmattitaitovaatimusJaArvioinninkohteetKooditToKoodisto(Long perusteId) {
        List<KoodiDto> koodit =  Stream.concat(
                addAmmattitaitovaatimuskooditToKoodisto(perusteId).stream(),
                addArvioinninKohdealueetToKoodisto(perusteId).stream())
                .collect(Collectors.toList());

        lisaaTutkinnonOsienKoodiRelaatiot(perusteId);

        return koodit;
    }

    private void lisaaTutkinnonOsienKoodiRelaatiot(Long perusteId) {
        Peruste peruste = perusteRepository.findOne(perusteId);

        Map<String, List<String>> tutkinnonosienKoodit = getTutkinnonOsaViitteet(peruste).stream()
                .map(TutkinnonOsaViite::getTutkinnonOsa)
                .filter(tutkinnonosa -> tutkinnonosa.getKoodi() != null && tutkinnonosa.getKoodi().getUri() != null)
                .collect(Collectors.toMap(t -> t.getKoodi().getUri(), t -> t.getKaikkiKoodit().stream()
                        .map(Koodi::getUri)
                        .filter(Objects::nonNull)
                        .collect(Collectors.toList()), (uri1, uri2) -> new ArrayList<>(Stream.concat(uri1.stream(), uri2.stream()).collect(Collectors.toSet()))));

        Map<String, List<String>> osaAlueidenKoodit = getTutkinnonOsaViitteet(peruste).stream()
                .map(TutkinnonOsaViite::getTutkinnonOsa)
                .filter(tutkinnonosa -> tutkinnonosa.getKoodi() != null && tutkinnonosa.getKoodi().getUri() != null)
                .map(TutkinnonOsa::getOsaAlueet)
                .filter(Objects::nonNull)
                .flatMap(Collection::stream)
                .collect(Collectors.toMap(t -> t.getKoodi().getUri(), t -> t.getKaikkiKoodit().stream()
                        .map(Koodi::getUri)
                        .filter(Objects::nonNull)
                        .collect(Collectors.toList()), (uri1, uri2) -> new ArrayList<>(Stream.concat(uri1.stream(), uri2.stream()).collect(Collectors.toSet()))));

        tutkinnonosienKoodit.forEach(this::addAlarelaatiot);
        osaAlueidenKoodit.forEach(this::addAlarelaatiot);
    }

    @Override
    public List<KoodiDto> addAmmattitaitovaatimuskooditToKoodisto(Long perusteId) {
        Peruste peruste = perusteRepository.findOne(perusteId);
        return Stream.concat(
                        checkAndAddAmmattitaitovaatimukskoodit(KoodistoUriArvo.AMMATTITAITOVAATIMUKSET, getVaatimukset(peruste).stream().filter(vaatimus -> vaatimus.getKoodi() == null).collect(Collectors.toList())).stream(),
                        checkAndAddAmmattitaitovaatimukskoodit(KoodistoUriArvo.OSAAMISTAVOITTEET, getVaatimuksetFromOsaAlueet(peruste).stream().filter(vaatimus -> vaatimus.getKoodi() == null).collect(Collectors.toList())).stream())
                .collect(Collectors.toList());

    }

    private List<KoodiDto> checkAndAddAmmattitaitovaatimukskoodit(String koodisto, List<Ammattitaitovaatimus2019> vaatimukset) {
        List<KoodiDto> koodit = new ArrayList<>();

        if (vaatimukset.isEmpty()) {
            return koodit;
        }

        List<KoodistoKoodiDto> ammattitaitovaatimukset = koodistoClient.getAll(koodisto);
        Map<Long, KoodistoKoodiDto> olemassaOlevatKoodit = new LinkedHashMap<>();

        Map<Map<Kieli, String>, List<Ammattitaitovaatimus2019>> uniqueVaatimukset = new LinkedHashMap<>();
        vaatimukset.forEach(vaatimus -> {
            Kieli vaatimusKieli = vaatimus.getVaatimus().getTeksti().keySet().stream().findFirst().get();
            Optional<KoodistoKoodiDto> olemassaolevaKoodi = ammattitaitovaatimukset.stream()
                    .filter(amv -> amv.getMetadataName(vaatimusKieli.toString()) != null
                            && amv.getMetadataName(vaatimusKieli.toString()).getNimi().equals(vaatimus.getVaatimus().getTeksti().get(vaatimusKieli)))
                    .findFirst();
            if (olemassaolevaKoodi.isPresent()) {
                olemassaOlevatKoodit.put(vaatimus.getId(), olemassaolevaKoodi.get());
            } else {
                if (uniqueVaatimukset.get(vaatimus.getVaatimus().getTeksti()) == null) {
                    uniqueVaatimukset.put(vaatimus.getVaatimus().getTeksti(), new ArrayList<>());
                }

                uniqueVaatimukset.get(vaatimus.getVaatimus().getTeksti()).add(vaatimus);
            }

        });

        Stack<Long> koodiStack = new Stack<>();
        koodiStack.addAll(koodistoClient.nextKoodiId(koodisto, uniqueVaatimukset.keySet().size()));

        for (Map<Kieli, String> teksti : uniqueVaatimukset.keySet()) {

            LokalisoituTekstiDto lokalisoituTekstiDto = new LokalisoituTekstiDto(null, teksti);
            KoodistoKoodiDto lisattyKoodi = koodistoClient.addKoodiNimella(koodisto, lokalisoituTekstiDto, koodiStack.pop());

            if (lisattyKoodi == null) {
                log.error("Koodin lisääminen epäonnistui {} {}", lokalisoituTekstiDto, lisattyKoodi);
                continue;
            }

            Koodi koodi = new Koodi();
            koodi.setKoodisto(lisattyKoodi.getKoodisto().getKoodistoUri());
            koodi.setUri(lisattyKoodi.getKoodiUri());
            koodi.setVersio(lisattyKoodi.getVersio() != null ? Long.valueOf(lisattyKoodi.getVersio()) : null);
            koodit.add(mapper.map(koodi, KoodiDto.class));

            uniqueVaatimukset.get(teksti).forEach(ammattitaitovaatimus2019 -> {
                ammattitaitovaatimus2019.setKoodi(koodi);
                ammattitaitovaatimusRepository.save(ammattitaitovaatimus2019);
            });
        }

        for (Long ammattitaitovaatimus2019Id : olemassaOlevatKoodit.keySet()) {

            Ammattitaitovaatimus2019 ammattitaitovaatimus2019 = vaatimukset.stream()
                    .filter(vaatimus -> vaatimus.getId().equals(ammattitaitovaatimus2019Id))
                    .findFirst().get();
            KoodistoKoodiDto koodistoKoodi = olemassaOlevatKoodit.get(ammattitaitovaatimus2019Id);

            Koodi koodi = new Koodi();
            koodi.setKoodisto(koodisto);
            koodi.setUri(koodistoKoodi.getKoodiUri());
            koodi.setVersio(koodistoKoodi.getVersio() != null ? Long.valueOf(koodistoKoodi.getVersio()) : null);

            ammattitaitovaatimus2019.setKoodi(koodi);
            ammattitaitovaatimusRepository.save(ammattitaitovaatimus2019);
        }

        return koodit;
    }

    @Override
    public List<KoodiDto> addArvioinninKohdealueetToKoodisto(Long perusteId) {
        List<KoodiDto> koodit = new ArrayList<>();
        Peruste peruste = perusteRepository.findOne(perusteId);

        List<ArvioinninKohdealue> arvioinninKohdealiueet = getArvioinninKohdealueet(peruste).stream().filter(kohdealue -> kohdealue.getKoodi() == null).collect(Collectors.toList());

        if (arvioinninKohdealiueet.isEmpty()) {
            return koodit;
        }

        Map<Map<Kieli, String>, List<ArvioinninKohdealue>> uniqueKohdealueet = new LinkedHashMap<>();
        arvioinninKohdealiueet.forEach(vaatimus -> {
            if (uniqueKohdealueet.get(vaatimus.getOtsikko().getTeksti()) == null) {
                uniqueKohdealueet.put(vaatimus.getOtsikko().getTeksti(), new ArrayList<>());
            }

            uniqueKohdealueet.get(vaatimus.getOtsikko().getTeksti()).add(vaatimus);
        });

        Stack<Long> koodiStack = new Stack<>();
        koodiStack.addAll(koodistoClient.nextKoodiId("ammattitaitovaatimukset", uniqueKohdealueet.keySet().size()));

        for (Map<Kieli, String> teksti : uniqueKohdealueet.keySet()) {

            LokalisoituTekstiDto lokalisoituTekstiDto = new LokalisoituTekstiDto(null, teksti);
            KoodistoKoodiDto lisattyKoodi = koodistoClient.addKoodiNimella("ammattitaitovaatimukset", lokalisoituTekstiDto, koodiStack.pop());

            if (lisattyKoodi == null) {
                log.error("Koodin lisääminen epäonnistui {} {}", lokalisoituTekstiDto, lisattyKoodi);
                continue;
            }

            Koodi koodi = new Koodi();
            koodi.setKoodisto(lisattyKoodi.getKoodisto().getKoodistoUri());
            koodi.setUri(lisattyKoodi.getKoodiUri());
            koodi.setVersio(lisattyKoodi.getVersio() != null ? Long.valueOf(lisattyKoodi.getVersio()) : null);
            koodit.add(mapper.map(koodi, KoodiDto.class));

            uniqueKohdealueet.get(teksti).forEach(kohdealue -> {
                kohdealue.setKoodi(koodi);
                arvioinninKohdealueRepository.save(kohdealue);
            });
        }
        return koodit;
    }

    @Override
    public List<Ammattitaitovaatimus2019Dto> getAmmattitaitovaatimukset(Long perusteId) {
        Peruste peruste = perusteRepository.findOne(perusteId);
        List<Ammattitaitovaatimus2019> vaatimukset = getVaatimukset(peruste);
        return mapper.mapAsList(vaatimukset, Ammattitaitovaatimus2019Dto.class);
    }

    @IgnorePerusteUpdateCheck
    @Override
    public void lisaaAmmattitaitovaatimusTutkinnonosaKoodistoon(Date projektiPaivitysAika) {

        Date vrtAika = projektiPaivitysAika == null ? new DateTime(1970, 1, 1, 0, 0).toDate() : projektiPaivitysAika;
        List<Peruste> perusteet = perusteRepository.findByTilaAikaTyyppiKoulutustyyppi(ProjektiTila.JULKAISTU, vrtAika, PerusteTyyppi.NORMAALI, KoulutusTyyppi.ammatilliset());

        log.debug("Löytyi {} kpl perusteita", perusteet.size());
        perusteet.forEach(peruste -> {
            addTutkintoOsaKooditToTutkintonimikkeet(peruste);

            List<TutkinnonOsa> tutkinnonOsat = getTutkinnonOsaViitteet(peruste).stream().map(TutkinnonOsaViite::getTutkinnonOsa).collect(Collectors.toList());
            addTutkintoOsaKooditToKoulutus(peruste, tutkinnonOsat);
            addAmmattitaitovaatimusKooditToTutkintoOsa(tutkinnonOsat);
        });
    }

    private void addAmmattitaitovaatimusKooditToTutkintoOsa(List<TutkinnonOsa> tutkinnonOsat) {
        tutkinnonOsat.forEach(tutkinnonOsa -> {
            if (tutkinnonOsa.getKoodi() != null) {
                if (tutkinnonOsa.getAmmattitaitovaatimukset2019() != null) {
                    addAlarelaatiot(tutkinnonOsa.getKoodi().getUri(), tutkinnonOsa.getAmmattitaitovaatimukset2019().getVaatimukset().stream()
                            .filter(vaatimus -> vaatimus != null && vaatimus.getKoodi() != null)
                            .map(vaatimus -> vaatimus.getKoodi().getUri()).collect(Collectors.toList()));

                    addAlarelaatiot(tutkinnonOsa.getKoodi().getUri(), tutkinnonOsa.getAmmattitaitovaatimukset2019().getKohdealueet().stream()
                            .map(Ammattitaitovaatimus2019Kohdealue::getVaatimukset)
                            .flatMap(Collection::stream)
                            .filter(vaatimus -> vaatimus != null && vaatimus.getKoodi() != null)
                            .map(vaatimus -> vaatimus.getKoodi().getUri()).collect(Collectors.toList()));
                }
            }
        });
    }

    private void addTutkintoOsaKooditToKoulutus(Peruste peruste, List<TutkinnonOsa> tutkinnonOsat) {
        if (peruste.getKoulutukset() != null) {
            peruste.getKoulutukset().forEach(koulutus -> {
                tutkinnonOsat.forEach(tutkinnonOsa -> {
                    if (tutkinnonOsa.getKoodi() != null) {
                        addAlarelaatiot(koulutus.getKoulutuskoodiUri(), Collections.singletonList(tutkinnonOsa.getKoodi().getUri()));
                    }
                });
            });
        }
    }

    private void addTutkintoOsaKooditToTutkintonimikkeet(Peruste peruste) {
        List<TutkintonimikeKoodiDto> tutkintonimikekoodit = perusteService.getTutkintonimikeKoodit(peruste.getId());

        tutkintonimikekoodit.forEach((tutkintonimikekoodi -> {
            addAlarelaatiot(tutkintonimikekoodi.getTutkintonimikeUri(), Collections.singletonList(tutkintonimikekoodi.getTutkinnonOsaUri()));
            addAlarelaatiot(tutkintonimikekoodi.getOsaamisalaUri(), Collections.singletonList(tutkintonimikekoodi.getTutkinnonOsaUri()));
        }));

    }

    private void addAlarelaatiot(String koodiUri, List<String> lapsiKoodiUrit) {
        if (koodiUri != null) {
            log.debug("kasitellaan koodiUri: {}", koodiUri);
            List<String> alarelaatiot = koodistoClient.getAlarelaatio(koodiUri).stream().map(KoodistoKoodiDto::getKoodiUri).collect(Collectors.toList());
            lapsiKoodiUrit = lapsiKoodiUrit.stream()
                    .filter(lapsiKoodiUri -> lapsiKoodiUri != null && !alarelaatiot.contains(lapsiKoodiUri))
                    .collect(Collectors.toList());

            log.debug("Lisätään relaatiot {} <- {}", koodiUri, lapsiKoodiUrit);

            if (lapsiKoodiUrit.size() > 1) {
                koodistoClient.addKoodirelaatiot(koodiUri, lapsiKoodiUrit, KoodiRelaatioTyyppi.SISALTYY);
            } else if (lapsiKoodiUrit.size() == 1) {
                koodistoClient.addKoodirelaatio(koodiUri, lapsiKoodiUrit.get(0), KoodiRelaatioTyyppi.SISALTYY);
            }
        }
    }
}
