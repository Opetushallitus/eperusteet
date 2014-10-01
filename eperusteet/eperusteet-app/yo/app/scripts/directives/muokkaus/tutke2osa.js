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
/*global _*/

angular.module('eperusteApp')
  .directive('tutke2kentat', function () {
    return {
      restrict: 'AE',
      templateUrl: 'views/partials/muokkaus/tutke2kentat.html',
      scope: {
        mainLevelEditing: '=editEnabled',
        tutkinnonosa: '=',
        kontrollit: '='
      },
      controller: 'Tutke2KentatController'
    };
  })

  .service('Tutke2OsaData', function () {
    this.data = null;
    this.set = function (data) {
      this.data = data;
    };
    this.get = function () {
      return this.data;
    };
  })

  .controller('Tutke2KentatController', function ($scope, Tutke2Osa, Tutke2OsaData,
    TutkinnonOsanOsaAlue, Osaamistavoite, Varmistusdialogi, $rootScope, $timeout,
    Utils, Notifikaatiot, Lukitus, $q, YleinenData) {

    var Editointikontrollit = $scope.kontrollit;
    var tutke2osaDefer = $q.defer();

    $scope.valitseKieli = _.bind(YleinenData.valitseKieli, YleinenData);

    function lukitse(cb) {
      Lukitus.lukitsePerusteenosa($scope.tutke2osa.tutkinnonOsaId, cb);
    }

    function vapauta() {
      Lukitus.vapautaPerusteenosa($scope.tutke2osa.tutkinnonOsaId);
    }

    $scope.viewOptions = {
      oneAtATime: false
    };

    $scope.pakollisuusoptions = [
      {label: 'pakollinen', value: true},
      {label: 'valinnainen', value: false},
    ];

    $scope.tutkinnonosa.then(function (res) {
      $scope.tutke2osa = Tutke2Osa.init(res.id);
      $scope.tutke2osa.fetch().then(function () {
        tutke2osaDefer.resolve();
      });
      Tutke2OsaData.set($scope.tutke2osa);
      editModeCallback($scope.mainLevelEditing);
    });

    $scope.isEditingInProgress = function () {
      return $scope.osaAlue.$editing || $scope.osaamistavoite.$editing;
    };

    function stopEvent(event) {
      if (event) {
        event.stopPropagation();
      }
    }

    function verifyRemove(cb) {
      Varmistusdialogi.dialogi({
        otsikko: 'vahvista-poisto',
        teksti: 'poistetaanko',
        primaryBtn: 'poista',
        successCb: cb
      })();
    }

    function saveCb() {
      $scope.tutke2osa.fetch();
      vapauta();
    }

    $scope.osaAlue = {
      $editing: null,
      jumpTo: function (alue) {
        Utils.scrollTo('#' + alue.$uniqueId);
      },
      add: function () {
        var newAlue = {
          nimi: {}
        };
      $scope.tutke2osa.$editing.push(newAlue);
      },
      edit: function (alue, $event, state) {
        stopEvent($event);
        state = _.isUndefined(state) || state;
        if (state) {
          Editointikontrollit.registerCallback($scope.osaAlue.callbacks);
          lukitse(function () {
            Editointikontrollit.startEditing();
          });
          alue.$open = true;
          alue.$editing = true;
          $scope.osaAlue.$editing = angular.copy(alue);
          $scope.original = alue;
        } else {
          if ($scope.original) {
            $scope.original.$editing = false;
          }
          $scope.osaAlue.$editing = null;
        }
      },
      remove: function (alue) {
        if (alue.id) {
          verifyRemove(function () {
            _.remove($scope.tutke2osa.$editing, alue);
          });
        } else {
          _.remove($scope.tutke2osa.$editing, alue);
        }
      },
      removeDirect: function (alue, $event) {
        stopEvent($event);
        verifyRemove(function () {
          if (alue.id) {
            alue.$delete($scope.tutke2osa.params,  function() {
              $scope.tutke2osa.fetch();
              Editointikontrollit.cancelEditing();
            }, Notifikaatiot.serverCb);
          }

        });
      },
      save: function (alue, $event, kommentti) {
        stopEvent($event);
        alue = alue || $scope.osaAlue.$editing;
        var stripped = _.omit(alue, 'osaamistavoitteet');
        stripped.osaamistavoitteet = _.map(alue.osaamistavoitteet, function (tavoite) {
          return _.omit(tavoite, 'tunnustaminen', 'tavoitteet', 'arviointi', '_esitieto');
        });
        if (kommentti) {
          // TODO kommentille tuki
          //stripped.metadata = {kommentti: kommentti};
        }
        $scope.osaAlue.edit(alue, null, false);

        if (alue.id) {
          TutkinnonOsanOsaAlue.save(_.extend({
            osaalueenId: alue.id
          }, $scope.tutke2osa.params), stripped, saveCb, Notifikaatiot.serverCb);
        } else {
          TutkinnonOsanOsaAlue.save($scope.tutke2osa.params, stripped, saveCb, Notifikaatiot.serverCb);
        }

      },
      callbacks: {
        edit: angular.noop,
        save: function(kommentti) {
          $scope.osaAlue.save(null, null, kommentti);
        },
        cancel: function() {
          $scope.osaAlue.edit(null, null, false);
          vapauta();
        },
        validate: function() {
          return Utils.hasLocalizedText($scope.osaAlue.$editing.nimi) &&
            _.all(_.map($scope.osaAlue.$editing.osaamistavoitteet, function (tavoite) {
            return Utils.hasLocalizedText(tavoite.nimi);
          }));
        }
      }
    };

    $scope.osaamistavoite = {
      $editing: null,
      isLinked: function (alue, tavoite) {
        var linked = _.find(alue.osaamistavoitteet, function (item) {
          return '' + item._esitieto === '' + tavoite.id;
        });
        return !!linked;
      },
      add: function (alue) {
        var newTavoite = {pakollinen: true};
        Tutke2Osa.fixTavoite(newTavoite);
        if (!alue.osaamistavoitteet) {
          alue.osaamistavoitteet = [];
        }
        alue.osaamistavoitteet.push(newTavoite);
      },
      edit: function (alue, tavoite, $event, state) {
        stopEvent($event);
        state = _.isUndefined(state) || state;
        if (state) {
          Editointikontrollit.registerCallback($scope.osaamistavoite.callbacks);
          lukitse(function () {
            Editointikontrollit.startEditing();
          });
          $scope.tavoitteenAlue = alue;
          tavoite.$editing = true;
          $scope.osaamistavoite.$editing = angular.copy(tavoite);
          $scope.original = tavoite;
        } else {
          $scope.original.$editing = false;
          $scope.osaamistavoite.$editing = null;
        }
      },
      remove: function (tavoite) {
        if (tavoite.id) {
          verifyRemove(function () {
            _.remove($scope.osaAlue.$editing.osaamistavoitteet, tavoite);
          });
        } else {
          _.remove($scope.osaAlue.$editing.osaamistavoitteet, tavoite);
        }
      },
      removeDirect: function (alue, tavoite, $event) {
        stopEvent($event);
        verifyRemove(function () {
          if (tavoite.id) {
            var params = _.extend({osaalueenId: alue.id}, $scope.tutke2osa.params);
            tavoite.$delete(params, function() {
              $scope.tutke2osa.getTavoitteet(alue, alue);
              Editointikontrollit.cancelEditing();
            }, Notifikaatiot.serverCb);
          }
        });
      },
      save: function () {
        var alue = $scope.tavoitteenAlue;
        var tavoite = $scope.osaamistavoite.$editing;
        var payload;
        if (!tavoite.id) {
          payload = angular.copy(tavoite);
        }
        $scope.osaamistavoite.edit(null, null, null, false);
        $rootScope.$broadcast('notifyCKEditor');

        if (tavoite.pakollinen) {
          tavoite._esitieto = null;
        }
        var params = _.extend({osaalueenId: alue.id}, $scope.tutke2osa.params);
        if (tavoite.id) {
          tavoite.$save(params, function () {
            $scope.tutke2osa.getTavoitteet(alue, alue);
          }, Notifikaatiot.serverCb);
        } else {
          Osaamistavoite.save(params, payload, function () {
            $scope.tutke2osa.getTavoitteet(alue, alue);
          }, Notifikaatiot.serverCb);
        }
      },
      callbacks: {
        edit: angular.noop,
        save: function(/*kommentti*/) {
          // TODO kommentille tuki
          $scope.osaamistavoite.save();
        },
        cancel: function() {
          $scope.osaamistavoite.edit(null, null, null, false);
          vapauta();
        },
        validate: function() {
          return Utils.hasLocalizedText($scope.osaamistavoite.$editing.nimi);
        }
      }
    };

    function editModeCallback(editing) {
      if (!$scope.tutke2osa) {
        return;
      }
      if (editing) {
        tutke2osaDefer.promise.then(function () {
          $scope.tutke2osa.$editing = angular.copy($scope.tutke2osa.osaAlueet);
        });
      } else {
        $scope.tutke2osa.$editing = null;
      }
    }

    $scope.$watch('mainLevelEditing', editModeCallback);

    $scope.getTavoitteet = function (alue, pakollinen) {
      if (pakollinen) {
        return alue.$groups ? _.filter(alue.$groups.grouped[alue.$chosen], 'pakollinen') : [];
      }
      var grouped = alue.$groups ? _.reject(alue.$groups.grouped[alue.$chosen], 'pakollinen') : [];
      var ungrouped = alue.$groups ? alue.$groups.ungrouped : [];
      return grouped.concat(ungrouped);
    };
  })

  .factory('Tutke2Osa', function ($q, TutkinnonOsanOsaAlue, Osaamistavoite) {
    var unique = 0;
    function Tutke2OsaImpl(tutkinnonOsaId) {
      this.tutkinnonOsaId = tutkinnonOsaId;
      this.params = { osanId: tutkinnonOsaId };
      this.tavoiteMap = {};
    }

    Tutke2OsaImpl.prototype.fetch = function (skipTavoitteet) {
      var that = this;
      //this.$fetching = true;
      var deferred = $q.all([
        TutkinnonOsanOsaAlue.list(this.params).$promise
      ]).then(function (data) {
        that.osaAlueet = data[0];
        _.each(that.osaAlueet, function (alue) {
          if (alue.nimi === null) {
            alue.nimi = {};
          }
          alue.$open = true;
          alue.$uniqueId = 'osa-alue-' + unique++;
          if (!skipTavoitteet) {
            that.getTavoitteet(alue, alue);
          } else {
            //that.$fetching = false;
          }
        });
      }, function () {
        //that.$fetching = false;
      });
      return deferred;
    };

    Tutke2OsaImpl.prototype.getTavoitteet = function (osaAlue, arr) {
      var that = this;
      var params = _.extend({osaalueenId: osaAlue.id}, this.params);
      Osaamistavoite.list(params, function (res) {
        _.each(res, function (tavoite) {
          fixTavoite(tavoite);
          that.tavoiteMap[tavoite.id] = tavoite;
        });
        arr.osaamistavoitteet = res;
        osaAlue.$groups = groupTavoitteet(res, that.tavoiteMap);
        var pakollinenIds = _.keys(osaAlue.$groups.grouped);
        osaAlue.$esitietoOptions = _.map(pakollinenIds, function (id) {
          return {value: id, label: osaAlue.$groups.grouped[id][0].nimi};
        });
        osaAlue.$esitietoOptions.unshift({value: null, label: '<ei asetettu>'});
        osaAlue.$chosen = _.first(pakollinenIds);
        //that.$fetching = false;
      }, function () {
        //that.$fetching = false;
      });
    };

    function fixTavoite(tavoite) {
      _.each(['nimi', 'tunnustaminen', 'tavoitteet'], function (key) {
        if (_.isEmpty(tavoite[key])) {
          tavoite[key] = {};
        }
      });

      if (_.isEmpty(tavoite.arviointi)) {
        tavoite.arviointi = {
          lisatiedot: null,
          arvioinninKohdealueet: []
        };
      }
    }

    function groupTavoitteet(tavoitteet, tavoiteMap) {
      var groups = {
        grouped: {},
        ungrouped: {}
      };
      var processed = [];
      _.each(tavoitteet, function (tavoite) {
        if (tavoite.pakollinen) {
          groups.grouped[tavoite.id] = [tavoite];
          processed.push(tavoite);
        }
      });
      _.each(tavoitteet, function (tavoite) {
        if (!tavoite.pakollinen && tavoite._esitieto &&
            _.has(groups.grouped, tavoite._esitieto)) {
          groups.grouped[tavoite._esitieto].push(tavoite);
          processed.push(tavoite);
        }
      });
      groups.ungrouped = _.difference(tavoitteet, processed);
      groups.$size = _.size(groups.grouped);
      groups.$options = _.map(_.keys(groups.grouped), function (key) {
        return {label: tavoiteMap[key].nimi, value: key};
      });
      return groups;
    }

    return {
      init: function (tutkinnonOsaId) {
        return new Tutke2OsaImpl(tutkinnonOsaId);
      },
      fixTavoite: fixTavoite
    };
  });
