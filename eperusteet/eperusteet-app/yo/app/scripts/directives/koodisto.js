'use strict';
/* global _ */

angular.module('eperusteApp')
  .service('Koodisto', function($http, SERVICE_LOC) {
    var taydennykset = [];
    var koodistoVaihtoehdot = ['tutkinnonosat'];
    var nykyinenKoodisto = _.first(koodistoVaihtoehdot);

    function hae(koodisto, cb) {
      if (!_.isEmpty(taydennykset) && koodisto === nykyinenKoodisto) { cb(); return; }
      $http.get(SERVICE_LOC + '/koodisto/' + koodisto).then(function(re) {
        taydennykset = _.map(re.data, function(kv) {
          var nimi = {
            fi: '',
            se: ''
          };
          _.forEach(kv.metadata, function(obj) {
            nimi[obj.kieli.toLowerCase()] = obj.nimi;
          });
          return {
            koodi: kv.koodiUri,
            nimi: nimi,
            haku: _.reduce(nimi, function(result, v) {
              return result + v;
            }).toLowerCase()
          };
        });
        cb();
      });
    }

    function filtteri(haku) {
      haku = haku.toLowerCase();
      var res = _.filter(taydennykset, function(t) {
        return t.koodi.indexOf(haku) !== -1 || t.haku.indexOf(haku) !== -1;
      });
      return res;
    }

    return {
      hae: hae,
      filtteri: filtteri,
      vaihtoehdot: _.clone(koodistoVaihtoehdot)
    };
  })
  .controller('KoodistoModalCtrl', function($scope, $modalInstance, Koodisto) {
    $scope.koodistoVaihtoehdot = Koodisto.vaihtoehdot;
    $scope.tyyppi = _.first($scope.koodistoVaihtoehdot);
    $scope.haku = function(rajaus) { $scope.loydetyt = Koodisto.filtteri(rajaus); };
    $scope.lataa = true;

    Koodisto.hae($scope.tyyppi, function() {
      $scope.lataa = false;
      $scope.haku('');
    });

    $scope.ok = function(koodi) { $modalInstance.close(koodi); };
    $scope.peruuta = function() { $modalInstance.dismiss(); };
  })
  .directive('koodistoSelect', function($modal) {
    return {
      template: '<button class="btn btn-default" type="text" ng-click="activate()">Hae koodistosta</button>',
      restrict: 'E',
      link: function($scope, el, attrs) {
        var valmis = $scope.$eval(attrs.valmis);
        var tyyppi = attrs.tyyppi;

        if (!valmis) {
          console.log('koodisto-select: valmis-callback puuttuu');
          return;
        }

        $scope.activate = function() {
          $modal.open({
            templateUrl: 'views/modals/koodistoModal.html',
            controller: 'KoodistoModalCtrl'
          }).result.then(function(koodi) {
            console.log('koodi:', koodi);
            valmis(koodi);
          }, function() {});
        };
      }
    };
  });
