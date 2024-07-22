package fi.vm.sade.eperusteet.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.base.Throwables;
import com.google.common.collect.Sets;
import fi.vm.sade.eperusteet.config.InitJacksonConverter;
import fi.vm.sade.eperusteet.domain.GeneratorVersion;
import fi.vm.sade.eperusteet.domain.JulkaistuPeruste;
import fi.vm.sade.eperusteet.domain.JulkaistuPerusteData;
import fi.vm.sade.eperusteet.domain.JulkaisuLiite;
import fi.vm.sade.eperusteet.domain.JulkaisuPerusteTila;
import fi.vm.sade.eperusteet.domain.JulkaisuTila;
import fi.vm.sade.eperusteet.domain.Kieli;
import fi.vm.sade.eperusteet.domain.Koodi;
import fi.vm.sade.eperusteet.domain.KoulutusTyyppi;
import fi.vm.sade.eperusteet.domain.KoulutustyyppiToteutus;
import fi.vm.sade.eperusteet.domain.MuokkausTapahtuma;
import fi.vm.sade.eperusteet.domain.OpasTyyppi;
import fi.vm.sade.eperusteet.domain.Peruste;
import fi.vm.sade.eperusteet.domain.PerusteTila;
import fi.vm.sade.eperusteet.domain.PerusteTyyppi;
import fi.vm.sade.eperusteet.domain.Perusteprojekti;
import fi.vm.sade.eperusteet.domain.ProjektiTila;
import fi.vm.sade.eperusteet.domain.Suoritustapa;
import fi.vm.sade.eperusteet.domain.Suoritustapakoodi;
import fi.vm.sade.eperusteet.domain.TekstiPalanen;
import fi.vm.sade.eperusteet.domain.TutkintonimikeKoodi;
import fi.vm.sade.eperusteet.domain.liite.Liite;
import fi.vm.sade.eperusteet.domain.maarays.Maarays;
import fi.vm.sade.eperusteet.domain.maarays.MaaraysTila;
import fi.vm.sade.eperusteet.domain.tutkinnonosa.TutkinnonOsa;
import fi.vm.sade.eperusteet.domain.validation.ValidHtml;
import fi.vm.sade.eperusteet.dto.DokumenttiDto;
import fi.vm.sade.eperusteet.dto.JulkaisuSisaltoTyyppi;
import fi.vm.sade.eperusteet.dto.MuokkaustietoKayttajallaDto;
import fi.vm.sade.eperusteet.dto.kayttaja.KayttajanTietoDto;
import fi.vm.sade.eperusteet.dto.koodisto.KoodistoKoodiDto;
import fi.vm.sade.eperusteet.dto.koodisto.KoodistoUriArvo;
import fi.vm.sade.eperusteet.dto.maarays.MaaraysDto;
import fi.vm.sade.eperusteet.dto.peruste.JulkaisuBaseDto;
import fi.vm.sade.eperusteet.dto.peruste.JulkaisuLiiteDto;
import fi.vm.sade.eperusteet.dto.peruste.PerusteKaikkiDto;
import fi.vm.sade.eperusteet.dto.peruste.PerusteenJulkaisuData;
import fi.vm.sade.eperusteet.dto.peruste.SuoritustapaLaajaDto;
import fi.vm.sade.eperusteet.dto.peruste.TutkintonimikeKoodiDto;
import fi.vm.sade.eperusteet.dto.tutkinnonrakenne.KoodiDto;
import fi.vm.sade.eperusteet.dto.tutkinnonrakenne.TutkinnonOsaViiteDto;
import fi.vm.sade.eperusteet.dto.util.LokalisoituTekstiDto;
import fi.vm.sade.eperusteet.repository.JulkaistuPerusteDataStoreRepository;
import fi.vm.sade.eperusteet.repository.JulkaisuPerusteTilaRepository;
import fi.vm.sade.eperusteet.repository.JulkaisutRepository;
import fi.vm.sade.eperusteet.repository.KoodiRepository;
import fi.vm.sade.eperusteet.repository.PerusteRepository;
import fi.vm.sade.eperusteet.repository.PerusteprojektiRepository;
import fi.vm.sade.eperusteet.repository.TutkinnonOsaRepository;
import fi.vm.sade.eperusteet.repository.TutkintonimikeKoodiRepository;
import fi.vm.sade.eperusteet.repository.liite.LiiteRepository;
import fi.vm.sade.eperusteet.service.AmmattitaitovaatimusService;
import fi.vm.sade.eperusteet.service.JulkaisuPerusteTilaService;
import fi.vm.sade.eperusteet.service.JulkaisutService;
import fi.vm.sade.eperusteet.service.KayttajanTietoService;
import fi.vm.sade.eperusteet.service.KoodistoClient;
import fi.vm.sade.eperusteet.service.LiiteTiedostoService;
import fi.vm.sade.eperusteet.service.MaaraysService;
import fi.vm.sade.eperusteet.service.PerusteService;
import fi.vm.sade.eperusteet.service.PerusteenMuokkaustietoService;
import fi.vm.sade.eperusteet.service.PerusteprojektiService;
import fi.vm.sade.eperusteet.service.dokumentti.DokumenttiService;
import fi.vm.sade.eperusteet.service.event.aop.IgnorePerusteUpdateCheck;
import fi.vm.sade.eperusteet.service.exception.BusinessRuleViolationException;
import fi.vm.sade.eperusteet.service.exception.DokumenttiException;
import fi.vm.sade.eperusteet.service.mapping.Dto;
import fi.vm.sade.eperusteet.service.mapping.DtoMapper;
import fi.vm.sade.eperusteet.service.util.Pair;
import fi.vm.sade.eperusteet.service.util.Validointi;
import lombok.extern.slf4j.Slf4j;
import org.apache.tika.mime.MimeTypeException;
import org.joda.time.DateTime;
import org.jsoup.Jsoup;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Profile;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.web.HttpMediaTypeNotSupportedException;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
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
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toSet;

