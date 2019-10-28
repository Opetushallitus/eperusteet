package fi.vm.sade.eperusteet.service;

import fi.vm.sade.eperusteet.domain.KoulutustyyppiToteutus;
import fi.vm.sade.eperusteet.domain.Peruste;
import fi.vm.sade.eperusteet.domain.annotation.Identifiable;
import fi.vm.sade.eperusteet.repository.PerusteRepository;
import fi.vm.sade.eperusteet.service.exception.BusinessRuleViolationException;
import fi.vm.sade.eperusteet.service.util.PerusteIdentifiable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Component
@Transactional
public class PerusteDispatcher {

    @Autowired
    private PerusteRepository perusteRepository;

    @Autowired
    private List<PerusteToteutus> kaikkiToteutukset;

    private HashMap<Class, PerusteToteutus> defaults = new HashMap<>();
    private Map<Class, HashMap<KoulutustyyppiToteutus, PerusteToteutus>> toteutuksetMap = new HashMap<>();

    @PostConstruct
    public void postConstruct() {
        for (PerusteToteutus toteutus : kaikkiToteutukset) {
            Set<KoulutustyyppiToteutus> toteutukset = toteutus.getTyypit();
            Class impl = toteutus.getImpl();
            if (toteutukset.isEmpty()) {
                defaults.put(impl, toteutus);
            }
            else {
                if (!toteutuksetMap.containsKey(impl)) {
                    toteutuksetMap.put(impl, new HashMap<>());
                }
                HashMap<KoulutustyyppiToteutus, PerusteToteutus> map = toteutuksetMap.get(impl);
                toteutukset.forEach(t -> {
                    map.put(t, toteutus);
                });
            }
        }
    }

    @PreAuthorize("permitAll()")
    public <T extends PerusteToteutus> T get(Long perusteId, Class<T> clazz) {
        Peruste p = perusteRepository.findOne(perusteId);
        if (p == null) {
            throw new BusinessRuleViolationException("Perustetta ei ole");
        }
        return get(p, clazz);
    }

    @PreAuthorize("permitAll()")
    public <I extends PerusteIdentifiable & Identifiable, T extends PerusteToteutus> T get(I peruste, Class<T> clazz) {
        return get(peruste.getToteutus(), clazz);
    }

    @PreAuthorize("permitAll()")
    public <T extends PerusteToteutus> T get(Class<T> clazz) {
        return get((KoulutustyyppiToteutus)null, clazz);
    }

    @PreAuthorize("permitAll()")
    public <T extends PerusteToteutus> T get(KoulutustyyppiToteutus toteutus, Class<T> clazz) {
        if (toteutus != null) {
            HashMap<KoulutustyyppiToteutus, PerusteToteutus> toteutukset = this.toteutuksetMap.getOrDefault(clazz, null);
            if (toteutukset != null && toteutukset.containsKey(toteutus)) {
                PerusteToteutus impl = toteutukset.getOrDefault(toteutus, null);
                if (impl != null) {
                    return (T) impl;
                }
            }
        }
        PerusteToteutus impl = defaults.getOrDefault(clazz, null);
        if (impl != null) {
            return (T) impl;
        }
        throw new BusinessRuleViolationException("Toteutusta ei löytynyt: "
                + clazz.getSimpleName()
                + " " + (toteutus != null ? toteutus.toString() : ""));
    }

}
