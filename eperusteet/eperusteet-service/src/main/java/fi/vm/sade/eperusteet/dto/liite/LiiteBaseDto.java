package fi.vm.sade.eperusteet.dto.liite;

import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class LiiteBaseDto {
    private UUID id;
    private String nimi;
}
