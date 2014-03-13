'use strict';
/* global _ */

angular.module('eperusteApp')
  .service('Koodisto', function($http, $modal, SERVICE_LOC) {
    var taydennykset = [];
    var koodistoVaihtoehdot = ['tutkinnonosat', 'koulutus'];
    var nykyinenKoodisto = _.first(koodistoVaihtoehdot);

    function hae(koodisto, cb) {
      if (!_.isEmpty(taydennykset) && koodisto === nykyinenKoodisto) { cb(); return; }
      $http.get(SERVICE_LOC + '/koodisto/' + koodisto).then(function(re) {
        taydennykset = _(re.data).map(function(kv) {
          var nimi = {
            fi: '',
            sv: '',
            en: ''
          };
          _.forEach(kv.metadata, function(obj) {
            nimi[obj.kieli.toLowerCase()] = obj.nimi;
          });

          var haku = _.reduce(_.values(nimi), function(result, v) {
            return result + v;
          }).toLowerCase();
          return {
            koodi: kv.koodiUri,
            nimi: nimi,
            haku: haku
          };
        }).value();
        cb();
      });
    }

    function filtteri(haku) {
      haku = haku.toLowerCase();
      return _.filter(taydennykset, function(t) { return t.koodi.indexOf(haku) !== -1 || t.haku.indexOf(haku) !== -1; });
    }

    function modaali(successCb, resolve, failureCb) {
      return function() {
        resolve = resolve || {};
        failureCb = failureCb || function() {};
        $modal.open({
          templateUrl: 'views/modals/koodistoModal.html',
          controller: 'KoodistoModalCtrl',
          resolve: resolve
        }).result.then(successCb, failureCb);
      };
    }

    return {
      hae: hae,
      filtteri: filtteri,
      vaihtoehdot: _.clone(koodistoVaihtoehdot),
      modaali: modaali
    };
  })
  .controller('KoodistoModalCtrl', function($scope, $modalInstance, $translate, $timeout, Koodisto, tyyppi) {
    $scope.koodistoVaihtoehdot = Koodisto.vaihtoehdot;
    $scope.tyyppi = tyyppi;
    $scope.loydetyt = [];
    $scope.haku = function(rajaus, kieli) { $scope.loydetyt = Koodisto.filtteri(rajaus, kieli);};
    $scope.lataa = true;
    $scope.syote = '';
    $scope.kieli = 'fi';

    Koodisto.hae($scope.tyyppi, function() {
      $scope.lataa = false;
      $scope.haku('', $scope.kieli);
    });

    $scope.ok = function(koodi) { $modalInstance.close(koodi); };
    $scope.peruuta = function() { $modalInstance.dismiss(); };
  })
  .directive('koodistoSelect', function(Koodisto) {
    return {
      template: '<button class="btn btn-default" type="text" ng-click="activate()" editointi-kontrolli>{{ "hae-koodi-koodistosta" | translate }}</button>',
      restrict: 'E',
      link: function($scope, el, attrs) {
        var valmis = $scope.$eval(attrs.valmis);
        var tyyppi = attrs.tyyppi || 'tutkinnonosat';

        if (!valmis) {
          console.log('koodisto-select: valmis-callback puuttuu');
          return;
        } else if (_.indexOf(Koodisto.vaihtoehdot, tyyppi) === -1) {
          console.log('koodisto-select:', tyyppi, 'ei vastaa mitään mitään vaihtoehtoa:', Koodisto.vaihtoehdot);
          return;
        }
        $scope.activate = Koodisto.modaali(function(koodi) { valmis(koodi); }, { tyyppi: function() { return tyyppi; } });
      }
    };
  });
