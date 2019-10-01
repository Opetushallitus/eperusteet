package fi.vm.sade.eperusteet.service.impl;

import fi.vm.sade.eperusteet.domain.SkeduloituAjo;
import fi.vm.sade.eperusteet.repository.SkeduloituajoRepository;
import fi.vm.sade.eperusteet.service.SkeduloituajoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Transactional
@Service
public class SkeduloituajoServiceImpl implements SkeduloituajoService {

    @Autowired
    private SkeduloituajoRepository skeduloituajoRepository;

    @Override
    public SkeduloituAjo lisaaUusiAjo(String nimi) {
        return skeduloituajoRepository.save(SkeduloituAjo.builder().nimi(nimi).build());
    }
}
