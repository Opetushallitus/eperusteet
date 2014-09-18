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
    this.hasValue = function(obj, path) {
      return this.nestedHas(obj, path, '.') && !_.isEmpty(this.nestedGet(obj, path, '.'));
    };

    this.nestedHas = function(obj, path, delimiter) {

      function innerNestedHas(obj, names) {
        if(_.has(obj, names[0])) {
          return names.length > 1 ? innerNestedHas(obj[names[0]], names.splice(1, names.length)) : true;
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
          return innerNestedGet(obj[names[0]], names.splice(1, names.length));
        } else {
          return obj[names[0]];
        }
      }

      if(!this.nestedHas(obj, path, delimiter)) {
        return undefined;
      }
      var propertyNames = path.split(delimiter);

      return innerNestedGet(obj, propertyNames);
    };

    this.nestedSet = function(obj, path, delimiter, value) {

      function innerNestedSet(obj, names, newValue) {
        if(names.length > 1) {
          if(!_.has(obj, names[0]) || obj[names[0]] === null) {
            obj[names[0]] = {};
          }
          innerNestedSet(obj[names[0]], names.splice(1, names.length), newValue);
        }
        else {
            obj[names[0]] = newValue;
        }
      }

      var propertyNames = path.split(delimiter);

      innerNestedSet(obj, propertyNames, value);
    };

    this.nestedOmit = function(obj, path, delimiter) {

      function innerNestedOmit(obj, names) {
        if(names.length > 1) {
          obj[names[0]] = innerNestedOmit(obj[names[0]], names.splice(1, names.length));
          return obj;
        }
        else {
          return _.omit(obj, names[0]);
        }
      }

      var propertyNames = path.split(delimiter);

      return innerNestedOmit(obj, propertyNames);
    };
  });
