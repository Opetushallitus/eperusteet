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
  .controller('PerusopetusController', function($scope, FilterWatcher) {
    $scope.isNaviVisible = function () { return true; };
    $scope.navi = {
      header: 'perusteen-sisältö',
      oneAtATime: true,
      sections: [
        {
          title: 'Opetussuunnitelma',
          items: [
            {label: 'otsikko 1'},
            {label: 'otsikko 1.1', depth: 1},
            {label: 'otsikko 1.2', depth: 1},
            {label: 'otsikko 1.2.1', depth: 2},
            {label: 'otsikko 1.3', depth: 1},
            {label: 'otsikko 2'},
          ]
        },
        {
          title: 'Opetuksen sisällöt',
          include: 'views/partials/navifilters.html',
          $open: true,
          model: {
            oneAtATime: false,
            sections: [
              {title: 'Vuosiluokat', $open: true, items: [
                {label: 'Vuosiluokat 1-2'},
                {label: 'Vuosiluokat 3-4'},
                {label: 'Vuosiluokat 5-6'},
                {label: 'Vuosiluokat 7-9'},
              ]},
              {title: 'Oppiaineet', items: [
                {label: 'Äidinkieli ja kirjallisuus'},
                {label: 'Matematiikka'},
                {label: 'Fysiikka ja kemia'},
                {label: 'Musiikki'},
                {label: 'Liikunta'},
              ], $open: true},
              {title: 'Oppiaineen sisällöt', $open: true, include: 'views/partials/sisaltotagit.html'},
            ]
          }
        },
        {title: 'Liitteet'},
      ]
    };
    $scope.filtterit = {};
    $scope.setFilters = function () {
      _.each($scope.navi.sections, function (mainsection) {
        if (mainsection.model) {
          _.each(mainsection.model.sections, function (section) {
            $scope.filtterit[section.title] = _(section.items).filter('$selected').pluck('label').value();
          });
        }
      });
    };
    FilterWatcher.register($scope.setFilters);
  })
  .service('FilterWatcher', function () {
    var cb = angular.noop;
    this.register = function (callback) {
      cb = callback;
    };
    this.notify = function () {
      cb();
    };
  })
  .directive('multichoice', function () {
    return {
      restrict: 'AE',
      templateUrl: 'views/partials/multichoice.html',
      scope: {
        items: '='
      },
      link: function (scope) {
        _.each(scope.items, function (item) {
          item.$selected = true;
        });
      },
      controller: function ($scope, FilterWatcher) {
        $scope.model = {
          all: true
        };
        $scope.toggle = function () {
          _.each($scope.items, function (item) {
            item.$selected = $scope.model.all;
          });
          FilterWatcher.notify();
        };
        $scope.update = function () {
          var all = _.filter($scope.items, '$selected').length === $scope.items.length;
          if (all && !$scope.model.all) {
            $scope.model.all = true;
          }
          if (!all && $scope.model.all) {
            $scope.model.all = false;
          }
          FilterWatcher.notify();
        };
      }
    };
  })
  .controller('TagFilterController', function ($scope) {
    $scope.model = {
      tags: {
        items: [],
        clear: function () {
          $scope.model.tags.items = [];
        }
      },
      dialog: {
        isOpen: false,
        addButtonPressed: function () {
          $scope.model.dialog.isOpen = !$scope.model.dialog.isOpen;
        },
        close: function () {
          $scope.model.dialog.isOpen = false;
        }
      }
    };
  });