@Slf4j
@Service
@Transactional
@Profile("!test")
public class JulkaisutServiceImpl implements JulkaisutService {

    @Value("${fi.vm.sade.eperusteet.salli_virheelliset:false}")
    private boolean salliVirheelliset;

    @Dto
    @Autowired
    private DtoMapper mapper;

    @Autowired
    private PerusteRepository perusteRepository;

    @Autowired
    private PerusteprojektiRepository perusteprojektiRepository;

    @Autowired
    private JulkaisutRepository julkaisutRepository;

    @Autowired
    private LiiteRepository liiteRepository;

    @Lazy
    @Autowired
    private PerusteprojektiService perusteprojektiService;

    @Autowired
    private PerusteService perusteService;

    @Autowired
    private PerusteenMuokkaustietoService muokkausTietoService;

    @Autowired
    private KayttajanTietoService kayttajanTietoService;

    @Autowired
    private DokumenttiService dokumenttiService;

    @Autowired
    private KoodistoClient koodistoClient;

    @Autowired
    private KoodiRepository koodiRepository;

    @Autowired
    private TutkintonimikeKoodiRepository tutkintonimikeKoodiRepository;

    @Autowired
    private AmmattitaitovaatimusService ammattitaitovaatimusService;

    @Autowired
    private JulkaisuPerusteTilaRepository julkaisuPerusteTilaRepository;

    @Autowired
    private LiiteTiedostoService liiteTiedostoService;

    @Autowired
    private TutkinnonOsaRepository tutkinnonOsaRepository;

    @Autowired
    private MaaraysService maaraysService;

    @Autowired
    private JulkaistuPerusteDataStoreRepository julkaistuPerusteDataStoreRepository;

    @Autowired
    private CacheManager cacheManager;

    @Autowired
    private JulkaisuPerusteTilaService julkaisuPerusteTilaService;

    @Autowired
    @Lazy
    private JulkaisutService self;

    private static final int JULKAISUN_ODOTUSAIKA_SEKUNNEISSA = 60 * 60;
    public static final Set<String> DOCUMENT_TYPES;

    static {
        DOCUMENT_TYPES = Collections.unmodifiableSet(new HashSet<>(Collections.singletonList(
                MediaType.APPLICATION_PDF_VALUE
        )));
    }

    private final ObjectMapper objectMapper = InitJacksonConverter.createMapper();

    @Override
    public List<JulkaisuBaseDto> getJulkaisutJaViimeisinStatus(long id) {
        List<JulkaisuBaseDto> julkaisut = getJulkaistutPerusteet(id);

        JulkaisuPerusteTila julkaisuPerusteTila = julkaisuPerusteTilaRepository.findById(id).orElse(null);
        if (julkaisuPerusteTila != null
                && (julkaisuPerusteTila.getJulkaisutila().equals(JulkaisuTila.KESKEN) || julkaisuPerusteTila.getJulkaisutila().equals(JulkaisuTila.VIRHE))) {
            julkaisut.add(JulkaisuBaseDto.builder()
                    .tila(julkaisuPerusteTila.getJulkaisutila())
                    .luotu(julkaisuPerusteTila.getMuokattu())
                    .revision(julkaisut.stream().mapToInt(JulkaisuBaseDto::getRevision).max().orElse(0) + 1)
                    .build());
        }

        return taytaKayttajaTiedot(julkaisut);
    }

    @Override
    public List<JulkaisuBaseDto> getJulkaisut(long id) {
        return new ArrayList<>(getJulkaistutPerusteet(id));
    }

    @Override
    public List<JulkaisuBaseDto> getJulkisetJulkaisut(long id) {
        return getJulkaistutPerusteet(id).stream()
                .filter(JulkaisuBaseDto::getJulkinen)
                .collect(Collectors.toList());
    }

