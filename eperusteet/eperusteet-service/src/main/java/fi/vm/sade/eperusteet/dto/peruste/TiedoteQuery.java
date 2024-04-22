package fi.vm.sade.eperusteet.dto.peruste;

import fi.vm.sade.eperusteet.domain.Kieli;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TiedoteQuery {
    private int sivu = 0;
    private int sivukoko = 25;
    private List<Kieli> kieli;
    private String nimi;
    private Long perusteId;
    private Boolean perusteeton; // Jos true, palauttaa vain ne tiedotteet, jotka eivät liity mihinkään perusteeseen
    private Boolean julkinen; // Jos null, haetaan julkiset ja sisäiset
    private Boolean yleinen; // Jos halutaan esittää mm. etusivulla
    private List<String> tiedoteJulkaisuPaikka;
    private List<String> koulutusTyyppi;
    private List<Long> perusteIds;
    private String jarjestys;
    private Boolean jarjestysNouseva;
    private Boolean koulutustyypiton;
}
