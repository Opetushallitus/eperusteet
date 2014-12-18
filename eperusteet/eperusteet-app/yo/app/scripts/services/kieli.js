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
  .service('Kieli', function ($rootScope, $state, $stateParams) {
    var sisaltokieli = 'fi';

    this.SISALTOKIELET = [
      'fi',
      'sv',
      'se',
      'ru',
      'en'
    ];

    this.setSisaltokieli = function (kielikoodi) {
      if (_.indexOf(this.SISALTOKIELET, kielikoodi) > -1) {
        var old = sisaltokieli;
        sisaltokieli = kielikoodi;
        if (old !== kielikoodi) {
          $rootScope.$broadcast('changed:sisaltokieli', kielikoodi);
        }
      }
    };

    this.getSisaltokieli = function () {
      return sisaltokieli;
    };

    this.setUiKieli = function (kielikoodi) {
      if (this.isValidKielikoodi(kielikoodi)) {
        $state.go($state.current.name, _.merge($stateParams, {lang: kielikoodi}), {reload: true});
      }
    };

    this.isValidKielikoodi = function (kielikoodi) {
      return _.indexOf(this.SISALTOKIELET, kielikoodi) > -1;
    };
  })

  .directive('kielenvaihto', function () {
    return {
      restrict: 'AE',
      scope: {
        modal: '@modal'
      },
      controller: 'KieliCtrl',
      templateUrl: 'views/directives/kielenvaihto.html'
    };
  })

  .controller('KieliCtrl', function($scope, $stateParams, YleinenData, $state, Kieli, Profiili, $q) {
    $scope.isModal = $scope.modal === 'true';
    $scope.sisaltokielet = Kieli.SISALTOKIELET;
    $scope.sisaltokieli = Kieli.getSisaltokieli();
    $scope.uiLangChangeAllowed = true;
    var stateInit = $q.defer();
    var casFetched = $q.defer();

    var info = Profiili.profiili();
    if (info.$casFetched) {
      casFetched.resolve();
    }

    $scope.$on('$stateChangeSuccess', function () {
      stateInit.resolve();
    });

    $scope.$on('fetched:casTiedot', function () {
      casFetched.resolve();
    });

    $q.all([stateInit.promise, casFetched.promise]).then(function () {
      var lang = Profiili.lang();
      // Disable ui language change if language preference found in CAS
      if (Kieli.isValidKielikoodi(lang)) {
        $scope.uiLangChangeAllowed = false;
        Kieli.setUiKieli(lang);
      }
      var profiili = Profiili.profiili();
      if (profiili.preferenssit.sisaltokieli) {
        Kieli.setSisaltokieli(profiili.preferenssit.sisaltokieli);
      }
    });

    $scope.$on('changed:sisaltokieli', function (event, value) {
      $scope.sisaltokieli = value;
      Profiili.setPreferenssi('sisaltokieli', value);
    });

    $scope.setSisaltokieli = function (kieli) {
      Kieli.setSisaltokieli(kieli);
    };

    $scope.koodit = _.map(_.pairs(YleinenData.kielet), function (item) {
      return {koodi: item[1], nimi: item[0]};
    });
    $scope.kieli = YleinenData.kieli;

    $scope.$on('notifyCKEditor', function () {
      $scope.kieli = YleinenData.kieli;
    });

    $scope.vaihdaKieli = function(kielikoodi) {
      Kieli.setUiKieli(kielikoodi);
    };

  });
