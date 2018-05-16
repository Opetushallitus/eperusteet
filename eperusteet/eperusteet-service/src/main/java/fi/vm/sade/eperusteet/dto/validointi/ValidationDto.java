package fi.vm.sade.eperusteet.dto.validointi;

import fi.vm.sade.eperusteet.dto.perusteprojekti.PerusteprojektiListausDto;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Getter
@Setter
public class ValidationDto {
    private PerusteprojektiListausDto perusteprojekti;
    private Date lastCheck;
    private boolean vaihtoOk = false;
    private List<ValidointiStatusInfoDto> infot = new ArrayList<>();
}