    @Override
    @IgnorePerusteUpdateCheck
    public CompletableFuture<Void> teeJulkaisu(long projektiId, JulkaisuBaseDto julkaisuBaseDto) {
        Perusteprojekti perusteprojekti = perusteprojektiRepository.findById(projektiId).orElse(null);

        if (!isValidTiedote(julkaisuBaseDto.getTiedote()) || !isValidTiedote(julkaisuBaseDto.getJulkinenTiedote())) {
            throw new BusinessRuleViolationException("tiedote-sisaltaa-kiellettyja-merkkeja");
        }

        JulkaisuPerusteTila julkaisuPerusteTila = getOrCreateTila(perusteprojekti.getPeruste().getId());
        julkaisuPerusteTila.setJulkaisutila(JulkaisuTila.KESKEN);
        julkaisuPerusteTilaService.saveJulkaisuPerusteTila(julkaisuPerusteTila);

        return self.teeJulkaisuAsync(projektiId, julkaisuBaseDto);
    }

    private JulkaisuPerusteTila getOrCreateTila(Long perusteId) {
        JulkaisuPerusteTila julkaisuPerusteTila = julkaisuPerusteTilaRepository.findById(perusteId).orElse(null);
        if (julkaisuPerusteTila == null) {
            julkaisuPerusteTila = new JulkaisuPerusteTila();
            julkaisuPerusteTila.setPerusteId(perusteId);
            julkaisuPerusteTila.setJulkaisutila(JulkaisuTila.JULKAISEMATON);
        }

        return julkaisuPerusteTila;
    }

    @Override
    public JulkaisuTila viimeisinJulkaisuTila(Long perusteId) {
        JulkaisuPerusteTila julkaisuPerusteTila = julkaisuPerusteTilaRepository.findById(perusteId).orElse(null);

        if (julkaisuPerusteTila != null &&
                julkaisuPerusteTila.getJulkaisutila().equals(JulkaisuTila.KESKEN)
                && (new Date().getTime() - julkaisuPerusteTila.getMuokattu().getTime()) / 1000 > JULKAISUN_ODOTUSAIKA_SEKUNNEISSA) {
            log.error("Julkaisu kesti yli {} sekuntia, perusteella {}", JULKAISUN_ODOTUSAIKA_SEKUNNEISSA, perusteId);
            julkaisuPerusteTila.setJulkaisutila(JulkaisuTila.VIRHE);
            julkaisuPerusteTilaService.saveJulkaisuPerusteTila(julkaisuPerusteTila);
        }

        return julkaisuPerusteTila != null ? julkaisuPerusteTila.getJulkaisutila() : JulkaisuTila.JULKAISEMATON;
    }

    @Override
    @IgnorePerusteUpdateCheck
    @Async("julkaisuTaskExecutor")
    public CompletableFuture<Void> teeJulkaisuAsync(long projektiId, JulkaisuBaseDto julkaisuBaseDto) {
        log.debug("teeJulkaisu: {}", projektiId);

        Perusteprojekti perusteprojekti = perusteprojektiRepository.findById(projektiId).orElse(null);
            if (perusteprojekti == null) {
                throw new BusinessRuleViolationException("projektia-ei-ole");
            }

        Peruste peruste = perusteprojekti.getPeruste();
        JulkaisuPerusteTila julkaisuPerusteTila = getOrCreateTila(peruste.getId());

        try {

            if (julkaisuBaseDto.getPeruste() != null && !Objects.equals(peruste.getId(), julkaisuBaseDto.getPeruste().getId())) {
                throw new BusinessRuleViolationException("vain-oman-perusteen-voi-julkaista");
            }

            // Validoinnit
            List<Validointi> validoinnit = perusteprojektiService.validoiProjekti(projektiId, ProjektiTila.JULKAISTU);

            if (!salliVirheelliset && validoinnit.stream().anyMatch(Validointi::virheellinen)) {
                throw new BusinessRuleViolationException("projekti-ei-validi");
            }

            // Aseta peruste julkaistuksi jos ei jo ole (peruste ei saa olla)
            Date julkaisuaika = new Date();
            peruste.asetaTila(PerusteTila.VALMIS);
            peruste.getPerusteprojekti().setTila(ProjektiTila.JULKAISTU);
            peruste.getGlobalVersion().setAikaleima(julkaisuaika);
            perusteRepository.save(peruste);

            kooditaValiaikaisetKoodit(peruste.getId());
            PerusteKaikkiDto sisalto = perusteService.getKaikkiSisalto(peruste.getId());
            sisalto.setViimeisinJulkaisuAika(Optional.of(julkaisuaika));
            ObjectNode perusteDataJson = objectMapper.valueToTree(sisalto);

            Set<Long> dokumentit = generoiJulkaisuPdf(sisalto);
            String username = SecurityContextHolder.getContext().getAuthentication().getName();
            JulkaistuPeruste julkaisu = new JulkaistuPeruste();
            julkaisu.setRevision(seuraavaVapaaJulkaisuNumero(peruste.getId()));
            julkaisu.setTiedote(TekstiPalanen.of(julkaisuBaseDto.getTiedote().getTekstit()));
            julkaisu.setLuoja(username);
            julkaisu.setLuotu(julkaisuaika);
            julkaisu.setPeruste(peruste);
            julkaisu.setMuutosmaaraysVoimaan(julkaisuBaseDto.getMuutosmaaraysVoimaan());
            julkaisu.setJulkinen(true);

            if (julkaisuBaseDto.getJulkinenTiedote() != null) {
                julkaisu.setJulkinenTiedote(TekstiPalanen.of(julkaisuBaseDto.getJulkinenTiedote().getTekstit()));
            }

            if (!dokumentit.isEmpty()) {
                julkaisu.setDokumentit(dokumentit);
            }

            julkaisu.setLiitteet(addLiitteet(julkaisu, julkaisuBaseDto.getLiitteet()));
            julkaisu.setData(new JulkaistuPerusteData(perusteDataJson));

            lisaaMaaraysKokoelmaan(julkaisuBaseDto, peruste, julkaisu);

            julkaisutRepository.saveAndFlush(julkaisu);
            julkaistuPerusteDataStoreRepository.syncPeruste(peruste.getId());

            if (peruste.getToteutus().equals(KoulutustyyppiToteutus.AMMATILLINEN)) {
                Cache amosaaperusteet = cacheManager.getCache("amosaaperusteet");
                if (amosaaperusteet != null) {
                    amosaaperusteet.clear();
                }
            }

            muokkausTietoService.addMuokkaustieto(peruste.getId(), peruste, MuokkausTapahtuma.JULKAISU);
        } catch(Exception e) {
            log.error(Throwables.getStackTraceAsString(e));
            julkaisuPerusteTila.setJulkaisutila(JulkaisuTila.VIRHE);
            julkaisuPerusteTilaService.saveJulkaisuPerusteTila(julkaisuPerusteTila);
            throw new BusinessRuleViolationException("julkaisun-tallennus-epaonnistui");
        }

        julkaisuPerusteTila.setJulkaisutila(JulkaisuTila.JULKAISTU);
        julkaisuPerusteTilaService.saveJulkaisuPerusteTila(julkaisuPerusteTila);

        return CompletableFuture.completedFuture(null);
    }

