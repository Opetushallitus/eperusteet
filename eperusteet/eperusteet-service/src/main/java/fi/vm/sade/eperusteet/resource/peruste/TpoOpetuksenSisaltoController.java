package fi.vm.sade.eperusteet.resource.peruste;

import fi.vm.sade.eperusteet.dto.yl.TaiteenalaDto;
import fi.vm.sade.eperusteet.resource.config.InternalApi;
import fi.vm.sade.eperusteet.service.audit.EperusteetAudit;
import fi.vm.sade.eperusteet.service.audit.LogMessage;
import fi.vm.sade.eperusteet.service.yl.AIPEOpetuksenPerusteenSisaltoService;
import fi.vm.sade.eperusteet.service.yl.TpoOpetuksenSisaltoService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static fi.vm.sade.eperusteet.service.audit.EperusteetMessageFields.TAITEENALA;
import static fi.vm.sade.eperusteet.service.audit.EperusteetOperation.LISAYS;
import static fi.vm.sade.eperusteet.service.audit.EperusteetOperation.MUOKKAUS;
import static fi.vm.sade.eperusteet.service.audit.EperusteetOperation.POISTO;
import static org.springframework.web.bind.annotation.RequestMethod.*;

@RestController
@RequestMapping("/perusteet/{perusteId}/tpoopetus")
@InternalApi
public class TpoOpetuksenSisaltoController {
    private static final Logger logger = LoggerFactory.getLogger(AIPEOpetuksenSisaltoController.class);

    @Autowired
    private EperusteetAudit audit;

    @Autowired
    TpoOpetuksenSisaltoService sisalto;

//    @RequestMapping(value = "/taiteenalat", method = GET)
//    public ResponseEntity<List<TaiteenalaDto>> gettaiteenalaet(
//            @PathVariable("perusteId") final Long perusteId
//    ) {
//        return ResponseEntity.ok(sisalto.getTaiteenalat(perusteId));
//    }

    @RequestMapping(value = "/taiteenalat", method = POST)
    @ResponseBody
    public ResponseEntity<TaiteenalaDto> addTaiteenala(
            @PathVariable("perusteId") final Long perusteId,
            @RequestBody TaiteenalaDto taiteenalaDto
    ) {
        return audit.withAudit(LogMessage.builder(perusteId, TAITEENALA, LISAYS),
                (Void) -> ResponseEntity.ok(sisalto.addTaiteenala(perusteId, taiteenalaDto)));
    }

    @RequestMapping(value = "/taiteenalat/{taiteenalaId}", method = GET)
    public ResponseEntity<TaiteenalaDto> getTaiteenala(
            @PathVariable("perusteId") final Long perusteId,
            @PathVariable("taiteenalaId") final Long taiteenalaId
    ) {
        return ResponseEntity.ok(sisalto.getTaiteenala(perusteId, taiteenalaId));
    }

    @RequestMapping(value = "/taiteenalat/{taiteenalaId}", method = DELETE)
    public ResponseEntity removeTaiteenala(
            @PathVariable("perusteId") final Long perusteId,
            @PathVariable("taiteenalaId") final Long taiteenalaId
    ) {
        audit.withAudit(LogMessage.builder(perusteId, TAITEENALA, POISTO), (Void) -> {
            sisalto.removeTaiteenala(perusteId, taiteenalaId);
            return null;
        });
        return ResponseEntity.ok().build();
    }

    @RequestMapping(value = "/taiteenalat/{taiteenalaId}", method = PUT)
    @ResponseBody
    public ResponseEntity<TaiteenalaDto> updateTaiteenala(
            @PathVariable("perusteId") final Long perusteId,
            @PathVariable("taiteenalaId") final Long taiteenalaId,
            @RequestBody TaiteenalaDto taiteenalaDto
    ) {
        return audit.withAudit(LogMessage.builder(perusteId, TAITEENALA, MUOKKAUS),
                (Void) -> ResponseEntity.ok(sisalto.updateTaiteenala(perusteId, taiteenalaId, taiteenalaDto)));
    }
}
