'use strict';
/* global _ */

angular.module('eperusteApp')
  .config(function($stateProvider) {
    $stateProvider
      .state('perusteprojekti.editoi.projektiryhma', {
        url: '/projektiryhma',
        templateUrl: 'views/partials/perusteprojektiProjektiryhma.html',
        controller: 'ProjektiryhmaCtrl',
        naviBase: ['perusteprojekti', ':perusteProjektiId']
      });
  })

  .controller('ProjektiryhmaCtrl', function($scope, PerusteProjektiService, $modal) {
    PerusteProjektiService.watcher($scope, 'projekti');

    $scope.ryhma = [
      {rooli: 'omistaja', nimi: 'Olaf Omistaja', puhelin: '040-1234567', email: 'ossi.omistaja@joku.fi'},
      {rooli: 'sihteeri', nimi: 'Sirkku Sihteeri', puhelin: '040-1234567', email: 'email.email@joku.fi'},
      {rooli: 'jasen', nimi: 'Jetro Jäsen', puhelin: '040-1234567', email: 'email.email@joku.fi'},
      {rooli: 'jasen', nimi: 'Jaana Jäsen', puhelin: '040-1234567', email: 'email.email@joku.fi'},
      {rooli: 'jasen', nimi: 'Matti Esimerkki', puhelin: '040-1234567', email: 'email.email@joku.fi'},
      {rooli: 'jasen', nimi: 'Erkki-Sakari Meikäläinen', puhelin: '040-1234567', email: 'email.email@joku.fi'},
      {rooli: 'kommentoija', nimi: 'Killian Kalle K. Kommentoija', puhelin: '+358-(0)40-1234567', email: 'email.tosipitkaemail@jokujossain.fi'},
      {rooli: 'kommentoija', nimi: 'Kathryn Kommentoija', puhelin: '040-1234567', email: 'email.email@joku.fi'}
    ];

    $scope.kutsuUusi = function () {
      $modal.open({
        template: '<div class="modal-header"><h2>Lisää jäsen</h2></div>' +
          '<div class="modal-body"><jasenkortti jasen="jasen" muokkaus-moodi="true"></jasenkortti></div>' +
          '<div class="modal-footer">' +
          '<button class="btn btn-default" ng-click="tallenna()" translate>tallenna-nappi</button>' +
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
        '<jasenkortti ng-repeat="jasen in ryhma | filter:{rooli: rooli}:true" ryhma="ryhma" jasen="jasen" voi-muokata="true"/>' +
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
