package fi.vm.sade.eperusteet.dto.yl.lukio.julkinen;

import fi.vm.sade.eperusteet.dto.peruste.PerusteenOsaViiteDto;
import fi.vm.sade.eperusteet.dto.peruste.PerusteenSisaltoDto;
import fi.vm.sade.eperusteet.dto.yl.lukio.osaviitteet.AihekokonaisuudetLaajaDto;
import fi.vm.sade.eperusteet.dto.yl.lukio.osaviitteet.OpetuksenYleisetTavoitteetLaajaDto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LukiokoulutuksenPerusteenSisaltoDto implements Serializable, PerusteenSisaltoDto {
    private PerusteenOsaViiteDto.Laaja sisalto;
    private AihekokonaisuudetLaajaDto aihekokonaisuudet;
    private OpetuksenYleisetTavoitteetLaajaDto opetuksenYleisetTavoitteet;
    private LukioOppiainePuuDto rakenne;
}
