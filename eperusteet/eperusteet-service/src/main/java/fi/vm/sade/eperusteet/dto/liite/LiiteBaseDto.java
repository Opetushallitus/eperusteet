package fi.vm.sade.eperusteet.dto.liite;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LiiteBaseDto {
    private UUID id;
    private String nimi;
}
