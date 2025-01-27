package fi.vm.sade.eperusteet.resource;

import fi.vm.sade.eperusteet.dto.GeneerinenArviointiasteikkoDto;
import fi.vm.sade.eperusteet.dto.GeneerinenArviointiasteikkoKaikkiDto;
import fi.vm.sade.eperusteet.service.GeneerinenArviointiasteikkoService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static org.springframework.web.bind.annotation.RequestMethod.DELETE;
import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;
import static org.springframework.web.bind.annotation.RequestMethod.PUT;

@RestController
@RequestMapping("/api/geneerinenarviointi")
@Tag(name = "GeneerinenArviointiasteikko")
public class GeneerinenArviointiasteikkoController {

    @Autowired
    private GeneerinenArviointiasteikkoService geneerinenArviointiasteikkoService;

    @RequestMapping(method = GET)
    public List<GeneerinenArviointiasteikkoDto> getAllGeneerisetArviointiasteikot() {
        return geneerinenArviointiasteikkoService.getAll();
    }

    @RequestMapping(value = "/julkaistu", method = GET)
    public List<GeneerinenArviointiasteikkoDto> getAllGeneerisetArviointiasteikotJulkaistu() {
        return geneerinenArviointiasteikkoService.getJulkaistut();
    }

    @RequestMapping(value = "/{id}", method = GET)
    public GeneerinenArviointiasteikkoDto getOneGeneerisetArviointiasteikko(@PathVariable Long id) {
        return geneerinenArviointiasteikkoService.getOne(id);
    }

    @RequestMapping(value = "/{id}/kaikki", method = GET)
    public GeneerinenArviointiasteikkoKaikkiDto getOneGeneerisetArviointiasteikkoKaikki(@PathVariable Long id) {
        return geneerinenArviointiasteikkoService.getOne(id, GeneerinenArviointiasteikkoKaikkiDto.class);
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
