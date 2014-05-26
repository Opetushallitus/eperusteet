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
  .controller('ProjektiryhmaCtrl', function($scope, $modal, $stateParams, PerusteprojektiJasenet, PerusteProjektiService) {
    PerusteProjektiService.watcher($scope, 'projekti');

    $scope.ryhma = [];

    PerusteprojektiJasenet.get({
      id: $stateParams.perusteProjektiId
    }, function(jasenet) {
      $scope.ryhma = jasenet;
    });

    $scope.kutsuUusi = function () {
      $modal.open({
        template: '<div class="modal-header"><h2>Lisää jäsen</h2></div>' +
          '<div class="modal-body"><jasenkortti jasen="jasen" muokkaus-moodi="true"></jasenkortti></div>' +
          '<div class="modal-footer">' +
          '<button class="btn btn-primary" ng-click="tallenna()" translate>tallenna</button>' +
          '<button class="btn btn-default" ng-click="peruuta()" translate>peruuta</button>' +
          '</div>',
        controller: 'uusiJasenCtrl',
        resolve: {
          data: function () { return { ryhma: $scope.ryhma }; }
        }
      });
    };
  })

  .controller('uusiJasenCtrl', function ($scope, $modalInstance, kayttajaToiminnot, data) {
    $scope.jasen = {nimi: '', email: '', puhelin: '', rooli: 'jasen'};
    $scope.kayttajaToiminnot = kayttajaToiminnot;
    $scope.tallenna = function () {
      data.ryhma.push($scope.jasen);
      $modalInstance.close();
    };
    $scope.peruuta = function () {
      $modalInstance.close();
    };
  })

  .directive('ryhmanjasenet', function () {
    return {
      restrict: 'E',
      template: '<div class="ryhma-rooli">' +
        '<h3>{{\'projektiryhma-otsikko-\' + rooli | kaanna}}</h3>' +
        '<jasenkortti ng-repeat="jasen in ryhma | filter:{rooli: rooli}:true" ryhma="ryhma" jasen="jasen" voi-muokata="true"></jasenkortti>' +
        '</div>',
      controller: 'ryhmanjasenetCtrl',
      scope: {
        ryhma: '=',
        rooli: '@'
      }
    };
  })

  .controller('ryhmanjasenetCtrl', function ($scope, kayttajaToiminnot) {
    $scope.kayttajaToiminnot = kayttajaToiminnot;
  })

  .directive('jasenkortti', function () {
    return {
      restrict: 'E',
      templateUrl: 'views/partials/jasenkortti.html',
      controller: 'jasenkorttiCtrl',
      scope: {
        ryhma: '=',
        jasen: '='
      },
      link: function (scope, element, attrs) {
        scope.voiMuokata = (attrs.voiMuokata === 'true');
        scope.muokkausMoodi = (attrs.muokkausMoodi === 'true');
      }
    };
  })

  .service('kayttajaToiminnot', function () {
    this.nimikirjaimet = function (nimi) {
      return _.reduce(nimi.split(' '), function (memo, osa) {
        return memo + (osa ? osa[0] : '');
      }, '').toUpperCase();
    };
  });
