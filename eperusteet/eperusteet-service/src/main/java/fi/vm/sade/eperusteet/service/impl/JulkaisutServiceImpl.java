package fi.vm.sade.eperusteet.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.base.Throwables;
import com.google.common.collect.Sets;
import fi.vm.sade.eperusteet.domain.GeneratorVersion;
import fi.vm.sade.eperusteet.domain.JulkaistuPeruste;
import fi.vm.sade.eperusteet.domain.JulkaistuPerusteData;
import fi.vm.sade.eperusteet.domain.Koodi;
import fi.vm.sade.eperusteet.domain.KoulutusTyyppi;
import fi.vm.sade.eperusteet.domain.MuokkausTapahtuma;
import fi.vm.sade.eperusteet.domain.Peruste;
import fi.vm.sade.eperusteet.domain.PerusteTila;
import fi.vm.sade.eperusteet.domain.PerusteTyyppi;
import fi.vm.sade.eperusteet.domain.Perusteprojekti;
import fi.vm.sade.eperusteet.domain.ProjektiTila;
import fi.vm.sade.eperusteet.domain.Suoritustapa;
import fi.vm.sade.eperusteet.domain.Suoritustapakoodi;
import fi.vm.sade.eperusteet.domain.TekstiPalanen;
import fi.vm.sade.eperusteet.domain.TutkintonimikeKoodi;
import fi.vm.sade.eperusteet.dto.DokumenttiDto;
import fi.vm.sade.eperusteet.dto.TilaUpdateStatus;
import fi.vm.sade.eperusteet.dto.kayttaja.KayttajanTietoDto;
import fi.vm.sade.eperusteet.dto.koodisto.KoodistoKoodiDto;
import fi.vm.sade.eperusteet.dto.peruste.JulkaisuBaseDto;
import fi.vm.sade.eperusteet.dto.peruste.PerusteKaikkiDto;
import fi.vm.sade.eperusteet.dto.peruste.PerusteenJulkaisuData;
import fi.vm.sade.eperusteet.dto.peruste.TutkintonimikeKoodiDto;
import fi.vm.sade.eperusteet.dto.tutkinnonrakenne.KoodiDto;
import fi.vm.sade.eperusteet.repository.JulkaisutRepository;
import fi.vm.sade.eperusteet.repository.KoodiRepository;
import fi.vm.sade.eperusteet.repository.PerusteRepository;
import fi.vm.sade.eperusteet.repository.PerusteprojektiRepository;
import fi.vm.sade.eperusteet.repository.TutkintonimikeKoodiRepository;
import fi.vm.sade.eperusteet.resource.config.InitJacksonConverter;
import fi.vm.sade.eperusteet.service.AmmattitaitovaatimusService;
import fi.vm.sade.eperusteet.service.JulkaisutService;
import fi.vm.sade.eperusteet.service.KayttajanTietoService;
import fi.vm.sade.eperusteet.service.KoodistoClient;
import fi.vm.sade.eperusteet.service.PerusteService;
import fi.vm.sade.eperusteet.service.PerusteenMuokkaustietoService;
import fi.vm.sade.eperusteet.service.PerusteprojektiService;
import fi.vm.sade.eperusteet.service.dokumentti.DokumenttiService;
import fi.vm.sade.eperusteet.service.event.aop.IgnorePerusteUpdateCheck;
import fi.vm.sade.eperusteet.service.exception.BusinessRuleViolationException;
import fi.vm.sade.eperusteet.service.exception.DokumenttiException;
import fi.vm.sade.eperusteet.service.mapping.Dto;
import fi.vm.sade.eperusteet.service.mapping.DtoMapper;
import fi.vm.sade.eperusteet.utils.domain.utils.Tila;
import lombok.extern.slf4j.Slf4j;
import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import org.joda.time.DateTime;
import org.json.JSONException;
import org.skyscreamer.jsonassert.JSONCompare;
import org.skyscreamer.jsonassert.JSONCompareMode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toSet;

