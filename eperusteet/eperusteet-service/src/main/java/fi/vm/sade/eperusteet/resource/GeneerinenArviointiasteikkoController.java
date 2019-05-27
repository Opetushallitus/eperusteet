package fi.vm.sade.eperusteet.resource;

import fi.vm.sade.eperusteet.dto.GeneerinenArviointiasteikkoDto;
import fi.vm.sade.eperusteet.service.GeneerinenArviointiasteikkoService;
import io.swagger.annotations.Api;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static org.springframework.web.bind.annotation.RequestMethod.*;

@RestController
@RequestMapping("/geneerinenarviointi")
@Api(value = "GeneerinenArviointiasteikko")
public class GeneerinenArviointiasteikkoController {

    @Autowired
    private GeneerinenArviointiasteikkoService geneerinenArviointiasteikkoService;

    @RequestMapping(method = GET)
    public List<GeneerinenArviointiasteikkoDto> getAllGeneerisetArviointiasteikot() {
        return geneerinenArviointiasteikkoService.getAll();
    }

    @RequestMapping(value = "/{id}", method = GET)
    public GeneerinenArviointiasteikkoDto getOneGeneerisetArviointiasteikko(@PathVariable Long id) {
        return geneerinenArviointiasteikkoService.getOne(id);
    }

    @RequestMapping(method = POST)
    public GeneerinenArviointiasteikkoDto addGeneerinenArviointiasteikko(
            @RequestBody GeneerinenArviointiasteikkoDto asteikko) {
        return geneerinenArviointiasteikkoService.add(asteikko);
    }

    @RequestMapping(value = "/{id}/kopioi", method = POST)
    public GeneerinenArviointiasteikkoDto kopioiGeneerinenArviontiasteikko(
            @PathVariable Long id) {
        return geneerinenArviointiasteikkoService.kopioi(id);
    }

    @RequestMapping(value = "/{id}", method = PUT)
    public GeneerinenArviointiasteikkoDto updateGeneerinenArviontiasteikko(@PathVariable Long id,
                                                                           @RequestBody GeneerinenArviointiasteikkoDto asteikko) {
        return geneerinenArviointiasteikkoService.update(id, asteikko);
    }

    @RequestMapping(value = "/{id}", method = DELETE)
    public void removeGeneerinenArviontiasteikko(@PathVariable Long id) {
        geneerinenArviointiasteikkoService.remove(id);
    }
}
