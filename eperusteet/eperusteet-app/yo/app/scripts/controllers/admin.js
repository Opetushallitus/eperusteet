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
        templateUrl: 'views/admin.html',
        controller: 'AdminCtrl',
      });
  })
  .controller('AdminCtrl', function($rootScope, $scope, PerusteProjektit, Algoritmit, PerusteprojektiTila, Notifikaatiot,
                                    Kaanna, YleinenData, Varmistusdialogi) {
    $scope.jarjestysTapa = 'nimi';
    $scope.jarjestysOrder = false;
    $scope.tilaRajain = null;
    $scope.tilat = YleinenData.tilakuvaukset;
    $scope.filteredPp = [];
    $scope.itemsPerPage = 10;
    $scope.nykyinen = 1;
    $scope.alaraja = 0;
    $scope.ylaraja = $scope.alaraja + $scope.itemsPerPage;

    $scope.asetaJarjestys = function(tyyppi, suunta) {
      if ($scope.jarjestysTapa === tyyppi) {
        $scope.jarjestysOrder = !$scope.jarjestysOrder;
        suunta = $scope.jarjestysOrder;
      }
      else {
        $scope.jarjestysOrder = false;
        $scope.jarjestysTapa = tyyppi;
      }
    };
    $scope.jarjestys = function(data) {
      switch($scope.jarjestysTapa) {
        case 'nimi': return Kaanna.kaanna(data.nimi);
        case 'haltija': return data.haltija;
        case 'diaarinumero': return data.diaarinumero;
        case 'tila': return data.tila;
        default:
          break;
      }
    };

    $scope.valitseSivu = function(sivu) {
      if (sivu > 0 && sivu <= Math.ceil(_.size($scope.filteredPp) / $scope.itemsPerPage)) {
        $scope.nykyinen = sivu;
        $scope.alaraja = $scope.itemsPerPage * (sivu - 1);
        $scope.ylaraja = $scope.alaraja + $scope.itemsPerPage;
      }
    };

    PerusteProjektit.hae({}, function(res) {
      angular.forEach(res, function(projekti) {
        projekti.suoritustapa = YleinenData.valitseSuoritustapaKoulutustyypille(projekti.koulutustyyppi);
      });
      $scope.perusteprojektit = res;
    });

    $scope.palauta = function(pp) {
      var uusiTila = 'laadinta';
      Varmistusdialogi.dialogi({
        otsikko: Kaanna.kaanna('vahvista-palautus'),
        teksti: Kaanna.kaanna('vahvista-palautus-sisältö', {
          nimi: pp.nimi,
          tila: Kaanna.kaanna('tila-' + uusiTila)
        })
      })(function() {
        PerusteprojektiTila.save({ id: pp.id, tila: uusiTila }, {}, function(vastaus) {
          if (vastaus.vaihtoOk) { pp.tila = uusiTila; }
          else { Notifikaatiot.varoitus('tilan-vaihto-epaonnistui'); }
        }, Notifikaatiot.serverCb);
      });
    };

    $scope.rajaaSisaltoa = function(pp) {
      return (!$scope.tilaRajain || $scope.tilaRajain === pp.tila) && (_.isEmpty($scope.rajaus) ||
              Algoritmit.match($scope.rajaus, pp.nimi) ||
              Algoritmit.match($scope.rajaus, 'tila-' + pp.tila) ||
              Algoritmit.match($scope.rajaus, pp.diaarinumero));
    };
  });
