'use strict';
/* global _ */

/**
 * Perusteprojektin sivunavigaatioelementti
 */
angular.module('eperusteApp')
  .directive('sivunavigaatio', function() {
    return {
      templateUrl: 'views/partials/sivunavigaatio.html',
      restrict: 'E',
      controller: 'sivunavigaatioCtrl'
    };
  })

  .controller('sivunavigaatioCtrl', function($rootScope, $scope, $state, SivunavigaatioService, PerusteProjektiService) {
    $scope.menuCollapsed = true;
    $rootScope.$on('$stateChangeStart', function () {
      $scope.menuCollapsed = true;
    });
    $scope.goBackToMain = function () {
      $state.go('perusteprojekti.suoritustapa.sisalto', {perusteProjektiId: $scope.projekti.id, suoritustapa: PerusteProjektiService.getSuoritustapa()});
    };
    $scope.toggleSideMenu = function () {
      $scope.menuCollapsed = !$scope.menuCollapsed;
    };
    $scope.isHidden = function () {
      if ($scope.data.piilota) {
        // TODO: parempi/tehokkaampi ratkaisu. Sisältö-div on tämän direktiivin
        // ulkopuolella, mutta sen tyyli riippuu 'piilota'-attribuutista.
        angular.element('.sivunavi-sisalto').css('margin-left', '0px');
      }
      return $scope.data.piilota;
    };
    SivunavigaatioService.bind($scope);
  })

  .service('SivunavigaatioService', function (Suoritustapa) {
    this.data = {
      osiot: false,
      piilota: false,
      projekti: {id: 0}
    };
    this.bind = function (scope) {
      scope.data = this.data;
    };

    /**
     * Asettaa sivunavigaation tiettyyn tilaan.
     * Suositeltu käyttöpaikka: $stateProvider.state -> onEnter
     * @param data {Object} Mahdolliset asetukset:
     *     osiot: true näyttää projektin kaikki osiot,
     *            false näyttää vain "takaisin"-linkin
     *     piilota: true piilottaa koko navigaatioelementin
     */
    this.aseta = function (data) {
      if (data.projekti) {
        this.data.projekti = data.projekti;
        // TODO: kevyempi API jolla haetaan pelkät otsikot/linkkeihin
        // tarvittavat tiedot, ei koko sisältöä
        var that = this;
        Suoritustapa.get({perusteenId: this.data.projekti._peruste, suoritustapa: 'ops'}, function(vastaus) {
          that.data.projekti.peruste = {};
          that.data.projekti.peruste.sisalto = vastaus;
        });
        return;
      }
      if (!_.isUndefined(data.osiot)) {
        this.data.osiot = data.osiot;
      }
      this.data.piilota = !!data.piilota;
    };
    /**
     * Asettaa projektiobjektin, täytyy sisältää ainakin 'id'
     */
    this.asetaProjekti = function (projekti) {
      this.aseta({projekti: projekti});
    };
  });
