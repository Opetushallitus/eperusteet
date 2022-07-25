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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.base.Throwables;
import fi.vm.sade.eperusteet.domain.AIPEOpetuksenSisalto;
import fi.vm.sade.eperusteet.domain.Diaarinumero;
import fi.vm.sade.eperusteet.domain.JulkaistuPeruste;
import fi.vm.sade.eperusteet.domain.KVLiite;
import fi.vm.sade.eperusteet.domain.Kieli;
import fi.vm.sade.eperusteet.domain.Koodi;
import fi.vm.sade.eperusteet.domain.Koulutus;
import fi.vm.sade.eperusteet.domain.KoulutusTyyppi;
import fi.vm.sade.eperusteet.domain.KoulutustyyppiToteutus;
import fi.vm.sade.eperusteet.domain.LaajuusYksikko;
import fi.vm.sade.eperusteet.domain.Maarayskirje;
import fi.vm.sade.eperusteet.domain.MuokkausTapahtuma;
import fi.vm.sade.eperusteet.domain.Muutosmaarays;
import fi.vm.sade.eperusteet.domain.Peruste;
import fi.vm.sade.eperusteet.domain.PerusteTila;
import fi.vm.sade.eperusteet.domain.PerusteTyyppi;
import fi.vm.sade.eperusteet.domain.PerusteVersion;
import fi.vm.sade.eperusteet.domain.PerusteenOsa;
import fi.vm.sade.eperusteet.domain.PerusteenOsaTunniste;
import fi.vm.sade.eperusteet.domain.PerusteenOsaViite;
import fi.vm.sade.eperusteet.domain.ProjektiTila;
import fi.vm.sade.eperusteet.domain.Suoritustapa;
import fi.vm.sade.eperusteet.domain.Suoritustapakoodi;
import fi.vm.sade.eperusteet.domain.TekstiKappale;
import fi.vm.sade.eperusteet.domain.TekstiPalanen;
import fi.vm.sade.eperusteet.domain.TutkintonimikeKoodi;
import fi.vm.sade.eperusteet.domain.liite.Liite;
import fi.vm.sade.eperusteet.domain.lops2019.Lops2019Sisalto;
import fi.vm.sade.eperusteet.domain.lops2019.laajaalainenosaaminen.Lops2019LaajaAlainenOsaaminenKokonaisuus;
import fi.vm.sade.eperusteet.domain.tutkinnonosa.TutkinnonOsa;
import fi.vm.sade.eperusteet.domain.tutkinnonrakenne.AbstractRakenneOsa;
import fi.vm.sade.eperusteet.domain.tutkinnonrakenne.RakenneModuuli;
import fi.vm.sade.eperusteet.domain.tutkinnonrakenne.RakenneModuuliRooli;
import fi.vm.sade.eperusteet.domain.tutkinnonrakenne.TutkinnonOsaViite;
import fi.vm.sade.eperusteet.domain.tuva.TutkintoonvalmentavaSisalto;
import fi.vm.sade.eperusteet.domain.vst.VapaasivistystyoSisalto;
import fi.vm.sade.eperusteet.domain.yl.EsiopetuksenPerusteenSisalto;
import fi.vm.sade.eperusteet.domain.yl.LaajaalainenOsaaminen;
import fi.vm.sade.eperusteet.domain.yl.Oppiaine;
import fi.vm.sade.eperusteet.domain.yl.PerusopetuksenPerusteenSisalto;
import fi.vm.sade.eperusteet.domain.yl.TpoOpetuksenSisalto;
import fi.vm.sade.eperusteet.domain.yl.VuosiluokkaKokonaisuus;
import fi.vm.sade.eperusteet.domain.yl.lukio.LukioOpetussuunnitelmaRakenne;
import fi.vm.sade.eperusteet.domain.yl.lukio.LukiokoulutuksenPerusteenSisalto;
import fi.vm.sade.eperusteet.domain.yl.lukio.OpetuksenYleisetTavoitteet;
import fi.vm.sade.eperusteet.dto.KoulutustyyppiLukumaara;
import fi.vm.sade.eperusteet.dto.LukkoDto;
import fi.vm.sade.eperusteet.dto.PerusteTekstikappaleillaDto;
import fi.vm.sade.eperusteet.dto.Reference;
import fi.vm.sade.eperusteet.dto.koodisto.KoodistoKoodiDto;
import fi.vm.sade.eperusteet.dto.liite.LiiteBaseDto;
import fi.vm.sade.eperusteet.dto.liite.LiiteDto;
import fi.vm.sade.eperusteet.dto.lops2019.Lops2019OppiaineKaikkiDto;
import fi.vm.sade.eperusteet.dto.peruste.KVLiiteDto;
import fi.vm.sade.eperusteet.dto.peruste.KVLiiteJulkinenDto;
import fi.vm.sade.eperusteet.dto.peruste.KVLiiteTasoDto;
import fi.vm.sade.eperusteet.dto.peruste.KoosteenOsaamisalaDto;
import fi.vm.sade.eperusteet.dto.peruste.MaarayskirjeDto;
import fi.vm.sade.eperusteet.dto.peruste.NavigationNodeDto;
import fi.vm.sade.eperusteet.dto.peruste.NavigationType;
import fi.vm.sade.eperusteet.dto.peruste.PerusteBaseDto;
import fi.vm.sade.eperusteet.dto.peruste.PerusteDto;
import fi.vm.sade.eperusteet.dto.peruste.PerusteHakuDto;
import fi.vm.sade.eperusteet.dto.peruste.PerusteHakuInternalDto;
import fi.vm.sade.eperusteet.dto.peruste.PerusteInfoDto;
import fi.vm.sade.eperusteet.dto.peruste.PerusteKaikkiDto;
import fi.vm.sade.eperusteet.dto.peruste.PerusteKevytDto;
import fi.vm.sade.eperusteet.dto.peruste.PerusteKoosteDto;
import fi.vm.sade.eperusteet.dto.peruste.PerusteQuery;
import fi.vm.sade.eperusteet.dto.peruste.PerusteVersionDto;
import fi.vm.sade.eperusteet.dto.peruste.PerusteenOsaViiteDto;
import fi.vm.sade.eperusteet.dto.peruste.SuoritustapaDto;
import fi.vm.sade.eperusteet.dto.peruste.TekstiKappaleDto;
import fi.vm.sade.eperusteet.dto.peruste.TermiDto;
import fi.vm.sade.eperusteet.dto.peruste.TutkintonimikeKoodiDto;
import fi.vm.sade.eperusteet.dto.perusteprojekti.PerusteprojektiImportDto;
import fi.vm.sade.eperusteet.dto.perusteprojekti.PerusteprojektiKevytDto;
import fi.vm.sade.eperusteet.dto.perusteprojekti.PerusteprojektiLuontiDto;
import fi.vm.sade.eperusteet.dto.tutkinnonosa.TutkinnonOsaDto;
import fi.vm.sade.eperusteet.dto.tutkinnonosa.TutkinnonOsaKaikkiDto;
import fi.vm.sade.eperusteet.dto.tutkinnonosa.TutkinnonOsaTilaDto;
import fi.vm.sade.eperusteet.dto.tutkinnonrakenne.AbstractRakenneOsaDto;
import fi.vm.sade.eperusteet.dto.tutkinnonrakenne.KoodiDto;
import fi.vm.sade.eperusteet.dto.tutkinnonrakenne.RakenneModuuliDto;
import fi.vm.sade.eperusteet.dto.tutkinnonrakenne.RakenneOsaDto;
import fi.vm.sade.eperusteet.dto.tutkinnonrakenne.TutkinnonOsaViiteDto;
import fi.vm.sade.eperusteet.dto.util.LokalisoituTekstiDto;
import fi.vm.sade.eperusteet.dto.util.PageDto;
import fi.vm.sade.eperusteet.dto.util.TutkinnonOsaViiteUpdateDto;
import fi.vm.sade.eperusteet.dto.util.UpdateDto;
import fi.vm.sade.eperusteet.dto.yl.TPOOpetuksenSisaltoDto;
import fi.vm.sade.eperusteet.dto.yl.lukio.LukiokoulutuksenYleisetTavoitteetDto;
import fi.vm.sade.eperusteet.repository.JulkaisutRepository;
import fi.vm.sade.eperusteet.repository.KVLiiteRepository;
import fi.vm.sade.eperusteet.repository.KoodiRepository;
import fi.vm.sade.eperusteet.repository.KoulutusRepository;
import fi.vm.sade.eperusteet.repository.LukioYleisetTavoitteetRepository;
import fi.vm.sade.eperusteet.repository.OppiaineRepository;
import fi.vm.sade.eperusteet.repository.PerusteRepository;
import fi.vm.sade.eperusteet.repository.PerusteenOsaRepository;
import fi.vm.sade.eperusteet.repository.PerusteenOsaViiteRepository;
import fi.vm.sade.eperusteet.repository.PerusteprojektiRepository;
import fi.vm.sade.eperusteet.repository.RakenneRepository;
import fi.vm.sade.eperusteet.repository.SuoritustapaRepository;
import fi.vm.sade.eperusteet.repository.TekstiPalanenRepository;
import fi.vm.sade.eperusteet.repository.TekstikappaleRepository;
import fi.vm.sade.eperusteet.repository.TutkinnonOsaViiteRepository;
import fi.vm.sade.eperusteet.repository.TutkintonimikeKoodiRepository;
import fi.vm.sade.eperusteet.repository.VuosiluokkaKokonaisuusRepository;
import fi.vm.sade.eperusteet.repository.liite.LiiteRepository;
import fi.vm.sade.eperusteet.repository.version.Revision;
import fi.vm.sade.eperusteet.resource.config.InitJacksonConverter;
import fi.vm.sade.eperusteet.service.KoodistoClient;
import fi.vm.sade.eperusteet.service.LocalizedMessagesService;
import fi.vm.sade.eperusteet.service.NavigationBuilder;
import fi.vm.sade.eperusteet.service.NavigationBuilderPublic;
import fi.vm.sade.eperusteet.service.PerusteDispatcher;
import fi.vm.sade.eperusteet.service.PerusteImport;
import fi.vm.sade.eperusteet.service.PerusteService;
import fi.vm.sade.eperusteet.service.PerusteenMuokkaustietoService;
import fi.vm.sade.eperusteet.service.PerusteenOsaService;
import fi.vm.sade.eperusteet.service.PerusteenOsaViiteService;
import fi.vm.sade.eperusteet.service.TermistoService;
import fi.vm.sade.eperusteet.service.TutkinnonOsaViiteService;
import fi.vm.sade.eperusteet.service.VapaasivistystyoSisaltoService;
import fi.vm.sade.eperusteet.service.event.PerusteUpdatedEvent;
import fi.vm.sade.eperusteet.service.event.aop.IgnorePerusteUpdateCheck;
import fi.vm.sade.eperusteet.service.exception.BusinessRuleViolationException;
import fi.vm.sade.eperusteet.service.exception.NotExistsException;
import fi.vm.sade.eperusteet.service.internal.LockManager;
import fi.vm.sade.eperusteet.service.internal.SuoritustapaService;
import fi.vm.sade.eperusteet.service.mapping.Dto;
import fi.vm.sade.eperusteet.service.mapping.DtoMapper;
import fi.vm.sade.eperusteet.service.mapping.Koodisto;
import fi.vm.sade.eperusteet.service.security.PermissionManager;
import fi.vm.sade.eperusteet.service.yl.AihekokonaisuudetService;
import fi.vm.sade.eperusteet.service.yl.Lops2019Service;
import fi.vm.sade.eperusteet.service.yl.LukiokoulutuksenPerusteenSisaltoService;

