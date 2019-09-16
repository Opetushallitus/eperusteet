package fi.vm.sade.eperusteet.service.impl;

import fi.vm.sade.eperusteet.domain.*;
import fi.vm.sade.eperusteet.domain.arviointi.ArvioinninKohdealue;
import fi.vm.sade.eperusteet.domain.arviointi.Arviointi;
import fi.vm.sade.eperusteet.domain.tutkinnonosa.Ammattitaitovaatimukset2019;
import fi.vm.sade.eperusteet.domain.tutkinnonosa.Ammattitaitovaatimus2019;
import fi.vm.sade.eperusteet.domain.tutkinnonosa.Ammattitaitovaatimus2019Kohdealue;
import fi.vm.sade.eperusteet.domain.tutkinnonosa.TutkinnonOsa;
import fi.vm.sade.eperusteet.domain.tutkinnonrakenne.TutkinnonOsaViite;
import fi.vm.sade.eperusteet.dto.AmmattitaitovaatimusQueryDto;
import fi.vm.sade.eperusteet.dto.Reference;
import fi.vm.sade.eperusteet.dto.koodisto.KoodistoDto;
import fi.vm.sade.eperusteet.dto.koodisto.KoodistoKoodiDto;
import fi.vm.sade.eperusteet.dto.koodisto.KoodistoMetadataDto;
import fi.vm.sade.eperusteet.dto.peruste.PerusteBaseDto;
import fi.vm.sade.eperusteet.dto.peruste.PerusteInfoDto;
import fi.vm.sade.eperusteet.dto.peruste.SuoritustapaDto;
import fi.vm.sade.eperusteet.dto.perusteprojekti.PerusteprojektiKevytDto;
import fi.vm.sade.eperusteet.dto.tutkinnonosa.Ammattitaitovaatimus2019Dto;
import fi.vm.sade.eperusteet.dto.tutkinnonosa.TutkinnonOsaDto;
import fi.vm.sade.eperusteet.dto.tutkinnonrakenne.KoodiDto;
import fi.vm.sade.eperusteet.dto.tutkinnonrakenne.TutkinnonOsaViiteKontekstiDto;
import fi.vm.sade.eperusteet.dto.util.LokalisoituTekstiDto;
import fi.vm.sade.eperusteet.repository.*;
import fi.vm.sade.eperusteet.service.AmmattitaitovaatimusService;
import fi.vm.sade.eperusteet.service.KoodistoClient;
import fi.vm.sade.eperusteet.service.mapping.Dto;
import fi.vm.sade.eperusteet.service.mapping.DtoMapper;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

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

    // FIXME
    private long nextKoodiId() {
        List<KoodistoKoodiDto> koodit = koodistoClient.getAll("ammattitaitovaatimukset");
        if (koodit.size() == 0) {
            return 1000L;
        }
        else {
            koodit.sort(Comparator.comparing(KoodistoKoodiDto::getKoodiArvo));
            for (int idx = 0; idx < koodit.size() - 2; ++idx) {
                long a = Long.parseLong(koodit.get(idx).getKoodiArvo()) + 1;
                long b = Long.parseLong(koodit.get(idx + 1).getKoodiArvo());
                if (a < b) {
                    return a;
                }
            }
            return Long.parseLong(koodit.get(koodit.size() - 1).getKoodiArvo()) + 1;
        }
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

    private List<TutkinnonOsaViite> getTutkinnonOsaViitteet(Peruste peruste) {
        return peruste.getSuoritustavat().stream()
                .map(Suoritustapa::getTutkinnonOsat)
                .flatMap(Collection::stream)
                .collect(Collectors.toList());
    }

    @Override
    public void updateAmmattitaitovaatimukset(Long perusteId) {
        Peruste peruste = perusteRepository.findOne(perusteId);
        List<TutkinnonOsaViite> tovs = getTutkinnonOsaViitteet(peruste);
        for (TutkinnonOsaViite tov : tovs) {
            if (tov.getTutkinnonOsa().getAmmattitaitovaatimukset() != null) {
                TekstiPalanen tekstit = tov.getTutkinnonOsa().getAmmattitaitovaatimukset();
                Map<Kieli, String> kohde = new HashMap<>();
                Map<Kieli, List<String>> vaatimukset = new HashMap<>();
                tekstit.getTeksti().forEach((key, value) -> {
                    Document doc = Jsoup.parse(value);
                    List<String> items = doc.select("li").stream()
                            .map(Element::text)
                            .collect(Collectors.toList());
                    kohde.put(key, doc.select("p").text());
                    vaatimukset.put(key, items);
                });
                Ammattitaitovaatimukset2019 av = new Ammattitaitovaatimukset2019();
            }
        }
    }

    @Override
    public List<KoodiDto> addAmmattitaitovaatimuskooditToKoodisto(Long perusteId) {
        List<KoodiDto> koodit = new ArrayList<>();
        Peruste peruste = perusteRepository.findOne(perusteId);
        long seuraavaKoodi = nextKoodiId();

        List<Ammattitaitovaatimus2019> vaatimukset = getVaatimukset(peruste);
        for (Ammattitaitovaatimus2019 v : vaatimukset) {
            if (v.getKoodi() == null) {
                KoodistoKoodiDto uusiKoodi = KoodistoKoodiDto.builder()
                        .koodiArvo(Long.toString(seuraavaKoodi))
                        .koodiUri("ammattitaitovaatimukset_" + seuraavaKoodi)
                        .koodisto(KoodistoDto.of("ammattitaitovaatimukset"))
                        .voimassaAlkuPvm(new Date())
                        .metadata(v.getVaatimus().getTeksti().entrySet().stream()
                                .map((k) -> KoodistoMetadataDto.of(k.getValue(), k.getKey().toString().toUpperCase(), k.getValue()))
                                .toArray(KoodistoMetadataDto[]::new))
                        .build();
                KoodistoKoodiDto lisattyKoodi = koodistoClient.addKoodi(uusiKoodi);
                if (lisattyKoodi == null
                        || lisattyKoodi.getKoodisto() == null
                        || lisattyKoodi.getKoodisto().getKoodistoUri() == null
                        || lisattyKoodi.getKoodiUri() == null) {
                    log.error("Koodin lisääminen epäonnistui {} {}", uusiKoodi, lisattyKoodi);
                    continue;
                }

                Koodi koodi = new Koodi();
                koodi.setKoodisto(lisattyKoodi.getKoodisto().getKoodistoUri());
                koodi.setUri(lisattyKoodi.getKoodiUri());
                koodi.setVersio(lisattyKoodi.getVersio() != null ? Long.valueOf(lisattyKoodi.getVersio()) : null);
                v.setKoodi(koodi);
                koodit.add(mapper.map(koodi, KoodiDto.class));
                ammattitaitovaatimusRepository.save(v);
                ++seuraavaKoodi;
            }
        }
        return koodit;
    }

    @Override
    public List<Ammattitaitovaatimus2019Dto> getAmmattitaitovaatimukset(Long perusteId) {
        Peruste peruste = perusteRepository.findOne(perusteId);
        List<Ammattitaitovaatimus2019> vaatimukset = getVaatimukset(peruste);
        return mapper.mapAsList(vaatimukset, Ammattitaitovaatimus2019Dto.class);
    }
}
