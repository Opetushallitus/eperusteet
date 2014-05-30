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
      transclude: true,
      controller: 'sivunavigaatioCtrl'
    };
  })

  .controller('sivunavigaatioCtrl', function($rootScope, $scope, $state, SivunavigaatioService, PerusteProjektiService) {
    $scope.menuCollapsed = true;
    $rootScope.$on('$stateChangeStart', function () {
      $scope.menuCollapsed = true;
    });
    $scope.goBackToMain = function () {
      $state.go('perusteprojekti.suoritustapa.sisalto', {perusteProjektiId: $scope.projekti.id, suoritustapa: PerusteProjektiService.getSuoritustapa()}, {reload: true});
    };
    $scope.toggleSideMenu = function () {
      $scope.menuCollapsed = !$scope.menuCollapsed;
    };
    $scope.isHidden = function () {
      return $scope.data.piilota;
    };
    SivunavigaatioService.bind($scope);
  })

  .service('SivunavigaatioService', function () {
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
      if (data.perusteprojektiTiedot) {
        this.data.projekti = data.perusteprojektiTiedot.getProjekti();
        this.data.projekti.peruste = data.perusteprojektiTiedot.getPeruste();
        this.data.projekti.peruste.sisalto = data.perusteprojektiTiedot.getSisalto();
      }

      if (!_.isUndefined(data.osiot)) {
        this.data.osiot = data.osiot;
      }
      this.data.piilota = !!data.piilota;
    };

  });
