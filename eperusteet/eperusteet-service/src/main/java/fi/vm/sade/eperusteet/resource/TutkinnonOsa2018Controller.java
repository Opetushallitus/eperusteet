package fi.vm.sade.eperusteet.resource;

import com.fasterxml.jackson.databind.JsonNode;
import fi.vm.sade.eperusteet.domain.Suoritustapakoodi;
import fi.vm.sade.eperusteet.domain.tutkinnonrakenne.TutkinnonOsaViite;
import fi.vm.sade.eperusteet.dto.peruste.PerusteenOsaViiteDto;
import fi.vm.sade.eperusteet.dto.tutkinnonosa.AmmattitaitovaatimusQueryDto;
import fi.vm.sade.eperusteet.dto.tutkinnonosa.TutkinnonOsa2018Dto;
import fi.vm.sade.eperusteet.dto.tutkinnonrakenne.TutkinnonOsaViiteDto;
import fi.vm.sade.eperusteet.resource.config.InternalApi;
import fi.vm.sade.eperusteet.service.PerusteService;
import fi.vm.sade.eperusteet.service.PerusteenOsaViiteService;
import fi.vm.sade.eperusteet.service.audit.EperusteetAudit;
import fi.vm.sade.eperusteet.service.audit.LogMessage;
import io.swagger.annotations.Api;
import org.hibernate.cfg.NotYetImplementedException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.ldap.repository.Query;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.List;

import static fi.vm.sade.eperusteet.service.audit.EperusteetMessageFields.PERUSTEENOSAVIITE;
import static fi.vm.sade.eperusteet.service.audit.EperusteetMessageFields.TUTKINNONOSAVIITE;
import static fi.vm.sade.eperusteet.service.audit.EperusteetOperation.LISAYS;
import static org.springframework.web.bind.annotation.RequestMethod.DELETE;
import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

@Controller
@Api(value = "Tutkinnonosat2018", description = "Uusi tutkinnon osien hallinta paremmalla ammattitaitovaatimustuella")
@RequestMapping(value = "/perusteet/{perusteId}/suoritustavat/{suoritustapa}/tutkinnonosat")
@InternalApi
public class TutkinnonOsa2018Controller {
    @Autowired
    private EperusteetAudit audit;

    @Autowired
    private PerusteService service;

    @Autowired
    private PerusteenOsaViiteService viiteService;

    @RequestMapping(method = GET, headers = { "versio=2" })
    public List<PerusteenOsaViiteDto.Matala> tutkinnonOsat(
            @PathVariable("perusteId") final Long perusteId,
            @PathVariable("suoritustapa") final String suoritustapa) {
        throw new UnsupportedOperationException();
    }

    @RequestMapping(value = "/{tosaId}", method = GET, headers = { "versio=2" })
    public PerusteenOsaViiteDto.Matala tutkinnonOsa(
            @PathVariable("perusteId") final Long perusteId,
            @PathVariable("suoritustapa") final String suoritustapa,
            @PathVariable("perusteId") final Long tosaId) {
        throw new UnsupportedOperationException();
    }

    @RequestMapping(method = POST, headers = { "versio=2" })
    @ResponseStatus(HttpStatus.CREATED)
    public PerusteenOsaViiteDto.Matala addTutkinnonOsa(
            @PathVariable("perusteId") final Long perusteId,
            @PathVariable("suoritustapa") final String suoritustapa,
            @RequestBody(required = false) final TutkinnonOsaViiteDto<TutkinnonOsa2018Dto> osa) {
//        return audit.withAudit(LogMessage.builder(perusteId, TUTKINNONOSAVIITE, LISAYS), (Void) -> {
//            if (osa.getTutkinnonOsa() != null) {
//                return service.attachTutkinnonOsa(perusteId, suoritustapa, osa);
//            }
//            return service.addTutkinnonOsa(perusteId, suoritustapa, osa);
//        });

        return null;
    }

    @RequestMapping(value = "/{tosaId}", method = DELETE, headers = { "versio=2" })
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void tutkinnonOsaDelete(
            @PathVariable("perusteId") final Long perusteId,
            @PathVariable("suoritustapa") final String suoritustapa,
            @PathVariable("perusteId") final Long tosaId) {
        throw new UnsupportedOperationException();
    }

}
