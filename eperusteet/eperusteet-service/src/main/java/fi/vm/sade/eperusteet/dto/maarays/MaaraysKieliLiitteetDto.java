package fi.vm.sade.eperusteet.dto.maarays;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MaaraysKieliLiitteetDto {
    private Long id;
    private List<MaaraysLiiteDto> liitteet = new ArrayList<>();
}
