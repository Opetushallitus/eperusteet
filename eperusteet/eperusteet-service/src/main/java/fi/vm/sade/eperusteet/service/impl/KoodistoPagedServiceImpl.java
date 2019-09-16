package fi.vm.sade.eperusteet.service.impl;

import fi.vm.sade.eperusteet.dto.koodisto.KoodistoKoodiDto;
import fi.vm.sade.eperusteet.dto.koodisto.KoodistoPageDto;
import fi.vm.sade.eperusteet.service.KoodistoClient;
import fi.vm.sade.eperusteet.service.KoodistoPagedService;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.support.MutableSortDefinition;
import org.springframework.beans.support.PagedListHolder;
import org.springframework.context.annotation.Profile;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

@Service
public class KoodistoPagedServiceImpl implements KoodistoPagedService {

    @Autowired
    private KoodistoClient koodistoClient;

    @Override
    public Page<KoodistoKoodiDto> getAllPaged(String koodisto, String nimiFilter, KoodistoPageDto koodistoPageDto) {

        List<KoodistoKoodiDto> koodistoList = koodistoClient.getAll(koodisto).stream()
                .filter(koodistoDto ->  StringUtils.isEmpty(nimiFilter) ||
                            (koodistoDto.getMetadataName(koodistoPageDto.getKieli()) != null
                                    && koodistoDto.getMetadataName(koodistoPageDto.getKieli()).getNimi().toLowerCase().contains(nimiFilter.toLowerCase()))
                )
                .collect(Collectors.toList());

        koodistoList.sort(Comparator.comparing((KoodistoKoodiDto o) -> o.getMetadataName(koodistoPageDto.getKieli()) != null ?
                o.getMetadataName(koodistoPageDto.getKieli()).getNimi().toLowerCase() : ""));

        int startItem = koodistoPageDto.getSivu() * koodistoPageDto.getSivukoko();
        List<KoodistoKoodiDto> list;

        if (koodistoList.size() < startItem) {
            list = Collections.emptyList();
        } else {
            int toIndex = Math.min(startItem + koodistoPageDto.getSivukoko(), koodistoList.size());
            list = koodistoList.subList(startItem, toIndex);
        }

        return new PageImpl<KoodistoKoodiDto>(list, new PageRequest(koodistoPageDto.getSivu(), koodistoPageDto.getSivukoko()), koodistoList.size());
    }
}
