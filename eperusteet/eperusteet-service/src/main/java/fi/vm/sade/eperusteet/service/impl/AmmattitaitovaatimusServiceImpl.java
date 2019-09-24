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
import fi.vm.sade.eperusteet.service.security.PermissionManager;
import fi.vm.sade.eperusteet.service.util.SecurityUtil;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
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

    @Autowired
    private PermissionManager permissionManager;

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

        List<Ammattitaitovaatimus2019> vaatimukset = getVaatimukset(peruste).stream().filter(vaatimus -> vaatimus.getKoodi() == null).collect(Collectors.toList());
        Stack<Long> koodiStack = new Stack<>();
        koodiStack.addAll(koodistoClient.nextKoodiId("ammattitaitovaatimukset", vaatimukset.size()));

        for (Ammattitaitovaatimus2019 v : vaatimukset) {

                LokalisoituTekstiDto lokalisoituTekstiDto = new LokalisoituTekstiDto(null, v.getVaatimus().getTeksti());
                KoodistoKoodiDto lisattyKoodi = koodistoClient.addKoodiNimella("ammattitaitovaatimukset", lokalisoituTekstiDto, koodiStack.pop());

                if (lisattyKoodi == null) {
                    log.error("Koodin lisääminen epäonnistui {} {}", lokalisoituTekstiDto, lisattyKoodi);
                    continue;
                }

                Koodi koodi = new Koodi();
                koodi.setKoodisto(lisattyKoodi.getKoodisto().getKoodistoUri());
                koodi.setUri(lisattyKoodi.getKoodiUri());
                koodi.setVersio(lisattyKoodi.getVersio() != null ? Long.valueOf(lisattyKoodi.getVersio()) : null);
                v.setKoodi(koodi);
                koodit.add(mapper.map(koodi, KoodiDto.class));
                ammattitaitovaatimusRepository.save(v);
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
