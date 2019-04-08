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

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import fi.vm.sade.eperusteet.domain.*;
import fi.vm.sade.eperusteet.domain.liite.Liite;
import fi.vm.sade.eperusteet.domain.tutkinnonosa.TutkinnonOsa;
import fi.vm.sade.eperusteet.domain.tutkinnonrakenne.RakenneModuuli;
import fi.vm.sade.eperusteet.domain.tutkinnonrakenne.TutkinnonOsaViite;
import fi.vm.sade.eperusteet.domain.validation.ValidointiStatus;
import fi.vm.sade.eperusteet.domain.yl.Koodillinen;
import fi.vm.sade.eperusteet.domain.yl.Nimetty;
import fi.vm.sade.eperusteet.domain.yl.Oppiaine;
import fi.vm.sade.eperusteet.domain.yl.lukio.LukioOpetussuunnitelmaRakenne;
import fi.vm.sade.eperusteet.domain.yl.lukio.LukiokoulutuksenPerusteenSisalto;
import fi.vm.sade.eperusteet.domain.yl.lukio.Lukiokurssi;
import fi.vm.sade.eperusteet.dto.*;
import fi.vm.sade.eperusteet.dto.kayttaja.KayttajanProjektitiedotDto;
import fi.vm.sade.eperusteet.dto.kayttaja.KayttajanTietoDto;
import fi.vm.sade.eperusteet.dto.koodisto.KoodistoKoodiDto;
import fi.vm.sade.eperusteet.dto.peruste.PerusteVersionDto;
import fi.vm.sade.eperusteet.dto.peruste.PerusteenOsaTyoryhmaDto;
import fi.vm.sade.eperusteet.dto.peruste.PerusteprojektiQueryDto;
import fi.vm.sade.eperusteet.dto.perusteprojekti.*;
import fi.vm.sade.eperusteet.dto.tutkinnonosa.TutkinnonOsaDto;
import fi.vm.sade.eperusteet.dto.util.CombinedDto;
import fi.vm.sade.eperusteet.dto.Reference;
import fi.vm.sade.eperusteet.dto.util.LokalisoituTekstiDto;
import fi.vm.sade.eperusteet.dto.validointi.ValidationDto;
import fi.vm.sade.eperusteet.repository.*;
import fi.vm.sade.eperusteet.repository.liite.LiiteRepository;
import fi.vm.sade.eperusteet.service.*;
import fi.vm.sade.eperusteet.service.dokumentti.DokumenttiService;
import fi.vm.sade.eperusteet.service.event.aop.IgnorePerusteUpdateCheck;
import fi.vm.sade.eperusteet.service.exception.BusinessRuleViolationException;
import fi.vm.sade.eperusteet.service.exception.DokumenttiException;
import fi.vm.sade.eperusteet.service.mapping.Dto;
import fi.vm.sade.eperusteet.service.mapping.DtoMapper;
import fi.vm.sade.eperusteet.service.mapping.KayttajanTietoParser;
import fi.vm.sade.eperusteet.service.util.Pair;
import fi.vm.sade.eperusteet.service.util.RestClientFactory;
import fi.vm.sade.javautils.http.OphHttpClient;
import fi.vm.sade.javautils.http.OphHttpEntity;
import fi.vm.sade.javautils.http.OphHttpRequest;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.entity.ContentType;
import org.apache.tika.Tika;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static fi.vm.sade.eperusteet.domain.ProjektiTila.*;
import static fi.vm.sade.eperusteet.dto.util.LokalisoituTekstiDto.localized;
import static fi.vm.sade.eperusteet.resource.peruste.LiitetiedostoController.DOCUMENT_TYPES;
import static fi.vm.sade.eperusteet.service.util.Util.*;
import static java.util.stream.Collectors.toMap;
import static javax.servlet.http.HttpServletResponse.SC_OK;
import static javax.servlet.http.HttpServletResponse.SC_UNAUTHORIZED;

/**
 *
 * @author harrik
 */
@Slf4j
@Service
public class PerusteprojektiServiceImpl implements PerusteprojektiService {

    private static final String HENKILO_YHTEYSTIEDOT_API = "/s2s/henkilo/yhteystiedot";

    final Tika tika = new Tika();

    @Value("${cas.service.oppijanumerorekisteri-service:''}")
    private String onrServiceUrl;

    @Autowired
    @Dto
    private DtoMapper mapper;

    private ObjectMapper omapper = new ObjectMapper();

    @Autowired
    private LiiteRepository liiteRepository;

    @Autowired
    private PerusteprojektiRepository repository;

    @Autowired
    private PerusteRepository perusteRepository;

    @Autowired
    private PerusteService perusteService;

    @Autowired
    private KayttajanTietoService kayttajanTietoService;

    @Autowired
    private RestClientFactory restClientFactory;

    @Autowired
    private PerusteprojektiTyoryhmaRepository perusteprojektiTyoryhmaRepository;

    @Autowired
    private PerusteenOsaTyoryhmaRepository perusteenOsaTyoryhmaRepository;

    @Autowired
    private PerusteenOsaRepository perusteenOsaRepository;

    @Autowired
    private PerusteenOsaViiteRepository perusteenOsaViiteRepository;

    @Autowired
    private TutkinnonOsaViiteRepository tutkinnonOsaViiteRepository;

    @Autowired
    private KoodistoClient koodistoService;

    @Autowired
    private DokumenttiService dokumenttiService;

    @Autowired
    private TiedoteService tiedoteService;

    @Autowired
    private ValidointiStatusRepository validointiStatusRepository;

    @Autowired
    private KoulutuskoodiStatusRepository koulutuskoodiStatusRepository;

    @Autowired
    private MaarayskirjeStatusRepository maarayskirjeStatusRepository;

    @Autowired
    private ProjektiValidator projektiValidator;

    @Autowired
    private KoodistoClient koodistoClient;

    @Autowired
    private TutkintonimikeKoodiService tutkintonimikeKoodiService;

    @Autowired
    private PlatformTransactionManager tm;

    @Autowired
    private LocalizedMessagesService messages;

