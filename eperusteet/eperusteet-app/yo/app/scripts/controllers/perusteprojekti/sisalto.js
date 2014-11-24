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
  .controller('PerusteprojektisisaltoCtrl', function($scope, $state, $stateParams, $timeout,
    $modal, PerusteenOsat, PerusteenOsaViitteet, SuoritustapaSisalto, PerusteProjektiService,
    perusteprojektiTiedot, TutkinnonOsaEditMode, Notifikaatiot, Kaanna, Algoritmit,
    Editointikontrollit, TEXT_HIERARCHY_MAX_DEPTH, PerusteProjektiSivunavi, Projektiryhma,
    PerusteprojektiTyoryhmat, TekstikappaleOperations) {
    $scope.textMaxDepth = TEXT_HIERARCHY_MAX_DEPTH;

    function lisaaSisalto(method, sisalto, cb) {
      cb = cb || angular.noop;
      SuoritustapaSisalto[method]({
        perusteId: $scope.projekti._peruste,
        suoritustapa: PerusteProjektiService.getSuoritustapa()
      }, sisalto, cb, Notifikaatiot.serverCb);
    }

    $scope.rajaus = '';
    $scope.projekti = perusteprojektiTiedot.getProjekti();
    $scope.peruste = perusteprojektiTiedot.getPeruste();
    TekstikappaleOperations.setPeruste($scope.peruste);
    $scope.peruste.sisalto = perusteprojektiTiedot.getSisalto();
    $scope.valittuSuoritustapa = PerusteProjektiService.getSuoritustapa();
    $scope.naytaTutkinnonOsat = true;
    $scope.naytaRakenne = true;
    $scope.muokkausTutkintokohtaisetOsat = false;
    $scope.tyyppi = 'kaikki';
    $scope.tyoryhmaMap = {};
    $scope.tiivistelma = Kaanna.kaanna($scope.peruste.kuvaus);

    if (_.size($scope.peruste.sisalto) > 1 && _.first($scope.peruste.suoritustavat).suoritustapakoodi !== 'ops') {
      $scope.peruste.suoritustavat = _.arraySwap($scope.peruste.suoritustavat, 0, 1);
    }

    PerusteprojektiTyoryhmat.getAll({ id: $stateParams.perusteProjektiId }, function(res) {
      var tyoryhmaMap = {};
      _.each(_.sortBy(res, 'nimi'), function(tr) {
        if (!_.isArray(tyoryhmaMap[tr._perusteenosa])) {
          tyoryhmaMap[tr._perusteenosa] = [];
        }
        tyoryhmaMap[tr._perusteenosa].push(tr.nimi);
      });
      tyoryhmaMap.$resolved = true;
      $scope.tyoryhmaMap = tyoryhmaMap;
    });

    function korjaaLapsi(lapsi) {
      switch (lapsi.perusteenOsa.tunniste) {
        case 'rakenne':
          lapsi.$url = $state.href('root.perusteprojekti.suoritustapa.muodostumissaannot');
          lapsi.$type = 'ep-tree';
          break;
        default:
          lapsi.$url = $state.href('root.perusteprojekti.suoritustapa.perusteenosa', { perusteenOsanTyyppi: 'tekstikappale', perusteenOsaViiteId: lapsi.id, versio: '' });
      }
    }

    Algoritmit.kaikilleLapsisolmuille($scope.peruste.sisalto, 'lapset', korjaaLapsi);

    $scope.aakkosJarjestys = function(data) { return Kaanna.kaanna(data.perusteenOsa.nimi); };

    $scope.filterJasen = function(jasen) { return $scope.tyyppi === 'kaikki' || $scope.tyoryhmat[$scope.tyyppi][jasen.oidHenkilo]; };
    $scope.filterRyhma = function(ryhma) { return _.some(ryhma, $scope.filterJasen); };
    $scope.naytaRyhmanHenkilot = function(tyyppi, tyoryhmat, ryhma) {
      $modal.open({
        template: '' +
          '<div class="modal-header"><h2 kaanna>tyoryhma</h2></div>' +
          '<div class="modal-body">' +
          '  <projektiryhma-henkilot tyoryhmat="tyoryhmat" ryhma="ryhma" tyyppi="tyyppi" ng-if="!lataa && !error"></projektiryhma-henkilot>' +
          '</div>' +
          '<div class="modal-footer">' +
          '  <button class="btn btn-primary" ng-click="ok()" kaanna>sulje</button>' +
          '</div>',
        controller: function($scope, $modalInstance, tyoryhmat, ryhma) {
          $scope.tyyppi = tyyppi;
          $scope.tyoryhmat = tyoryhmat;
          $scope.ryhma = ryhma;
          $scope.ok = $modalInstance.dismiss;
        },
        resolve: {
          tyyppi: function() { return tyyppi; },
          tyoryhmat: function() { return tyoryhmat; },
          ryhma: function() { return ryhma; },
        }
      });
    };

    $scope.rajaaSisaltoa = function(value, tyyppi) {
      if (_.isUndefined(value)) { return; }
      $scope.tyyppi = tyyppi;
      Algoritmit.kaikilleTutkintokohtaisilleOsille($scope.peruste.sisalto, function(osa, lapsellaOn) {
        var tyoryhmat = osa.perusteenOsa ? $scope.tyoryhmaMap[osa.perusteenOsa.id] || [] : [];
        var omistaaTyoryhman = tyyppi === 'kaikki' || tyoryhmat.indexOf(tyyppi) !== -1;
        osa.$filtered = (lapsellaOn || Algoritmit.rajausVertailu(value, osa, 'perusteenOsa', 'nimi')) && omistaaTyoryhman;
        return osa.$filtered;
      });
      $scope.naytaTutkinnonOsat = Kaanna.kaanna('tutkinnonosat').toLowerCase().indexOf(value.toLowerCase()) !== -1;
      $scope.naytaRakenne = Kaanna.kaanna('tutkinnon-rakenne').toLowerCase().indexOf(value.toLowerCase()) !== -1;
    };

    Projektiryhma.jasenetJaTyoryhmat($stateParams.perusteProjektiId, function(re) {
      $scope.jasenet = _.zipObject(_.map(re.jasenet, 'oidHenkilo'), re.jasenet);
      $scope.ryhma = re.ryhma;
      $scope.tyoryhmat = re.tyoryhmat;
      $scope.lataa = false;
    });

    $scope.tuoSisalto = function() {
      function lisaaLapset(parent, lapset, cb) {
        cb = cb || angular.noop;
        lapset = lapset || [];
        if (_.isEmpty(lapset)) { cb(); return; }

        var lapsi = _.first(lapset);
        SuoritustapaSisalto.addChild({
          perusteId: $scope.projekti._peruste,
          suoritustapa: PerusteProjektiService.getSuoritustapa(),
          perusteenosaViiteId: parent.id,
          childId: lapsi.perusteenOsa.id
        }, {}, function(res) {
          lisaaLapset(res, lapsi.lapset, function() {
            parent.lapset = parent.lapset || [];
            parent.lapset.push(lapsi);
            lisaaLapset(parent, _.rest(lapset), cb);
          });
        });
      }

      $modal.open({
        templateUrl: 'views/modals/tuotekstikappale.html',
        controller: 'TuoTekstikappale',
        size: 'lg',
        resolve: {
          peruste: function() { return $scope.peruste; },
          suoritustapa: function() { return PerusteProjektiService.getSuoritustapa(); },
        }
      })
      .result.then(function(lisattavaSisalto) {
        Algoritmit.asyncTraverse(lisattavaSisalto, function(lapsi, next) {
          lisaaSisalto('add', { _perusteenOsa: lapsi.perusteenOsa.id }, function(pov) {
            PerusteenOsat.get({
              osanId: pov._perusteenOsa
            }, function(po) {
              pov.perusteenOsa = po;
              lisaaLapset(pov, lapsi.lapset, function() {
                korjaaLapsi(pov);
                $scope.peruste.sisalto.lapset.push(pov);
                next();
              });
            });
          });
        }, function() { Notifikaatiot.onnistui('tekstikappaleiden-tuonti-onnistui'); });
      });
    };

    $scope.createSisalto = function() {
      lisaaSisalto('save', {}, function(response) {
        TutkinnonOsaEditMode.setMode(true); // Uusi luotu, siirry suoraan muokkaustilaan
        $state.go('root.perusteprojekti.suoritustapa.perusteenosa', {
          perusteenOsanTyyppi: 'tekstikappale',
          perusteenOsaViiteId: response.id,
          versio: ''
        });
      });
    };

    $scope.avaaSuljeKaikki = function(state) {
      var open = false;
      Algoritmit.kaikilleLapsisolmuille($scope.peruste.sisalto, 'lapset', function(lapsi) {
        open = open || lapsi.$opened;
      });
      Algoritmit.kaikilleLapsisolmuille($scope.peruste.sisalto, 'lapset', function(lapsi) {
        lapsi.$opened = _.isUndefined(state) ? !open : state;
      });
    };

    $scope.vaihdaSuoritustapa = function(suoritustapakoodi) {
      $scope.valittuSuoritustapa = suoritustapakoodi;
      PerusteProjektiService.setSuoritustapa(suoritustapakoodi);

      $state.go('root.perusteprojekti.suoritustapa.sisalto', {
        perusteProjektiId: $stateParams.perusteProjektiId,
        suoritustapa: suoritustapakoodi
      });
    };

    (function() {
      Algoritmit.kaikilleTutkintokohtaisilleOsille($scope.peruste.sisalto, function(osa) {
        osa.$opened = false;
      });
      $scope.rajaaSisaltoa();
    }());

    Editointikontrollit.registerCallback({
      edit: function() {
        $scope.muokkausTutkintokohtaisetOsat = true;
        $scope.rajaus = '';
        $scope.avaaSuljeKaikki(true);
      },
      save: function() {
        TekstikappaleOperations.updateViitteet($scope.peruste.sisalto, function () {
          $scope.muokkausTutkintokohtaisetOsat = false;
          Notifikaatiot.onnistui('osien-rakenteen-päivitys-onnistui');
          PerusteProjektiSivunavi.refresh(true);
        });
      },
      cancel: function() {
        $state.go($state.current.name, $stateParams, {
          reload: true
        });
      },
      validate: function() { return true; },
      notify: angular.noop
    });

    $scope.aloitaMuokkaus = function() {
      Editointikontrollit.startEditing();
    };
  });
