/*
 * Copyright (c) 2013 The Finnish Board of Education - Opetushallitus
 *
 * This program is free software: Licensed under the EUPL, Version 1.1 or - as
 * soon as they will be approved by the European Commission - subsequent versions
 * of the EUPL (the "Licence");
 *
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at: http://ec.europa.eu/idabc/eupl
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * European Union Public Licence for more details.
 */
package fi.vm.sade.eperusteet.resource;

import com.wordnik.swagger.annotations.Api;
import fi.vm.sade.eperusteet.dto.kayttaja.HenkiloTietoDto;
import fi.vm.sade.eperusteet.dto.tutkinnonrakenne.TutkinnonOsaViiteDto;
import fi.vm.sade.eperusteet.dto.util.CombinedDto;
import fi.vm.sade.eperusteet.repository.version.Revision;
import fi.vm.sade.eperusteet.service.KayttajanTietoService;
import fi.vm.sade.eperusteet.service.TutkinnonOsaViiteService;
import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 *
 * @author harrik
 */
@Controller
@RequestMapping("/tutkinnonosat")
@Api(value = "Tutkinnonosat", description = "Tutkinnon osien hallinta")
public class TutkinnonOsaController {

    @Autowired
    private TutkinnonOsaViiteService tutkinnonOsaViiteService;

    @Autowired
    private KayttajanTietoService kayttajanTietoService;

    @RequestMapping(value = "/viite/{id}/versiot", method = GET)
    @ResponseBody
    public List<CombinedDto<Revision, HenkiloTietoDto>> getViiteVersiot(@PathVariable("id") final Long id) {
        List<Revision> versiot = tutkinnonOsaViiteService.getVersiot(id);
        List<CombinedDto<Revision, HenkiloTietoDto>> laajennetut = new ArrayList<>();
        for (Revision r : versiot) {
            laajennetut.add(new CombinedDto<>(r, new HenkiloTietoDto(kayttajanTietoService.hae(r.getMuokkaajaOid()))));
        }
        return laajennetut;
    }

    @RequestMapping(value = "/viite/{id}/versio/{versioId}", method = GET)
    @ResponseBody
    public ResponseEntity<TutkinnonOsaViiteDto> getViiteVersio(@PathVariable("id") final Long id, @PathVariable("versioId") final Integer versioId) {
        TutkinnonOsaViiteDto t = tutkinnonOsaViiteService.getVersio(id, versioId);
        if (t == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(t, HttpStatus.OK);
    }

        @RequestMapping(value = "/palauta/viite/{id}/versio/{versioId}", method = POST)
    @ResponseBody
    public ResponseEntity<TutkinnonOsaViiteDto> revertToVersio(@PathVariable("id") final Long id, @PathVariable("versioId") final Integer versioId) {
        TutkinnonOsaViiteDto t = tutkinnonOsaViiteService.revertToVersio(id, versioId);
        return new ResponseEntity<>(t, HttpStatus.OK);
    }
}
