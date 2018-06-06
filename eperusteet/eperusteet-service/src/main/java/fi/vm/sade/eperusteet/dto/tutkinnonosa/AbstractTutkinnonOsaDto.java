package fi.vm.sade.eperusteet.dto.tutkinnonosa;

import fi.vm.sade.eperusteet.domain.PerusteTila;
import fi.vm.sade.eperusteet.domain.PerusteenOsaTunniste;
import fi.vm.sade.eperusteet.domain.tutkinnonosa.TutkinnonOsaTyyppi;
import fi.vm.sade.eperusteet.dto.KevytTekstiKappaleDto;
import fi.vm.sade.eperusteet.dto.peruste.PerusteenOsaDto;
import fi.vm.sade.eperusteet.dto.tutkinnonrakenne.KoodiDto;
import fi.vm.sade.eperusteet.dto.util.LokalisoituTekstiDto;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.List;

@Getter
@Setter
@SuperBuilder
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public abstract class AbstractTutkinnonOsaDto extends PerusteenOsaDto.Laaja {
    private LokalisoituTekstiDto tavoitteet;
    private LokalisoituTekstiDto ammattitaidonOsoittamistavat;
    private List<KevytTekstiKappaleDto> vapaatTekstit;
    private TutkinnonOsaTyyppi tyyppi;
    private LokalisoituTekstiDto kuvaus;
    private KoodiDto koodi;

    public AbstractTutkinnonOsaDto() {
    }
    public AbstractTutkinnonOsaDto (LokalisoituTekstiDto nimi, PerusteTila tila, PerusteenOsaTunniste tunniste) {
        super(nimi, tila, tunniste);
    }
}
