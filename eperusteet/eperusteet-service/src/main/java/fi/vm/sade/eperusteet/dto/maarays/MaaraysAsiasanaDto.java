package fi.vm.sade.eperusteet.dto.maarays;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MaaraysAsiasanaDto {
    private Long id;
    private List<String> asiasana;
}
