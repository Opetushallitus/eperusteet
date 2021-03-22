package fi.vm.sade.eperusteet.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import fi.vm.sade.eperusteet.dto.PalauteDto;
import fi.vm.sade.eperusteet.service.PalauteService;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

@Profile("local")
@Service
public class PalauteServiceLocalImpl implements PalauteService {

    private static List<PalauteDto> palautteet = new ArrayList<>();
    private final static String PALAUTE_KEY = "eperusteet-opintopolku";

    @Override
    public PalauteDto lahetaPalaute(PalauteDto palaute) throws JsonProcessingException {
        palaute.setCreatedAt(new Date());
        if (ObjectUtils.isEmpty(palaute.getKey())) {
            palaute.setKey(PALAUTE_KEY);
        }
        palautteet.add(palaute);
        return palaute;
    }

    @Override
    public List<Object> getPalautteet(String palautekanava) {
        return palautteet.stream().filter(palaute -> palautekanava.equals(palaute.getKey())).collect(Collectors.toList());
    }

}
