package fi.vm.sade.eperusteet.resource.hallinta;

import fi.vm.sade.eperusteet.service.ScheduledTask;
import fi.vm.sade.eperusteet.service.impl.TutkinnonosienAmmattiaitovaatimusKooditTask;
import fi.vm.sade.eperusteet.utils.client.OphClientHelper;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import springfox.documentation.annotations.ApiIgnore;

@RestController
@RequestMapping("/eraajo")
@ApiIgnore
public class EraAjoController {

    @Autowired
    private Map<String, ScheduledTask> tasks;

    @Autowired
    OphClientHelper helper;

    @RequestMapping(value = "/{nimi}/execute", method = RequestMethod.GET)
    public void executeTask(@PathVariable String nimi) {
        tasks.get(nimi).execute();
    }

}
