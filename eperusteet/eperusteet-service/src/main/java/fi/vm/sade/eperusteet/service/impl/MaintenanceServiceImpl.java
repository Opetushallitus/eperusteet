package fi.vm.sade.eperusteet.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.collect.Lists;
import fi.vm.sade.eperusteet.domain.JulkaistuPeruste;
import fi.vm.sade.eperusteet.domain.JulkaistuPerusteData;
import fi.vm.sade.eperusteet.domain.Kieli;
import fi.vm.sade.eperusteet.domain.Koodi;
import fi.vm.sade.eperusteet.domain.KoulutusTyyppi;
import fi.vm.sade.eperusteet.domain.Peruste;
import fi.vm.sade.eperusteet.domain.PerusteTyyppi;
import fi.vm.sade.eperusteet.domain.PerusteenOsaViite;
import fi.vm.sade.eperusteet.domain.ProjektiTila;
import fi.vm.sade.eperusteet.domain.Suoritustapakoodi;
import fi.vm.sade.eperusteet.domain.TekstiKappale;
import fi.vm.sade.eperusteet.domain.TekstiPalanen;
import fi.vm.sade.eperusteet.domain.Yllapito;
import fi.vm.sade.eperusteet.domain.maarays.Maarays;
import fi.vm.sade.eperusteet.domain.maarays.MaaraysAsiasana;
import fi.vm.sade.eperusteet.domain.maarays.MaaraysKieliLiitteet;
import fi.vm.sade.eperusteet.domain.maarays.MaaraysLiittyyTyyppi;
import fi.vm.sade.eperusteet.domain.maarays.MaaraysTila;
import fi.vm.sade.eperusteet.domain.maarays.MaaraysTyyppi;
import fi.vm.sade.eperusteet.dto.YllapitoDto;
import fi.vm.sade.eperusteet.dto.peruste.PerusteKaikkiDto;
import fi.vm.sade.eperusteet.dto.peruste.PerusteenOsaViiteDto;
import fi.vm.sade.eperusteet.dto.peruste.TekstiKappaleDto;
import fi.vm.sade.eperusteet.dto.tutkinnonrakenne.KoodiDto;
import fi.vm.sade.eperusteet.repository.JulkaistuPerusteDataStoreRepository;
import fi.vm.sade.eperusteet.repository.JulkaisutRepository;
import fi.vm.sade.eperusteet.repository.MaaraysRepository;
import fi.vm.sade.eperusteet.repository.PerusteRepository;
import fi.vm.sade.eperusteet.repository.YllapitoRepository;
import fi.vm.sade.eperusteet.resource.config.InitJacksonConverter;
import fi.vm.sade.eperusteet.service.JulkaisutService;
import fi.vm.sade.eperusteet.service.MaintenanceService;
import fi.vm.sade.eperusteet.service.PerusteService;
import fi.vm.sade.eperusteet.service.PerusteenOsaViiteService;
import fi.vm.sade.eperusteet.service.event.aop.IgnorePerusteUpdateCheck;
import fi.vm.sade.eperusteet.service.mapping.Dto;
import fi.vm.sade.eperusteet.service.mapping.DtoMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Queue;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@Transactional
@Slf4j
public class MaintenanceServiceImpl implements MaintenanceService {

    @Autowired
    private JulkaisutRepository julkaisutRepository;

    @Autowired
    private PerusteRepository perusteRepository;

    @Autowired
    private PerusteService perusteService;

    @Autowired
    private PerusteenOsaViiteService perusteenOsaViiteService;

    @Autowired
    private PlatformTransactionManager ptm;

    @Autowired
    private YllapitoRepository yllapitoRepository;

    @Autowired
    private CacheManager cacheManager;

    @Autowired
    @Dto
    private DtoMapper mapper;

    @Autowired
    private JulkaisutService julkaisutService;

    @Autowired
    private MaaraysRepository maaraysRepository;

    @Autowired
    private JulkaistuPerusteDataStoreRepository julkaistuPerusteDataStoreRepository;

    private final ObjectMapper objectMapper = InitJacksonConverter.createMapper();

