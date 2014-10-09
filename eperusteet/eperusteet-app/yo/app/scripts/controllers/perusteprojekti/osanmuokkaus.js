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

    this.setModel = function (data) {
      this.model = data;
    };

    this.getModel = function () {
      if (!this.model) {
        this.model = this.fetch();
      }
      return this.model;
    };

    this.setBackState = function () {
      this.backState = [$state.current.name, _.clone($stateParams)];
    };

    this.goBack = function () {
      if (!this.backState) {
        return;
      }
      var params = _.clone(this.backState);
      this.backState = null;
      $state.go.apply($state, params);
    };

    this.fetch = function () {
      // TODO dummy data
      var osa = PerusopetusService.getOsat(PerusopetusService.OPPIAINEET)[0].vuosiluokkakokonaisuudet[1].tekstikappaleet[0];
      return osa;
    };
  })

  .controller('OsanMuokkausController', function($scope, $stateParams, $compile, OsanMuokkausHelper,
      Editointikontrollit) {
    $scope.objekti = OsanMuokkausHelper.getModel();

    var MAPPING = {
      tekstikappale: {
        directive: 'osanmuokkaus-tekstikappale',
        attrs: {
          model: 'objekti',
        },
        title: 'muokkaus-tekstikappale',
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
      sisaltoalueet: {
        directive: 'osanmuokkaus-sisaltoalueet',
        attrs: {},
        title: 'muokkaus-sisaltoalueet',
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
        attrs: {},
        title: 'muokkaus-tavoitteet',
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
      templateUrl: 'views/directives/osanmuokkaustekstikappale.html',
      restrict: 'E',
      scope: {
        model: '=',
        config: '='
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

  .directive('osanmuokkausSisaltoalueet', function () {
    return {
      templateUrl: 'views/directives/perusopetus/osanmuokkaussisaltoalueet.html',
      restrict: 'E',
      scope: {
        model: '=',
        config: '='
      }
    };
  });
