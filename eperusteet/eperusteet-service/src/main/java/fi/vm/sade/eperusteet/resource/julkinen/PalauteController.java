package fi.vm.sade.eperusteet.resource.julkinen;

import com.fasterxml.jackson.core.JsonProcessingException;
import fi.vm.sade.eperusteet.dto.PalauteDto;
import fi.vm.sade.eperusteet.service.PalauteService;
import io.swagger.annotations.Api;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

@RestController
@RequestMapping("/api/palaute")
@Api(value = "Palautteet")
public class PalauteController {

    @Autowired
    private PalauteService palauteService;

    @RequestMapping(method = POST)
    @ResponseStatus(HttpStatus.CREATED)
    @ResponseBody
    public PalauteDto sendPalaute(@RequestBody PalauteDto palauteDto) throws JsonProcessingException {
        return palauteService.lahetaPalaute(palauteDto);
    }

    @RequestMapping(method = POST, value = "/update")
    @ResponseBody
    public PalauteDto updatePalaute(@RequestBody PalauteDto palauteDto) {
        return palauteService.paivitaPalaute(palauteDto);
    }

    @RequestMapping(method = GET)
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public Object palautteet(@RequestParam(value = "palautekanava", defaultValue = "eperusteet-opintopolku") String palautekanava) throws JsonProcessingException {
        return palauteService.getPalautteet(palautekanava);
    }

    @RequestMapping(method = GET, value = "/status")
    @ResponseBody
    public List<PalauteDto> palauteStatukset(@RequestParam(value = "palautekanava") String palautekanava) throws JsonProcessingException {
        return palauteService.getPalauteStatus(palautekanava);
    }
}
