package fi.vm.sade.eperusteet.resource;

import fi.vm.sade.eperusteet.dto.OpintoalaDto;
import fi.vm.sade.eperusteet.resource.config.InternalApi;
import fi.vm.sade.eperusteet.service.OpintoalaService;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.web.bind.annotation.RequestMethod.GET;

@RestController
@RequestMapping("/opintoalat")
@InternalApi
public class OpintoalaController {
    @Autowired
    private OpintoalaService service;

    @RequestMapping(method = GET)
    @ResponseBody
    public List<OpintoalaDto> getAllOpintoalat() {
        List<OpintoalaDto> olist = service.getAll();
        return olist;
    }
}
