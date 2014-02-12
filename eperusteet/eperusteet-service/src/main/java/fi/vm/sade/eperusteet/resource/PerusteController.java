package fi.vm.sade.eperusteet.resource;

import fi.vm.sade.eperusteet.domain.PerusteenOsaViite;
import fi.vm.sade.eperusteet.dto.PerusteDto;
import fi.vm.sade.eperusteet.service.PerusteService;
import fi.vm.sade.eperusteet.dto.PerusteQuery;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import static org.springframework.web.bind.annotation.RequestMethod.*;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping("/api/perusteet")
public class PerusteController {

    private static final Logger LOG = LoggerFactory.getLogger(PerusteController.class);

    @Autowired
    private PerusteService service;

    @RequestMapping(method = GET)
    @ResponseBody
    public ResponseEntity<Page<PerusteDto>> getAll(PerusteQuery pquery) {
        PageRequest p = new PageRequest(pquery.getSivu(), Math.min(pquery.getSivukoko(), 100));
        return new ResponseEntity<>(service.findBy(p, pquery), ResponseHeaders.cacheHeaders(1, TimeUnit.MINUTES), HttpStatus.OK);
    }

    @RequestMapping(value = "/{id}", method = GET)
    @ResponseBody
    public ResponseEntity<PerusteDto> get(@PathVariable("id") final long id) {
        PerusteDto t = service.get(id);
        if (t == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(t, ResponseHeaders.cacheHeaders(1, TimeUnit.SECONDS), HttpStatus.OK);
    }

    @RequestMapping(value = "/{perusteId}/osat/{id}/lapset", method = POST)
    @ResponseBody
    public ResponseEntity<PerusteenOsaViite> lisääViite(
        @PathVariable("perusteId") final Long perusteId,
        @PathVariable("id") final Long viiteId,
        @RequestParam(value = "ennen", required = false) Long ennen, @RequestBody PerusteenOsaViite viite) {

        return new ResponseEntity<>(service.addViite(viiteId, ennen, viite), HttpStatus.CREATED);
    }

}
