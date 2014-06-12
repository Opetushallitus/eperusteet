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
/*global _*/

angular.module('eperusteApp')
  .factory('LukkoSisalto', function(SERVICE_LOC, $resource) {
    return $resource(SERVICE_LOC + '/perusteet/:osanId/suoritustavat/:suoritustapa/lukko', {
      osanId: '@osanId',
      suoritustapa: '@suoritustapa'
    });
  })
  .factory('LukkoPerusteenosa', function(SERVICE_LOC, $resource) {
    return $resource(SERVICE_LOC + '/perusteenosat/:osanId/lukko', {
      osanId: '@osanId'
    });
  })
  .service('Lukitus', function(LUKITSIN_MINIMI, LUKITSIN_MAKSIMI, LukkoPerusteenosa, LukkoSisalto, Notifikaatiot) {
    var lukitsin = null;

    var onevent = _.debounce(function() {
      if (lukitsin) {
        lukitsin();
      }
    }, LUKITSIN_MINIMI, {
      maxWait: LUKITSIN_MAKSIMI
    });
    angular.element(window).on('click', onevent);

    function lukitse(Resource, obj, success) {
      success = success || angular.noop;
      lukitsin = function() {
        Resource.save(obj, angular.noop, Notifikaatiot.serverLukitus);
      };
      Resource.save(obj, success, Notifikaatiot.serverLukitus);
    }

    function vapauta(Resource, obj, success) {
      success = success || angular.noop;
      Resource.remove(obj, success, Notifikaatiot.serverLukitus);
      lukitsin = null;
    }

    function lukitseSisalto(id, suoritustapa, success) {
      lukitse(LukkoSisalto, {
        osanId: id,
        suoritustapa: suoritustapa
      }, success);
    }

    function vapautaSisalto(id, suoritustapa, success) {
      vapauta(LukkoSisalto, {
        osanId: id,
        suoritustapa: suoritustapa
      }, success);
    }

    function lukitsePerusteenosa(id, success) {
      lukitse(LukkoPerusteenosa, {
        osanId: id
      }, success);
    }

    function vapautaPerusteenosa(id, success) {
      vapauta(LukkoPerusteenosa, {
        osanId: id
      }, success);
    }

    return {
      lukitseSisalto: lukitseSisalto,
      vapautaSisalto: vapautaSisalto,
      lukitsePerusteenosa: lukitsePerusteenosa,
      vapautaPerusteenosa: vapautaPerusteenosa
    };
  });
