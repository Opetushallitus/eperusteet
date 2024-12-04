package fi.vm.sade.eperusteet.resource.peruste;

import com.google.common.base.Supplier;
import fi.vm.sade.eperusteet.dto.yl.lukio.julkinen.LukioOppiainePuuDto;
import fi.vm.sade.eperusteet.resource.util.CacheableResponse;
import fi.vm.sade.eperusteet.service.PerusteService;
import fi.vm.sade.eperusteet.service.yl.LukiokoulutuksenPerusteenSisaltoService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/perusteet/{perusteId}/lukiokoulutus/julkinen")
@Tag(name = "Lukioperusteen julkiset tiedot")
public class LukiokoulutusJulkisetPerusteenSisallotController {
    @Autowired
    private LukiokoulutuksenPerusteenSisaltoService sisaltoService;

    @Autowired
    private PerusteService perusteet;

    @RequestMapping(value = "/oppiainerakenne", method = RequestMethod.GET)
    public ResponseEntity<LukioOppiainePuuDto> getOppiainePuuRakenne(@PathVariable("perusteId") Long perusteId) {
        return handleGet(perusteId, () -> sisaltoService.getOppiaineTreeStructure(perusteId));
    }

    private <T> ResponseEntity<T> handleGet(Long perusteId, Supplier<T> response) {
        return CacheableResponse.create(perusteet.getPerusteVersion(perusteId), 1, response);
    }

}
