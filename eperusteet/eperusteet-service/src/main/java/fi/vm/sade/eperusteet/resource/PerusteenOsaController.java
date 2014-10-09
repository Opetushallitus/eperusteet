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
import fi.vm.sade.eperusteet.dto.LukkoDto;
import fi.vm.sade.eperusteet.dto.kayttaja.HenkiloTietoDto;
import fi.vm.sade.eperusteet.dto.peruste.PerusteenOsaDto;
import fi.vm.sade.eperusteet.dto.tutkinnonOsa.OsaAlueLaajaDto;
import fi.vm.sade.eperusteet.dto.tutkinnonOsa.OsaamistavoiteLaajaDto;
import fi.vm.sade.eperusteet.dto.util.CombinedDto;
import fi.vm.sade.eperusteet.dto.util.PerusteenOsaUpdateDto;
import fi.vm.sade.eperusteet.repository.version.Revision;
import fi.vm.sade.eperusteet.resource.util.PerusteenOsaMappings;
import fi.vm.sade.eperusteet.resource.util.WrappedList;
import fi.vm.sade.eperusteet.service.KayttajanTietoService;
import fi.vm.sade.eperusteet.service.PerusteenOsaService;
import java.util.ArrayList;
import java.util.List;
import javax.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.util.UriComponentsBuilder;

import static org.springframework.web.bind.annotation.RequestMethod.DELETE;
import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;
import static org.springframework.web.bind.annotation.RequestMethod.PUT;

@Controller
@RequestMapping("/perusteenosat")
@Api(value = "Perusteenosat", description = "Perusteen osien hallinta")
public class PerusteenOsaController {

    @Autowired
    private PerusteenOsaService service;

    @Autowired
    private KayttajanTietoService kayttajanTietoService;

    @RequestMapping(method = GET, params = "nimi")
    @ResponseBody
    public List<PerusteenOsaDto.Suppea> getAllWithName(@RequestParam("nimi") final String name) {
        return service.getAllWithName(name);
    }

