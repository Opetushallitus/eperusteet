package fi.vm.sade.eperusteet.dto.fakes;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Referer {
    private Referable ref;
    private Referable javaOptional;
}