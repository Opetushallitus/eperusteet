'use strict';
/*global _*/
/*global $*/

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
  .directive('tree', function($compile, $state, $modal, $timeout) {
    function validoiRyhma(rakenne, tutkinnonOsat) {
      if (!rakenne) return;

      delete rakenne.$virhe;

      if (rakenne.muodostumisSaanto) {
        var minimi, maksimi;

        if (rakenne.muodostumisSaanto.laajuus) {
          var msl = rakenne.muodostumisSaanto.laajuus;
          if (msl.minimi) {
            var summa = _(rakenne.osat)
              .map(function(osa) { return osa.$laajuus; })
              .reduce(function(sum, newval) { return sum + newval; });
            if (summa < msl.minimi) {
              rakenne.$virhe = 'muodostumis-rakenne-validointi-1';
            }
          }
        }

        if (rakenne.muodostumisSaanto.koko) {
          var msk = rakenne.muodostumisSaanto.koko;
          if (_.size(rakenne.osat) < msk.minimi) {
            rakenne.$virhe = 'muodostumis-rakenne-validointi-2';
          }
        }

        var tosat = _(rakenne.osat).filter(function(osa) { return osa._tutkinnonOsa; }).value();
        if (_.size(tosat) !== _(tosat).uniq('_tutkinnonOsa').size()) {
            rakenne.$virhe = 'muodostumis-rakenne-validointi-3';
        }
      }
    }

    function laskeLaajuudet(rakenne, tutkinnonOsat) {
      if (!rakenne) { return; }
      if (rakenne._tutkinnonOsa) {
        rakenne.$laajuus = tutkinnonOsat[rakenne._tutkinnonOsa].laajuus;
      } else if (rakenne.osat) {
        rakenne.$laajuus = 0;
        _.forEach(rakenne.osat, function(osa) {
          laskeLaajuudet(osa, tutkinnonOsat);
          rakenne.$laajuus += osa.$laajuus || 0;
        });
      } else {
        console.log('jokin meni pieleen');
      }
    }

    function generoiOtsikko() {
      var tosa = 'tutkinnonOsat[rakenne._tutkinnonOsa]';
      var otsikko = '' +
        '<span ng-if="rakenne._tutkinnonOsa">{{ ' + tosa + '.nimi.fi }} {{' + tosa + '.laajuus }}ov</span>' +
        '<span ng-if="!rakenne._tutkinnonOsa && rakenne.muodostumisSaanto === undefined && rakenne.otsikko && rakenne.otsikko.fi.length > 0">{{ rakenne.otsikko.fi }}</span>' +
        '<span ng-if="rakenne.muodostumisSaanto !== undefined">' +
        '  <span ng-if="rakenne.muodostumisSaanto.laajuus">' +
        '    <span ng-if="rakenne.muodostumisSaanto.laajuus.minimi === rakenne.muodostumisSaanto.laajuus.maksimi">' +
        '      Valitse seuraavista vähintään {{ rakenne.muodostumisSaanto.laajuus.minimi }}ov edestä' +
        '    </span>' +
        '    <span ng-if="rakenne.muodostumisSaanto.laajuus.minimi !== rakenne.muodostumisSaanto.laajuus.maksimi">' +
        '      Valitse seuraavista vähintään {{ rakenne.muodostumisSaanto.laajuus.minimi }} ja enintään {{ rakenne.muodostumisSaanto.laajuus.maksimi }}ov edestä' +
        '    </span>' +
        '  </span>' +
        '  <span ng-if="rakenne.muodostumisSaanto.koko">' +
        '    <span ng-if="rakenne.muodostumisSaanto.koko.minimi === rakenne.muodostumisSaanto.koko.maksimi">' +
        '      Valitse seuraavista vähintään {{ rakenne.muodostumisSaanto.koko.minimi }}' +
        '    </span>' +
        '    <span ng-if="rakenne.muodostumisSaanto.koko.minimi !== rakenne.muodostumisSaanto.koko.maksimi">' +
        '      Valitse seuraavista vähintään {{ rakenne.muodostumisSaanto.koko.minimi }} ja enintään {{ rakenne.muodostumisSaanto.koko.maksimi }}' +
        '    </span>' +
        '  </span>' +
        '</span>' +
        '';
      return otsikko;
    }

    function generoiOptiot(rakenne, tutkinnonOsat) {
      var url = '';
      if (rakenne._tutkinnonOsa) {
        url = $state.href('muokkaus.vanha', { perusteenOsanId: rakenne._tutkinnonOsa, perusteenOsanTyyppi: 'tutkinnonosa' });
      }
      return '';
        // '<span ng-show="naytaTyokalut">' +
        // '<span>' +
        // '<a ng-if="!rakenne._perusteenOsa" href="" ng-click="muokkaa(rakenne, vanhempi)"><span class="glyphicon glyphicon-pencil"></a>' +
        // '<a ng-if="rakenne._perusteenOsa" ng-href="' + url + '"><span class="glyphicon glyphicon-pencil"></a>' +
        // '<span class="dropdown"><a ng-hide="lisaaUusi" href="" class="dropdown-toggle"><span class="glyphicon glyphicon-plus"></span> Lisää</a>' +
        // '  <ul class="dropdown-menu">' +
        // '    <li><a href="" ng-click="uusiTutkinnonOsa(liitaUusiTutkinnonOsa)">Uusi tutkinnon osa</a></li>' +
        // '    <li><a href="" ng-click="tuoTutkinnonOsa()">Hae tutkinnon osa</a></li>' +
        // '    <li><a href="" ng-click="lisaaUusi = 2">Ryhmä</a></li>' +
        // '  </ul>' +
        // '</span>' +
        // '<a href="" ng-click="poista(rakenne, vanhempi)"><span class="glyphicon glyphicon-remove"></a>' +
        // '</span>';
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
        'muokkaus': '='
      },
      link: function(scope, el) {
        scope.lisaaUusi = 0;
        scope.lisataanUuttaPerusteenOsaa = false;
        scope.scratchpad = [];
        scope.roskakori = [];

        // _.forEach(scope.rakenne.osat, function(osa) {
        //   osa.$parent = scope.rakenne;
        // });

        function liitaUusiTutkinnonOsa(osa) {
          scope.rakenne.osat.push({
            otsikko: { fi: 'Uusi' },
            kuvaus: { fi: '' },
            _tutkinnonOsa: 1337,
            muodostumisSaanto: {
              maara: 10,
              yksikko: 'ov',
              tyyppi: 'laajuus',
              pakollinen: false
            }
          });
        }

        scope.poista = function(i, a) { _.remove(a.osat, i); };

        scope.ryhmaModaali = function(ryhma, vanhempi) {
          $modal.open({
            templateUrl: 'views/modals/ryhmaModal.html',
            controller: 'MuodostumisryhmaModalCtrl',
            resolve: { ryhma: function() { return ryhma; } }
          }).result.then(function(uusiryhma) {
            var indeksi = scope.vanhempi.osat.indexOf(ryhma);
            if (indeksi !== -1) {
              scope.vanhempi.osat[indeksi] = uusiryhma;
            }
          });
        };

        scope.$watch('rakenne', function(uusirakenne) {
          laskeLaajuudet(scope.rakenne, scope.tutkinnonOsat);
          validoiRyhma(uusirakenne, scope.tutkinnonOsat);
        }, true);

        scope.sortableOptions = {
          placeholder: 'group-placeholder',
          connectWith: '.tree-group',
          disabled: !scope.muokkaus,
          delay: 100,
          // tolerance: 'pointer',
          cursorAt: { top : 2, left: 2 },
          cursor: 'move',
        };

        scope.liitaUusiTutkinnonOsa = liitaUusiTutkinnonOsa;

        var optiot = '' +
          '<div ng-mouseenter="rakenne.$naytaMuokkaa = true" ng-mouseleave="rakenne.$naytaMuokkaa = false">' +
          '  <span ng-if="!rakenne._tutkinnonOsa">' +
          '    <a ng-hide="rakenne.$collapsed" href="" ng-click="rakenne.$collapsed = !rakenne.$collapsed"><span class="glyphicon glyphicon-chevron-down"></span></a>' +
          '    <a ng-show="rakenne.$collapsed" href="" ng-click="rakenne.$collapsed = !rakenne.$collapsed"><span class="glyphicon glyphicon-chevron-up"></span></a>' +
          '  </span> ' +
          '  <span class="tree-item" ng-click="rakenne.$laajenna = !rakenne.$laajenna">' + generoiOtsikko() + '</span>' +
          '  <span ng-if="!rakenne._tutkinnonOsa && muokkaus" class="pull-right">' +
          '    <a href="" ng-click="ryhmaModaali(rakenne, vanhempi)">Muokkaa ryhmää <span class="glyphicon glyphicon-chevron-right"></span></a>' +
          '  </span>' +
          '</div>';
        optiot += generoiOptiot(scope.rakenne, scope.tutkinnonOsat);
        var kentta = '<div ng-if="rakenne._tutkinnonOsa" class="bubble-osa">' + optiot + '</div>';
        kentta += '<div ng-if="!rakenne._tutkinnonOsa" class="bubble">' + optiot + '</div>';
        kentta += '<div ng-model="rakenne" ng-show="muokkaus && rakenne.$virhe" class="virhe"><span>{{ rakenne.$virhe | translate }}</span></div>'
        kentta += '<div ng-show="rakenne.$laajenna" class="beef">' +
          '<div ng-if="rakenne._tutkinnonOsa">20ov</div>' +
          '<div ng-if="rakenne.kuvaus">{{ rakenne.kuvaus.fi }}</div>' +
          '</div>';

        var template = '';

        if (scope.rakenne && _.isArray(scope.rakenne.osat)) {
          template =
            '<div>' +
            '<div grab-cursor>' +
            kentta +
            '</div>' +
            '<div ng-show="!rakenne.$collapsed">';

          template +=
            '<ul ui-sortable="sortableOptions" id="tree-sortable" class="tree-group" ng-model="rakenne.osat">' +
            '  <li ng-repeat="osa in rakenne.osat">' +
            '    <tree muokkaus="muokkaus" rakenne="osa" vanhempi="rakenne" tutkinnon-osat="tutkinnonOsat" uusi-tutkinnon-osa="uusiTutkinnonOsa" ng-init="notfirst = true"></tree>' +
            '  </li>' +
            '</ul>' +
            '</div>';
        } else {
          template = kentta;
        }

        var templateElement = angular.element(template);
        $compile(templateElement)(scope);
        el.replaceWith(templateElement);
      }
    };
  })
  .directive('treeWrapper', function($modal, Editointikontrollit, TutkinnonOsanTuonti) {
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
        'muokkaus': '='
      },
      link: function(scope) {
        scope.suljettuViimeksi = true;
        scope.lisataanUuttaOsaa = false;
        scope.uusiOsa = null;
        scope.skratchpad = [];
        scope.uniikit = [];
        scope.topredicate = 'nimi.fi';

        function paivitaUniikit() {
          scope.uniikit = [];
          scope.uniikit = _.map(scope.rakenne.tutkinnonOsat, function(osa) {
            return _.pick(osa, '_tutkinnonOsa', 'pakollinen');
          });
        }
        paivitaUniikit();

        scope.sortableOptions = {
          placeholder: 'group-placeholder',
          connectWith: '.tree-group',
          disabled: !scope.muokkaus,
          delay: 100,
          // tolerance: 'pointer',
          cursorAt: { top : 2, left: 2 },
          cursor: 'move',
        };

        scope.sortableOptionsUnique = {
          placeholder: 'group-placeholder',
          connectWith: '#tree-sortable',
          disabled: !scope.muokkaus,
          delay: 100,
          // tolerance: 'pointer',
          cursorAt: { top : 2, left: 2 },
          cursor: 'move',
          stop: function() { paivitaUniikit(); }
        };

        scope.ryhmaModaali = function(ryhma) {
          $modal.open({
            templateUrl: 'views/modals/ryhmaModal.html',
            controller: 'MuodostumisryhmaModalCtrl',
            resolve: { ryhma: function() { return ryhma; } }
          }).result.then(function(uusiryhma) {
            if (ryhma === undefined) { scope.skratchpad.push(uusiryhma); }
            else { ryhma = uusiryhma; }
          });
        };

        scope.suljePolut = function() {
          scope.rakenne.rakenne.$collapsed = scope.suljettuViimeksi;
          kaikilleRakenteille(scope.rakenne.rakenne.osat, function(osa) {
            osa.$collapsed = scope.suljettuViimeksi;
          });
          scope.suljettuViimeksi = !scope.suljettuViimeksi;
        };

        scope.tuoTutkinnonosa = TutkinnonOsanTuonti.modaali(function(osa) {
          osa.laajuus = 20; // FIXME: poista kun laajuus tulee osan mukana
          scope.rakenne.tutkinnonOsat[osa.id] = osa;
          scope.skratchpad.push({
            kuvaus: { fi: '' },
            _tutkinnonOsa: osa.id
          });
          paivitaUniikit();
        });

        Editointikontrollit.registerAdditionalSaveCallback(function() {
          scope.lisataanUuttaOsaa = false;
        });

        scope.uusiTutkinnonOsa = function(cb) {
          scope.lisataanUuttaOsaa = true;
          cb();
        };
      }
    };
  })
  .controller('MuodostumisryhmaModalCtrl', function($scope, $modalInstance, ryhma) {
    $scope.ms = {
      laajuus: false,
      koko: false,
    };
    $scope.ryhma = ryhma ? angular.copy(ryhma) : {};
    if (!$scope.ryhma.muodostumisSaanto) { $scope.ryhma.muodostumisSaanto = {}; }
    if (!$scope.ryhma.otsikko) { $scope.ryhma.otsikko = {}; }
    if (!$scope.ryhma.kuvaus) { $scope.ryhma.kuvaus = {}; }
    if ($scope.ryhma.muodostumisSaanto.laajuus) { $scope.ms.laajuus = false; }
    if ($scope.ryhma.muodostumisSaanto.koko) { $scope.ms.koko = false; }

    // $scope.toggleLaajuus = function() { $scope.laajuus = !$scope.laajuus; };
    // $scope.toggleKoko = function() { $scope.koko = !$scope.koko; };

    $scope.ok = function(uusiryhma) {
      if (uusiryhma.osat === undefined) { uusiryhma.osat = []; }

      if ($scope.ms.laajuus && $scope.ms.koko) { uusiryhma = _.omit(uusiryhma, 'muodostumisSaanto'); }
      else if ($scope.ms.laajus) { uusiryhma = _.omit(uusiryhma, 'muodostumisSaanto.laajuus'); }
      else if ($scope.ms.koko) { uusiryhma = _.omit(uusiryhma, 'muodostumisSaanto.koko'); }

      $modalInstance.close(uusiryhma);
    };
    $scope.peruuta = function() { $modalInstance.dismiss(); };
  });
