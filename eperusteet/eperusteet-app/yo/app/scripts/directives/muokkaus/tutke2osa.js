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
        editEnabled: '=',
        tutkinnonosa: '='
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
    TutkinnonOsanOsaAlue, Osaamistavoite, Varmistusdialogi, $rootScope) {
    $scope.viewOptions = {
      oneAtATime: false
    };

    $scope.pakollisuusoptions = [
      {label: 'pakollinen', value: true},
      {label: 'valinnainen', value: false},
    ];

    $scope.tutkinnonosa.then(function (res) {
      $scope.tutke2osa = Tutke2Osa.init(res.id);
      $scope.tutke2osa.fetch();
      Tutke2OsaData.set($scope.tutke2osa);
    });

    function getChoiceName(key) {
      return $scope.tutke2osa.tavoiteMap[key].nimi;
    }

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

    $scope.osaAlue = {
      $editing: null,
      hasChoices: function (alue) {
        return !_.isEmpty(alue.$groups) && _.keys(alue.$groups.grouped).length > 1;
      },
      groups: function (alue) {
        var ret = _.map(_.keys(alue.$groups.grouped), function (key) {
          return {label: getChoiceName(key), value: key};
        });
        return ret;
      },
      add: function () {
        var newAlue = {
          nimi: {}
        };
        $scope.tutke2osa.osaAlueet.push(newAlue);
        $scope.osaAlue.edit(newAlue);
      },
      edit: function (alue, $event, state) {
        stopEvent($event);
        state = _.isUndefined(state) || state;
        if (state) {
          alue.$open = true;
        } else {
          $scope.tutke2osa.fetch();
        }
        $scope.osaAlue.$editing = state ? alue : null;
        alue.$editing = state;
      },
      remove: function (alue, $event) {
        stopEvent($event);
        verifyRemove(function () {
          if (alue.id) {
            alue.$delete($scope.tutke2osa.params);
          }
          $scope.tutke2osa.fetch();
        });
      },
      save: function (alue, $event) {
        stopEvent($event);
        $scope.osaAlue.edit(alue, null, false);

        var stripped = _.omit(alue, 'osaamistavoitteet');
        if (alue.id) {
          TutkinnonOsanOsaAlue.save(_.extend({
            osaalueenId: alue.id
          }, $scope.tutke2osa.params), stripped, function () {
            $scope.tutke2osa.fetch();
          });
        } else {
          TutkinnonOsanOsaAlue.save($scope.tutke2osa.params, stripped, function () {
            $scope.tutke2osa.fetch();
          });
        }

      }
    };

    $scope.osaamistavoite = {
      $editing: null,
      add: function (alue) {
        var newTavoite = {pakollinen: true};
        Tutke2Osa.fixTavoite(newTavoite);
        if (!alue.osaamistavoitteet) {
          alue.osaamistavoitteet = [];
        }
        alue.osaamistavoitteet.push(newTavoite);
        $scope.osaamistavoite.edit(alue, newTavoite);
      },
      edit: function (alue, tavoite, $event, state) {
        stopEvent($event);
        state = _.isUndefined(state) || state;
        tavoite.$editing = state;
        $scope.osaamistavoite.$editing = state ? tavoite : null;
        if (!state && $event) {
          $scope.tutke2osa.getTavoitteet(alue, alue);
        }
      },
      remove: function (alue, tavoite, $event) {
        stopEvent($event);
        verifyRemove(function () {
          if (tavoite.id) {
            var params = _.extend({osaalueenId: alue.id}, $scope.tutke2osa.params);
            tavoite.$delete(params);
          }
          $scope.tutke2osa.getTavoitteet(alue, alue);
        });
      },
      save: function (alue, tavoite, $event) {
        $rootScope.$broadcast('notifyCKEditor');

        stopEvent($event);
        if (tavoite.pakollinen) {
          tavoite._esitieto = null;
        }
        $scope.osaamistavoite.edit(alue, tavoite, null, false);

        var params = _.extend({osaalueenId: alue.id}, $scope.tutke2osa.params);
        if (tavoite.id) {
          tavoite.$save(params, function () {
            $scope.tutke2osa.getTavoitteet(alue, alue);
          });
        } else {
          Osaamistavoite.save(params, angular.copy(tavoite), function () {
            $scope.tutke2osa.getTavoitteet(alue, alue);
          });
        }
      }
    };

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
    function Tutke2OsaImpl(tutkinnonOsaId) {
      this.tutkinnonOsaId = tutkinnonOsaId;
      this.params = { osanId: tutkinnonOsaId };
      this.tavoiteMap = {};
    }

    Tutke2OsaImpl.prototype.fetch = function (skipTavoitteet) {
      var that = this;
      this.$fetching = true;
      $q.all([
        TutkinnonOsanOsaAlue.list(this.params).$promise
      ]).then(function (data) {
        that.osaAlueet = data[0];
        _.each(that.osaAlueet, function (alue) {
          if (alue.nimi === null) {
            alue.nimi = {};
          }
          alue.$open = true;
          if (!skipTavoitteet) {
            that.getTavoitteet(alue, alue);
          } else {
            that.$fetching = false;
          }
        });
      }, function () {
        that.$fetching = false;
      });
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
        osaAlue.$groups = groupTavoitteet(res);
        var pakollinenIds = _.keys(osaAlue.$groups.grouped);
        osaAlue.$esitietoOptions = _.map(pakollinenIds, function (id) {
          return {value: id, label: osaAlue.$groups.grouped[id][0].nimi};
        });
        osaAlue.$esitietoOptions.unshift({value: null, label: '<ei asetettu>'});
        osaAlue.$chosen = _.first(pakollinenIds);
        that.$fetching = false;
      }, function () {
        that.$fetching = false;
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

    function groupTavoitteet(tavoitteet) {
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
        if (!tavoite.pakollinen && tavoite._esitieto) {
          groups.grouped[tavoite._esitieto].push(tavoite);
          processed.push(tavoite);
        }
      });
      groups.ungrouped = _.difference(tavoitteet, processed);
      return groups;
    }

    return {
      init: function (tutkinnonOsaId) {
        return new Tutke2OsaImpl(tutkinnonOsaId);
      },
      fixTavoite: fixTavoite
    };
  });
