package fi.vm.sade.eperusteet.resource;

import fi.vm.sade.eperusteet.resource.config.InternalApi;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.web.bind.annotation.RequestMethod.GET;

@RestController
@RequestMapping("/ping")
@InternalApi
public class PingController {
    @RequestMapping(method = GET)
    @ResponseBody
    public ResponseEntity<?> isAlive() {
        return new ResponseEntity<>(HttpStatus.OK);
    }
}
