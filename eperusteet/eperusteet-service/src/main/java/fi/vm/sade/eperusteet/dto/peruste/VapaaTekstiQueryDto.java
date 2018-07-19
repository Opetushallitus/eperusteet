package fi.vm.sade.eperusteet.dto.peruste;


import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class VapaaTekstiQueryDto extends PageableQueryDto {
    private String teksti = "";
}
