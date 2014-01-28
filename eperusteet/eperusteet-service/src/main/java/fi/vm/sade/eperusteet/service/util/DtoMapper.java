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

package fi.vm.sade.eperusteet.service.util;

/**
 *
 * @author jhyoty
 */
public interface DtoMapper {

       /**
     * Create and return a new instance of type D mapped with the properties of
     * <code>sourceObject</code>.
     *
     * @param sourceObject
     *            the object to map from
     * @param destinationClass
     *            the type of the new object to return
     * @return a new instance of type D mapped with the properties of
     *         <code>sourceObject</code>
     */
    <S, D> D map(S sourceObject, Class<D> destinationClass);

    /**
     * Maps the properties of <code>sourceObject</code> onto
     * <code>destinationObject</code>.
     *
     * @param sourceObject
     *            the object from which to read the properties
     * @param destinationObject
     *            the object onto which the properties should be mapped
     */
    <S, D> void map(S sourceObject, D destinationObject);

    /**
     * Return the underlying mapper implementation
     * @param <M>
     * @param mapperClass
     * @return mapper implementation.
     */
    <M> M unwrap(Class<M> mapperClass);

}
