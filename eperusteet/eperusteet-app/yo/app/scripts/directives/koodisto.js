'use strict';
/* global _ */

angular.module('eperusteApp')
  .service('Koodisto', function($http, $modal, SERVICE_LOC, $resource) {
    var taydennykset = [];
    var koodistoVaihtoehdot = ['tutkinnonosat', 'koulutus'];
    var nykyinenKoodisto = _.first(koodistoVaihtoehdot);
    var lisaFiltteri = function() { return true; };

    function hae(koodisto, cb) {
      if (!_.isEmpty(taydennykset) && koodisto === nykyinenKoodisto) { cb(); return; }
      $http.get(SERVICE_LOC + '/koodisto/' + koodisto).then(function(re) {
        taydennykset = koodistoMapping(re.data);
        cb();
      });
    }

    function haeAlarelaatiot(koodi, cb) {
      var resource = $resource(SERVICE_LOC + '/koodisto/relaatio/sisaltyy-alakoodit/:koodi');
      console.log('koodi', koodi);
      resource.query({koodi: koodi}, function (vastaus) {
        var relaatiot = koodistoMapping(vastaus);
        cb(relaatiot);
      });
    }

    function koodistoMapping(koodistoData) {
      return _(koodistoData).map(function(kd) {
        var nimi = {
          fi: '',
          sv: '',
          en: ''
        };
        _.forEach(kd.metadata, function(obj) {
          nimi[obj.kieli.toLowerCase()] = obj.nimi;
        });

        var haku = _.reduce(_.values(nimi), function(result, v) {
          return result + v;
        }).toLowerCase();
        return {
          koodi: kd.koodiUri,
          nimi: nimi,
          koodisto: kd.koodisto,
          haku: haku
        };
      }).value();
    }

    function filtteri(haku) {
      haku = haku.toLowerCase();
      return _.filter(taydennykset, function(t) { return (t.koodi.indexOf(haku) !== -1 || t.haku.indexOf(haku) !== -1) && lisaFiltteri(t); });
    }

    function modaali(successCb, resolve, failureCb, lisaf) {
      if (filtteri) {
        lisaFiltteri = lisaf;
      }

      return function() {
        resolve = resolve || {};
        failureCb = failureCb || function() {};
        $modal.open({
          templateUrl: 'views/modals/koodistoModal.html',
          controller: 'KoodistoModalCtrl',
          resolve: resolve }).result.then(successCb, failureCb);
      };
    }

    return {
      hae: hae,
      filtteri: filtteri,
      vaihtoehdot: _.clone(koodistoVaihtoehdot),
      modaali: modaali,
      haeAlarelaatiot: haeAlarelaatiot
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
        var filtteri = $scope.$eval(attrs.filtteri);
        var tyyppi = attrs.tyyppi || 'tutkinnonosat';

        if (!valmis) {
          console.log('koodisto-select: valmis-callback puuttuu');
          return;
        } else if (_.indexOf(Koodisto.vaihtoehdot, tyyppi) === -1) {
          console.log('koodisto-select:', tyyppi, 'ei vastaa mitään mitään vaihtoehtoa:', Koodisto.vaihtoehdot);
          return;
        }
        $scope.activate = Koodisto.modaali(function(koodi) { valmis(koodi); }, { tyyppi: function() { return tyyppi; } }, function(){}, filtteri);
      }
    };
  });
