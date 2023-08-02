package fi.vm.sade.eperusteet.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import fi.vm.sade.eperusteet.domain.*;
import fi.vm.sade.eperusteet.dto.YllapitoDto;
import fi.vm.sade.eperusteet.dto.peruste.PerusteKaikkiDto;
import fi.vm.sade.eperusteet.dto.peruste.PerusteenOsaViiteDto;
import fi.vm.sade.eperusteet.dto.peruste.TekstiKappaleDto;
import fi.vm.sade.eperusteet.dto.tutkinnonrakenne.KoodiDto;
import fi.vm.sade.eperusteet.repository.JulkaisutRepository;
import fi.vm.sade.eperusteet.repository.PerusteRepository;
import fi.vm.sade.eperusteet.repository.YllapitoRepository;
import fi.vm.sade.eperusteet.resource.config.InitJacksonConverter;
import fi.vm.sade.eperusteet.service.*;
import fi.vm.sade.eperusteet.service.event.aop.IgnorePerusteUpdateCheck;
import fi.vm.sade.eperusteet.service.mapping.Dto;
import fi.vm.sade.eperusteet.service.mapping.DtoMapper;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.annotations.Cache;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.*;
import java.util.stream.Collectors;
import org.springframework.util.CollectionUtils;

@Service
@Transactional
@Slf4j
@Profile("!test")
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
            return true;
        });
    }

    @Override
    @IgnorePerusteUpdateCheck
    public void clearCache(String cache) {
        Objects.requireNonNull(cacheManager.getCache(cache)).clear();
    }
}
