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
/* global _ */

angular.module('eperusteApp')
  .service('OsanMuokkausHelper', function ($q, $stateParams, PerusopetusService, $state, Lukitus) {
    var vuosiluokat = [];
    var model = null;
    var isLocked = false;
    var backState = null;
    var vuosiluokka = null;
    var path = null;
    var oppiaine = null;
    var osaamiset = null;

    function reset() {
      backState = null;
      vuosiluokka = null;
      osaamiset = null;
      path = null;
      oppiaine = null;
    };

    function getModel() {
      return path ? model[path] : model;
    };

    function setup(uusiModel, uusiPath, uusiOppiaine, cb) {
      cb = cb || angular.noop;
      oppiaine = $stateParams.osanTyyppi === PerusopetusService.OPPIAINEET ? uusiModel : null;
      if (uusiOppiaine) {
        oppiaine = uusiOppiaine;
      }
      model = uusiModel;
      path = uusiPath;
      isLocked = false;
      backState = [$state.current.name, _.clone($stateParams)];

      $q.all([
        PerusopetusService.getOsat(PerusopetusService.VUOSILUOKAT, true).$promise,
        PerusopetusService.getOsat(PerusopetusService.OSAAMINEN, true).$promise
      ]).then(function(res) {
        vuosiluokat = res[0];
        osaamiset = res[1];
        vuosiluokka = uusiModel.vuosiluokkaKokonaisuus ? _.find(vuosiluokat, function(vl) {
          return vl.id === parseInt(model.vuosiluokkaKokonaisuus, 10);
        }) : null;
        if (isVuosiluokkakokonaisuudenOsa()) {
          Lukitus.lukitseOppiaineenVuosiluokkakokonaisuus(oppiaine.id, model.id, function () {
            isLocked = true;
            cb();
          });
        } else if (oppiaine) {
          Lukitus.lukitseOppiaine(oppiaine.id, function() {
            isLocked = true;
            cb();
          });
        }
      });
    };

    function save() {
      if (isVuosiluokkakokonaisuudenOsa()) {
        PerusopetusService.saveVuosiluokkakokonaisuudenOsa(model, oppiaine, function () {
          Lukitus.vapautaOppiaineenVuosiluokkakokonaisuus(oppiaine.id, model.id, function () {
            isLocked = false;
            goBack();
          });
        });
      } else if (path) {
        var payload = _.pick(model, ['id', path]);
        PerusopetusService.saveOsa(payload, backState[1], function () {
          if (isLocked && oppiaine) {
            Lukitus.vapautaOppiaine(oppiaine.id, function () {
              isLocked = false;
              goBack();
            });
          }
        });
      }
    };

    function goBack() {
      if (!backState) {
        return;
      }

      if (isLocked) {
        if (isVuosiluokkakokonaisuudenOsa()) {
          Lukitus.vapautaOppiaineenVuosiluokkakokonaisuus(oppiaine.id, model.id, function () {
            isLocked = false;
          });
        } else if (oppiaine) {
          Lukitus.vapautaOppiaine(oppiaine.id, function () {
            isLocked = false;
          });
        }
      }
      var params = _.clone(backState);
      reset();
      $state.go.apply($state, params, {reload: true});
    };

    function isVuosiluokkakokonaisuudenOsa() {
      return !!vuosiluokka;
    };

    function getOsaamiset() {
      return osaamiset;
    };

    function getOppiaine() {
      return oppiaine;
    };

    function getVuosiluokkakokonaisuus() {
      return vuosiluokka;
    };

    return {
      reset: reset,
      getModel: getModel,
      setup: setup,
      save: save,
      goBack: goBack,
      getOppiaine: getOppiaine,
      getOsaamiset: getOsaamiset,
      getVuosiluokkakokonaisuus: getVuosiluokkakokonaisuus,
    };
  })

  .controller('OsanMuokkausController', function($scope, $stateParams, $compile, OsanMuokkausHelper,
      Editointikontrollit, $rootScope) {
    $scope.objekti = OsanMuokkausHelper.getModel();
    if (!$scope.objekti) {
      return;
    }

    var MAPPING = {
      tekstikappale: {
        directive: 'osanmuokkaus-tekstikappale',
        attrs: {
          model: 'objekti',
        },
        callbacks: {
          save: function () {
            $rootScope.$broadcast('notifyCKEditor');
            OsanMuokkausHelper.save();
          },
          edit: function () {},
          cancel: function () {
            OsanMuokkausHelper.goBack();
          },
        }
      },
      sisaltoalueet: {
        directive: 'osanmuokkaus-sisaltoalueet',
        attrs: {
          model: 'objekti',
        },
        callbacks: {
          save: function () {
            $rootScope.$broadcast('notifyCKEditor');
            OsanMuokkausHelper.save();
          },
          edit: function () {},
          cancel: function () {
            OsanMuokkausHelper.goBack();
          },
        }
      },
      kohdealueet: {
        directive: 'osanmuokkaus-kohdealueet',
        attrs: {
          model: 'objekti',
        },
        callbacks: {
          save: function () {
            OsanMuokkausHelper.save();
          },
          edit: function () {},
          cancel: function () {
            OsanMuokkausHelper.goBack();
          },
        }
      },
      tavoitteet: {
        directive: 'osanmuokkaus-tavoitteet',
        attrs: {
          model: 'objekti',
        },
        callbacks: {
          save: function () {
            OsanMuokkausHelper.save();
          },
          edit: function () {},
          cancel: function () {
            OsanMuokkausHelper.goBack();
          },
        }
      }
    };

    var config = MAPPING[$stateParams.osanTyyppi];
    $scope.config = config;
    var muokkausDirective = angular.element('<' + config.directive + '>').attr('config', 'config');
    _.each(config.attrs, function (value, key) {
      muokkausDirective.attr(key, value);
    });
    var el = $compile(muokkausDirective)($scope);

    angular.element('#muokkaus-elementti-placeholder').replaceWith(el);

    Editointikontrollit.registerCallback(config.callbacks);
    Editointikontrollit.startEditing();
    // TODO muokkauksesta poistumisvaroitus
  })

  .directive('osanmuokkausTekstikappale', function () {
    return {
      templateUrl: 'views/directives/perusopetus/osanmuokkaustekstikappale.html',
      restrict: 'E',
      scope: {
        model: '=',
        config: '=',
        editMode: '@'
      },
      controller: 'OsanmuokkausTekstikappaleController',
      link: function (scope, element, attrs) {
        attrs.$observe('editMode', function (val) {
          scope.editMode = val !== 'false';
        });
      }
    };
  })

  .controller('OsanmuokkausTekstikappaleController', function ($scope, OsanMuokkausHelper, $rootScope,
      YleinenData, $state, $stateParams) {
    $scope.valitseKieli = _.bind(YleinenData.valitseKieli, YleinenData);
    $scope.getTitle = function () {
      return OsanMuokkausHelper.isVuosiluokkakokonaisuudenOsa() ?
        'muokkaus-vuosiluokkakokonaisuuden-osa' :
        ($scope.model.$isNew ? 'luonti-tekstikappale' : 'muokkaus-tekstikappale');
    };
    // Odota tekstikenttien alustus ja päivitä editointipalkin sijainti
    var received = 0;
    $scope.$on('ckEditorInstanceReady', function() {
      if (++received === 2) {
        $rootScope.$broadcast('editointikontrollitRefresh');
      }
    });

    $scope.edit = function () {
      OsanMuokkausHelper.setup($scope.model);
      $state.go('root.perusteprojekti.suoritustapa.muokkaus', $stateParams);
    };
  })

  .directive('osanmuokkausKohdealueet', function () {
    return {
      templateUrl: 'views/directives/perusopetus/osanmuokkauskohdealueet.html',
      restrict: 'E',
      scope: {
        model: '=',
        config: '='
      },
      controller: function($scope) {
        $scope.model = $scope.model || [];

        $scope.lisaaKohdealue = function(uusiKohdealue) {
          $scope.model.push({ nimi: _.clone(uusiKohdealue) });
          $scope.uusiKohdealue = {};
        };

        $scope.poistaKohdealue = _.partial(_.remove, $scope.model);
      }
    };
  })

  .directive('osanmuokkausTavoitteet', function () {
    return {
      templateUrl: 'views/directives/perusopetus/osanmuokkaustavoitteet.html',
      restrict: 'E',
      scope: {
        model: '=',
        config: '='
      }
    };
  })

  .directive('perusopetusMuokkausInfo', function (OsanMuokkausHelper) {
    return {
      templateUrl: 'views/directives/perusopetus/muokkausinfo.html',
      restrict: 'AE',
      link: function (scope, element, attrs) {
        scope.muokkausinfoOsa = attrs.osa || '';
      },
      controller: function ($scope) {
        $scope.getOppiaine = function () {
          var oppiaine = OsanMuokkausHelper.getOppiaine();
          return oppiaine ? oppiaine.nimi : '';
        };
        $scope.getVuosiluokkakokonaisuus = function () {
          var vlk = OsanMuokkausHelper.getVuosiluokkakokonaisuus();
          return vlk ? vlk.nimi : '';
        };
      }
    };
  });