    private void lisaaMaaraysKokoelmaan(JulkaisuBaseDto julkaisuBaseDto, Peruste peruste, JulkaistuPeruste julkaisu) {
        MaaraysDto maarays = maaraysService.getPerusteenMaarays(peruste.getId());
        if (maarays != null) {
            maarays.setTila(MaaraysTila.JULKAISTU);
            maarays.setNimi(mapper.map(peruste.getNimi(), LokalisoituTekstiDto.class));
            maarays.setDiaarinumero(peruste.getDiaarinumero().getDiaarinumero());
            maarays.setVoimassaoloAlkaa(peruste.getVoimassaoloAlkaa());
            maarays.setVoimassaoloLoppuu(peruste.getVoimassaoloLoppuu());
            maarays.setMaarayspvm(peruste.getPaatospvm());
            maarays = maaraysService.updateMaarays(maarays);
        }

        if (julkaisuBaseDto.getMuutosmaarays() != null) {
            if (peruste.getJulkaisut().isEmpty() && maarays != null) {
                Long maaraysId = maarays.getId();
                maaraysService.deleteMaarays(maarays.getId(), peruste.getId());
                julkaisuBaseDto.getMuutosmaarays().setKorvattavatMaaraykset(
                        julkaisuBaseDto.getMuutosmaarays().getKorvattavatMaaraykset().stream()
                                .filter(korvattava -> !korvattava.getId().equals(maaraysId)).collect(Collectors.toList())
                );
                julkaisuBaseDto.getMuutosmaarays().setMuutettavatMaaraykset(
                        julkaisuBaseDto.getMuutosmaarays().getMuutettavatMaaraykset().stream()
                                .filter(korvattava -> !korvattava.getId().equals(maaraysId)).collect(Collectors.toList())
                );
            }

            julkaisuBaseDto.getMuutosmaarays().setTila(MaaraysTila.JULKAISTU);
            MaaraysDto muutosMaaraysDto = maaraysService.updateMaarays(julkaisuBaseDto.getMuutosmaarays());
            julkaisu.setMuutosmaarays(mapper.map(muutosMaaraysDto, Maarays.class));
        }

        maaraysService.getPerusteenMuutosmaaraykset(peruste.getId()).forEach(muutosmaarays -> {
            muutosmaarays.setTila(MaaraysTila.JULKAISTU);
            maaraysService.updateMaarays(muutosmaarays);
        });
    }

    @Override
    @IgnorePerusteUpdateCheck
    public Set<Long> generoiJulkaisuPdf(PerusteKaikkiDto perusteDto) {

        if ((!perusteDto.getTyyppi().equals(PerusteTyyppi.NORMAALI) && !perusteDto.getTyyppi().equals(PerusteTyyppi.OPAS)) || OpasTyyppi.TIETOAPALVELUSTA.equals(perusteDto.getOpasTyyppi())) {
            return Collections.emptySet();
        }

        Set<Suoritustapakoodi> suoritustavat = perusteDto.getSuoritustavat().stream().map(SuoritustapaLaajaDto::getSuoritustapakoodi).collect(toSet());
        if (suoritustavat.isEmpty()) {
            if (perusteDto.getTyyppi().equals(PerusteTyyppi.OPAS)) {
                suoritustavat.add(Suoritustapakoodi.OPAS);
            } else {
                suoritustavat.add(Suoritustapakoodi.REFORMI);
            }
        }

        Set<Long> documentIds = suoritustavat.stream()
                .map(suoritustapa -> perusteDto.getKielet().stream()
                        .map(kieli -> {
                            try {
                                return generateDocument(perusteDto, kieli, suoritustapa, GeneratorVersion.UUSI);
                            } catch (DokumenttiException e) {
                                log.error(e.getLocalizedMessage(), e);
                            }
                            return null;
                        })
                        .collect(toSet()))
                .flatMap(Collection::stream)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        generoiJulkaisunKvLiitteet(perusteDto);
        return documentIds;
    }

