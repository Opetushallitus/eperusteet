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
  .service('OsanMuokkausHelper', function ($stateParams, PerusopetusService, $state) {
    this.model = null;
    this.backState = null;
    this.vuosiluokka = null;
    this.path = null;
    this.oppiaine = null;

    this.getModel = function () {
      if (!this.model) {
        //this.model = this.fetch();
      }
      return this.path ? this.model[this.path] : this.model;
    };

    this.setup = function (model, path) {
      this.oppiaine = $stateParams.osanTyyppi === PerusopetusService.OPPIAINEET ? model : null;
      this.model = model;
      this.path = path;
      this.backState = [$state.current.name, _.clone($stateParams)];
    };

    this.save = function () {
      if (this.path) {
        var payload = _.pick(this.model, ['id', this.path]);
        PerusopetusService.saveOsa(payload, this.backState[1]);
      }
      console.log(this.model, this.path, this.backState);
    };

    this.goBack = function () {
      if (!this.backState) {
        return;
      }
      var params = _.clone(this.backState);
      this.backState = null;
      $state.go.apply($state, params, {reload: true});
    };

    this.fetch = function () {
      // TODO dummy data
      var kokonaisuus = PerusopetusService.getOsat(PerusopetusService.OPPIAINEET)[0].vuosiluokkakokonaisuudet[1];
      var osa = kokonaisuus.tekstikappaleet[0];
      if ($stateParams.osanTyyppi === 'sisaltoalueet') {
        osa = kokonaisuus.sisaltoalueet;
      } else if ($stateParams.osanTyyppi === 'tavoitteet') {
        osa = kokonaisuus;
      }
      var vuosiluokat = PerusopetusService.getOsat(PerusopetusService.VUOSILUOKAT);
      this.vuosiluokka = _.find(vuosiluokat, function (item) {
        return kokonaisuus._id === item.id;
      });
      return osa;
    };

    this.isVuosiluokkakokonaisuudenOsa = function () {
      return !!this.vuosiluokka;
    };

    this.getOppiaine = function () {
      return this.oppiaine;
    };

    this.getVuosiluokkakokonaisuus = function () {
      return this.vuosiluokka;
    };
  })

  .controller('OsanMuokkausController', function($scope, $stateParams, $compile, OsanMuokkausHelper,
      Editointikontrollit) {
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
            OsanMuokkausHelper.save();
            OsanMuokkausHelper.goBack();
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
            OsanMuokkausHelper.goBack();
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
            OsanMuokkausHelper.goBack();
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
      },
      controller: 'OsanmuokkausTekstikappaleController'
    };
  })

  .controller('OsanmuokkausTekstikappaleController', function ($scope, OsanMuokkausHelper, $rootScope) {
    $scope.getTitle = function () {
      return OsanMuokkausHelper.isVuosiluokkakokonaisuudenOsa() ?
        'muokkaus-vuosiluokkakokonaisuuden-osa' : 'muokkaus-tekstikappale';
    };
    // Odota tekstikenttien alustus ja päivitä editointipalkin sijainti
    var received = 0;
    $scope.$on('ckEditorInstanceReady', function() {
      if (++received === 2) {
        $rootScope.$broadcast('editointikontrollitRefresh');
      }
    });
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
