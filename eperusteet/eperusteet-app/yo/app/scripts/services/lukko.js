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
