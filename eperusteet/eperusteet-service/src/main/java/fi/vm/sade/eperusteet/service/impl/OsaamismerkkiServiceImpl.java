package fi.vm.sade.eperusteet.service.impl;

import fi.vm.sade.eperusteet.domain.osaamismerkki.Osaamismerkki;
import fi.vm.sade.eperusteet.dto.osaamismerkki.OsaamismerkkiDto;
import fi.vm.sade.eperusteet.dto.osaamismerkki.OsaamismerkkiQuery;
import fi.vm.sade.eperusteet.dto.util.PageDto;
import fi.vm.sade.eperusteet.repository.OsaamismerkkiRepository;
import fi.vm.sade.eperusteet.repository.OsaamismerkkiRepositoryCustom;
import fi.vm.sade.eperusteet.service.OsaamismerkkiService;
import fi.vm.sade.eperusteet.service.mapping.Dto;
import fi.vm.sade.eperusteet.service.mapping.DtoMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class OsaamismerkkiServiceImpl implements OsaamismerkkiService {

    @Dto
    @Autowired
    private DtoMapper mapper;

    @Autowired
    private OsaamismerkkiRepository osaamismerkkiRepository;

    @Autowired
    private OsaamismerkkiRepositoryCustom osaamismerkkiRepositoryCustom;

    @Override
    public Page<OsaamismerkkiDto> findBy(OsaamismerkkiQuery query) {

        PageRequest pageRequest = new PageRequest(
                query.getSivu(),
                query.getSivukoko(),
                Sort.Direction.DESC,
                "muokattu"
        );

        Page<Osaamismerkki> osaamismerkit = osaamismerkkiRepositoryCustom.findBy(pageRequest, query);

        return new PageDto<>(osaamismerkit, OsaamismerkkiDto.class, pageRequest, mapper);
    }
}
