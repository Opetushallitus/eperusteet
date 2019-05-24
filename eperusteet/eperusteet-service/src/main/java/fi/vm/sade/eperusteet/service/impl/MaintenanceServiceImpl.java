package fi.vm.sade.eperusteet.service.impl;

import fi.vm.sade.eperusteet.domain.*;
import fi.vm.sade.eperusteet.dto.peruste.PerusteenOsaViiteDto;
import fi.vm.sade.eperusteet.dto.peruste.TekstiKappaleDto;
import fi.vm.sade.eperusteet.dto.tutkinnonrakenne.KoodiDto;
import fi.vm.sade.eperusteet.dto.util.LokalisoituTekstiDto;
import fi.vm.sade.eperusteet.repository.PerusteRepository;
import fi.vm.sade.eperusteet.service.MaintenanceService;
import fi.vm.sade.eperusteet.service.PerusteenOsaViiteService;
import fi.vm.sade.eperusteet.service.mapping.Dto;
import fi.vm.sade.eperusteet.service.mapping.DtoMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
@Slf4j
public class MaintenanceServiceImpl implements MaintenanceService {

    @Autowired
    private PerusteRepository perusteRepository;

    @Autowired
    private PerusteenOsaViiteService perusteenOsaViiteService;

    @Autowired
    @Dto
    private DtoMapper mapper;


    @Override
    public void addMissingOsaamisalakuvaukset() {
        Set<Peruste> korjattavat = perusteRepository.findAllPerusteet().stream()
                .filter(peruste -> ProjektiTila.JULKAISTU.equals(peruste.getPerusteprojekti().getTila()))
//                .filter(peruste -> peruste.getVoimassaoloLoppuu() == null
//                        || (peruste.getSiirtymaPaattyy() != null && peruste.getSiirtymaPaattyy().after(new Date())))
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
}
