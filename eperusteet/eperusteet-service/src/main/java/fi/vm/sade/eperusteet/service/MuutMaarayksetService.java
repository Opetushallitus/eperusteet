package fi.vm.sade.eperusteet.service;

import fi.vm.sade.eperusteet.dto.MuuMaaraysDto;
import java.util.List;

public interface MuutMaarayksetService {

    List<MuuMaaraysDto> getMaaraykset();

    MuuMaaraysDto addMaarays(MuuMaaraysDto muuMaaraysDto);

    MuuMaaraysDto updateMaarays(MuuMaaraysDto muuMaaraysDto);

    void deleteMaarays(long id);
}
