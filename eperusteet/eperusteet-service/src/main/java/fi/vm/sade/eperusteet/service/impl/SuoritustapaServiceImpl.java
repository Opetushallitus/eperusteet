package fi.vm.sade.eperusteet.service.impl;

import fi.vm.sade.eperusteet.domain.LaajuusYksikko;
import fi.vm.sade.eperusteet.domain.PerusteenOsaTunniste;
import fi.vm.sade.eperusteet.domain.PerusteenOsaViite;
import fi.vm.sade.eperusteet.domain.Suoritustapa;
import fi.vm.sade.eperusteet.domain.Suoritustapakoodi;
import fi.vm.sade.eperusteet.domain.tutkinnonrakenne.AbstractRakenneOsa;
import fi.vm.sade.eperusteet.domain.tutkinnonrakenne.MuodostumisSaanto;
import fi.vm.sade.eperusteet.domain.tutkinnonrakenne.RakenneModuuli;
import fi.vm.sade.eperusteet.domain.tutkinnonrakenne.RakenneOsa;
import fi.vm.sade.eperusteet.domain.tutkinnonrakenne.TutkinnonOsaViite;
import fi.vm.sade.eperusteet.repository.PerusteenOsaViiteRepository;
import fi.vm.sade.eperusteet.repository.RakenneRepository;
import fi.vm.sade.eperusteet.repository.SuoritustapaRepository;
import fi.vm.sade.eperusteet.repository.TutkinnonOsaViiteRepository;
import fi.vm.sade.eperusteet.service.internal.SuoritustapaService;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SuoritustapaServiceImpl implements SuoritustapaService {

    @Autowired
    private SuoritustapaRepository suoritustapaRepository;

    @Autowired
    private PerusteenOsaViiteRepository perusteenOsaViiteRepository;

    @Autowired
    private RakenneRepository rakenneRepository;

    @Autowired
    private TutkinnonOsaViiteRepository tutkinnonOsaViiteRepository;

    @Transactional
    private Suoritustapa createCommon(Suoritustapakoodi suoritustapakoodi, LaajuusYksikko yksikko) {
        Suoritustapa suoritustapa = new Suoritustapa();
        suoritustapa.setSuoritustapakoodi(suoritustapakoodi);
        suoritustapa.setLaajuusYksikko(yksikko);


        PerusteenOsaViite perusteenOsaViite = perusteenOsaViiteRepository.save(new PerusteenOsaViite());
        perusteenOsaViite.setLapset(new ArrayList<>());
        RakenneModuuli rakenne = rakenneRepository.save(new RakenneModuuli());

        suoritustapa.setSisalto(perusteenOsaViite);
        perusteenOsaViite.setSuoritustapa(suoritustapa);
        suoritustapa.setRakenne(rakenne);

        return suoritustapaRepository.save(suoritustapa);
    }

    @Transactional
    private void kopioiSisalto(PerusteenOsaViite vanha, PerusteenOsaViite parent) {
        List<PerusteenOsaViite> vanhaLapset = vanha.getLapset();
        if (vanhaLapset != null) {
            for (PerusteenOsaViite vanhaPov : vanhaLapset) {
                if (vanhaPov.getPerusteenOsa() != null && vanhaPov.getPerusteenOsa().getTunniste() != PerusteenOsaTunniste.RAKENNE) {
                    PerusteenOsaViite pov = perusteenOsaViiteRepository.save(new PerusteenOsaViite());
                    pov.setLapset(new ArrayList());
                    pov.setVanhempi(parent);
                    pov.setPerusteenOsa(vanhaPov.getPerusteenOsa().copy());
                    parent.getLapset().add(pov);
                    kopioiSisalto(vanhaPov, pov);
                }
            }
        }
    }

    @Transactional
    private RakenneOsa kopioiRakenneOsa(RakenneOsa vanha, Map<TutkinnonOsaViite, TutkinnonOsaViite> viitemap) {
        RakenneOsa osa = new RakenneOsa();
        osa.setPakollinen(vanha.getPakollinen());
        osa.setErikoisuus(vanha.getErikoisuus());
        osa.setTutkinnonOsaViite(viitemap.get(vanha.getTutkinnonOsaViite()));
        osa.setKuvaus(vanha.getKuvaus());
        return osa;
    }

    @Transactional
    private RakenneModuuli kopioiRakenne(RakenneModuuli vanha, Map<TutkinnonOsaViite, TutkinnonOsaViite> viitemap) {
        if (vanha == null) {
            return null;
        }

        RakenneModuuli rm = new RakenneModuuli();
        rm.setKuvaus(vanha.getKuvaus());
        rm.setNimi(vanha.getNimi());
        rm.setRooli(vanha.getRooli());
        rm.setOsaamisala(vanha.getOsaamisala());
        rm.setTutkintonimike(vanha.getTutkintonimike());
        rakenneRepository.save(rm);

        MuodostumisSaanto vanhaMs = vanha.getMuodostumisSaanto();
        if (vanhaMs != null) {
            MuodostumisSaanto ms = new MuodostumisSaanto();
            ms.setKoko(vanhaMs.getKoko());
            ms.setLaajuus(vanhaMs.getLaajuus());
            rm.setMuodostumisSaanto(ms);
        }

        List<AbstractRakenneOsa> vanhatOsat = vanha.getOsat();
        List<AbstractRakenneOsa> osat = rm.getOsat();
        if (osat != null) {
            for (AbstractRakenneOsa aro : vanhatOsat) {
                if (aro instanceof RakenneModuuli) {
                    osat.add(kopioiRakenne((RakenneModuuli)aro, viitemap));
                }
                else if (aro instanceof RakenneOsa) {
                    osat.add(kopioiRakenneOsa((RakenneOsa)aro, viitemap));
                }
            }
        }
        return rm;
    }

    @Override
    @Transactional
    public Suoritustapa createSuoritustapaWithSisaltoAndRakenneRoots(Suoritustapakoodi suoritustapakoodi, LaajuusYksikko yksikko) {
        Suoritustapa suoritustapa = createCommon(suoritustapakoodi, yksikko);
        suoritustapa = suoritustapaRepository.save(suoritustapa);
        return suoritustapa;
    }

    @Override
    @Transactional
    public Suoritustapa createSuoritustapa(Suoritustapakoodi suoritustapakoodi, LaajuusYksikko yksikko) {
        Suoritustapa suoritustapa = new Suoritustapa();
        suoritustapa.setSuoritustapakoodi(suoritustapakoodi);
        suoritustapa.setLaajuusYksikko(yksikko);

        suoritustapa = suoritustapaRepository.save(suoritustapa);
        return suoritustapa;
    }

    @Override
    @Transactional
    public Suoritustapa createFromOther(final Long suoritustapaId) {
        Suoritustapa vanhaSt = suoritustapaRepository.getOne(suoritustapaId);
        Suoritustapa suoritustapa = createCommon(vanhaSt.getSuoritustapakoodi(), vanhaSt.getLaajuusYksikko());
        Set<TutkinnonOsaViite> tosat = suoritustapa.getTutkinnonOsat();
        Set<TutkinnonOsaViite> vanhatTosat = vanhaSt.getTutkinnonOsat();

        Map<TutkinnonOsaViite, TutkinnonOsaViite> viitemap = new HashMap<>();

        for (TutkinnonOsaViite tov : vanhatTosat) {
            TutkinnonOsaViite uusiTov = new TutkinnonOsaViite();
            uusiTov.setSuoritustapa(suoritustapa);
            uusiTov.setTutkinnonOsa(tov.getTutkinnonOsa());
            uusiTov.setJarjestys(tov.getJarjestys());
            uusiTov.setLaajuus(tov.getLaajuus());
            tutkinnonOsaViiteRepository.save(uusiTov);
            tosat.add(uusiTov);
            viitemap.put(tov, uusiTov);
        }

        suoritustapa.setRakenne(kopioiRakenne(vanhaSt.getRakenne(), viitemap));
        if (vanhaSt.getSisalto() != null) {
            kopioiSisalto(vanhaSt.getSisalto(), suoritustapa.getSisalto());
        }
        suoritustapa = suoritustapaRepository.save(suoritustapa);
        return suoritustapa;
    }
}
