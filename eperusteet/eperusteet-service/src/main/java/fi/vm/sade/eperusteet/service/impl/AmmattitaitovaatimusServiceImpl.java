package fi.vm.sade.eperusteet.service.impl;

import com.google.common.collect.Lists;
import fi.vm.sade.eperusteet.domain.*;
import fi.vm.sade.eperusteet.domain.arviointi.ArvioinninKohdealue;
import fi.vm.sade.eperusteet.domain.arviointi.Arviointi;
import fi.vm.sade.eperusteet.domain.tutkinnonosa.Ammattitaitovaatimukset2019;
import fi.vm.sade.eperusteet.domain.tutkinnonosa.Ammattitaitovaatimus2019;
import fi.vm.sade.eperusteet.domain.tutkinnonosa.Ammattitaitovaatimus2019Kohdealue;
import fi.vm.sade.eperusteet.domain.tutkinnonosa.TutkinnonOsa;
import fi.vm.sade.eperusteet.domain.tutkinnonrakenne.TutkinnonOsaViite;
import fi.vm.sade.eperusteet.dto.AmmattitaitovaatimusQueryDto;
import fi.vm.sade.eperusteet.dto.koodisto.KoodistoDto;
import fi.vm.sade.eperusteet.dto.koodisto.KoodistoKoodiDto;
import fi.vm.sade.eperusteet.dto.koodisto.KoodistoMetadataDto;
import fi.vm.sade.eperusteet.dto.peruste.PerusteBaseDto;
import fi.vm.sade.eperusteet.dto.tutkinnonosa.Ammattitaitovaatimus2019Dto;
import fi.vm.sade.eperusteet.dto.tutkinnonrakenne.KoodiDto;
import fi.vm.sade.eperusteet.dto.util.LokalisoituTekstiDto;
import fi.vm.sade.eperusteet.repository.*;
import fi.vm.sade.eperusteet.service.AmmattitaitovaatimusService;
import fi.vm.sade.eperusteet.service.KoodistoClient;
import fi.vm.sade.eperusteet.service.mapping.Dto;
import fi.vm.sade.eperusteet.service.mapping.DtoMapper;
import lombok.extern.slf4j.Slf4j;
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
    public Page<PerusteBaseDto> findPerusteet(PageRequest p, AmmattitaitovaatimusQueryDto pquery) {
        Page<Peruste> result = ammattitaitovaatimusRepository.findBy(p, pquery);
        Page<PerusteBaseDto> resultDto = result.map(peruste -> mapper.map(peruste, PerusteBaseDto.class));
        return resultDto;
    }

    private long nextKoodiId() {
        Koodi viimeisinKoodi = koodiRepository.findFirstByKoodistoOrderByUriDesc("ammattitaitovaatimukset");
        if (viimeisinKoodi != null) {
            KoodiDto koodiDto = mapper.map(viimeisinKoodi, KoodiDto.class);
            if (koodiDto.getArvo() != null) {
                return Long.valueOf(koodiDto.getArvo()) + 1;
            }
        }
        return 1000L;
    }

    private List<Ammattitaitovaatimus2019> getVaatimukset(Peruste peruste) {
        return peruste.getSuoritustavat().stream()
                .map(Suoritustapa::getTutkinnonOsat)
                .flatMap(Collection::stream)
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
                uusiKoodi = koodistoClient.addKoodi(uusiKoodi);
                Koodi koodi = new Koodi();
                koodi.setKoodisto(uusiKoodi.getKoodisto().getKoodistoUri());
                koodi.setUri(uusiKoodi.getKoodiUri());
                koodi.setVersio(uusiKoodi.getVersio() != null ? Long.valueOf(uusiKoodi.getVersio()) : null);
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
