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
import fi.vm.sade.eperusteet.dto.util.EntityReference;
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
import org.apache.commons.io.IOUtils;
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
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

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
    private ProjektiValidator projektiValidator;

    @Autowired
    private KoodistoClient koodistoClient;

    @Autowired
    private TutkintonimikeKoodiService tutkintonimikeKoodiService;

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
    @Transactional
    public void validoiPerusteetTask() {
        Set<Perusteprojekti> projektit = new HashSet<>();
        projektit.addAll(repository.findAllValidoimattomat());
        projektit.addAll(repository.findAllValidoimattomatUudet());
        log.debug("Tarkastetaan " + projektit.size() + " perustetta.");

        for (Perusteprojekti pp : projektit) {
            try {
                if (pp.getTila() != JULKAISTU || pp.getPeruste().getTyyppi() != PerusteTyyppi.NORMAALI) {
                    continue;
                }

                ValidointiStatus vs = validointiStatusRepository.findOneByPeruste(pp.getPeruste());
                boolean vaatiiValidoinnin = vs == null
                        || !vs.isVaihtoOk()
                        || pp.getPeruste().getGlobalVersion().getAikaleima().after(vs.getLastCheck());

                if (!vaatiiValidoinnin) {
                    continue;
                }

                log.debug("Perusteen ajastettu validointi: " + pp.getPeruste().getId());

                TilaUpdateStatus status = projektiValidator.run(pp.getId(), JULKAISTU);

                if (vs != null) {
                    mapper.map(status, vs);
                }
                else {
                    vs = mapper.map(status, ValidointiStatus.class);
                }

                vs.setPeruste(pp.getPeruste());
                vs.setLastCheck(pp.getPeruste().getGlobalVersion().getAikaleima());

                validointiStatusRepository.save(vs);
            }
            catch (AuthenticationCredentialsNotFoundException ex) {
                log.debug(ex.getMessage());
            }
        }
    }

    @Override
    @IgnorePerusteUpdateCheck
    @Transactional
    public void tarkistaKooditTask() {
        Set<Perusteprojekti> projektit = new HashSet<>();
        projektit.addAll(repository.findAllKoodiValidoimattomat());
        projektit.addAll(repository.findAllKoodiValidoimattomatUudet());

        log.debug("Tarkastetaan " + projektit.size() + " perusteen koulutuskoodit.");

        for (Perusteprojekti pp : projektit) {
            Peruste peruste = pp.getPeruste();
            KoulutuskoodiStatus status = koulutuskoodiStatusRepository.findOneByPeruste(peruste);
            boolean vaatiiTarkistuksen = status == null
                    || !status.isKooditOk()
                    || pp.getPeruste().getGlobalVersion().getAikaleima().after(status.getLastCheck());

            if (!vaatiiTarkistuksen) {
                return;
            }

            if (status == null) {
                status = new KoulutuskoodiStatus();
            }

            tarkistaTutkinnonKoodit(peruste, status);

            koulutuskoodiStatusRepository.save(status);
        }
    }

    private void tarkistaTutkinnonKoodit(Peruste p, KoulutuskoodiStatus status) {
        status.setLastCheck(new Date());
        status.setPeruste(p);

        Set<Koulutus> koulutuskoodit = p.getKoulutukset();

        if (p.getSuoritustavat() != null && p.getSuoritustavat().size() > 0) {
            log.debug("Tarkistetaan perustetta: " + p.getNimi().toString());

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
            peruste = perusteService.luoPerusteRunko(koulutustyyppi, yksikko, tyyppi, perusteprojektiDto.isReforminMukainen());
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
    public List<ProjektiTila> getTilat(Long id) {
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
        TilaUpdateStatus updateStatus = validoiProjektiImpl(id, tila);
        updateStatus.merge(projektiValidator.run(id, tila));
        return updateStatus;
    }


    @Transactional(readOnly = true)
    @IgnorePerusteUpdateCheck
    private List<TutkintonimikeKoodiDto> doGetTutkintonimikeKoodit(Long perusteId) {
        List<TutkintonimikeKoodi> koodit = tutkintonimikeKoodiRepository.findByPerusteId(perusteId);
        return mapper.mapAsList(koodit, TutkintonimikeKoodiDto.class);
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

        // Haetaan projekti ja peruste
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

        // Aseta perusteen tila projektitilan mukaiseksi
        switch (tila) {
            case POISTETTU:
                setPerusteTila(projekti.getPeruste(), PerusteTila.POISTETTU);
                break;
            case LAADINTA:
            case VIIMEISTELY:
                setPerusteTila(projekti.getPeruste(), PerusteTila.LUONNOS);
                break;
            case KAANNOS:
            case VALMIS:
            case JULKAISTU:
                setPerusteTila(projekti.getPeruste(), PerusteTila.VALMIS);
                break;
            default:
                throw new BusinessRuleViolationException("tila-ei-ole-toteutettu");
        }
        projekti.setTila(tila);

        // Julkaisun rutiinit
        if (tila == ProjektiTila.JULKAISTU && projekti.getTila() == ProjektiTila.VALMIS) {

            // EP-1357 Julkaisun yhteydessä on pakko tehdä tiedote
            if (tiedoteDto == null) {
                throw new BusinessRuleViolationException("Julkaisun yhteydessä täytyy tehdä tiedoite");
            }
            tiedoteDto.setId(null);
            tiedoteDto.setJulkinen(true);
            tiedoteDto.setPerusteprojekti(new EntityReference(projekti.getId()));
            tiedoteService.addTiedote(tiedoteDto);

            // Dokumentit generoidaan automaattisesti julkaisun yhteydessä
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
                                            LOG.error(e.getLocalizedMessage(), e);
                                        }
                                    })));
        }

        repository.save(projekti);
        return updateStatus;
    }

    /**
     * Validoi perusteprojektin tilaa vasten
     *
     * @param id
     * @param tila
     * @return
     */
    @Deprecated
    @Transactional
    @IgnorePerusteUpdateCheck
    private TilaUpdateStatus validoiProjektiImpl(Long id, ProjektiTila tila) {

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
        if (tila != ProjektiTila.POISTETTU && tila != LAADINTA) {
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
                && tila != LAADINTA && tila != POISTETTU) {
            if (KoulutusTyyppi.of(peruste.getKoulutustyyppi()).isAmmatillinen()) {
                Set<String> osaamisalat = peruste.getOsaamisalat()
                        .stream()
                        .map(Koodi::getUri)
                        .collect(Collectors.toSet());
                List<TutkintonimikeKoodiDto> tutkintonimikkeet = doGetTutkintonimikeKoodit(peruste.getId());

                { // Tutkintonimikkeiden osaamisalat täytyvät olla perusteessa
                    Set<String> tutkintonimikkeidenOsaamisalat = tutkintonimikkeet.stream()
                            .map(TutkintonimikeKoodiDto::getOsaamisalaUri)
                            .filter(Objects::nonNull)
                            .collect(Collectors.toSet());

                    for (String nimikkeenOsaamisala : tutkintonimikkeidenOsaamisalat) {
                        if (!osaamisalat.contains(nimikkeenOsaamisala)) {
                            updateStatus.addStatus("tutkintonimikkeen-osaamisala-puuttuu-perusteesta");
                            updateStatus.setVaihtoOk(false);
                            break;
                        }
                    }
                }
            }

            if (peruste.getLukiokoulutuksenPerusteenSisalto() == null) {
                Validointi validointi;

                // Osaamisaloilla täytyy olla tekstikuvaukset
                if (peruste.getOsaamisalat() != null) {
                    PerusteenOsaViite sisalto = peruste.getSisalto(null);
                    if (sisalto != null) {
                        Set<Koodi> kuvaukselliset = flattenSisalto(sisalto).stream()
                                .filter(osa -> osa.getPerusteenOsa() instanceof TekstiKappale)
                                .map(osa -> (TekstiKappale) osa.getPerusteenOsa())
                                .map(TekstiKappale::getOsaamisala)
                                .filter(Objects::nonNull)
                                .collect(Collectors.toSet());

                        if (!Objects.equals(peruste.getOsaamisalat(), kuvaukselliset)) {
                            updateStatus.addStatus("osaamisalan-kuvauksia-puuttuu-sisallosta");
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
                                    new PerusteenRakenne.Context(peruste.getOsaamisalat(), doGetTutkintonimikeKoodit(peruste.getId())),
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
                                LOG.error(e.getMessage(), e);
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
                List<TutkintonimikeKoodiDto> tutkintonimikeKoodit = doGetTutkintonimikeKoodit(projekti.getPeruste().getId());
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
                for (Entry<String, String> entry : lokalisointivirheet.entrySet()) {
                    updateStatus.setVaihtoOk(false);
                    updateStatus.addStatus(entry.getKey());
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
                .addErrorStatusForAll("peruste-lukio-liittamaton-kurssi", () ->
                        rakenne.kurssit()
                                .filter(empty(Lukiokurssi::getOppiaineet))
                                .map(localized(Nimetty::getNimi)))
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
    @Transactional
    @IgnorePerusteUpdateCheck
    public void lataaMaarayskirjeetTask() {
        List<Perusteprojekti> projektit = repository.findAll().stream()
                .filter(projekti -> projekti.getTila().equals(JULKAISTU))
                .collect(Collectors.toList());

        log.debug("Tarkastetaan " + projektit.size() + " perustetta (vain julkaistut).");

        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_OCTET_STREAM));
        HttpEntity<String> entity = new HttpEntity<>(headers);

        projektit.forEach(projekti -> {
            Peruste peruste = projekti.getPeruste();
            Maarayskirje maarayskirje = peruste.getMaarayskirje();

            // Koitetaan ladata määräyskirjeet, jos niitä ei ole vielä haettu
            if (maarayskirje != null) {

                log.debug("Aloitetaan " + peruste.getId() + " määräyskirjeen läpikäyminen.");


                Map<Kieli, String> urls = maarayskirje.getUrl();
                Map<Kieli, Liite> liitteet = maarayskirje.getLiitteet() != null
                        ? maarayskirje.getLiitteet()
                        : new HashMap<>();

                if (urls != null) {
                    urls.forEach((kieli, url) -> {

                        if (!liitteet.containsKey(kieli)) {
                            log.debug("Koitetaan ladata määräyskirje " + url + " osoitteesta.");

                            try {
                                ResponseEntity<byte[]> res = restTemplate.exchange(url, HttpMethod.GET, entity, byte[].class);

                                byte[] data = res.getBody();
                                if (res.getStatusCode().equals(HttpStatus.OK) && data != null) {
                                    String tyyppi = tika.detect(data);
                                    if (DOCUMENT_TYPES.contains(tyyppi)) {
                                        // Lisätään määräyskirje ja liitetään se perusteeseen
                                        Liite liite = liiteRepository.add(tyyppi, "maarayskirje.pdf", data);
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
                                    }
                                }
                            } catch (RestClientException e) {
                                // Continue
                                log.error(e.getLocalizedMessage());
                            }
                        } else {
                            log.debug("Määräyskirje löytyy jo kielellä " + kieli);
                        }

                    });
                }
            }
        });
    }

}
