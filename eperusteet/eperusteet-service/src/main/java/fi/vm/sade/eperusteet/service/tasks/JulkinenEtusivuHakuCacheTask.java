package fi.vm.sade.eperusteet.service.tasks;

import fi.vm.sade.eperusteet.dto.util.CacheArvot;
import fi.vm.sade.eperusteet.service.JulkinenService;
import fi.vm.sade.eperusteet.service.MaintenanceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
public class JulkinenEtusivuHakuCacheTask extends AbstractScheduledTask {

    @Autowired
    private JulkinenService julkinenService;

    @Lazy
    @Autowired
    private MaintenanceService maintenanceService;

    @Override
    public int getPriority() {
        return 100;
    }

    @Override
    public void executeTask(Date viimeisinajoaika) {
        maintenanceService.clearCache(CacheArvot.JULKINEN_ETUSIVU);
        julkinenService.getJulkisivuDatat();
    }
}
