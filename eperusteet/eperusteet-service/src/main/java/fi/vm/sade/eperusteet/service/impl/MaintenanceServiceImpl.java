package fi.vm.sade.eperusteet.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import fi.vm.sade.eperusteet.domain.*;
import fi.vm.sade.eperusteet.dto.peruste.PerusteKaikkiDto;
import fi.vm.sade.eperusteet.dto.peruste.PerusteenOsaViiteDto;
import fi.vm.sade.eperusteet.dto.peruste.TekstiKappaleDto;
import fi.vm.sade.eperusteet.dto.tutkinnonrakenne.KoodiDto;
import fi.vm.sade.eperusteet.dto.util.LokalisoituTekstiDto;
import fi.vm.sade.eperusteet.repository.JulkaisutRepository;
import fi.vm.sade.eperusteet.repository.PerusteRepository;
import fi.vm.sade.eperusteet.repository.PerusteprojektiRepository;
import fi.vm.sade.eperusteet.resource.config.InitJacksonConverter;
import fi.vm.sade.eperusteet.service.*;
import fi.vm.sade.eperusteet.service.impl.validators.ValidointiTask;
import fi.vm.sade.eperusteet.service.mapping.Dto;
import fi.vm.sade.eperusteet.service.mapping.DtoMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
@Slf4j
@Profile("!test")
public class MaintenanceServiceImpl implements MaintenanceService {

    @Autowired
    private JulkaisutRepository julkaisutRepository;

    @Autowired
    private PerusteprojektiRepository perusteprojektiRepository;

    @Autowired
    private PerusteRepository perusteRepository;

    @Autowired
    private PerusteService perusteService;

    @Autowired
    private PerusteenOsaViiteService perusteenOsaViiteService;

    @Autowired
    private ValidointiTask validointiTask;

    @Autowired
    private PlatformTransactionManager ptm;

    @Autowired
    @Dto
    private DtoMapper mapper;

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
            PerusteenOsaViite sisalto = peruste.getSisalto(Suoritustapakoodi.REFORMI);
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
                    tk.setNimi(new LokalisoituTekstiDto(mapped.getNimi()));
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
    @Transactional(propagation = Propagation.NEVER)
    public void runValidointi() {
        validointiTask.execute();
    }

    @Override
    @Transactional(propagation = Propagation.NEVER)
    public void teeJulkaisut() {
        List<Perusteprojekti> projektit = perusteprojektiRepository.findAllByTilaAndPerusteTyyppi(ProjektiTila.JULKAISTU, PerusteTyyppi.NORMAALI);
        List<Long> perusteet = projektit.stream()
                .map(Perusteprojekti::getPeruste)
                .map(Peruste::getId)
                .collect(Collectors.toList());

        String username = SecurityContextHolder.getContext().getAuthentication().getName();

        for (Long perusteId : perusteet) {
            try {
                teeJulkaisu(username, perusteId);
            }
            catch (RuntimeException ex) {
                log.error(ex.getLocalizedMessage(), ex);
            }
        }
    }

    @Transactional(propagation = Propagation.NEVER)
    private void teeJulkaisu(String username, Long perusteId) {
        TransactionTemplate template = new TransactionTemplate(ptm);
        template.execute(status -> {
            Peruste peruste = perusteRepository.findOne(perusteId);
            PerusteVersion version = peruste.getGlobalVersion();
            List<JulkaistuPeruste> julkaisut = julkaisutRepository.findAllByPeruste(peruste);
            if (julkaisut.stream().anyMatch(julkaisu -> julkaisu.getLuotu().compareTo(version.getAikaleima()) > 0)) {
                log.info("Perusteella jo julkaisu: " + peruste.getId());
                return true;
            }

            log.info("Luodaan julkaisu perusteelle: " + peruste.getId());

            PerusteKaikkiDto sisalto = perusteService.getKokoSisalto(peruste.getId());
            JulkaistuPeruste julkaisu = new JulkaistuPeruste();
            julkaisu.setRevision(julkaisut.size());
            julkaisu.setLuoja("");
            julkaisu.setTiedote(TekstiPalanen.of(Kieli.FI, "Julkaisu"));
            julkaisu.setLuoja(username);
            julkaisu.setLuotu(version.getAikaleima());
            julkaisu.setPeruste(peruste);

            ObjectNode data = objectMapper.valueToTree(sisalto);
            julkaisu.setData(new JulkaistuPerusteData(data));
            julkaisutRepository.save(julkaisu);
            return true;
        });
    }
}
