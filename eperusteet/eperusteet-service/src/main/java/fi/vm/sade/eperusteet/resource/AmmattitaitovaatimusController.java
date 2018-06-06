package fi.vm.sade.eperusteet.resource;

import fi.vm.sade.eperusteet.dto.peruste.PerusteenOsaViiteDto;
import fi.vm.sade.eperusteet.dto.tutkinnonosa.AmmattitaitovaatimusQueryDto;
import fi.vm.sade.eperusteet.dto.tutkinnonosa.TutkinnonOsa2018Dto;
import fi.vm.sade.eperusteet.resource.config.InternalApi;
import fi.vm.sade.eperusteet.service.PerusteService;
import fi.vm.sade.eperusteet.service.PerusteenOsaViiteService;
import fi.vm.sade.eperusteet.service.audit.EperusteetAudit;
import io.swagger.annotations.Api;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;

import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

@Controller
@Api(value = "Ammattitaitovaatimukset", description = "Rajapinnat ammattitaitovaatimusten käsittelyyn")
@InternalApi
public class AmmattitaitovaatimusController {
    @Autowired
    private EperusteetAudit audit;

    @Autowired
    private PerusteService service;

    @Autowired
    private PerusteenOsaViiteService viiteService;

    @RequestMapping(value = "/ammattitaitovaatimukset", method = GET)
    public PerusteenOsaViiteDto.Matala ammattitaitovaatimukset(final AmmattitaitovaatimusQueryDto query) {
        throw new UnsupportedOperationException();
    }

    @RequestMapping(value = "/ammattitaitovaatimukset/{koodi}", method = GET)
    public PerusteenOsaViiteDto.Matala ammattitaitovaatimus(
            @PathVariable("perusteId") final String koodi) {
        throw new UnsupportedOperationException();
    }

}
