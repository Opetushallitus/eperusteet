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
  .directive('tavoitteet', function () {
    return {
      templateUrl: 'views/directives/perusopetus/tavoitteet.html',
      restrict: 'A',
      scope: {
        model: '=tavoitteet'
      },
      controller: 'TavoitteetController',
      link: function (scope) {
        // TODO call on model update
        scope.mapModel();
      }
    };
  })
  .controller('TavoitteetController', function ($scope, YleinenData, PerusopetusService, $state) {
    $scope.valitseKieli = _.bind(YleinenData.valitseKieli, YleinenData);
    // TODO don't fetch here, from parent maybe?
    $scope.osaamiset = PerusopetusService.getOsat(PerusopetusService.OSAAMINEN);
    $scope.vuosiluokka = _.find(PerusopetusService.getOsat(PerusopetusService.VUOSILUOKAT), {id: $scope.model._id});

    $scope.mapModel = function () {
      var uniqueId = 0;
      _.each($scope.model.kohdealueet, function (kohdealue) {
        kohdealue.$accordionOpen = true;
        _.each(kohdealue.tavoitteet, function (tavoite) {
          tavoite.$runningIndex = ++uniqueId;
          tavoite.$sisaltoalueet = _.map(tavoite.sisaltoalueet, function (sisaltoalueId) {
            return _.find($scope.model.sisaltoalueet, function(item) {
              return item.id === sisaltoalueId;
            });
          });
          tavoite.$osaaminen = _.map(tavoite.osaaminen, function (osaamisId) {
            var osaaminen = _.find($scope.osaamiset, function (item) {
              return item.perusteenOsa.id === osaamisId;
            }).perusteenOsa;
            var vuosiluokkakuvaus = _.find($scope.vuosiluokka.osaamisenkuvaukset, function (item) {
              return item.osaaminen === osaamisId;
            });
            return {
              nimi: osaaminen.nimi,
              teksti: vuosiluokkakuvaus ? vuosiluokkakuvaus.teksti : 'ei-kuvausta',
              /* TODO vuosiluokkakokonaisuuden id */
              extra: '<div class="clearfix"><a class="pull-right" href="' +
                $state.href('root.perusteprojekti.osaalue', {osanTyyppi: PerusopetusService.VUOSILUOKAT, osanId: ''}) +
                '" kaanna="vuosiluokkakokonaisuuden-osaamisalueet"></a></div>'
            };
          });
        });
      });
    };

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

  .directive('sisaltoalueet', function () {
    return {
      templateUrl: 'views/directives/perusopetus/sisaltoalueet.html',
      restrict: 'A',
      scope: {
        model: '=sisaltoalueet'
      },
      controller: 'SisaltoalueetController'
    };
  })
  .controller('SisaltoalueetController', function ($scope, YleinenData) {
    $scope.valitseKieli = _.bind(YleinenData.valitseKieli, YleinenData);
  });
