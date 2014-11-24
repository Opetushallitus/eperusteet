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
  .controller('TuoTekstikappale', function($scope, $modalInstance, Notifikaatiot, peruste,
      suoritustapa, PerusteenRakenne, SuoritustapaSisalto, YleinenData, Perusteet, Algoritmit, Kaanna) {
    $scope.perusteet = [];
    $scope.sivuja = 0;
    $scope.sivu = 0;
    $scope.valittuPeruste = null;
    $scope.kaikkiValittu = null;
    $scope.valitut = 0;
    $scope.search = {
      term: '',
      changed: function () {
        $scope.paginate.current = 1;
      },
      filterFn: function (item) {
        return Algoritmit.match($scope.search.term, item.perusteenOsa.nimi);
      }
    };

    $scope.paginate = {
      perPage: 10,
      current: 1,
    };

    $scope.orderFn = function (item) {
      return Kaanna.kaanna(item.perusteenOsa.nimi).toLowerCase();
    };

    $scope.updateTotal = function () {
      $scope.valitut = _.size(_.filter($scope.sisalto, '$valittu'));
    };

    $scope.toggleKaikki = function(valinta) {
      _.each($scope.sisalto, function(tulos) {
        tulos.$valittu = false;
        if ($scope.search.term) {
          if ($scope.search.filterFn(tulos)) {
            tulos.$valittu = valinta;
          }
        } else {
          tulos.$valittu = valinta;
        }
      });
      $scope.updateTotal();
    };

    $scope.haku = function(haku) {
      PerusteenRakenne.haePerusteita(haku, function(res) {
        $scope.perusteet = res.data;
        $scope.sivuja = res.sivuja;
        $scope.sivu = res.sivu;
      });
    };
    $scope.haku('');

    $scope.valitse = function(valittuPeruste) {
      $scope.valittuPeruste = valittuPeruste;
      Perusteet.get({perusteId: valittuPeruste.id}, function (res) {
        SuoritustapaSisalto.get({
          perusteId: valittuPeruste.id,
          suoritustapa: YleinenData.validSuoritustapa(res, suoritustapa)
        }, function(res) {
          $scope.sisalto = _.reject(res.lapset, function(lapsi) {
            return lapsi.perusteenOsa.tunniste === 'rakenne';
          });
        }, Notifikaatiot.serverCb);
      }, Notifikaatiot.serverCb);
    };

    $scope.takaisin = function() {
      $scope.valittuPeruste = null;
      $scope.search.term = '';
      $scope.paginate.current = 1;
      $scope.valitut = 0;
    };
    $scope.peru = function() { $modalInstance.dismiss(); };
    $scope.ok = function() {
      $modalInstance.close(_.filter($scope.sisalto, function(s) { return s.$valittu; }));
    };
  });
