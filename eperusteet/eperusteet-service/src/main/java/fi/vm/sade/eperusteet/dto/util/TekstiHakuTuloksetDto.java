package fi.vm.sade.eperusteet.dto.util;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@EqualsAndHashCode(of = { "sivu", "sivukoko", "teksti" })
public class TekstiHakuTuloksetDto implements Serializable {
    List<TekstiHakuTulosDto> tulokset = new ArrayList<>();
    int sivu = 0;
    int sivukoko = 10;
    String teksti;
}
