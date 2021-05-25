package fi.vm.sade.eperusteet.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.collect.Sets;
import fi.vm.sade.eperusteet.domain.*;
import fi.vm.sade.eperusteet.dto.DokumenttiDto;
import fi.vm.sade.eperusteet.dto.TilaUpdateStatus;
import fi.vm.sade.eperusteet.dto.kayttaja.KayttajanTietoDto;
import fi.vm.sade.eperusteet.dto.koodisto.KoodistoKoodiDto;
import fi.vm.sade.eperusteet.dto.peruste.JulkaisuBaseDto;
import fi.vm.sade.eperusteet.dto.peruste.PerusteDto;
import fi.vm.sade.eperusteet.dto.peruste.PerusteKaikkiDto;
import fi.vm.sade.eperusteet.dto.peruste.TutkintonimikeKoodiDto;
import fi.vm.sade.eperusteet.dto.tutkinnonrakenne.KoodiDto;
import fi.vm.sade.eperusteet.dto.util.LokalisoituTekstiDto;
import fi.vm.sade.eperusteet.repository.JulkaisutRepository;
import fi.vm.sade.eperusteet.repository.KoodiRepository;
import fi.vm.sade.eperusteet.repository.PerusteRepository;
import fi.vm.sade.eperusteet.repository.PerusteprojektiRepository;
import fi.vm.sade.eperusteet.repository.TutkintonimikeKoodiRepository;
import fi.vm.sade.eperusteet.resource.config.InitJacksonConverter;
import fi.vm.sade.eperusteet.service.JulkaisutService;
import fi.vm.sade.eperusteet.service.KayttajanTietoService;
import fi.vm.sade.eperusteet.service.KoodistoClient;
import fi.vm.sade.eperusteet.service.PerusteService;
import fi.vm.sade.eperusteet.service.PerusteenMuokkaustietoService;
import fi.vm.sade.eperusteet.service.PerusteprojektiService;
import fi.vm.sade.eperusteet.service.dokumentti.DokumenttiService;
import fi.vm.sade.eperusteet.service.exception.BusinessRuleViolationException;
import fi.vm.sade.eperusteet.service.exception.DokumenttiException;
import fi.vm.sade.eperusteet.service.mapping.Dto;
import fi.vm.sade.eperusteet.service.mapping.DtoMapper;
import fi.vm.sade.eperusteet.service.util.JsonMapper;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import org.springframework.util.ObjectUtils;

import static java.util.stream.Collectors.toSet;

@Slf4j
@Service
@Transactional
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
        PerusteKaikkiDto sisalto = perusteService.getKaikkiSisalto(peruste.getId());
        ObjectNode perusteDataJson = objectMapper.valueToTree(sisalto);
        List<JulkaistuPeruste> julkaisut = julkaisutRepository.findAllByPerusteOrderByRevisionDesc(perusteprojekti.getPeruste());
        if (julkaisut != null && julkaisut.size() > 0) {
            JulkaistuPeruste last = julkaisut.get(0);
            if (last.getData().getHash() == perusteDataJson.hashCode()) {
                throw new BusinessRuleViolationException("ei-muuttunut-viime-julkaisun-jalkeen");
            }
        }

        TilaUpdateStatus status = perusteprojektiService.validoiProjekti(projektiId, ProjektiTila.JULKAISTU);

        if (!salliVirheelliset && !status.isVaihtoOk()) {
            throw new BusinessRuleViolationException("projekti-ei-validi");
        }

        // Aseta peruste julkaistuksi jos ei jo ole (peruste ei saa olla)
        peruste.asetaTila(PerusteTila.VALMIS);
        peruste.getPerusteprojekti().setTila(ProjektiTila.JULKAISTU);

        PerusteVersion version = peruste.getGlobalVersion();
        long julkaisutCount = julkaisutRepository.countByPeruste(peruste);
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

            kooditaValiaikaisetKoodit(peruste);

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
        julkaisu.setLuotu(version.getAikaleima());
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

    private void kooditaValiaikaisetKoodit(Peruste peruste) {

        peruste.getOsaamisalat().stream()
                .filter(osaamisala -> osaamisala.isTemporary())
                .forEach(osaamisala -> {
                    KoodiDto osaamisalaKoodi = mapper.map(osaamisala, KoodiDto.class);
                    KoodistoKoodiDto lisattyKoodi = koodistoClient.addKoodiNimella("osaamisala", osaamisalaKoodi.getNimi());

                    osaamisala.setUri(lisattyKoodi.getKoodiUri());
                    osaamisala.setKoodisto("osaamisala");
                    osaamisala.setVersio(lisattyKoodi.getVersio() != null ? Long.valueOf(lisattyKoodi.getVersio()) : null);
                    osaamisala.setNimi(null);
                    koodiRepository.save(osaamisala);
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
