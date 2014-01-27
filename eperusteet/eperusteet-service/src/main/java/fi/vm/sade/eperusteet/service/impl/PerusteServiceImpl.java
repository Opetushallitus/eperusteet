package fi.vm.sade.eperusteet.service.impl;

import fi.vm.sade.eperusteet.domain.Kieli;
import fi.vm.sade.eperusteet.domain.Peruste;
import fi.vm.sade.eperusteet.domain.PerusteenOsaViite;
import fi.vm.sade.eperusteet.repository.PerusteRepository;
import fi.vm.sade.eperusteet.repository.PerusteenOsaViiteRepository;
import fi.vm.sade.eperusteet.service.PerusteService;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 *
 * @author jhyoty
 */
@Service
public class PerusteServiceImpl implements PerusteService {

    private static final Logger LOG = LoggerFactory.getLogger(PerusteServiceImpl.class);

    @Autowired
    PerusteRepository perusteet;

    @Autowired
    PerusteenOsaViiteRepository viitteet;

    @Override
    public Page<Peruste> getAll(PageRequest page, String kieli) {
        return findBy(page, null, null, null, kieli, null, false);
    }

    @Override
    public Page<Peruste> findBy(PageRequest page, String nimi, List<String> koulutusala, List<String> tyyppi, String kieli, List<String> opintoala, boolean siirtyma) {
        return perusteet.findBy(Kieli.of(kieli), nimi, koulutusala, tyyppi, page, opintoala, siirtyma);
    }

    @Override
    @Transactional
    public Peruste get(final Long id) {
        Peruste p = perusteet.findById(id);
//        if (p != null && p.getRakenne() != null) {
//            p.getRakenne().getPerusteenOsa();
//        }
        return p;
    }

    @Override
    @Transactional
    public PerusteenOsaViite addViite(final Long parentId, final Long seuraavaViite, PerusteenOsaViite viite) {
        LOG.info("ennen = " + seuraavaViite);
        PerusteenOsaViite v = viitteet.findOne(parentId);
        viite.setVanhempi(v);
        int i = 0;
        if (seuraavaViite != null) {
            for (PerusteenOsaViite o : v.getLapset()) {
                if (o.getId().equals(seuraavaViite)) {
                    break;
                }
                i++;
            }
        } else {
            v.getLapset().size();
        }
        v.getLapset().add(i, viite);
        return viitteet.save(viite);
    }

}
