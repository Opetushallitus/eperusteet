package fi.vm.sade.eperusteet.dto.lukutaitokoulutus;

import com.fasterxml.jackson.annotation.JsonTypeName;
import fi.vm.sade.eperusteet.dto.peruste.NavigationType;
import fi.vm.sade.eperusteet.dto.peruste.PerusteenOsaDto;
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@JsonTypeName("lukutaitokoulutus_tavoitteetjasisaltoalueet")
public class LukutaitokoulutusTavoitteetJaSisaltoalueetDto extends PerusteenOsaDto.Laaja {

    private List<LukutaitokoulutusTavoitealueDto> tavoitealueet = new ArrayList<>();

    @Override
    public String getOsanTyyppi() {
        return "lukutaitokoulutus_tavoitteetjasisaltoalueet";
    }

    @Override
    public NavigationType getNavigationType() {
        return NavigationType.lukutaitokoulutus_tavoitteetjasisaltoalueet;
    }
}
