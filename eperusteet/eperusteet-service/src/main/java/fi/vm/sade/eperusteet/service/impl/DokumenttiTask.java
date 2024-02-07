package fi.vm.sade.eperusteet.service.impl;

import fi.vm.sade.eperusteet.service.ScheduledTask;
import fi.vm.sade.eperusteet.service.dokumentti.DokumenttiService;
import org.springframework.beans.factory.annotation.Autowired;

@Deprecated
public class DokumenttiTask implements ScheduledTask {

    @Autowired
    DokumenttiService dokumenttiService;

    @Override
    public int getPriority() {
        return 1000;
    }

    @Override
    public String getName() {
        return "dokumentti";
    }

    @Override
    public void execute() {
        //dokumenttiService.paivitaDokumentit();
    }
}
