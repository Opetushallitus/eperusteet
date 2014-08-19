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

  .controller('Tutke2KentatController', function ($scope, Tutke2Osa) {
    $scope.viewOptions = {
      oneAtATime: false
    };

    $scope.tutkinnonosa.then(function (res) {
      $scope.tutke2osa = Tutke2Osa.init(res.id);
      $scope.tutke2osa.fetch();
    });

    function getChoiceName(key) {
      return $scope.tutke2osa.tavoiteMap[key].nimi;
    }

    $scope.osaAlue = {
      hasChoices: function (alue) {
        return !_.isEmpty(alue.$groups) && _.keys(alue.$groups.grouped).length > 1;
      },
      groups: function (alue) {
        var ret = _.map(_.keys(alue.$groups.grouped), function (key) {
          return {label: getChoiceName(key), value: key};
        });
        return ret;
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

    Tutke2OsaImpl.prototype.fetch = function () {
      var that = this;
      $q.all([
        TutkinnonOsanOsaAlue.list(this.params).$promise
      ]).then(function (data) {
        that.osaAlueet = data[0];
        _.each(that.osaAlueet, function (alue) {
          if (alue.nimi === null) {
            alue.nimi = {};
          }
          alue.$open = true;
          that.getTavoitteet(alue, alue);
        });
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
        osaAlue.$chosen = _.first(_.keys(osaAlue.$groups.grouped));
      });
    };

    function fixTavoite(tavoite) {
      if (_.isEmpty(tavoite)) {
        tavoite = {};
      }
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
      // TODO valinnainen -> pakollinen linkit
      groups.ungrouped = _.difference(tavoitteet, processed);
      return groups;
    }

    return {
      init: function (tutkinnonOsaId) {
        return new Tutke2OsaImpl(tutkinnonOsaId);
      }
    };
  });
