package fi.vm.sade.eperusteet.domain;

import lombok.Getter;
import lombok.Setter;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Entity
@Table(name = "dokumentti")
@Getter
@Setter
public class Dokumentti implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private long id;

    @NotNull
    @Column(name = "peruste_id")
    private Long perusteId;

    @NotNull
    private String luoja;

    @Column(insertable = true, updatable = false)
    @Enumerated(EnumType.STRING)
    @NotNull
    private Kieli kieli;

    @Temporal(TemporalType.TIMESTAMP)
    @NotNull
    private Date aloitusaika;

    @Temporal(TemporalType.TIMESTAMP)
    private Date valmistumisaika;

    @Enumerated(EnumType.STRING)
    @NotNull
    private DokumenttiTila tila = DokumenttiTila.EI_OLE;

    @Lob
    @Basic(fetch = FetchType.LAZY)
    @Column(name = "dokumenttidata")
    private byte[] data;

    @Lob
    @Basic(fetch = FetchType.LAZY)
    @Column(name = "dokumenttihtml")
    private byte[] html;

    @Enumerated(EnumType.STRING)
    @NotNull
    private DokumenttiVirhe virhekoodi = DokumenttiVirhe.EI_VIRHETTA;

    @Enumerated(EnumType.STRING)
    @NotNull
    private Suoritustapakoodi suoritustapakoodi;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "generator_version")
    private GeneratorVersion generatorVersion = GeneratorVersion.UUSI;

    public List<String> getDataTyypit() {
        List<String> tyypit = new ArrayList<>();
        if (data != null) {
            tyypit.add("PDF");
        }
        if (html != null) {
            tyypit.add("HTML");
        }
        return tyypit;
    }
}
