package fi.vm.sade.eperusteet.resource;

import fi.vm.sade.eperusteet.dto.KommenttiDto;
import fi.vm.sade.eperusteet.dto.kayttaja.KayttajanTietoDto;
import fi.vm.sade.eperusteet.config.InternalApi;
import fi.vm.sade.eperusteet.service.KayttajanTietoService;
import fi.vm.sade.eperusteet.service.KommenttiService;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.web.bind.annotation.RequestMethod.DELETE;
import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;
import static org.springframework.web.bind.annotation.RequestMethod.PUT;

@RestController
@RequestMapping("/api/kommentit")
@InternalApi
public class KommenttiController {

    @Autowired
    KommenttiService service;

    @Autowired
    KayttajanTietoService kayttajanTietoService;

    private List<KommenttiDto> rikastaKommentit(List<KommenttiDto> kommentit) {
        for (KommenttiDto k : kommentit) {
            KayttajanTietoDto kayttaja = kayttajanTietoService.hae(k.getMuokkaaja());
            if (kayttaja != null) {
                String kutsumanimi = kayttaja.getKutsumanimi();
                String etunimet = kayttaja.getEtunimet();
                String etunimi = kutsumanimi != null ? kutsumanimi : etunimet;
                k.setNimi(etunimi + " " + kayttaja.getSukunimi());
            }
        }
        return kommentit;
    }

    @RequestMapping(value = "/perusteprojekti/{id}/suoritustapa/{suoritustapa}", method = GET)
    public ResponseEntity<List<KommenttiDto>> getAllKommentitBySuoritustapa(
            @PathVariable("id") final long id,
            @PathVariable("suoritustapa") final String suoritustapa) {
        List<KommenttiDto> t = service.getAllBySuoritustapa(id, suoritustapa);
        return new ResponseEntity<>(rikastaKommentit(t), HttpStatus.OK);
    }

    @RequestMapping(value = "/perusteprojekti/{id}/perusteenosa/{perusteenOsaId}", method = GET)
    public ResponseEntity<List<KommenttiDto>> getAllKommentitByPerusteenOsa(
            @PathVariable("id") final long id,
            @PathVariable("perusteenOsaId") final long perusteenOsaId) {
        List<KommenttiDto> t = service.getAllByPerusteenOsa(id, perusteenOsaId);
        return new ResponseEntity<>(rikastaKommentit(t), HttpStatus.OK);
    }

    @RequestMapping(value = "/perusteprojekti/{id}", method = GET)
    public ResponseEntity<List<KommenttiDto>> getAllKommentit(@PathVariable("id") final long id) {
        List<KommenttiDto> t = service.getAllByPerusteprojekti(id);
        return new ResponseEntity<>(rikastaKommentit(t), HttpStatus.OK);
    }

    @RequestMapping(value = "/ylin/{id}", method = GET)
    public ResponseEntity<List<KommenttiDto>> getAllKommentitByYlin(@PathVariable("id") final long id) {
        List<KommenttiDto> t = service.getAllByYlin(id);
        return new ResponseEntity<>(rikastaKommentit(t), HttpStatus.OK);
    }

    @RequestMapping(value = "/parent/{id}", method = GET)
    public ResponseEntity<List<KommenttiDto>> getAllByParent(@PathVariable("id") final long id) {
        List<KommenttiDto> t = service.getAllByParent(id);
        return new ResponseEntity<>(rikastaKommentit(t), HttpStatus.OK);
    }

    @RequestMapping(value = "/{id}", method = GET)
    public ResponseEntity<KommenttiDto> getKommentti(@PathVariable("id") final long id) {
        KommenttiDto t = service.get(id);
        return new ResponseEntity<>(t, t == null ? HttpStatus.NOT_FOUND : HttpStatus.OK);
    }

    @RequestMapping(method = {POST, PUT})
    public ResponseEntity<KommenttiDto> addKommentti(@RequestBody KommenttiDto body) {
        return new ResponseEntity<>(service.add(body), HttpStatus.CREATED);
    }

    @RequestMapping(value = "/{id}", method = {POST, PUT})
    public ResponseEntity<KommenttiDto> updateKommentti(@PathVariable("id") final long id, @RequestBody KommenttiDto body) {
        return new ResponseEntity<>(service.update(id, body), HttpStatus.OK);
    }

    @RequestMapping(value = "/{id}", method = DELETE)
    public void deleteKommentti(@PathVariable("id") final long id) {
        service.delete(id);
    }
}
