package fi.vm.sade.eperusteet.service.impl;

import fi.vm.sade.eperusteet.service.PerusteprojektiService;
import java.util.Date;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class MaarayskirjeTask extends AbstractScheduledTask {

    @Autowired
    private PerusteprojektiService perusteprojektiService;

    @Override
    public int getPriority() {
        return 1500;
    }

    @Override
    public void executeTask(Date viimeisinajoaika) {
        perusteprojektiService.lataaMaarayskirjeetTask();
    }

}
