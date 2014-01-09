package fi.vm.sade.eperusteet.resource;

import fi.vm.sade.eperusteet.domain.Peruste;
import fi.vm.sade.eperusteet.domain.PerusteenOsaViite;
import fi.vm.sade.eperusteet.service.PerusteService;
import java.util.List;
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
    public ResponseEntity<Page<Peruste>> getAll(
            @RequestParam(value = "sivu", required = false, defaultValue = "0") int sivu,
            @RequestParam(value = "sivukoko", required = false, defaultValue = "25") int sivukoko,
            @RequestParam(value = "nimi", required = false) String nimi,
            @RequestParam(value = "ala", required = false) List<String> ala,
            @RequestParam(value = "tyyppi", required = false) List<String> tyyppi,
            @RequestParam(value = "kieli", required = false, defaultValue = "fi") String kieli,
            @RequestParam(value = "opintoala", required = false) List<String> opintoala
    ) {
        PageRequest p = new PageRequest(sivu, Math.min(sivukoko, 100));
        Page<Peruste> r;

        r = service.findBy(p, nimi, ala, tyyppi, kieli, opintoala);

        return new ResponseEntity<>(r,
                                    ResponseHeaders.cacheHeaders(7, TimeUnit.MINUTES),
                                    r.hasContent() ? HttpStatus.OK : HttpStatus.NOT_FOUND);
    }

    @RequestMapping(value = "/{id}", method = GET)
    @ResponseBody
    public ResponseEntity<Peruste> get(@PathVariable("id") final long id) {
        Peruste t = service.get(id);
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
