package fi.vm.sade.eperusteet.service.tasks;

import fi.vm.sade.eperusteet.service.MaintenanceService;
import fi.vm.sade.eperusteet.service.YlopsClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
public class YlopsTilastotTask extends AbstractScheduledTask {

    @Autowired
    private YlopsClient ylopsClient;

    @Autowired
    private MaintenanceService maintenanceService;

    @Override
    public int getPriority() {
        return 550;
    }

    @Override
    public void executeTask(Date viimeisinajoaika) {
        maintenanceService.clearCache("ylopstilastot");
        ylopsClient.getTilastot();
    }
}
