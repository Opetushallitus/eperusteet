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
  .service('Lukitus', function(LukkoPerusteenosa, LukkoSisalto, Notifikaatiot) {
    function lukitseSisalto(id, suoritustapa, success) {
      success = success || angular.noop;
      LukkoSisalto.save({
        osanId: id,
        suoritustapa: suoritustapa
      },
      success,
      Notifikaatiot.serverLukitus);
    }

    function vapautaSisalto(id, suoritustapa, success) {
      success = success || angular.noop;
      LukkoSisalto.remove({
        osanId: id,
        suoritustapa: suoritustapa
      },
      success,
      Notifikaatiot.serverCb);
    }

    function lukitsePerusteenosa(id, success) {
      success = success || angular.noop;
      LukkoPerusteenosa.save({
        osanId: id
      },
      success,
      Notifikaatiot.serverLukitus);
    }

    function vapautaPerusteenosa(id, success) {
      success = success || angular.noop;
      LukkoPerusteenosa.remove({
        osanId: id
      },
      success,
      Notifikaatiot.serverCb);
    }

    return {
      lukitseSisalto: lukitseSisalto,
      vapautaSisalto: vapautaSisalto,
      lukitsePerusteenosa: lukitsePerusteenosa,
      vapautaPerusteenosa: vapautaPerusteenosa
    };
  });