    private void generoiJulkaisunKvLiitteet(PerusteKaikkiDto perusteDto) {
        if (KoulutustyyppiToteutus.AMMATILLINEN.equals(perusteDto.getToteutus())) {
            List<Kieli> kielet = new ArrayList<>(Arrays.asList(Kieli.FI, Kieli.SV, Kieli.EN));
            kielet.forEach(kieli -> {
                try {
                    generateDocument(perusteDto, kieli, Suoritustapakoodi.REFORMI, GeneratorVersion.KVLIITE);
                } catch (DokumenttiException e) {
                    log.error(e.getLocalizedMessage(), e);
                }
            });
        }
    }

    @Override
    public boolean julkaisemattomiaMuutoksia(long perusteId) {
        List<MuokkaustietoKayttajallaDto> muokkaustiedot = muokkausTietoService.getPerusteenMuokkausTietos(perusteId, new Date(), 1);
        return julkaisutRepository.countByPerusteId(perusteId) > 0
                &&!muokkaustiedot.isEmpty()
                && muokkaustiedot.stream().noneMatch(muokkaustieto -> muokkaustieto.getTapahtuma().equals(MuokkausTapahtuma.JULKAISU));
    }

    @Override
    @IgnorePerusteUpdateCheck
    public JulkaisuBaseDto aktivoiJulkaisu(long projektiId, int revision) throws HttpMediaTypeNotSupportedException, MimeTypeException {
        Perusteprojekti perusteprojekti = perusteprojektiRepository.findById(projektiId).orElse(null);

        if (perusteprojekti == null) {
            throw new BusinessRuleViolationException("projektia-ei-ole");
        }

        Peruste peruste = perusteprojekti.getPeruste();
        JulkaistuPeruste vanhaJulkaisu = julkaisutRepository.findFirstByPerusteAndRevisionOrderByIdDesc(peruste, revision);

        JulkaistuPeruste julkaisu = new JulkaistuPeruste();
        julkaisu.setRevision(seuraavaVapaaJulkaisuNumero(peruste.getId()));
        julkaisu.setTiedote(vanhaJulkaisu.getTiedote());
        julkaisu.setDokumentit(Sets.newHashSet(vanhaJulkaisu.getDokumentit()));
        julkaisu.setPeruste(peruste);
        julkaisu.setData(vanhaJulkaisu.getData());
        julkaisu.setJulkinen(true);
        julkaisu.setJulkinenTiedote(vanhaJulkaisu.getJulkinenTiedote());
        julkaisu.setMuutosmaaraysVoimaan(vanhaJulkaisu.getMuutosmaaraysVoimaan());

        if (vanhaJulkaisu.getLiitteet() != null) {
            List<JulkaisuLiiteDto> vanhatLiitteet = mapper.mapAsList(vanhaJulkaisu.getLiitteet(), JulkaisuLiiteDto.class);
            vanhatLiitteet.forEach(liite -> liite.setId(null));
            julkaisu.setLiitteet(addLiitteet(julkaisu, vanhatLiitteet));
        }

        julkaisu = julkaisutRepository.save(julkaisu);
        muokkausTietoService.addMuokkaustieto(peruste.getId(), peruste, MuokkausTapahtuma.JULKAISU);

        return taytaKayttajaTiedot(mapper.map(julkaisu, JulkaisuBaseDto.class));
    }

    @Override
    public Page<PerusteenJulkaisuData> getJulkisetJulkaisut(List<String> koulutustyyppi, String nimi, String nimiTaiKoodi, String kieli, String tyyppi, boolean tulevat,
                                                            boolean voimassa, boolean siirtyma, boolean poistuneet, boolean koulutusvienti, String diaarinumero,
                                                            String koodi, JulkaisuSisaltoTyyppi sisaltotyyppi,
                                                            Integer sivu, Integer sivukoko) {
        Pageable pageable = PageRequest.of(sivu, sivukoko);
        Long currentMillis = DateTime.now().getMillis();
        if (tyyppi.equals(PerusteTyyppi.DIGITAALINEN_OSAAMINEN.toString())) {
            koulutustyyppi = List.of("");
        } else if (CollectionUtils.isEmpty((koulutustyyppi))) {
            koulutustyyppi = Arrays.stream(KoulutusTyyppi.values()).map(KoulutusTyyppi::toString).collect(Collectors.toList());
        }

        Page<PerusteenJulkaisuData> julkaisut = julkaisutRepository.findAllJulkisetJulkaisut(
                koulutustyyppi,
                nimi,
                nimiTaiKoodi,
                kieli,
                currentMillis,
                tulevat,
                voimassa,
                siirtyma,
                poistuneet,
                koulutusvienti,
                tyyppi,
                diaarinumero,
                koodi,
                sisaltotyyppi.name().toLowerCase(),
                pageable)
                .map(this::convertToPerusteData);

        if (!sisaltotyyppi.equals(JulkaisuSisaltoTyyppi.PERUSTE)) {
            return taytaPerusteet(julkaisut, tulevat, voimassa, siirtyma, poistuneet);
        }

        return julkaisut;
    }

