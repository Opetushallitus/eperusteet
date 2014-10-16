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

import com.google.common.base.Function;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import fi.vm.sade.eperusteet.domain.ReferenceableEntity;
import fi.vm.sade.eperusteet.dto.ReferenceableDto;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import ma.glasnost.orika.MapperFacade;
import ma.glasnost.orika.MappingContext;

/**
 * Kuvaa joukon A-alkioita joukoksi B-alkioita.
 * Jos A:ta vastaava B on jo kohdejoukossa olemassa, mappaa alkion olemassa olevaan alkioon, muussa tapauksessa
 * lisää uuden alkion. Jos joukot tukevat järjestämistä, järjestys säilytetään. Kohdejoukosta poistetaan alkiot joita ei ole lähdejoukossa.
 *
 * @author jhyoty
 */
public class CollectionMergeMapper<A extends ReferenceableDto, B extends ReferenceableEntity>  {

    private final Class<A> typeA;
    private final Class<B> typeB;
    private MapperFacade mapperFacade;

    public CollectionMergeMapper(Class<A> typeA, Class<B> typeB) {
        this.typeA = typeA;
        this.typeB = typeB;
    }

    public void setMapperFacade(MapperFacade mapperFacede) {
        this.mapperFacade = mapperFacede;
    }

    @SuppressWarnings("unchecked")
    public void mapAtoB(Collection<A> a, Collection<B> b, MappingContext context) {

        if (b.isEmpty()) {
            map(a, b, typeB, context);
        } else {
            mergeMap(a, b, context);
        }
    }

    public void mapBtoA(Collection<B> b, Collection<A> a, MappingContext context) {
        a.clear();
        map(b, a, typeA, context);
    }

    private void mergeMap(Collection<A> a, Collection<B> b, MappingContext context) {
        ImmutableMap<Long, B> indx = Maps.uniqueIndex(b, indexFunction);

        List<B> tmp = new ArrayList<>();
        for (A f : a) {
            B item = indx.get(f.getId());
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

    private <S, D> void map(Collection<S> s, Collection<D> d, Class<D> destElemType, MappingContext context) {
        List<D> list = mapperFacade.mapAsList(s, destElemType, context);
        d.addAll(list);
    }

    private final Function<B, Long> indexFunction = new Function<B, Long>() {
        @Override
        public Long apply(B input) {
            return input.getId();
        }

    };
}
