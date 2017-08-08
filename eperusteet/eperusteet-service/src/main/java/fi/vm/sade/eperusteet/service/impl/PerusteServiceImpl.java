/*
 * Copyright (c) 2013 The Finnish Board of Education - Opetushallitus
 *
 * This program is free software: Licensed under the EUPL, Version 1.1 or - as
 * soon as they will be approved by the European Commission - subsequent versions
 * of the EUPL (the "Licence");
 *
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at: http://ec.europa.eu/idabc/eupl
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * European Union Public Licence for more details.
 */
package fi.vm.sade.eperusteet.service.impl;

import fi.vm.sade.eperusteet.domain.*;
import fi.vm.sade.eperusteet.domain.tutkinnonosa.TutkinnonOsa;
import fi.vm.sade.eperusteet.domain.tutkinnonrakenne.AbstractRakenneOsa;
import fi.vm.sade.eperusteet.domain.tutkinnonrakenne.RakenneModuuli;
import fi.vm.sade.eperusteet.domain.tutkinnonrakenne.TutkinnonOsaViite;
import fi.vm.sade.eperusteet.domain.yl.*;
import fi.vm.sade.eperusteet.domain.yl.lukio.LukioOpetussuunnitelmaRakenne;
import fi.vm.sade.eperusteet.domain.yl.lukio.LukiokoulutuksenPerusteenSisalto;
import fi.vm.sade.eperusteet.domain.yl.lukio.OpetuksenYleisetTavoitteet;
import fi.vm.sade.eperusteet.dto.LukkoDto;
import fi.vm.sade.eperusteet.dto.koodisto.KoodistoKoodiDto;
import fi.vm.sade.eperusteet.dto.peruste.*;
import fi.vm.sade.eperusteet.dto.perusteprojekti.PerusteprojektiLuontiDto;
import fi.vm.sade.eperusteet.dto.tutkinnonosa.TutkinnonOsaDto;
import fi.vm.sade.eperusteet.dto.tutkinnonosa.TutkinnonOsaExcelDto;
import fi.vm.sade.eperusteet.dto.tutkinnonosa.TutkinnonOsaKaikkiDto;
import fi.vm.sade.eperusteet.dto.tutkinnonosa.TutkinnonOsaTilaDto;
import fi.vm.sade.eperusteet.dto.tutkinnonrakenne.AbstractRakenneOsaDto;
import fi.vm.sade.eperusteet.dto.tutkinnonrakenne.RakenneModuuliDto;
import fi.vm.sade.eperusteet.dto.tutkinnonrakenne.TutkinnonOsaViiteDto;
import fi.vm.sade.eperusteet.dto.util.*;
import fi.vm.sade.eperusteet.dto.yl.lukio.LukiokoulutuksenYleisetTavoitteetDto;
import fi.vm.sade.eperusteet.repository.*;
import fi.vm.sade.eperusteet.repository.version.Revision;
import fi.vm.sade.eperusteet.service.*;
import fi.vm.sade.eperusteet.service.event.PerusteUpdatedEvent;
import fi.vm.sade.eperusteet.service.event.aop.IgnorePerusteUpdateCheck;
import fi.vm.sade.eperusteet.service.exception.BusinessRuleViolationException;
import fi.vm.sade.eperusteet.service.exception.NotExistsException;
import fi.vm.sade.eperusteet.service.internal.LockManager;
import fi.vm.sade.eperusteet.service.internal.SuoritustapaService;
import fi.vm.sade.eperusteet.service.mapping.Dto;
import fi.vm.sade.eperusteet.service.mapping.DtoMapper;
import fi.vm.sade.eperusteet.service.mapping.Koodisto;
import fi.vm.sade.eperusteet.service.yl.AihekokonaisuudetService;
import fi.vm.sade.eperusteet.service.yl.LukiokoulutuksenPerusteenSisaltoService;
import java.util.*;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.persistence.EntityNotFoundException;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.Validator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationListener;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.access.method.P;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 *
 * @author jhyoty
 */
@Service
@Transactional
public class PerusteServiceImpl implements PerusteService, ApplicationListener<PerusteUpdatedEvent> {

    private static final String KOODISTO_REST_URL = "https://virkailija.opintopolku.fi/koodisto-service/rest/json/";
    private static final String KOODISTO_RELAATIO_YLA = "relaatio/sisaltyy-ylakoodit/";
    private static final String KOODISTO_RELAATIO_ALA = "relaatio/sisaltyy-alakoodit/";
    private static final String KOULUTUSALALUOKITUS = "koulutusalaoph2002";
    private static final String OPINTOALALUOKITUS = "opintoalaoph2002";

    private static final List<String> ERIKOISTAPAUKSET = new ArrayList<>(Arrays.asList(new String[]{"koulutus_357802",
        "koulutus_327110", "koulutus_354803", "koulutus_324111", "koulutus_354710",
        "koulutus_324125", "koulutus_357709", "koulutus_327124", "koulutus_355904", "koulutus_324129", "koulutus_358903",
        "koulutus_327127",
        "koulutus_355412", "koulutus_324126", "koulutus_355413", "koulutus_324127", "koulutus_358412", "koulutus_327126",
        "koulutus_354708",
        "koulutus_324123", "koulutus_357707", "koulutus_327122"}));

    @Autowired
    private PerusteRepository perusteet;

    @Autowired
    private KVLiiteRepository kvliiteRepository;

    @Autowired
    private KoulutusRepository koulutusRepo;

    @Autowired
    private SuoritustapaService suoritustapaService;

    @Autowired
    private PerusteenOsaViiteService perusteenOsaViiteService;

    @Autowired
    private TutkinnonOsaViiteService tutkinnonOsaViiteService;

    @Autowired
    private PerusteenOsaViiteRepository perusteenOsaViiteRepo;

    @Autowired
    private KoodiRepository koodiRepository;

    @Autowired
    @Dto
    private DtoMapper mapper;

    @Autowired
    @Koodisto
    private DtoMapper koodistoMapper;

    @Autowired
    private SuoritustapaRepository suoritustapaRepository;

    @Autowired
    private PerusteenOsaRepository perusteenOsaRepository;

    @Autowired
    private TutkintonimikeKoodiRepository tutkintonimikeKoodiRepository;

    @Autowired
    private PerusteenOsaService perusteenOsaService;

    @Autowired
    private TutkinnonOsaViiteRepository tutkinnonOsaViiteRepository;

    @Autowired
    private TekstiPalanenRepository tekstiPalanenRepository;

    @Autowired
    private VuosiluokkaKokonaisuusRepository vuosiluokkaKokonaisuusRepository;

    @Autowired
    private LockManager lockManager;

    @Autowired
    private RakenneRepository rakenneRepository;

    @Autowired
    private OppiaineRepository oppiaineRepository;

    @Autowired
    private AihekokonaisuudetService aihekokonaisuudetService;

    @Autowired
    private LukioYleisetTavoitteetRepository lukioYleisetTavoitteetRepository;

    @Autowired
    private LukiokoulutuksenPerusteenSisaltoService lukiokoulutuksenPerusteenSisaltoService;

    @Autowired
    private Validator validator;

    @Autowired
    private KoodistoClient koodistoService;

    @Override
    public List<PerusteDto> getUusimmat() {
        return mapper.mapAsList(perusteet.findAllUusimmat(new PageRequest(0, 10)), PerusteDto.class);
    }

