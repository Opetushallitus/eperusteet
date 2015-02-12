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
  .directive('tavoitteet', function() {
    return {
      templateUrl: 'views/directives/perusopetus/tavoitteet.html',
      restrict: 'A',
      scope: {
        model: '=tavoitteet',
        editable: '@?',
        providedVuosiluokka: '=?vuosiluokka',
        providedOsaamiset: '=?osaamiset',
        providedOppiaine: '=?oppiaine'
      },
      controller: 'TavoitteetController',
      link: function(scope) {
        // TODO call on model update
        scope.mapModel();
      }
    };
  })
  .controller('TavoitteetController', function($scope, $modal, PerusopetusService, $state, $rootScope, $timeout,
    CloneHelper, OsanMuokkausHelper, $stateParams, ProxyService, Oppiaineet, Kaanna) {
    $scope.osaamiset = $scope.providedOsaamiset || OsanMuokkausHelper.getOsaamiset();
    if (_.isEmpty($scope.osaamiset)) {
      // käytetään vain lukutilan kanssa
      PerusopetusService.getOsat(PerusopetusService.OSAAMINEN, true).then(function (res) {
        $scope.osaamiset = res;
        $scope.mapModel();
      });
    }
    $scope.vuosiluokka = $scope.providedVuosiluokka || OsanMuokkausHelper.getVuosiluokkakokonaisuus();
    $scope.oppiaine = $scope.providedOppiaine || OsanMuokkausHelper.getOppiaine();
    var oppiaineId = $scope.oppiaine.id;
    $scope.editMode = false;
    $scope.currentEditable = null;

    $scope.kohdealueet = $scope.oppiaine.oppiaine ? $scope.oppiaine.oppiaine.kohdealueet : $scope.oppiaine.kohdealueet;

    $scope.$on('update:oppiaineenkohdealueet', function() {
      $scope.oppiaine = OsanMuokkausHelper.getOppiaine();
      oppiaineId = $scope.oppiaine.id;
      $scope.kohdealueet = $scope.oppiaine.kohdealueet;
      var kohdealueetMap = _.zipBy($scope.kohdealueet, 'id');
      angular.forEach($scope.model.tavoitteet, function(tavoite) {
        if (!_.isEmpty(tavoite.kohdealueet) && !kohdealueetMap[parseInt(_.first(tavoite.kohdealueet), 10)]) {
          tavoite.kohdealueet.length = 0;
          $scope.poistaValittuKohdealue(tavoite);
        }
      });
    });

    $scope.$watch('editable', function(value) {
      $scope.editMode = !!value;
    });

    $scope.treeOptions = {
      dropped: function() {
        $scope.mapModel(true);
      }
    };

    $scope.$watch('providedVuosiluokka', function () {
      $scope.vuosiluokka = $scope.providedVuosiluokka || OsanMuokkausHelper.getVuosiluokkakokonaisuus();
      $scope.mapModel();
    });

    function generateArraySetter(findFrom, manipulator) {
      manipulator = manipulator || angular.noop;
      return function(item) {
        var found = _.find(findFrom, function(findItem) { return parseInt(findItem, 10) === item.id; });
        item = _.clone(item);
        item.$hidden = !found;
        item.teksti = item.kuvaus;
        return manipulator(item) || item;
      };
    }

    $scope.kaannaKohdealue = function(ka) {
      return Kaanna.kaanna(ka.nimi);
    };

    $scope.poistaValittuKohdealue = function(tavoite) {
      tavoite.$valittuKohdealue = undefined;
    };

    $scope.asetaKohdealue = function(tavoite) {
      tavoite.$kohdealueet = tavoite.$valittuKohdealue ? [tavoite.$valittuKohdealue] : [];
    };

    $scope.mapModel = function(update) {
      _.each($scope.model.tavoitteet, function(tavoite) {
        if (!update) {
          tavoite.$accordionOpen = true;
        }

        var kohdealueId = !_.isEmpty(tavoite.kohdealueet) ? _.first(tavoite.kohdealueet) : null;
        if (kohdealueId) {
          tavoite.$valittuKohdealue = _.find($scope.kohdealueet, function(ka) {
            return ka.id === parseInt(kohdealueId, 10);
          });
        }

        tavoite.$sisaltoalueet = _.map($scope.model.sisaltoalueet, generateArraySetter(tavoite.sisaltoalueet));
        tavoite.$osaaminen = _.map($scope.kohdealueet, generateArraySetter(tavoite.laajattavoitteet, function(osaaminen) {
          var vuosiluokkakuvaus = _.find($scope.vuosiluokka.laajaalaisetOsaamiset, function(item) {
            return parseInt(item.laajaalainenOsaaminen, 10) === osaaminen.id;
          });
          osaaminen.teksti = vuosiluokkakuvaus ? vuosiluokkakuvaus.kuvaus : 'ei-kuvausta';
          osaaminen.extra = '<div class="clearfix"><a class="pull-right" href="' +
            $state.href('root.perusteprojekti.suoritustapa.osaalue', {
              suoritustapa: $stateParams.suoritustapa,
              osanTyyppi: PerusopetusService.VUOSILUOKAT,
              osanId: $scope.vuosiluokka.id,
              tabId: 0
            }) + '" kaanna="vuosiluokkakokonaisuuden-osaamisalueet"></a></div>';
        }));
      });
    };

    function setAccordion(mode) {
      var obj = $scope.model.tavoitteet;
      _.each(obj, function(tavoite) {
        tavoite.$accordionOpen = mode;
      });
    }

    function accordionState() {
      var obj = _.first($scope.model.tavoitteet);
      return obj && obj.$accordionOpen;
    }

    $scope.toggleAll = function() {
      setAccordion(!accordionState());
    };

    $scope.hasArviointi = function(tavoite) {
      return !!tavoite.arvioinninkohteet && tavoite.arvioinninkohteet.length > 0;
    };

    $scope.addArviointi = function(tavoite) {
      tavoite.arvioinninkohteet = [{arvioinninKohde: {}, hyvanOsaamisenKuvaus: {}}];
    };

    var cloner = CloneHelper.init(['tavoite', 'sisaltoalueet', 'laajattavoitteet', 'arvioinninkohteet']);
    var idFn = function(item) { return item.id; };
    var filterFn = function(item) { return !item.$hidden; };

    $scope.tavoiteFn = {
      edit: function(tavoite) {
        tavoite.$editing = true;
        tavoite.$accordionOpen = true;
        $scope.currentEditable = tavoite;
        cloner.clone(tavoite);
      },
      remove: function($index) {
        $scope.model.tavoitteet.splice($index, 1);
        $scope.mapModel(true);
      },
      ok: function() {
        $rootScope.$broadcast('notifyCKEditor');
        $scope.currentEditable.$editing = false;
        $scope.currentEditable.$new = false;
        $scope.currentEditable = null;
        _.each($scope.model.tavoitteet, function(tavoite) {
          tavoite.sisaltoalueet = _(tavoite.$sisaltoalueet).filter(filterFn).map(idFn).value();
          tavoite.laajattavoitteet = _(tavoite.$osaaminen).filter(filterFn).map(idFn).value();
          tavoite.kohdealueet = _(tavoite.$kohdealueet).filter(filterFn).map(idFn).value();
          tavoite.kohdealueet = tavoite.$valittuKohdealue ? [tavoite.$valittuKohdealue.id] : [];
        });
      },
      cancel: function() {
        if (!$scope.currentEditable.$new) {
          cloner.restore($scope.currentEditable);
        } else {
          $scope.tavoiteFn.remove($scope.model.tavoitteet.length - 1);
        }
        $scope.currentEditable.$editing = false;
        $scope.currentEditable = null;
      },
      add: function() {
        var newTavoite = {$editing: true, tavoite: {}, $new: true, $accordionOpen: true};
        $scope.currentEditable = newTavoite;
        $scope.model.tavoitteet.push(newTavoite);
        $scope.mapModel(true);
      },
      toggle: function(tavoite) {
        tavoite.$accordionOpen = !tavoite.$accordionOpen;
      }
    };
  });
