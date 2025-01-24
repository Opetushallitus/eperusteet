package fi.vm.sade.eperusteet.resource;

import fi.vm.sade.eperusteet.config.InternalApi;
import fi.vm.sade.eperusteet.domain.Diaarinumero;
import fi.vm.sade.eperusteet.domain.ProjektiTila;
import fi.vm.sade.eperusteet.dto.OmistajaDto;
import fi.vm.sade.eperusteet.dto.TiedoteDto;
import fi.vm.sade.eperusteet.dto.TilaUpdateStatus;
import fi.vm.sade.eperusteet.dto.kayttaja.KayttajanProjektitiedotDto;
import fi.vm.sade.eperusteet.dto.kayttaja.KayttajanTietoDto;
import fi.vm.sade.eperusteet.dto.peruste.PerusteenOsaTyoryhmaDto;
import fi.vm.sade.eperusteet.dto.peruste.PerusteprojektiQueryDto;
import fi.vm.sade.eperusteet.dto.perusteprojekti.DiaarinumeroHakuDto;
import fi.vm.sade.eperusteet.dto.perusteprojekti.PerusteprojektiDto;
import fi.vm.sade.eperusteet.dto.perusteprojekti.PerusteprojektiInfoDto;
import fi.vm.sade.eperusteet.dto.perusteprojekti.PerusteprojektiKevytDto;
import fi.vm.sade.eperusteet.dto.perusteprojekti.PerusteprojektiListausDto;
import fi.vm.sade.eperusteet.dto.perusteprojekti.PerusteprojektiLuontiDto;
import fi.vm.sade.eperusteet.dto.perusteprojekti.TyoryhmaHenkiloDto;
import fi.vm.sade.eperusteet.dto.util.CombinedDto;
import fi.vm.sade.eperusteet.service.PerusteprojektiService;
import fi.vm.sade.eperusteet.service.security.PermissionManager;
import fi.vm.sade.eperusteet.service.util.Validointi;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static org.springframework.web.bind.annotation.RequestMethod.DELETE;
import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

@Slf4j
@RestController
@RequestMapping("/api/perusteprojektit")
@Tag(name = "Perusteprojektit")
@InternalApi
public class PerusteprojektiController {

    @Autowired
    private PerusteprojektiService service;

    @Autowired
    private PermissionManager permission;

    @RequestMapping(value = "/info", method = GET)
    @ResponseBody
    public ResponseEntity<List<PerusteprojektiInfoDto>> getAllPerusteprojektit() {
        return new ResponseEntity<>(service.getBasicInfo(), HttpStatus.OK);
    }

    @RequestMapping(value = "/perusteHaku", method = GET)
    @ResponseBody
    public Page<PerusteprojektiKevytDto> getAllPerusteprojektitKevyt(PerusteprojektiQueryDto pquery) {
        PageRequest p = PageRequest.of(pquery.getSivu(), Math.min(pquery.getSivukoko(), 20));
        Page<PerusteprojektiKevytDto> page = service.findBy(p, pquery);
        return page;
    }

    @RequestMapping(value = "/omat", method = GET)
    @ResponseBody
    public ResponseEntity<List<PerusteprojektiListausDto>> getOmatPerusteprojektit() {
        return new ResponseEntity<>(service.getOmatProjektit(), HttpStatus.OK);
    }

    @RequestMapping(value = "/omat/julkaistut", method = GET)
    @ResponseBody
    public ResponseEntity<List<PerusteprojektiListausDto>> getOmatJulkaistutPerusteprojektit() {
        return new ResponseEntity<>(service.getOmatJulkaistut(), HttpStatus.OK);
    }

