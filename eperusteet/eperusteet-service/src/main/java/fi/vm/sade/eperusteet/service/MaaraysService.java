package fi.vm.sade.eperusteet.service;

import fi.vm.sade.eperusteet.dto.MaaraysDto;
import java.util.List;

public interface MaaraysService {

    List<MaaraysDto> getMaaraykset();

    MaaraysDto addMaarays(MaaraysDto maaraysDto);

    MaaraysDto updateMaarays(MaaraysDto maaraysDto);

    void deleteMaarays(long id);
}
