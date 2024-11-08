package fi.vm.sade.eperusteet.service.tasks;

import fi.vm.sade.eperusteet.service.AmosaaClient;
import fi.vm.sade.eperusteet.service.MaintenanceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
public class AmosaaTilastotTask extends AbstractScheduledTask {

    @Autowired
    private AmosaaClient amosaaClient;

    @Lazy
    @Autowired
    private MaintenanceService maintenanceService;

    @Override
    public int getPriority() {
        return 70;
    }

    @Override
    public void executeTask(Date viimeisinajoaika) {
        maintenanceService.clearCache("amosaatilastot");
        amosaaClient.getTilastot();
    }
}