    @Override
    public void addMissingOsaamisalakuvaukset() {
        Set<Peruste> korjattavat = perusteRepository.findAllPerusteet().stream()
                .filter(peruste -> ProjektiTila.JULKAISTU.equals(peruste.getPerusteprojekti().getTila()))
                .filter(peruste -> peruste.getVoimassaoloLoppuu() == null || peruste.getVoimassaoloLoppuu().before(new Date()))
                .filter(peruste -> peruste.getKoulutustyyppi() != null && KoulutusTyyppi.of(peruste.getKoulutustyyppi()).isAmmatillinen())
                .filter(peruste -> peruste.getSuoritustavat().size() == 1
                        && Suoritustapakoodi.REFORMI.equals(peruste.getSuoritustavat().iterator().next().getSuoritustapakoodi()))
                .filter(peruste -> peruste.getOsaamisalat() != null && !peruste.getOsaamisalat().isEmpty())
                .collect(Collectors.toSet());

        log.info("Mahdollisesti korjattavien perusteiden määrä: " + korjattavat.size());

        for (Peruste peruste : korjattavat) {
            PerusteenOsaViite sisalto = peruste.getSisallot(Suoritustapakoodi.REFORMI);
            Set<Koodi> osaamisalat = new HashSet<>(peruste.getOsaamisalat());
            int kaikkiOsaamisalat = osaamisalat.size();

            Queue<PerusteenOsaViite> nodes = new LinkedList<>();
            nodes.add(sisalto);
            while (!nodes.isEmpty()) {
                PerusteenOsaViite head = nodes.poll();
                if (head.getLapset() != null) {
                    nodes.addAll(head.getLapset());
                }

                if (head.getPerusteenOsa() instanceof TekstiKappale) {
                    TekstiKappale tk = (TekstiKappale) head.getPerusteenOsa();
                    if (tk.getOsaamisala() != null) {
                        osaamisalat.remove(tk.getOsaamisala());
                    }
                }
            }

            if (!osaamisalat.isEmpty()) {
                log.info("Lisätään puuttuvat osaamisalakuvaukset perusteeseen: "
                        + peruste.getDiaarinumero().getDiaarinumero() + " " + peruste.getPerusteprojekti().getId() + " ("
                        + osaamisalat.stream()
                            .map(Koodi::getUri)
                            .reduce((a, b) -> a + ", " + b)
                            .get()
                        + " | " + osaamisalat.size() + "/" + kaikkiOsaamisalat + ")");
                for (Koodi oa : osaamisalat) {
                    KoodiDto mapped = mapper.map(oa, KoodiDto.class);
                    TekstiKappaleDto tk = new TekstiKappaleDto();
                    tk.setOsaamisala(mapped);
                    tk.setNimi(mapped.getNimi());
                    PerusteenOsaViiteDto.Matala viite = new PerusteenOsaViiteDto.Matala();
                    viite.setPerusteenOsa(tk);
                    perusteenOsaViiteService.addSisaltoJulkaistuun(peruste.getId(), sisalto.getId(), viite);
                }
            }
            else {
                log.info("Osaamisalakuvaukset löydetty: " + peruste.getDiaarinumero().getDiaarinumero() + " " + peruste.getPerusteprojekti().getId());
            }
        }
        log.info("Osaamisalakuvaukset lisätty");
    }

    @Override
    @Async
    @IgnorePerusteUpdateCheck
    @Transactional(propagation = Propagation.NEVER)
    public void teeJulkaisut(boolean julkaiseKaikki, String tyyppi, String koulutustyyppi, String tiedote) {
        List<Long> perusteet = perusteRepository.findJulkaistutPerusteet(PerusteTyyppi.of(tyyppi), koulutustyyppi).stream()
                .filter(peruste -> julkaiseKaikki || CollectionUtils.isEmpty(peruste.getJulkaisut()))
                .map(Peruste::getId)
                .collect(Collectors.toList());

        log.info("julkaistavia perusteita " + perusteet.size());

        for (Long perusteId : perusteet) {
            try {
                julkaisePeruste(perusteId, tiedote);
            } catch (RuntimeException ex) {
                log.error(ex.getLocalizedMessage(), ex);
            }
        }

        log.info("julkaisut tehty");
    }

    @Override
    @IgnorePerusteUpdateCheck
    @Transactional(propagation = Propagation.NEVER)
    public void teeJulkaisu(long perusteId, String tiedote) {

        try {
            julkaisePeruste(perusteId, tiedote);
        } catch (RuntimeException ex) {
            log.error(ex.getLocalizedMessage(), ex);
        }

        log.info("julkaisut tehty");
    }

    @Override
    public List<YllapitoDto> getYllapidot() {
        return mapper.mapAsList(yllapitoRepository.findAll(), YllapitoDto.class);
    }

    @Override
    @Cacheable("yllapitovalues")
    @IgnorePerusteUpdateCheck
    public String getYllapitoValue(String key) {
        Yllapito yllapito = yllapitoRepository.findByKey(key);
        return yllapito != null ? yllapito.getValue() : null;
    }

    @Override
    public void updateYllapito(List<YllapitoDto> yllapitoList) {
        clearCache("yllapitovalues");
        yllapitoList.forEach(yp -> {
            Yllapito yllapito = yllapitoRepository.findOne(yp.getId());
            yllapito.setKey(yp.getKey());
            yllapito.setValue(yp.getValue());
            yllapito.setKuvaus(yp.getKuvaus());
            yllapitoRepository.save(yllapito);
        });
    }

