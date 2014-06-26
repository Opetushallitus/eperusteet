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

angular.module('eperusteApp')
  .controller('ProjektinTiedotCtrl', function($scope, $state,
    PerusteprojektiResource, PerusteProjektiService, Navigaatiopolku,
    perusteprojektiTiedot, Notifikaatiot, Perusteet, Editointikontrollit) {
    PerusteProjektiService.watcher($scope, 'projekti');

    $scope.editEnabled = false;
    var originalProjekti = null;

    var editingCallbacks = {
      edit: function () {
        originalProjekti = PerusteProjektiService.get();
      },
      save: function () {
        $scope.tallennaPerusteprojekti();
      },
      validate: function () {
        return $scope.perusteprojektiForm.$valid;
      },
      cancel: function () {
        $scope.projekti = originalProjekti;
      },
      notify: function (mode) {
        $scope.editEnabled = mode;
      }
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
    $scope.projekti.yksikko = $scope.projekti.yksikko || 'OSAAMISPISTE';

    Navigaatiopolku.asetaElementit({ perusteProjektiId: $scope.projekti.nimi });


    $scope.tabs = [{otsikko: 'projekti-perustiedot', url: 'views/partials/perusteprojekti/perusteprojektiPerustiedot.html'},
                   {otsikko: 'projekti-toimikausi', url: 'views/partials/perusteprojekti/perusteprojektiToimikausi.html'}];

    $scope.tallennaPerusteprojekti = function() {
      var projekti = PerusteProjektiService.get();
      if (projekti.id) {
        delete projekti.koulutustyyppi;
        delete projekti.yksikko;
      }
      else { projekti.id = null; }

      PerusteprojektiResource.update(projekti, function(vastaus) {
        PerusteProjektiService.save(vastaus);
        PerusteProjektiService.update();
        if ($scope.wizardissa()) {
          avaaProjektinSisalto(vastaus.id, vastaus._peruste);
        } else {
          Notifikaatiot.onnistui('tallennettu');
        }
      }, Notifikaatiot.serverCb);
    };

    var avaaProjektinSisalto = function(projektiId, perusteId) {
      Perusteet.get({ perusteId: perusteId }, function(res) {
        $state.go('perusteprojekti.suoritustapa.sisalto', {
          perusteProjektiId: projektiId,
          suoritustapa: res.suoritustavat[0].suoritustapakoodi
        }, { reload: true });
      });
    };
  });
