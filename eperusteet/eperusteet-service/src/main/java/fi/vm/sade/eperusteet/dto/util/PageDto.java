package fi.vm.sade.eperusteet.dto.util;

import com.google.common.base.Function;
import com.google.common.collect.Lists;
import fi.vm.sade.eperusteet.service.mapping.DtoMapper;
import java.util.ArrayList;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

public class PageDto<S,D> extends PageImpl<D> {

    public PageDto(final Page<S> source, final Class<D> dstClass, final Pageable page, final DtoMapper mapper) {
        super(new ArrayList<>(Lists.transform(source.getContent(), new Function<S, D>() {
            @Override
            public D apply(S f) {
                D result = mapper.map(f, dstClass);
                return result;
            }
        })), page, source.getTotalElements());
    }
}
