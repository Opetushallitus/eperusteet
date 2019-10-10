package fi.vm.sade.eperusteet.service.impl;

import fi.vm.sade.eperusteet.domain.SkeduloituAjo;
import fi.vm.sade.eperusteet.domain.SkeduloituAjoStatus;
import fi.vm.sade.eperusteet.service.ScheduledTask;
import fi.vm.sade.eperusteet.service.SkeduloituajoService;
import fi.vm.sade.eperusteet.service.event.aop.IgnorePerusteUpdateCheck;
import fi.vm.sade.eperusteet.service.exception.SkeduloituAjoAlreadyRunningException;
import java.util.Date;
import org.springframework.beans.factory.annotation.Autowired;

public abstract class AbstractScheduledTask implements ScheduledTask {

    @Autowired
    private SkeduloituajoService skeduloituajoService;

    public abstract void executeTask(Date viimeisinajoaika);

    @Override
    public String getName() {
        return this.getClass().getSimpleName();
    }

    @IgnorePerusteUpdateCheck
    @Override
    public void execute() {
        SkeduloituAjo skeduloituajo = skeduloituajoService.haeTaiLisaaAjo(getName());

        if (skeduloituajo.getStatus().equals(SkeduloituAjoStatus.AJOSSA)) {
            throw new SkeduloituAjoAlreadyRunningException("ajo-kaynnissa");
        }

        try {
            skeduloituajoService.kaynnistaAjo(skeduloituajo);
            executeTask(skeduloituajo.getViimeisinAjoLopetus());
            skeduloituajoService.pysaytaAjo(skeduloituajo);
        } catch (Exception e) {
            skeduloituajoService.paivitaAjoStatus(skeduloituajo, SkeduloituAjoStatus.AJOVIRHE);
            throw e;
        }
    }
}
