/*
 * Copyright Gofore Oy.
 * http://www.gofore.com/
 */
package fi.vm.sade.eperusteet.domain;

import java.io.Serializable;
import java.util.Date;
import java.util.List;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

/**
 *
 * @author jhyoty
 */
@Entity
@Table(name = "peruste")
public class Peruste implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    private TekstiPalanen nimi;
    private String tutkintokoodi;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "koulutusala_id")
    private Koulutusala koulutusala;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "peruste_opintoala",
            joinColumns = @JoinColumn(name = "peruste_id"),
            inverseJoinColumns = @JoinColumn(name = "opintoala_id"))
    private List<Opintoala> opintoalat;

    @Temporal(TemporalType.TIMESTAMP)
    private Date paivays;

    @Temporal(TemporalType.TIMESTAMP)
    private Date siirtyma;

    @OneToOne(fetch = FetchType.LAZY)
    private PerusteenOsaViite rakenne;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public TekstiPalanen getNimi() {
        return nimi;
    }

    public void setNimi(TekstiPalanen nimi) {
        this.nimi = nimi;
    }

    public String getTutkintokoodi() {
        return tutkintokoodi;
    }

    public void setTutkintokoodi(String tutkintokoodi) {
        this.tutkintokoodi = tutkintokoodi;
    }

    public Koulutusala getKoulutusala() {
        return koulutusala;
    }

    public void setKoulutusala(Koulutusala koulutusala) {
        this.koulutusala = koulutusala;
    }

    public List<Opintoala> getOpintoalat() {
        return opintoalat;
    }

    public void setOpintoalat(List<Opintoala> opintoalat) {
        this.opintoalat = opintoalat;
    }

    public Date getPaivays() {
        return paivays;
    }

    public void setPaivays(Date paivays) {
        this.paivays = paivays;
    }

    public Date getSiirtyma() {
        return siirtyma;
    }

    public void setSiirtyma(Date siirtyma) {
        this.siirtyma = siirtyma;
    }

    public PerusteenOsaViite getRakenne() {
        return rakenne;
    }

    public void setRakenne(PerusteenOsaViite juuriViite) {
        this.rakenne = juuriViite;
    }

}
