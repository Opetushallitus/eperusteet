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
  .service('ProjektinMurupolkuService', function ($rootScope, $state) {
    var PREFIX = 'root.perusteprojekti.';
    var namecache = {};
    var URLS = {
      tutkinnonosat: ['suoritustapa.tutkinnonosat'],
      osalistaus: ['osalistaus']
    };
    var custom = [];

    this.getName = function (idKey, stateParams) {
      console.log("getName", idKey, stateParams);
      return namecache[idKey] ? namecache[idKey][stateParams[idKey]] : null;
    };

    this.getUrl = function (key, stateParams) {
      return $state.href(PREFIX + URLS[key], stateParams);
    };

    var STATES = {
      'suoritustapa.tutkinnonosa': {
        items: [{url: 'tutkinnonosat'}, {getName: 'tutkinnonOsaViiteId'}]
      },
      'suoritustapa.tekstikappale': {
        items: 'custom'
      },
      'osaalue': {
        items: [{url: 'osalistaus', label: {getName: 'osanTyyppi'}}, {getName: 'osanId'}]
      }
    };

    this.get = function (stateName) {
      console.log("get", stateName);
      if (stateName.indexOf(PREFIX) === 0) {
        stateName = stateName.substring(PREFIX.length);
      }
      return STATES[stateName];
    };

    this.set = function (idKey, id, value) {
      console.log("set", idKey, id, value);
      if (!namecache[idKey]) {
        namecache[idKey] = {};
      }
      namecache[idKey][id] = value;
      $rootScope.$broadcast('update:projektinMurupolku');
    };

    this.setCustom = function (arr) {
      custom = _.cloneDeep(arr);
      $rootScope.$broadcast('update:projektinMurupolku');
    };

    this.getCustom = function () {
      return custom;
    };
  })

  .directive('projektinMurupolku', function() {
    return {
      templateUrl: 'views/directives/perusteprojekti/murupolku.html',
      restrict: 'AE',
      scope: {},
      controller: 'ProjektinMurupolkuController'
    };
  })

  .controller('ProjektinMurupolkuController', function($scope, $state, ProjektinMurupolkuService, $stateParams) {
    $scope.isActive = true;

    function resolveItem(item, simple) {
      var ret = item;
      if (_.isObject(item)) {
        if (_.has(item, 'url')) {
          var url = ProjektinMurupolkuService.getUrl(item.url, $stateParams);
          ret = {url: url, label: item.label ? resolveItem(item.label, true) : item.url};
        } else {
          var fn = _.first(_.keys(item));
          var value = item[fn];
          var label = ProjektinMurupolkuService[fn](value, $stateParams);
          ret = simple ? label : {label: label};
        }
      }
      console.log("resolved", ret);
      return ret;
    }

    function setCrumb() {
      var crumbConfig = ProjektinMurupolkuService.get($state.current.name);
      var items = crumbConfig ? _.cloneDeep(crumbConfig.items) : [];
      if (items === 'custom') {
        $scope.items = ProjektinMurupolkuService.getCustom();
      } else {
        for (var i = 0; i < items.length; ++i) {
          items[i] = resolveItem(items[i]);
        }
        $scope.items = items;
      }
    }

    $scope.$on('$stateChangeSuccess', setCrumb);
    $scope.$on('update:projektinMurupolku', setCrumb);
    $scope.$on('disableEditing', function () {
      $scope.isActive = true;
    });
    $scope.$on('enableEditing', function () {
      $scope.isActive = false;
    });

    setCrumb();
  });
