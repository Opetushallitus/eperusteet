package fi.vm.sade.eperusteet.resource;

import com.fasterxml.jackson.databind.JsonNode;
import fi.vm.sade.eperusteet.config.InternalApi;
import fi.vm.sade.eperusteet.service.AmosaaClient;
import fi.vm.sade.eperusteet.service.YlopsClient;
import io.swagger.annotations.Api;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.web.bind.annotation.RequestMethod.GET;

@RestController
@RequestMapping("/api/tilastot")
@Api(value = "Tilastot")
@InternalApi
public class TilastotController {

    @Autowired
    private AmosaaClient amosaaClient;

    @Autowired
    private YlopsClient ylopsClient;

    @RequestMapping(value = "/amosaa", method = GET)
    public List<Object> getAmosaaTilastot() {
        return amosaaClient.getTilastot();
    }

    @RequestMapping(value = "/ylops", method = GET)
    public JsonNode getYlopsTilastot() {
        return ylopsClient.getTilastot();
    }
}
