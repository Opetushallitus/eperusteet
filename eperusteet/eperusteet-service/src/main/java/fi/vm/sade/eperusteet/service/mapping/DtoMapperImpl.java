package fi.vm.sade.eperusteet.service.mapping;

import java.util.Collection;
import java.util.List;
import ma.glasnost.orika.MapperFacade;

public class DtoMapperImpl implements DtoMapper {

    private final MapperFacade mapper;

    public DtoMapperImpl(MapperFacade mapper) {
        this.mapper = mapper;
    }

    @Override
    public <S, D> D map(S sourceObject, Class<D> destinationClass) {
        return mapper.map(sourceObject, destinationClass);
    }

    @Override
    public <S, D> D map(S sourceObject, D destinationObject) {
        mapper.map(sourceObject, destinationObject);
        return destinationObject;
    }

    @Override
    public <S, D> List<D> mapAsList(Iterable<S> source, Class<D> destinationClass) {
        return mapper.mapAsList(source, destinationClass);
    }

    @Override
    public <S, D, T extends Collection<D>> T mapToCollection(Iterable<S> source, T dest, Class<D> elemType) {
        mapper.mapAsCollection(source, dest, elemType);
        return dest;
    }

    @Override
    public <M> M unwrap(Class<M> mapperClass) {
        return mapperClass.cast(mapper);
    }
}
