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
package fi.vm.sade.eperusteet.service.mapping;

import fi.vm.sade.eperusteet.domain.ReferenceableEntity;
import fi.vm.sade.eperusteet.dto.ReferenceableDto;
import ma.glasnost.orika.CustomMapper;
import ma.glasnost.orika.MappingContext;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Kuvaa joukon A-alkioita joukoksi B-alkioita. Jos A:ta vastaava B on jo kohdejoukossa olemassa,
 * mappaa alkion olemassa olevaan alkioon, muussa tapauksessa lisää uuden alkion.
 * Jos joukot tukevat järjestämistä, järjestys säilytetään.
 * Kohdejoukosta poistetaan alkiot joita ei ole lähdejoukossa.
 *
 * @author jhyoty
 */
public class ReferenceableCollectionMergeMapper
        extends CustomMapper<Collection<ReferenceableDto>,Collection<ReferenceableEntity>> {

    @Override
    public void mapBtoA(Collection<ReferenceableEntity> b, Collection<ReferenceableDto> a, MappingContext context) {
        a.clear();
        Class<? extends ReferenceableDto> typeA = context.getResolvedDestinationType()
                .getComponentType().getRawType().asSubclass(ReferenceableDto.class);
        map(b, a, typeA, context);
    }

    @Override
    public void mapAtoB(Collection<ReferenceableDto> a, Collection<ReferenceableEntity> b, MappingContext context) {
        if (b.isEmpty()) {
            Class<? extends ReferenceableEntity> typeB = context.getResolvedDestinationType()
                    .getComponentType().getRawType().asSubclass(ReferenceableEntity.class);
            map(a, b, typeB, context);
        } else {
            mergeMap(a, b, context);
        }
    }

    private void mergeMap(Collection<ReferenceableDto> a, Collection<ReferenceableEntity> b, MappingContext context) {
        Map<Serializable, ReferenceableEntity> indx = b.stream()
                .collect(Collectors.toMap(ReferenceableEntity::getId, r -> r));
        Class<? extends ReferenceableEntity> typeB = context.getResolvedDestinationType()
                .getComponentType().getRawType().asSubclass(ReferenceableEntity.class);

        List<ReferenceableEntity> tmp = new ArrayList<>();
        for (ReferenceableDto f : a) {
            ReferenceableEntity item = indx.get(f.getId());
            if (item != null) {
                mapperFacade.map(f, item, context);
            } else {
                item = mapperFacade.map(f, typeB, context);
            }
            tmp.add(item);
        }
        b.clear();
        b.addAll(tmp);
    }

    private <S, D> void map(Collection<S> s, Collection<D> d, Class<? extends D> destElemType, MappingContext context) {
        List<? extends D> list = mapperFacade.mapAsList(s, destElemType, context);
        d.addAll(list);
    }

}
