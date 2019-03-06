package fi.vm.sade.eperusteet.service.impl;

import fi.vm.sade.eperusteet.service.PerusteprojektiService;
import fi.vm.sade.eperusteet.service.ScheduledTask;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class MaarayskirjeTask implements ScheduledTask {

    @Autowired
    private PerusteprojektiService perusteprojektiService;

    @Override
    public int getPriority() {
        return 1500;
    }

    @Override
    public String getName() {
        return "maarayskirje";
    }

    @Override
    public void execute() {
        perusteprojektiService.lataaMaarayskirjeetTask();
    }

}
