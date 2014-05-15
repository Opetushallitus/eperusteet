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
  .config(function($stateProvider) {
    $stateProvider
      .state('perusteprojekti.editoi.sisalto', {
        url: '/sisalto',
        templateUrl: 'views/partials/perusteprojektiSisalto.html',
        controller: 'PerusteprojektisisaltoCtrl',
        naviRest: ['sisältö'],
        onEnter: ['SivunavigaatioService', function (SivunavigaatioService) {
          SivunavigaatioService.aseta({piilota: true});
        }]
      });
  })
  .controller('PerusteprojektisisaltoCtrl', function($scope, $stateParams, $translate, Kaanna,
    PerusteprojektiResource, Suoritustapa, SuoritustapaSisalto, PerusteProjektiService,
    Perusteet, PerusteenOsaViitteet, Varmistusdialogi, Notifikaatiot) {
     $scope.projekti = {};
     $scope.peruste = {};
     $scope.valittuSuoritustapa = '';
     $scope.poistoMouseLeaveLuokka = 'glyphicon glyphicon-remove pull-right smaller';
     $scope.poistoMouseOverLuokka = 'glyphicon glyphicon-remove pull-right larger';

    if ($stateParams.perusteProjektiId !== 'uusi') {
      $scope.projekti.id = $stateParams.perusteProjektiId;
      PerusteprojektiResource.get({id: $stateParams.perusteProjektiId}, function(vastaus) {
        $scope.projekti = vastaus;
        Perusteet.get({perusteenId: vastaus._peruste}, function(vastaus) {
          $scope.peruste = vastaus;
          if ($scope.peruste.suoritustavat !== null && $scope.peruste.suoritustavat.length > 0) {
            $scope.peruste.suoritustavat = _.sortBy($scope.peruste.suoritustavat, 'suoritustapakoodi');
            $scope.vaihdaSuoritustapa(PerusteProjektiService.getSuoritustapa() === '' ? vastaus.suoritustavat[0].suoritustapakoodi : PerusteProjektiService.getSuoritustapa());
          }
        }, function(virhe) {
          console.log('perusteen haku virhe', virhe);
        });

      }, function(virhe) {
        console.log('virhe', virhe);
      });

    } else {
      $scope.projekti.id = 'uusi';
      //Navigaatiopolku.asetaElementit({ perusteProjektiId: 'uusi' });
    }

    var haeSisalto = function(suoritustapa) {
      Suoritustapa.get({perusteenId: $scope.projekti._peruste, suoritustapa: suoritustapa}, function(vastaus) {
        $scope.peruste.sisalto = vastaus;
        $scope.valittuSuoritustapa = suoritustapa;
        console.log('suoritustapa sisältö', vastaus);
      }, function(virhe) {
        $scope.valittuSuoritustapa = '';
        console.log('suoritustapasisältöä ei löytynyt', virhe);
      });
    };

    $scope.createSisalto = function () {
      SuoritustapaSisalto.save({perusteId: $scope.projekti._peruste, suoritustapa: PerusteProjektiService.getSuoritustapa()}, {}, function(vastaus) {
        haeSisalto(PerusteProjektiService.getSuoritustapa());
        console.log('uusi suoritustapa sisältö', vastaus);
      }, function (virhe) {
        console.log('Uuden sisällön luontivirhe', virhe);
      });
    };

    $scope.vaihdaSuoritustapa = function(suoritustapakoodi) {
      $scope.valittuSuoritustapa = suoritustapakoodi;
      PerusteProjektiService.setSuoritustapa(suoritustapakoodi);
      haeSisalto($scope.valittuSuoritustapa);
    };

    $scope.setLargerSize = function (event) {
      event.currentTarget.className = $scope.poistoMouseOverLuokka;
    };

    $scope.setSmallerSize = function (event) {
      event.currentTarget.className = $scope.poistoMouseLeaveLuokka;
    };

    $scope.poistaSisalto = function(viiteId, nimi, event) {
      event.stopPropagation();
      nimi = Kaanna.kaanna(nimi);

      Varmistusdialogi.dialogi({
        successCb: poistaminenVarmistettu,
        otsikko: 'poista-tekstikappale-otsikko',
        teksti: $translate('poista-tekstikappale-teksti', {nimi: nimi}),
        data: viiteId
      })();
    };

    var poistaminenVarmistettu = function(viiteId) {
      PerusteenOsaViitteet.delete({viiteId: viiteId}, {}, function() {
        Notifikaatiot.onnistui('poisto-onnistui');
        haeSisalto(PerusteProjektiService.getSuoritustapa());
      }, function(virhe) {
        Notifikaatiot.varoitus(virhe);
      });
    };


  });
