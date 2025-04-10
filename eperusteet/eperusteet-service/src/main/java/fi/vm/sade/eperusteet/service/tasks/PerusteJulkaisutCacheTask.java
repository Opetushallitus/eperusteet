package fi.vm.sade.eperusteet.service.tasks;

import fi.vm.sade.eperusteet.dto.util.CacheArvot;
import fi.vm.sade.eperusteet.repository.JulkaistuPerusteDataStoreRepository;
import fi.vm.sade.eperusteet.service.MaintenanceService;
import fi.vm.sade.eperusteet.service.PerusteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
public class PerusteJulkaisutCacheTask extends AbstractScheduledTask {

    @Lazy
    @Autowired
    private MaintenanceService maintenanceService;

    @Autowired
    private JulkaistuPerusteDataStoreRepository julkaistuPerusteDataStoreRepository;

    @Autowired
    private PerusteService perusteService;

    @Override
    public int getPriority() {
        return 100;
    }

    @Override
    public void executeTask(Date viimeisinajoaika) {
        maintenanceService.clearCache(CacheArvot.PERUSTE_JULKAISU);
        maintenanceService.cacheJulkaistutPerusteet();
    }
}
