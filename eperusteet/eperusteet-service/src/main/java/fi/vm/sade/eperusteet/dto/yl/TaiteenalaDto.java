package fi.vm.sade.eperusteet.dto.yl;

import com.fasterxml.jackson.annotation.JsonTypeName;
import fi.vm.sade.eperusteet.domain.KevytTekstiKappale;
import fi.vm.sade.eperusteet.domain.Koodi;
import fi.vm.sade.eperusteet.domain.PerusteTila;
import fi.vm.sade.eperusteet.domain.PerusteenOsaTunniste;
import fi.vm.sade.eperusteet.dto.peruste.PerusteenOsaDto;
import fi.vm.sade.eperusteet.dto.util.LokalisoituTekstiDto;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonTypeName("taiteenala")
public class TaiteenalaDto extends PerusteenOsaDto.Laaja {
    public TaiteenalaDto() {
    }

    public TaiteenalaDto(LokalisoituTekstiDto nimi, PerusteTila tila, PerusteenOsaTunniste tunniste) {
        super(nimi, tila, tunniste);
    }

    @Getter
    @Setter
    private Koodi koodi;

    @Getter
    @Setter
    private KevytTekstiKappale kasvatus;

    @Getter
    @Setter
    private KevytTekstiKappale yhteisetOpinnot;

    @Getter
    @Setter
    private KevytTekstiKappale teemaopinnot;

    @Getter
    @Setter
    private KevytTekstiKappale aikuistenOpetus;

    @Getter
    @Setter
    private KevytTekstiKappale tyotavatOpetuksessa;

    @Getter
    @Setter
    private KevytTekstiKappale oppimisenArviointiOpetuksessa;

    public String getOsanTyyppi() {
        return "taiteenala";
    }
}
