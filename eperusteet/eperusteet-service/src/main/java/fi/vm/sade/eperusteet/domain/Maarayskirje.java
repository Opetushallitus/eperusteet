package fi.vm.sade.eperusteet.domain;

import javax.persistence.*;
import java.io.Serializable;
import java.util.*;

import fi.vm.sade.eperusteet.domain.liite.Liite;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.envers.Audited;

import static org.hibernate.envers.RelationTargetAuditMode.NOT_AUDITED;

@Entity
@Table(name = "maarayskirje")
@Audited
public class Maarayskirje implements Serializable {

    @Id
    @Getter
    @Setter
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    @Getter
    @Setter
    @CollectionTable(name = "maarayskirje_url")
    @ElementCollection(fetch = FetchType.EAGER)
    private Map<Kieli, String> url;

    @Getter
    @Setter
    @Audited(targetAuditMode = NOT_AUDITED)
    @OneToMany(cascade = CascadeType.ALL)
    @JoinTable(name = "maarayskirje_liite",
            joinColumns = {
                    @JoinColumn(name = "maarayskirje_id")},
            inverseJoinColumns = {
                    @JoinColumn(name = "liite_id")})
    private Map<Kieli, Liite> liitteet = new HashMap<>();

    public Maarayskirje copy() {
        Maarayskirje copy = new Maarayskirje();
        Map<Kieli, Liite> liitteet = new HashMap<>();
        this.liitteet.keySet().forEach(kieli -> liitteet.put(kieli, new Liite(
                this.liitteet.get(kieli).getTyyppi(),
                this.liitteet.get(kieli).getMime(),
                this.liitteet.get(kieli).getNimi(),
                this.liitteet.get(kieli).getData())));
        copy.setLiitteet(liitteet);

        return copy;
    }
}
