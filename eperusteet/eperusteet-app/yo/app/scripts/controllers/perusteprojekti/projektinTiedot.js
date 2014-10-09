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
  .controller('ProjektiTiedotSisaltoModalCtrl', function($scope, $modalInstance, YleinenData, PerusteprojektiResource,
                                                         Notifikaatiot, Perusteet, pohja) {
    $scope.ominaisuudet = {};
    $scope.suoritustavat = [];
    $scope.nykyinen = 1;
    $scope.itemsPerPage = YleinenData.defaultItemsInModal;
    $scope.totalItems = 0;

    var dhaku = _.debounce(function(haku) {
      Perusteet.get({
        nimi: haku,
        sivu: $scope.nykyinen - 1,
        sivukoko: $scope.itemsPerPage,
        tila: 'valmis',
        perusteTyyppi: pohja ? 'pohja' : 'normaali',
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
  .controller('ProjektinTiedotCtrl', function($scope, $state, $stateParams, $modal, $timeout, $translate,
    PerusteprojektiResource, PerusteProjektiService, Navigaatiopolku, perusteprojektiTiedot, Notifikaatiot,
    Perusteet, Editointikontrollit) {
    PerusteProjektiService.watcher($scope, 'projekti');

    $scope.lang = $translate.use() || $translate.preferredLanguage();
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

    $scope.pohja = function () {
      return $state.is('root.perusteprojektiwizard.pohja') || ($scope.peruste && $scope.peruste.tyyppi === 'pohja');
    };
    $scope.wizardissa = function () {
      return $state.is('root.perusteprojektiwizard.tiedot') || $state.is('root.perusteprojektiwizard.pohja');
    };

    $scope.voiMuokata = function () {
      // TODO Vain omistaja/sihteeri voi muokata
      return true;
    };

    $scope.muokkaa = function () {
      Editointikontrollit.startEditing();
    };

    $scope.puhdistaValinta = function() {
      PerusteProjektiService.clean();
      if ($scope.wizardissa()) {
        perusteprojektiTiedot.cleanData();
        $scope.peruste = undefined;
        $scope.projekti = {};
      }
    };
    $scope.puhdistaValinta();


    $scope.projekti = perusteprojektiTiedot.getProjekti();
    $scope.projekti.laajuusYksikko = $scope.projekti.laajuusYksikko || 'OSAAMISPISTE';

    Navigaatiopolku.asetaElementit({ perusteProjektiId: $scope.projekti.nimi });

    $scope.tabs = [{otsikko: 'projekti-perustiedot', url: 'views/partials/perusteprojekti/perustiedot.html'}];
    if (!$scope.pohja()) {
      $scope.tabs.push({otsikko: 'projekti-toimikausi', url: 'views/partials/perusteprojekti/toimikausi.html'});
    }

    $scope.haeRyhma = function() {
      $modal.open({
        templateUrl: 'views/modals/tuotyoryhma.html',
        controller: 'TyoryhmanTuontiModalCtrl'
      })
      .result.then(function(ryhma) {
        $scope.projekti.ryhmaOid = ryhma.oid;
        $scope.projekti.$ryhmaNimi = ryhma.nimi;
      });
    };

    $scope.mergeProjekti = function(tuoPohja) {
      $modal.open({
        templateUrl: 'views/modals/projektiSisaltoTuonti.html',
        controller: 'ProjektiTiedotSisaltoModalCtrl',
        resolve: {
          pohja: function() { return !!tuoPohja; },
        }
      })
      .result.then(function(peruste) {
        peruste.tila = 'laadinta';
        peruste.tyyppi = 'normaali';
        $scope.peruste = peruste;
        var onOps = false;
        $scope.projekti.perusteId = peruste.id;
        $scope.projekti.koulutustyyppi = peruste.koulutustyyppi;

        _.forEach(peruste.suoritustavat, function(st) {
          if (st.suoritustapakoodi === 'ops') {
            onOps = true;
            $scope.projekti.laajuusYksikko = st.laajuusYksikko;
          }
        });
      },
      angular.noop);
    };


    $scope.tallennaPerusteprojekti = function() {
      var projekti = PerusteProjektiService.get();
      if (projekti.id) {
        delete projekti.koulutustyyppi;
        delete projekti.laajuusYksikko;
      }
      else { projekti.id = null; }

      if ($scope.pohja()) {
        projekti = _.merge(_.pick(projekti, 'id', 'nimi', 'koulutustyyppi', 'ryhmaOid'), {
          tyyppi: 'pohja'
        });
      }

      PerusteprojektiResource.update(projekti, function(vastaus) {
        $scope.puhdistaValinta();
        if ($scope.wizardissa()) {
          PerusteProjektiService.goToProjektiState(vastaus, projekti);
        }
        else {
          Notifikaatiot.onnistui('tallennettu');
        }
      }, Notifikaatiot.serverCb);
    };
  })
  .controller('TyoryhmanTuontiModalCtrl', function($scope, $modalInstance, $translate, Organisaatioryhmat, Algoritmit) {
    $scope.haetaan = true;
    $scope.moro = 'moro';
    $scope.error = false;
    $scope.rajaus = '';
    $scope.lang = 'nimi.' + $translate.use() || $translate.preferredLanguage();

    $scope.totalItems = 0;
    $scope.itemsPerPage = 10;
    $scope.nykyinen = 1;

    Organisaatioryhmat.get(function(res) {
      $scope.haetaan = false;
      $scope.ryhmat = _(res).value();
      $scope.totalItems = _.size($scope.ryhmat);
    }, function() {
      $scope.haetaan = false;
      $scope.error = true;
    });

    $scope.paivitaRajaus = function(rajaus) {
      $scope.rajaus = rajaus;
    };

    $scope.rajaa = function(ryhma) {
      return Algoritmit.match($scope.rajaus, ryhma.nimi);
    };

    $scope.valitseSivu = function(sivu) {
      if (sivu > 0 && sivu <= Math.ceil($scope.totalItems / $scope.itemsPerPage)) {
        $scope.nykyinen = sivu;
      }
    };

    $scope.valitse = $modalInstance.close;
    $scope.peruuta = $modalInstance.dismiss;
  });
