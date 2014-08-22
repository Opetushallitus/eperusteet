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

import fi.vm.sade.eperusteet.dto.util.LokalisoituTekstiDto;
import fi.vm.sade.eperusteet.domain.Suoritustapakoodi;
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
    List<Status> infot;
    @Getter
    @Setter
    boolean vaihtoOk;
    
    public void addStatus(String viesti) {
        addStatus(viesti, null, null, null);
    }
    
    public void addStatus(String viesti, Suoritustapakoodi suoritustapa) {
        addStatus(viesti, suoritustapa, null, null);
    }
    
    public void addStatus(String viesti, Suoritustapakoodi suoritustapa, List<LokalisoituTekstiDto> nimet) {
        addStatus(viesti, suoritustapa, null, nimet);
    }
    
    public void addStatus(String viesti, Suoritustapakoodi suoritustapa, Validointi validointi) {
        addStatus(viesti, suoritustapa, validointi, null);
    }
    
    public void addStatus(String viesti, Suoritustapakoodi suoritustapa, Validointi validointi, List<LokalisoituTekstiDto> nimet) {
        if (infot == null) {
            infot = new ArrayList<>();
        }  
        infot.add(new Status(viesti, suoritustapa, validointi, nimet));
    }
    
    @Getter
    @Setter
    public static class Status {

        String viesti;
        Validointi validointi;
        List<LokalisoituTekstiDto> nimet;
        Suoritustapakoodi suoritustapa;
        
        public Status(String viesti, Suoritustapakoodi suoritustapa, Validointi validointi, List<LokalisoituTekstiDto> nimet) {
            this.viesti = viesti;
            this.validointi = validointi;
            this.nimet = nimet;
            this.suoritustapa = suoritustapa;
        }
    }

}
