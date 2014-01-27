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
import fi.vm.sade.eperusteet.domain.Kieli;
import java.util.List;

/**
 *
 * @author nkala
 */
public class PerusteQuery {
    private int sivu = 0;
    private int sivukoko= 25;
    private boolean siirtyma = false;
    private String nimi;
    private List<String> ala;
    private List<String> tyyppi;
    private String kieli = "fi";
    private List<String> opintoala;

    public int getSivu() {
        return sivu;
    }

    public int getSivukoko() {
        return sivukoko;
    }

    public boolean getSiirtyma() {
        return siirtyma;
    }

    public String getNimi() {
        return nimi;
    }

    public List<String> getKoulutusalat() {
        return ala;
    }

    public List<String> getTyyppi() {
        return tyyppi;
    }

    public String getKieli() {
        return kieli;
    }

    public List<String> getOpintoalat() {
        return opintoala;
    }

    public void setSivu(int sivu) {
        this.sivu = sivu;
    }

    public void setSivukoko(int sivukoko) {
        this.sivukoko = sivukoko;
    }

    public void setSiirtyma(boolean siirtyma) {
        this.siirtyma = siirtyma;
    }

    public void setNimi(String nimi) {
        this.nimi = nimi;
    }

    public void setKoulutusala(List<String> ala) {
        this.ala = ala;
    }

    public void setTyyppi(List<String> tyyppi) {
        this.tyyppi = tyyppi;
    }

    public void setKieli(String kieli) {
        this.kieli = kieli;
    }

    public void setOpintoala(List<String> opintoala) {
        this.opintoala = opintoala;
    }

}