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
  .config(function($stateProvider) {
    $stateProvider
      .state('root.admin', {
        url: '/admin',
        templateUrl: 'views/admin/base.html',
        controller: 'AdminBaseController',
      })
      .state('root.admin.perusteprojektit', {
        url: '/perusteprojektit',
        templateUrl: 'views/admin/perusteprojektit.html',
        controller: 'AdminPerusteprojektitController'
      })
      .state('root.admin.tiedotteet', {
        url: '/tiedotteet',
        templateUrl: 'views/admin/tiedotteet.html',
        controller: 'TiedotteidenHallintaController'
      });
  })

  .controller('AdminBaseController', function ($scope, $state) {
    $scope.tabs = [
      {label: 'perusteprojektit', state: 'root.admin.perusteprojektit'},
      {label: 'tiedotteet', state: 'root.admin.tiedotteet'}
    ];

    $scope.chooseTab = function ($index) {
      _.each($scope.tabs, function (item, index) {
        item.$tabActive = index === $index;
      });
      var state = $scope.tabs[$index];
      if (state) {
        $state.go(state.state);
      }
    };

    if ($state.current.name === 'root.admin') {
      $scope.chooseTab(0);
    } else {
      _.each($scope.tabs, function (item) {
        item.$tabActive = item.state === $state.current.name;
      });
    }
  })

  .controller('AdminPerusteprojektitController', function($rootScope, $scope, PerusteProjektit,
      Algoritmit, PerusteprojektiTila, Notifikaatiot, Kaanna, YleinenData, Varmistusdialogi,
      PerusteProjektiService, Utils) {
    $scope.jarjestysTapa = 'nimi';
    $scope.jarjestysOrder = false;
    $scope.tilaRajain = null;
    $scope.tilat = [];
    $scope.filteredPp = [];
    $scope.itemsPerPage = 10;
    $scope.nykyinen = 1;
    $scope.alaraja = 0;
    $scope.ylaraja = $scope.alaraja + $scope.itemsPerPage;

    $scope.asetaJarjestys = (tyyppi, suunta) => {
      if ($scope.jarjestysTapa === tyyppi) {
        $scope.jarjestysOrder = !$scope.jarjestysOrder;
        suunta = $scope.jarjestysOrder;
      }
      else {
        $scope.jarjestysOrder = false;
        $scope.jarjestysTapa = tyyppi;
      }
    };

    $scope.jarjestys = (data) =>{
      switch($scope.jarjestysTapa) {
        case 'nimi': return Utils.nameSort(data);
        case 'haltija': return data.haltija;
        case 'diaarinumero': return data.diaarinumero;
        case 'tila': return data.tila;
        default:
          break;
      }
    };

    $scope.valitseSivu = (sivu) => {
      if (sivu > 0 && sivu <= Math.ceil(_.size($scope.filteredPp) / $scope.itemsPerPage)) {
        $scope.nykyinen = sivu;
        $scope.alaraja = $scope.itemsPerPage * (sivu - 1);
        $scope.ylaraja = $scope.alaraja + $scope.itemsPerPage;
      }
    };

    PerusteProjektit.perusteHaku({}, (res) => {
      let mahdollisetTilat = {};
      angular.forEach(res, (projekti) => {
        projekti.suoritustapa = YleinenData.valitseSuoritustapaKoulutustyypille(projekti.koulutustyyppi);
        projekti.$url = PerusteProjektiService.getUrl(projekti);
        mahdollisetTilat[projekti.tila] = true;
      });
      $scope.tilat = _.keys(mahdollisetTilat);
      $scope.perusteprojektit = res;
    });

    $scope.palauta = (pp) => {
      const uusiTila = 'laadinta';
      Varmistusdialogi.dialogi({
        otsikko: Kaanna.kaanna('vahvista-palautus'),
        teksti: Kaanna.kaanna('vahvista-palautus-sisältö', {
          nimi: pp.nimi,
          tila: Kaanna.kaanna('tila-' + uusiTila)
        })
      })(() => {
        PerusteprojektiTila.save({ id: pp.id, tila: uusiTila }, {}, (vastaus) => {
          if (vastaus.vaihtoOk) { pp.tila = uusiTila; }
          else { Notifikaatiot.varoitus('tilan-vaihto-epaonnistui'); }
        }, Notifikaatiot.serverCb);
      });
    };

    $scope.rajaaSisaltoa = (pp) => {
      return (!$scope.tilaRajain || $scope.tilaRajain === pp.tila) && (_.isEmpty($scope.rajaus) ||
              Algoritmit.match($scope.rajaus, pp.nimi) ||
              Algoritmit.match($scope.rajaus, 'tila-' + pp.tila) ||
              (_.isEmpty(pp.perusteendiaarinumero) ? false : Algoritmit.match($scope.rajaus, pp.perusteendiaarinumero)) ||
              Algoritmit.match($scope.rajaus, pp.diaarinumero));
    };
  });
