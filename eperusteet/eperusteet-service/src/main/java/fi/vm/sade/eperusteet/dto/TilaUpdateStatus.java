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
package fi.vm.sade.eperusteet.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import fi.vm.sade.eperusteet.service.util.PerusteenRakenne.Validointi;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

/**
 *
 * @author harrik
 */
public class TilaUpdateStatus {
    @Getter
    @Setter
    List<Status> info;
    @Getter
    @Setter
    boolean vaihtoOk;
    
    public void addStatus(String viesti, Statuskoodi koodi) {

        addStatus(viesti, koodi, null);
    }
    
    public void addStatus(String viesti, Statuskoodi koodi, Validointi validointi) {
        if (info == null) {
            info = new ArrayList<>();
        }
        
        info.add(new Status(viesti, koodi, validointi));
    }
    
    @Getter
    @Setter
    public static class Status {

        String viesti;
        Statuskoodi koodi;
        Validointi validointi;
        
        public Status(String viesti, Statuskoodi koodi, Validointi validointi) {
            this.viesti = viesti;
            this.koodi = koodi;
            this.validointi = validointi;
        }
    }

    public enum Statuskoodi {

        INFO("info"),
        VAROITUS("varoitus"),
        VIRHE("virhe");

        private final String koodi;

        private Statuskoodi(String koodi) {
            this.koodi = koodi;
        }

        @Override
        public String toString() {
            return koodi;
        }

        @JsonCreator
        public static Statuskoodi of(String koodi) {
            for (Statuskoodi s : values()) {
                if (s.koodi.equalsIgnoreCase(koodi)) {
                    return s;
                }
            }
            throw new IllegalArgumentException(koodi + " ei ole kelvollinen statuskoodi");
        }
    }
}
