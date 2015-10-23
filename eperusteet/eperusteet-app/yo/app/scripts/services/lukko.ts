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

/// <reference path="../../ts_packages/tsd.d.ts" />

angular.module('eperusteApp')
  .factory('LukkoRakenne', function(SERVICE_LOC, $resource) {
    return $resource(SERVICE_LOC + '/perusteet/:osanId/suoritustavat/:suoritustapa/rakenne/lukko/', {
      osanId: '@osanId',
      suoritustapa: '@suoritustapa',
      tyyppi: '@tyyppi'
    }, {
      multiple: { method: 'GET', url: SERVICE_LOC + '/perusteet/:osanId/suoritustavat/:suoritustapa/lukko/:tyyppi'}
    });
  })
  .factory('LukkoPerusteenosa', function(SERVICE_LOC, $resource) {
    return $resource(SERVICE_LOC + '/perusteenosat/:osanId/lukko', {
      osanId: '@osanId'
    });
  })
  .factory('LukkoOppiaine', function (SERVICE_LOC, $resource) {
    return $resource(SERVICE_LOC + '/perusteet/:perusteId/perusopetus/oppiaineet/:osanId/lukko', {
      osanId: '@osanId',
      perusteId: '@perusteId'
    });
  })
  .factory('LukkoLaajaalainenOsaaminen', function (SERVICE_LOC, $resource) {
    return $resource(SERVICE_LOC + '/perusteet/:perusteId/perusopetus/laajaalaisetosaamiset/:osanId/lukko', {
      osanId: '@osanId',
      perusteId: '@perusteId'
    });
  })
  .factory('LukkoVuosiluokkakokonaisuus', function (SERVICE_LOC, $resource) {
    return $resource(SERVICE_LOC + '/perusteet/:perusteId/perusopetus/vuosiluokkakokonaisuudet/:osanId/lukko', {
      osanId: '@osanId',
      perusteId: '@perusteId'
    });
  })
  .factory('LukkoOppiaineenVuosiluokkakokonaisuus', function (SERVICE_LOC, $resource) {
    return $resource(SERVICE_LOC + '/perusteet/:perusteId/perusopetus/oppiaineet/:oppiaineId/vuosiluokkakokonaisuudet/:vuosiluokkaId/lukko', {
      oppiaineId: '@oppiaineId',
      perusteId: '@perusteId',
      vuosiluokkaId: '@vuosiluokkaId'
    });
  })
  .factory('LukkoPerusteenosaByTutkinnonOsaViite', function(SERVICE_LOC, $resource) {
    return $resource(SERVICE_LOC + '/perusteenosat/tutkinnonosaviite/:viiteId/lukko', {
      viiteId: '@viiteId'
    });
  })
  .controller('LukittuSisaltoMuuttunutModalCtrl', function($scope, $modalInstance) {
    $scope.ok = function() { $modalInstance.close(); };
    $scope.peruuta = function() { $modalInstance.dismiss(); };
    $scope.$on('$stateChangeSuccess', function() { $scope.peruuta(); });
  })
  .service('Lukitus', function($rootScope, $state, $stateParams, LUKITSIN_MINIMI, LUKITSIN_MAKSIMI, $timeout,
    Profiili, LukkoPerusteenosa, LukkoRakenne, Notifikaatiot, $modal, Editointikontrollit, Kaanna, $q,
    LukkoOppiaine, PerusopetusService, LukkoOppiaineenVuosiluokkakokonaisuus, LukkoPerusteenosaByTutkinnonOsaViite, LukkoVuosiluokkakokonaisuus, LukkoLaajaalainenOsaaminen) {

    var lukitsin = null;
    var etag = null;

    $rootScope.$on('$stateChangeSuccess', function() {
      lukitsin = null;
      etag = null;
    });

    function lueLukitus(Resource, obj, cb = _.noop, errorCb = Notifikaatiot.serverLukitus) {
      Resource.get(obj, cb, errorCb);
    }

    function tilaToLukkoResource() {
      // TODO: Lisää muille tiloille vastaavat
      if ($state.current.name === 'root.perusteprojekti.suoritustapa.osaalue') {
        switch($stateParams.osanTyyppi) {
          // case '': return LukkoPerusteenosa;
          // case '': return LukkoRakenne;
          case 'oppiaineet': return LukkoOppiaine;
          // case '': return LukkoOppiaineenVuosiluokkakokonaisuus;
          // case '': return LukkoPerusteenosaByTutkinnonOsaViite;
          case 'vuosiluokat': return LukkoVuosiluokkakokonaisuus;
          case 'osaaminen': return LukkoLaajaalainenOsaaminen;
          default: return null;
        }
      }
    }

    function tilaToLukkoParams() {
      // TODO: Lisää muille tiloille vastaavat
      if ($state.current.name === 'root.perusteprojekti.suoritustapa.osaalue' && $stateParams.osanTyyppi && $stateParams.osanId !== 'uusi') {
        return { perusteId: PerusopetusService.getPerusteId(), osanId: $stateParams.osanId };
      }
    }

    function genericVapauta(cb) {
      cb = cb || angular.noop;
      var lukkotyyppi = tilaToLukkoResource();
      if (lukkotyyppi) {
        vapauta(lukkotyyppi, tilaToLukkoParams(), cb);
      }
      else {
        console.log('Tilalle "' + $state.current.name + '" ei ole määritetty lukkotyyppiä');
      }
    }

    function genericLukitse(cb) {
      cb = cb || angular.noop;
      var lukkotyyppi = tilaToLukkoResource();
      if (lukkotyyppi) {
        var params = tilaToLukkoParams();
        if (params) {
          lukitse(lukkotyyppi, params, cb);
        }
        else { // Lukkoa ei tarvita koska kyseessä uusi
          cb();
        }
      }
      else {
        console.log('Tilalle "' + $state.current.name + '" ei ole määritetty lukkotyyppiä');
      }
    }

    function genericTarkistaLukitus(success, error) {
      success = success || angular.noop;
      error = error || angular.noop;

      function tarkistaLukitusCb(res) {
        if (res.status !== 404 && res.haltijaOid && new Date() <= new Date(res.vanhentuu) && !res.oma) {
          error(Kaanna.kaanna('lukitus-kayttajalla', {
            user: res.haltijaNimi || res.haltijaOid
          }));
        }
        else {
          success();
        }
      }

      var resource = tilaToLukkoResource();
      if (resource) {
        var params = tilaToLukkoParams();
        resource.get(params, tarkistaLukitusCb, tarkistaLukitusCb);
      }
    }

    function lukitse(Resource, obj, cb) {
      cb = cb || angular.noop;

      lukitsin = function() {
        Resource.save(obj, function(res, headers) {
          if (etag && headers().etag !== etag && Editointikontrollit.getEditMode()) {
            $modal.open({
              templateUrl: 'views/modals/sisaltoMuuttunut.html',
              controller: 'LukittuSisaltoMuuttunutModalCtrl'
            })
            .result.then(function() {
              etag = headers().etag;
            }, Editointikontrollit.cancelEditing);
          }
          else {
            etag = headers().etag;
            cb(res);
          }
        }, Notifikaatiot.serverLukitus);
      };
      lukitsin();
    }

    function vapauta(Resource, obj, cb) {
      cb = cb || angular.noop;
      Resource.remove(obj, cb, Notifikaatiot.serverLukitus);
      lukitsin = null;
      etag = null;
    }

    function lukitseSisalto(id, suoritustapa, cb) {
      lukitse(LukkoRakenne, {
        osanId: id,
        suoritustapa: suoritustapa
      }, cb);
    }

    function vapautaSisalto(id, suoritustapa, cb = _.noop) {
      var deferred = $q.defer();
      vapauta(LukkoRakenne, {
        osanId: id,
        suoritustapa: suoritustapa
      }, () => {
        deferred.resolve();
        cb();
      });
      return deferred.promise;
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

    function lukitsePerusteenosaByTutkinnonOsaViite(id, cb) {
      lukitse(LukkoPerusteenosaByTutkinnonOsaViite, {
        viiteId: id
      }, cb);
    }

    function vapautaPerusteenosaByTutkinnonOsaViite(id, cb) {
      vapauta(LukkoPerusteenosaByTutkinnonOsaViite, {
        viiteId: id
      }, cb);
    }

    function tarkistaLukitus(id, scope, suoritustapa) {
      var okCb = function(res) {
        if (res.haltijaOid && new Date() <= new Date(res.vanhentuu) && !res.oma) {
          scope.isLocked = true;
          scope.lockNotification = Kaanna.kaanna('lukitus-kayttajalla', {
            user: res.haltijaNimi || res.haltijaOid
          });
        }
        else {
          scope.isLocked = false;
          scope.lockNotification = '';
        }
      };

      if (suoritustapa) {
        lueLukitus(LukkoRakenne, {osanId: id, suoritustapa: suoritustapa}, okCb);
      } else {
        lueLukitus(LukkoPerusteenosa, {osanId: id}, okCb);
      }
    }

    function hae(parametrit, cb) {
      parametrit = _.merge({
        id: 0,
        tyyppi: 'sisalto',
        suoritustapa: 'naytto'
      }, parametrit);

      switch (parametrit.tyyppi) {
        case 'sisalto':
          lueLukitus(LukkoRakenne, { osanId: parametrit.id, suoritustapa: parametrit.suoritustapa }, cb);
          break;
        case 'perusteenosa':
          lueLukitus(LukkoPerusteenosa, { osanId: parametrit.id }, cb);
          break;
        default:
          break;
      }
    }

    function lukitseOppiaine(id, cb) {
      lukitse(LukkoOppiaine, {perusteId: PerusopetusService.getPerusteId(), osanId: id}, cb);
    }

    function vapautaOppiaine(id, cb) {
      vapauta(LukkoOppiaine, {perusteId: PerusopetusService.getPerusteId(), osanId: id}, cb);
    }

    function lukitseOppiaineenVuosiluokkakokonaisuus(oppiaineId, vuosiluokkaId, cb) {
      lukitse(LukkoOppiaineenVuosiluokkakokonaisuus, {
        perusteId: PerusopetusService.getPerusteId(),
        oppiaineId: oppiaineId,
        vuosiluokkaId: vuosiluokkaId
      }, cb);
    }

    function vapautaOppiaineenVuosiluokkakokonaisuus(oppiaineId, vuosiluokkaId, cb) {
      vapauta(LukkoOppiaineenVuosiluokkakokonaisuus, {
        perusteId: PerusopetusService.getPerusteId(),
        oppiaineId: oppiaineId,
        vuosiluokkaId: vuosiluokkaId
      }, cb);
    }

    return {
      lukitse: genericLukitse,
      vapauta: genericVapauta,
      hae: hae,
      tarkista: tarkistaLukitus,
      genericTarkista: genericTarkistaLukitus,
      lukitseSisalto: lukitseSisalto,
      vapautaSisalto: vapautaSisalto,
      lukitsePerusteenosa: lukitsePerusteenosa,
      vapautaPerusteenosa: vapautaPerusteenosa,

      lukitseOppiaine: lukitseOppiaine,
      vapautaOppiaine: vapautaOppiaine,
      lukitseOppiaineenVuosiluokkakokonaisuus: lukitseOppiaineenVuosiluokkakokonaisuus,
      vapautaOppiaineenVuosiluokkakokonaisuus: vapautaOppiaineenVuosiluokkakokonaisuus,
      lukitsePerusteenosaByTutkinnonOsaViite: lukitsePerusteenosaByTutkinnonOsaViite,
      vapautaPerusteenosaByTutkinnonOsaViite: vapautaPerusteenosaByTutkinnonOsaViite

    };
  });
