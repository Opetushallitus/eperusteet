package fi.vm.sade.eperusteet.service.impl;

import fi.vm.sade.eperusteet.domain.SkeduloituAjo;
import fi.vm.sade.eperusteet.repository.SkeduloituajoRepository;
import fi.vm.sade.eperusteet.service.ScheduledTask;
import fi.vm.sade.eperusteet.service.SkeduloituajoService;
import java.util.Date;
import org.springframework.beans.factory.annotation.Autowired;

public abstract class AbstractScheduledTask implements ScheduledTask {

    @Autowired
    private SkeduloituajoRepository skeduloituajoRepository;

    @Autowired
    private SkeduloituajoService skeduloituajoService;

    public abstract void executeTask(Date viimeisinajoaika);

    @Override
    public String getName() {
        return this.getClass().getName();
    }

    @Override
    public void execute() {
        SkeduloituAjo skeduloituajo = skeduloituajoRepository.findByNimi(getName());
        if (skeduloituajo == null) {
            skeduloituajo = skeduloituajoService.lisaaUusiAjo(getName());
        }

        executeTask(skeduloituajo.getViimeisinajo());

        skeduloituajo.setViimeisinajo(new Date());
        skeduloituajoRepository.save(skeduloituajo);
    }
}
