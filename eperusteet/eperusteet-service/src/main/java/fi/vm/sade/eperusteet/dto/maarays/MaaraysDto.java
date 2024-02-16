package fi.vm.sade.eperusteet.dto.maarays;

import fi.vm.sade.eperusteet.domain.Kieli;
import fi.vm.sade.eperusteet.domain.maarays.MaaraysLiittyyTyyppi;
import fi.vm.sade.eperusteet.domain.maarays.MaaraysTila;
import fi.vm.sade.eperusteet.domain.maarays.MaaraysTyyppi;
import fi.vm.sade.eperusteet.dto.kayttaja.KayttajanTietoDto;
import fi.vm.sade.eperusteet.dto.peruste.PerusteKevytDto;
import fi.vm.sade.eperusteet.dto.util.LokalisoituTekstiDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MaaraysDto {
    private Long id;
    private LokalisoituTekstiDto nimi;
    private LokalisoituTekstiDto kuvaus;
    private String diaarinumero;
    private Date voimassaoloAlkaa;
    private Date voimassaoloLoppuu;
    private Date maarayspvm;
    private Date muokattu;
    private MaaraysTyyppi tyyppi;
    private MaaraysLiittyyTyyppi liittyyTyyppi;
    private MaaraysTila tila;
    private List<String> koulutustyypit;
    private PerusteKevytDto peruste;
    private List<MaaraysKevytDto> korvattavatMaaraykset = new ArrayList<>();
    private List<MaaraysKevytDto> korvaavatMaaraykset = new ArrayList<>();
    private List<MaaraysKevytDto> muutettavatMaaraykset = new ArrayList<>();
    private List<MaaraysKevytDto> muuttavatMaaraykset = new ArrayList<>();
    private Map<Kieli, MaaraysKieliLiitteetDto> liitteet;
    private Map<Kieli, MaaraysAsiasanaDto> asiasanat;
    private KayttajanTietoDto muokkaajaKayttaja;
    private String muokkaaja;
}