    @RequestMapping(value = "/{id}", method = GET)
    @ResponseBody
    public ResponseEntity<PerusteprojektiDto> getPerusteprojekti(@PathVariable("id") final long id) {
        PerusteprojektiDto t = service.get(id);
        if (t == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(t, HttpStatus.OK);
    }

    @RequestMapping(value = "/{id}/jasenet", method = GET)
    @ResponseBody
    public ResponseEntity<List<KayttajanTietoDto>> getPerusteprojektiJasenet(@PathVariable("id") final long id) {
        return new ResponseEntity<>(service.getJasenet(id), HttpStatus.OK);
    }

    @RequestMapping(value = "/{id}/jasenet/tiedot", method = GET)
    @ResponseBody
    public ResponseEntity<List<CombinedDto<KayttajanTietoDto, KayttajanProjektitiedotDto>>> getJasenetTiedot(@PathVariable("id") final long id) {
        return new ResponseEntity<>(service.getJasenetTiedot(id), HttpStatus.OK);
    }

    @RequestMapping(value = "/{id}/tilat", method = GET)
    @ResponseBody
    public ResponseEntity<Set<ProjektiTila>> getPerusteprojektiTilat(@PathVariable("id") final long id) {
        return new ResponseEntity<>(service.getTilat(id), HttpStatus.OK);
    }

    @RequestMapping(value = "/{id}/validoi", method = GET)
    @ResponseBody
    public ResponseEntity<List<Validointi>> getPerusteprojektiValidointi(@PathVariable("id") final long id) {
        return new ResponseEntity<>(service.validoiProjekti(id, ProjektiTila.JULKAISTU), HttpStatus.OK);
    }

    @RequestMapping(value = "/{id}", method = POST)
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public PerusteprojektiDto updatePerusteprojekti(
            @PathVariable("id") final long id,
            @RequestBody PerusteprojektiDto perusteprojektiDto) {
        return service.update(id, perusteprojektiDto);
    }

    @RequestMapping(value = "/{id}/tila/{tila}", method = POST)
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public TilaUpdateStatus updatePerusteprojektiTila(
            @PathVariable("id") final long id,
            @PathVariable("tila") final String tila,
            @RequestBody TiedoteDto tiedoteDto
    ) {
        return service.updateTila(id, ProjektiTila.of(tila), tiedoteDto);
    }

    @RequestMapping(value = "/{id}/projekti/tila/{tila}", method = POST)
    @ResponseStatus(HttpStatus.OK)
    public void updateProjektiTilaOnly(
            @PathVariable("id") final long id,
            @PathVariable("tila") final String tila
    ) {
        service.updateProjektiTila(id, ProjektiTila.of(tila));
    }

    @RequestMapping(value = "/{id}/projekti/avaa", method = POST)
    @ResponseStatus(HttpStatus.OK)
    public void avaaPerusteProjekti(
            @PathVariable("id") final long id
    ) {
        service.avaaPerusteProjekti(id);
    }

    @RequestMapping(method = POST)
    @ResponseStatus(HttpStatus.CREATED)
    @ResponseBody
    public ResponseEntity<PerusteprojektiDto> addPerusteprojekti(
            @RequestBody PerusteprojektiLuontiDto perusteprojektiLuontiDto,
            UriComponentsBuilder ucb
    ) {
        PerusteprojektiDto perusteprojektiDto = service.save(perusteprojektiLuontiDto);
        return new ResponseEntity<>(perusteprojektiDto, buildHeadersFor(perusteprojektiDto.getId(), ucb), HttpStatus.CREATED);
    }

    @RequestMapping(method = POST, value = "/pohja")
    @ResponseStatus(HttpStatus.CREATED)
    @ResponseBody
    public ResponseEntity<PerusteprojektiDto> addPerusteprojektiPohja(
            @RequestBody PerusteprojektiLuontiDto perusteprojektiLuontiDto,
            UriComponentsBuilder ucb
    ) {
        PerusteprojektiDto perusteprojektiDto = service.savePohja(perusteprojektiLuontiDto);
        return new ResponseEntity<>(perusteprojektiDto, buildHeadersFor(perusteprojektiDto.getId(), ucb), HttpStatus.CREATED);
    }

    private HttpHeaders buildHeadersFor(Long id, UriComponentsBuilder ucb) {
        HttpHeaders headers = new HttpHeaders();
        headers.setLocation(ucb.path("/perusteprojektit/{id}").buildAndExpand(id).toUri());
        return headers;
    }

    @RequestMapping(value = "/diaarinumero/uniikki/{diaarinumero}", method = GET)
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public ResponseEntity<DiaarinumeroHakuDto> getPerusteprojektiByDiaarinumero(@PathVariable("diaarinumero") final Diaarinumero diaarinumero) {
        return new ResponseEntity<>(service.onkoDiaarinumeroKaytossa(diaarinumero), HttpStatus.OK);
    }

    @RequestMapping(value = "/{id}/tyoryhma", method = GET)
    @ResponseBody
    public ResponseEntity<List<TyoryhmaHenkiloDto>> getPerusteprojektiTyoryhmat(@PathVariable("id") final Long id) {
        List<TyoryhmaHenkiloDto> tyoryhmat = service.getTyoryhmaHenkilot(id);
        return new ResponseEntity<>(tyoryhmat, HttpStatus.OK);
    }

    @RequestMapping(value = "/{id}/tyoryhma/{nimi}", method = GET)
    @ResponseBody
    public ResponseEntity<List<TyoryhmaHenkiloDto>> getPerusteprojektiTyoryhmaByNimi(
            @PathVariable("nimi") final String nimi,
            @PathVariable("id") final Long id) {
        List<TyoryhmaHenkiloDto> tyoryhmat = service.getTyoryhmaHenkilot(id, nimi);
        return new ResponseEntity<>(tyoryhmat, HttpStatus.OK);
    }

    @RequestMapping(value = "/{id}/tyoryhma", method = POST)
    @ResponseBody
    public ResponseEntity<List<TyoryhmaHenkiloDto>> postMultipleTyoryhmaHenkilot(
            @PathVariable("id") final Long id,
            @RequestBody List<TyoryhmaHenkiloDto> tyoryhma) {
        List<TyoryhmaHenkiloDto> res = tyoryhma.stream()
                .map(thd -> service.saveTyoryhma(id, thd))
                .collect(Collectors.toList());
        return new ResponseEntity<>(res, HttpStatus.OK);
    }

    @RequestMapping(value = "/{id}/tyoryhma/{nimi}", method = POST)
    @ResponseBody
    public ResponseEntity<List<TyoryhmaHenkiloDto>> postMultipleTyoryhmaHenkilotToTyoryhma(
            @PathVariable("id") final Long id,
            @PathVariable("nimi") final String nimi,
            @RequestBody List<String> tyoryhma) {
        List<TyoryhmaHenkiloDto> res = service.saveTyoryhma(id, nimi, tyoryhma);
        return new ResponseEntity<>(res, HttpStatus.OK);
    }

    @RequestMapping(value = "/{id}/tyoryhma/{nimi}", method = DELETE)
    public ResponseEntity<TyoryhmaHenkiloDto> removeTyoryhmat(
            @PathVariable("id") final Long id,
            @PathVariable("nimi") final String nimi) {
        service.removeTyoryhma(id, nimi);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @RequestMapping(value = "/{id}/perusteenosat/{pid}/tyoryhmat", method = POST)
    @ResponseBody
    public ResponseEntity<List<String>> postPerusteenOsaTyoryhma(
            @PathVariable("id") final Long id,
            @PathVariable("pid") final Long pid,
            @RequestBody List<String> tyoryhmat) {
        return new ResponseEntity<>(service.setPerusteenOsaViiteTyoryhmat(id, pid, tyoryhmat), HttpStatus.OK);
    }

    @RequestMapping(value = "/{id}/perusteenosat/{pid}/tyoryhmat", method = GET)
    public ResponseEntity<List<String>> getPerusteprojektinTyoryhma(
            @PathVariable("id") final Long id,
            @PathVariable("pid") final Long pid) {
        return new ResponseEntity<>(service.getPerusteenOsaViiteTyoryhmat(id, pid), HttpStatus.OK);
    }

    @RequestMapping(value = "/{id}/perusteenosientyoryhmat", method = GET)
    public ResponseEntity<List<PerusteenOsaTyoryhmaDto>> getPerustenosienTyoryhmat(
            @PathVariable("id") final Long id) {
        List<PerusteenOsaTyoryhmaDto> sisallonTyoryhmat = service.getSisallonTyoryhmat(id);
        return new ResponseEntity<>(sisallonTyoryhmat, HttpStatus.OK);
    }

    @RequestMapping(value = "/{id}/oikeudet", method = GET)
    public ResponseEntity<Map<PermissionManager.Target, Set<PermissionManager.Permission>>> getPerusteprojektiOikeudet(
            @PathVariable("id") final Long id) {
        return new ResponseEntity<>(permission.getProjectPermissions(id), HttpStatus.OK);
    }

    @RequestMapping(value = "/oikeudet", method = GET)
    public ResponseEntity<Map<Long, Map<PermissionManager.Target, Set<PermissionManager.Permission>>>> getPerusteprojektienOikeudet(
            @RequestParam(value = "ids") final List<Long> ids) {
        return new ResponseEntity<>(ids.stream().collect(Collectors.toMap(id -> id, id -> permission.getProjectPermissions(id))), HttpStatus.OK);
    }

    @RequestMapping(value = "/{id}/oikeudet/{perusteenOsaId}", method = GET)
    public ResponseEntity<OmistajaDto> omistaaPerusteprojektiOsan(
            @PathVariable("id") final Long id,
            @PathVariable("perusteenOsaId") final Long perusteenOsaId) {
        return new ResponseEntity<>(service.isOwner(id, perusteenOsaId), HttpStatus.OK);
    }
}
