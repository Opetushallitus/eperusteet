'use strict';
// /*global _*/

angular.module('eperusteApp')
  .factory('LukkoPerusteenosa', function(SERVICE_LOC, $resource) {
    return $resource(SERVICE_LOC + '/perusteenosat/:osanId/lukko', {
      osanId: '@osanId'
    });
  })
  .service('Lukitus', function(LukkoPerusteenosa, Notifikaatiot) {
    function lukitse(id, success) {
      LukkoPerusteenosa.save({
        osanId: id
      },
      success,
      Notifikaatiot.serverLukitus);
    }

    function vapauta(id) {
      LukkoPerusteenosa.remove({
        osanId: id
      },
      angular.noop,
      Notifikaatiot.serverCb);
    }

    return {
      lukitse: lukitse,
      vapauta: vapauta
    };
  });
