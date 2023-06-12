package fi.vm.sade.eperusteet.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import fi.vm.sade.eperusteet.domain.Diaarinumero;
import fi.vm.sade.eperusteet.domain.KVLiite;
import fi.vm.sade.eperusteet.domain.Kieli;
import fi.vm.sade.eperusteet.domain.KoulutusTyyppi;
import fi.vm.sade.eperusteet.domain.LaajuusYksikko;
import fi.vm.sade.eperusteet.domain.Maarayskirje;
import fi.vm.sade.eperusteet.domain.MaarayskirjeStatus;
import fi.vm.sade.eperusteet.domain.Peruste;
import fi.vm.sade.eperusteet.domain.PerusteAikataulu;
import fi.vm.sade.eperusteet.domain.PerusteTila;
import fi.vm.sade.eperusteet.domain.PerusteTyyppi;
import fi.vm.sade.eperusteet.domain.PerusteVersion;
import fi.vm.sade.eperusteet.domain.PerusteenOsa;
import fi.vm.sade.eperusteet.domain.PerusteenOsaTyoryhma;
import fi.vm.sade.eperusteet.domain.PerusteenOsaViite;
import fi.vm.sade.eperusteet.domain.Perusteprojekti;
import fi.vm.sade.eperusteet.domain.PerusteprojektiTyoryhma;
import fi.vm.sade.eperusteet.domain.ProjektiTila;
import fi.vm.sade.eperusteet.domain.Suoritustapa;
import fi.vm.sade.eperusteet.domain.Suoritustapakoodi;
import fi.vm.sade.eperusteet.domain.TekstiPalanen;
import fi.vm.sade.eperusteet.domain.liite.Liite;
import fi.vm.sade.eperusteet.domain.liite.LiiteTyyppi;
import fi.vm.sade.eperusteet.domain.tutkinnonosa.TutkinnonOsa;
import fi.vm.sade.eperusteet.domain.tutkinnonrakenne.TutkinnonOsaViite;
import fi.vm.sade.eperusteet.domain.validation.ValidointiStatus;
import fi.vm.sade.eperusteet.dto.OmistajaDto;
import fi.vm.sade.eperusteet.dto.Reference;
import fi.vm.sade.eperusteet.dto.TiedoteDto;
import fi.vm.sade.eperusteet.dto.TilaUpdateStatus;
import fi.vm.sade.eperusteet.dto.ValidointiStatusType;
import fi.vm.sade.eperusteet.dto.kayttaja.KayttajanProjektitiedotDto;
import fi.vm.sade.eperusteet.dto.kayttaja.KayttajanTietoDto;
import fi.vm.sade.eperusteet.dto.peruste.JulkaisuBaseDto;
import fi.vm.sade.eperusteet.dto.peruste.PerusteBaseDto;
import fi.vm.sade.eperusteet.dto.peruste.PerusteVersionDto;
import fi.vm.sade.eperusteet.dto.peruste.PerusteenOsaTyoryhmaDto;
import fi.vm.sade.eperusteet.dto.peruste.PerusteprojektiQueryDto;
import fi.vm.sade.eperusteet.dto.perusteprojekti.DiaarinumeroHakuDto;
import fi.vm.sade.eperusteet.dto.perusteprojekti.PerusteprojektiDto;
import fi.vm.sade.eperusteet.dto.perusteprojekti.PerusteprojektiInfoDto;
import fi.vm.sade.eperusteet.dto.perusteprojekti.PerusteprojektiKevytDto;
import fi.vm.sade.eperusteet.dto.perusteprojekti.PerusteprojektiListausDto;
import fi.vm.sade.eperusteet.dto.perusteprojekti.PerusteprojektiLuontiDto;
import fi.vm.sade.eperusteet.dto.perusteprojekti.PerusteprojektiMaarayskirjeDto;
import fi.vm.sade.eperusteet.dto.perusteprojekti.PerusteprojektiValidointiDto;
import fi.vm.sade.eperusteet.dto.perusteprojekti.TyoryhmaHenkiloDto;
import fi.vm.sade.eperusteet.dto.util.CombinedDto;
import fi.vm.sade.eperusteet.dto.util.LokalisoituTekstiDto;
import fi.vm.sade.eperusteet.repository.MaarayskirjeStatusRepository;
import fi.vm.sade.eperusteet.repository.PerusteRepository;
import fi.vm.sade.eperusteet.repository.PerusteenOsaRepository;
import fi.vm.sade.eperusteet.repository.PerusteenOsaTyoryhmaRepository;
import fi.vm.sade.eperusteet.repository.PerusteenOsaViiteRepository;
import fi.vm.sade.eperusteet.repository.PerusteprojektiRepository;
import fi.vm.sade.eperusteet.repository.PerusteprojektiTyoryhmaRepository;
import fi.vm.sade.eperusteet.repository.TutkinnonOsaViiteRepository;
import fi.vm.sade.eperusteet.repository.ValidointiStatusRepository;
import fi.vm.sade.eperusteet.repository.liite.LiiteRepository;
import fi.vm.sade.eperusteet.service.AmmattitaitovaatimusService;
import fi.vm.sade.eperusteet.service.JulkaisutService;
import fi.vm.sade.eperusteet.service.KayttajanTietoService;
import fi.vm.sade.eperusteet.service.LiiteService;
import fi.vm.sade.eperusteet.service.LocalizedMessagesService;
import fi.vm.sade.eperusteet.service.PerusteService;
import fi.vm.sade.eperusteet.service.PerusteprojektiService;
import fi.vm.sade.eperusteet.service.ProjektiValidator;
import fi.vm.sade.eperusteet.service.TiedoteService;
import fi.vm.sade.eperusteet.service.event.aop.IgnorePerusteUpdateCheck;
import fi.vm.sade.eperusteet.service.exception.BusinessRuleViolationException;
import fi.vm.sade.eperusteet.service.mapping.Dto;
import fi.vm.sade.eperusteet.service.mapping.DtoMapper;
import fi.vm.sade.eperusteet.service.mapping.KayttajanTietoParser;
import fi.vm.sade.eperusteet.service.security.PermissionManager;
import fi.vm.sade.eperusteet.utils.client.RestClientFactory;
import fi.vm.sade.javautils.http.OphHttpClient;
import fi.vm.sade.javautils.http.OphHttpEntity;
import fi.vm.sade.javautils.http.OphHttpRequest;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.http.entity.ContentType;
import org.apache.tika.Tika;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.util.ObjectUtils;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static fi.vm.sade.eperusteet.domain.ProjektiTila.JULKAISTU;
import static fi.vm.sade.eperusteet.domain.ProjektiTila.LAADINTA;
import static fi.vm.sade.eperusteet.resource.peruste.LiitetiedostoController.DOCUMENT_TYPES;
import static javax.servlet.http.HttpServletResponse.SC_OK;
import static javax.servlet.http.HttpServletResponse.SC_UNAUTHORIZED;

