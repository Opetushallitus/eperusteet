package fi.vm.sade.eperusteet.resource;

import fi.vm.sade.eperusteet.dto.LokalisointiDto;
import fi.vm.sade.eperusteet.config.InternalApi;
import fi.vm.sade.eperusteet.service.LokalisointiService;
import io.swagger.annotations.Api;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

@InternalApi
@RestController
@RequestMapping("/api/lokalisointi")
@Api("Lokalisointi")
public class LokalisointiController {

    @Autowired
    private LokalisointiService lokalisointiService;

    @RequestMapping(value = "/eperusteet-opintopolku", method = GET)
    public List<LokalisointiDto> getAllKaannokset(@RequestParam(value = "locale", defaultValue = "fi") final String kieli) {
        return Stream.concat(
                lokalisointiService.getAllByCategoryAndLocale("eperusteet-opintopolku", kieli).stream(),
                lokalisointiService.getAllByCategoryAndLocale("eperusteet", kieli).stream())
                .collect(Collectors.toList());
    }

    @RequestMapping(value = "/kaannokset/{palvelu}/{kieli}", method = GET)
    public List<LokalisointiDto> getPalveluKaannokset(
            @PathVariable(value = "palvelu", required = true) final String palvelu,
            @PathVariable(value = "kieli", required = true) final String kieli) {
        return lokalisointiService.getAllByCategoryAndLocale(palvelu, kieli);
    }

    @RequestMapping(value = "/kaannokset", method = GET)
    public List<LokalisointiDto> getEperusteKaannokset() {
        ArrayList<LokalisointiDto> kaannokset = new ArrayList<>();
        kaannokset.addAll(lokalisointiService.getAllByCategoryAndLocale("eperusteet-opintopolku", "fi"));
        kaannokset.addAll(lokalisointiService.getAllByCategoryAndLocale("eperusteet-opintopolku", "sv"));
        kaannokset.addAll(lokalisointiService.getAllByCategoryAndLocale("eperusteet-opintopolku", "en"));
        kaannokset.addAll(lokalisointiService.getAllByCategoryAndLocale("eperusteet-ylops", "fi"));
        kaannokset.addAll(lokalisointiService.getAllByCategoryAndLocale("eperusteet-ylops", "sv"));
        kaannokset.addAll(lokalisointiService.getAllByCategoryAndLocale("eperusteet-ylops", "en"));
        kaannokset.addAll(lokalisointiService.getAllByCategoryAndLocale("eperusteet", "fi"));
        kaannokset.addAll(lokalisointiService.getAllByCategoryAndLocale("eperusteet", "sv"));
        kaannokset.addAll(lokalisointiService.getAllByCategoryAndLocale("eperusteet", "en"));
        return kaannokset;
    }

    @RequestMapping(value = "/kaannokset", method = POST)
    public void updateKaannokset(@RequestBody final List<LokalisointiDto> lokalisoinnit) {
        lokalisointiService.save(lokalisoinnit);
    }

}
