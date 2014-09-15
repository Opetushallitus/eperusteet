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
  .controller('ProjektiryhmaCtrl', function($scope, $modal, $stateParams, PerusteprojektiJasenet, Notifikaatiot,
    Projektiryhma, PerusteProjektiService, ColorCalculator, kayttajaToiminnot, PerusteprojektiTyoryhmat) {
    PerusteProjektiService.watcher($scope, 'projekti');

    $scope.ryhma = {};
    $scope.tyyppi = 'kaikki';
    $scope.lataa = true;
    $scope.error = false;

    $scope.vaihdaTyyppi = function(tyyppi) { $scope.tyyppi = tyyppi; };

    $scope.filterRyhma = function(ryhma) {
      return _.some(ryhma, $scope.filterJasen);
    };

    $scope.filterJasen = function(jasen) {
      return $scope.tyyppi === 'kaikki' || $scope.tyoryhmat[$scope.tyyppi][jasen.oidHenkilo];
    };

    Projektiryhma.jasenetJaTyoryhmat($stateParams.perusteProjektiId, function(re) {
      $scope.jasenet = re.jasenet;
      $scope.tyoryhmat = re.tyoryhmat;
      $scope.ryhma = re.ryhma;
      $scope.lataa = false;
    }, function(err) {
      Notifikaatiot.serverCb(err);
      $scope.error = true;
      $scope.lataa = false;
    });

    $scope.nimikirjaimet = kayttajaToiminnot.nimikirjaimet;

    $scope.styleFor = function(jasen) {
      return jasen.color ? {
        'background-color': '#' + jasen.color,
        'color': ColorCalculator.readableTextColorForBg(jasen.color)
      } : {};
    };

    $scope.muokkaaTyoryhmaa = function(ryhma) {
      $modal.open({
        templateUrl: 'views/modals/lisaaTyoryhma.html',
        controller: 'LuoTyoryhmaModalCtrl',
        resolve: {
          ryhma: function() { return _.clone($scope.tyoryhmat[ryhma]) || null; },
          jasenet: function() { return _.clone($scope.jasenet); }
        }
      })
      .result.then(function(muokattu) {
        if (muokattu) {
          PerusteprojektiTyoryhmat.save({
            id: $stateParams.perusteProjektiId,
            nimi: ryhma
          }, _.map(muokattu.jasenet, function(j) {
            return {
              kayttajaOid: j,
              nimi: muokattu.nimi
            };
          }), function(lisatyt) {
            delete $scope.tyoryhmat[ryhma];
            $scope.tyyppi = _.first(lisatyt).nimi;
            $scope.tyoryhmat[$scope.tyyppi] = _.zipObject(_.map(lisatyt, 'kayttajaOid'), lisatyt);
          }, Notifikaatiot.serverCb);
        }
        else {
          PerusteprojektiTyoryhmat.delete({
            id: $stateParams.perusteProjektiId,
            nimi: ryhma
          }, function() {
            delete $scope.tyoryhmat[ryhma];
            $scope.tyyppi = 'kaikki';
          }, Notifikaatiot.serverCb);
        }
      });
    };

  })
  .controller('LuoTyoryhmaModalCtrl', function($scope, $modalInstance, Varmistusdialogi, ryhma, jasenet) {
    $scope.uusi = ryhma ? false : true;

    $scope.ryhma = {
      nimi: ryhma ? _.first(_.values(ryhma)).nimi : '',
      jasenet: ryhma ? _.keys(ryhma) : []
    };

    $scope.jasenet = jasenet || [];
    $scope.jasenMap = _.zipObject(_.map($scope.jasenet, 'oidHenkilo'), $scope.jasenet);
    $scope.to = [];
    $scope.from = [];

    $scope.lista = [];
    var lisatyt = ryhma || {};

    if (!$scope.ryhma) {
      $scope.ryhma = {};
      $scope.ryhma.jasenet = [];
    }

    $scope.poistaLisatyt = function(t) { return !lisatyt[t.oidHenkilo]; };
    $scope.apulajittelija = function(oid) { return $scope.jasenMap[oid].sukunimi; };

    $scope.siirraOikealle = function(from) {
      _.forEach(from, function(t) {
        $scope.ryhma.jasenet.push(t.oidHenkilo);
        lisatyt[t.oidHenkilo] = true;
      });
      from = [];
    };

    $scope.siirraVasemmalle = function(to) {
      $scope.ryhma.jasenet = _.remove($scope.ryhma.jasenet, function(j) {
        var remove = !_.some(to, function(t) { return t === j; });
        if (!remove) { delete lisatyt[j]; }
        return remove;
      });
      to = [];
    };

    $scope.ok = _.partial($modalInstance.close, $scope.ryhma);
    $scope.peruuta = $modalInstance.dismiss;
    $scope.poista = function() {
      Varmistusdialogi.dialogi({
        otsikko: 'poistetaanko-työryhmä',
        successCb: _.partial($modalInstance.close, null)
      })();
    };
  })
  .service('kayttajaToiminnot', function() {
    this.nimikirjaimet = function(nimi) {
      return _.reduce(nimi.split(' '), function(memo, osa) {
        return memo + (osa ? osa[0] : '');
      }, '').toUpperCase();
    };
  });
