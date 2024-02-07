package fi.vm.sade.eperusteet.dto.yl.lukio;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class KurssinOppiaineDto implements Serializable {
    private Long oppiaineId;
    private Integer jarjestys;
}
