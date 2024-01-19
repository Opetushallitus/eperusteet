package fi.vm.sade.eperusteet.domain.maarays;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinTable;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Entity
@Setter
@Getter
@Table(name = "maarays_kieli_liitteet")
@AllArgsConstructor
@NoArgsConstructor
public class MaaraysKieliLiitteet {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    @OneToMany
    @JoinTable(name = "maarays_kieli_liite")
    private List<MaaraysLiite> liitteet = new ArrayList<>();

    public MaaraysKieliLiitteet copy() {
        MaaraysKieliLiitteet copy = new MaaraysKieliLiitteet();
        copy.getLiitteet().addAll(liitteet.stream().map(MaaraysLiite::copy).collect(Collectors.toList()));

        return copy;
    }
}
