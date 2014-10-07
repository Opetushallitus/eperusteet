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
  .directive('muokattavaOsio', function() {
    return {
      templateUrl: 'views/directives/muokattavaosio.html',
      restrict: 'A',
      scope: {
        model: '=muokattavaOsio',
        type: '@'
      },
      controller: 'MuokattavaOsioController'
    };
  })
  .controller('MuokattavaOsioController', function($scope, YleinenData, Utils) {
    $scope.valitseKieli = _.bind(YleinenData.valitseKieli, YleinenData);

    $scope.hasContent = false;
    $scope.$watch('model', function () {
      $scope.hasContent = $scope.type !== 'tekstikappale' || Utils.hasLocalizedText($scope.model.nimi);
    }, true);
  })

  .directive('sisaltoalueet', function () {
    return {
      templateUrl: 'views/directives/sisaltoalueet.html',
      restrict: 'A',
      scope: {
        model: '=sisaltoalueet'
      },
      controller: 'SisaltoalueetController'
    };
  })
  .controller('SisaltoalueetController', function ($scope, YleinenData) {
    $scope.valitseKieli = _.bind(YleinenData.valitseKieli, YleinenData);
  })

  .directive('tavoitteet', function () {
    return {
      templateUrl: 'views/directives/tavoitteet.html',
      restrict: 'A',
      scope: {
        model: '=tavoitteet'
      },
      controller: 'TavoitteetController',
      link: function (scope) {
        var uniqueId = 0;
        _.each(scope.model.kohdealueet, function (kohdealue) {
          kohdealue.$accordionOpen = true;
          _.each(kohdealue.tavoitteet, function (tavoite) {
            tavoite.$runningIndex = ++uniqueId;
            tavoite.$sisaltoalueet = _.map(tavoite.sisaltoalueet, function (sisaltoalueId) {
              return _.find(scope.model.sisaltoalueet, function(item) {
                return item.id === sisaltoalueId;
              });
            });
            tavoite.$osaaminen = _.map(tavoite.osaaminen, function (osaamisId) {
              var osaaminen = _.find(scope.osaamiset, function (item) {
                return item.perusteenOsa.id === osaamisId;
              }).perusteenOsa;
              var vuosiluokkakuvaus = _.find(scope.vuosiluokka.osaamisenkuvaukset, function (item) {
                return item.osaaminen === osaamisId;
              });
              return {
                nimi: osaaminen.nimi,
                teksti: vuosiluokkakuvaus ? vuosiluokkakuvaus.teksti : 'ei-kuvausta'
              };
            });
          });
        });
      }
    };
  })
  .controller('TavoitteetController', function ($scope, YleinenData, PerusopetusService) {
    $scope.valitseKieli = _.bind(YleinenData.valitseKieli, YleinenData);
    // TODO don't fetch here, from parent maybe?
    $scope.osaamiset = PerusopetusService.getOsat(PerusopetusService.OSAAMINEN);
    $scope.vuosiluokka = _.find(PerusopetusService.getOsat(PerusopetusService.VUOSILUOKAT), {id: $scope.model._id});

    function setAccordion(mode) {
      var obj = $scope.model.kohdealueet;
      _.each(obj, function (kohdealue) {
        kohdealue.$accordionOpen = mode;
      });
    }

    function accordionState() {
      var obj = _.first($scope.model.kohdealueet);
      return obj && obj.$accordionOpen;
    }

    $scope.toggleAll = function () {
      setAccordion(!accordionState());
    };
  })

  .directive('tagCloud', function () {
    return {
      templateUrl: 'views/directives/tagcloud.html',
      restrict: 'A',
      scope: {
        model: '=tagCloud',
        openable: '@'
      },
      controller: 'TagCloudController'
    };
  })
  .controller('TagCloudController', function (/*$scope*/) {
  });
