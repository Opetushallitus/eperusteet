package fi.vm.sade.eperusteet.resource;

import fi.vm.sade.eperusteet.dto.julkinen.JulkiEtusivuDto;
import fi.vm.sade.eperusteet.dto.julkinen.TietoaPalvelustaDto;
import fi.vm.sade.eperusteet.service.JulkinenService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/julkinen")
@Tag(name = "julkinen")
public class JulkinenController {

    @Autowired
    private JulkinenService julkinenService;

    @GetMapping("/etusivu")
    public Page<JulkiEtusivuDto> getOpetussuunnitelmatJaPerusteet(
            @RequestParam(value = "nimi", defaultValue = "", required = false) final String nimi,
            @RequestParam(value = "kieli", defaultValue = "fi", required = false) final String kieli,
            @RequestParam(value = "sivu", defaultValue = "0", required = false) final Integer sivu,
            @RequestParam(value = "sivukoko", defaultValue = "10", required = false) final Integer sivukoko) {
        return julkinenService.haeEtusivu(nimi, kieli, sivu, sivukoko);
    }

    @GetMapping("/tietoapalvelusta")
    public ResponseEntity<TietoaPalvelustaDto> getTietoaPalvelusta() {
        TietoaPalvelustaDto dto = julkinenService.getTietoaPalvelusta();
        if (dto == null) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(dto);
    }
}
