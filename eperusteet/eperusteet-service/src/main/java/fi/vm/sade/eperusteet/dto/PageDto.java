/*
 * Copyright (c) 2013 The Finnish Board of Education - Opetushallitus
 * 
 * This program is free software: Licensed under the EUPL, Version 1.1 or - as
 * soon as they will be approved by the European Commission - subsequent versions
 * of the EUPL (the "Licence");
 * 
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at: http://ec.europa.eu/idabc/eupl
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * European Union Public Licence for more details.
 */

package fi.vm.sade.eperusteet.dto;

import com.google.common.base.Function;
import com.google.common.collect.Lists;
import fi.vm.sade.eperusteet.service.mapping.DtoMapper;
import java.util.ArrayList;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

/**
 *
 * @author jhyoty
 */
public class PageDto<S,D> extends PageImpl<D> {

    public PageDto(final Page<S> source, final Class<D> dstClass, final Pageable page, final DtoMapper mapper) {
        super(new ArrayList<>(Lists.transform(source.getContent(), new Function<S, D>() {
            @Override
            public D apply(S f) {
                return mapper.map(f, dstClass);
            }
        })), page, source.getTotalElements());
    }
}
