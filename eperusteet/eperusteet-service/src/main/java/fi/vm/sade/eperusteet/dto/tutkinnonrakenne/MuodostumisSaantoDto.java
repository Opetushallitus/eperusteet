package fi.vm.sade.eperusteet.dto.tutkinnonrakenne;

import fi.vm.sade.eperusteet.domain.LaajuusYksikko;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MuodostumisSaantoDto {

    private Laajuus laajuus;
    private Koko koko;

    public MuodostumisSaantoDto(Laajuus laajuus) {
        this.laajuus = laajuus;
        this.koko = null;
    }

    public MuodostumisSaantoDto(Koko koko) {
        this.koko = koko;
        this.laajuus = null;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Laajuus {

        private Integer minimi;
        private Integer maksimi;
        private LaajuusYksikko yksikko;

    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Koko {

        Integer minimi;
        Integer maksimi;

    }
}
