package fi.vm.sade.eperusteet.resource;

import fi.vm.sade.eperusteet.dto.GeneerinenArviointiasteikkoDto;
import fi.vm.sade.eperusteet.service.GeneerinenArviointiasteikkoService;
import io.swagger.annotations.Api;
import org.springframework.beans.factory.annotation.Autowired;
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
    public GeneerinenArviointiasteikkoDto getOneGeneerisetArviointiasteikko(Long id) {
        return geneerinenArviointiasteikkoService.getOne(id);
    }

    @RequestMapping(method = POST)
    public GeneerinenArviointiasteikkoDto addGeneerinenArviointiasteikko(GeneerinenArviointiasteikkoDto asteikko) {
        return geneerinenArviointiasteikkoService.add(asteikko);
    }

    @RequestMapping(value = "/{id}", method = PUT)
    public GeneerinenArviointiasteikkoDto updateGeneerinenArviontiasteikko(Long id, GeneerinenArviointiasteikkoDto asteikko) {
        return geneerinenArviointiasteikkoService.update(id);
    }

    @RequestMapping(value = "/{id}", method = DELETE)
    public void removeGeneerinenArviontiasteikko(Long id) {
        geneerinenArviointiasteikkoService.remove(id);
    }
}
