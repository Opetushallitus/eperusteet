package fi.vm.sade.eperusteet.dto;

import fi.vm.sade.eperusteet.service.util.Validointi;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Data
@NoArgsConstructor
public class TilaUpdateStatus {

    private List<Validointi> validoinnit = new ArrayList<>();

    public TilaUpdateStatus(List<Validointi> validoinnit) {
        validoinnit.forEach(this::addValidointi);
    }

    public boolean isVaihtoOk() {
        return validoinnit.stream().noneMatch(Validointi::virheellinen);
    }

    public void addValidointi(Validointi uusiValidointi) {
        if (!CollectionUtils.isEmpty(uusiValidointi.getVirheet()) || !CollectionUtils.isEmpty(uusiValidointi.getHuomautukset())) {
            Optional<Validointi> validointi = validoinnit.stream().filter(v -> v.getKategoria().equals(uusiValidointi.getKategoria())).findFirst();
            if(validointi.isPresent()) {
                uusiValidointi.getVirheet().forEach(virhe -> validointi.get().addVirhe(virhe));
                uusiValidointi.getHuomautukset().forEach(huomautus -> validointi.get().addHuomautus(huomautus));
            } else {
                validoinnit.add(uusiValidointi);
            }
        }
    }

    public List<Validointi.Virhe> getVirheet() {
        return validoinnit.stream().map(Validointi::getVirheet).flatMap(Collection::stream).collect(Collectors.toList());
    }

    public List<Validointi.Virhe> getHuomautukset() {
        return validoinnit.stream().map(Validointi::getHuomautukset).flatMap(Collection::stream).collect(Collectors.toList());
    }

}
