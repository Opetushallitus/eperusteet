package fi.vm.sade.eperusteet.dto.maarays;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MaaraysKieliLiitteetDto {
    private Long id;
    private List<MaaraysLiiteDto> liitteet = new ArrayList<>();
}
