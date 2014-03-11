'use strict';
/* global _ */

angular.module('eperusteApp')
  .service('Koodisto', function($http, SERVICE_LOC) {
    var taydennykset = [];
    var koodistoVaihtoehdot = ['tutkinnonosat', 'koulutus'];
    var nykyinenKoodisto = _.first(koodistoVaihtoehdot);

    function hae(koodisto, cb) {
      if (!_.isEmpty(taydennykset) && koodisto === nykyinenKoodisto) { cb(); return; }
      $http.get(SERVICE_LOC + '/koodisto/' + koodisto).then(function(re) {
        taydennykset = _.map(re.data, function(kv) {
          var nimi = {
            fi: '',
            se: '',
            en: ''
          };
          _.forEach(kv.metadata, function(obj) {
            nimi[obj.kieli.toLowerCase()] = obj.nimi;
          });
          return {
            koodi: kv.koodiUri,
            nimi: nimi,
            haku: _.map(nimi, function(v, k) {
              var n = {};
              n[k] = v.toLowerCase();
              return n;
            })
          };
        });
        cb();
      });
    }

    function filtteri(haku, kieli) {
      haku = haku.toLowerCase();
      var res = _.filter(taydennykset, function(t) {
        return t.koodi.indexOf(haku) !== -1 || t.nimi[kieli].indexOf(haku) !== -1;
      });
      return res;
    }

    return {
      hae: hae,
      filtteri: filtteri,
      vaihtoehdot: _.clone(koodistoVaihtoehdot)
    };
  })
  .controller('KoodistoModalCtrl', function($scope, $modalInstance, $translate, Koodisto, tyyppi) {
    $scope.koodistoVaihtoehdot = Koodisto.vaihtoehdot;
    $scope.tyyppi = tyyppi;
    $scope.haku = function(rajaus, kieli) { $scope.loydetyt = Koodisto.filtteri(rajaus, kieli); };
    $scope.lataa = true;
    $scope.syote = '';
    $scope.kieli = 'fi';

    Koodisto.hae($scope.tyyppi, function() {
      $scope.lataa = false;
      $scope.haku('');
    });

    $scope.ok = function(koodi) { $modalInstance.close(koodi); };
    $scope.peruuta = function() { $modalInstance.dismiss(); };
  })
  .directive('koodistoSelect', function($modal, Koodisto) {
    return {
      template: '<button class="btn btn-default" type="text" ng-click="activate()">{{ "hae-koodi-koodistosta" | translate }}</button>',
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

        $scope.activate = function() {
          $modal.open({
            templateUrl: 'views/modals/koodistoModal.html',
            controller: 'KoodistoModalCtrl',
            resolve: { tyyppi: function() { return tyyppi; } }
          }).result.then(function(koodi) {
            valmis(koodi);
          }, function() {});
        };
      }
    };
  });
