package fi.vm.sade.eperusteet.service.tasks;

import fi.vm.sade.eperusteet.service.export.LampiExportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
public class LampiExportTask extends AbstractScheduledTask {

    @Autowired
    private LampiExportService lampiService;

    @Override
    public int getPriority() {
        return 120;
    }

    @Override
    public void executeTask(Date viimeisinajoaika) {
        try {
            lampiService.export();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