@Slf4j
@Service
public class PerusteprojektiServiceImpl implements PerusteprojektiService {

    private static final String VIRKAILIJA_HAKU_API = "/virkailija/haku";

    final Tika tika = new Tika();

    @Value("${cas.service.kayttooikeus-service:''}")
    private String kayttooikeusServiceUrl;

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
    private TiedoteService tiedoteService;

    @Autowired
    private ValidointiStatusRepository validointiStatusRepository;

    @Autowired
    private MaarayskirjeStatusRepository maarayskirjeStatusRepository;

    @Autowired
    private ProjektiValidator projektiValidator;

    @Autowired
    private PlatformTransactionManager tm;

    @Autowired
    private LocalizedMessagesService messages;

    @Autowired
    private AmmattitaitovaatimusService ammattitaitovaatimusService;

    @Autowired
    HttpHeaders httpHeaders;

    @Autowired
    private JulkaisutService julkaisutService;

    @Autowired
    private LiiteService liiteService;

    @Autowired
    private PermissionManager permissionManager;

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
    public void validoiPerusteetTask(int max) {

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

                if (max > 0 && counter > max) {
                    break;
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
                    || peruste.getViimeisinJulkaisuAika().orElse(peruste.getGlobalVersion().getAikaleima()).after(vs.getLastCheck());

            if (!vaatiiValidoinnin) {
                return true;
            }

            log.debug(String.format("%04d", counter) + " Perusteen ajastettu validointi: " + peruste.getId());

            TilaUpdateStatus tilaUpdateStatus = projektiValidator.run(pp.getId(), JULKAISTU);
            tilaUpdateStatus.setInfot(tilaUpdateStatus.getInfot().stream().filter(info -> info.getValidointiStatusType() != ValidointiStatusType.HUOMAUTUS).collect(Collectors.toList()));

            if (vs != null) {
                mapper.map(tilaUpdateStatus, vs);
            }
            else {
                vs = mapper.map(tilaUpdateStatus, ValidointiStatus.class);
            }

            vs.setPeruste(peruste);
            vs.setLastCheck(new Date());

            validointiStatusRepository.save(vs);

            return true;
        });
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
                ppk.setToteutus(peruste.getToteutus());
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
    @PreAuthorize("isAuthenticated()")
    public List<PerusteprojektiListausDto> getOmatProjektit() {
        Set<String> orgs = permissionManager.kayttajanOrganisaatiot();
        String user = SecurityContextHolder.getContext().getAuthentication().getName();
        return mapper.mapAsList(repository.findOmatPerusteprojektit(user, orgs), PerusteprojektiListausDto.class);
    }

