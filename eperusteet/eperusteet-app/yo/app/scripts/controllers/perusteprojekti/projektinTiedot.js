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
  .controller('ProjektiTiedotSisaltoModalCtrl', function($scope, $modalInstance, PerusteProjektit, YleinenData, PerusteprojektiResource,
                                                         Notifikaatiot, Perusteet) {
    $scope.ominaisuudet = {};
    $scope.suoritustavat = [];
    $scope.nykyinen = 1;
    $scope.itemsPerPage = YleinenData.defaultItemsInModal;
    $scope.totalItems = 0;

    var dhaku = _.debounce(function(haku) {
      Perusteet.get({
        nimi: haku,
        // tila: 'valmis', FIXME ota joskus pois
        sivu: $scope.nykyinen - 1,
        sivukoko: $scope.itemsPerPage
      }, function(perusteet) {
        $scope.perusteet = perusteet.data;
        $scope.totalItems = perusteet['kokonaismäärä'];
        $scope.itemsPerPage = perusteet.sivukoko;
      });
    }, 300, { maxWait: 1000 });

    $scope.haku = function(haku) { dhaku(haku); };
    $scope.haku('');

    $scope.valitseSivu = function(sivu) {
      if (sivu > 0 && sivu <= Math.ceil($scope.totalItems / $scope.itemsPerPage)) {
        $scope.nykyinen = sivu;
      }
      $scope.haku($scope.syote);
    };

    $scope.takaisin = function() {
      $scope.projekti = null;
      $scope.peruste = null;
      $scope.ominaisuudet = {};
    };
    $scope.ok = function(peruste) {
      $modalInstance.close(peruste);
    };
    $scope.peruuta = function() { $modalInstance.dismiss(); };
  })
  .controller('ProjektinTiedotCtrl', function($scope, $state, $stateParams, $modal,
    PerusteprojektiResource, PerusteProjektiService, Navigaatiopolku, perusteprojektiTiedot, Notifikaatiot, Perusteet, Editointikontrollit) {
    PerusteProjektiService.watcher($scope, 'projekti');

    $scope.editEnabled = false;
    var originalProjekti = null;

    var editingCallbacks = {
      edit: function () { originalProjekti = PerusteProjektiService.get(); },
      save: function () { $scope.tallennaPerusteprojekti(); },
      validate: function () { return $scope.perusteprojektiForm.$valid; },
      cancel: function () { $scope.projekti = originalProjekti; },
      notify: function (mode) { $scope.editEnabled = mode; }
    };
    Editointikontrollit.registerCallback(editingCallbacks);

    $scope.wizardissa = function () {
      return $state.is('perusteprojektiwizard.tiedot');
    };

    $scope.voiMuokata = function () {
      // TODO Vain omistaja/sihteeri voi muokata
      return true;
    };

    $scope.muokkaa = function () {
      Editointikontrollit.startEditing();
    };

    PerusteProjektiService.clean();
    if ($state.current.name === 'perusteprojektiwizard.tiedot') {
      perusteprojektiTiedot.cleanData();
    }

    $scope.projekti = perusteprojektiTiedot.getProjekti();
    $scope.projekti.laajuusYksikko = $scope.projekti.laajuusYksikko || 'OSAAMISPISTE';

    Navigaatiopolku.asetaElementit({ perusteProjektiId: $scope.projekti.nimi });

    $scope.tabs = [{otsikko: 'projekti-perustiedot', url: 'views/partials/perusteprojekti/perusteprojektiPerustiedot.html'},
                   {otsikko: 'projekti-toimikausi', url: 'views/partials/perusteprojekti/perusteprojektiToimikausi.html'}];

    $scope.mergeProjekti = function() {
      $modal.open({
        templateUrl: 'views/modals/projektiSisaltoTuonti.html',
        controller: 'ProjektiTiedotSisaltoModalCtrl',
        resolve: {
          projekti: function() { return $scope.projekti; }
        }
      })
      .result.then(function(peruste) {
        $scope.peruste = peruste;
        var onOps = false;
        $scope.projekti.koulutustyyppi = peruste.tutkintokoodi;
        $scope.projekti.perusteId = peruste.id;

        _.forEach(peruste.suoritustavat, function(st) {
          if (st.suoritustapakoodi === 'ops') {
            onOps = true;
            $scope.projekti.laajuusYksikko = st.laajuusYksikko;
          }
        });
      },
      angular.noop);
    };

    $scope.puhdistaValinta = function() {
      delete $scope.peruste;
      delete $scope.projekti.perusteId;
    };

    $scope.tallennaPerusteprojekti = function() {
      var projekti = PerusteProjektiService.get();
      if (projekti.id) {
        delete projekti.koulutustyyppi;
        delete projekti.laajuusYksikko;
      }
      else { projekti.id = null; }

      PerusteprojektiResource.update(projekti, function(vastaus) {
        PerusteProjektiService.save(vastaus);
        PerusteProjektiService.update();
        if ($scope.wizardissa()) {
          avaaProjektinSisalto(vastaus.id, vastaus._peruste);
        }
        else {
          Notifikaatiot.onnistui('tallennettu');
        }
      }, Notifikaatiot.serverCb);
    };

    var avaaProjektinSisalto = function(projektiId, perusteId) {
      Perusteet.get({
        perusteId: perusteId
      }, function(res) {
        console.log(res);
        $state.go('perusteprojekti.suoritustapa.sisalto', {
          perusteProjektiId: projektiId,
          suoritustapa: res.suoritustavat[0].suoritustapakoodi
        }, { reload: true });
      });
    };
  });