    private Page<PerusteenJulkaisuData> taytaPerusteet(Page<PerusteenJulkaisuData> julkaisut, boolean tulevat,
                                                       boolean voimassa, boolean siirtyma, boolean poistuneet) {
        Set<String> tutkinnonosaKoodit = julkaisut.getContent().stream()
                .filter(julkaisuData -> julkaisuData.getSisaltotyyppi().equals(JulkaisuSisaltoTyyppi.TUTKINNONOSA.name().toLowerCase()))
                .map(julkaisuData -> julkaisuData.getTutkinnonosa().getKoodiUri())
                .collect(Collectors.toSet());

        if (!ObjectUtils.isEmpty(tutkinnonosaKoodit)) {
            Map<String, List<PerusteenJulkaisuData>> perusteetTutkinnonosanKoodilla = new HashMap<>();
            julkaisutRepository.findAllJulkaistutPerusteetByKoodi(tutkinnonosaKoodit, DateTime.now().getMillis(), tulevat, voimassa, siirtyma, poistuneet).stream()
                    .map(this::convertToPerusteData)
                    .forEach(perusteData -> {
                        perusteData.getKoodit().forEach(koodi -> {
                            if (!perusteetTutkinnonosanKoodilla.containsKey(koodi)) {
                                perusteetTutkinnonosanKoodilla.put(koodi, new ArrayList<>());
                            }
                            perusteetTutkinnonosanKoodilla.get(koodi).add(perusteData);
                        });
                    });

            julkaisut.map(julkaisu -> {
                if (julkaisu.getTutkinnonosa() != null && perusteetTutkinnonosanKoodilla.containsKey(julkaisu.getTutkinnonosa().getKoodiUri())) {
                    if (julkaisu.getPerusteet() == null) {
                        julkaisu.setPerusteet(new ArrayList<>());
                    }
                    julkaisu.getPerusteet().addAll(perusteetTutkinnonosanKoodilla.get(julkaisu.getTutkinnonosa().getKoodiUri()));
                }
                return julkaisu;
            });
        }

        return julkaisut;
    }

