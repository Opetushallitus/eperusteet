package fi.vm.sade.eperusteet.domain.maarays;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
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
    @JoinTable(name = "maarays_kieli_liite",
            joinColumns = @JoinColumn(name = "maarays_kieli_liitteet_id"),
            inverseJoinColumns = @JoinColumn(name = "liitteet_id"))
    private List<MaaraysLiite> liitteet = new ArrayList<>();

    public MaaraysKieliLiitteet copy() {
        MaaraysKieliLiitteet copy = new MaaraysKieliLiitteet();
        copy.getLiitteet().addAll(liitteet.stream().map(MaaraysLiite::copy).collect(Collectors.toList()));

        return copy;
    }
}