@Slf4j
@Service
@Transactional
@Profile("default")
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

    private final ObjectMapper objectMapper = InitJacksonConverter.createMapper();
    private static final List<String> pdfEnabled = Arrays.asList(
            "koulutustyyppi_1", "koulutustyyppi_5", "koulutustyyppi_6", "koulutustyyppi_11",
            "koulutustyyppi_12", "koulutustyyppi_15", "koulutustyyppi_17", "koulutustyyppi_18",
            "koulutustyyppi_20", "koulutustyyppi_999907", "koulutustyyppi_10", "koulutustyyppi_40");

    @Override
    public List<JulkaisuBaseDto> getJulkaisut(long id) {
        Peruste peruste = perusteRepository.findOne(id);
        if (peruste == null) {
            throw new BusinessRuleViolationException("perustetta-ei-loytynyt");
        }

        List<JulkaistuPeruste> one = julkaisutRepository.findAllByPeruste(peruste);
        List<JulkaisuBaseDto> julkaisut = mapper.mapAsList(one, JulkaisuBaseDto.class);
        return taytaKayttajaTiedot(julkaisut);
    }

    @Override
    @IgnorePerusteUpdateCheck
    public JulkaisuBaseDto teeJulkaisu(long projektiId, JulkaisuBaseDto julkaisuBaseDto) {
        Perusteprojekti perusteprojekti = perusteprojektiRepository.findOne(projektiId);

        if (perusteprojekti == null) {
            throw new BusinessRuleViolationException("projektia-ei-ole");
        }

        Peruste peruste = perusteprojekti.getPeruste();

        if (peruste == null) {
            throw new BusinessRuleViolationException("perustetta-ei-ole");
        }

        if (julkaisuBaseDto.getPeruste() != null && !Objects.equals(peruste.getId(), julkaisuBaseDto.getPeruste().getId())) {
            throw new BusinessRuleViolationException("vain-oman-perusteen-voi-julkaista");
        }

        // Validoinnit
        TilaUpdateStatus status = perusteprojektiService.validoiProjekti(projektiId, ProjektiTila.JULKAISTU);

        if (!salliVirheelliset && !status.isVaihtoOk()) {
            throw new BusinessRuleViolationException("projekti-ei-validi");
        }

        long julkaisutCount = julkaisutRepository.countByPeruste(peruste);
        if (julkaisutCount > 0 && !onkoMuutoksia(peruste.getId())) {
            throw new BusinessRuleViolationException("ei-muuttunut-viime-julkaisun-jalkeen");
        }

        // Aseta peruste julkaistuksi jos ei jo ole (peruste ei saa olla)
        peruste.asetaTila(PerusteTila.VALMIS);
        peruste.getPerusteprojekti().setTila(ProjektiTila.JULKAISTU);

        kooditaValiaikaisetKoodit(peruste);
        PerusteKaikkiDto sisalto = perusteService.getKaikkiSisalto(peruste.getId());
        ObjectNode perusteDataJson = objectMapper.valueToTree(sisalto);

        Set<Long> dokumentit = new HashSet<>();

        if (pdfEnabled.contains(peruste.getKoulutustyyppi())) {

            Set<Suoritustapakoodi> suoritustavat = peruste.getSuoritustavat().stream().map(Suoritustapa::getSuoritustapakoodi).collect(toSet());
            if (suoritustavat.isEmpty()) {
                if (peruste.getTyyppi().equals(PerusteTyyppi.OPAS)) {
                    suoritustavat.add(Suoritustapakoodi.OPAS);
                } else {
                    suoritustavat.add(Suoritustapakoodi.REFORMI);
                }
            }

            dokumentit.addAll(suoritustavat.stream()
                    .map(suoritustapa -> peruste.getKielet().stream()
                            .map(kieli -> {
                                try {
                                    DokumenttiDto createDtoFor = dokumenttiService.createDtoFor(
                                            peruste.getId(),
                                            kieli,
                                            suoritustapa,
                                            GeneratorVersion.UUSI
                                    );
                                    dokumenttiService.generateWithDto(createDtoFor);
                                    return createDtoFor.getId();
                                } catch (DokumenttiException e) {
                                    log.error(e.getLocalizedMessage(), e);
                                }

                                return null;
                            })
                            .collect(toSet()))
                    .filter(id -> id != null)
                    .flatMap(Collection::stream)
                    .collect(toSet()));
        }

        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        JulkaistuPeruste julkaisu = new JulkaistuPeruste();
        julkaisu.setRevision((int) julkaisutCount);
        julkaisu.setTiedote(TekstiPalanen.of(julkaisuBaseDto.getTiedote().getTekstit()));
        julkaisu.setLuoja(username);
        julkaisu.setLuotu(new Date());
        julkaisu.setPeruste(peruste);

        if (!dokumentit.isEmpty()) {
            julkaisu.setDokumentit(dokumentit);
        }

        julkaisu.setData(new JulkaistuPerusteData(perusteDataJson));
        julkaisu = julkaisutRepository.save(julkaisu);
        {
            Cache amosaaperusteet = CacheManager.getInstance().getCache("amosaaperusteet");
            if (amosaaperusteet != null) {
                amosaaperusteet.removeAll();
            }
        }

        muokkausTietoService.addMuokkaustieto(peruste.getId(), peruste, MuokkausTapahtuma.JULKAISU);

        return taytaKayttajaTiedot(mapper.map(julkaisu, JulkaisuBaseDto.class));
    }

    @Override
    public boolean onkoMuutoksia(long perusteId) {
        try {
            Peruste peruste = perusteRepository.findOne(perusteId);
            JulkaistuPeruste viimeisinJulkaisu = julkaisutRepository.findFirstByPerusteOrderByRevisionDesc(peruste);

            if (viimeisinJulkaisu == null) {
                return false;
            }

            ObjectNode data = viimeisinJulkaisu.getData().getData();
            String julkaistu = generoiOpetussuunnitelmaKaikkiDtotoString(objectMapper.treeToValue(data, PerusteKaikkiDto.class));
            String nykyinen = generoiOpetussuunnitelmaKaikkiDtotoString(perusteService.getKaikkiSisalto(peruste.getId()));

            return JSONCompare.compareJSON(julkaistu, nykyinen, JSONCompareMode.LENIENT).failed();
        } catch (IOException | JSONException e) {
            log.error(Throwables.getStackTraceAsString(e));
            throw new BusinessRuleViolationException("onko-muutoksia-julkaisuun-verrattuna-tarkistus-epaonnistui");
        }
    }

    private String generoiOpetussuunnitelmaKaikkiDtotoString(PerusteKaikkiDto perusteKaikkiDto) throws IOException {
        perusteKaikkiDto.setViimeisinJulkaisuAika(null);
        perusteKaikkiDto.setTila(null);
        return objectMapper.writeValueAsString(perusteKaikkiDto);
    }

    @Override
    @IgnorePerusteUpdateCheck
    public JulkaisuBaseDto aktivoiJulkaisu(long projektiId, int revision) {
        Perusteprojekti perusteprojekti = perusteprojektiRepository.findOne(projektiId);

        if (perusteprojekti == null) {
            throw new BusinessRuleViolationException("projektia-ei-ole");
        }

        Peruste peruste = perusteprojekti.getPeruste();
        JulkaistuPeruste vanhaJulkaisu = julkaisutRepository.findFirstByPerusteAndRevisionOrderByIdDesc(peruste, revision);
        long julkaisutCount = julkaisutRepository.countByPeruste(peruste);

        JulkaistuPeruste julkaisu = new JulkaistuPeruste();
        julkaisu.setRevision((int) julkaisutCount);
        julkaisu.setTiedote(vanhaJulkaisu.getTiedote());
        julkaisu.setDokumentit(Sets.newHashSet(vanhaJulkaisu.getDokumentit()));
        julkaisu.setPeruste(peruste);
        julkaisu.setData(vanhaJulkaisu.getData());
        julkaisu = julkaisutRepository.save(julkaisu);
        muokkausTietoService.addMuokkaustieto(peruste.getId(), peruste, MuokkausTapahtuma.JULKAISU);

        return taytaKayttajaTiedot(mapper.map(julkaisu, JulkaisuBaseDto.class));
    }

    @Override
    public Page<PerusteenJulkaisuData> getJulkisetJulkaisut(List<String> koulutustyyppi, String nimi, String kieli, String tyyppi, boolean tulevat,
                                                            boolean voimassa, boolean siirtyma, boolean poistuneet, boolean koulutusvienti, String diaarinumero,
                                                            String koodi, Integer sivu, Integer sivukoko) {
        Pageable pageable = new PageRequest(sivu, sivukoko);
        Long currentMillis = DateTime.now().getMillis();
        if (CollectionUtils.isEmpty((koulutustyyppi))) {
            koulutustyyppi = Arrays.stream(KoulutusTyyppi.values()).map(KoulutusTyyppi::toString).collect(Collectors.toList());
        }
        return julkaisutRepository.findAllJulkisetJulkaisut(
                koulutustyyppi,
                nimi,
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
                pageable)
                .map(obj -> {
                    try {
                        return objectMapper.readValue(obj, PerusteenJulkaisuData.class);
                    } catch (IOException e) {
                        e.printStackTrace();
                        throw new RuntimeException(e);
                    }
                });
    }

    @Override
    public Date viimeisinPerusteenJulkaisuaika(Long perusteId) {
        JulkaistuPeruste viimeisinJulkaisu = julkaisutRepository.findFirstByPerusteIdOrderByRevisionDesc(perusteId);
        if (viimeisinJulkaisu != null) {
            return viimeisinJulkaisu.getLuotu();
        } else {
            Peruste peruste = perusteRepository.findOne(perusteId);
            if (peruste != null && (peruste.getTila().equals(Tila.JULKAISTU) || peruste.getTyyppi().equals(PerusteTyyppi.AMOSAA_YHTEINEN))) {
                return peruste.getGlobalVersion().getAikaleima();
            }
        }

        return null;
    }

    private void kooditaValiaikaisetKoodit(Peruste peruste) {
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

        ammattitaitovaatimusService.addAmmattitaitovaatimuskooditToKoodisto(peruste.getPerusteprojekti().getId(), peruste.getId());
    }

    private List<JulkaisuBaseDto> taytaKayttajaTiedot(List<JulkaisuBaseDto> julkaisut) {
        Map<String, KayttajanTietoDto> kayttajatiedot = kayttajanTietoService
                .haeKayttajatiedot(julkaisut.stream().map(JulkaisuBaseDto::getLuoja).collect(Collectors.toList()))
                .stream().collect(Collectors.toMap(kayttajanTieto -> kayttajanTieto.getOidHenkilo(), kayttajanTieto -> kayttajanTieto));
        julkaisut.forEach(julkaisu -> julkaisu.setKayttajanTieto(kayttajatiedot.get(julkaisu.getLuoja())));
        return julkaisut;
    }

    private JulkaisuBaseDto taytaKayttajaTiedot(JulkaisuBaseDto julkaisu) {
        return taytaKayttajaTiedot(Arrays.asList(julkaisu)).get(0);
    }

}
