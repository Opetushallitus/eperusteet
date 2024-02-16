package fi.vm.sade.eperusteet.service.util;

import fi.vm.sade.eperusteet.dto.util.LokalisoituTekstiDto;
import fi.vm.sade.eperusteet.dto.yl.OppiaineDto;
import fi.vm.sade.eperusteet.service.yl.OppiaineOpetuksenSisaltoTyyppi;
import fi.vm.sade.eperusteet.service.yl.OppiaineService;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class OppiaineUtil {
    private OppiaineUtil() {
    }

    @Getter
    @Setter
    public static class Reference<T> {
        private T id;
    }

    @Getter
    public static class OppiaineLuontiPuu {
        private final LokalisoituTekstiDto teksti;
        private final List<OppiaineLuontiPuu> lapset = new ArrayList<>();
        private Long id;
        private OppiaineLuontiPuu vanhempi;
        private boolean koosteinen = false;
        private Reference<Long> idRef;

        public OppiaineLuontiPuu(LokalisoituTekstiDto teksti) {
            this.teksti = teksti;
        }

        public OppiaineLuontiPuu maara(OppiaineLuontiPuu lapsi) {
            lapsi.vanhempi = this.koosteinen();
            lapset.add(lapsi);
            return this;
        }

        public OppiaineLuontiPuu koosteinen() {
            this.koosteinen = true;
            return this;
        }

        public OppiaineLuontiPuu as(Reference<Long> idRef) {
            this.idRef = idRef;
            return this;
        }

        public OppiaineDto luo(OppiaineService service, Long perusteId, OppiaineOpetuksenSisaltoTyyppi tyyppi) {
            OppiaineDto luotava = oppaine(teksti, vanhempi == null ? null : vanhempi.id);
            luotava.setKoosteinen(Optional.of(this.koosteinen));
            OppiaineDto luotu = service.addOppiaine(perusteId, luotava, tyyppi);
            this.id = luotu.getId();
            if (this.idRef != null) {
                this.idRef.setId(luotu.getId());
            }
            this.lapset.forEach(lapsi -> lapsi.luo(service, perusteId, tyyppi));
            return luotu;
        }
    }

    public static OppiaineLuontiPuu oppiaine(LokalisoituTekstiDto nimi) {
        return new OppiaineLuontiPuu(nimi);
    }

    public static OppiaineDto oppaine(LokalisoituTekstiDto nimi, Long parentId) {
        OppiaineDto dto = new OppiaineDto();
        dto.setNimi(Optional.of(nimi));
        dto.setOppiaine(parentId == null ? Optional.empty() : Optional.of(new fi.vm.sade.eperusteet.dto.Reference(parentId.toString())));

        return dto;
    }



}
