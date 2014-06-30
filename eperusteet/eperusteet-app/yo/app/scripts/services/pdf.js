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

'use strict';
// /*global _*/

angular.module('eperusteApp')
  .factory('Dokumentti', function($resource, SERVICE_LOC) {

      // api:
      //
      // Generointi:
      // /dokumentti/create/:id/:kieli
      // /dokumentti/create/:id // oletuskieli
      //
      // Luonnin seuranta/tilakysely:
      // /dokumentti/query/:token
      //
      // Valmiin dokumentin hakeminen:
      // /dokumentti/get/:token
      //
      // Yksivaiheinen luominen
      // /dokumentti/:id/:kieli
      // /dokumentti/:id // oletuskieli

    return $resource(SERVICE_LOC + '/dokumentti/create/:id/:kieli', {
      id: '@id',
      kieli: '@kieli'
    });
  })
  .factory('DokumenttiHaku', function($resource, SERVICE_LOC) {
    return $resource(SERVICE_LOC + '/dokumentti/:tapa/:token', {
      tapa: '@tapa', // query | get
      token: '@token'
    },
    {
        hae: {method: 'GET', responseType: 'arraybuffer'}
    });
  })
  .service('Pdf', function(Dokumentti, DokumenttiHaku, SERVICE_LOC) {

    function generoiPdf(perusteId, success, failure) {
      success = success || angular.noop;
      failure = failure || angular.noop;

      Dokumentti.get({
        id: perusteId,
        kieli: 'fi'
      }, success);
    }

    function haeTila(tokenId, success, failure) {
      success = success || angular.noop;
      failure = failure || angular.noop;

      DokumenttiHaku.get({
        tapa: 'query',
        token: tokenId
      }, success);
    }

    function haeDokumentti(tokenId, success, failure) {
        success = success || angular.noop;
        failure = failure || angular.noop;

        return DokumenttiHaku.hae({
            tapa: 'get',
            token: tokenId
        }, success);
    }

    function haeLinkki(tokenId) {
        // dis like, ewwww
        return SERVICE_LOC + '/dokumentti/get/'+tokenId;
    }

    return {
      generoiPdf: generoiPdf,
      haeDokumentti: haeDokumentti,
      haeTila: haeTila,
      haeLinkki: haeLinkki
    };
  });
