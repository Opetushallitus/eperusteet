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
    PerusteTutkinnonosa, TutkinnonOsanTuonti, TutkinnonOsaEditMode) {

    $scope.peruste = perusteprojektiTiedot.getPeruste();
    $scope.suoritustapa = PerusteProjektiService.getSuoritustapa();
    $scope.tutkinnonOsat = [];
    $scope.editoi = false;
    $scope.naytaToisestaSuoritustavastaTuonti = perusteprojektiTiedot.getPeruste().suoritustavat.length > 1;
    $scope.yksikko = _.zipObject(_.map($scope.peruste.suoritustavat, 'suoritustapakoodi'),
                                  _.map($scope.peruste.suoritustavat, 'laajuusYksikko'));

    function haeTutkinnonosat() {
      PerusteenRakenne.haeTutkinnonosat($stateParams.perusteProjektiId, $scope.suoritustapa, function(res) {
        $scope.tutkinnonOsat = _.reject(res, function(r) { return r.poistettu; });
      });
    }
    haeTutkinnonosat();

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

    $scope.lisaaTutkinnonOsa = function(tyyppi) {
      var osa = tyyppi ? {tyyppi: tyyppi} : {};

      PerusteTutkinnonosa.save({
        perusteId: $scope.peruste.id,
        suoritustapa: $stateParams.suoritustapa
      }, osa,
      function(res) {
        $scope.tutkinnonOsat.unshift(res);
        TutkinnonOsaEditMode.setMode(true);
        $state.go('root.perusteprojekti.suoritustapa.perusteenosa', {
          perusteenOsaId: res._tutkinnonOsa,
          perusteenOsanTyyppi: 'tutkinnonosa',
          versio: ''
        });
      },
      function(err) {
        Notifikaatiot.serverCb(err);
      });
    };

    $scope.getHref = function(valittu) {
      return $state.href('root.perusteprojekti.suoritustapa.perusteenosa', { perusteenOsaId: valittu._tutkinnonOsa, perusteenOsanTyyppi: 'tutkinnonosa', versio: '' });
    };
  });
