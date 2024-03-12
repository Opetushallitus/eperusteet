package fi.vm.sade.eperusteet.dto.julkinen;

import fi.vm.sade.eperusteet.domain.Kieli;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.Map;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class JulkiEtusivuDto {
    private Long id;
    private JulkiEtusivuTyyppi etusivuTyyppi;
    private String koulutustyyppi;
    private Map<String, String> nimi;
    private Date voimassaoloAlkaa;
    private Date voimassaoloLoppuu;
    private AmosaaKoulutustoimijaDto koulutustoimija;
    private Set<YlopsOrganisaatioDto> organisaatiot;
    private Set<Kieli> kielet;
}
