package fi.vm.sade.eperusteet.dto.tutkinnonosa;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import fi.vm.sade.eperusteet.domain.Kieli;
import fi.vm.sade.eperusteet.domain.tutkinnonosa.TutkinnonOsaTyyppi;
import fi.vm.sade.eperusteet.dto.GeneerinenArviointiasteikkoKaikkiDto;
import fi.vm.sade.eperusteet.dto.KevytTekstiKappaleDto;
import fi.vm.sade.eperusteet.dto.ammattitaitovaatimukset.AmmattitaitovaatimusKohdealueetDto;
import fi.vm.sade.eperusteet.dto.arviointi.ArviointiDto;
import fi.vm.sade.eperusteet.dto.peruste.PerusteRakenneOsa;
import fi.vm.sade.eperusteet.dto.peruste.PerusteenOsaDto;
import fi.vm.sade.eperusteet.dto.tutkinnonrakenne.KoodiDto;
import fi.vm.sade.eperusteet.dto.util.LokalisoituTekstiDto;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class TutkinnonOsaKaikkiDto extends PerusteenOsaDto {
    private final String osanTyyppi = "tutkinnonosa";

    private LokalisoituTekstiDto kuvaus;
    private Long opintoluokitus;
    private KoodiDto koodi;
    private String koodiUri;
    private String koodiArvo;
    private BigDecimal laajuus;
    private Long tutkinnonosaViiteId;

    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private BigDecimal laajuusMaksimi;

    @Schema(description = "Yhteisen tutkinnon osan osa-alueet")
    private List<OsaAlueKaikkiDto> osaAlueet;

    @Schema(description = "Ilmaisee onko kyseessä normaali vai yhteinen osa (uusi tai vanha)")
    private TutkinnonOsaTyyppi tyyppi;
    private ValmaTelmaSisaltoDto valmaTelmaSisalto;

    @Schema(description = "Yleinen perusteen ulkopuolella käytetty arviointiasteikko. Käytetään kaikissa uusissa perusteissa.")
    private GeneerinenArviointiasteikkoKaikkiDto geneerinenArviointiasteikko;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @Schema(description = "Tutkinnon osan lisätarkennukset")
    private List<KevytTekstiKappaleDto> vapaatTekstit;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @Schema(description = "Uusien reformin mukaisien perusteiden ammattitaitovaatimukset")
    private Ammattitaitovaatimukset2019Dto ammattitaitovaatimukset2019;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private LokalisoituTekstiDto ammattitaidonOsoittamistavat;

    @Deprecated
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @Schema(description = "Vanhentunut rakenteeton tavoitteet. Ei käytössä uusissa reformin mukaisissa tutkinnon osissa.")
    private LokalisoituTekstiDto tavoitteet;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @Schema(description = "Tutkinnon osa -kohtainen arviointi")
    private ArviointiDto arviointi;

    @Deprecated
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @Schema(description = "Vanhentunut rakenteeton ammattitaitovaatimukset. Ei käytössä uusissa reformin mukaisissa tutkinnon osissa.")
    private LokalisoituTekstiDto ammattitaitovaatimukset;

    @Deprecated
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @Schema(description = "Vanhentunut rakenteeton ammattitaitovaatimukset. Ei käytössä uusissa reformin mukaisissa tutkinnon osissa.")
    private List<AmmattitaitovaatimusKohdealueetDto> ammattitaitovaatimuksetLista;

    public LokalisoituTekstiDto getAmmattitaitovaatimukset() {
        if (ammattitaitovaatimukset == null && ammattitaitovaatimukset2019 != null) {
            Map<Kieli, String> tekstit = new HashMap<>();
            for (Kieli kieli : Kieli.values()) {
                StringBuilder root = new StringBuilder();
                String kohde = LokalisoituTekstiDto.getOrDefault(ammattitaitovaatimukset2019.getKohde(), kieli, null);
                root.append("<dl>");
                if (kohde != null) {
                    root.append("<dt><i>").append(kohde).append("</i></dt>");
                }

                { // Kohdealueettomat
                    for (Ammattitaitovaatimus2019Dto va : ammattitaitovaatimukset2019.getVaatimukset()) {
                        String str = LokalisoituTekstiDto.getOrDefault(va.getVaatimus(), kieli, null);
                        if (!StringUtils.isEmpty(str)) {
                            root.append("<dd style=\"display: list-item;\">").append(str).append("</dd>");
                        }
                    }
                    root.append("</dl>");
                }

                { // Kohdealueelliset
                    for (AmmattitaitovaatimustenKohdealue2019Dto ka : ammattitaitovaatimukset2019.getKohdealueet()) {
                        if (ka.getVaatimukset() == null || ka.getVaatimukset().isEmpty()) {
                            continue;
                        }

                        if (ka.getKuvaus() != null) {
                            String nimi = LokalisoituTekstiDto.getOrDefault(ka.getKuvaus(), kieli, null);
                            if (nimi != null) {
                                root.append("<b>").append(nimi).append("</b>");
                            }
                        }

                        root.append("<dl>");

                        if (kohde != null) {
                            root.append("<dt><i>").append(kohde).append("</i></dt>");
                        }

                        for (Ammattitaitovaatimus2019Dto va : ka.getVaatimukset()) {
                            String str = LokalisoituTekstiDto.getOrDefault(va.getVaatimus(), kieli, null);
                            if (!StringUtils.isEmpty(str)) {
                                root.append("<dd style=\"display: list-item;\">").append(str).append("</dd>");
                            }
                        }
                        root.append("</dl>");
                    }
                }

                String result = root.toString().replaceAll("<dl></dl>", "");

                if (!result.isEmpty()) {
                    tekstit.put(kieli, result);
                }
            }
            return LokalisoituTekstiDto.of(tekstit.isEmpty() ? null : tekstit);
        }
        return ammattitaitovaatimukset;
    }

    public String getKoodiUri() {
        KoodiDto koodi = this.getKoodi();
        if (koodi != null) {
            return koodi.getUri();
        } else {
            return koodiUri;
        }
    }

    public String getKoodiArvo() {
        KoodiDto koodi = this.getKoodi();
        if (koodi != null) {
            return koodi.getArvo();
        } else {
            return koodiArvo;
        }
    }

    @Override
    public LokalisoituTekstiDto getNimi() {
        if (koodi != null && koodi.getNimi() != null && !org.springframework.util.CollectionUtils.isEmpty(koodi.getNimi().getTekstit())) {
            Map<String, String> kielet = new HashMap<>();
            kielet.computeIfAbsent("fi", val -> koodi.getNimi().getTekstit().getOrDefault(Kieli.FI, super.getNimi().get(Kieli.FI)));
            kielet.computeIfAbsent("sv", val -> koodi.getNimi().getTekstit().getOrDefault(Kieli.SV, super.getNimi().get(Kieli.SV)));
            kielet.computeIfAbsent("en", val -> koodi.getNimi().getTekstit().getOrDefault(Kieli.EN, super.getNimi().get(Kieli.EN)));
            return new LokalisoituTekstiDto(kielet);
        } else {
            return super.getNimi();
        }
    }

    public PerusteRakenneOsa getPerusteenOsa() {
        return new PerusteRakenneOsa("tutkinnonosa", getNimi());
    }
}
