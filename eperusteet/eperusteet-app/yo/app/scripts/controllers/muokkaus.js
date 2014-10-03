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
  .controller('MuokkausCtrl', function($scope, $stateParams, $compile, Navigaatiopolku, PerusteenOsat,
                                       virheService, VersionHelper) {
    $scope.tyyppi = $stateParams.perusteenOsanTyyppi;
    $scope.objekti = null;
    $scope.versiot = {};
    $scope.isLocked = false;

    if ($stateParams.perusteenOsaId !== 'uusi') {
      var successCb = function(re) {
        Navigaatiopolku.asetaElementit({ perusteenOsaId: re.nimi });
      };
      var errorCb = function() {
        virheService.virhe('virhe-perusteenosaa-ei-löytynyt');
      };
      var versio = $stateParams.versio ? $stateParams.versio.replace(/\//g, '') : null;
      if (versio) {
        VersionHelper.getPerusteenosaVersions($scope.versiot, {id: $stateParams.perusteenOsaId}, true, function () {
          var revNumber = VersionHelper.select($scope.versiot, versio);
          if (!revNumber) {
            errorCb();
          } else {
            $scope.objekti = PerusteenOsat.getVersio({
              osanId: $stateParams.perusteenOsaId,
              versioId: revNumber
            }, successCb, errorCb);
          }
        });
      } else {
        $scope.objekti = PerusteenOsat.get({ osanId: $stateParams.perusteenOsaId }, successCb, errorCb);
      }
    }
    else {
      Navigaatiopolku.asetaElementit({ perusteenOsaId: 'uusi' });
    }

    var muokkausDirective = null;
    if ($stateParams.perusteenOsanTyyppi === 'tekstikappale') {
      muokkausDirective = angular.element('<muokkaus-tekstikappale ng-if="objekti.$resolved" tekstikappale="objekti" versiot="versiot"></muokkaus-tekstikappale>');
    }
    else if ($stateParams.perusteenOsanTyyppi === 'tutkinnonosa') {
      muokkausDirective = angular.element('<muokkaus-tutkinnonosa ng-if="objekti.$resolved" tutkinnon-osa="objekti" versiot="versiot"></muokkaus-tutkinnonosa>');
    }
    else if ($stateParams.perusteenOsanTyyppi === 'tutkinnonosa2') {
      muokkausDirective = angular.element('<muokkaus-tutkinnonosa2 ng-if="objekti.$resolved" tutkinnon-osa="objekti" versiot="versiot"></muokkaus-tutkinnonosa2>');
    }
    else {
      virheService.virhe('virhe-perusteenosaa-ei-löytynyt');
    }
    var el = $compile(muokkausDirective)($scope);

    angular.element('#muokkaus-elementti-placeholder').replaceWith(el);


  })
  .service('MuokkausUtils', function() {
    /**
     * Access nested object/array.
     * @param obj [Object]
     * @param path [String] e.g. 'nested.path', 'somearray[1].innerKey'
     * @param action [String] default action 'get', can be also 'has' (returns boolean) or 'set'.
     * @param value Used if action is set
     * @param replace Used if action is set, replaces object data per key
     *       instead of full overwrite of obj[path] (preserves object pointer)
     */
    function access(obj, path, action, value, replace) {
      var match = path.match(/(.+)\[(\d+)\]/);
      if (match) {
        var index = parseInt(match[2], 10);
        var arr = obj[match[1]];
        if (action === 'has') {
          return _.isArray(arr) && arr.length > index;
        } else if (action === 'set') {
          arr[index] = value;
        }
        return arr[index];
      }
      if (action === 'has') {
        return _.has(obj, path);
      } else if (action === 'set') {
        if (replace) {
          _.each(value, function (val, key) {
            obj[path][key] = val;
          });
        } else {
          obj[path] = value;
        }
      }
      return obj[path];
    }

    function has(obj, path) {
      return access(obj, path, 'has');
    }

    function set(obj, path, value, replace) {
      return access(obj, path, 'set', value, replace);
    }

    this.hasValue = function(obj, path) {
      return this.nestedHas(obj, path, '.') && !_.isEmpty(this.nestedGet(obj, path, '.'));
    };

    this.nestedHas = function(obj, path, delimiter) {

      function innerNestedHas(obj, names) {
        if(has(obj, names[0])) {
          return names.length > 1 ? innerNestedHas(access(obj, names[0]), names.splice(1, names.length)) : true;
        } else {
          return false;
        }
      }

      var propertyNames = path.split(delimiter);

      return innerNestedHas(obj, propertyNames);
    };

    this.nestedGet = function(obj, path, delimiter) {
      function innerNestedGet(obj, names) {
        if(names.length > 1) {
          return innerNestedGet(access(obj, names[0]), names.splice(1, names.length));
        } else {
          return access(obj, names[0]);
        }
      }

      if(!this.nestedHas(obj, path, delimiter)) {
        return undefined;
      }
      var propertyNames = path.split(delimiter);

      return innerNestedGet(obj, propertyNames);
    };

    this.nestedSet = function(obj, path, delimiter, value, replace) {
      function innerNestedSet(obj, names, newValue) {
        var dest = access(obj, names[0]);
        if(names.length > 1) {
          if(!has(obj, names[0]) || dest === null) {
            dest = set(obj, names[0], {});
          }
          innerNestedSet(dest, names.splice(1, names.length), newValue);
        } else {
          set(obj, names[0], newValue, replace);
        }
      }

      var propertyNames = path.split(delimiter);

      innerNestedSet(obj, propertyNames, value);
    };
  });
