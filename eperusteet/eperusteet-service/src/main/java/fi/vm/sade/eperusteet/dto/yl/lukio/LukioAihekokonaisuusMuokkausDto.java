package fi.vm.sade.eperusteet.dto.yl.lukio;

import fi.vm.sade.eperusteet.dto.util.LokalisoituTekstiDto;
import fi.vm.sade.eperusteet.dto.util.UpdateDto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.Optional;

@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class LukioAihekokonaisuusMuokkausDto extends UpdateDto<LukioAihekokonaisuusMuokkausDto> implements Serializable {
    @NotNull
    private Long id;
    @NotNull
    private LokalisoituTekstiDto otsikko;
    private Optional<LokalisoituTekstiDto> yleiskuvaus;
    private Long jnro;
}
