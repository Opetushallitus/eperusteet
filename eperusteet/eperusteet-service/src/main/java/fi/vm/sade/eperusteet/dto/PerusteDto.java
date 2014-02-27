/*
 * Copyright Gofore Oy. 
 * http://www.gofore.com/ 
 */
package fi.vm.sade.eperusteet.dto;

import java.io.Serializable;
import java.util.Date;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

/**
 *
 * @author jhyoty
 */
@Getter
@Setter
public class PerusteDto implements Serializable {

    private Long id;
    private LokalisoituTekstiDto nimi;
    private String tutkintokoodi;
    private String koulutusala;
    private List<String> opintoalat;
    private Date paivays;
    private Date siirtyma;
}