package fi.vm.sade.eperusteet.service.mapping;

import com.fasterxml.jackson.databind.JsonNode;
import fi.vm.sade.eperusteet.dto.kayttaja.KayttajanTietoDto;
import java.util.ArrayList;
import java.util.List;

public class KayttajanTietoParser {

    private static String getField(JsonNode json, String... fields) {
        for (String field : fields) {
            if (json != null) {
                json = json.get(field);
            } else {
                return null;
            }
        }
        return json != null ? json.asText() : null;
    }

    public static KayttajanTietoDto parsiKayttaja(JsonNode json) {
        KayttajanTietoDto ktd = new KayttajanTietoDto(getField(json, "oidHenkilo"));
        ktd.setEtunimet(getField(json, "etunimet"));
        ktd.setKieliKoodi(getField(json, "asiointiKieli", "kieliKoodi"));
        ktd.setKutsumanimi(getField(json, "kutsumanimi"));
        ktd.setSukunimi(getField(json, "sukunimi"));
        ktd.setOidHenkilo(getField(json, "oidHenkilo"));
        ktd.setYhteystiedot(json.get("yhteystiedotRyhma"));
        return ktd;
    }

    public static List<KayttajanTietoDto> parsiKayttajat(JsonNode jsonList) {
        List<KayttajanTietoDto> ktds = new ArrayList<>();
        for (JsonNode json : jsonList) {
            ktds.add(parsiKayttaja(json));
        }
        return ktds;
    }

}
