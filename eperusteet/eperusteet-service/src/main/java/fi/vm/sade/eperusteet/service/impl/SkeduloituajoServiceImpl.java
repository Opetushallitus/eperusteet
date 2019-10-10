package fi.vm.sade.eperusteet.service.impl;

import fi.vm.sade.eperusteet.domain.SkeduloituAjo;
import fi.vm.sade.eperusteet.domain.SkeduloituAjoStatus;
import fi.vm.sade.eperusteet.repository.SkeduloituajoRepository;
import fi.vm.sade.eperusteet.service.SkeduloituajoService;
import fi.vm.sade.eperusteet.service.event.aop.IgnorePerusteUpdateCheck;
import java.util.Date;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Transactional
@Service
public class SkeduloituajoServiceImpl implements SkeduloituajoService {

    @Autowired
    private SkeduloituajoRepository skeduloituajoRepository;

    @IgnorePerusteUpdateCheck
    @Override
    public SkeduloituAjo haeTaiLisaaAjo(String nimi) {
        SkeduloituAjo skeduloituajo = skeduloituajoRepository.findByNimi(nimi);
        if (skeduloituajo == null) {
            skeduloituajo = lisaaUusiAjo(nimi);
        }

        return skeduloituajo;
    }

    @IgnorePerusteUpdateCheck
    @Override
    public SkeduloituAjo lisaaUusiAjo(String nimi) {
        return skeduloituajoRepository.save(SkeduloituAjo.builder()
                .nimi(nimi)
                .status(SkeduloituAjoStatus.PYSAYTETTY)
                .build());
    }

    @IgnorePerusteUpdateCheck
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @Override
    public SkeduloituAjo paivitaAjoStatus(SkeduloituAjo skeduloituAjo, SkeduloituAjoStatus status) {
        skeduloituAjo.setStatus(status);
        return skeduloituajoRepository.save(skeduloituAjo);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @IgnorePerusteUpdateCheck
    @Override
    public SkeduloituAjo pysaytaAjo(SkeduloituAjo skeduloituAjo) {
        skeduloituAjo.setStatus(SkeduloituAjoStatus.PYSAYTETTY);
        skeduloituAjo.setViimeisinAjoLopetus(new Date());
        return skeduloituajoRepository.save(skeduloituAjo);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @IgnorePerusteUpdateCheck
    @Override
    public SkeduloituAjo kaynnistaAjo(SkeduloituAjo skeduloituAjo) {
        skeduloituAjo.setStatus(SkeduloituAjoStatus.AJOSSA);
        skeduloituAjo.setViimeisinAjoKaynnistys(new Date());
        return skeduloituajoRepository.save(skeduloituAjo);
    }

}