import java.io.File;
import java.io.IOException;
import java.sql.Blob;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.Stack;
import java.util.UUID;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;
import javax.persistence.EntityManager;
import javax.persistence.EntityNotFoundException;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.Validator;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.tika.mime.MimeTypeException;
import org.apache.tika.mime.MimeTypes;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.ApplicationListener;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.method.P;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import static fi.vm.sade.eperusteet.domain.KoulutusTyyppi.AIKUISTENLUKIOKOULUTUS;
import static fi.vm.sade.eperusteet.domain.KoulutusTyyppi.LUKIOKOULUTUS;
import static fi.vm.sade.eperusteet.domain.KoulutusTyyppi.LUKIOVALMISTAVAKOULUTUS;
import static fi.vm.sade.eperusteet.domain.KoulutusTyyppi.MAAHANMUUTTAJIENKOTOUTUMISKOULUTUS;
import static fi.vm.sade.eperusteet.domain.KoulutusTyyppi.TUTKINTOONVALMENTAVA;

/**
 *
 * @author jhyoty
 */

@Slf4j
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
    private PerusteDispatcher dispatcher;

    @Autowired
    private PerusteRepository perusteRepository;

    @Autowired
    private PerusteprojektiRepository perusteprojektiRepository;

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
    private TekstikappaleRepository tekstikappaleRepository;

    @Autowired
    private PerusteenOsaViiteRepository perusteenOsaViiteRepository;

    @Autowired
    PerusteService self;

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
    private Lops2019Service lops2019Service;

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

    @Autowired
    private LocalizedMessagesService messages;

    @Autowired
    private LiiteRepository liitteet;

    @Autowired
    private EntityManager em;

    @Autowired
    private LiiteRepository liiteRepository;

    @Autowired
    private PerusteenMuokkaustietoService muokkausTietoService;

    @Autowired
    private TermistoService termistoService;

    @Autowired
    private JulkaisutRepository julkaisutRepository;

    @Autowired
    private PermissionManager permissionManager;

    @Autowired
    private VapaasivistystyoSisaltoService vapaasivistystyoSisaltoService;

    @Autowired
    private OpasSisaltoService opasSisaltoService;

    @Autowired
    private KoodistoClient koodistoClient;

    private final ObjectMapper ieMapper = InitJacksonConverter.createImportExportMapper();
    private final ObjectMapper objectMapper = InitJacksonConverter.createMapper();

    @Override
    public List<PerusteDto> getUusimmat(Set<Kieli> kielet) {
        return mapper.mapAsList(perusteRepository.findAllUusimmat(kielet, new PageRequest(0, 10)), PerusteDto.class);
    }

    @Override
    public List<PerusteKoosteDto> getKooste() {
        return perusteRepository.findAllPerusteet().stream()
                .filter(peruste -> peruste.getTila() == PerusteTila.VALMIS)
                .filter(peruste -> peruste.getKoulutustyyppi() != null && KoulutusTyyppi.of(peruste.getKoulutustyyppi()).isAmmatillinen())
                .map(peruste -> {
                    PerusteKoosteDto result = mapper.map(peruste, PerusteKoosteDto.class);
                    Set<Koodi> tutkinnonOsat = new LinkedHashSet<>();
                    for (Suoritustapa st : peruste.getSuoritustavat()) {
                        for (TutkinnonOsaViite t : st.getTutkinnonOsat()) {
                            tutkinnonOsat.add(t.getTutkinnonOsa().getKoodi());
                        }
                    }

                    result.setTutkinnonOsat(mapper.mapAsList(tutkinnonOsat, KoodiDto.class));

                    if (peruste.getOsaamisalat() != null && !peruste.getOsaamisalat().isEmpty()) {
                        List<TutkintonimikeKoodiDto> tutkintonimikeKoodit = getTutkintonimikeKoodit(peruste.getId());
                        result.setOsaamisalat(peruste.getOsaamisalat().stream()
                                .map(osaamisala -> {
                                    KoosteenOsaamisalaDto oa = new KoosteenOsaamisalaDto();
                                    oa.setKoodi(mapper.map(osaamisala, KoodiDto.class));
                                    oa.setTutkinnonOsat(tutkintonimikeKoodit.stream()
                                            .filter(tk -> Objects.equals(tk.getOsaamisalaUri(), osaamisala.getUri()))
                                            .filter(tk -> Objects.nonNull(tk.getTutkinnonOsaUri()))
                                            .map(tk -> mapper.map(new Koodi("tutkinnonosat", tk.getTutkinnonOsaUri()), KoodiDto.class))
                                            .collect(Collectors.toList()));
                                    return oa;
                                })
                                .collect(Collectors.toList()));
                    }
                    return result;
                })
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PerusteHakuDto> getAll(PageRequest page, String kieli) {
        return findByImpl(page, new PerusteQuery());
    }

    @Override
    @Transactional(readOnly = true)
    public List<PerusteInfoDto> getAllInfo() {
        return mapper.mapAsList(perusteRepository.findAll(), PerusteInfoDto.class);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PerusteInfoDto> getAllPerusopetusInfo() {
        List<Peruste> res = new ArrayList<>();
        List<Peruste> perusopetus = perusteRepository.findAllByKoulutustyyppi(KoulutusTyyppi.PERUSOPETUS.toString());
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

    @Transactional(readOnly = true)
    private Page<PerusteHakuDto> findByImpl(PageRequest page, PerusteQuery pquery) {
        return findByImpl(page, pquery, PerusteHakuDto.class);
    }

    @Transactional(readOnly = true)
    private <T extends PerusteHakuDto> Page<T> findByImpl(PageRequest page, PerusteQuery pquery, Class<T> type) {
        Set<Long> koodistostaLoydetyt = new HashSet<>();

        // Ladataan koodistosta osaamisala ja tutkintonimikehakua vastaavat koodit
        if (StringUtils.isNotEmpty(pquery.getNimi()) && pquery.isOsaamisalat()) {
            List<KoodistoKoodiDto> osaamisalakoodit = koodistoService.filterBy("osaamisala", pquery.getNimi());
            Set<Long> osaamisalat = getPerusteetByUris(
                        osaamisalakoodit.stream().map(KoodistoKoodiDto::getKoodiUri),
                        perusteRepository::findAllByOsaamisala)
                    .map(Peruste::getId)
                    .collect(Collectors.toSet());
            koodistostaLoydetyt.addAll(osaamisalat);
        }

        // Haetaan perusteiden id:t mihin on liitetty osaamisalat tai tutkintonimikkeet
        if (StringUtils.isNotEmpty(pquery.getNimi()) && pquery.isTutkintonimikkeet()) {
            List<KoodistoKoodiDto> tutkintonimikekoodit = koodistoService.filterBy("tutkintonimikkeet", pquery.getNimi());
            Set<Long> tutkintonimikkeelliset = getPerusteetByUris(
                        tutkintonimikekoodit.stream().map(KoodistoKoodiDto::getKoodiUri),
                        tutkintonimikeKoodiRepository::findAllByTutkintonimikeUri)
                    .map(Peruste::getId)
                    .collect(Collectors.toSet());
            koodistostaLoydetyt.addAll(tutkintonimikkeelliset);
        }

        // Lisätään mahdolliset perusteet hakujoukkoon
        Page<Peruste> result = perusteRepository.findBy(page, pquery, koodistostaLoydetyt)
                .map(p -> { // Collect tutkintonimikkeet for easy display in list views
                    p.setTutkintonimikeKoodit(tutkintonimikeKoodiRepository.findByPerusteId(p.getId()).stream()
                        .map(tutkintonimikeKoodi -> new Koodi(tutkintonimikeKoodi.getTutkintonimikeUri(), "tutkintonimikkeet"))
                        .collect(Collectors.toSet()));
                    return p;
                });
        PageDto<Peruste, T> resultDto = new PageDto<>(result, type, page, mapper);

        for (T haettu : resultDto) {
            // Lisätään korvaavat ja korvattavat perusteet
            if (haettu.getKorvattavatDiaarinumerot() == null) {
                continue;
            }

            Set<Diaarinumero> korvattavatDiaarinumerot = haettu.getKorvattavatDiaarinumerot().stream()
                    .map(Diaarinumero::new)
                    .collect(Collectors.toSet());

            if (!korvattavatDiaarinumerot.isEmpty()) {
                Set<Peruste> korvattavat = perusteRepository.findAllByDiaarinumerot(korvattavatDiaarinumerot);
                List<PerusteInfoDto> korvattavatDto = mapper.mapAsList(korvattavat, PerusteInfoDto.class);
                haettu.setKorvattavatPerusteet(korvattavatDto);
            }

            Set<Peruste> korvaajat = perusteRepository.findAllKorvaavatByDiaarinumero(haettu.getDiaarinumero());
            List<PerusteInfoDto> korvaajatDto = mapper.mapAsList(korvaajat, PerusteInfoDto.class);
            haettu.setKorvaavatPerusteet(korvaajatDto);

            taytaLaajuus(haettu);
        }

        return resultDto;
    }

    private void taytaLaajuus(PerusteDto peruste) {
        if (peruste.getKoulutustyyppi() != null && KoulutusTyyppi.of(peruste.getKoulutustyyppi()).isAmmatillinen()) {
            peruste.getSuoritustavat().forEach(suoritustapa -> {
                try {
                    if (peruste.getLaajuus() == null) {
                        peruste.setLaajuus(getTutkinnonLaajuus(peruste.getId(), suoritustapa.getSuoritustapakoodi()));
                    }
                } catch (Exception ex) {
                }
            });
        }
    }

    // Sisäisen haku (palauttaa myös keskeneräisiä)
    @Override
    @Transactional(readOnly = true)
    public Page<PerusteHakuInternalDto> findByInternal(PageRequest page, PerusteQuery pquery) {
        // Voidaan käyttää vain esikatseltaviin perusteprojektien perusteisiin
//        pquery.setEsikatseltavissa(true);
        return findByImpl(page, pquery, PerusteHakuInternalDto.class);
    }

    @Override
    @Cacheable("pohjaperusteet")
    public List<PerusteKevytDto> getPohjaperusteet(PerusteTyyppi tyyppi) {
        return mapper.mapAsList(perusteRepository.findValmiitByTyyppi(tyyppi, false), PerusteKevytDto.class);
    }

    @Override
    public List<PerusteKevytDto> getJulkaistutPerusteet() {
        return mapper.mapAsList(perusteRepository.findJulkaistutPerusteet(), PerusteKevytDto.class);
    }

    @Override
    @Cacheable("julkaistutkoulutustyypit")
    public List<KoulutustyyppiLukumaara> getJulkaistutKoulutustyyppiLukumaarat(Kieli kieli) {
        Long currentMillis = DateTime.now().getMillis();
        return julkaisutRepository.findJulkaistutKoulutustyyppiLukumaaratByKieli(
                kieli.toString(),
                currentMillis).stream()
                .filter(koulutustyyppi -> koulutustyyppi != null)
                .collect(Collectors.toList());
    }

    @Override
    public List<KoulutusTyyppi> getJulkaistutKoulutustyyppit(Kieli kieli) {
        return perusteRepository.findJulkaistutDistinctKoulutustyyppiByKieli(kieli).stream()
                .filter(koulutustyyppi -> koulutustyyppi != null)
                .map(KoulutusTyyppi::of)
                .collect(Collectors.toList());
    }

    @Override
    public List<PerusteDto> getOpasKiinnitettyKoodi(String koodiUri) {
        return mapper.mapAsList(perusteRepository.findAllByJulkaisutOppaatKiinnitettyKoodilla(koodiUri), PerusteDto.class);
    }

    // Julkinen haku
    @Override
    @Transactional(readOnly = true)
    public Page<PerusteHakuDto> findJulkinenBy(PageRequest page, PerusteQuery pquery) {
        pquery.setTila(PerusteTila.VALMIS.toString());
        pquery.setJulkaistu(true);
        if (pquery.getPerusteTyyppi() == null) {
            pquery.setPerusteTyyppi(PerusteTyyppi.NORMAALI.toString());
        }
        return findByImpl(page, pquery);
    }

    @Override
    public Page<PerusteBaseDto> getJulkaisuAikatauluPerusteet(Integer sivu, Integer sivukoko, List<String> koulutusTyyppit) {
        return perusteRepository.findAllJulkaisuaikataulullisetPerusteet(koulutusTyyppit, new PageRequest(sivu, Math.min(sivukoko, 20))).map(peruste -> mapper.map(peruste, PerusteBaseDto.class));
    }

    @Override
    public List<KoulutustyyppiLukumaara> getVoimassaolevatJulkaistutPerusteLukumaarat(List<String> koulutusTyyppit) {
        return perusteRepository.findVoimassaolevatJulkaistutPerusteLukumaarat(koulutusTyyppit);
    }

    // Julkinen haku kevyemmällä paluuarvolla
    @Override
    @Transactional(readOnly = true)
    public Page<PerusteInfoDto> findByInfo(PageRequest page, PerusteQuery pquery) {
        pquery.setTila(PerusteTila.VALMIS.toString());

        Page<Peruste> result = perusteRepository.findBy(page, pquery);
        return new PageDto<>(result, PerusteInfoDto.class, page, mapper);
    }

    @Override
    @Transactional(readOnly = true)
    public PerusteInfoDto getMeta(final Long id) {
        Peruste p = perusteRepository.findOne(id);
        if (p.getTila() != PerusteTila.VALMIS) {
            throw new BusinessRuleViolationException("vain-julkaistuille-perusteille");
        }
        return mapper.map(p, PerusteInfoDto.class);
    }

    // TODO: Use native "with recursive" if too slow
    @Transactional(readOnly = true)
    private void asetaOsaamisalakuvaukset(PerusteenOsaViite viite, List<TekstiKappaleDto> kuvaukset) {
        if (viite == null) {
            return;
        }

        TekstiKappale tk = (TekstiKappale) viite.getPerusteenOsa();
        if (tk != null && tk.getOsaamisala() != null) {
            kuvaukset.add(mapper.map(tk.getOsaamisala(), TekstiKappaleDto.class));
        }
        for (PerusteenOsaViite alikappale : viite.getLapset()) {
            asetaOsaamisalakuvaukset(alikappale, kuvaukset);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public PerusteDto get(final Long id) {
        Peruste p = perusteRepository.findOne(id);
        PerusteDto dto = mapper.map(p, PerusteDto.class);
        if (dto != null) {
            Revision latestRevision = perusteRepository.getLatestRevisionId(id);
            if (latestRevision != null) {
                dto.setRevision(latestRevision.getNumero());
                if (dto.getSuoritustavat() != null && !dto.getSuoritustavat().isEmpty()) {
                    dto.setTutkintonimikkeet(getTutkintonimikeKoodit(id));
                }
            }

            taytaLaajuus(dto);
        }

        return dto;
    }

    @Override
    @Transactional(readOnly = true)
    public ProjektiTila getPerusteProjektiTila(@P("perusteId") final Long id) {
        Peruste p = perusteRepository.findOne(id);
        PerusteprojektiKevytDto projektiKevytDto = mapper.map(p.getPerusteprojekti(), PerusteprojektiKevytDto.class);
        return projektiKevytDto.getTila();
    }

    @Override
    @Transactional(readOnly = true)
    public PerusteKaikkiDto getAmosaaYhteinenPohja() {
        List<Peruste> loydetyt = perusteRepository.findAllAmosaaYhteisetPohjat();

        if (loydetyt.size() == 1) {
            return getJulkaistuSisalto(loydetyt.get(0).getId());
        } else {
            Optional<Peruste> op = loydetyt.stream()
                    .filter((p) -> p.getVoimassaoloAlkaa() != null && p.getVoimassaoloAlkaa().before(new Date()))
                    .reduce((current, next) -> next.getVoimassaoloAlkaa().after(current.getVoimassaoloAlkaa()) ? next : current);

            if (op.isPresent()) {
                return getJulkaistuSisalto(op.get().getId());
            }
        }

        return null;
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable("amosaaperusteet")
    public List<PerusteHakuDto> getAmosaaOpsit() {
        Set<Peruste> perusteet = julkaisutRepository.findAmosaaJulkaisut();
        perusteet.addAll(perusteRepository.findAllAmosaa());
        List<PerusteHakuDto> result = perusteet.stream()
                .map(p -> mapper.map(p, PerusteHakuDto.class))
                .collect(Collectors.toList());
        return result;
    }

    @Override
    @Transactional(readOnly = true)
    public PerusteInfoDto getByDiaari(Diaarinumero diaarinumero) {
        List<Peruste> kaikki = perusteRepository.findAllByDiaarinumero(diaarinumero);
        Set<Peruste> loydetyt = kaikki.stream()
                .filter(p -> !PerusteTila.POISTETTU.equals(p.getTila()))
                .filter(p -> ProjektiTila.JULKAISTU.equals(p.getPerusteprojekti().getTila())
                  || julkaisutRepository.countByPeruste(p) > 0)
                .collect(Collectors.toSet());

        Peruste peruste = null;

        if (loydetyt.size() == 1) {
            peruste = loydetyt.iterator().next();
        } else {
            Optional<Peruste> op = loydetyt.stream()
                    .filter((p) -> p.getVoimassaoloAlkaa() != null)
                    .filter((p) -> p.getVoimassaoloAlkaa().before(new Date()))
                    .reduce((a, b) -> a.getVoimassaoloAlkaa().after(b.getVoimassaoloAlkaa()) ? a : b);

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
    @IgnorePerusteUpdateCheck
    @Transactional(readOnly = true)
    public Map<Suoritustapakoodi, Map<String, List<TekstiKappaleDto>>> getOsaamisalaKuvaukset(final Long perusteId) {
        Peruste peruste = perusteRepository.findOne(perusteId);

        KoulutusTyyppi tyyppi = KoulutusTyyppi.of(peruste.getKoulutustyyppi());

        if (!tyyppi.isAmmatillinen() && !tyyppi.isValmaTelma()) {
            return new HashMap<>();
        }

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
        return perusteRepository.getGlobalPerusteVersion(id);
    }

    @Override
    @Transactional(readOnly = true)
    public Revision getLastModifiedRevision(final Long id) {
        PerusteTila tila = perusteRepository.getTila(id);
        if (tila == null) {
            return null;
        }
        if (tila == PerusteTila.LUONNOS) {
            //luonnos-tilassa olevan perusteen viimeisimmän muokkauksen määrittäminen on epäluotettavaa.
            return Revision.DRAFT;
        }
        return perusteRepository.getLatestRevisionId(id);
    }

    @Override
    @Transactional(readOnly = true)
    public PerusteKaikkiDto getJulkaistuSisalto(final Long id, boolean esikatselu) {
        return getJulkaistuSisalto(id, null, esikatselu);
    }

    @Override
    @Transactional(readOnly = true)
    public PerusteKaikkiDto getJulkaistuSisalto(final Long id) {
        return getJulkaistuSisalto(id, null, false);
    }

    @Override
    @Transactional(readOnly = true)
    public PerusteKaikkiDto getJulkaistuSisalto(final Long id, Integer perusteRev, boolean useCurrentData) {
        Peruste peruste;
        if (perusteRev != null) {
            peruste = perusteRepository.findRevision(id, perusteRev);
        } else {
            peruste = perusteRepository.getOne(id);
        }

        if (peruste == null) {
            return null;
        }

        JulkaistuPeruste julkaisu = julkaisutRepository.findFirstByPerusteOrderByRevisionDesc(peruste);
        if (!useCurrentData && julkaisu != null) {
            ObjectNode data = julkaisu.getData().getData();
            try {
                return objectMapper.treeToValue(data, PerusteKaikkiDto.class);
            } catch (JsonProcessingException e) {
                log.error(Throwables.getStackTraceAsString(e));
                throw new BusinessRuleViolationException("perusteen-haku-epaonnistui");
            }
        } else if (!peruste.getPerusteprojekti().isEsikatseltavissa() && !Objects.equals(peruste.getPerusteprojekti().getTila(), ProjektiTila.JULKAISTU)) {
            throw new BusinessRuleViolationException("perustetta-ei-loydy");
        }

        return getKaikkiSisalto(id, perusteRev);
    }

    @Override
    @Transactional(readOnly = true)
    @IgnorePerusteUpdateCheck
    public PerusteKaikkiDto getKaikkiSisalto(final Long id) {
        return getKaikkiSisalto(id, null);
    }

    @Override
    @Transactional(readOnly = true)
    @IgnorePerusteUpdateCheck
    public PerusteKaikkiDto getKaikkiSisalto(final Long id, Integer perusteRev) {
        Peruste peruste;
        boolean hasPermission = permissionManager.hasPerustePermission(SecurityContextHolder.getContext().getAuthentication(), id, PermissionManager.Permission.LUKU);
        if (perusteRev != null) {
            peruste = perusteRepository.findRevision(id, perusteRev);
        } else {
            peruste = perusteRepository.findOne(id);
        }

        if (peruste == null) {
            return null;
        }

        PerusteKaikkiDto perusteDto = null;

        if (perusteRev == null && !hasPermission && Objects.equals(peruste.getPerusteprojekti().getTila(), ProjektiTila.JULKAISTU)) {
            throw new AccessDeniedException("ei-riittavia-oikeuksia");
        }

        perusteDto = mapper.map(peruste, PerusteKaikkiDto.class);

        if (peruste.getTpoOpetuksenSisalto() != null) {
            perusteDto.setTpoOpetuksenSisalto(mapper.map(peruste.getTpoOpetuksenSisalto(), TPOOpetuksenSisaltoDto.class));
        }

        if (peruste.getLukiokoulutuksenPerusteenSisalto() != null) {
            updateLukioKaikkiRakenne(perusteDto, peruste);
        }

        if (peruste.getLops2019Sisalto() != null) {
            getLops2019KaikkiRakenne(perusteDto, peruste);
        }

        Revision rev = perusteRepository.getLatestRevisionId(id);
        if (rev != null) {
            perusteDto.setRevision(rev.getNumero());
        } else {
            perusteDto.setRevision(0);
        }

        if (perusteDto.getSuoritustavat() != null
                && !perusteDto.getSuoritustavat().isEmpty()
                && peruste.getLukiokoulutuksenPerusteenSisalto() == null) {
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

    private void getLops2019KaikkiRakenne(PerusteKaikkiDto perusteDto, Peruste peruste) {
        lops2019Service.getOppiaineet(peruste.getId()).forEach(oppiaine -> {
            Lops2019OppiaineKaikkiDto oa = lops2019Service.getOppiaineKaikki(peruste.getId(), oppiaine.getId());
            List<Lops2019OppiaineKaikkiDto> oppimaarat = oa.getOppimaarat().stream()
                    .map(om -> lops2019Service.getOppiaineKaikki(peruste.getId(), om.getId()))
                    .collect(Collectors.toList());
            oa.setOppimaarat(oppimaarat);
            perusteDto.getLops2019Sisalto().getOppiaineet().add(oa);
        });
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
        Peruste p = perusteRepository.findPerusteByIdAndSuoritustapakoodi(id, suoritustapakoodi);
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

        Peruste peruste = perusteRepository.findOne(perusteId);
        if (peruste == null) {
            throw new NotExistsException("Perustetta ei ole olemassa");
        }
        PerusteenOsaViite sisalto = peruste.getSisallot(suoritustapakoodi);
        if (sisalto == null) {
            throw new NotExistsException("Perusteen sisältörakennetta ei ole olemassa");
        }
        return mapper.map(sisalto, view);
    }

    @Override
    @IgnorePerusteUpdateCheck
    @Transactional
    public void onApplicationEvent(PerusteUpdatedEvent event) {
        Peruste peruste = perusteRepository.findOne(event.getPerusteId());
        if (peruste == null) {
            return;
        }
        Date muokattu = new Date();
        if (peruste.getTila() == PerusteTila.VALMIS) {
            perusteRepository.setRevisioKommentti("Perusteen sisältöä korjattu");
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
                || "amosaa/yhteiset".equals(diaarinumero)
                || Pattern.matches("^\\d{1,3}/\\d{3}/\\d{4}$", diaarinumero)
                || Pattern.matches("^OPH-\\d{1,5}-\\d{4}$", diaarinumero);
    }

    @Override
    public PerusteDto update(Long perusteId, PerusteDto perusteDto) {
        Peruste current = perusteRepository.findOne(perusteId);

        if (current == null || current.getTila() == PerusteTila.POISTETTU) {
            throw new NotExistsException("Päivitettävää perustetta ei ole olemassa tai se on poistettu");
        }

        if (current.getTyyppi() == PerusteTyyppi.OPAS) {
            perusteRepository.lock(current);
            Peruste updated = mapper.map(perusteDto, Peruste.class);
            current.setKielet(updated.getKielet());
            current.setKuvaus(updated.getKuvaus());
            current.setNimi(updated.getNimi());
            current.setOsaamisalat(null);
            current.setSiirtymaPaattyy(null);
            current.setVoimassaoloAlkaa(perusteDto.getVoimassaoloAlkaa());
            current.setVoimassaoloLoppuu(perusteDto.getVoimassaoloLoppuu());
            current.setPaatospvm(null);
            current.setKoulutusvienti(false);
            current.setDiaarinumero(null);
            current.setKorvattavatDiaarinumerot(null);
            current.setKoulutukset(null);
            current.setMaarayskirje(null);
            current.setMuutosmaaraykset(null);
            current.setKoulutustyyppi(updated.getKoulutustyyppi());
            current.setOppaanPerusteet(updated.getOppaanPerusteet());
            current.setOppaanKoulutustyypit(updated.getOppaanKoulutustyypit());
            perusteRepository.save(current);
        } else {
            if (!isDiaariValid(perusteDto.getDiaarinumero())) {
                throw new BusinessRuleViolationException("diaarinumero-ei-validi");
            }

            perusteRepository.lock(current);
            Peruste updated = mapper.map(perusteDto, Peruste.class);


            // Liitetään määräyskirjeet
            Maarayskirje maarayskirje = updated.getMaarayskirje();
            MaarayskirjeDto maarayskirjeDto = perusteDto.getMaarayskirje();
            if (maarayskirje != null && maarayskirjeDto != null) {
                Map<Kieli, LiiteBaseDto> dtoLiitteet = maarayskirjeDto.getLiitteet();
                if (!ObjectUtils.isEmpty(dtoLiitteet)) {
                    dtoLiitteet.forEach((kieli, liiteDto) -> {
                        Peruste peruste = perusteRepository.findOne(perusteId);
                        if (kieli != null && liiteDto != null) {
                            Liite liite = liitteet.findOne(liiteDto.getId());

                            if (!liite.getPerusteet().contains(peruste)) {
                                throw new BusinessRuleViolationException("liite-ei-kuulu-julkaistuun-perusteeseen");
                            }

                            if (maarayskirje.getLiitteet() == null) {
                                maarayskirje.setLiitteet(new HashMap<>());
                            }

                            maarayskirje.getLiitteet().put(kieli, liite);
                        }
                    });
                }
            }


            if (updated.getMuutosmaaraykset() != null) {
                for (Muutosmaarays muutosmaarays : updated.getMuutosmaaraykset()) {
                    Map<Kieli, Liite> liitteet = muutosmaarays.getLiitteet();
                    Map<Kieli, Liite> tempLiitteet = new HashMap<>();
                    if (muutosmaarays.getLiitteet() != null) {
                        liitteet.forEach((kieli, liiteId) -> {
                            Liite liite = liiteRepository.findOne(perusteId, liiteId.getId());
                            if (liite != null) {
                                tempLiitteet.put(kieli, liite);
                            }
                        });

                        muutosmaarays.setPeruste(current);
                        muutosmaarays.setLiitteet(tempLiitteet);
                    }
                }
            }

            if (!Objects.equals(current.getKoulutustyyppi(), updated.getKoulutustyyppi())) {
                ArrayList<String> lukioKoulutustyypit = new ArrayList<>();
                lukioKoulutustyypit.add(LUKIOKOULUTUS.toString());
                lukioKoulutustyypit.add(AIKUISTENLUKIOKOULUTUS.toString());
                lukioKoulutustyypit.add(LUKIOVALMISTAVAKOULUTUS.toString());

                // Sallitaan jos sama sisältörakenne
                if (lukioKoulutustyypit.contains(current.getKoulutustyyppi()) || lukioKoulutustyypit.contains(updated.getKoulutustyyppi())) {
                    if (lukioKoulutustyypit.contains(current.getKoulutustyyppi()) && lukioKoulutustyypit.contains(updated.getKoulutustyyppi())) {
                    // noop
                    } else {
                        throw new BusinessRuleViolationException("Koulutustyypin sisällön rakenne täytyy olla sama");
                    }
                }
            }

            if (!Objects.equals(current.getToteutus(), updated.getToteutus())) {
                throw new BusinessRuleViolationException("Toteutustyyppiä ei voi vaihtaa");
            }

            if (current.getTila() == PerusteTila.VALMIS) {
                current = updateValmisPeruste(current, updated);
            }
            else {
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
                current.setKoulutustyyppi(updated.getKoulutustyyppi());

                if (updated.getVstSisalto() != null) {
                    current.setSisalto(updated.getVstSisalto());
                }
            }
        }

        perusteRepository.save(current);
        muokkausTietoService.addMuokkaustieto(perusteId, current, MuokkausTapahtuma.PAIVITYS);
        PerusteDto result = mapper.map(current, PerusteDto.class);
        return result;
    }

    @Override
    public PerusteDto updateFull(Long id, PerusteDto perusteDto) {
        Peruste current = perusteRepository.findOne(id);
        update(id, perusteDto);

        if (current.getTyyppi() != PerusteTyyppi.OPAS && perusteDto.getKvliite() != null) {
            KVLiite liite = current.getKvliite();
            KVLiiteDto kvliiteDto = perusteDto.getKvliite();

            if (current.getKvliite() == null) {
                liite = mapper.map(kvliiteDto, KVLiite.class);
                liite.setPeruste(current);
                liite = kvliiteRepository.save(liite);
                current.setKvliite(liite);
            } else if (kvliiteDto.getId() != null && !kvliiteDto.getId().equals(liite.getId())) {
                throw new BusinessRuleViolationException("virheellinen-liite");
            } else {
                kvliiteDto.setId(liite.getId());
                mapper.map(kvliiteDto, liite);
            }
        }

        if (perusteDto.getVstSisalto() != null) {
            vapaasivistystyoSisaltoService.update(id, perusteDto.getVstSisalto());
        }

        if (perusteDto.getOppaanSisalto() != null) {
            opasSisaltoService.update(id, perusteDto.getOppaanSisalto());
        }

        perusteRepository.save(current);
        muokkausTietoService.addMuokkaustieto(id, current, MuokkausTapahtuma.PAIVITYS);

        return mapper.map(current, PerusteDto.class);
    }

    @Override
    public void updateOsaamisalat(Long id, Set<KoodiDto> osaamisalat) {
        PerusteDto peruste = get(id);
        peruste.setOsaamisalat(osaamisalat);
        update(id, peruste);
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
        current.setKoulutusvienti(updated.isKoulutusvienti());
        current.setKoulutustyyppi(updated.getKoulutustyyppi());

        if (updated.getOsaamisalat() != null && !Objects.deepEquals(current.getOsaamisalat(), updated.getOsaamisalat())) {
            throw new BusinessRuleViolationException("Valmiin perusteen osaamisaloja ei voi muuttaa");
        }

        current.setSiirtymaPaattyy(updated.getSiirtymaPaattyy());
        current.setVoimassaoloAlkaa(updated.getVoimassaoloAlkaa());
        current.setVoimassaoloLoppuu(updated.getVoimassaoloLoppuu());

        Set<ConstraintViolation<Peruste>> violations = new HashSet<>();
        switch (current.getTyyppi()) {
            case OPAS:
                violations = validator.validate(current, Peruste.ValmisOpas.class);
                break;
            case POHJA:
                violations = validator.validate(current, Peruste.ValmisPohja.class);
                break;
            case NORMAALI:
                violations = validator.validate(current, Peruste.Valmis.class);
                break;
            default:
                break;
        }

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
        if (rev != null) {
            rakenneModuuliDto.setVersioId(rev.getNumero());
        } else {
            rakenneModuuliDto.setVersioId(0);
        }
        return rakenneModuuliDto;
    }

    @Override
    @Transactional(readOnly = true)
    public Integer getTutkinnonLaajuus(Long perusteid, Suoritustapakoodi suoritustapakoodi) {

        Long rakenneId = rakenneRepository.getRakenneIdWithPerusteAndSuoritustapa(perusteid, suoritustapakoodi);
        if (rakenneId == null) {
            throw new NotExistsException("Rakennetta ei ole olemassa");
        }

        RakenneModuuli rakenne = rakenneRepository.findOne(rakenneId);
        if (rakenne.getMuodostumisSaanto() != null && rakenne.getMuodostumisSaanto().getLaajuus() != null && rakenne.getMuodostumisSaanto().getLaajuus().getMinimi() != null) {
            return rakenne.getMuodostumisSaanto().getLaajuus().getMinimi();
        }

        return null;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Revision> getRakenneVersiot(Long id, Suoritustapakoodi suoritustapakoodi) {
        List<Revision> versiot = new ArrayList<>();

        Peruste peruste = perusteRepository.findOne(id);
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
        Peruste peruste = perusteRepository.findOne(id);
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
        Peruste peruste = perusteRepository.findOne(id);
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

        muokkausTietoService.addMuokkaustieto(id, peruste, MuokkausTapahtuma.JARJESTETTY);
        return updateTutkinnonRakenne(id, suoritustapakoodi, mapper.map(suoritustapaVersio.getRakenne(), RakenneModuuliDto.class));
    }

    @Override
    @Transactional(readOnly = true)
    @IgnorePerusteUpdateCheck
    public List<TutkinnonOsaViiteDto> getTutkinnonOsat(Long perusteid, Suoritustapakoodi suoritustapakoodi) {
        Peruste peruste = perusteRepository.findOne(perusteid);
        Suoritustapa suoritustapa = peruste.getSuoritustapa(suoritustapakoodi);
        return new ArrayList<>(suoritustapa.getTutkinnonOsat().stream().map(tosaViite -> {
            TutkinnonOsaViiteDto viiteDto = mapper.map(tosaViite, TutkinnonOsaViiteDto.class);
            viiteDto.setTutkinnonOsaDto(mapper.map(tosaViite.getTutkinnonOsa(), TutkinnonOsaDto.class));
            return viiteDto;
        }).collect(Collectors.toSet()));
    }

    @Override
    @Transactional(readOnly = true)
    public List<TutkinnonOsaTilaDto> getTutkinnonOsienTilat(Long perusteid, Suoritustapakoodi suoritustapakoodi) {
        Peruste peruste = perusteRepository.findOne(perusteid);
        Suoritustapa suoritustapa = peruste.getSuoritustapa(suoritustapakoodi);
        return mapper.mapAsList(suoritustapa.getTutkinnonOsat().stream()
                .map(TutkinnonOsaViite::getTutkinnonOsa)
                .collect(Collectors.toList()), TutkinnonOsaTilaDto.class);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TutkinnonOsaViiteDto> getTutkinnonOsat(Long perusteid, Suoritustapakoodi suoritustapakoodi, Integer revisio) {
        Peruste peruste = perusteRepository.findOne(perusteid);
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
            perusteRepository.setRevisioKommentti(rakenne.getMetadata().getKommentti());
        }

        muokkausTietoService.addMuokkaustieto(perusteId, mapper.map(updated, RakenneModuuli.class), MuokkausTapahtuma.PAIVITYS);
        return updated;
    }

    private Set<UUID> keraaTunnisteet(RakenneModuuli rakenne) {
        HashSet<UUID> result = new HashSet<>();
        if (rakenne != null) {
            result.add(rakenne.getTunniste());
            if (rakenne.getOsat() != null) {
                rakenne.getOsat()
                        .forEach(osa -> {
                            result.add(osa.getTunniste());
                            if (osa instanceof RakenneModuuli) {
                                result.addAll(keraaTunnisteet((RakenneModuuli)osa));
                            }
                        });
            }
        }
        return result;
    }

    @Override
    @Transactional
    public RakenneModuuliDto updateTutkinnonRakenne(Long perusteId, Suoritustapakoodi suoritustapakoodi, RakenneModuuliDto rakenne) {

        // EP-1487 päätason muodostumiselle ei sallita kokoa
        if (rakenne.getMuodostumisSaanto() != null && rakenne.getMuodostumisSaanto().getKoko() != null) {
            rakenne.getMuodostumisSaanto().setKoko(null);
        }

        Peruste peruste = perusteRepository.findOne(perusteId);

        Long rakenneId = rakenneRepository.getRakenneIdWithPerusteAndSuoritustapa(perusteId, suoritustapakoodi);
        if (rakenneId == null) {
            throw new NotExistsException("Rakennetta ei ole olemassa");
        }

        Suoritustapa suoritustapa = peruste.getSuoritustapa(suoritustapakoodi);

        rakenne.setRooli(RakenneModuuliRooli.NORMAALI);
        List<TutkintonimikeKoodiDto> tutkintonimikeKoodit = mapper.mapAsList(tutkintonimikeKoodiRepository.findByPerusteId(peruste.getId()), TutkintonimikeKoodiDto.class);
        Map<Reference, String> tovToKoodiMap = suoritustapa.getTutkinnonOsat().stream()
                .collect(Collectors.toMap(
                        tosa -> new Reference(tosa.getId()),
                        tosa -> (tosa.getTutkinnonOsa().getKoodi() != null)
                                ? tosa.getTutkinnonOsa().getKoodi().getUri()
                                : ""));
        tarkistaUniikitKoodit(rakenne, tutkintonimikeKoodit, tovToKoodiMap);

        RakenneModuuli nykyinen = rakenneRepository.findOne(rakenneId);
        lockManager.ensureLockedByAuthenticatedUser(nykyinen.getId());

        rakenne.foreach(new VisitorImpl(maxRakenneDepth));
        RakenneModuuli moduuli = mapper.map(rakenne, RakenneModuuli.class);

        // Valmiin perusteen tunnisteet eivät saa muuttua
        boolean rakenneMuuttunut = moduuli.isSame(nykyinen, 0, true).isPresent();
        if (rakenneMuuttunut) {
            if (PerusteTila.VALMIS.equals(peruste.getTila())) {
                Set<UUID> nykyisetTunnisteet = keraaTunnisteet(nykyinen);
                Set<UUID> uudetTunnisteet = keraaTunnisteet(moduuli);

                if (!uudetTunnisteet.containsAll(nykyisetTunnisteet)) {
                    throw new BusinessRuleViolationException("rakenteen-tunnisteita-ei-voi-muuttaa");
                }
            }

            if (perusteRepository.findOne(perusteId).getTila() == PerusteTila.VALMIS) {
                Optional<AbstractRakenneOsa.RakenneOsaVirhe> muutosVirheellinen = moduuli.isSame(nykyinen, 0, false);
                if (muutosVirheellinen.isPresent()) {
                    throw new BusinessRuleViolationException(muutosVirheellinen.get().getMessage());
                }
            }

            moduuli = checkIfKoodiAlreadyExists(moduuli);

            { // Save-delete ongelma uniikkien tunnisteiden kanssa
                rakenneRepository.delete(suoritustapa.getRakenne());
                suoritustapa.setRakenne(new RakenneModuuli());
                em.flush();
            }

            (new RakenneModuuli()).mergeState(moduuli);
            suoritustapa.setRakenne(rakenneRepository.save(moduuli));
            onApplicationEvent(PerusteUpdatedEvent.of(this, perusteId));
        }

        RakenneModuuliDto updated = mapper.map(moduuli, RakenneModuuliDto.class);
        updateAllTutkinnonOsaJarjestys(perusteId, updated);
        return updated;
    }

    private Stream<AbstractRakenneOsaDto> haeUniikitTutkinnonOsaViitteet(AbstractRakenneOsaDto root) {
        if (root instanceof RakenneModuuliDto) {
            return Stream.concat(Stream.of(root), ((RakenneModuuliDto) root).getOsat().stream()
                    .map(this::haeUniikitTutkinnonOsaViitteet)
                    .flatMap(osa -> osa));
        } else {
            return Stream.of(root);
        }
    }

    @Override
    @Transactional
    public void updateAllTutkinnonOsaJarjestys(Long perusteId, RakenneModuuliDto rakenne) {
        // Lista tutkinnon osista tutkinnon muodostumisen mukaan
        List<TutkinnonOsaViite> viitteet = haeUniikitTutkinnonOsaViitteet(rakenne)
                .filter(osa -> osa instanceof RakenneOsaDto)
                .map(osa -> ((RakenneOsaDto) osa).getTutkinnonOsaViite())
                .filter(Objects::nonNull)
                .distinct()
                .map(osa -> mapper.map(osa, TutkinnonOsaViite.class))
                .collect(Collectors.toList());

        // Tallennetaan uudet järjestysluvut
        Integer jnro = 1;
        for (TutkinnonOsaViite osa : viitteet) {
            osa.setJarjestys(jnro);
            jnro++;
        }
    }

    private void tarkistaUniikitKoodit(RakenneModuuliDto rakenneModuuli,
                                       List<TutkintonimikeKoodiDto> tutkintonimikeKoodit,
                                       Map<Reference, String> tovToKoodiUriMap) {
        Map<String, List<RakenneModuuliDto>> tutkintonimikeRyhmat = new HashMap<>();
        Map<String, List<RakenneModuuliDto>> osaamisalaRyhmat = new HashMap<>();
        Stack<RakenneModuuliDto> stack = new Stack<>();
        stack.push(rakenneModuuli);

        while (!stack.empty()) {
            RakenneModuuliDto head = stack.pop();

            // Tutkintonimikeryhmät kerätään myöhempää tarkastelua varten
            if (head.getTutkintonimike() != null) {
                String uri = head.getTutkintonimike().getUri();
                List<RakenneModuuliDto> moduulit = tutkintonimikeRyhmat.computeIfAbsent(uri, k -> new ArrayList<>());
                moduulit.add(head);
            }

            if (head.getOsaamisala() != null) {
                String uri = head.getOsaamisala().getOsaamisalakoodiUri();
                List<RakenneModuuliDto> moduulit = osaamisalaRyhmat.computeIfAbsent(uri, k -> new ArrayList<>());
                moduulit.add(head);
            }

            if (head.getOsat() != null) {
                for (AbstractRakenneOsaDto osa : head.getOsat()) {
                    if (osa instanceof RakenneModuuliDto) {
                        stack.push((RakenneModuuliDto)osa);
                    }
                }
            }
        }

        { // Käytetään perusteen metatietoihin liitettyjä tutkintonimikekoodiliitoksia validointiin
            List<TutkintonimikeKoodiDto> tutkintonimikeTutkinnonOsaLiitokset = tutkintonimikeKoodit.stream()
                    .filter(tk -> StringUtils.isNotEmpty(tk.getTutkinnonOsaUri()) && StringUtils.isNotEmpty(tk.getTutkintonimikeUri()))
                    .collect(Collectors.toList());

            for (TutkintonimikeKoodiDto tk : tutkintonimikeTutkinnonOsaLiitokset) {
                List<RakenneModuuliDto> moduulit = tutkintonimikeRyhmat.get(tk.getTutkintonimikeUri());

                // Käydään läpi vain päällekkäiset tutkintonimikeryhmät
                if (moduulit == null || moduulit.size() < 2) {
                    continue;
                }

                // Kerätään tutkintonimikkeiden vaaditut tutkintonimikekoodit
                Set<String> moduulinVaaditutKoodit = tutkintonimikeTutkinnonOsaLiitokset.stream()
                        .filter(liitos -> Objects.equals(tk.getTutkintonimikeUri(), liitos.getTutkintonimikeUri()))
                        .map(TutkintonimikeKoodiDto::getTutkinnonOsaUri)
                        .collect(Collectors.toSet());

                for (RakenneModuuliDto moduuli : moduulit) {
                    if (!CollectionUtils.isEmpty(moduuli.getOsat())) {
                        Set<String> tutkinnonOsaKoodit = moduuli.getOsat().stream()
                                .filter(osa -> osa instanceof RakenneOsaDto)
                                .map(osa -> ((RakenneOsaDto) osa).getTutkinnonOsaViite())
                                .map(tovToKoodiUriMap::get)
                                .filter(StringUtils::isNotEmpty)
                                .collect(Collectors.toSet());

                        if (!moduulinVaaditutKoodit.removeAll(tutkinnonOsaKoodit)) {
                            throw new BusinessRuleViolationException("tutkintonimikeryhmalle-maaritetty-tutkinnon-osa-useaan-kertaan");
                        }
                    }
                }
            }
        }

        { // Osaamisalojen rakenteen tarkistus
            // Osaamisala kiinnitetty rakenteeseen useammin kuin kerran -> Ryhmän sisältöjen tätyy olla uniikit muihin verrattuna
            osaamisalaRyhmat.entrySet().stream()
                    .filter(entry -> entry.getValue().size() > 1)
                    .forEach(entry -> {
                        List<Set<String>> ryhmat = entry.getValue().stream()
                                .map(moduuli -> moduuli.getOsat().stream()
                                        .map(AbstractRakenneOsaDto::validationIdentifier)
                                        .filter(StringUtils::isNotEmpty)
                                        .collect(Collectors.toSet()))
                                .collect(Collectors.toList());

                        // Uniikit koodit
                        final int totalSize = ryhmat.stream()
                                .map(Set::size)
                                .reduce((acc, next) -> acc + next)
                                .orElse(0);

                        Set<String> koodit = ryhmat.stream()
                                .reduce((acc, next) -> {
                                    acc.addAll(next);
                                    return acc;
                                })
                                .orElseGet(HashSet::new);

                        // Yhdistettyjen koodien määrän täytyy olla sama kuin kokonaismäärä erikseen laskettuna
                        if (totalSize != koodit.size()) {
                            throw new BusinessRuleViolationException("osaamisala-liitetty-virheelliseti-tutkinnon-osiin");
                        }
                    });
        }
    }


    private RakenneModuuli checkIfKoodiAlreadyExists(RakenneModuuli rakenneModuuli) {
        if (rakenneModuuli != null) {
            if (rakenneModuuli.getOsaamisala() != null && rakenneModuuli.getOsaamisala().getUri() != null) {
                Koodi osaamisalaKoodi = koodiRepository.findFirstByUriOrderByVersioDesc(
                        rakenneModuuli.getOsaamisala().getUri());
                if (osaamisalaKoodi != null) {
                    rakenneModuuli.setOsaamisala(osaamisalaKoodi);
                } else {
                    rakenneModuuli.setOsaamisala(koodiRepository.save(rakenneModuuli.getOsaamisala()));
                }
            } else {
                rakenneModuuli.setOsaamisala(null);
            }

            if (rakenneModuuli.getTutkintonimike() != null && rakenneModuuli.getTutkintonimike().getUri() != null) {
                Koodi tutkintonimikeKoodi = koodiRepository.findFirstByUriOrderByVersioDesc(
                        rakenneModuuli.getTutkintonimike().getUri());
                if (tutkintonimikeKoodi != null) {
                    rakenneModuuli.setTutkintonimike(tutkintonimikeKoodi);
                } else {
                    rakenneModuuli.setTutkintonimike(koodiRepository.save(rakenneModuuli.getTutkintonimike()));
                }
            } else {
                rakenneModuuli.setTutkintonimike(null);
            }

            for (AbstractRakenneOsa osa : rakenneModuuli.getOsat()) {
                if (osa instanceof RakenneModuuli) {
                    osa = checkIfKoodiAlreadyExists((RakenneModuuli) osa);
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
                muokkausTietoService.addMuokkaustieto(id, viite, MuokkausTapahtuma.POISTO);
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
        Peruste peruste = perusteRepository.findOne(id);

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
        viite.getTutkinnonOsa().asetaAlkuperainenPeruste(peruste);

        viite.setSuoritustapa(suoritustapa);
        viite.setMuokattu(new Date());

        if (suoritustapa.getTutkinnonOsat().add(viite)) {
            viite = tutkinnonOsaViiteRepository.save(viite);
        } else {
            throw new BusinessRuleViolationException("Viite tutkinnon osaan on jo olemassa");
        }
        muokkausTietoService.addMuokkaustieto(id, viite, MuokkausTapahtuma.LUONTI);
        return mapper.map(viite, TutkinnonOsaViiteDto.class);

    }

    @Override
    @Transactional
    public TutkinnonOsaViiteDto attachTutkinnonOsa(Long id, Suoritustapakoodi suoritustapakoodi, TutkinnonOsaViiteDto osa) {
        return attachTutkinnonOsa(id, suoritustapakoodi, osa, null);
    }

    @Override
    @Transactional
    public TutkinnonOsaViiteDto attachTutkinnonOsa(Long id, Suoritustapakoodi suoritustapakoodi, TutkinnonOsaViiteDto osa, PerusteKevytDto alkuperainenPeruste) {
        final Suoritustapa suoritustapa = getSuoritustapaEntity(id, suoritustapakoodi);

        // Workaround jolla estetään versiointiongelmat yhtäaikaisten muokkausten tapauksessa
        suoritustapaRepository.lock(suoritustapa, false);
        TutkinnonOsaViite viite = mapper.map(osa, TutkinnonOsaViite.class);
        viite.setSuoritustapa(suoritustapa);
        viite.setMuokattu(new Date());

        if (viite.getTutkinnonOsa().getAlkuperainenPeruste() == null && alkuperainenPeruste != null) {
            Peruste peruste = perusteRepository.findOne(alkuperainenPeruste.getId());
            viite.getTutkinnonOsa().asetaAlkuperainenPeruste(peruste);
        }

        if (suoritustapa.getTutkinnonOsat().add(viite)) {
            viite = tutkinnonOsaViiteRepository.save(viite);
            suoritustapaRepository.save(suoritustapa);
        } else {
            throw new BusinessRuleViolationException("Viite tutkinnon osaan on jo olemassa");
        }

        TutkinnonOsaViiteDto viiteDto = mapper.map(viite, TutkinnonOsaViiteDto.class);
        viiteDto.setTutkinnonOsaDto(mapper.map(viite.getTutkinnonOsa(), TutkinnonOsaDto.class));
        return viiteDto;
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
        muokkausTietoService.addMuokkaustieto(id, viite, MuokkausTapahtuma.PAIVITYS);
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
        TutkinnonOsa tutkinnonOsa = viite.getTutkinnonOsa();

        if (viite == null || !viite.getSuoritustapa().equals(suoritustapa)) {
            throw new BusinessRuleViolationException("Virheellinen viiteId");
        }

        TutkinnonOsaViiteDto viiteDto = mapper.map(viite, TutkinnonOsaViiteDto.class);
        TutkinnonOsaDto tutkinnonOsaDto = mapper.map(viite.getTutkinnonOsa(), TutkinnonOsaDto.class);
        viiteDto.setTutkinnonOsaDto(tutkinnonOsaDto);

        return viiteDto;
    }

    private Suoritustapa getSuoritustapaEntity(Long perusteid, Suoritustapakoodi suoritustapakoodi) {
        if (!perusteRepository.exists(perusteid)) {
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
        Peruste peruste = perusteRepository.findOne(perusteId);
        if (peruste == null) {
            throw new NotExistsException("Perustetta ei ole olemassa");
        }
        PerusteenOsaViite sisalto = peruste.getSisallot(suoritustapakoodi);
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
    @IgnorePerusteUpdateCheck
    public List<TutkintonimikeKoodiDto> getTutkintonimikeKoodit(Long perusteId) {
        return doGetTutkintonimikeKoodit(perusteId);
    }

    @Override
    @Transactional(readOnly = true)
    @IgnorePerusteUpdateCheck
    public TutkintonimikeKoodiDto getTutkintonimikeKoodi(@P("perusteId") Long perusteId, String tutkintonimikeKoodiUri) {
        return getTutkintonimikeKoodit(perusteId).stream()
                .filter(tutkintonimikeKoodi -> tutkintonimikeKoodi.getTutkintonimikeUri().equals(tutkintonimikeKoodiUri))
                .findFirst()
                .orElse(null);
    }

    @Override
    @Transactional(readOnly = true)
    @IgnorePerusteUpdateCheck
    public List<TutkintonimikeKoodiDto> doGetTutkintonimikeKoodit(Long perusteId) {
        List<TutkintonimikeKoodi> koodit = tutkintonimikeKoodiRepository.findByPerusteId(perusteId);
        return mapper.mapAsList(koodit, TutkintonimikeKoodiDto.class);
    }

    @Override
    public TutkintonimikeKoodiDto addTutkintonimikeKoodi(Long perusteId, TutkintonimikeKoodiDto dto) {
        Peruste peruste = perusteRepository.findOne(perusteId);
        dto.setPeruste(peruste.getReference());
        TutkintonimikeKoodi tnk = mapper.map(dto, TutkintonimikeKoodi.class);
        TutkintonimikeKoodi saved = tutkintonimikeKoodiRepository.save(tnk);
        return mapper.map(saved, TutkintonimikeKoodiDto.class);
    }

    @Override
    public TutkintonimikeKoodiDto updateTutkintonimikeKoodi(Long perusteId, TutkintonimikeKoodiDto dto) {
        TutkintonimikeKoodi tnk = mapper.map(dto, TutkintonimikeKoodi.class);
        TutkintonimikeKoodi saved = tutkintonimikeKoodiRepository.save(tnk);
        return mapper.map(saved, TutkintonimikeKoodiDto.class);
    }

    @Override
    public void updateTutkintonimikkeet(Long perusteId, List<TutkintonimikeKoodiDto> tutkintonimikeKoodiDtos) {
        tutkintonimikeKoodiDtos = tutkintonimikeKoodiDtos.stream().map(tutkintonimike -> {
            if (tutkintonimike.getTutkintonimikeUri() == null) {
                KoodistoKoodiDto lisattyKoodi = koodistoClient.addKoodiNimella("tutkintonimikkeet", tutkintonimike.getNimi(), 5);
                tutkintonimike.setTutkintonimikeArvo(lisattyKoodi.getKoodiArvo());
                tutkintonimike.setTutkintonimikeUri(lisattyKoodi.getKoodiUri());
                return tutkintonimike;
            }
            return tutkintonimike;
        }).collect(Collectors.toList());

        List<TutkintonimikeKoodiDto> perusteenTutkintonimikkeet = getTutkintonimikeKoodit(perusteId);
        List<String> perusteenTutkintonimikeUrit = perusteenTutkintonimikkeet.stream().map(TutkintonimikeKoodiDto::getTutkintonimikeUri).collect(Collectors.toList());
        List<String> tallennettavatTutkintonimikkeet = tutkintonimikeKoodiDtos.stream().map(TutkintonimikeKoodiDto::getTutkintonimikeUri).collect(Collectors.toList());
        tutkintonimikeKoodiDtos.stream()
                .filter(tutkintonimike -> perusteenTutkintonimikeUrit.contains(tutkintonimike.getTutkintonimikeUri()))
                .filter(tutkintonimike -> tutkintonimike.isTutkintonimikeTemporary())
                .forEach(tutkintonimike -> updateTutkintonimikeKoodi(perusteId, tutkintonimike));
        tutkintonimikeKoodiDtos.stream()
                .filter(tutkintonimike -> !perusteenTutkintonimikeUrit.contains(tutkintonimike.getTutkintonimikeUri()))
                .forEach(tutkintonimike -> addTutkintonimikeKoodi(perusteId, tutkintonimike));
        perusteenTutkintonimikkeet.stream()
                .filter(tutkintonimike -> !tallennettavatTutkintonimikkeet.contains(tutkintonimike.getTutkintonimikeUri()))
                .forEach(tutkintonimike -> removeTutkintonimikeKoodi(perusteId, tutkintonimike.getId()));

        getTutkintonimikeKoodit(perusteId).forEach(tutkintonimikeKoodi -> {
            if (tutkintonimikeKoodi.isTutkintonimikeTemporary()) {
                Koodi koodi = koodiRepository.findFirstByUriOrderByVersioDesc(tutkintonimikeKoodi.getTutkintonimikeUri());
                if (koodi != null) {
                    LokalisoituTekstiDto koodiNimi = mapper.map(koodi, KoodiDto.class).getNimi();

                    if (koodiNimi == null || !koodiNimi.equals(tutkintonimikeKoodi.getNimi())) {
                        koodi.setNimi(mapper.map(tutkintonimikeKoodi.getNimi(), TekstiPalanen.class));
                        koodiRepository.save(koodi);
                    }
                } else {
                    koodi = new Koodi();
                    koodi.setUri(tutkintonimikeKoodi.getTutkintonimikeUri());
                    koodi.setNimi(mapper.map(tutkintonimikeKoodi.getNimi(), TekstiPalanen.class));
                    koodiRepository.save(koodi);
                }
            }

        });
    }

    @Override
    public void removeTutkintonimikeKoodi(Long perusteId, Long tutkintonimikeKoodiId) {
        TutkintonimikeKoodi tnk = tutkintonimikeKoodiRepository.findOne(tutkintonimikeKoodiId);
        if (Objects.equals(tnk.getPeruste().getId(), perusteId)) {
            tutkintonimikeKoodiRepository.delete(tutkintonimikeKoodiId);
        }
    }

    @Override
    public void exportPeruste(Long perusteId, ZipOutputStream zipOutputStream) throws IOException {
        Peruste peruste = perusteRepository.getOne(perusteId);
        PerusteprojektiLuontiDto projektiDto = mapper.map(peruste.getPerusteprojekti(), PerusteprojektiLuontiDto.class);
        PerusteKaikkiDto perusteDto = getJulkaistuSisalto(perusteId);

        // Perusteprojekti
        zipOutputStream.putNextEntry(new ZipEntry("perusteprojekti.json"));
        ieMapper.writeValue(zipOutputStream, projektiDto);

        // Peruste
        zipOutputStream.putNextEntry(new ZipEntry("peruste.json"));
        ieMapper.writeValue(zipOutputStream, perusteDto);

        // Termit
        List<TermiDto> termit = termistoService.getTermit(perusteId);
        zipOutputStream.putNextEntry(new ZipEntry("termit.json"));
        ieMapper.writeValue(zipOutputStream, termit);

        // Liitteet
        List<Liite> liitteet = this.liitteet.findByPerusteId(perusteId);
        zipOutputStream.putNextEntry(new ZipEntry("liitteet.json"));
        ieMapper.writeValue(zipOutputStream, mapper.mapAsList(liitteet, LiiteDto.class));

        for (Liite liite : liitteet) {
            try {
                Blob data = liite.getData();
                MimeTypes mimeTypes = MimeTypes.getDefaultMimeTypes();
                String extension = mimeTypes.forName(liite.getMime()).getExtension();
                zipOutputStream.putNextEntry(new ZipEntry("liitetiedostot/" + liite.getId() + extension));
                IOUtils.copy(data.getBinaryStream(), zipOutputStream);
            } catch (MimeTypeException | SQLException e) {
                log.error(e.getLocalizedMessage(), e);
            }
        }
    }

    @Override
    public void importPeruste(MultipartHttpServletRequest request) throws IOException {
        PerusteprojektiLuontiDto perusteprojektiDto = null;
        PerusteKaikkiDto perusteKaikkiDto = null;
        List<TermiDto> termit = new ArrayList<>();
        HashMap<UUID, byte[]> liitetiedostot = new HashMap<>();
        List<LiiteDto> liitteet = new ArrayList<>();

        Iterator<String> it = request.getFileNames();
        while (it.hasNext()) {
            ZipInputStream zipInputStream = new ZipInputStream(request.getFile(it.next()).getInputStream());
            ZipEntry entry;
            while((entry = zipInputStream.getNextEntry()) != null) {
                String entryName = entry.getName();

                if (entryName.startsWith("liitetiedostot/")) {
                    // Käytetään tiedostonimestä otettua UUID:tä
                    UUID uuid = UUID.fromString(FilenameUtils.removeExtension(new File(entry.getName()).getName()));
                    liitetiedostot.put(uuid, IOUtils.toByteArray(zipInputStream));
                    continue;
                }

                switch (entryName) {
                    case "perusteprojekti.json":
                        perusteprojektiDto = ieMapper.readValue(zipInputStream, PerusteprojektiLuontiDto.class);
                        break;
                    case "peruste.json":
                        perusteKaikkiDto = ieMapper.readValue(zipInputStream, PerusteKaikkiDto.class);
                        break;
                    case "termit.json":
                        termit = ieMapper.readValue(zipInputStream, new TypeReference<List<TermiDto>>(){});
                        break;
                    case "liitteet.json":
                        liitteet = ieMapper.readValue(zipInputStream, new TypeReference<List<LiiteDto>>(){});
                        break;
                    default:
                        log.warn("Tuntematon tiedosto: " + entryName);
                        break;
                }
            }

            zipInputStream.close();
        }

        PerusteprojektiImportDto importDto = new PerusteprojektiImportDto(perusteprojektiDto, perusteKaikkiDto, termit, liitetiedostot, liitteet);
        dispatcher.get(importDto.getPeruste(), PerusteImport.class)
                .tuoPerusteprojekti(importDto);
    }

    private Peruste getPeruste(Long perusteId) {
        Peruste peruste = perusteRepository.getOne(perusteId);
        if (peruste == null) {
            throw new BusinessRuleViolationException("peruste-puuttuu");
        }
        return peruste;
    }

    private void lisaaTutkinnonMuodostuminen(Peruste peruste) {
        if ((LUKIOKOULUTUS.toString().equals(peruste.getKoulutustyyppi())
                || AIKUISTENLUKIOKOULUTUS.toString().equals(peruste.getKoulutustyyppi())
                || LUKIOVALMISTAVAKOULUTUS.toString().equals(peruste.getKoulutustyyppi()))
                && KoulutustyyppiToteutus.LOPS2019.equals(peruste.getToteutus())) {
            return;
        }

        if (KoulutusTyyppi.PERUSOPETUS.toString().equals(peruste.getKoulutustyyppi())) {
            lisaaKovakoodattuPerusteenOsa(
                    peruste.getPerusopetuksenPerusteenSisalto().getSisalto(),
                    "docgen.laaja_alaiset_osaamiset.title",
                    PerusteenOsaTunniste.LAAJAALAINENOSAAMINEN);
            return;
        }

        if (KoulutusTyyppi.AIKUISTENPERUSOPETUS.toString().equals(peruste.getKoulutustyyppi())) {
            lisaaKovakoodattuPerusteenOsa(
                    peruste.getAipeOpetuksenPerusteenSisalto().getSisalto(),
                    "docgen.laaja_alaiset_osaamiset.title",
                    PerusteenOsaTunniste.LAAJAALAINENOSAAMINEN);
            return;
        }

        for (Suoritustapa st : peruste.getSuoritustavat()) {
            PerusteenOsaViite sisalto = st.getSisalto();
            String nimenLokalisointi;
            if (KoulutusTyyppi.of(peruste.getKoulutustyyppi()).equals(KoulutusTyyppi.VALMA)
                    || KoulutusTyyppi.of(peruste.getKoulutustyyppi()).equals(KoulutusTyyppi.TELMA)) {
                nimenLokalisointi = "docgen.koulutuksen_muodostuminen.title";
            } else {
                nimenLokalisointi = "docgen.tutkinnon_muodostuminen.title";
            }

            lisaaKovakoodattuPerusteenOsa(sisalto, nimenLokalisointi, PerusteenOsaTunniste.RAKENNE);
        }
    }

    private void lisaaKovakoodattuPerusteenOsa(
            PerusteenOsaViite sisalto,
            String nimenLokalisointi,
            PerusteenOsaTunniste tunniste) {
        TekstiKappale tk = new TekstiKappale();
        HashMap<Kieli, String> hm = new HashMap<>();
        hm.put(Kieli.FI, messages.translate(nimenLokalisointi, Kieli.FI));
        hm.put(Kieli.SV, messages.translate(nimenLokalisointi, Kieli.SV));
        tk.setNimi(tekstiPalanenRepository.save(TekstiPalanen.of(hm)));
        tk.setTunniste(tunniste);
        PerusteenOsaViite pov = perusteenOsaViiteRepo.save(new PerusteenOsaViite());
        pov.setPerusteenOsa(perusteenOsaRepository.save(tk));
        pov.setVanhempi(sisalto);
        sisalto.getLapset().add(pov);
    }

    @Override
    public Peruste luoPerusteRunko(
            KoulutusTyyppi koulutustyyppi,
            KoulutustyyppiToteutus toteutus,
            LaajuusYksikko yksikko,
            PerusteTyyppi tyyppi
    ) {
        return luoPerusteRunko(koulutustyyppi, toteutus,yksikko, tyyppi, false);
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
            KoulutustyyppiToteutus toteutus,
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
        } else if (koulutustyyppi == KoulutusTyyppi.TPO) {
            peruste.setSisalto(new TpoOpetuksenSisalto());
        } else if (koulutustyyppi == KoulutusTyyppi.PERUSOPETUS) {
            peruste.setSisalto(new PerusopetuksenPerusteenSisalto());
        } else if (koulutustyyppi == KoulutusTyyppi.ESIOPETUS
                || koulutustyyppi == KoulutusTyyppi.PERUSOPETUSVALMISTAVA
                || koulutustyyppi == KoulutusTyyppi.LISAOPETUS
                || koulutustyyppi == KoulutusTyyppi.VARHAISKASVATUS) {
            peruste.setSisalto(new EsiopetuksenPerusteenSisalto());
        } else if (koulutustyyppi == LUKIOKOULUTUS
                || koulutustyyppi == AIKUISTENLUKIOKOULUTUS
                || koulutustyyppi == LUKIOVALMISTAVAKOULUTUS) {
            if (KoulutustyyppiToteutus.LOPS2019.equals(toteutus)) {
                st = suoritustapaService.createSuoritustapa(Suoritustapakoodi.LUKIOKOULUTUS2019, LaajuusYksikko.OPINTOPISTE);
                Lops2019Sisalto sisalto = new Lops2019Sisalto();
                sisalto.setLaajaAlainenOsaaminen(new Lops2019LaajaAlainenOsaaminenKokonaisuus());
                peruste.setSisalto(sisalto);
                peruste.setToteutus(toteutus);
            } else {
                st = suoritustapaService.createSuoritustapaWithSisaltoAndRakenneRoots(Suoritustapakoodi.LUKIOKOULUTUS, LaajuusYksikko.KURSSI);
                LukiokoulutuksenPerusteenSisalto sisalto = new LukiokoulutuksenPerusteenSisalto();
                initLukioOpetuksenYleisetTavoitteet(sisalto);
                aihekokonaisuudetService.initAihekokonaisuudet(sisalto);
                initLukioOpetussuunitelmaRakenne(peruste, sisalto);
            }
        } else if (koulutustyyppi == KoulutusTyyppi.AIKUISTENPERUSOPETUS) {
            AIPEOpetuksenSisalto sisalto = new AIPEOpetuksenSisalto();
            peruste.setSisalto(sisalto);
        } else if (koulutustyyppi.isVapaaSivistystyo() || koulutustyyppi == MAAHANMUUTTAJIENKOTOUTUMISKOULUTUS) {
            peruste.setSisalto(new VapaasivistystyoSisalto());
        } else if (koulutustyyppi == TUTKINTOONVALMENTAVA) {
            peruste.setSisalto(new TutkintoonvalmentavaSisalto());
        }

        if (st != null) {
            suoritustavat.add(st);
        }

        peruste.setSuoritustavat(suoritustavat);
        for (Suoritustapa suoritustapa : suoritustavat) {
            suoritustapa.getPerusteet().add(peruste);
        }
        perusteRepository.save(peruste);
        lisaaTutkinnonMuodostuminen(peruste);
        return peruste;
    }

    private void initLukioOpetussuunitelmaRakenne(Peruste peruste, LukiokoulutuksenPerusteenSisalto sisalto) {
        sisalto.getOpetussuunnitelma().setSisalto(sisalto);
        peruste.setSisalto(sisalto);
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
        sisalto.setSisalto(vanha.getSisalto().copy());
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
        Peruste vanha = perusteRepository.getOne(luontiDto.getPerusteId());
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
            peruste.setSisalto(uusiSisalto);
            peruste = perusteRepository.save(peruste);
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
            peruste = perusteRepository.save(peruste);

            if (KoulutusTyyppi.PERUSOPETUS.toString().equalsIgnoreCase(vanha.getKoulutustyyppi())) {
                peruste.setSisalto(kloonaaPerusopetuksenSisalto(peruste, vanha.getPerusopetuksenPerusteenSisalto()));
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
        Peruste peruste = perusteRepository.getOne(perusteId);
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
        Peruste peruste = perusteRepository.getOne(perusteId);
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
        Peruste peruste = perusteRepository.getOne(perusteId);
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

            if (dto instanceof RakenneModuuliDto) {
                // Tarkistetaan, että tutkinnossa määriteltäviin ryhmiin ei ole lisätty osia
                RakenneModuuliDto rakenneModuuliDto = (RakenneModuuliDto) dto;
                if (rakenneModuuliDto.getRooli() != null
                        && rakenneModuuliDto.getRooli().equals(RakenneModuuliRooli.VIRTUAALINEN)
                        && rakenneModuuliDto.getOsat().size() > 0) {
                    throw new BusinessRuleViolationException("ryhman-rooli-ei-salli-sisaltoa");
                }

                // Osaamisalaa ja tutkintonimikettä ei voi asettaa samanaikaisesti
                if (rakenneModuuliDto.getTutkintonimike() != null && rakenneModuuliDto.getOsaamisala() != null) {
                    throw new BusinessRuleViolationException("virheellisteti-asetettu-tutkintonimike");
                }

                // Tarkistetaan tutkintonimike
                if (rakenneModuuliDto.getTutkintonimike() == null && rakenneModuuliDto.getRooli() == RakenneModuuliRooli.TUTKINTONIMIKE) {
                    throw new BusinessRuleViolationException("virheellisesti-asetettu-tutkintonimike");
                }
                if (rakenneModuuliDto.getRooli() == RakenneModuuliRooli.TUTKINTONIMIKE && rakenneModuuliDto.getTutkintonimike() == null) {
                    throw new BusinessRuleViolationException("tutkintonimikeryhmalta-puuttuu-tutkintonimikekoodi");
                }

                if (rakenneModuuliDto.getOsaamisala() == null && rakenneModuuliDto.getRooli() == RakenneModuuliRooli.OSAAMISALA) {
                    throw new BusinessRuleViolationException("virheellisesti-asetettu-osaamisala");
                }
                if (rakenneModuuliDto.getRooli() == RakenneModuuliRooli.OSAAMISALA && rakenneModuuliDto.getOsaamisala() == null) {
                    throw new BusinessRuleViolationException("osaamisalaryhmalta-puuttuu-osaamisalakoodi");
                }
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

    @Override
    @IgnorePerusteUpdateCheck
    public KVLiiteJulkinenDto getJulkinenKVLiite(long perusteId) {
        Peruste peruste = perusteRepository.getOne(perusteId);
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
            kvliiteDto.setTutkinnostaPaattavaViranomainen(pohjaLiiteDto.getTutkinnostaPaattavaViranomainen());
            kvliiteDto.setArvosanaAsteikko(pohjaLiiteDto.getArvosanaAsteikko());
            kvliiteDto.setJatkoopintoKelpoisuus(pohjaLiiteDto.getJatkoopintoKelpoisuus());
            kvliiteDto.setKansainvalisetSopimukset(pohjaLiiteDto.getKansainvalisetSopimukset());
            kvliiteDto.setSaadosPerusta(pohjaLiiteDto.getSaadosPerusta());
            kvliiteDto.setTutkintotodistuksenSaaminen(pohjaLiiteDto.getTutkintotodistuksenSaaminen());
            kvliiteDto.setPohjakoulutusvaatimukset(pohjaLiiteDto.getPohjakoulutusvaatimukset());
            kvliiteDto.setLisatietoja(pohjaLiiteDto.getLisatietoja());
        }

        kvliiteDto.setMuodostumisenKuvaus(muodostumistenKuvaukset);

        if (!ObjectUtils.isEmpty(peruste.getKoulutukset())) {
            kvliiteDto.setTasot(haeTasot(peruste.getId(), peruste));
        }

        return kvliiteDto;
    }

    @Override
    public PerusteDto updateKvLiite(Long id, KVLiiteJulkinenDto kvliiteDto) {
        Peruste current = perusteRepository.findOne(id);

        KVLiite liite = current.getKvliite();

        if (current.getKvliite() == null) {
            liite = mapper.map(kvliiteDto, KVLiite.class);
            liite.setPeruste(current);
            liite = kvliiteRepository.save(liite);
            current.setKvliite(liite);
        }
        else if (kvliiteDto.getId() != null && !kvliiteDto.getId().equals(liite.getId())) {
            throw new BusinessRuleViolationException("virheellinen-liite");
        }
        else {
            kvliiteDto.setId(liite.getId());
            mapper.map(kvliiteDto, liite);
        }

        muokkausTietoService.addMuokkaustieto(id, liite, MuokkausTapahtuma.PAIVITYS);

        return mapper.map(current, PerusteDto.class);
    }

    @Override
    @IgnorePerusteUpdateCheck
    public List<KVLiiteTasoDto> haeTasot(Long perusteId, Peruste peruste) {

        if (ObjectUtils.isEmpty(peruste.getKoulutukset())) {
            return Collections.emptyList();
        }

        Set<String> tasokoodiFilter = new HashSet<>();
        return peruste.getKoulutukset().stream()
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
                                        lokaali -> "ISCED " + getISCEDTaso(peruste, el.getCodeElementValue())))));
                    }
                    else {
                        result.setNimi(null);
                    }
                    return result;
                })
                .sorted(Comparator.comparingInt(KVLiiteTasoDto::getJarjestys))
                .collect(Collectors.toList());
    }

    private boolean isTasoKoodi(String koodi) {

        return koodi != null
                && (koodi.startsWith("eqf_")
                || koodi.startsWith("nqf_")
                || koodi.startsWith("isced2011koulutusastetaso1_"));
    }

    private String getISCEDTaso(Peruste peruste, String oletus) {
        switch (KoulutusTyyppi.of(peruste.getKoulutustyyppi())) {
            case PERUSTUTKINTO:
            case AMMATTITUTKINTO:
                return "3";
            case ERIKOISAMMATTITUTKINTO:
                return "4";
            default:
                return oletus;
        }
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

    @Override
//    @Cacheable("peruste-navigation")
    public NavigationNodeDto buildNavigationWithDate(Long perusteId, Date pvm, String kieli) {
        NavigationNodeDto navigationNodeDto = dispatcher.get(perusteId, NavigationBuilder.class)
                .buildNavigation(perusteId, kieli);
        return siirraLiitteetLoppuun(navigationNodeDto);
    }

    private NavigationNodeDto siirraLiitteetLoppuun(NavigationNodeDto navigationNodeDto) {
        Stack<NavigationNodeDto> stack = new Stack<>();
        stack.push(navigationNodeDto);

        List<NavigationNodeDto> liitteet = new ArrayList<>();

        while (!stack.empty()) {
            NavigationNodeDto head = stack.pop();

            // Kerätään liitteet talteen
            liitteet.addAll(head.getChildren().stream()
                    .filter(child -> Objects.equals(child.getType(), NavigationType.liite))
                    .collect(Collectors.toList()));

            // Poistetaan liitteet
            head.setChildren(head.getChildren().stream()
                .filter(child -> !Objects.equals(child.getType(), NavigationType.liite))
                .collect(Collectors.toList()));

            // Käydään lävitse myös lapset
            stack.addAll(head.getChildren());
        }

        // Lisätään liitteet loppuun
        navigationNodeDto.getChildren().addAll(liitteet);

        return navigationNodeDto;
    }

    @Override
    public NavigationNodeDto buildNavigation(Long perusteId, String kieli) {
        Peruste peruste = getPeruste(perusteId);
        return self.buildNavigationWithDate(perusteId, peruste.getGlobalVersion().getAikaleima(), kieli);
    }

    @Override
    public NavigationNodeDto buildNavigationPublic(Long perusteId, String kieli, boolean esikatselu) {
        NavigationNodeDto navigationNodeDto = dispatcher.get(perusteId, NavigationBuilderPublic.class)
                .buildNavigation(perusteId, kieli, esikatselu);
        return siirraLiitteetLoppuun(navigationNodeDto);
    }

    @Override
    public List<PerusteTekstikappaleillaDto> findByTekstikappaleKoodi(String koodi) {
        List<TekstiKappale> tekstiKappales = tekstikappaleRepository.findByKooditUri(koodi);
        return tekstikappaleidenPerusteet(tekstiKappales);
    }

    @Override
    public List<PerusteKevytDto> getAllOppaidenPerusteet() {
        List<Peruste> oppaidenPerusteet = perusteRepository.findOppaidenPerusteet();
        return mapper.mapAsList(oppaidenPerusteet, PerusteKevytDto.class);
    }

    private List<PerusteTekstikappaleillaDto> tekstikappaleidenPerusteet(List<TekstiKappale> osat) {

        Map<Peruste, List<TekstiKappale>> tekstikappaleetPerusteilla = new HashMap<>();
        osat.forEach(osa -> {
            List<Long> roots = perusteenOsaViiteRepository.findRootsByPerusteenOsaId(osa.getId());
            Set<Peruste> perusteet = roots.isEmpty() ? Collections.emptySet() : perusteRepository.findPerusteetBySisaltoRoots(roots, PerusteTila.VALMIS);

            perusteet.forEach(peruste -> {
                if (tekstikappaleetPerusteilla.get(peruste) == null) {
                    tekstikappaleetPerusteilla.put(peruste, new ArrayList<>());
                }
                tekstikappaleetPerusteilla.get(peruste).add(osa);
            });

        });

        List<PerusteTekstikappaleillaDto> perusteetTekstikappaleilla = new ArrayList<>();
        tekstikappaleetPerusteilla.forEach((peruste, tekstikappaleet) -> {
            perusteetTekstikappaleilla.add(PerusteTekstikappaleillaDto.builder()
                    .peruste(mapper.map(peruste, PerusteDto.class))
                    .tekstikappeet(mapper.mapAsList(tekstikappaleet, TekstiKappaleDto.class))
                    .build());
        });

        return perusteetTekstikappaleilla;
    }
}
