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
    $scope.$on('$stateChangeSuccess', function() { $scope.peruuta(); });
  })
  .service('Lukitus', function($rootScope, LUKITSIN_MINIMI, LUKITSIN_MAKSIMI,
    LukkoPerusteenosa, LukkoSisalto, Notifikaatiot, $modal, Editointikontrollit, $translate) {
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

    $rootScope.$on('$stateChangeSuccess', function() {
      lukitsin = null;
      etag = null;
    });

    function lueLukitus(Resource, obj, cb, errorCb) {
      Resource.get(obj, cb || angular.noop, errorCb || Notifikaatiot.serverLukitus);
    }

    function lukitse(Resource, obj, cb) {
      cb = cb || angular.noop;
      lukitsin = function(isNew) {
        Resource.save(obj, function(res, headers) {
          if (isNew) {
            etag = headers().etag;
            cb(res);
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
        }, Notifikaatiot.serverLukitus);
      };
      lukitsin(true);
    }

    function vapauta(Resource, obj, cb) {
      cb = cb || angular.noop;
      Resource.remove(obj, cb, Notifikaatiot.serverLukitus);
      lukitsin = null;
    }

    function lukitseSisalto(id, suoritustapa, cb) {
      lukitse(LukkoSisalto, {
        osanId: id,
        suoritustapa: suoritustapa
      }, cb);
    }

    function vapautaSisalto(id, suoritustapa, cb) {
      vapauta(LukkoSisalto, {
        osanId: id,
        suoritustapa: suoritustapa
      }, cb);
    }

    function lukitsePerusteenosa(id, cb) {
      lukitse(LukkoPerusteenosa, {
        osanId: id
      }, cb);
    }

    function vapautaPerusteenosa(id, cb) {
      vapauta(LukkoPerusteenosa, {
        osanId: id
      }, cb);
    }

    function tarkistaLukitus(id, scope, suoritustapa) {
      var okCb = function () {
        scope.isLocked = false;
        scope.lockNotification = '';
      };
      var failCb = function (res) {
        scope.isLocked = true;
        // TODO käyttäjän oikea nimi id:n sijaan
        scope.lockNotification = $translate.instant('lukitus-kayttajalla', { // FIXME
          user: res.data ? res.data.haltijaOid : ''
        });
      };
      if (suoritustapa) {
        lueLukitus(LukkoSisalto, {osanId: id, suoritustapa: suoritustapa}, okCb, failCb);
      } else {
        lueLukitus(LukkoPerusteenosa, {osanId: id}, okCb, failCb);
      }
    }

    return {
      tarkista: tarkistaLukitus,
      lukitseSisalto: lukitseSisalto,
      vapautaSisalto: vapautaSisalto,
      lukitsePerusteenosa: lukitsePerusteenosa,
      vapautaPerusteenosa: vapautaPerusteenosa
    };
  });