    @Override
    @Transactional(readOnly = true)
    public List<PerusteprojektiInfoDto> getBasicInfo() {
        return mapper.mapAsList(repository.findAll(), PerusteprojektiInfoDto.class);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PerusteprojektiKevytDto> getKevytBasicInfo() {
        return repository.findAll().stream()
                .map(pp -> {
                    Peruste peruste = pp.getPeruste();
                    PerusteprojektiKevytDto ppk = mapper.map(pp, PerusteprojektiKevytDto.class);
                    if (ppk != null && peruste != null) {
                        String pdiaari = peruste.getDiaarinumero() != null ? peruste.getDiaarinumero().toString() : null;
                        ppk.setPerusteendiaarinumero(pdiaari);
                        ppk.setKoulutustyyppi(peruste.getKoulutustyyppi());
                        ppk.setTyyppi(peruste.getTyyppi());
                        ppk.setSuoritustavat(peruste.getSuoritustavat().stream()
                                .map(Suoritustapa::getSuoritustapakoodi)
                                .map(Suoritustapakoodi::toString)
                                .collect(Collectors.toSet()));
                        if (peruste.getGlobalVersion() != null) {
                            ppk.setGlobalVersion(mapper.map(peruste.getGlobalVersion(), PerusteVersionDto.class));
                        }
                    }
                    return ppk;
                })
                .collect(Collectors.toList());
    }

    @Override
    @IgnorePerusteUpdateCheck
    @Transactional(propagation = Propagation.NEVER)
    public void validoiPerusteetTask() {

        // Haetaan validoitavat projektit
        TransactionTemplate template = new TransactionTemplate(tm);
        List<PerusteprojektiValidointiDto> projektit = template.execute(status -> mapper.mapAsList(Stream
                .concat(
                        repository.findAllValidoimattomat().stream(),
                        repository.findAllValidoimattomatUudet().stream()
                )
                .collect(Collectors.toList()), PerusteprojektiValidointiDto.class));

        log.debug("Tarkastetaan " + projektit.size() + " perustetta.");

        int counter = 1;

        for (PerusteprojektiValidointiDto pp : projektit) {
            try {
                if (pp.getPeruste().getTyyppi() != PerusteTyyppi.NORMAALI) {
                    continue;
                }

                validoiPerusteTask(pp, counter);

            } catch (RuntimeException e) {
                log.error(e.getLocalizedMessage(), e);
            }
            counter++;
        }
    }

    @Transactional(propagation = Propagation.NEVER)
    private void validoiPerusteTask(PerusteprojektiValidointiDto pp, int counter) {

        TransactionTemplate template = new TransactionTemplate(tm);

        template.execute(status -> {

            Peruste peruste = perusteRepository.findOne(pp.getPeruste().getId());
            ValidointiStatus vs = validointiStatusRepository.findOneByPeruste(peruste);
            boolean vaatiiValidoinnin = vs == null
                    || !vs.isVaihtoOk()
                    || peruste.getGlobalVersion().getAikaleima().after(vs.getLastCheck());

            if (!vaatiiValidoinnin) {
                return true;
            }

            log.debug(String.format("%04d", counter) + " Perusteen ajastettu validointi: " + peruste.getId());

            TilaUpdateStatus tilaUpdateStatus = projektiValidator.run(pp.getId(), JULKAISTU);

            if (vs != null) {
                mapper.map(tilaUpdateStatus, vs);
            }
            else {
                vs = mapper.map(tilaUpdateStatus, ValidointiStatus.class);
            }

            vs.setPeruste(peruste);
            vs.setLastCheck(peruste.getGlobalVersion().getAikaleima());

            validointiStatusRepository.save(vs);

            return true;
        });
    }

    @Override
    @IgnorePerusteUpdateCheck
    @Transactional(propagation = Propagation.NEVER)
    public void tarkistaKooditTask() {

        // Haetaan tarkistettavat projektit
        TransactionTemplate template = new TransactionTemplate(tm);
        List<PerusteprojektiKoulutuskoodiDto> projektit = template.execute(status -> mapper.mapAsList(Stream
                .concat(
                        repository.findAllKoodiValidoimattomat().stream(),
                        repository.findAllKoodiValidoimattomatUudet().stream()
                )
                .collect(Collectors.toList()), PerusteprojektiKoulutuskoodiDto.class));

        log.debug("Tarkastetaan " + projektit.size() + " perusteen koulutuskoodit.");

        int counter = 1;

        for (PerusteprojektiKoulutuskoodiDto pp : projektit) {
            try {

                tarkistaKoodi(pp, counter);

            } catch (RuntimeException e) {
                log.error(e.getLocalizedMessage(), e);
            }

            counter++;
        }
    }

    @Transactional(propagation = Propagation.NEVER)
    private void tarkistaKoodi(PerusteprojektiKoulutuskoodiDto pp, int counter) {

        TransactionTemplate template = new TransactionTemplate(tm);

        template.execute(status -> {
            Peruste peruste = perusteRepository.findOne(pp.getPeruste().getId());
            KoulutuskoodiStatus koulutuskoodiStatus = koulutuskoodiStatusRepository.findOneByPeruste(peruste);
            boolean vaatiiTarkistuksen = koulutuskoodiStatus == null
                    || !koulutuskoodiStatus.isKooditOk()
                    || peruste.getGlobalVersion().getAikaleima().after(koulutuskoodiStatus.getLastCheck());

            if (!vaatiiTarkistuksen) {
                return false;
            }

            if (koulutuskoodiStatus == null) {
                koulutuskoodiStatus = new KoulutuskoodiStatus();
            }

            tarkistaTutkinnonKoodit(peruste, koulutuskoodiStatus, counter);

            koulutuskoodiStatusRepository.save(koulutuskoodiStatus);

            return true;
        });
    }

    private void tarkistaTutkinnonKoodit(Peruste p, KoulutuskoodiStatus status, int counter) {
        status.setLastCheck(new Date());
        status.setPeruste(p);

        Set<Koulutus> koulutuskoodit = p.getKoulutukset();

        if (p.getSuoritustavat() != null && p.getSuoritustavat().size() > 0) {
            log.debug(String.format("%04d", counter) + " Tarkistetaan perustetta: " + p.getNimi().toString());

            log.debug("  Käydään lävitse tutkinnon osat suoritustapa kerrallaan.");
            for (Suoritustapa st : p.getSuoritustavat()) {
                log.debug("  Tarkistetaan suoritustapa: " + st.getSuoritustapakoodi());

                List<TutkinnonOsaViite> viitteet = mapper.mapAsList(perusteService.getTutkinnonOsat(p.getId(),
                        st.getSuoritustapakoodi()), TutkinnonOsaViite.class);

                List<Pair<Koodi, TutkinnonOsaViite>> koodit = new ArrayList<>();
                for (TutkinnonOsaViite viite : viitteet) {
                    TutkinnonOsa tutkinnonOsa = viite.getTutkinnonOsa();
                    if (tutkinnonOsa != null) {
                        Koodi koodi = tutkinnonOsa.getKoodi();
                        if (koodi != null) {
                            koodit.add(Pair.of(koodi, viite));
                        }
                    }
                }

                if (koodit.size() > 0) {
                    log.debug("    Tutkinnon osien koodit:");

                    for (Pair<Koodi, TutkinnonOsaViite> pari : koodit) {
                        Koodi koodi = pari.getFirst();
                        TutkinnonOsaViite viite = pari.getSecond();

                        log.debug("    - " + koodi.getUri());

                        List<KoodistoKoodiDto> ylarelaatiot = getKoulutukset(koodi.getUri());

                        boolean koodiOk = false;


                        // Katsotaan ensiksi, löytyykä koulutus suoraan ylärelaatiosta
                        if (hasKoulutus(koulutuskoodit, ylarelaatiot)) {
                            // Tarkistetaan ylärelaatiot
                            log.debug("    On linkitetty suoraan.");
                            koodiOk = true;
                            return;
                        } else {
                            // Tarkistetaan rinnasteiden ylärelaatiot (syvyys = 1)
                            List<KoodistoKoodiDto> rinnasteiset = koodistoClient.getRinnasteiset(koodi.getUri());
                            for (KoodistoKoodiDto rinnasteinen : rinnasteiset) {
                                List<KoodistoKoodiDto> koulutukset = getKoulutukset(rinnasteinen.getKoodiUri());

                                if (hasKoulutus(koulutuskoodit, koulutukset)) {
                                    // Tutkinnon osan koodi on linkitetty ainakin yhteen koulutuskoodiin
                                    log.debug("    On linkitetty rinnasteisen.");
                                    koodiOk = true;
                                }
                            }
                        }

                        if (!koodiOk) {
                            addStatusInfo(status, st, viite);
                            status.setKooditOk(false);
                        } else {
                            status.setInfot(new ArrayList<>());
                            status.setKooditOk(true);
                        }
                    }
                } else {
                    log.debug("    Yhtään tutkinnon osan koodia ei löytynyt.");
                }
            }
        }
    }

    private void addStatusInfo(KoulutuskoodiStatus status, Suoritustapa st, TutkinnonOsaViite viite) {
        KoulutuskoodiStatusInfo info = new KoulutuskoodiStatusInfo();
        info.setSuoritustapa(st.getSuoritustapakoodi());
        info.setViite(viite);

        if (!status.getInfot().contains(info)) {
            status.getInfot().add(info);
        }
    }

    private boolean hasKoulutus(Set<Koulutus> koulutuskoodit, List<KoodistoKoodiDto> koulutukset) {
        for (KoodistoKoodiDto koodi : koulutukset) {
            for (Koulutus koulutus : koulutuskoodit) {
                String koodiUri = koodi.getKoodiUri();
                String koulutuskoodiUri = koulutus.getKoulutuskoodiUri();
                if (Objects.equals(koodiUri, koulutuskoodiUri)) {
                    return true;
                }
            }
        }
        return false;
    }

    private String getKoodisto(KoodistoKoodiDto koodi) {
        if (koodi != null && koodi.getKoodisto() != null) {
            return koodi.getKoodisto().getKoodistoUri();
        }

        return null;
    }

    private List<KoodistoKoodiDto> getKoulutukset(String koodiUri) {
        return koodistoClient.getYlarelaatio(koodiUri).stream()
                .filter(koodi -> "koulutus".equals(getKoodisto(koodi)))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PerusteprojektiKevytDto> findBy(PageRequest page, PerusteprojektiQueryDto query) {
        Page<PerusteprojektiKevytDto> result = repository.findBy(page, query).map(pp -> {
            PerusteprojektiKevytDto ppk = mapper.map(pp, PerusteprojektiKevytDto.class);
            Peruste peruste = pp.getPeruste();
            if (ppk != null && peruste != null) {
                String pdiaari = peruste.getDiaarinumero() != null ? peruste.getDiaarinumero().toString() : null;
                ppk.setPerusteendiaarinumero(pdiaari);
                ppk.setKoulutustyyppi(peruste.getKoulutustyyppi());
                ppk.setTyyppi(peruste.getTyyppi());
                ppk.setSuoritustavat(peruste.getSuoritustavat().stream()
                        .map(Suoritustapa::getSuoritustapakoodi)
                        .map(Suoritustapakoodi::toString)
                        .collect(Collectors.toSet()));
                if (peruste.getGlobalVersion() != null) {
                    ppk.setGlobalVersion(mapper.map(peruste.getGlobalVersion(), PerusteVersionDto.class));
                }
            }
            return ppk;
        });

        return result;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ValidationDto> getVirheelliset(PageRequest p) {
        Page<ValidointiStatus> virheelliset = validointiStatusRepository.findVirheelliset(p);
        Page<ValidationDto> result = virheelliset
                .map(validation -> {
                    ValidationDto dto = mapper.map(validation, ValidationDto.class);
                    dto.setPerusteprojekti(mapper.map(validation.getPeruste().getPerusteprojekti(), PerusteprojektiListausDto.class));
                    return dto;
                });
        return result;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<KoulutuskoodiStatusDto> getKoodiongelmat(PageRequest p) {
        Page<KoulutuskoodiStatus> ongelmalliset = koulutuskoodiStatusRepository.findOngelmalliset(p);
        return ongelmalliset.map(status -> {
            KoulutuskoodiStatusDto dto = mapper.map(status, KoulutuskoodiStatusDto.class);
            dto.setPerusteprojekti(mapper.map(status.getPeruste().getPerusteprojekti(), PerusteprojektiListausDto.class));
            return dto;
        });
    }

    @Override
    @Transactional(readOnly = true)
    @PreAuthorize("isAuthenticated()")
    public List<PerusteprojektiListausDto> getOmatProjektit() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        Set<String> orgs = authentication.getAuthorities().stream()
                .filter(Objects::nonNull)
                .map(GrantedAuthority::getAuthority)
                .filter(Objects::nonNull)
                .map(x -> x.split("_"))
                .filter(x -> x.length > 0)
                .map(x -> x[x.length - 1])
                .collect(Collectors.toSet());
        String user = authentication.getName();
        return mapper.mapAsList(repository.findOmatPerusteprojektit(user, orgs), PerusteprojektiListausDto.class);
    }

    @Override
    @Transactional(readOnly = true)
    public PerusteprojektiDto get(Long id) {
        Perusteprojekti p = repository.findOne(id);
        return mapper.map(p, PerusteprojektiDto.class);
    }

    @Override
    @Transactional(readOnly = true)
    public List<KayttajanTietoDto> getJasenet(Long id) {
        Perusteprojekti p = repository.findOne(id);

        if (p == null || ObjectUtils.isEmpty(p.getRyhmaOid())) {
            throw new BusinessRuleViolationException("Perusteprojektilla ei ole oid:a");
        }

        String ryhmaOid = p.getRyhmaOid();

        OphHttpClient client = restClientFactory.get(onrServiceUrl, true);

        String url = onrServiceUrl + HENKILO_YHTEYSTIEDOT_API;

        OphHttpRequest request = OphHttpRequest.Builder
                .post(url)
                .setEntity(new OphHttpEntity.Builder()
                        .content("{ \"organisaatioOids\": [\"" + ryhmaOid + "\"] }")
                        .contentType(ContentType.APPLICATION_JSON)
                        .build())
                .build();

        return client.<List<KayttajanTietoDto>>execute(request)
                .handleErrorStatus(SC_UNAUTHORIZED)
                .with(res -> Optional.empty())
                .expectedStatus(SC_OK)
                .mapWith(res -> {
                    try {
                        JsonNode jsonNode = omapper.readTree(res);
                        return KayttajanTietoParser.parsiKayttajat(jsonNode);
                    } catch (IOException ex) {
                        throw new BusinessRuleViolationException("Käyttäjien tietojen hakeminen epäonnistui");
                    }
                }).orElse(new ArrayList<>());
    }

    @Override
    @Transactional(readOnly = true)
    public List<CombinedDto<KayttajanTietoDto, KayttajanProjektitiedotDto>> getJasenetTiedot(Long id) {
        List<CombinedDto<KayttajanTietoDto, KayttajanProjektitiedotDto>> kayttajat = new ArrayList<>();
        Perusteprojekti p = repository.findOne(id);

        if (p == null || ObjectUtils.isEmpty(p.getRyhmaOid())) {
            throw new BusinessRuleViolationException("Perusteprojektilla ei ole oid:a");
        }

        String ryhmaOid = p.getRyhmaOid();

        // Ryhmä liian suuri haulle
        if (ryhmaOid.equals("1.2.246.562.10.00000000001")) {
            return kayttajat;
        }

        OphHttpClient client = restClientFactory.get(onrServiceUrl, true);

        String url = onrServiceUrl + HENKILO_YHTEYSTIEDOT_API;

        OphHttpRequest request = OphHttpRequest.Builder
                .post(url)
                .setEntity(new OphHttpEntity.Builder()
                        .content("{ \"organisaatioOids\": [\"" + ryhmaOid + "\"] }")
                        .contentType(ContentType.APPLICATION_JSON)
                        .build())
                .build();

        client.<JsonNode>execute(request)
                .handleErrorStatus(SC_UNAUTHORIZED)
                .with(res -> Optional.empty())
                .expectedStatus(SC_OK)
                .mapWith(res -> {
                    try {
                        return omapper.readTree(res);
                    } catch (IOException ex) {
                        // throw new BusinessRuleViolationException("Käyttäjien tietojen hakeminen epäonnistui");
                        return null;
                    }
                }).ifPresent(tree -> {
                    for (JsonNode node : tree) {
                        String oid = node.get("oidHenkilo").asText();
                        // Todo: Tämä on erittäin hidas jos lista on iso
                        KayttajanTietoDto kayttaja = kayttajanTietoService.hae(oid);
                        KayttajanProjektitiedotDto kayttajanProjektitiedot = kayttajanTietoService.haePerusteprojekti(oid, id);

                        if (kayttaja != null && kayttajanProjektitiedot != null) {
                            CombinedDto<KayttajanTietoDto, KayttajanProjektitiedotDto> combined = new CombinedDto<>(
                                    kayttaja,
                                    kayttajanProjektitiedot
                            );
                            kayttajat.add(combined);
                        }
                    }
                });

        return kayttajat;
    }

    @Override
    @Transactional
    public PerusteprojektiDto save(PerusteprojektiLuontiDto perusteprojektiDto) {
        Perusteprojekti perusteprojekti = mapper.map(perusteprojektiDto, Perusteprojekti.class);

        KoulutusTyyppi koulutustyyppi = KoulutusTyyppi.of(perusteprojektiDto.getKoulutustyyppi());
        if (koulutustyyppi != null && !koulutustyyppi.isAmmatillinen()) {
            perusteprojektiDto.setReforminMukainen(false);
        }

        LaajuusYksikko yksikko = perusteprojektiDto.getLaajuusYksikko();
        PerusteTyyppi tyyppi = perusteprojektiDto.getTyyppi() == null ? PerusteTyyppi.NORMAALI : perusteprojektiDto.getTyyppi();
        perusteprojekti.setTila(LAADINTA);
        perusteprojekti.setRyhmaOid(perusteprojektiDto.getRyhmaOid());

        perusteprojektiDto.setReforminMukainen(
                perusteprojektiDto.isReforminMukainen()
                        && KoulutusTyyppi.of(perusteprojektiDto.getKoulutustyyppi()).isAmmatillinen());

        if (tyyppi == PerusteTyyppi.OPAS) {
            throw new BusinessRuleViolationException("Virheellinen perustetyyppi");
        }

        if (tyyppi != PerusteTyyppi.POHJA) {
            if (koulutustyyppi == null) {
                throw new BusinessRuleViolationException("Opetussuunnitelmalla täytyy olla koulutustyyppi");
            }

            if (yksikko == null && koulutustyyppi
                    .isOneOf(KoulutusTyyppi.PERUSTUTKINTO,
                            KoulutusTyyppi.AMMATTITUTKINTO,
                            KoulutusTyyppi.ERIKOISAMMATTITUTKINTO,
                            KoulutusTyyppi.TELMA,
                            KoulutusTyyppi.VALMA)) {
                throw new BusinessRuleViolationException("Opetussuunnitelmalla täytyy olla yksikkö");
            }

            if (perusteprojektiDto.getDiaarinumero() == null) {
                throw new BusinessRuleViolationException("Diaarinumeroa ei ole asetettu");
            }

            DiaarinumeroHakuDto diaariHaku = onkoDiaarinumeroKaytossa(new Diaarinumero(perusteprojektiDto.getDiaarinumero()));
            boolean korvaava = diaariHaku.getTila() == ProjektiTila.JULKAISTU && diaariHaku.getLoytyi();

            if (korvaava) {
                Perusteprojekti jyrattava = repository.findOne(diaariHaku.getId());
                perusteprojekti.setPaatosPvm(jyrattava.getPaatosPvm());
                perusteprojekti.setToimikausiAlku(jyrattava.getToimikausiAlku());
                perusteprojekti.setToimikausiLoppu(jyrattava.getToimikausiLoppu());
            }
        }

        if (perusteprojektiDto.getRyhmaOid() == null) {
            throw new BusinessRuleViolationException("Perustetyöryhmä ei ole asetettu");
        }

        Peruste peruste;
        if (perusteprojektiDto.getPerusteId() == null) {
            peruste = perusteService.luoPerusteRunko(koulutustyyppi, perusteprojektiDto.getToteutus(),
                    yksikko, tyyppi, perusteprojektiDto.isReforminMukainen());
        } else {
            Peruste pohjaPeruste = perusteRepository.findOne(perusteprojektiDto.getPerusteId());
            if (pohjaPeruste == null) {
                throw new BusinessRuleViolationException("perustetta-ei-olemassa");
            }
            perusteprojektiDto.setKoulutustyyppi(pohjaPeruste.getKoulutustyyppi());
            peruste = perusteService.luoPerusteRunkoToisestaPerusteesta(perusteprojektiDto, tyyppi);
        }

        if (KoulutusTyyppi.of(peruste.getKoulutustyyppi()).isAmmatillinen()) {
            KVLiite kvliite = new KVLiite();
            if (perusteprojektiDto.getPerusteId() != null) {
                Peruste pohja = perusteRepository.findOne(perusteprojektiDto.getPerusteId());
                if (pohja != null) {
                    kvliite.setPohja(pohja.getKvliite());
                }
            }
            peruste.setKvliite(kvliite);
        }

        if (tyyppi == PerusteTyyppi.POHJA) {
            TekstiPalanen pnimi = TekstiPalanen.of(Kieli.FI, perusteprojektiDto.getNimi());
            peruste.setNimi(pnimi);
        }

        perusteprojekti.setPeruste(peruste);
        perusteprojekti = repository.saveAndFlush(perusteprojekti);

        return mapper.map(perusteprojekti, PerusteprojektiDto.class);
    }

    @Override
    @Transactional(readOnly = true)
    public DiaarinumeroHakuDto onkoDiaarinumeroKaytossa(Diaarinumero diaarinumero) {
        DiaarinumeroHakuDto reply = new DiaarinumeroHakuDto();
        reply.setLoytyi(false);
        List<Perusteprojekti> perusteprojektit = repository.findByDiaarinumero(diaarinumero);

        if (perusteprojektit.isEmpty()) {
            return reply;
        }

        Perusteprojekti pp = perusteprojektit.get(0);

        for (Perusteprojekti p : perusteprojektit) {
            if (p.getTila() == ProjektiTila.JULKAISTU) {
                pp = p;
                break;
            }
            if (p.getMuokattu().after(pp.getMuokattu())) {
                pp = p;
            }
        }

        reply.setId(pp.getId());
        reply.setLoytyi(true);
        reply.setTila(pp.getTila());
        reply.setDiaarinumero(pp.getDiaarinumero().getDiaarinumero());
        return reply;
    }

    @Override
    @Transactional
    public PerusteprojektiDto update(Long id, PerusteprojektiDto perusteprojektiDto) {
        Perusteprojekti vanhaProjekti = repository.findOne(id);
        if (vanhaProjekti == null) {
            throw new BusinessRuleViolationException("Projektia ei ole olemassa id:llä: " + id);
        }

        perusteprojektiDto.setId(id);
        perusteprojektiDto.setTila(vanhaProjekti.getTila());
        Perusteprojekti perusteprojekti = mapper.map(perusteprojektiDto, Perusteprojekti.class);
        perusteprojekti.setPeruste(vanhaProjekti.getPeruste());
        perusteprojekti = repository.save(perusteprojekti);

        return mapper.map(perusteprojekti, PerusteprojektiDto.class);
    }

    @Override
    @Transactional(readOnly = true)
    public Set<ProjektiTila> getTilat(Long id) {
        Perusteprojekti p = repository.findOne(id);
        if (p == null) {
            throw new BusinessRuleViolationException("Projektia ei ole olemassa id:llä: " + id);
        }

        return p.getTila().mahdollisetTilat(p.getPeruste().getTyyppi());
    }

    @Override
    public OmistajaDto isOwner(Long id, Long perusteenOsaId) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    @IgnorePerusteUpdateCheck
    @Transactional
    public TilaUpdateStatus validoiProjekti(Long id, ProjektiTila tila) {
        return projektiValidator.run(id, tila);
    }


    @Override
    @IgnorePerusteUpdateCheck
    @Transactional
    public TilaUpdateStatus updateTila(Long id, ProjektiTila tila, TiedoteDto tiedoteDto) {

        TilaUpdateStatus updateStatus = validoiProjekti(id, tila);

        // Perusteen tilan muutos
        if (!updateStatus.isVaihtoOk()) {
            return updateStatus;
        }

        Perusteprojekti projekti = repository.findOne(id);
        Peruste peruste = projekti.getPeruste();

        // Tarkistetaan mahdolliset tilat
        updateStatus.setVaihtoOk(projekti.getTila().mahdollisetTilat(projekti.getPeruste().getTyyppi()).contains(tila));
        if (!updateStatus.isVaihtoOk()) {
            String viesti = "Tilasiirtymä tilasta '" + projekti.getTila().toString() + "' tilaan '"
                    + tila.toString() + "' ei mahdollinen";
            updateStatus.addStatus(viesti);
            return updateStatus;
        }

        // Dokumentit generoidaan automaattisesti julkaisun yhteydessä
        if (tila == ProjektiTila.JULKAISTU && projekti.getTila() == ProjektiTila.VALMIS) {
            setPerusteTila(projekti.getPeruste(), PerusteTila.VALMIS);

            // EP-1357 Julkaisun yhteydessä on pakko tehdä tiedote
            if (tiedoteDto == null) {
                throw new BusinessRuleViolationException("Julkaisun yhteydessä täytyy tehdä tiedoite");
            }
            tiedoteDto.setId(null);
            tiedoteDto.setJulkinen(true);
            tiedoteDto.setPerusteprojekti(new Reference(projekti.getId()));
            tiedoteService.addTiedote(tiedoteDto);

            Optional.of(peruste)
                    .ifPresent(p -> p.getSuoritustavat()
                            .forEach(suoritustapa -> p.getKielet()
                                    .forEach(kieli -> {
                                        try {
                                            DokumenttiDto createDtoFor = dokumenttiService.createDtoFor(
                                                    p.getId(),
                                                    kieli,
                                                    suoritustapa.getSuoritustapakoodi(),
                                                    GeneratorVersion.UUSI
                                            );
                                            dokumenttiService.setStarted(createDtoFor);
                                            dokumenttiService.generateWithDto(createDtoFor);
                                        } catch (DokumenttiException e) {
                                            log.error(e.getLocalizedMessage(), e);
                                        }
                                    })));
        }

        if (tila == ProjektiTila.POISTETTU) {
            if (PerusteTyyppi.POHJA.equals(projekti.getPeruste().getTyyppi())) {
                projekti.setTila(ProjektiTila.POISTETTU);
                projekti.getPeruste().asetaTila(PerusteTila.POISTETTU);
            }
            else {
                setPerusteTila(projekti.getPeruste(), PerusteTila.POISTETTU);
            }
        }

        if (tila == LAADINTA) {
            if (PerusteTyyppi.POHJA.equals(projekti.getPeruste().getTyyppi())) {
                projekti.setTila(ProjektiTila.LAADINTA);
                projekti.getPeruste().asetaTila(PerusteTila.LUONNOS);
            }
            else {
                setPerusteTila(projekti.getPeruste(), PerusteTila.LUONNOS);
            }
        }

        if (projekti.getPeruste().getTyyppi() == PerusteTyyppi.POHJA
                && tila == ProjektiTila.VALMIS
                && projekti.getTila() == LAADINTA) {
            setPerusteTila(projekti.getPeruste(), PerusteTila.VALMIS);
        }

        projekti.setTila(tila);
        repository.save(projekti);
        return updateStatus;
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

    private void setPerusteTila(Peruste peruste, PerusteTila tila) {

        // Asetetaan perusteen osien tilat
        for (Suoritustapa suoritustapa : peruste.getSuoritustavat()) {
            setSisaltoTila(peruste, suoritustapa.getSisalto(), tila);
            for (TutkinnonOsaViite tutkinnonosaViite : suoritustapa.getTutkinnonOsat()) {
                setOsatTila(peruste, tutkinnonosaViite, tila);
            }
        }

        if (peruste.getPerusopetuksenPerusteenSisalto() != null) {
            setSisaltoTila(peruste, peruste.getPerusopetuksenPerusteenSisalto().getSisalto(), tila);
        }

        if (peruste.getEsiopetuksenPerusteenSisalto() != null) {
            setSisaltoTila(peruste, peruste.getEsiopetuksenPerusteenSisalto().getSisalto(), tila);
        }

        if (peruste.getLukiokoulutuksenPerusteenSisalto() != null) {
            setSisaltoTila(peruste, peruste.getLukiokoulutuksenPerusteenSisalto().getSisalto(), tila);
        }

        if (peruste.getAipeOpetuksenPerusteenSisalto() != null) {
            setSisaltoTila(peruste, peruste.getAipeOpetuksenPerusteenSisalto().getSisalto(), tila);
        }

        peruste.asetaTila(tila);
    }

    @Transactional
    private void palautaJulkaistuImpl(Peruste peruste, PerusteenOsa po, Long povId) {
        // Tarkistetaan omistaako palautettava peruste, jos on palautetaan se luonnokseksi
        peruste.getSuoritustavat()
                .forEach(st -> st.getTutkinnonOsat().stream()
                        .map(TutkinnonOsaViite::getId)
                        .filter(id -> id.equals(povId))
                        .findFirst()
                        .ifPresent(x -> {
                            po.palautaLuonnokseksi();
                        }));
    }

    @Transactional
    private Set<Peruste> perusteetJoissaJulkaistuna(PerusteenOsa osa) {
        if (osa instanceof TutkinnonOsa) {
            return tutkinnonOsaViiteRepository.findAllByTutkinnonOsa((TutkinnonOsa)osa).stream()
                    .map(TutkinnonOsaViite::getSuoritustapa)
                    .filter(Objects::nonNull)
                    .map(Suoritustapa::getPerusteet)
                    .flatMap(Collection::stream)
                    .filter(peruste -> peruste.getTila() == PerusteTila.VALMIS)
                    .collect(Collectors.toSet());
        }
        else {
            return perusteenOsaViiteRepository.findAllByPerusteenOsa(osa).stream()
                    .map(pov -> {
                        PerusteenOsaViite result = pov;
                        while (result.getVanhempi() != null) {
                            result = result.getVanhempi();
                        }
                        return result;
                    })
                    .map(PerusteenOsaViite::getSuoritustapa)
                    .filter(Objects::nonNull)
                    .map(Suoritustapa::getPerusteet)
                    .flatMap(Collection::stream)
                    .filter(peruste -> peruste.getTila() == PerusteTila.VALMIS)
                    .collect(Collectors.toSet());
        }
    }

    @Transactional
    private void palautaJulkaistu(Peruste peruste, PerusteenOsa po) {
        if (po instanceof TutkinnonOsa) {
            tutkinnonOsaViiteRepository.findAllByTutkinnonOsa((TutkinnonOsa)po).stream()
                    .map(TutkinnonOsaViite::getId)
                    .sorted()
                    .findFirst()
                    .ifPresent(id -> palautaJulkaistuImpl(peruste, po, id));
        }
        else {
            perusteenOsaViiteRepository.findAllByPerusteenOsa(po).stream()
                    .map(PerusteenOsaViite::getId)
                    .sorted()
                    .findFirst()
                    .ifPresent(id -> palautaJulkaistuImpl(peruste, po, id));
        }
    }

    private PerusteenOsaViite setSisaltoTila(Peruste peruste, PerusteenOsaViite sisaltoRoot, PerusteTila tila) {
        // Perusteen osan tilan poistaminen valmiista edellyttää ettei mikään muu perusteen osaa käyttävä peruste
        // ole julkaistuna.
        if (sisaltoRoot.getPerusteenOsa() != null) {
            boolean salliTilamuutos = true;
            if (sisaltoRoot.getPerusteenOsa().getTila() == PerusteTila.VALMIS) {
                Set<Peruste> perusteet = perusteetJoissaJulkaistuna(sisaltoRoot.getPerusteenOsa());
                boolean hasCurrentPeruste = perusteet.contains(peruste);
                if (hasCurrentPeruste) {
                    perusteet.remove(peruste);
                }
                salliTilamuutos = hasCurrentPeruste && perusteet.isEmpty();
            }

            if (salliTilamuutos) {
                if (tila == PerusteTila.LUONNOS) {
                    sisaltoRoot.getPerusteenOsa().palautaLuonnokseksi();
                    palautaJulkaistu(peruste, sisaltoRoot.getPerusteenOsa());
                }
                else {
                    sisaltoRoot.getPerusteenOsa().asetaTila(tila);
                }
            }
        }

        if (sisaltoRoot.getLapset() != null) {
            for (PerusteenOsaViite lapsi : sisaltoRoot.getLapset()) {
                setSisaltoTila(peruste, lapsi, tila);
            }
        }
        return sisaltoRoot;
    }

    private TutkinnonOsaViite setOsatTila(Peruste peruste, TutkinnonOsaViite osa, PerusteTila tila) {
        if (osa.getTutkinnonOsa() != null) {
            // Tutkinnon osan tilan voi alentaa ainoastaan jos kaikki kiinnitetyt
            // perusteet ovat julkaisemattomia
            if (!PerusteTila.VALMIS.equals(tila) && PerusteTila.VALMIS.equals(osa.getTutkinnonOsa().getTila())) {
                Set<Peruste> perusteetJoissaTosa = tutkinnonOsaViiteRepository.findAllByTutkinnonOsa(osa.getTutkinnonOsa()).stream()
                        .map(tosa -> tosa.getSuoritustapa().getPerusteet())
                        .flatMap(Collection::stream)
                        .filter(p -> PerusteTila.VALMIS.equals(p.getTila()))
                        .collect(Collectors.toSet());
                if (perusteetJoissaTosa.size() != 1 || !perusteetJoissaTosa.contains(peruste)) {
                    return osa;
                }
            }

            if (tila == PerusteTila.LUONNOS) {
                palautaJulkaistu(peruste, osa.getTutkinnonOsa());
            }
            else {
                osa.getTutkinnonOsa().asetaTila(tila);
            }
        }
        return osa;
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

    private Collection<TutkinnonOsaViite> getViitteet(Suoritustapa suoritustapa) {
        return suoritustapa.getTutkinnonOsat();
    }

    @Transactional
    @Override
    public List<TyoryhmaHenkiloDto> saveTyoryhma(Long perusteProjektiId, String tyoryhma, List<String> henkilot) {
        Perusteprojekti pp = repository.findOne(perusteProjektiId);
        removeTyoryhma(perusteProjektiId, tyoryhma);
        perusteprojektiTyoryhmaRepository.flush();
        List<PerusteprojektiTyoryhma> res = new ArrayList<>();

        for (String trh : henkilot) {
            res.add(perusteprojektiTyoryhmaRepository.save(new PerusteprojektiTyoryhma(pp, trh, tyoryhma)));
        }
        return mapper.mapAsList(res, TyoryhmaHenkiloDto.class);
    }

    @Transactional
    @Override
    public TyoryhmaHenkiloDto saveTyoryhma(Long perusteProjektiId, TyoryhmaHenkiloDto tr) {
        Perusteprojekti pp = repository.findOne(perusteProjektiId);
        PerusteprojektiTyoryhma ppt = perusteprojektiTyoryhmaRepository.save(new PerusteprojektiTyoryhma(pp,
                tr.getKayttajaOid(), tr.getNimi()));
        return mapper.map(ppt, TyoryhmaHenkiloDto.class);
    }

    @Transactional(readOnly = true)
    @Override
    public List<TyoryhmaHenkiloDto> getTyoryhmaHenkilot(Long perusteProjektiId) {
        Perusteprojekti pp = repository.findOne(perusteProjektiId);
        List<PerusteprojektiTyoryhma> tr = perusteprojektiTyoryhmaRepository.findAllByPerusteprojekti(pp);
        return mapper.mapAsList(tr, TyoryhmaHenkiloDto.class);
    }

    @Transactional(readOnly = true)
    @Override
    public List<TyoryhmaHenkiloDto> getTyoryhmaHenkilot(Long perusteProjektiId, String nimi) {
        Perusteprojekti pp = repository.findOne(perusteProjektiId);
        List<PerusteprojektiTyoryhma> tr = perusteprojektiTyoryhmaRepository.findAllByPerusteprojektiAndNimi(pp, nimi);
        return mapper.mapAsList(tr, TyoryhmaHenkiloDto.class);
    }

    @Transactional
    @Override
    public void removeTyoryhma(Long perusteProjektiId, String nimi) {
        Perusteprojekti pp = repository.findOne(perusteProjektiId);
        perusteprojektiTyoryhmaRepository.deleteAllByPerusteprojektiAndNimi(pp, nimi);
    }

    @Transactional
    @Override
    public List<String> setPerusteenOsaViiteTyoryhmat(Long perusteProjektiId, Long perusteenOsaId, List<String> nimet) {
        Perusteprojekti pp = repository.findOne(perusteProjektiId);
        PerusteenOsa po = perusteenOsaRepository.findOne(perusteenOsaId);
        Set<String> uniques = new HashSet<>(nimet);
        perusteenOsaTyoryhmaRepository.deleteAllByPerusteenosaAndPerusteprojekti(po, pp);
        perusteenOsaTyoryhmaRepository.flush();
        List<String> res = new ArrayList<>();

        for (String nimi : uniques) {
            PerusteenOsaTyoryhma pot = new PerusteenOsaTyoryhma();
            pot.setNimi(nimi);
            pot.setPerusteprojekti(pp);
            pot.setPerusteenosa(po);
            if (perusteprojektiTyoryhmaRepository.findAllByPerusteprojektiAndNimi(pp, nimi).isEmpty()) {
                throw new BusinessRuleViolationException("Perusteprojekti ryhmää ei ole olemassa: " + nimi);
            }
            res.add(perusteenOsaTyoryhmaRepository.save(pot).getNimi());
        }
        return res;
    }

    @Transactional(readOnly = true)
    @Override
    public List<String> getPerusteenOsaViiteTyoryhmat(Long perusteProjektiId, Long perusteenOsaId) {
        Perusteprojekti pp = repository.findOne(perusteProjektiId);
        PerusteenOsa po = perusteenOsaRepository.findOne(perusteenOsaId);
        List<PerusteenOsaTyoryhma> tyoryhmat = perusteenOsaTyoryhmaRepository.findAllByPerusteenosaAndPerusteprojekti(po, pp);
        List<String> res = new ArrayList<>();
        for (PerusteenOsaTyoryhma s : tyoryhmat) {
            res.add(s.getNimi());
        }
        return res;
    }

    @Transactional(readOnly = true)
    @Override
    public List<PerusteenOsaTyoryhmaDto> getSisallonTyoryhmat(Long perusteProjektiId) {
        Perusteprojekti pp = repository.findOne(perusteProjektiId);
        List<PerusteenOsaTyoryhma> tyoryhmat = perusteenOsaTyoryhmaRepository.findAllByPerusteprojekti(pp);
        return mapper.mapAsList(tyoryhmat, PerusteenOsaTyoryhmaDto.class);
    }

    @Override
    @IgnorePerusteUpdateCheck
    @Transactional(propagation = Propagation.NEVER)
    public void lataaMaarayskirjeetTask() {

        // Haetaan maarayskirjeen latausta tarvitsevat projektit
        TransactionTemplate template = new TransactionTemplate(tm);
        List<PerusteprojektiMaarayskirjeDto> projektit = template.execute(status -> mapper.mapAsList(Stream
                .concat(
                        repository.findAllMaarayskirjeet().stream(),
                        repository.findAllMaarayskirjeetUudet().stream()
                )
                .collect(Collectors.toList()), PerusteprojektiMaarayskirjeDto.class));

        log.debug("Tarkastetaan " + projektit.size() + " perusteen maarayskirjeet");

        int counter = 1;
        for (PerusteprojektiMaarayskirjeDto pp : projektit) {
            try {

                lataaPerusteenMaarayskirje(pp, counter);

            } catch (RuntimeException e) {
                log.error(e.getLocalizedMessage(), e);
            }

            counter++;

        }
    }

    private void lataaPerusteenMaarayskirje(PerusteprojektiMaarayskirjeDto pp, int counter) {

        TransactionTemplate template = new TransactionTemplate(tm);

        template.execute(status -> {
            Peruste peruste = perusteRepository.findOne(pp.getPeruste().getId());

            MaarayskirjeStatus mks = maarayskirjeStatusRepository.findOneByPeruste(peruste);
            boolean vaatiiLataamisen = mks == null
                    || !mks.isLataaminenOk()
                    || peruste.getGlobalVersion().getAikaleima().after(mks.getLastCheck());

            if (!vaatiiLataamisen) {
                return true;
            }

            // Jos kyseessä uusi julkaistu peruste
            if (mks == null) {
                mks = new MaarayskirjeStatus();
                mks.setPeruste(peruste);
            }

            mks.setLastCheck(new Date());
            mks.setLataaminenOk(true);

            // Tee varsinainen lataaminen
            lataaMaarayskirje(peruste, mks, counter);

            maarayskirjeStatusRepository.save(mks);

            return true;
        });
    }

    private void lataaMaarayskirje(Peruste peruste, MaarayskirjeStatus mks, int counter) {
        Maarayskirje maarayskirje = peruste.getMaarayskirje();

        // Koitetaan ladata määräyskirjeet, jos niitä ei ole vielä haettu
        if (maarayskirje != null) {

            log.debug(String.format("%04d", counter)
                    + " Aloitetaan muuttuneen perusteen " + peruste.getId() + " määräyskirjeen läpikäyminen");


            Map<Kieli, String> urls = maarayskirje.getUrl();
            Map<Kieli, Liite> liitteet = maarayskirje.getLiitteet() != null
                    ? maarayskirje.getLiitteet()
                    : new HashMap<>();

            if (urls != null) {
                for (Map.Entry<Kieli, String> entry : urls.entrySet()) {
                    Kieli kieli = entry.getKey();
                    String url = entry.getValue();

                    if (kieli != null && !ObjectUtils.isEmpty(url) && !liitteet.containsKey(kieli)) {

                        try {

                            log.debug("Ladataan määräyskirje " + url);

                            RestTemplate restTemplate = new RestTemplate();
                            HttpHeaders headers = new HttpHeaders();
                            headers.setAccept(Collections.singletonList(MediaType.APPLICATION_OCTET_STREAM));
                            HttpEntity<String> entity = new HttpEntity<>(headers);

                            ResponseEntity<byte[]> res = restTemplate.exchange(url, HttpMethod.GET, entity, byte[].class);

                            byte[] data = res.getBody();
                            if (res.getStatusCode().equals(HttpStatus.OK) && data != null) {
                                String tyyppi = tika.detect(data);
                                if (DOCUMENT_TYPES.contains(tyyppi)) {
                                    // Lisätään määräyskirje ja liitetään se perusteeseen
                                    String nimi = messages.translate("maarayskirje", kieli);
                                    if (ObjectUtils.isEmpty(nimi)) {
                                        nimi = "maarayskirje";
                                    }
                                    Liite liite = liiteRepository.add(tyyppi, nimi + ".pdf", data);
                                    liitteet.put(kieli, liite);
                                    peruste.attachLiite(liite);

                                    // Päivitetään global version
                                    Date muokattu = new Date();
                                    if (peruste.getTila() == PerusteTila.VALMIS) {
                                        perusteRepository.setRevisioKommentti("Perusteeseen lisätty määräyskirje");
                                        peruste.muokattu();
                                        muokattu = peruste.getMuokattu();
                                    }
                                    if (peruste.getGlobalVersion() == null) {
                                        peruste.setGlobalVersion(new PerusteVersion(peruste));
                                    }
                                    peruste.getGlobalVersion().setAikaleima(muokattu);
                                    log.debug("Määräyskirje " + url + " lataaminen onnistui");
                                }
                            }
                        } catch (RestClientException | IllegalArgumentException | IllegalStateException e) {
                            // Jos lataaminen ei onnistunut
                            mks.setLataaminenOk(false);

                            log.error("Määräyskirje " + url + " lataaminen epäonnistui: " + e.getLocalizedMessage());
                        }
                    }
                }
            }
        }
    }

}
