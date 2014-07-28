/*
 * Copyright (c) 2013 The Finnish Board of Education - Opetushallitus
 *
 * This program is free software: Licensed under the EUPL, Version 1.1 or - as
 * soon as they will be approved by the European Commission - subsequent versions
 * of the EUPL (the "Licence");
 *
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at: http://ec.europa.eu/idabc/eupl
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * European Union Public Licence for more details.
 */
package fi.vm.sade.eperusteet.dto.tutkinnonrakenne;

import fi.vm.sade.eperusteet.domain.LaajuusYksikko;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MuodostumisSaantoDto {

    private Laajuus laajuus;
    private Koko koko;

    public MuodostumisSaantoDto() {
    }

    public MuodostumisSaantoDto(Laajuus laajuus) {
        this.laajuus = laajuus;
        this.koko = null;
    }

    public MuodostumisSaantoDto(Koko koko) {
        this.koko = koko;
        this.laajuus = null;
    }

    @Getter
    @Setter
    public static class Laajuus {

        private Integer minimi;
        private Integer maksimi;
        private LaajuusYksikko yksikko;

        public Laajuus(Integer minimi, Integer maksimi, LaajuusYksikko yksikko) {
            this.minimi = minimi;
            this.maksimi = maksimi;
            this.yksikko = yksikko;
        }

        public Laajuus() {
        }

    }

    @Getter
    @Setter
    public static class Koko {

        Integer minimi;
        Integer maksimi;

        public Koko(Integer minimi, Integer maksimi) {
            this.minimi = minimi;
            this.maksimi = maksimi;
        }

        public Koko() {

        }
    }
}