    @Override
    public List<PerusteExcelDto> getKooste() {
        return perusteet.findAll().stream()
                .filter(peruste -> peruste.getTila() == PerusteTila.VALMIS)
                .filter(peruste -> KoulutusTyyppi.of(peruste.getKoulutustyyppi()).isAmmatillinen())
                .map(peruste -> {
                    PerusteExcelDto result = mapper.map(peruste, PerusteExcelDto.class);
                    Set<TutkinnonOsa> tutkinnonOsat = new LinkedHashSet<>();
                    for (Suoritustapa st : peruste.getSuoritustavat()) {
                        for (TutkinnonOsaViite t : st.getTutkinnonOsat()) {
                            tutkinnonOsat.add(t.getTutkinnonOsa());
                        }
                    }
                    result.setTutkinnonOsat(mapper.mapAsList(tutkinnonOsat, TutkinnonOsaExcelDto.class));
                    return result;
                })
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PerusteHakuDto> getAll(PageRequest page, String kieli) {
        return findBy(page, new PerusteQuery());
    }

    @Override
    @Transactional(readOnly = true)
    public List<PerusteInfoDto> getAllInfo() {
        return mapper.mapAsList(perusteet.findAll(), PerusteInfoDto.class);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PerusteInfoDto> getAllPerusopetusInfo() {
        List<Peruste> res = new ArrayList<>();
        List<Peruste> perusopetus = perusteet.findAllByKoulutustyyppi(KoulutusTyyppi.PERUSOPETUS.toString());
        for (Peruste p : perusopetus) {
            if (p.getTila() == PerusteTila.VALMIS) {
                res.add(p);
            }
        }

        return mapper.mapAsList(res, PerusteInfoDto.class);
    }

    private Stream<Peruste> getPerusteetByUris(Stream<String> urit, Function<String, Stream<Peruste>> perusteByUriFinder) {
        return urit.map(perusteByUriFinder).flatMap(Function.identity());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PerusteHakuDto> findBy(PageRequest page, PerusteQuery pquery) {
        Stream<Peruste> koodistostaHaetut = Stream.empty();

        // Ladataan koodistosta osaamisala ja tutkintonimikehakua vastaavat koodit
        if (pquery.getNimi() != null && "".equals(pquery.getNimi()) && pquery.isOsaamisalat()) {
            Stream<KoodistoKoodiDto> urit = koodistoService.filterBy("osaamisala", pquery.getNimi());
            Stream <Peruste> osaamisalat = getPerusteetByUris(urit.map(KoodistoKoodiDto::getKoodiUri),
                    perusteet::findAllByOsaamisala);
            koodistostaHaetut = Stream.concat(koodistostaHaetut, osaamisalat);
        }

        // Haetaan perusteiden id:t mihin on liitetty osaamisalat tai tutkintonimikkeet
        if (pquery.getNimi() != null && pquery.isTutkintonimikkeet()) {
            koodistostaHaetut = Stream.concat(koodistostaHaetut, getPerusteetByUris(
                    koodistoService.filterBy(
                            "tutkintonimikkeet",
                            pquery.getNimi()).map(KoodistoKoodiDto::getKoodiUri),
                            tutkintonimikeKoodiRepository::findAllByTutkintonimikeUri));
        }

        // Lisätään mahdolliset perusteet hakujoukkoon
        Page<Peruste> result = perusteet.findBy(page, pquery, koodistostaHaetut.map(Peruste::getId).collect(Collectors.toSet()));
        PageDto<Peruste, PerusteHakuDto> resultDto = new PageDto<>(result, PerusteHakuDto.class, page, mapper);

        if (pquery.isTutkintonimikkeet()) {
            resultDto.forEach(dto -> {
                List<TutkintonimikeKoodiDto> tutkintonimikeKoodit = getTutkintonimikeKoodit(dto.getId());
                dto.setTutkintonimikkeet(mapper.mapAsList(tutkintonimikeKoodit, TutkintonimikeKoodiDto.class));

                List<CombinedDto<TutkintonimikeKoodiDto, HashMap<String, KoodistoKoodiDto>>> tutkintonimikkeetKoodisto = new ArrayList<>();

                // FIXME
                for (TutkintonimikeKoodiDto tkd : tutkintonimikeKoodit) {
                    HashMap<String, KoodistoKoodiDto> nimet = new HashMap<>();
                    if (tkd.getOsaamisalaUri() != null) {
                        nimet.put(tkd.getOsaamisalaArvo(), koodistoService.get("osaamisala", tkd.getOsaamisalaUri()));
                    }
                    nimet.put(tkd.getTutkintonimikeArvo(), koodistoService.get("tutkintonimikkeet", tkd.getTutkintonimikeUri()));
                    if (tkd.getTutkinnonOsaUri() != null) {
                        nimet.put(tkd.getTutkinnonOsaArvo(), koodistoService.get("tutkinnonosat", tkd.getTutkinnonOsaUri()));
                    }
                    tutkintonimikkeetKoodisto.add(new CombinedDto<>(tkd, nimet));
                }

                dto.setTutkintonimikkeetKoodisto(tutkintonimikkeetKoodisto);
            });
        }

        // Lisätään korvaavat ja korvattavat perusteet
        for (PerusteHakuDto haettu : resultDto) {
            Set<Diaarinumero> korvattavatDiaarinumerot = haettu.getKorvattavatDiaarinumerot().stream()
                    .map(Diaarinumero::new)
                    .collect(Collectors.toSet());

            if (!korvattavatDiaarinumerot.isEmpty()) {
                Set<Peruste> korvattavat = perusteet.findAllByDiaarinumerot(korvattavatDiaarinumerot);
                List<PerusteInfoDto> korvattavatDto = mapper.mapAsList(korvattavat, PerusteInfoDto.class);
                haettu.setKorvattavatPerusteet(korvattavatDto);
            }

            Set<Peruste> korvaajat = perusteet.findAllKorvaavatByDiaarinumero(haettu.getDiaarinumero());
            List<PerusteInfoDto> korvaajatDto = mapper.mapAsList(korvaajat, PerusteInfoDto.class);
            haettu.setKorvaavatPerusteet(korvaajatDto);
        }

        return resultDto;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PerusteHakuDto> findByInternal(PageRequest page, PerusteQuery pquery) {
        // Voidaan käyttää vain esikatseltaviin perusteprojektien perusteisiin
        pquery.setEsikatseltavissa(true);
        return findBy(page, pquery);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PerusteInfoDto> findByInfo(PageRequest page, PerusteQuery pquery) {
        Page<Peruste> result = perusteet.findBy(page, pquery);
        return new PageDto<>(result, PerusteInfoDto.class, page, mapper);
    }

    @Override
    @Transactional(readOnly = true)
    public PerusteInfoDto getMeta(final Long id) {
        Peruste p = perusteet.findOne(id);
        if (p.getTila() != PerusteTila.VALMIS) {
            throw new BusinessRuleViolationException("vain-julkaistuille-perusteille");
        }
        return mapper.map(p, PerusteInfoDto.class);
    }

    @Override
    @Transactional(readOnly = true)
    public PerusteDto get(final Long id) {
        Peruste p = perusteet.findOne(id);
        PerusteDto dto = mapper.map(p, PerusteDto.class);
        if (dto != null) {
            dto.setRevision(perusteet.getLatestRevisionId(id).getNumero());
            if (dto.getSuoritustavat() != null && !dto.getSuoritustavat().isEmpty()) {
                dto.setTutkintonimikkeet(getTutkintonimikeKoodit(id));
            }
        }

        return dto;
    }

    @Override
    @Transactional(readOnly = true)
    public PerusteKaikkiDto getAmosaaYhteinenPohja() {
        List<Peruste> loydetyt = perusteet.findAllByDiaarinumero(new Diaarinumero("amosaa/yhteiset"));

        if (loydetyt.size() == 1) {
            return getKokoSisalto(loydetyt.get(0).getId());
        } else {
            Optional<Peruste> op = loydetyt.stream()
                    .filter((p) -> p.getVoimassaoloAlkaa() != null)
                    .filter((p) -> p.getVoimassaoloAlkaa().before(new Date()))
                    .sorted(Comparator.comparing(Peruste::getVoimassaoloAlkaa))
                    .findFirst();

            if (op.isPresent()) {
                return getKokoSisalto(op.get().getId());
            }
        }

        return null;
    }

    @Override
    @Transactional(readOnly = true)
    public List<PerusteHakuDto> getAmosaaOpsit() {
        List<Peruste> amosaaPerusteet = perusteet.findAllAmosaa();
        return mapper.mapAsList(amosaaPerusteet, PerusteHakuDto.class);
    }

    @Override
    @Transactional(readOnly = true)
    public PerusteInfoDto getByDiaari(Diaarinumero diaarinumero) {
        List<Peruste> loydetyt = perusteet.findByDiaarinumeroAndTila(diaarinumero, PerusteTila.VALMIS);

        Peruste peruste = null;

        if (loydetyt.size() == 1) {
            peruste = loydetyt.get(0);
        } else {
            Optional<Peruste> op = loydetyt.stream()
                    .filter((p) -> p.getVoimassaoloAlkaa() != null)
                    .filter((p) -> p.getVoimassaoloAlkaa().before(new Date()))
                    .sorted(Comparator.comparing(Peruste::getVoimassaoloAlkaa))
                    .findFirst();

            if (op.isPresent()) {
                peruste = op.get();
            }
        }

        return peruste != null ? mapper.map(peruste, PerusteInfoDto.class) : null;
    }

    private void gatherOsaamisalaKuvaukset(PerusteenOsaViite pov, Map<String, List<TekstiKappaleDto>> stosaamisalat) {
        PerusteenOsa perusteenOsa = pov.getPerusteenOsa();
        if (perusteenOsa instanceof TekstiKappale) {
            TekstiKappale tk = (TekstiKappale) perusteenOsa;
            Koodi osaamisala = tk.getOsaamisala();
            if (osaamisala != null) {
                if (!stosaamisalat.containsKey(osaamisala.getUri())) {
                    stosaamisalat.put(osaamisala.getUri(), new ArrayList<>());
                }
                stosaamisalat.get(osaamisala.getUri()).add(mapper.map(tk, TekstiKappaleDto.class));
            }
        }

        for (PerusteenOsaViite lapsi : pov.getLapset()) {
            gatherOsaamisalaKuvaukset(lapsi, stosaamisalat);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Map<Suoritustapakoodi, Map<String, List<TekstiKappaleDto>>> getOsaamisalaKuvaukset(final Long id) {
        Peruste peruste = perusteet.findOne(id);
        Map<Suoritustapakoodi, Map<String, List<TekstiKappaleDto>>> osaamisalakuvaukset = new HashMap<>();
        Set<Suoritustapa> suoritustavat = peruste.getSuoritustavat();

        for (Suoritustapa st : suoritustavat) {
            Map<String, List<TekstiKappaleDto>> osaamisalat = new HashMap<>();
            osaamisalakuvaukset.put(st.getSuoritustapakoodi(), osaamisalat);
            gatherOsaamisalaKuvaukset(st.getSisalto(), osaamisalat);
        }

        return osaamisalakuvaukset;
    }

    @Override
    @Transactional(readOnly = true)
    public PerusteVersionDto getPerusteVersion(final long id) {
        return perusteet.getGlobalPerusteVersion(id);
    }

    @Override
    @Transactional(readOnly = true)
    public Revision getLastModifiedRevision(final Long id) {
        PerusteTila tila = perusteet.getTila(id);
        if (tila == null) {
            return null;
        }
        if (tila == PerusteTila.LUONNOS) {
            //luonnos-tilassa olevan perusteen viimeisimmän muokkauksen määrittäminen on epäluotettavaa.
            return Revision.DRAFT;
        }
        return perusteet.getLatestRevisionId(id);
    }

    @Override
    @Transactional(readOnly = true)
    public PerusteKaikkiDto getKokoSisalto(final Long id) {
        Peruste peruste = perusteet.findOne(id);
        if (peruste == null) {
            return null;
        }

        PerusteKaikkiDto perusteDto = mapper.map(peruste, PerusteKaikkiDto.class);
        if (peruste.getLukiokoulutuksenPerusteenSisalto() != null) {
            updateLukioKaikkiRakenne(perusteDto, peruste);
        }
        perusteDto.setRevision(perusteet.getLatestRevisionId(id).getNumero());

        if (!perusteDto.getSuoritustavat().isEmpty()
                && perusteDto.getLukiokoulutuksenPerusteenSisalto() == null) {
            perusteDto.setTutkintonimikkeet(getTutkintonimikeKoodit(id));

            Set<TutkinnonOsa> tutkinnonOsat = new LinkedHashSet<>();
            for (Suoritustapa st : peruste.getSuoritustavat()) {
                for (TutkinnonOsaViite t : st.getTutkinnonOsat()) {
                    tutkinnonOsat.add(t.getTutkinnonOsa());
                }
            }
            perusteDto.setTutkinnonOsat(mapper.mapAsList(tutkinnonOsat, TutkinnonOsaKaikkiDto.class));
        }

        return perusteDto;
    }

    private void updateLukioKaikkiRakenne(PerusteKaikkiDto perusteDto, Peruste peruste) {
        if (perusteDto.getLukiokoulutuksenPerusteenSisalto() != null) {
            perusteDto.getLukiokoulutuksenPerusteenSisalto().setRakenne(
                    lukiokoulutuksenPerusteenSisaltoService.getOppiaineTreeStructure(peruste.getId()));
        }
        if (perusteDto.getSuoritustavat() != null) {
            // turhia pois:
            perusteDto.getSuoritustavat().forEach(st -> {
                st.setRakenne(null);
                st.setSisalto(null);
                st.setTutkinnonOsat(null);
            });
        }
    }

    @Override
    @Transactional(readOnly = true)
    public PerusteDto getByIdAndSuoritustapa(final Long id, Suoritustapakoodi suoritustapakoodi) {
        Peruste p = perusteet.findPerusteByIdAndSuoritustapakoodi(id, suoritustapakoodi);
        return mapper.map(p, PerusteDto.class);
    }

    @Override
    @Transactional(readOnly = true)
    public PerusteenOsaViiteDto.Laaja getSuoritustapaSisalto(Long perusteId, Suoritustapakoodi suoritustapakoodi) {
        return getSuoritustapaSisalto(perusteId, suoritustapakoodi, PerusteenOsaViiteDto.Laaja.class);
    }

    @Override
    @Transactional(readOnly = true)
    public <T extends PerusteenOsaViiteDto.Puu<?, ?>> T getSuoritustapaSisalto(Long perusteId, Suoritustapakoodi suoritustapakoodi, Class<T> view) {

        Peruste peruste = perusteet.findOne(perusteId);
        if (peruste == null) {
            throw new NotExistsException("Perustetta ei ole olemassa");
        }
        PerusteenOsaViite sisalto = peruste.getSisalto(suoritustapakoodi);
        if (sisalto == null) {
            throw new NotExistsException("Perusteen sisältörakennetta ei ole olemassa");
        }
        return mapper.map(sisalto, view);
    }

    @Override
    @IgnorePerusteUpdateCheck
    @Transactional
//    @PreAuthorize("hasPermission(#event.perusteId, 'peruste', 'KORJAUS') or hasPermission(#event.perusteId, 'peruste', 'MUOKKAUS') " +
//            "or hasPermission(#event.perusteId, 'peruste', 'TILANVAIHTO')")
    public void onApplicationEvent(@P("event") PerusteUpdatedEvent event) {
        Peruste peruste = perusteet.findOne(event.getPerusteId());
        if (peruste == null) {
            return;
        }
        Date muokattu = new Date();
        if (peruste.getTila() == PerusteTila.VALMIS) {
            perusteet.setRevisioKommentti("Perusteen sisältöä korjattu");
            peruste.muokattu();
            muokattu = peruste.getMuokattu();
        }
        if (peruste.getGlobalVersion() == null) {
            peruste.setGlobalVersion(new PerusteVersion(peruste));
        }
        peruste.getGlobalVersion().setAikaleima(muokattu);
    }

    @Override
    public boolean isDiaariValid(String diaarinumero) {
        return diaarinumero == null
                || "".equals(diaarinumero)
                || Pattern.matches("^\\d{1,3}/\\d{3}/\\d{4}$", diaarinumero)
                || Pattern.matches("^OPH-\\d{1,5}-\\d{4}$", diaarinumero);
    }

    @Override
    public PerusteDto update(Long perusteId, PerusteDto perusteDto) {
        if (!isDiaariValid(perusteDto.getDiaarinumero())) {
            throw new BusinessRuleViolationException("diaarinumero-ei-validi");
        }

        Peruste current = perusteet.findOne(perusteId);
        if (current == null || current.getTila() == PerusteTila.POISTETTU) {
            throw new NotExistsException("Päivitettävää perustetta ei ole olemassa tai se on poistettu");
        }
        perusteet.lock(current);
        Peruste updated = mapper.map(perusteDto, Peruste.class);
        if (updated.getMuutosmaaraykset() != null) {
            for (Muutosmaarays muutosmaarays : updated.getMuutosmaaraykset()) {
                muutosmaarays.setPeruste(current);
            }
        }

        if (!current.getKoulutustyyppi().equals(updated.getKoulutustyyppi())) {
            throw new BusinessRuleViolationException("Koulutustyyppiä ei voi vaihtaa");
        }

        if (current.getTila() == PerusteTila.VALMIS) {
            current = updateValmisPeruste(current, updated);
        } else {
            // FIXME: refactor
            current.setDiaarinumero(updated.getDiaarinumero());
            current.setKielet(updated.getKielet());
            current.setKorvattavatDiaarinumerot(updated.getKorvattavatDiaarinumerot());
            current.setKoulutukset(updated.getKoulutukset());
            current.setKuvaus(updated.getKuvaus());
            current.setMaarayskirje(updated.getMaarayskirje());
            current.setMuutosmaaraykset(updated.getMuutosmaaraykset());
            current.setNimi(updated.getNimi());
            current.setOsaamisalat(updated.getOsaamisalat());
            current.setSiirtymaPaattyy(updated.getSiirtymaPaattyy());
            current.setVoimassaoloAlkaa(updated.getVoimassaoloAlkaa());
            current.setVoimassaoloLoppuu(updated.getVoimassaoloLoppuu());
            current.setPaatospvm(updated.getPaatospvm());
            current.setKoulutusvienti(updated.isKoulutusvienti());
        }

        perusteet.save(current);
        return mapper.map(current, PerusteDto.class);
    }

    @Override
    public PerusteDto updateFull(Long id, PerusteDto perusteDto) {
        Peruste current = perusteet.findOne(id);
        update(id, perusteDto);

        KVLiite liite = current.getKvliite();
        KVLiiteDto kvliiteDto = perusteDto.getKvliite();

        if (current.getKvliite() == null) {
            if (current.getTyyppi() == PerusteTyyppi.POHJA) {
                liite = mapper.map(kvliiteDto, KVLiite.class);
                kvliiteRepository.save(liite);
            }
        }
        else if (kvliiteDto != null) {
            if (kvliiteDto.getId() != null && !kvliiteDto.getId().equals(liite.getId())) {
                throw new BusinessRuleViolationException("virheellinen-liite");
            }
            kvliiteDto.setId(liite.getId());
            mapper.map(kvliiteDto, liite);
        }

        perusteet.save(current);
        return mapper.map(current, PerusteDto.class);
    }

    private Peruste updateValmisPeruste(Peruste current, Peruste updated) {
        current.setKielet(updated.getKielet());
        current.setKorvattavatDiaarinumerot(updated.getKorvattavatDiaarinumerot());
        current.setKoulutukset(updated.getKoulutukset());
        current.setMaarayskirje(updated.getMaarayskirje());
        current.setMuutosmaaraykset(updated.getMuutosmaaraykset());
        current.setKuvaus(updated.getKuvaus());
        current.setNimi(updated.getNimi());
        current.setPaatospvm(updated.getPaatospvm());
        current.setDiaarinumero(updated.getDiaarinumero());
        current.setKoulutusvienti(updated.isKoulutusvienti());

        if (updated.getOsaamisalat() != null && !Objects.deepEquals(current.getOsaamisalat(), updated.getOsaamisalat())) {
            throw new BusinessRuleViolationException("Valmiin perusteen osaamisaloja ei voi muuttaa");
        }

        current.setSiirtymaPaattyy(updated.getSiirtymaPaattyy());
        current.setVoimassaoloAlkaa(updated.getVoimassaoloAlkaa());
        current.setVoimassaoloLoppuu(updated.getVoimassaoloLoppuu());

        Set<ConstraintViolation<Peruste>> violations = validator.validate(current, Peruste.Valmis.class);
        if (!violations.isEmpty()) {
            throw new ConstraintViolationException(violations);
        }
        return current;
    }

    @Deprecated
    private Set<Koulutus> checkIfKoulutuksetAlreadyExists(Set<Koulutus> koulutukset) {
        Set<Koulutus> tmp = new HashSet<>();
        if (koulutukset != null) {
            for (Koulutus koulutus : koulutukset) {
                Koulutus k = koulutusRepo.findOneByKoulutuskoodiArvo(koulutus.getKoulutuskoodiArvo());
                if (k != null) {
                    k.mergeState(koulutus);
                    tmp.add(k);
                } else {
                    tmp.add(koulutus);
                }
            }
        }
        return tmp;
    }

    @Override
    @Transactional(readOnly = true)
    public SuoritustapaDto getSuoritustapa(Long perusteId, Suoritustapakoodi suoritustapakoodi) {
        Suoritustapa entity = getSuoritustapaEntity(perusteId, suoritustapakoodi);
        return mapper.map(entity, SuoritustapaDto.class);
    }

    @Override
    @Transactional(readOnly = true)
    public RakenneModuuliDto getTutkinnonRakenne(Long perusteid, Suoritustapakoodi suoritustapakoodi, Integer eTag) {

        Long rakenneId = rakenneRepository.getRakenneIdWithPerusteAndSuoritustapa(perusteid, suoritustapakoodi);
        if (rakenneId == null) {
            throw new NotExistsException("Rakennetta ei ole olemassa");
        }
        Revision rev = rakenneRepository.getLatestRevisionId(rakenneId);
        if (eTag != null && rev != null && rev.getNumero().equals(eTag)) {
            return null;
        }

        RakenneModuuli rakenne = rakenneRepository.findOne(rakenneId);
        RakenneModuuliDto rakenneModuuliDto = mapper.map(rakenne, RakenneModuuliDto.class);
        rakenneModuuliDto.setVersioId(rev.getNumero());
        return rakenneModuuliDto;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Revision> getRakenneVersiot(Long id, Suoritustapakoodi suoritustapakoodi) {
        List<Revision> versiot = new ArrayList<>();

        Peruste peruste = perusteet.findOne(id);
        if (peruste == null) {
            throw new EntityNotFoundException("Perustetta ei löytynyt id:llä: " + id);
        }
        Suoritustapa suoritustapa = peruste.getSuoritustapa(suoritustapakoodi);
        if (suoritustapa == null) {
            throw new EntityNotFoundException("Suoritustapaa " + suoritustapakoodi.toString() + " ei löytynyt");
        }
        RakenneModuuli rakenne = suoritustapa.getRakenne();
        if (rakenne != null) {
            versiot = rakenneRepository.getRevisions(rakenne.getId());
        }

        return versiot;
    }

    private Suoritustapa haeSuoritustapaVersio(Long id, Suoritustapakoodi suoritustapakoodi, Integer versioId) {
        Peruste peruste = perusteet.findOne(id);
        if (peruste == null) {
            throw new EntityNotFoundException("Perustetta ei löytynyt id:llä: " + id);
        }
        Suoritustapa suoritustapa = peruste.getSuoritustapa(suoritustapakoodi);
        if (suoritustapa == null) {
            throw new EntityNotFoundException("Suoritustapaa " + suoritustapakoodi.toString() + " ei löytynyt");
        }
        return suoritustapaRepository.findRevision(suoritustapa.getId(), versioId);
    }

    @Override
    @Transactional(readOnly = true)
    public RakenneModuuliDto getRakenneVersio(Long id, Suoritustapakoodi suoritustapakoodi, Integer versioId) {
        return mapper.map(haeSuoritustapaVersio(id, suoritustapakoodi, versioId).getRakenne(), RakenneModuuliDto.class);
    }

    @Override
    @Transactional
    public RakenneModuuliDto revertRakenneVersio(Long id, Suoritustapakoodi suoritustapakoodi, Integer versioId) {
        Suoritustapa suoritustapaVersio = haeSuoritustapaVersio(id, suoritustapakoodi, versioId);
        Peruste peruste = perusteet.findOne(id);
        Set<TutkinnonOsaViite> rakenneTovat = suoritustapaVersio.getTutkinnonOsat();

        Map<Long, TutkinnonOsaViite> tovat = new HashMap<>();
        Suoritustapa suoritustapa = peruste.getSuoritustapa(suoritustapakoodi);
        Set<TutkinnonOsaViite> tutkinnonOsat = suoritustapa.getTutkinnonOsat();

        for (TutkinnonOsaViite tov : tutkinnonOsat) {
            tovat.put(tov.getTutkinnonOsa().getId(), tov);
        }

        //TODO korjaa palautus
        for (TutkinnonOsaViite tov : rakenneTovat) {
            TutkinnonOsaViite utov = tovat.get(tov.getTutkinnonOsa().getId());
            if (utov == null) {
                TutkinnonOsaViiteDto tmp = mapper.map(tov, TutkinnonOsaViiteDto.class);
                tmp.setId(null);
                tmp = attachTutkinnonOsa(id, suoritustapakoodi, tmp);
                tov.setId(tmp.getId());
            } else if (!utov.getId().equals(tov.getId())) {
                tov.setId(utov.getId());
            }
        }
        return updateTutkinnonRakenne(id, suoritustapakoodi, mapper.map(suoritustapaVersio.getRakenne(), RakenneModuuliDto.class));
    }

    @Override
    @Transactional(readOnly = true)
    public List<TutkinnonOsaViiteDto> getTutkinnonOsat(Long perusteid, Suoritustapakoodi suoritustapakoodi) {
        Peruste peruste = perusteet.findOne(perusteid);
        Suoritustapa suoritustapa = peruste.getSuoritustapa(suoritustapakoodi);
        return mapper.mapAsList(suoritustapa.getTutkinnonOsat(), TutkinnonOsaViiteDto.class);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TutkinnonOsaTilaDto> getTutkinnonOsienTilat(Long perusteid, Suoritustapakoodi suoritustapakoodi) {
        Peruste peruste = perusteet.findOne(perusteid);
        Suoritustapa suoritustapa = peruste.getSuoritustapa(suoritustapakoodi);
        return mapper.mapAsList(suoritustapa.getTutkinnonOsat().stream()
                .map(TutkinnonOsaViite::getTutkinnonOsa)
                .collect(Collectors.toList()), TutkinnonOsaTilaDto.class);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TutkinnonOsaViiteDto> getTutkinnonOsat(Long perusteid, Suoritustapakoodi suoritustapakoodi, Integer revisio) {
        Peruste peruste = perusteet.findOne(perusteid);
        Suoritustapa suoritustapa = suoritustapaRepository.findRevision(peruste.getSuoritustapa(suoritustapakoodi).getId(), revisio);
        return mapper.mapAsList(suoritustapa.getTutkinnonOsat(), TutkinnonOsaViiteDto.class);
    }

    @Value("${fi.vm.sade.eperusteet.tutkinnonrakenne.maksimisyvyys}")
    private int maxRakenneDepth;

    @Override
    @Transactional
    public RakenneModuuliDto updateTutkinnonRakenne(Long perusteId, Suoritustapakoodi suoritustapakoodi, UpdateDto<RakenneModuuliDto> rakenne) {
        RakenneModuuliDto updated = updateTutkinnonRakenne(perusteId, suoritustapakoodi, rakenne.getDto());
        if (rakenne.getMetadata() != null) {
            perusteet.setRevisioKommentti(rakenne.getMetadata().getKommentti());
        }
        return updated;
    }

    @Override
    @Transactional
    public RakenneModuuliDto updateTutkinnonRakenne(Long perusteId, Suoritustapakoodi suoritustapakoodi, RakenneModuuliDto rakenne) {

        Suoritustapa suoritustapa = getSuoritustapaEntity(perusteId, suoritustapakoodi);
        lockManager.ensureLockedByAuthenticatedUser(suoritustapa.getRakenne().getId());

        rakenne.foreach(new VisitorImpl(maxRakenneDepth));
        RakenneModuuli moduuli = mapper.map(rakenne, RakenneModuuli.class);

        if (!moduuli.isSame(suoritustapa.getRakenne(), false)) {
            if (perusteet.findOne(perusteId).getTila() == PerusteTila.VALMIS) {
                if (!moduuli.isSame(suoritustapa.getRakenne(), true)) {
                    throw new BusinessRuleViolationException("Vain tekstimuutokset rakenteeseen ovat sallittuja");
                }
            }
            RakenneModuuli current = suoritustapa.getRakenne();
            moduuli = checkIfOsaamisalatAlreadyExists(moduuli);
            if (current != null) {
                current.mergeState(moduuli);
            } else {
                current = moduuli;
                suoritustapa.setRakenne(current);
            }
            rakenneRepository.save(current);
            onApplicationEvent(PerusteUpdatedEvent.of(this, perusteId));
        }

        return mapper.map(moduuli, RakenneModuuliDto.class);
    }

    private RakenneModuuli checkIfOsaamisalatAlreadyExists(RakenneModuuli rakenneModuuli) {
        Koodi osaamisalaTemp;
        if (rakenneModuuli != null) {
            if (rakenneModuuli.getOsaamisala() != null && rakenneModuuli.getOsaamisala().getUri() != null) {
                osaamisalaTemp = koodiRepository.findOneByUriAndVersio(rakenneModuuli.getOsaamisala().getUri(), rakenneModuuli.getOsaamisala().getVersio());
                if (osaamisalaTemp != null) {
                    rakenneModuuli.setOsaamisala(osaamisalaTemp);
                } else {
                    rakenneModuuli.setOsaamisala(koodiRepository.save(rakenneModuuli.getOsaamisala()));
                }
            } else {
                rakenneModuuli.setOsaamisala(null);
            }
            for (AbstractRakenneOsa osa : rakenneModuuli.getOsat()) {
                if (osa instanceof RakenneModuuli) {
                    osa = checkIfOsaamisalatAlreadyExists((RakenneModuuli) osa);
                }
            }
        }
        return rakenneModuuli;
    }

    @Override
    @Transactional
    public void removeTutkinnonOsa(Long id, Suoritustapakoodi suoritustapakoodi, Long osaId) {
        Suoritustapa suoritustapa = getSuoritustapaEntity(id, suoritustapakoodi);
        //varmistetaan että rakenteen muokkaus ei ole käynnissä.
        lockManager.lock(suoritustapa.getId());
        //workaround jolla estetään versiointiongelmat yhtäaikaisten muokkausten tapauksessa.
        suoritustapaRepository.lock(suoritustapa);
        try {
            TutkinnonOsaViite viite = tutkinnonOsaViiteRepository.findOne(osaId);
            if (suoritustapa.getTutkinnonOsat().contains(viite)) {
                if (tutkinnonOsaViiteRepository.isInUse(viite)) {
                    throw new BusinessRuleViolationException("Tutkinnonosa on käytössä");
                }
                suoritustapa.getTutkinnonOsat().remove(viite);
            } else {
                throw new BusinessRuleViolationException("Tutkinnonosa ei kuulu tähän suoritustapaan");
            }
        } finally {
            lockManager.unlock(suoritustapa.getId());
        }
    }

    @Override
    @Transactional
    public TutkinnonOsaViiteDto addTutkinnonOsa(Long id, Suoritustapakoodi suoritustapakoodi, TutkinnonOsaViiteDto osa) {
        final Suoritustapa suoritustapa = getSuoritustapaEntity(id, suoritustapakoodi);
        //workaround jolla estetään versiointiongelmat yhtäaikaisten muokkausten tapauksessa.
        suoritustapaRepository.lock(suoritustapa);
        TutkinnonOsaViite viite = mapper.map(osa, TutkinnonOsaViite.class);

        if (viite.getTutkinnonOsa() == null && osa.getTutkinnonOsaDto() != null) {
            viite.setTutkinnonOsa(mapper.map(osa.getTutkinnonOsaDto(), TutkinnonOsa.class));
            viite.getTutkinnonOsa().setId(null);
        }

        if (viite.getTutkinnonOsa() == null) {
            TutkinnonOsa tutkinnonOsa = new TutkinnonOsa();
            if (osa.getTyyppi() != null) {
                tutkinnonOsa.setTyyppi(osa.getTyyppi());
            }
            viite.setTutkinnonOsa(tutkinnonOsa);
        }
        viite.setSuoritustapa(suoritustapa);
        viite.setMuokattu(new Date());
        if (suoritustapa.getTutkinnonOsat().add(viite)) {
            viite = tutkinnonOsaViiteRepository.save(viite);
        } else {
            throw new BusinessRuleViolationException("Viite tutkinnon osaan on jo olemassa");
        }
        return mapper.map(viite, TutkinnonOsaViiteDto.class);

    }

    @Override
    @Transactional
    public TutkinnonOsaViiteDto attachTutkinnonOsa(Long id, Suoritustapakoodi suoritustapakoodi, TutkinnonOsaViiteDto osa) {
        final Suoritustapa suoritustapa = getSuoritustapaEntity(id, suoritustapakoodi);
        final Peruste peruste = perusteet.findPerusteByIdAndSuoritustapakoodi(id, suoritustapakoodi);
        //if (peruste.getKoulutustyyppi().equals(KoulutusTyyppi.VALMA))
        // Workaround jolla estetään versiointiongelmat yhtäaikaisten muokkausten tapauksessa
        suoritustapaRepository.lock(suoritustapa, false);
        TutkinnonOsaViite viite = mapper.map(osa, TutkinnonOsaViite.class);
        viite.setSuoritustapa(suoritustapa);
        viite.setMuokattu(new Date());
        if (suoritustapa.getTutkinnonOsat().add(viite)) {
            viite = tutkinnonOsaViiteRepository.save(viite);
        } else {
            throw new BusinessRuleViolationException("Viite tutkinnon osaan on jo olemassa");
        }
        return mapper.map(viite, TutkinnonOsaViiteDto.class);
    }

    @Override
    @Transactional
    public TutkinnonOsaViiteDto updateTutkinnonOsa(Long id, Suoritustapakoodi suoritustapakoodi, TutkinnonOsaViiteDto osa) {
        final Suoritustapa suoritustapa = getSuoritustapaEntity(id, suoritustapakoodi);
        TutkinnonOsaViite viite = tutkinnonOsaViiteRepository.findOne(osa.getId());

        if (viite == null || !viite.getSuoritustapa().equals(suoritustapa)
                || !viite.getTutkinnonOsa().getReference().equals(osa.getTutkinnonOsa())) {
            throw new BusinessRuleViolationException("Virheellinen viite");
        }

        TutkinnonOsaViiteDto dto = tutkinnonOsaViiteService.update(osa);
        onApplicationEvent(PerusteUpdatedEvent.of(this, id));
        return dto;
    }

    @Override
    @Transactional
    public TutkinnonOsaViiteDto updateTutkinnonOsa(Long id, Suoritustapakoodi suoritustapakoodi, TutkinnonOsaViiteUpdateDto osa) {
        TutkinnonOsaViiteDto updated = updateTutkinnonOsa(id, suoritustapakoodi, osa.getDto());

        if (osa.getMetadata() != null) {
            tutkinnonOsaViiteRepository.setRevisioKommentti(osa.getMetadata().getKommentti());
        }
        return updated;

    }

    @Override
    @Transactional(readOnly = true)
    public TutkinnonOsaViiteDto getTutkinnonOsaViite(Long perusteId, Suoritustapakoodi suoritustapakoodi, Long viiteId) {
        final Suoritustapa suoritustapa = getSuoritustapaEntity(perusteId, suoritustapakoodi);
        TutkinnonOsaViite viite = tutkinnonOsaViiteRepository.findOne(viiteId);

        if (viite == null || !viite.getSuoritustapa().equals(suoritustapa)) {
            throw new BusinessRuleViolationException("Virheellinen viiteId");
        }

        TutkinnonOsaViiteDto viiteDto = mapper.map(viite, TutkinnonOsaViiteDto.class);
        TutkinnonOsaDto tutkinnonOsaDto = mapper.map(viite.getTutkinnonOsa(), TutkinnonOsaDto.class);
        viiteDto.setTutkinnonOsaDto(tutkinnonOsaDto);

        return viiteDto;
    }

    private Suoritustapa getSuoritustapaEntity(Long perusteid, Suoritustapakoodi suoritustapakoodi) {
        if (!perusteet.exists(perusteid)) {
            throw new NotExistsException("Perustetta ei ole olemassa");
        }
        Suoritustapa suoritustapa = suoritustapaRepository.findByPerusteAndKoodi(perusteid, suoritustapakoodi);
        if (suoritustapa == null) {
            throw new BusinessRuleViolationException(
                    "Perusteella " + perusteid + " + ei ole suoritustapaa " + suoritustapakoodi
            );
        }
        return suoritustapa;
    }

    @Override
    @Transactional
    public PerusteenOsaViiteDto.Matala addSisalto(Long perusteId, Suoritustapakoodi suoritustapakoodi, PerusteenOsaViiteDto.Matala viite) {
        Suoritustapa suoritustapa = getSuoritustapaEntity(perusteId, suoritustapakoodi);
        if (suoritustapa == null) {
            throw new BusinessRuleViolationException("Suoritustapaa ei ole");
        }
        return perusteenOsaViiteService.addSisalto(perusteId, suoritustapa.getSisalto().getId(), viite);
    }

    @Override
    @Transactional
    public PerusteenOsaViiteDto.Matala addSisaltoUUSI(Long perusteId, Suoritustapakoodi suoritustapakoodi, PerusteenOsaViiteDto.Matala viite) {
        Peruste peruste = perusteet.findOne(perusteId);
        if (peruste == null) {
            throw new NotExistsException("Perustetta ei ole olemassa");
        }
        PerusteenOsaViite sisalto = peruste.getSisalto(suoritustapakoodi);
        if (sisalto == null) {
            throw new NotExistsException("Perusteen sisältörakennetta ei ole olemassa");
        }
        return perusteenOsaViiteService.addSisalto(perusteId, sisalto.getId(), viite);
    }

    @Override
    @Transactional
    public PerusteenOsaViiteDto.Matala addSisaltoLapsi(Long perusteId, Long perusteenosaViiteId, PerusteenOsaViiteDto.Matala viite) {
        return perusteenOsaViiteService.addSisalto(perusteId, perusteenosaViiteId, viite);
    }

    private void getLocksPerusteenOsat(PerusteenOsaViite sisalto, Map<Long, LukkoDto> map) {
        for (PerusteenOsaViite lapsi : sisalto.getLapset()) {
            LukkoDto lock = perusteenOsaService.getLock(lapsi.getPerusteenOsa().getId());
            if (lock != null) {
                map.put(lapsi.getPerusteenOsa().getId(), lock);
            }
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<TutkintonimikeKoodiDto> getTutkintonimikeKoodit(Long perusteId) {
        List<TutkintonimikeKoodi> koodit = tutkintonimikeKoodiRepository.findByPerusteId(perusteId);
        return mapper.mapAsList(koodit, TutkintonimikeKoodiDto.class);
    }

    @Override
    public TutkintonimikeKoodiDto addTutkintonimikeKoodi(Long perusteId, TutkintonimikeKoodiDto dto) {
        Peruste peruste = perusteet.findOne(perusteId);
        dto.setPeruste(peruste.getReference());
        TutkintonimikeKoodi tnk = mapper.map(dto, TutkintonimikeKoodi.class);
        TutkintonimikeKoodi saved = tutkintonimikeKoodiRepository.save(tnk);
        return mapper.map(saved, TutkintonimikeKoodiDto.class);
    }

    @Override
    public void removeTutkintonimikeKoodi(Long perusteId, Long tutkintonimikeKoodiId) {
        TutkintonimikeKoodi tnk = tutkintonimikeKoodiRepository.findOne(tutkintonimikeKoodiId);
        if (Objects.equals(tnk.getPeruste().getId(), perusteId)) {
            tutkintonimikeKoodiRepository.delete(tutkintonimikeKoodiId);
        }
    }

    private void lisaaTutkinnonMuodostuminen(Peruste peruste) {
        if (KoulutusTyyppi.PERUSOPETUS.toString().equals(peruste.getKoulutustyyppi())) {
            PerusteenOsaViite sisalto = peruste.getPerusopetuksenPerusteenSisalto().getSisalto();
            TekstiKappale tk = new TekstiKappale();
            HashMap<Kieli, String> hm = new HashMap<>();
            hm.put(Kieli.FI, "Laaja-alaiset osaamiset");
            tk.setNimi(tekstiPalanenRepository.save(TekstiPalanen.of(hm)));
            tk.setTunniste(PerusteenOsaTunniste.LAAJAALAINENOSAAMINEN);
            PerusteenOsaViite pov = perusteenOsaViiteRepo.save(new PerusteenOsaViite());
            pov.setPerusteenOsa(perusteenOsaRepository.save(tk));
            pov.setVanhempi(sisalto);
            sisalto.getLapset().add(pov);
        } else if (KoulutusTyyppi.AIKUISTENPERUSOPETUS.toString().equals(peruste.getKoulutustyyppi())) {
            PerusteenOsaViite sisalto = peruste.getAipeOpetuksenPerusteenSisalto().getSisalto();
            TekstiKappale tk = new TekstiKappale();
            HashMap<Kieli, String> hm = new HashMap<>();
            hm.put(Kieli.FI, "Laaja-alaiset osaamiset");
            tk.setNimi(tekstiPalanenRepository.save(TekstiPalanen.of(hm)));
            tk.setTunniste(PerusteenOsaTunniste.LAAJAALAINENOSAAMINEN);
            PerusteenOsaViite pov = perusteenOsaViiteRepo.save(new PerusteenOsaViite());
            pov.setPerusteenOsa(perusteenOsaRepository.save(tk));
            pov.setVanhempi(sisalto);
            sisalto.getLapset().add(pov);
        } else {
            for (Suoritustapa st : peruste.getSuoritustavat()) {
                PerusteenOsaViite sisalto = st.getSisalto();
                List<PerusteenOsaViite> lapset = sisalto.getLapset();
                TekstiKappale tk = new TekstiKappale();
                HashMap<Kieli, String> hm = new HashMap<>();
                hm.put(Kieli.FI, "Tutkinnon muodostuminen");
                tk.setNimi(tekstiPalanenRepository.save(TekstiPalanen.of(hm)));
                tk.setTunniste(PerusteenOsaTunniste.RAKENNE);
                PerusteenOsaViite pov = perusteenOsaViiteRepo.save(new PerusteenOsaViite());
                pov.setPerusteenOsa(perusteenOsaRepository.save(tk));
                pov.setVanhempi(sisalto);
                lapset.add(pov);
            }
        }
    }

    public Peruste luoPerusteRunko(KoulutusTyyppi koulutustyyppi, LaajuusYksikko yksikko, PerusteTyyppi tyyppi) {
        return luoPerusteRunko(koulutustyyppi, yksikko, tyyppi, false);
    }

    /**
     * Luo uuden perusteen perusrakenteella.
     *
     * @param koulutustyyppi Koulutustyyppi
     * @param yksikko Yksikkö
     * @param isReforminMukainen Reformin mukainen
     * @param tyyppi Tyyppi
     * @return Palauttaa 'tyhjän' perusterungon
     */
    @Override
    public Peruste luoPerusteRunko(
            KoulutusTyyppi koulutustyyppi,
            LaajuusYksikko yksikko,
            PerusteTyyppi tyyppi,
            boolean isReforminMukainen
    ) {
        if (koulutustyyppi == null) {
            throw new BusinessRuleViolationException("Koulutustyyppiä ei ole asetettu");
        }

        Peruste peruste = new Peruste();
        peruste.setKoulutustyyppi(koulutustyyppi.toString());
        peruste.setTyyppi(tyyppi);
        Set<Suoritustapa> suoritustavat = new HashSet<>();
        yksikko = yksikko != null ? yksikko : LaajuusYksikko.OSAAMISPISTE;

        if (!isReforminMukainen && koulutustyyppi.isAmmatillinen()) {
            suoritustavat.add(suoritustapaService.createSuoritustapaWithSisaltoAndRakenneRoots(Suoritustapakoodi.NAYTTO, yksikko));
        }

        Suoritustapa st = null;

        // ~2018 eteenpäin koulutustyypit 1, 11 ja 12
        if (isReforminMukainen) {
            st = suoritustapaService.createSuoritustapaWithSisaltoAndRakenneRoots(Suoritustapakoodi.REFORMI, LaajuusYksikko.OSAAMISPISTE);
        } else if (koulutustyyppi.isOneOf(KoulutusTyyppi.PERUSTUTKINTO, KoulutusTyyppi.TELMA, KoulutusTyyppi.VALMA)) {
            st = suoritustapaService.createSuoritustapaWithSisaltoAndRakenneRoots(Suoritustapakoodi.OPS, yksikko);
        } else if (koulutustyyppi == KoulutusTyyppi.PERUSOPETUS) {
            peruste.setPerusopetuksenPerusteenSisalto(new PerusopetuksenPerusteenSisalto());
        } else if (koulutustyyppi == KoulutusTyyppi.ESIOPETUS
                || koulutustyyppi == KoulutusTyyppi.PERUSOPETUSVALMISTAVA
                || koulutustyyppi == KoulutusTyyppi.LISAOPETUS
                || koulutustyyppi == KoulutusTyyppi.VARHAISKASVATUS) {
            peruste.setEsiopetuksenPerusteenSisalto(new EsiopetuksenPerusteenSisalto());
        } else if (koulutustyyppi == KoulutusTyyppi.LUKIOKOULUTUS
                || koulutustyyppi == KoulutusTyyppi.AIKUISTENLUKIOKOULUTUS
                || koulutustyyppi == KoulutusTyyppi.LUKIOVALMISTAVAKOULUTUS) {
            st = suoritustapaService.createSuoritustapaWithSisaltoAndRakenneRoots(Suoritustapakoodi.LUKIOKOULUTUS, LaajuusYksikko.KURSSI);
            LukiokoulutuksenPerusteenSisalto sisalto = new LukiokoulutuksenPerusteenSisalto();
            initLukioOpetuksenYleisetTavoitteet(sisalto);
            aihekokonaisuudetService.initAihekokonaisuudet(sisalto);
            initLukioOpetussuunitelmaRakenne(peruste, sisalto);
        }
        else if (koulutustyyppi == KoulutusTyyppi.AIKUISTENPERUSOPETUS) {
            AIPEOpetuksenSisalto sisalto = new AIPEOpetuksenSisalto();
            peruste.setSisalto(sisalto);
        }

        if (st != null) {
            suoritustavat.add(st);
        }

        peruste.setSuoritustavat(suoritustavat);
        for (Suoritustapa suoritustapa : suoritustavat) {
            suoritustapa.getPerusteet().add(peruste);
        }
        perusteet.save(peruste);
        lisaaTutkinnonMuodostuminen(peruste);
        return peruste;
    }

    private void initLukioOpetussuunitelmaRakenne(Peruste peruste, LukiokoulutuksenPerusteenSisalto sisalto) {
        sisalto.getOpetussuunnitelma().setSisalto(sisalto);
        peruste.setLukiokoulutuksenPerusteenSisalto(sisalto);
        LukioOpetussuunnitelmaRakenne rakenne = sisalto.getOpetussuunnitelma();
        rakenne.setNimi(TekstiPalanen.of(Kieli.FI, "Oppiaineet"));
        rakenne.setTunniste(PerusteenOsaTunniste.RAKENNE);
        rakenne.getViite().setPerusteenOsa(sisalto.getOpetussuunnitelma());
        rakenne.getViite().setVanhempi(sisalto.getSisalto());
        sisalto.getSisalto().getLapset().add(rakenne.getViite());
    }

    private EsiopetuksenPerusteenSisalto kloonaaEsiopetuksenSisalto(Peruste uusi, EsiopetuksenPerusteenSisalto vanha) {
        return vanha.kloonaa(uusi);
    }

    private PerusopetuksenPerusteenSisalto kloonaaPerusopetuksenSisalto(Peruste uusi, PerusopetuksenPerusteenSisalto vanha) {
        PerusopetuksenPerusteenSisalto sisalto = new PerusopetuksenPerusteenSisalto();
        sisalto.setSisalto(vanha.getSisalto().kloonaa());
        sisalto.setPeruste(uusi);

        Map<LaajaalainenOsaaminen, LaajaalainenOsaaminen> laajainenOsaaminenMapper = new HashMap<>();
        for (LaajaalainenOsaaminen laaja : vanha.getLaajaalaisetosaamiset()) {
            LaajaalainenOsaaminen uusilaaja = laaja.kloonaa();
            laajainenOsaaminenMapper.put(laaja, uusilaaja);
            sisalto.addLaajaalainenosaaminen(uusilaaja);
        }

        Map<VuosiluokkaKokonaisuus, VuosiluokkaKokonaisuus> vuosiluokkaKokonaisuusMapper = new HashMap<>();
        for (VuosiluokkaKokonaisuus vlk : vanha.getVuosiluokkakokonaisuudet()) {
            VuosiluokkaKokonaisuus uusiVlk = vuosiluokkaKokonaisuusRepository.save(vlk.kloonaa());
            vuosiluokkaKokonaisuusMapper.put(vlk, uusiVlk);
            sisalto.addVuosiluokkakokonaisuus(uusiVlk);
        }

        for (Oppiaine oa : vanha.getOppiaineetCopy()) {
            sisalto.addOppiaine(oppiaineRepository.save(oa.kloonaa(laajainenOsaaminenMapper, vuosiluokkaKokonaisuusMapper)));
        }
        return sisalto;
    }

    @Override
    @SuppressWarnings("ServiceMethodEntity")
    public Peruste luoPerusteRunkoToisestaPerusteesta(PerusteprojektiLuontiDto luontiDto, PerusteTyyppi tyyppi) {
        Peruste vanha = perusteet.getOne(luontiDto.getPerusteId());
        Peruste peruste = new Peruste();
        peruste.setTyyppi(tyyppi);
        peruste.setKuvaus(vanha.getKuvaus());
        peruste.setNimi(vanha.getNimi());
        peruste.setKoulutustyyppi(vanha.getKoulutustyyppi());

        // Osaamisalat
        if (vanha.getOsaamisalat() != null) {
            peruste.setOsaamisalat(new HashSet());
            peruste.getOsaamisalat().addAll(vanha.getOsaamisalat());
        }

        // Koulutukset
        Set<Koulutus> vanhatKoulutukset = vanha.getKoulutukset();
        Set<Koulutus> koulutukset = new HashSet<>();

        if (vanhatKoulutukset != null) {
            for (Koulutus vanhaKoulutus : vanhatKoulutukset) {
                koulutukset.add(vanhaKoulutus);
            }
            peruste.setKoulutukset(koulutukset);
        }

        if (KoulutusTyyppi.ESIOPETUS.toString().equalsIgnoreCase(vanha.getKoulutustyyppi())
                || KoulutusTyyppi.LISAOPETUS.toString().equalsIgnoreCase(vanha.getKoulutustyyppi())
                || KoulutusTyyppi.PERUSOPETUSVALMISTAVA.toString().equalsIgnoreCase(vanha.getKoulutustyyppi())
                || KoulutusTyyppi.VARHAISKASVATUS.toString().equalsIgnoreCase(vanha.getKoulutustyyppi())) {
            EsiopetuksenPerusteenSisalto uusiSisalto = kloonaaEsiopetuksenSisalto(peruste, vanha.getEsiopetuksenPerusteenSisalto());
            uusiSisalto.setPeruste(peruste);
            peruste.setEsiopetuksenPerusteenSisalto(uusiSisalto);
            peruste = perusteet.save(peruste);
        } else {
            Set<Suoritustapa> suoritustavat = vanha.getSuoritustavat();
            Set<Suoritustapa> uudetSuoritustavat = new HashSet<>();

            for (Suoritustapa st : suoritustavat) {
                uudetSuoritustavat.add(suoritustapaService.createFromOther(st.getId()));
            }

            for (Suoritustapa st : uudetSuoritustavat) {
                st.setLaajuusYksikko(luontiDto.getLaajuusYksikko());
            }

            peruste.setSuoritustavat(uudetSuoritustavat);
            for (Suoritustapa uusi : uudetSuoritustavat) {
                uusi.getPerusteet().add(peruste);
            }
            peruste = perusteet.save(peruste);

            peruste = perusteet.save(peruste);
            if (KoulutusTyyppi.PERUSOPETUS.toString().equalsIgnoreCase(vanha.getKoulutustyyppi())) {
                peruste.setPerusopetuksenPerusteenSisalto(kloonaaPerusopetuksenSisalto(peruste, vanha.getPerusopetuksenPerusteenSisalto()));
            } else {
                lisaaTutkinnonMuodostuminen(peruste);
            }
        }

        // Tutkintonimikkeet
        for (TutkintonimikeKoodi nimike : tutkintonimikeKoodiRepository.findByPerusteId(vanha.getId())) {
            TutkintonimikeKoodi uusi = new TutkintonimikeKoodi(nimike);
            uusi.setPeruste(peruste);
            tutkintonimikeKoodiRepository.save(uusi);
        }

        return peruste;
    }

    @Override
    @Transactional(readOnly = true)
    public LukiokoulutuksenYleisetTavoitteetDto getYleisetTavoitteet(long perusteId) {
        return getYeisettavoitteetLatestOrByVersion(perusteId, null);
    }

    @Override
    @Transactional(readOnly = true)
    public LukiokoulutuksenYleisetTavoitteetDto getYleisetTavoitteetByVersion(long perusteId, int revision) {
        return getYeisettavoitteetLatestOrByVersion(perusteId, revision);
    }

    private LukiokoulutuksenYleisetTavoitteetDto getYeisettavoitteetLatestOrByVersion(long perusteId, Integer revision) {
        Peruste peruste = perusteet.getOne(perusteId);
        OpetuksenYleisetTavoitteet opetuksenYleisetTavoitteet = peruste.getLukiokoulutuksenPerusteenSisalto().getOpetuksenYleisetTavoitteet();

        if (revision != null) {
            opetuksenYleisetTavoitteet = lukioYleisetTavoitteetRepository.findRevision(opetuksenYleisetTavoitteet.getId(), revision);
        }

        if (opetuksenYleisetTavoitteet != null) {
            return mapper.map(opetuksenYleisetTavoitteet, LukiokoulutuksenYleisetTavoitteetDto.class);
        } else {
            return new LukiokoulutuksenYleisetTavoitteetDto();
        }
    }

    @Override
    @Transactional
    public void tallennaYleisetTavoitteet(Long perusteId, LukiokoulutuksenYleisetTavoitteetDto lukiokoulutuksenYleisetTavoitteetDto) {
        Peruste peruste = perusteet.getOne(perusteId);
        LukiokoulutuksenPerusteenSisalto sisalto = peruste.getLukiokoulutuksenPerusteenSisalto();
        OpetuksenYleisetTavoitteet opetuksenYleisetTavoitteet = sisalto.getOpetuksenYleisetTavoitteet();
        if (opetuksenYleisetTavoitteet == null) {
            opetuksenYleisetTavoitteet = initLukioOpetuksenYleisetTavoitteet(sisalto);
        }
        mapper.map(lukiokoulutuksenYleisetTavoitteetDto, opetuksenYleisetTavoitteet);
        lukioYleisetTavoitteetRepository.setRevisioKommentti(lukiokoulutuksenYleisetTavoitteetDto.getMetadataOrEmpty().getKommentti());
    }

    private OpetuksenYleisetTavoitteet initLukioOpetuksenYleisetTavoitteet(LukiokoulutuksenPerusteenSisalto sisalto) {
        OpetuksenYleisetTavoitteet opetuksenYleisetTavoitteet;
        opetuksenYleisetTavoitteet = new OpetuksenYleisetTavoitteet();
        sisalto.setOpetuksenYleisetTavoitteet(opetuksenYleisetTavoitteet);
        opetuksenYleisetTavoitteet.setSisalto(sisalto);
        opetuksenYleisetTavoitteet.setNimi(TekstiPalanen.of(Kieli.FI, "Opetuksen yleiset tavoitteet"));
        opetuksenYleisetTavoitteet.setTunniste(PerusteenOsaTunniste.NORMAALI);
        opetuksenYleisetTavoitteet.getViite().setPerusteenOsa(opetuksenYleisetTavoitteet);
        opetuksenYleisetTavoitteet.getViite().setVanhempi(sisalto.getSisalto());
        sisalto.getSisalto().getLapset().add(opetuksenYleisetTavoitteet.getViite());
        return opetuksenYleisetTavoitteet;
    }

    @Transactional(readOnly = true)
    @Override
    public List<Revision> getYleisetTavoitteetVersiot(Long perusteId) {
        Peruste peruste = perusteet.getOne(perusteId);
        OpetuksenYleisetTavoitteet opetuksenYleisetTavoitteet = peruste.getLukiokoulutuksenPerusteenSisalto().getOpetuksenYleisetTavoitteet();
        if (opetuksenYleisetTavoitteet != null) {
            return lukioYleisetTavoitteetRepository.getRevisions(opetuksenYleisetTavoitteet.getId());
        } else {
            return new ArrayList<Revision>();
        }
    }

    private static class VisitorImpl implements AbstractRakenneOsaDto.Visitor {

        private final int maxDepth;

        public VisitorImpl(int maxDepth) {
            this.maxDepth = maxDepth;
        }

        @Override
        public void visit(final AbstractRakenneOsaDto dto, final int depth) {
            if (depth >= maxDepth) {
                throw new BusinessRuleViolationException("Tutkinnon rakennehierarkia ylittää maksimisyvyyden");
            }
        }
    }

    @Transactional(readOnly = false)
    @Override
    public LukiokoulutuksenYleisetTavoitteetDto palautaYleisetTavoitteet(long perusteId, int revisio) {
        LukiokoulutuksenYleisetTavoitteetDto yleistTavoitteet = getYleisetTavoitteetByVersion(perusteId, revisio);
        tallennaYleisetTavoitteet(perusteId, yleistTavoitteet);
        return yleistTavoitteet;
    }

    private boolean isTasoKoodi(String koodi) {
        return koodi != null
                && (koodi.startsWith("eqf_")
                || koodi.startsWith("nqf_")
                || koodi.startsWith("isced2011koulutusastetaso1_"));
    }

    @Override
    @IgnorePerusteUpdateCheck
    public KVLiiteJulkinenDto getJulkinenKVLiite(long perusteId) {
        Peruste peruste = perusteet.getOne(perusteId);
        PerusteDto perusteDto = mapper.map(peruste, PerusteDto.class);
        KVLiiteJulkinenDto kvliiteDto = mapper.map(peruste.getKvliite(), KVLiiteJulkinenDto.class);

        if (kvliiteDto == null) {
            kvliiteDto = new KVLiiteJulkinenDto();
        }

        kvliiteDto.setDiaarinumero(perusteDto.getDiaarinumero());
        kvliiteDto.setKoulutustyyppi(perusteDto.getKoulutustyyppi());
        kvliiteDto.setKuvaus(perusteDto.getKuvaus());
        kvliiteDto.setNimi(perusteDto.getNimi());
        kvliiteDto.setVoimassaoloAlkaa(perusteDto.getVoimassaoloAlkaa());

        Map<Suoritustapakoodi, LokalisoituTekstiDto> muodostumistenKuvaukset = new HashMap<>();

        for (Suoritustapa suoritustapa : peruste.getSuoritustavat()) {
            if (suoritustapa.getRakenne() != null) {
                TekstiPalanen kuvaus = suoritustapa.getRakenne().getKuvaus();
                Suoritustapakoodi koodi = suoritustapa.getSuoritustapakoodi();
                muodostumistenKuvaukset.put(koodi, mapper.map(kuvaus, LokalisoituTekstiDto.class));
            }
        }

        KVLiite kvliite = peruste.getKvliite();
        if (kvliite != null) {
            KVLiite pohjaLiite = kvliite.getPohja();
            KVLiiteDto pohjaLiiteDto = null;

            if (pohjaLiite == null) {
                pohjaLiite = kvliite;
            }
            else {
                kvliiteDto.setPeriytynyt(true);
            }

            pohjaLiiteDto = mapper.map(pohjaLiite, KVLiiteDto.class);

            if (kvliiteDto.getSuorittaneenOsaaminen() == null) {
                kvliiteDto.setSuorittaneenOsaaminen(pohjaLiiteDto.getSuorittaneenOsaaminen());
            }
            if (kvliiteDto.getTyotehtavatJoissaVoiToimia() == null) {
                kvliiteDto.setTyotehtavatJoissaVoiToimia(pohjaLiiteDto.getTyotehtavatJoissaVoiToimia());
            }

            kvliiteDto.setTutkintotodistuksenAntaja(pohjaLiiteDto.getTutkintotodistuksenAntaja());
            kvliiteDto.setArvosanaAsteikko(pohjaLiiteDto.getArvosanaAsteikko());
            kvliiteDto.setJatkoopintoKelpoisuus(pohjaLiiteDto.getJatkoopintoKelpoisuus());
            kvliiteDto.setKansainvalisetSopimukset(pohjaLiiteDto.getKansainvalisetSopimukset());
            kvliiteDto.setSaadosPerusta(pohjaLiiteDto.getSaadosPerusta());
            kvliiteDto.setPohjakoulutusvaatimukset(pohjaLiiteDto.getPohjakoulutusvaatimukset());
            kvliiteDto.setLisatietoja(pohjaLiiteDto.getLisatietoja());
            kvliiteDto.setTutkinnonVirallinenAsema(pohjaLiiteDto.getTutkinnonVirallinenAsema());
        }

        Set<String> tasokoodiFilter = new HashSet<>();
        kvliiteDto.setTasot(peruste.getKoulutukset().stream()
                .map(Koulutus::getKoulutuskoodiUri)
                .map(koulutusKoodiUri -> koodistoService.getLatest(koulutusKoodiUri))
                .map(latest -> koodistoService.getAllByVersio(latest.getKoodiUri(), latest.getVersio()))
                .map(all -> Arrays.stream(all.getIncludesCodeElements()))
                .flatMap(x -> x)
                .filter(el -> isTasoKoodi(el.getCodeElementUri()))
                .filter(el -> tasokoodiFilter.add(el.getCodeElementUri()))
                .map(el -> {
                    KVLiiteTasoDto result = new KVLiiteTasoDto();
                    result.setCodeUri(el.getCodeElementUri());
                    result.setCodeValue(el.getCodeElementValue());

                    if (el.getCodeElementUri().startsWith("nqf_") || el.getCodeElementUri().startsWith("eqf_")) {
                        result.setNimi(new LokalisoituTekstiDto(Arrays.stream(el.getParentMetadata())
                            .collect(Collectors.toMap(
                                    lokaali -> lokaali.getKieli().toLowerCase(),
                                    lokaali -> lokaali.getKuvaus() + " " + el.getCodeElementValue()))));
                    }
                    else if (el.getCodeElementUri().startsWith("isced2011")) { // ISCED
                        result.setNimi(new LokalisoituTekstiDto(Arrays.stream(el.getParentMetadata())
                            .collect(Collectors.toMap(
                                    lokaali -> lokaali.getKieli().toLowerCase(),
                                    lokaali -> "ISCED " + el.getCodeElementValue()))));
                    }
                    else {
                        result.setNimi(null);
                    }
                    return result;
                })
                .collect(Collectors.toList()));
        return kvliiteDto;
    }

    @Override
    public TutkinnonOsaViiteDto getTutkinnonOsaViiteByKoodiUri(Long perusteId, Suoritustapakoodi suoritustapakoodi, String koodiUri) {
        final Suoritustapa suoritustapa = getSuoritustapaEntity(perusteId, suoritustapakoodi);
        TutkinnonOsaViite viite = tutkinnonOsaViiteRepository.findOneByKoodiUri(koodiUri, suoritustapakoodi);

        if (viite == null || !viite.getSuoritustapa().equals(suoritustapa)) {
            throw new BusinessRuleViolationException("Virheellinen viiteId");
        }

        TutkinnonOsaViiteDto viiteDto = mapper.map(viite, TutkinnonOsaViiteDto.class);
        TutkinnonOsaDto tutkinnonOsaDto = mapper.map(viite.getTutkinnonOsa(), TutkinnonOsaDto.class);
        viiteDto.setTutkinnonOsaDto(tutkinnonOsaDto);

        return viiteDto;
    }

    private static final Logger LOG = LoggerFactory.getLogger(PerusteServiceImpl.class);
}
