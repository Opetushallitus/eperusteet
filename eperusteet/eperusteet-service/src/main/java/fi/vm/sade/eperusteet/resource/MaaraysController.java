package fi.vm.sade.eperusteet.resource;

import com.google.common.base.Throwables;
import fi.vm.sade.eperusteet.domain.Kieli;
import fi.vm.sade.eperusteet.domain.KoulutusTyyppi;
import fi.vm.sade.eperusteet.domain.maarays.MaaraysTyyppi;
import fi.vm.sade.eperusteet.dto.Voimassaolo;
import fi.vm.sade.eperusteet.dto.maarays.MaaraysAsiasanaDto;
import fi.vm.sade.eperusteet.dto.maarays.MaaraysDto;
import fi.vm.sade.eperusteet.dto.maarays.MaaraysKevytDto;
import fi.vm.sade.eperusteet.dto.maarays.MaaraysLiiteDto;
import fi.vm.sade.eperusteet.dto.maarays.MaaraysLiiteUploadDto;
import fi.vm.sade.eperusteet.dto.maarays.MaaraysQueryDto;
import fi.vm.sade.eperusteet.resource.config.InternalApi;
import fi.vm.sade.eperusteet.resource.util.CacheControl;
import fi.vm.sade.eperusteet.service.MaaraysService;
import fi.vm.sade.eperusteet.service.exception.BusinessRuleViolationException;
import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@InternalApi
@RestController
@RequestMapping(value = "/maaraykset")
@Api("Maaraykset")
public class MaaraysController {

    @Autowired
    private MaaraysService maaraysService;

    @GetMapping
    public Page<MaaraysDto> getMaaraykset(
            @RequestParam(value = "nimi", defaultValue = "", required = false) String nimi,
            @RequestParam(value = "kieli", defaultValue = "fi", required = false) String kieli,
            @RequestParam(value = "tyyppi", required = false) final MaaraysTyyppi tyyppi,
            @RequestParam(value = "koulutustyyppi", required = false) final List<String> koulutustyypit,
            @RequestParam(value = "tuleva", required = false) boolean tuleva,
            @RequestParam(value = "voimassa", required = false) boolean voimassa,
            @RequestParam(value = "paattynyt", required = false) boolean paattynyt,
            @RequestParam(value = "luonnos", required = false) boolean luonnos,
            @RequestParam(value = "julkaistu", required = false) boolean julkaistu,
            @RequestParam(value = "sivu", required = false, defaultValue = "0") Integer sivu,
            @RequestParam(value = "sivukoko", required = false, defaultValue = "10") Integer sivukoko,
            @RequestParam(value = "jarjestysTapa", defaultValue = "nimi", required = false) String jarjestysTapa,
            @RequestParam(value = "jarjestys", defaultValue = "ASC", required = false) String jarjestys) {
        return maaraysService.getMaaraykset(MaaraysQueryDto.builder()
                        .nimi(nimi)
                        .kieli(Kieli.of(kieli))
                        .tyyppi(tyyppi)
                        .koulutustyypit(koulutustyypit)
                        .tuleva(tuleva)
                        .voimassa(voimassa)
                        .paattynyt(paattynyt)
                        .luonnos(luonnos)
                        .julkaistu(julkaistu)
                        .sivu(sivu)
                        .sivukoko(sivukoko)
                        .jarjestysTapa(jarjestysTapa)
                        .jarjestys(Sort.Direction.fromString(jarjestys))
                .build());
    }

    @GetMapping(value = "/koulutustyypit")
    public List<String> getMaarayksienKoulutustyypit() {
        return maaraysService.getMaarayksienKoulutustyypit();
    }

    @GetMapping(value = "/nimet")
    public List<MaaraysKevytDto> getMaarayksetNimet() {
        return maaraysService.getMaaraykset(MaaraysKevytDto.class);
    }

    @GetMapping(value = "/asiasanat")
    public Map<Kieli, List<String>> getAsiasanat() {
        return maaraysService.getAsiasanat();
    }

    @GetMapping(value = "/{id}")
    public MaaraysDto getMaarays(@PathVariable("id") final Long id) {
        return maaraysService.getMaarays(id);
    }

    @GetMapping(value = "/peruste/{perusteId}")
    public MaaraysDto getMaaraysPerusteella(@PathVariable("perusteId") final Long perusteId) {
        return maaraysService.getPerusteenMaarays(perusteId);
    }

    @InternalApi
    @PostMapping
    public MaaraysDto addMaarays(@RequestBody MaaraysDto maaraysDto) {
        return maaraysService.addMaarays(maaraysDto);
    }

    @InternalApi
    @PostMapping(value = "/{id}")
    public MaaraysDto updateMaarays(
            @PathVariable("id") final Long id,
            @RequestBody MaaraysDto maaraysDto) {
        return maaraysService.updateMaarays(maaraysDto);
    }

    @InternalApi
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @DeleteMapping(value = "/{id}")
    public void deleteMaarays(@PathVariable("id") final Long id) {
        maaraysService.deleteMaarays(id);
    }

    @PostMapping(value = "/liite")
    public ResponseEntity<String> uploadMaaraysLiite(@RequestBody MaaraysLiiteDto maaraysLiiteUploadDto) {
        UUID uuid = maaraysService.uploadFile(maaraysLiiteUploadDto);
        return new ResponseEntity<>(uuid.toString(), HttpStatus.CREATED);
    }

    @GetMapping(value = "/liite/{fileName}")
    @CacheControl(age = CacheControl.ONE_YEAR)
    public void getMaaraysLiite(
            @PathVariable("fileName") String fileName,
            @RequestHeader(value = "If-None-Match", required = false) String etag,
            HttpServletResponse response
    ) {
        UUID id = UUID.fromString(fileName);
        MaaraysLiiteDto dto = maaraysService.getLiite(id);

        try {
            getFile(id, dto, response, etag);
        } catch(Exception e) {
            log.error(Throwables.getStackTraceAsString(e));
            throw new BusinessRuleViolationException("liitteen-haku-epaonnistui");
        }
    }

    private void getFile(UUID id, MaaraysLiiteDto maaraysLiite, HttpServletResponse response, String etag) throws IOException, SQLException {
        if (maaraysLiite != null) {
            response.setHeader("Content-disposition", "inline; filename=\"" + maaraysLiite.getNimi() + ".pdf\"");

            if (maaraysLiite.getId().toString().equals(etag)) {
                response.setStatus(HttpStatus.NOT_MODIFIED.value());
            } else {
                response.setHeader("Content-Type", MediaType.APPLICATION_PDF_VALUE);
                response.setHeader("ETag", id.toString());
                try (OutputStream os = response.getOutputStream()) {
                    maaraysService.exportLiite(id, os);
                    os.flush();
                }
            }
        } else {
            response.setStatus(HttpStatus.NOT_FOUND.value());
        }
    }


}
