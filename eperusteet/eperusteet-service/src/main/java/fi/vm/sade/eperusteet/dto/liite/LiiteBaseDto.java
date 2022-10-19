package fi.vm.sade.eperusteet.dto.liite;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LiiteBaseDto {
    private UUID id;
    private String nimi;
}
