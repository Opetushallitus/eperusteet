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
  .controller('LukittuSisaltoMuuttunutModalCtrl', function($scope, $modalInstance) {
    $scope.ok = function() { $modalInstance.close(); };
    $scope.peruuta = function() { $modalInstance.dismiss(); };
  })
  .service('Lukitus', function($timeout, LUKITSIN_MINIMI, LUKITSIN_MAKSIMI, LukkoPerusteenosa, LukkoSisalto, Notifikaatiot, $modal, Editointikontrollit) {
    var lukitsin = null;
    var etag = null;

    var onevent = _.debounce(function() {
      if (lukitsin) { lukitsin(); }
    }, LUKITSIN_MINIMI, {
      leading: true,
      trailing: false,
      maxWait: LUKITSIN_MAKSIMI
    });
    angular.element(window).on('click', onevent);

    function lueLukitus(Resource, obj, success) {
      success = success || angular.noop;
      Resource.get(obj, success, Notifikaatiot.serverLukitus);
    }

    function lukitse(Resource, obj, success) {
      success = success || angular.noop;
      lukitsin = function(isNew) {
        Resource.save(obj, function(res, headers) {
          if (isNew) {
            etag = headers().etag;
            success(res);
          }

          if (etag && headers().etag !== etag) {
            $modal.open({
              templateUrl: 'views/modals/sisaltoMuuttunut.html',
              controller: 'LukittuSisaltoMuuttunutModalCtrl'
            })
            .result.then(function() {
              etag = headers().etag;
            },
            function() {
              Editointikontrollit.cancelEditing();
            });
          }
        },
        Notifikaatiot.serverLukitus);
      };
      lukitsin(true);
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
      lueLukitus: lueLukitus,
      lukitseSisalto: lukitseSisalto,
      vapautaSisalto: vapautaSisalto,
      lukitsePerusteenosa: lukitsePerusteenosa,
      vapautaPerusteenosa: vapautaPerusteenosa
    };
  });
