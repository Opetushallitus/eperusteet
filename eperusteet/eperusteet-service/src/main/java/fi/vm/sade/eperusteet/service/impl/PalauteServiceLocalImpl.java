package fi.vm.sade.eperusteet.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import fi.vm.sade.eperusteet.dto.PalauteDto;
import fi.vm.sade.eperusteet.service.PalauteService;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

@Profile("local")
@Service
public class PalauteServiceLocalImpl implements PalauteService {

    private static List<Object> palautteet = new ArrayList<>();
    private final static String PALAUTE_KEY = "eperusteet-opintopolku";

    @Override
    public PalauteDto lahetaPalaute(PalauteDto palaute) throws JsonProcessingException {
        palaute.setCreatedAt(new Date());
        palaute.setKey(PALAUTE_KEY);
        palautteet.add(palaute);
        return palaute;
    }

    @Override
    public List<Object> getPalautteet() {
        return palautteet;
    }
}