    @RequestMapping(value = "/{id}", method = GET)
    @ResponseBody
    public ResponseEntity<PerusteenOsaDto.Laaja> get(@PathVariable("id") final Long id) {
        PerusteenOsaDto.Laaja t = service.get(id);
        if (t == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(t, HttpStatus.OK);
    }

    @RequestMapping(value = "/{id}/versiot", method = GET)
    @ResponseBody
    public List<CombinedDto<Revision, HenkiloTietoDto>> getVersiot(@PathVariable("id") final Long id) {
        List<Revision> versiot = service.getVersiot(id);
        List<CombinedDto<Revision, HenkiloTietoDto>> laajennetut = new ArrayList<>();
        for (Revision r : versiot) {
            laajennetut.add(new CombinedDto<>(r, new HenkiloTietoDto(kayttajanTietoService.hae(r.getMuokkaajaOid()))));
        }
        return laajennetut;
    }

    @RequestMapping(value = "/{id}/versio/{versioId}", method = GET)
    @ResponseBody
    public ResponseEntity<PerusteenOsaDto.Laaja> getVersio(@PathVariable("id") final Long id, @PathVariable("versioId") final Integer versioId) {
        PerusteenOsaDto.Laaja t = service.getVersio(id, versioId);
        if (t == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(t, HttpStatus.OK);
    }

    @RequestMapping(value = "/{id}/palauta/{versioId}", method = POST)
    @ResponseBody
    public ResponseEntity<PerusteenOsaDto.Laaja> revertToVersio(@PathVariable("id") final Long id, @PathVariable("versioId") final Integer versioId) {
        PerusteenOsaDto.Laaja t = service.revertToVersio(id, versioId);
        return new ResponseEntity<>(t, HttpStatus.OK);
    }

    @RequestMapping(method = GET, params = "koodiUri")
    @ResponseBody
    public List<PerusteenOsaDto.Laaja> get(@RequestParam("koodiUri") final String koodiUri) {
        List<PerusteenOsaDto.Laaja> t = service.getAllByKoodiUri(koodiUri);
        return PerusteenOsaDtoList.wrap(t);
    }

    @RequestMapping(method = POST)
    @ResponseStatus(HttpStatus.CREATED)
    @ResponseBody
    public ResponseEntity<PerusteenOsaDto.Laaja> add(@RequestBody PerusteenOsaDto.Laaja perusteenOsaDto, UriComponentsBuilder ucb) {
        PerusteenOsaDto.Laaja dto = service.add(perusteenOsaDto);
        return new ResponseEntity<>(dto, buildHeadersFor(dto.getId(), ucb), HttpStatus.CREATED);
    }

    @RequestMapping(value = "/{id}", method = POST)
    @ResponseBody
    public PerusteenOsaDto.Laaja update(@PathVariable("id") final Long id, @RequestBody PerusteenOsaUpdateDto dto) {
        return service.update(dto);
    }

    /**
     * Luo ja liittää uuden osa-alueen tutkinnon osaan.
     *
     * @param id
     * @param osaAlueDto
     * @return Uusi tutkinnon osan osa-alue
     */
    @RequestMapping(value = "/{id}/osaalue", method = POST)
    @ResponseBody
    @ResponseStatus(HttpStatus.CREATED)
    public OsaAlueLaajaDto addTutkinnonOsaOsaAlue(@PathVariable("id") final Long id, @RequestBody(required = false) OsaAlueLaajaDto osaAlueDto) {
        return service.addTutkinnonOsaOsaAlue(id, osaAlueDto);
    }

    /**
     * Päivittää tutkinnon osan osa-alueen tietoja.
     *
     * @param id
     * @param osaAlueId
     * @param osaAlue
     * @return Päivitetty tutkinnon osan osa-alue
     */
    @RequestMapping(value = "{id}/osaalue/{osaAlueId}", method = POST)
    @ResponseBody
    public ResponseEntity<OsaAlueLaajaDto> updateTutkinnonOsaOsaAlue(@PathVariable("id") final Long id, @PathVariable("osaAlueId") final Long osaAlueId, @RequestBody OsaAlueLaajaDto osaAlue) {
        return new ResponseEntity<>(service.updateTutkinnonOsaOsaAlue(id, osaAlueId, osaAlue), HttpStatus.OK);
    }

    @RequestMapping(value = "/{id}/osaalueet", method = GET)
    @ResponseBody
    public ResponseEntity<List<OsaAlueLaajaDto>> getTutkinnonOsaOsaAlueet(@PathVariable("id") final Long id) {
        return new ResponseEntity<>(service.getTutkinnonOsaOsaAlueet(id), HttpStatus.OK);
    }

    /**
     * Poistaa tutkinnon osan osa-alueen
     *
     * @param id
     * @param osaAlueId
     */
    @RequestMapping(value = "/{id}/osaalue/{osaAlueId}", method = DELETE)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void removeOsaAlue(
        @PathVariable("id") final Long id,
        @PathVariable("osaAlueId") final Long osaAlueId) {
        service.removeOsaAlue(id, osaAlueId);
    }

    /**
     * Luo ja liittää uuden osaamistavoitteen tutkinnon osa osa-alueeseen.
     *
     * @param id
     * @param osaAlueId
     * @param osaamistavoiteDto
     * @return Uusi osaamistavoiteDto
     */
    @RequestMapping(value = "/{id}/osaalue/{osaAlueId}/osaamistavoite", method = POST)
    @ResponseBody
    @ResponseStatus(HttpStatus.CREATED)
    public OsaamistavoiteLaajaDto addOsaamistavoite(
        @PathVariable("id") final Long id,
        @PathVariable("osaAlueId") final Long osaAlueId,
        @RequestBody(required = false) OsaamistavoiteLaajaDto osaamistavoiteDto) {
        return service.addOsaamistavoite(id, osaAlueId, osaamistavoiteDto);
    }

    /**
     * Päivittää osaamistavoitteen tutkinnon osa osa-alueeseen.
     *
     * @param id
     * @param osaAlueId
     * @param osaamistavoiteId
     * @param osaamistavoite requestBody
     * @return Päivitetty osaamistavoiteDto
     */
    @RequestMapping(value = "/{id}/osaalue/{osaAlueId}/osaamistavoite/{osaamistavoiteId}", method = POST)
    @ResponseBody
    public OsaamistavoiteLaajaDto updateOsaamistavoite(
        @PathVariable("id") final Long id,
        @PathVariable("osaAlueId") final Long osaAlueId,
        @PathVariable("osaamistavoiteId") final Long osaamistavoiteId,
        @RequestBody OsaamistavoiteLaajaDto osaamistavoite) {
        osaamistavoite.setId(osaamistavoiteId);
        return service.updateOsaamistavoite(id, osaAlueId, osaamistavoiteId, osaamistavoite);
    }

    /**
     * Listaa tutkinnon osa osa-alueen osaamistavoitteet
     *
     * @param id
     * @param osaAlueId
     * @return
     */
    @RequestMapping(value = "/{id}/osaalue/{osaAlueId}/osaamistavoitteet", method = GET)
    @ResponseBody
    public ResponseEntity<List<OsaamistavoiteLaajaDto>> getOsaamistavoitteet(
        @PathVariable("id") final Long id,
        @PathVariable("osaAlueId") final Long osaAlueId) {
        return new ResponseEntity<>(service.getOsaamistavoitteet(id, osaAlueId), HttpStatus.OK);
    }

    /**
     * Poistaa tutkinnon osan osa-alueen osaamistavoitteen
     *
     * @param id
     * @param osaAlueId
     * @param osaamistavoiteId
     */
    @RequestMapping(value = "/{id}/osaalue/{osaAlueId}/osaamistavoite/{osaamistavoiteId}", method = DELETE)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void removeOsaamistavoite(
        @PathVariable("id") final Long id,
        @PathVariable("osaAlueId") final Long osaAlueId,
        @PathVariable("osaamistavoiteId") final Long osaamistavoiteId) {
        service.removeOsaamistavoite(id, osaAlueId, osaamistavoiteId);
    }

    @RequestMapping(value = "/{id}/lukko", method = GET)
    @ResponseBody
    public ResponseEntity<LukkoDto> checkLock(@PathVariable("id") final Long id,
        @RequestHeader(value = "If-None-Match", required = false) Integer eTag,
        HttpServletResponse response) {
        LukkoDto lock = service.getLock(id);
        response.addHeader("ETag", String.valueOf(service.getLatestRevision(id)));
        return new ResponseEntity<>(lock, HttpStatus.OK);
    }

    @RequestMapping(value = "/{id}/lukko", method = {POST, PUT})
    @ResponseBody
    public ResponseEntity<LukkoDto> lock(@PathVariable("id") final Long id,
        @RequestHeader(value = "If-None-Match", required = false) Integer eTag,
        HttpServletResponse response) {
        LukkoDto lock = service.lock(id);
        response.addHeader("ETag", String.valueOf(service.getLatestRevision(id)));
        return new ResponseEntity<>(lock, HttpStatus.OK);
    }

    @RequestMapping(value = "/{id}/lukko", method = DELETE)
    @ResponseBody
    public void unlock(@PathVariable("id") final Long id) {
        service.unlock(id);
    }

    @RequestMapping(value = "/{id}", method = DELETE, consumes = "*/*")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @ResponseBody
    public void delete(@PathVariable final Long id) {
        service.delete(id);
    }

    @RequestMapping(value = "/tyypit", method = GET)
    @ResponseBody
    public List<String> getPerusteenOsaTypes() {
        return PerusteenOsaMappings.getPerusteenOsaTypes();
    }

    private HttpHeaders buildHeadersFor(Long id, UriComponentsBuilder ucb) {
        HttpHeaders headers = new HttpHeaders();
        headers.setLocation(ucb.path("/perusteenosat/{id}").buildAndExpand(id).toUri());
        return headers;
    }

    //apuluokka listan tyyppi-informaation säilyttämiseen.
    private static class PerusteenOsaDtoList extends WrappedList<PerusteenOsaDto.Laaja> {
        public PerusteenOsaDtoList(List<PerusteenOsaDto.Laaja> c) {
            super(c);
        }
    };

}