    private void julkaisePeruste(Long perusteId, String tiedote) {
        TransactionTemplate template = new TransactionTemplate(ptm);
        template.execute(status -> {
            Peruste peruste = perusteRepository.findOne(perusteId);
            List<JulkaistuPeruste> julkaisut = julkaisutRepository.findAllByPeruste(peruste);

            log.info("Luodaan julkaisu perusteelle: " + peruste.getId());

            PerusteKaikkiDto sisalto = perusteService.getKaikkiSisalto(peruste.getId());
            Set<Long> dokumentit = julkaisutService.generoiJulkaisuPdf(peruste);
            JulkaistuPeruste julkaisu = new JulkaistuPeruste();
            julkaisu.setRevision(julkaisutService.seuraavaVapaaJulkaisuNumero(perusteId));
            julkaisu.setTiedote(TekstiPalanen.of(Kieli.FI, tiedote));
            julkaisu.setLuoja("maintenance");
            julkaisu.setLuotu(new Date());
            julkaisu.setPeruste(peruste);
            julkaisu.setJulkinen(true);
            julkaisu.setDokumentit(dokumentit);

            ObjectNode data = objectMapper.valueToTree(sisalto);
            julkaisu.setData(new JulkaistuPerusteData(data));
            julkaisutRepository.save(julkaisu);
            julkaistuPerusteDataStoreRepository.syncPeruste(peruste.getId());
            return true;
        });
    }

    @Override
    @IgnorePerusteUpdateCheck
    public void clearCache(String cache) {
        Objects.requireNonNull(cacheManager.getCache(cache)).clear();
    }

    @Override
    public void teeMaarayksetPerusteille() {
        List<Peruste> perusteet = perusteRepository.findAllByEiMaaraystaEiPoistettu();
        List<Maarays> maaraykset = perusteet.stream().map(this::perusteMaarays).collect(Collectors.toList());

        Lists.partition(maaraykset, 10).forEach(maarayksetSubList -> {
            TransactionTemplate txTemplate = new TransactionTemplate(ptm);
            txTemplate.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
            txTemplate.execute(status -> {
                maaraysRepository.save(maarayksetSubList);
                return true;
            });
        });
    }

    private Maarays perusteMaarays(Peruste peruste) {
        Maarays maarays = new Maarays();
        maarays.setPeruste(peruste);
        maarays.setTyyppi(MaaraysTyyppi.PERUSTE);
        maarays.setLiittyyTyyppi(MaaraysLiittyyTyyppi.EI_LIITY);
        maarays.setAsiasanat(Stream.of(Kieli.FI, Kieli.SV, Kieli.EN).collect(Collectors.toMap(kieli -> kieli, kieli -> new MaaraysAsiasana())));
        maarays.setLiitteet(Stream.of(Kieli.FI, Kieli.SV, Kieli.EN).collect(Collectors.toMap(kieli -> kieli, kieli -> new MaaraysKieliLiitteet())));
        maarays.setKorvattavatMaaraykset(new ArrayList<>());
        maarays.setMuutettavatMaaraykset(new ArrayList<>());
        maarays.setKoulutustyypit(List.of(peruste.getKoulutustyyppi()));

        Optional<JulkaistuPeruste> julkaisu = peruste.getJulkaisut().stream().max(Comparator.comparing(JulkaistuPeruste::getLuotu));
        if (julkaisu.isPresent()) {
            PerusteKaikkiDto perusteKaikkiDto = null;

            try {
                perusteKaikkiDto = objectMapper.treeToValue(julkaisu.get().getData().getData(), PerusteKaikkiDto.class);
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }

            maarays.setTila(MaaraysTila.JULKAISTU);
            maarays.setNimi(TekstiPalanen.of(perusteKaikkiDto.getNimi().getTekstit()));
            maarays.setDiaarinumero(perusteKaikkiDto.getDiaarinumero());
            maarays.setVoimassaoloAlkaa(perusteKaikkiDto.getVoimassaoloAlkaa());
            maarays.setVoimassaoloLoppuu(perusteKaikkiDto.getVoimassaoloLoppuu());
            maarays.setMaarayspvm(perusteKaikkiDto.getPaatospvm());
        } else {
            maarays.setTila(MaaraysTila.LUONNOS);
            if (peruste.getNimi() == null) {
                maarays.setNimi(TekstiPalanen.of(Kieli.FI, "Nimetön määräys"));
            } else {
                maarays.setNimi(TekstiPalanen.of(peruste.getNimi()));
            }
            if (peruste.getDiaarinumero() != null) {
                maarays.setDiaarinumero(peruste.getDiaarinumero().getDiaarinumero());
            }
            maarays.setVoimassaoloAlkaa(peruste.getVoimassaoloAlkaa());
            maarays.setVoimassaoloLoppuu(peruste.getVoimassaoloLoppuu());
            maarays.setMaarayspvm(peruste.getPaatospvm());
        }

        return maarays;
    }
}