    private PerusteenJulkaisuData convertToPerusteData(String perusteObj) {
        try {
            return objectMapper.readValue(perusteObj, PerusteenJulkaisuData.class);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<PerusteenJulkaisuData> getKaikkiPerusteet() {
        return julkaisutRepository.findAllJulkaistutPerusteetByVoimassaolo(DateTime.now().getMillis(), true, true, false, false).stream()
                .map(this::convertToPerusteData).collect(Collectors.toList());
    }

    @Override
    @IgnorePerusteUpdateCheck
    public Date viimeisinPerusteenJulkaisuaika(Long perusteId) {
        JulkaistuPeruste viimeisinJulkaisu = julkaisutRepository.findFirstByPerusteIdOrderByRevisionDesc(perusteId);
        if (viimeisinJulkaisu != null) {
            return viimeisinJulkaisu.getLuotu();
        } else {
            Peruste peruste = perusteRepository.findOne(perusteId);
            if (peruste != null && (peruste.getTila().equals(PerusteTila.VALMIS) || peruste.getTyyppi().equals(PerusteTyyppi.AMOSAA_YHTEINEN))) {
                return peruste.getGlobalVersion().getAikaleima();
            }
        }

        return null;
    }

    @Override
    public void kooditaValiaikaisetKoodit(Long perusteId) {
        Peruste peruste = perusteRepository.findOne(perusteId);
        peruste.getKoodit().stream()
                .filter(koodi -> koodi.isTemporary())
                .forEach(koodi -> {
                    KoodiDto koodiDto = mapper.map(koodi, KoodiDto.class);
                    KoodistoKoodiDto lisattyKoodi = koodistoClient.addKoodiNimella(koodi.getKoodisto(), koodiDto.getNimi());

                    koodi.setUri(lisattyKoodi.getKoodiUri());
                    koodi.setKoodisto(lisattyKoodi.getKoodisto().getKoodistoUri());
                    koodi.setVersio(lisattyKoodi.getVersio() != null ? Long.valueOf(lisattyKoodi.getVersio()) : null);
                    koodi.setNimi(null);
                    koodiRepository.save(koodi);
                });

        List<TutkintonimikeKoodiDto> perusteenTutkintonimikkeet = perusteService.getTutkintonimikeKoodit(peruste.getId());
        perusteenTutkintonimikkeet.forEach(tutkintonimikeKoodiDto -> {
            if (tutkintonimikeKoodiDto.getTutkintonimikeUri() != null && tutkintonimikeKoodiDto.getTutkintonimikeUri().contains("temporary")) {
                Koodi tutkintonimike = koodiRepository.findFirstByUriOrderByVersioDesc(tutkintonimikeKoodiDto.getTutkintonimikeUri());
                TutkintonimikeKoodi tutkintonimikeKoodi = mapper.map(tutkintonimikeKoodiDto, TutkintonimikeKoodi.class);
                KoodistoKoodiDto lisattyKoodi = koodistoClient.addKoodiNimella("tutkintonimikkeet", tutkintonimikeKoodiDto.getNimi(), 5);

                if (tutkintonimike != null) {
                    tutkintonimike.setUri(lisattyKoodi.getKoodiUri());
                    tutkintonimike.setKoodisto("tutkintonimikkeet");
                    tutkintonimike.setVersio(lisattyKoodi.getVersio() != null ? Long.valueOf(lisattyKoodi.getVersio()) : null);
                    tutkintonimike.setNimi(null);
                    koodiRepository.save(tutkintonimike);
                }

                tutkintonimikeKoodi.setTutkintonimikeArvo(lisattyKoodi.getKoodiArvo());
                tutkintonimikeKoodi.setTutkintonimikeUri(lisattyKoodi.getKoodiUri());
                tutkintonimikeKoodi.setNimi(null);
                tutkintonimikeKoodiRepository.save(tutkintonimikeKoodi);
            }
        });

        for (Suoritustapa suoritustapa : peruste.getSuoritustavat()) {
            List<TutkinnonOsaViiteDto> tutkinnonOsaViitteet = perusteService.getTutkinnonOsat(peruste.getId(), suoritustapa.getSuoritustapakoodi());

            tutkinnonOsaViitteet.forEach(tutkinnonOsaViite -> {
                if (tutkinnonOsaViite.getTutkinnonOsaDto().getKoodi() == null ) {
                    KoodistoKoodiDto lisattyKoodi = koodistoClient.addKoodiNimella(KoodistoUriArvo.TUTKINNONOSAT, tutkinnonOsaViite.getTutkinnonOsaDto().getNimi());
                    if (lisattyKoodi != null) {
                        Koodi koodi = new Koodi();
                        koodi.setUri(lisattyKoodi.getKoodiUri());
                        koodi.setKoodisto(KoodistoUriArvo.TUTKINNONOSAT);
                        koodi = koodiRepository.save(koodi);

                        TutkinnonOsa tutkinnonOsa = mapper.map(tutkinnonOsaViite.getTutkinnonOsaDto(), TutkinnonOsa.class);
                        tutkinnonOsa.setKoodi(koodi);
                        tutkinnonOsaRepository.save(tutkinnonOsa);
                    }
                }
            });
        }

        ammattitaitovaatimusService.addAmmattitaitovaatimusJaArvioinninkohteetKooditToKoodisto(peruste.getId());
    }

    @Override
    @IgnorePerusteUpdateCheck
    public void nollaaJulkaisuTila(Long perusteId) {
        JulkaisuPerusteTila julkaisuPerusteTila = getOrCreateTila(perusteId);
        julkaisuPerusteTila.setJulkaisutila(JulkaisuTila.JULKAISEMATON);
        julkaisuPerusteTilaService.saveJulkaisuPerusteTila(julkaisuPerusteTila);
    }

    private List<JulkaisuBaseDto> taytaKayttajaTiedot(List<JulkaisuBaseDto> julkaisut) {
        Map<String, KayttajanTietoDto> kayttajatiedot = kayttajanTietoService
                .haeKayttajatiedot(julkaisut.stream().map(JulkaisuBaseDto::getLuoja).filter(Objects::nonNull).collect(Collectors.toList()))
                .stream().collect(Collectors.toMap(kayttajanTieto -> kayttajanTieto.getOidHenkilo(), kayttajanTieto -> kayttajanTieto));
        julkaisut.forEach(julkaisu -> julkaisu.setKayttajanTieto(kayttajatiedot.get(julkaisu.getLuoja())));
        return julkaisut;
    }

    private JulkaisuBaseDto taytaKayttajaTiedot(JulkaisuBaseDto julkaisu) {
        return taytaKayttajaTiedot(Arrays.asList(julkaisu)).get(0);
    }

    @Override
    @IgnorePerusteUpdateCheck
    public int seuraavaVapaaJulkaisuNumero(long perusteId) {
        Peruste peruste = perusteRepository.findOne(perusteId);
        List<JulkaistuPeruste> vanhatJulkaisut = julkaisutRepository.findAllByPeruste(peruste);
        return vanhatJulkaisut.stream().mapToInt(JulkaistuPeruste::getRevision).max().orElse(0) + 1;
    }

    @Override
    @IgnorePerusteUpdateCheck
    @Transactional
    public void updateJulkaisu(Long perusteId, JulkaisuBaseDto julkaisuBaseDto) {
        try {
            Peruste peruste = perusteRepository.findOne(perusteId);
            JulkaistuPeruste julkaisu = julkaisutRepository.findFirstByPerusteAndRevisionOrderByIdDesc(peruste, julkaisuBaseDto.getRevision());

            julkaisu.setTiedote(TekstiPalanen.of(julkaisuBaseDto.getTiedote().getTekstit()));
            julkaisu.setJulkinenTiedote(TekstiPalanen.of(julkaisuBaseDto.getJulkinenTiedote().getTekstit()));
            julkaisu.setMuutosmaaraysVoimaan(julkaisuBaseDto.getMuutosmaaraysVoimaan());
            julkaisu.setJulkinen(julkaisuBaseDto.getJulkinen());
            julkaisu.setLiitteet(addLiitteet(julkaisu, julkaisuBaseDto.getLiitteet()));
            julkaisu.setMuutosmaarays(mapper.map(julkaisuBaseDto.getMuutosmaarays(), Maarays.class));

            julkaisutRepository.saveAndFlush(julkaisu);
        } catch(Exception e) {
            log.error(Throwables.getStackTraceAsString(e));
            throw new BusinessRuleViolationException("julkaisun-tallennus-epaonnistui");
        }
    }

    private Long generateDocument(PerusteKaikkiDto perusteDto, Kieli kieli, Suoritustapakoodi suoritustapakoodi, GeneratorVersion version) throws DokumenttiException {
        DokumenttiDto createDtoFor = dokumenttiService.createDtoFor(
                perusteDto.getId(),
                kieli,
                suoritustapakoodi,
                version
        );
        dokumenttiService.setStarted(createDtoFor);
        dokumenttiService.generateWithDto(createDtoFor, perusteDto);
        return createDtoFor.getId();
    }

    private List<JulkaisuLiite> addLiitteet(JulkaistuPeruste julkaisu,
                                            List<JulkaisuLiiteDto> liitteet) throws HttpMediaTypeNotSupportedException, MimeTypeException {
        List<JulkaisuLiite> tempLiitteet = new ArrayList<>();

        if (liitteet != null) {
            for (JulkaisuLiiteDto julkaisuLiite : liitteet) {
                Liite liite = null;
                JulkaisuLiite mappedJulkaisuLiite = mapper.map(julkaisuLiite, JulkaisuLiite.class);
                if (julkaisuLiite.getData() != null) {
                    Pair<UUID, String> filePair = uploadLiite(julkaisuLiite);
                    liite = liiteRepository.findById(filePair.getFirst()).orElse(null);
                } else if (julkaisuLiite.getLiite() != null && julkaisuLiite.getLiite().getId() != null) {
                    liite = liiteRepository.findById(julkaisuLiite.getLiite().getId()).orElse(null);
                }

                if (liite != null) {
                    mappedJulkaisuLiite.setLiite(liite);
                    mappedJulkaisuLiite.setNimi(julkaisuLiite.getNimi());
                    mappedJulkaisuLiite.setKieli(julkaisuLiite.getKieli());
                    mappedJulkaisuLiite.setJulkaistuPeruste(julkaisu);
                    tempLiitteet.add(mappedJulkaisuLiite);
                }
            }
        }
        return tempLiitteet;
    }

    private Pair<UUID, String> uploadLiite(JulkaisuLiiteDto julkaisuLiite) throws HttpMediaTypeNotSupportedException, MimeTypeException {
        try {
            byte[] decoder = Base64.getDecoder().decode(julkaisuLiite.getData());
            InputStream is = new ByteArrayInputStream(decoder);
            return liiteTiedostoService.uploadFile(
                    null,
                    julkaisuLiite.getLiite().getNimi(),
                    is,
                    decoder.length,
                    julkaisuLiite.getLiite().getTyyppi(),
                    DOCUMENT_TYPES,
                    null, null, null);
        } catch (IOException e) {
            throw new BusinessRuleViolationException("liitteen-lisaaminen-epaonnistui");
        }
    }

    private List<JulkaisuBaseDto> getJulkaistutPerusteet(Long id) {
        Peruste peruste = perusteRepository.findById(id).orElse(null);
        if (peruste == null) {
            throw new BusinessRuleViolationException("perustetta-ei-loytynyt");
        }

        List<JulkaistuPeruste> one = julkaisutRepository.findAllByPeruste(peruste);
        return mapper.mapAsList(one, JulkaisuBaseDto.class);
    }

    private boolean isValidTiedote(LokalisoituTekstiDto tiedote) {
        if (tiedote == null) {
            return true;
        }

        Set<Kieli> kielet = new HashSet<>(Arrays.asList(Kieli.FI, Kieli.SV, Kieli.EN));
        for (Kieli kieli : kielet) {
            if (tiedote.get(kieli) != null && !Jsoup.isValid(tiedote.get(kieli), ValidHtml.WhitelistType.SIMPLIFIED.getWhitelist())) {
                return false;
            }
        }
        return true;
    }
}
