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
        model: '=tavoitteet',
        editable: '@?'
      },
      controller: 'TavoitteetController',
      link: function (scope) {
        // TODO call on model update
        scope.mapModel();
      }
    };
  })
  .controller('TavoitteetController', function ($scope, YleinenData, PerusopetusService, $state, $rootScope) {
    $scope.valitseKieli = _.bind(YleinenData.valitseKieli, YleinenData);
    // TODO don't fetch here, from parent maybe?
    $scope.osaamiset = PerusopetusService.getOsat(PerusopetusService.OSAAMINEN);
    $scope.vuosiluokka = _.find(PerusopetusService.getOsat(PerusopetusService.VUOSILUOKAT), {id: $scope.model._id});
    $scope.editMode = false;
    $scope.original = {};
    $scope.currentEditable = null;
    $scope.$watch('editable', function (value) {
      $scope.editMode = !!value;
    });

    $scope.treeOptions = {
      accept: function(sourceNodeScope, destNodesScope) {
        var draggingTavoite = _.has(sourceNodeScope, 'tavoite');
        var droppingTavoite = destNodesScope.depth() === 1;
        return draggingTavoite ? droppingTavoite : !droppingTavoite;
      },
      dropped: function () {
        $scope.mapModel(true);
      }
    };

    $scope.mapModel = function (update) {
      var uniqueId = 0;
      _.each($scope.model.kohdealueet, function (kohdealue) {
        if (!update) {
          kohdealue.$accordionOpen = true;
        }
        _.each(kohdealue.tavoitteet, function (tavoite) {
          tavoite.$runningIndex = ++uniqueId;
          tavoite.$sisaltoalueet = _.map($scope.model.sisaltoalueet, function (item) {
            var found = _.find(tavoite.sisaltoalueet, function (alueId) {
              return alueId === item.id;
            });
            item = _.clone(item);
            item.$hidden = !found;
            return item;
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
                $state.href('root.perusteprojekti.osaalue', {
                  osanTyyppi: PerusopetusService.VUOSILUOKAT,
                  osanId: '',
                  tabId: 0
                }) +
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

    $scope.hasArviointi = function (tavoite) {
      return !!tavoite.arviointi && tavoite.arviointi.length > 0;
    };

    $scope.addArviointi = function (tavoite) {
      tavoite.arviointi = [{kohde: {}, kuvaus: {}}];
    };

    $scope.kohdealueFn = {
      edit: function (kohdealue) {
        kohdealue.$editing = true;
        $scope.currentEditable = kohdealue;
        $scope.original.kohdealueNimi = _.clone(kohdealue.nimi);
      },
      remove: function ($index) {
        $scope.model.kohdealueet.splice($index, 1);
      },
      add: function () {
        var newAlue = {$editing: true, $accordionOpen: true, $new: true, tavoitteet: []};
        $scope.currentEditable = newAlue;
        $scope.model.kohdealueet.push(newAlue);
      },
      ok: function () {
        $scope.currentEditable.$editing = false;
        $scope.currentEditable.$new = false;
        $scope.currentEditable = null;
      },
      cancel: function () {
        if ($scope.currentEditable.$new) {
          $scope.kohdealueFn.remove($scope.model.kohdealueet.length - 1);
        } else {
          $scope.currentEditable.$editing = false;
          $scope.currentEditable.nimi = _.clone($scope.original.kohdealueNimi);
        }
        $scope.currentEditable = null;
        $scope.original = {};
      }
    };

    $scope.tavoiteFn = {
      edit: function (tavoite) {
        tavoite.$editing = true;
        $scope.currentEditable = tavoite;
        $scope.original.tavoite = _.cloneDeep(tavoite);
      },
      remove: function (kohdealue, $index) {
        kohdealue.tavoitteet.splice($index, 1);
        $scope.mapModel(true);
      },
      ok: function () {
        $rootScope.$broadcast('notifyCKEditor');
        $scope.currentEditable.$editing = false;
        $scope.currentEditable.$new = false;
        $scope.currentEditable = null;
      },
      cancel: function () {
        if (!$scope.currentEditable.$new) {
          _.each($scope.currentEditable, function (value, key) {
            $scope.currentEditable[key] = _.cloneDeep($scope.original.tavoite[key]);
          });
        } else {
          $scope.tavoiteFn.remove($scope.original.kohdealue, $scope.original.kohdealue.tavoitteet.length - 1);
        }
        $scope.currentEditable.$editing = false;
        $scope.original = {};
        $scope.currentEditable = null;
      },
      add: function (kohdealue) {
        var newTavoite = {$editing: true, kuvaus: {}, $new: true};
        $scope.currentEditable = newTavoite;
        kohdealue.tavoitteet.push(newTavoite);
        $scope.original.kohdealue = kohdealue;
        $scope.mapModel(true);
      }
    };
  });
