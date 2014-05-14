'use strict';
/*global _*/

angular.module('eperusteApp')
  .service('TreeCache', function() {
    var puuId = -1;
    var puu = {};

    return {
      nykyinen: function() { return puuId; },
      hae: function() { console.log('haetaan'); return _.clone(puu); },
      tallenna: function(rakenne, id) {
        if (id) {
          puu = _.clone(rakenne);
          puuId = id;
        }
      },
      puhdista: function() {
        puuId = -1;
        puu = {};
      }
    };
  })
  .service('Muodostumissaannot', function($modal) {
    function osienLaajuudenSumma(osat) {
        return _(osat)
          .map(function(osa) { return osa.$vaadittuLaajuus ? osa.$vaadittuLaajuus : osa.$laajuus; })
          .reduce(function(sum, newval) { return sum + newval; });
    }

    function validoiRyhma(rakenne) {
      function lajittele(osat) {
        var buckets = {};
        _.forEach(osat, function(osa) {
          if (!buckets[osa.$laajuus]) { buckets[osa.$laajuus] = 0; }
          buckets[osa.$laajuus] += 1;
        });
        return buckets;
      }

      function avaintenSumma(osat, n, avaimetCb) {
        var res = 0;
        var i = n;
        var lajitellut = lajittele(osat);
        _.forEach(avaimetCb(lajitellut), function(k) {
          while (lajitellut[k]-- > 0 && i-- > 0) { res += parseInt(k, 10) || 0; }
        });
        return res;
      }

      if (!rakenne) { return; }

      delete rakenne.$virhe;

      // On rakennemoduuli
      if (rakenne.muodostumisSaanto) {
        var msl = rakenne.muodostumisSaanto.laajuus;
        var msk = rakenne.muodostumisSaanto.koko;

        if (msl && msk) {
          var minimi = avaintenSumma(rakenne.osat, msk.minimi, function(lajitellut) { return _.keys(lajitellut); });
          var maksimi = avaintenSumma(rakenne.osat, msk.maksimi, function(lajitellut) { return _.keys(lajitellut).reverse(); });
          if (minimi < msl.minimi) { rakenne.$virhe = 'rakenne-validointi-maara-laajuus-minimi'; }
          else if (maksimi < msl.maksimi) { rakenne.$virhe =  'rakenne-validointi-maara-laajuus-maksimi'; }
        } else if (msl) {
          // Validoidaan maksimi
          if (msl.maksimi) {
            if (osienLaajuudenSumma(rakenne.osat) < msl.maksimi) {
              rakenne.$virhe = 'muodostumis-rakenne-validointi-laajuus';
            }
          }
        } else if (msk) {
          if (_.size(rakenne.osat) < msk.maksimi) {
            rakenne.$virhe = 'muodostumis-rakenne-validointi-maara';
          }
        }

        var tosat = _(rakenne.osat)
          .filter(function(osa) { return osa._tutkinnonOsa; })
          .value();
        if (_.size(tosat) !== _(tosat).uniq('_tutkinnonOsa').size()) {
            rakenne.$virhe = 'muodostumis-rakenne-validointi-uniikit';
        }
      }
    }

    // Laskee rekursiivisesti puun solmujen (rakennemoduulien) kokonaislaajuuden
    function laskeLaajuudet(rakenne, tutkinnonOsat, root) {
      root = root || true;

      if (!rakenne) { return; }

      _.forEach(rakenne.osat, function(osa) { laskeLaajuudet(osa, tutkinnonOsat, false); });
      rakenne.$laajuus = 0;

      if (rakenne._tutkinnonOsa) {
        rakenne.$laajuus = tutkinnonOsat[rakenne._tutkinnonOsa].laajuus;
      }
      else {
        if (rakenne.osat && rakenne.muodostumisSaanto) {
          var msl = rakenne.muodostumisSaanto.laajuus;
          if (msl) {
            rakenne.$vaadittuLaajuus = msl.maksimi;
          }
        }
        rakenne.$laajuus = osienLaajuudenSumma(rakenne.osat);
      }
    }

    function ryhmaModaali(thenCb) {
      return function(suoritustapa, ryhma, vanhempi) {
        $modal.open({
          templateUrl: 'views/modals/ryhmaModal.html',
          controller: 'MuodostumisryhmaModalCtrl',
          resolve: {
            ryhma: function() { return ryhma; },
            vanhempi: function() { return vanhempi; },
            suoritustapa: function() { return suoritustapa; }
          }
        })
        .result.then(function(res) { thenCb(ryhma, vanhempi, res); });
      };
    }

    return {
      validoiRyhma: validoiRyhma,
      laskeLaajuudet: laskeLaajuudet,
      ryhmaModaali: ryhmaModaali
    };
  })
  .directive('tree', function($compile, $state, $modal, Muodostumissaannot) {
    function generoiOtsikko() {
      var tosa = 'tutkinnonOsat[rakenne._tutkinnonOsa]';
      return '' +
        '<span ng-if="rakenne._tutkinnonOsa">{{ ' + tosa + '.nimi | kaanna | rajaaKoko:40 }}, <b>{{' + tosa + '.laajuus || 0 }}</b>ov</span>' +
        '<span class="pull-right" ng-if="rakenne._tutkinnonOsa && muokkaus"><a href="" ng-click="poista(rakenne, vanhempi)"><span class="glyphicon glyphicon-remove"></a></span>' +
        '<span ng-if="!rakenne._tutkinnonOsa && rakenne.nimi"><b>{{ rakenne.nimi | kaanna  }}</b></span>';
    }

    return {
      restrict: 'AE',
      transclude: false,
      terminal: true,
      scope: {
        rakenne: '=',
        tutkinnonOsat: '=',
        uusiTutkinnonOsa: '=',
        vanhempi: '=',
        apumuuttujat: '=',
        muokkaus: '='
      },
      link: function(scope, el) {
        scope.lisaaUusi = 0;
        scope.lisataanUuttaPerusteenOsaa = false;
        scope.scratchpad = [];
        scope.roskakori = [];

        scope.poista = function(i, a) { _.remove(a.osat, i); };

        scope.ryhmaModaali = Muodostumissaannot.ryhmaModaali(function(ryhma, vanhempi, uusiryhma) {
          if (!scope.vanhempi) {
            scope.rakenne = uusiryhma;
          } else {
            var indeksi = scope.vanhempi.osat.indexOf(ryhma);
            if (!uusiryhma) { _.remove(scope.vanhempi.osat, ryhma); }
            else if (indeksi !== -1) { scope.vanhempi.osat[indeksi] = uusiryhma; }
          }
        });

        scope.$watch('rakenne', function(uusirakenne) {
          Muodostumissaannot.laskeLaajuudet(scope.rakenne, scope.tutkinnonOsat);
          Muodostumissaannot.validoiRyhma(uusirakenne, scope.tutkinnonOsat);
        }, true);

        scope.sortableOptions = {
          placeholder: 'placeholder',
          connectWith: '.tree-group',
          disabled: !scope.muokkaus,
          delay: 100,
          //tolerance: 'pointer',
          //cursorAt: { top : 2, left: 2 },
          cursor: 'move',
          start: function(e, ui) {
            ui.placeholder.html('<div class="group-placeholder"></div>');
          }
        };

        scope.$watch('muokkaus', function() {
          scope.sortableOptions.disabled = !scope.muokkaus;
        });

        var optiot = '' +
          '<span ng-if="!rakenne._tutkinnonOsa" class="colorbox">' +
          '  <a href="" ng-click="rakenne.$collapsed = !rakenne.$collapsed">' +
          '    <span ng-hide="rakenne.$collapsed" class="glyphicon glyphicon-chevron-up"></span>' +
          '    <span ng-show="rakenne.$collapsed" class="glyphicon glyphicon-chevron-down"></span>' +
          '  </a>' +
          '</span>' +
          '<div class="right">' +
          '  <div ng-if="!rakenne._tutkinnonOsa && muokkaus" class="right-item">' +
          '    <a href="" ng-click="ryhmaModaali(apumuuttujat.suoritustapa, rakenne, vanhempi)"><span class="glyphicon glyphicon-pencil"></span></a>' +
          '  </div>' +
          '  <div class="pull-right" ng-if="!rakenne._tutkinnonOsa && muokkaus">' +
          '    <span class="right-item" ng-show="apumuuttujat.suoritustapa !== \'naytto\' && rakenne.$vaadittuLaajuus"><b>{{ rakenne.$laajuus || 0 }}</b>/<b>{{ rakenne.$vaadittuLaajuus || 0 }}</b>ov</span>' +
          '    <span class="right-item" ng-show="apumuuttujat.suoritustapa !== \'naytto\'"><b>{{ rakenne.$laajuus || 0 }}</b>ov</span>' +
          '    <span class="right-item"><b>{{ rakenne.osat.length }}kpl</b></span>' +
          '  </div>' +
          '</div>' +

          '<div class="left">' +
          '  <span class="tree-item">' + generoiOtsikko() + '</span>' +
          '</div>';

        var kentta = '<div ng-if="rakenne._tutkinnonOsa" ng-class="{ \'pointer\': muokkaus }" class="bubble-osa">' + optiot + '</div>';
        kentta += '<div ng-if="!rakenne._tutkinnonOsa" ng-class="{ \'pointer\': muokkaus }" class="bubble">' + optiot + '</div>';
        kentta += '<div ng-model="rakenne" ng-show="muokkaus && rakenne.$virhe" class="virhe"><span>{{ rakenne.$virhe | translate }}</span></div>';

        var template = '';

        template =
          '<div>' +
          kentta +
          '</div>';

        template =
          '<div ng-if="!vanhempi">' +
          '  <div class="otsikko">' +
          '    <h4 ng-show="muokkaus"><a href="" ng-click="ryhmaModaali(apumuuttujat.suoritustapa, rakenne, vanhempi)">{{ rakenne.nimi || \'nimetön\' | kaanna }}</a>, {{ rakenne.$laajuus || 0 }} / {{ rakenne.muodostumisSaanto.laajuus.minimi || 0 }}ov</h4>' +
          '    <h4 ng-hide="muokkaus">{{ rakenne.nimi || \'nimetön\' | kaanna }}, {{ rakenne.$laajuus || 0 }} / {{ rakenne.muodostumisSaanto.laajuus.minimi || 0 }}ov</h4>' +
          '    <div ng-if="rakenne.$virhe" class="isovirhe">{{ rakenne.$virhe | kaanna }}</div>' +
          '  </div>' +
          '</div>' +
          '<div ng-if="vanhempi">' + kentta + '</div>' +
          '<div ng-show="!rakenne.$collapsed">' +
          '  <ul ng-if="rakenne.osat !== undefined" ui-sortable="sortableOptions" id="tree-sortable" class="tree-group" ng-model="rakenne.osat">' +
          '    <li ng-repeat="osa in rakenne.osat">' +
          '      <tree apumuuttujat="apumuuttujat" muokkaus="muokkaus" rakenne="osa" vanhempi="rakenne" tutkinnon-osat="tutkinnonOsat" uusi-tutkinnon-osa="uusiTutkinnonOsa" ng-init="notfirst = true"></tree>' +
          '    </li>' +
          '  </ul>' +
          '</div>';

        var templateElement = angular.element(template);
        $compile(templateElement)(scope);
        el.replaceWith(templateElement);
      }
    };
  })
  .directive('treeWrapper', function($stateParams, $modal, $state, Editointikontrollit, TutkinnonOsanTuonti, Kaanna,
                                     PerusteTutkinnonosa, Notifikaatiot, PerusteenRakenne, Muodostumissaannot) {
    function kaikilleRakenteille(rakenne, f) {
      if (!rakenne || !f) { return; }
      _.forEach(rakenne, function(r) {
        kaikilleRakenteille(r.osat, f);
        f(r);
      });
    }

    return {
      restrict: 'AE',
      transclude: true,
      terminal: true,
      templateUrl: 'views/partials/tree.html',
      scope: {
        rakenne: '=',
        voiLiikuttaa: '=',
        ajaKaikille: '=',
        muokkaus: '='
      },
      link: function(scope) {
        scope.suljettuViimeksi = true;
        scope.lisataanUuttaOsaa = false;
        scope.uusiOsa = null;
        scope.skratchpad = [];
        scope.uniikit = [];
        scope.topredicate = 'nimi.fi';
        scope.tosarajaus = '';

        scope.$watch('rakenne.$suoritustapa', function() {
          scope.apumuuttujat = {
            suoritustapa: scope.rakenne.$suoritustapa
          };
        });

        function paivitaUniikit() {
          scope.uniikit = [];
          scope.uniikit = _.map(scope.rakenne.tutkinnonOsat, function(osa) {
            return {  _tutkinnonOsa: osa._tutkinnonOsa };
          });
        }
        paivitaUniikit();

        scope.sortableOptions = {
          placeholder: 'placeholder',
          connectWith: '.tree-group',
          disabled: !scope.muokkaus,
          delay: 100,
          //tolerance: 'pointer',
          //cursorAt: { top : 2, left: 2 },
          cursor: 'move',
          start: function(e, ui) {
            ui.placeholder.html('<div class="group-placeholder"></div>');
          }
        };

        scope.sortableOptionsUnique = {
          placeholder: 'placeholder',
          connectWith: '.tree-group',
          disabled: !scope.muokkaus,
          delay: 100,
          //tolerance: 'pointer',
          //cursorAt: { top : 2, left: 2 },
          cursor: 'move',
          stop: function() { paivitaUniikit(); },
          start: function(e, ui) {
            ui.placeholder.html('<div class="group-placeholder"></div>');
          }
        };

        scope.$watch('muokkaus', function() {
          scope.sortableOptions.disabled = !scope.muokkaus;
          scope.sortableOptionsUnique.disabled = !scope.muokkaus;
        });

        scope.ryhmaModaali = Muodostumissaannot.ryhmaModaali(function(ryhma, vanhempi, uusiryhma) {
          if (uusiryhma) {
            if (ryhma === undefined) { scope.skratchpad.push(uusiryhma); }
            else { ryhma = uusiryhma; }
          }
          else { _.remove(scope.skratchpad, ryhma); }
        });

        scope.paivitaRajaus = function(rajaus) { scope.tosarajaus = rajaus; };
        scope.rajaaTutkinnonOsia = function(haku) {
          return Kaanna.kaanna(haku.nimi).toLowerCase().indexOf(scope.tosarajaus.toLowerCase()) !== -1;
        };

        scope.suljePolut = function() {
          scope.rakenne.rakenne.$collapsed = scope.suljettuViimeksi;
          kaikilleRakenteille(scope.rakenne.rakenne.osat, function(osa) {
            osa.$collapsed = scope.suljettuViimeksi;
          });
          scope.suljettuViimeksi = !scope.suljettuViimeksi;
        };

        scope.tuoTutkinnonosa = TutkinnonOsanTuonti.modaali(scope.rakenne.$suoritustapa, function(osat) {
          var after = _.after(_.size(osat), function() { paivitaUniikit(); });
          _.forEach(osat, function(osa) { scope.lisaaTutkinnonOsa(osa, after); });
        });

        scope.lisaaTutkinnonOsa = function(osa, cb) {
          if (osa) {
            osa = { _tutkinnonOsa: osa._tutkinnonOsa };
          }
          else { osa =  {}; }
          cb = cb || function(){};

          PerusteTutkinnonosa.save({
            perusteenId: scope.rakenne.$peruste.id,
            suoritustapa: scope.rakenne.$suoritustapa
          }, osa, function(res) {
            scope.rakenne.tutkinnonOsat[res._tutkinnonOsa] = res;
            cb();
          }, function(err) {
            Notifikaatiot.fataali('tallennus-epäonnistui', err);
            cb();
          });
        };

        scope.poistaTutkinnonOsa = function(v) {
          PerusteenRakenne.poistaTutkinnonOsaViite(v, scope.rakenne.$peruste.id, scope.rakenne.$suoritustapa, function() {
            delete scope.rakenne.tutkinnonOsat[v._tutkinnonOsa];
          });
        };

        scope.uusiTutkinnonOsa = function(cb) {
          scope.lisataanUuttaOsaa = true;
          cb();
        };
      }
    };
  })
  .controller('MuodostumisryhmaModalCtrl', function($scope, $modalInstance, ryhma, vanhempi, suoritustapa, Varmistusdialogi) {
    $scope.vanhempi = vanhempi;
    $scope.suoritustapa = suoritustapa;
    console.log($scope.suoritustapa);

    var msl = ryhma && ryhma.muodostumisSaanto && ryhma.muodostumisSaanto.laajuus ? ryhma.muodostumisSaanto.laajuus : null;
    var msk = ryhma && ryhma.muodostumisSaanto && ryhma.muodostumisSaanto.koko ? ryhma.muodostumisSaanto.koko : null;

    $scope.ms = {
      laajuus: msl ? true : false,
      koko: msk ? true : false
    };

    $scope.luonti = !_.isObject(ryhma);
    $scope.ryhma = ryhma ? angular.copy(ryhma) : {};
    if (!$scope.ryhma.muodostumisSaanto) { $scope.ryhma.muodostumisSaanto = {}; }
    if (!$scope.ryhma.nimi) { $scope.ryhma.nimi = {}; }
    if (!$scope.ryhma.kuvaus) { $scope.ryhma.kuvaus = {}; }

    $scope.ok = function(uusiryhma) {
      if (uusiryhma) {
        if (uusiryhma.osat === undefined) { uusiryhma.osat = []; }
        if (!$scope.ms.laajuus) { delete uusiryhma.muodostumisSaanto.laajuus; }
        if (!$scope.ms.koko) { delete uusiryhma.muodostumisSaanto.koko; }
      }
      $modalInstance.close(uusiryhma);
    };

    $scope.poista = function () {
      Varmistusdialogi.dialogi({
        otsikko: 'poistetaanko-ryhma',
        successCb: function () {
          $scope.ok(null);
        }
      })();
    };

    $scope.peruuta = function() { $modalInstance.dismiss(); };
  });
