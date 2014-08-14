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
  .service('Preferenssit', function () {
    /* TODO: Käyttäjän preferenssit keksiin tai käyttäjäprofiiliin? */
    this.data = {
      nakymatyyli: 'palikka'
    };
  })
  .controller('PerusteprojektiTutkinnonOsatCtrl', function($scope, $state, $stateParams,
    perusteprojektiTiedot, PerusteProjektiService, PerusteenRakenne, Notifikaatiot,
    Kaanna, PerusteTutkinnonosa, TutkinnonOsanTuonti, TutkinnonOsaEditMode, Algoritmit,
    Preferenssit) {

    $scope.peruste = perusteprojektiTiedot.getPeruste();
    $scope.suoritustapa = PerusteProjektiService.getSuoritustapa();
    $scope.tutkinnonOsat = [];
    $scope.tosarajaus = '';
    $scope.editoi = false;
    $scope.preferenssit = Preferenssit.data;
    $scope.jarjestysTapa = 'nimi';
    $scope.jarjestysOrder = false;
    $scope.naytaToisestaSuoritustavastaTuonti = perusteprojektiTiedot.getPeruste().suoritustavat.length > 1;
    $scope.yksikko = _.zipObject(_.map($scope.peruste.suoritustavat, 'suoritustapakoodi'),
                                  _.map($scope.peruste.suoritustavat, 'laajuusYksikko'))
                                  [$scope.suoritustapa];

    $scope.paivitaRajaus = function(rajaus) { $scope.tosarajaus = rajaus; };
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
        case 'laajuus': return data.laajuus;
        case 'muokattu': return data.muokattu;
        default:
          break;
      }
    };

    function haeTutkinnonosat() {
      PerusteenRakenne.haeTutkinnonosat($stateParams.perusteProjektiId, $scope.suoritustapa, function(res) {
        $scope.tutkinnonOsat = _.reject(res, function(r) { return r.poistettu; });
      });
    }
    haeTutkinnonosat();

    $scope.rajaaTutkinnonOsia = function(haku) {
      return Algoritmit.rajausVertailu($scope.tosarajaus, haku, 'nimi');
    };

    $scope.tuoSuoritustavasta = TutkinnonOsanTuonti.suoritustavoista(perusteprojektiTiedot.getPeruste(), $scope.suoritustapa, function(osat) {
      _.forEach(osat, function(osa) { $scope.lisaaTutkinnonOsaSuoraan(osa); });
    });

    $scope.tuoTutkinnonosa = TutkinnonOsanTuonti.kaikista($scope.suoritustapa, function(osat) {
      _.forEach(osat, function(osa) {
        delete osa.id;
        $scope.lisaaTutkinnonOsaSuoraan(osa);
      });
    });

    $scope.lisaaTutkinnonOsaSuoraan = function(osa) {
      PerusteTutkinnonosa.save({
        perusteId: $scope.peruste.id,
        suoritustapa: $stateParams.suoritustapa
      }, osa,
      function(res) {
        $scope.tutkinnonOsat.unshift(res);
      }, Notifikaatiot.serverCb);
    };

    $scope.lisaaTutkinnonOsa = function(osa, cb) {
      osa = osa ? {_tutkinnonOsa: osa._tutkinnonOsa} : {};
      cb = cb || angular.noop;

      PerusteTutkinnonosa.save({
        perusteId: $scope.peruste.id,
        suoritustapa: $stateParams.suoritustapa
      }, osa,
      function(res) {
        $scope.tutkinnonOsat.unshift(res);
        cb();
        TutkinnonOsaEditMode.setMode(true);
        $state.go('root.perusteprojekti.suoritustapa.perusteenosa', {
          perusteenOsaId: res._tutkinnonOsa,
          perusteenOsanTyyppi: 'tutkinnonosa'
        });
      },
      function(err) {
        Notifikaatiot.serverCb(err);
        cb();
      });
    };

    $scope.getHref = function(valittu) {
      var url = $state.href('root.perusteprojekti.suoritustapa.perusteenosa', { perusteenOsaId: valittu._tutkinnonOsa, perusteenOsanTyyppi: 'tutkinnonosa' });
      console.log(url);
      return url;
    };
  });