    @Override
    @Transactional(readOnly = true)
    @PreAuthorize("isAuthenticated()")
    public List<PerusteprojektiListausDto> getOmatJulkaistut() {
        Set<String> orgs = permissionManager.kayttajanOrganisaatiot();
        String user = SecurityContextHolder.getContext().getAuthentication().getName();
        return mapper.mapAsList(repository.findOmatJulkaistutPerusteprojektit(user, orgs), PerusteprojektiListausDto.class);
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

        OphHttpClient client = restClientFactory.get(kayttooikeusServiceUrl, true);

        String url = kayttooikeusServiceUrl + VIRKAILIJA_HAKU_API;

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
        if ("1.2.246.562.10.00000000001".equals(ryhmaOid)) {
            return kayttajat;
        }

        OphHttpClient client = restClientFactory.get(kayttooikeusServiceUrl, true);

        String url = kayttooikeusServiceUrl + VIRKAILIJA_HAKU_API;

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
                        String oid = node.get("oid").asText();
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

        KoulutusTyyppi koulutustyyppi = perusteprojektiDto.getKoulutustyyppi() != null ? KoulutusTyyppi.of(perusteprojektiDto.getKoulutustyyppi()) : null;
        LaajuusYksikko yksikko = perusteprojektiDto.getLaajuusYksikko();
        PerusteTyyppi tyyppi = perusteprojektiDto.getTyyppi() == null ? PerusteTyyppi.NORMAALI : perusteprojektiDto.getTyyppi();
        perusteprojekti.setTila(LAADINTA);
        perusteprojekti.setRyhmaOid(perusteprojektiDto.getRyhmaOid());

        perusteprojektiDto.setReforminMukainen(
                perusteprojektiDto.isReforminMukainen()
                        && perusteprojektiDto.getKoulutustyyppi() != null
                        && KoulutusTyyppi.of(perusteprojektiDto.getKoulutustyyppi()).isAmmatillinen());

        if (tyyppi == PerusteTyyppi.OPAS) {
            throw new BusinessRuleViolationException("Virheellinen perustetyyppi");
        }

        if (tyyppi != PerusteTyyppi.POHJA && tyyppi != PerusteTyyppi.DIGITAALINEN_OSAAMINEN) {
            if (yksikko == null && koulutustyyppi
                    .isOneOf(KoulutusTyyppi.PERUSTUTKINTO,
                            KoulutusTyyppi.AMMATTITUTKINTO,
                            KoulutusTyyppi.ERIKOISAMMATTITUTKINTO,
                            KoulutusTyyppi.TELMA,
                            KoulutusTyyppi.VALMA)) {
                throw new BusinessRuleViolationException("Opetussuunnitelmalla täytyy olla yksikkö");
            }

            if (StringUtils.isNotBlank(perusteprojektiDto.getDiaarinumero())) {
                DiaarinumeroHakuDto diaariHaku = onkoDiaarinumeroKaytossa(new Diaarinumero(perusteprojektiDto.getDiaarinumero()));
                boolean korvaava = diaariHaku.getTila() == ProjektiTila.JULKAISTU && diaariHaku.getLoytyi();

                if (korvaava) {
                    Perusteprojekti jyrattava = repository.findOne(diaariHaku.getId());
                    perusteprojekti.setPaatosPvm(jyrattava.getPaatosPvm());
                    perusteprojekti.setToimikausiAlku(jyrattava.getToimikausiAlku());
                    perusteprojekti.setToimikausiLoppu(jyrattava.getToimikausiLoppu());
                }
            }
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

        if (peruste.getKoulutustyyppi() != null && KoulutusTyyppi.of(peruste.getKoulutustyyppi()).isAmmatillinen()) {
            KVLiite kvliite = new KVLiite();
            if (perusteprojektiDto.getPerusteId() != null) {
                Peruste pohja = perusteRepository.findOne(perusteprojektiDto.getPerusteId());
                if (pohja != null) {
                    kvliite.setPohja(pohja.getKvliite());
                }
            }
            peruste.setKvliite(kvliite);
        }

        if (tyyppi.equals(PerusteTyyppi.POHJA) || tyyppi.equals(PerusteTyyppi.DIGITAALINEN_OSAAMINEN)) {
            TekstiPalanen pnimi = TekstiPalanen.of(Kieli.FI, perusteprojektiDto.getNimi());
            peruste.setNimi(pnimi);
        }

        if (perusteprojektiDto.getKuvaus() != null) {
            peruste.setKuvaus(mapper.map(perusteprojektiDto.getKuvaus(), TekstiPalanen.class));
        }

        if (!CollectionUtils.isEmpty(perusteprojektiDto.getPerusteenAikataulut())) {
            List<PerusteAikataulu> aikataulut = mapper.mapAsList(perusteprojektiDto.getPerusteenAikataulut(), PerusteAikataulu.class).stream().map(aikataulu -> {
                aikataulu.setPeruste(peruste);
                return aikataulu;
            }).collect(Collectors.toList());
            peruste.setPerusteenAikataulut(aikataulut);
        }

        if (perusteprojektiDto.getDiaarinumero() != null) {
            peruste.setDiaarinumero(new Diaarinumero(perusteprojektiDto.getDiaarinumero()));
        }

        perusteprojekti.setPeruste(peruste);
        perusteprojekti = repository.saveAndFlush(perusteprojekti);

        if (perusteprojektiDto.getPerusteId() != null) {
            liiteService.copyLiitteetForPeruste(peruste.getId(), perusteprojektiDto.getPerusteId());
        }

        return mapper.map(perusteprojekti, PerusteprojektiDto.class);
    }

    @Override
    @Transactional
    public PerusteprojektiDto savePohja(PerusteprojektiLuontiDto perusteprojektiDto) {
        return save(perusteprojektiDto);
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

        Perusteprojekti projekti = repository.findOne(id);
        Peruste peruste = projekti.getPeruste();

        if (projekti.getTila().equals(ProjektiTila.POISTETTU) && tila.equals(LAADINTA) && peruste.getViimeisinJulkaisuAika().isPresent()) {
            peruste.asetaTila(PerusteTila.VALMIS);
            projekti.setTila(ProjektiTila.JULKAISTU);
            return new TilaUpdateStatus();
        }

        TilaUpdateStatus updateStatus;

        if (projekti.getTila().equals(ProjektiTila.POISTETTU)) {
            updateStatus = new TilaUpdateStatus();
        } else {
            updateStatus = validoiProjekti(id, tila);
        }

        // Perusteen tilan muutos
        if (!updateStatus.isVaihtoOk()) {
            return updateStatus;
        }

        // Tarkistetaan mahdolliset tilat
        updateStatus.setVaihtoOk(projekti.getTila().mahdollisetTilat(projekti.getPeruste().getTyyppi()).contains(tila));
        if (!updateStatus.isVaihtoOk()) {
            String viesti = "Tilasiirtymä tilasta '" + projekti.getTila().toString() + "' tilaan '"
                    + tila.toString() + "' ei mahdollinen";
            updateStatus.addStatus(viesti);
            return updateStatus;
        }

        // Lisätään koodittomat ammattiatitovaatimukset koodistoon
        if (tila == ProjektiTila.JULKAISTU) {
            ammattitaitovaatimusService.addAmmattitaitovaatimusJaArvioinninkohteetKooditToKoodisto(peruste.getId());
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
            julkaisutService.teeJulkaisu(
                    projekti.getId(),
                    JulkaisuBaseDto
                            .builder()
                            .peruste(mapper.map(peruste, PerusteBaseDto.class))
                            .tiedote(tiedoteDto.getSisalto() != null ? tiedoteDto.getSisalto() : LokalisoituTekstiDto.of("Julkaisu"))
                            .build());
        }

        if (tila == ProjektiTila.POISTETTU) {
            if (PerusteTyyppi.POHJA.equals(projekti.getPeruste().getTyyppi())) {
                projekti.setTila(ProjektiTila.POISTETTU);
                projekti.getPeruste().asetaTila(PerusteTila.POISTETTU);
            } else {
                setPerusteTila(projekti.getPeruste(), PerusteTila.POISTETTU);
            }
        }

        if (tila == LAADINTA) {
            if (PerusteTyyppi.POHJA.equals(projekti.getPeruste().getTyyppi())) {
                projekti.setTila(ProjektiTila.LAADINTA);
                projekti.getPeruste().asetaTila(PerusteTila.LUONNOS);
            } else {
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

    @Override
    @IgnorePerusteUpdateCheck
    @Transactional
    public void updateProjektiTila(Long id, ProjektiTila tila) {
        Perusteprojekti projekti = repository.findOne(id);
        projekti.setTila(tila);
        if (tila.equals(ProjektiTila.VALMIS)) {
            projekti.getPeruste().asetaTila(PerusteTila.VALMIS);
        }
        repository.save(projekti);
    }

    @Override
    @IgnorePerusteUpdateCheck
    @Transactional
    public void avaaPerusteProjekti(Long id) {
        Perusteprojekti projekti = repository.findOne(id);
        projekti.setTila(LAADINTA);
        projekti.getPeruste().asetaTila(PerusteTila.LUONNOS);
        repository.save(projekti);
    }

    private void setPerusteTila(Peruste peruste, PerusteTila tila) {
        // Asetetaan sisältöjen tilat
        if (peruste.getKoulutustyyppi() != null && KoulutusTyyppi.of(peruste.getKoulutustyyppi()).isAmmatillinen()) {
            for (Suoritustapa suoritustapa : peruste.getSuoritustavat()) {
                setSisaltoTila(peruste, suoritustapa.getSisalto(), tila);
                for (TutkinnonOsaViite tutkinnonosaViite : suoritustapa.getTutkinnonOsat()) {
                    setOsatTila(peruste, tutkinnonosaViite, tila);
                }
            }
        }
        else {
            peruste.getSisallot().forEach(sisalto -> {
                if (sisalto != null) {
                    setSisaltoTila(peruste, sisalto.getSisalto(), tila);
                }
            });
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
        if (sisaltoRoot != null && sisaltoRoot.getPerusteenOsa() != null) {
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

        if (sisaltoRoot != null && sisaltoRoot.getLapset() != null) {
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
                    || peruste.getViimeisinJulkaisuAika().orElse(peruste.getGlobalVersion().getAikaleima()).after(mks.getLastCheck());

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
                            httpHeaders.setAccept(Collections.singletonList(MediaType.APPLICATION_OCTET_STREAM));
                            HttpEntity<String> entity = new HttpEntity<>(httpHeaders);

                            ResponseEntity<byte[]> res = restTemplate.exchange(url, HttpMethod.GET, entity, byte[].class);

                            byte[] data = res.getBody();
                            if (res.getStatusCode().equals(HttpStatus.OK) && data != null) {
                                String mime = tika.detect(data);
                                if (DOCUMENT_TYPES.contains(mime)) {
                                    // Lisätään määräyskirje ja liitetään se perusteeseen
                                    String nimi = messages.translate("maarayskirje", kieli);
                                    if (ObjectUtils.isEmpty(nimi)) {
                                        nimi = "maarayskirje";
                                    }
                                    Liite liite = liiteRepository.add(LiiteTyyppi.MAARAYSKIRJE, mime, nimi + ".pdf", data);
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
