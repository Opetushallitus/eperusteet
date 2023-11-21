package fi.vm.sade.eperusteet.dto.maarays;

import com.fasterxml.jackson.annotation.JsonIgnore;
import fi.vm.sade.eperusteet.domain.Kieli;
import fi.vm.sade.eperusteet.domain.KoulutusTyyppi;
import fi.vm.sade.eperusteet.domain.maarays.MaaraysTila;
import fi.vm.sade.eperusteet.domain.maarays.MaaraysTyyppi;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Sort;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MaaraysQueryDto {
    private String nimi = "";
    private Kieli kieli = Kieli.FI;
    private MaaraysTyyppi tyyppi;
    private List<KoulutusTyyppi> koulutustyypit;
    private boolean tuleva;
    private boolean voimassa;
    private boolean paattynyt;
    private boolean luonnos;
    private boolean julkaistu;
    private Integer sivu = 0;
    private Integer sivukoko = 10;
    private String jarjestysTapa = "nimi";
    private Sort.Direction jarjestys = Sort.Direction.ASC;

    @JsonIgnore
    public MaaraysTila getTila() {
        if (luonnos && !julkaistu) {
            return MaaraysTila.LUONNOS;
        }

        if (julkaistu && !luonnos) {
            return MaaraysTila.JULKAISTU;
        }

        return null;
    }
}
