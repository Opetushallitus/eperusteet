'use strict';
/* global _ */
/* global XLSX */

function generateTableHeaders() {
  var theaders = {};
  var count = 0;
  _.each(_.range(26), function(i) {
    theaders[count++] = String.fromCharCode(65 + i);
  });
  _.each(_.range(26), function(i) {
    _.each(_.range(26), function(j) {
      theaders[count++] = String.fromCharCode(65 + i) + String.fromCharCode(65 + j);
    });
  });
  return theaders;
}

angular.module('eperusteApp')
  .config(function($routeProvider) {
    $routeProvider
      .when('/excel', {
        templateUrl: 'views/excel.html',
        controller: 'ExcelCtrl'
      });
  })
  .controller('ExcelCtrl', function($scope, $q) {
    // Konvertoi parsitun XLSX-tiedoston perusteen osiksi.
    // $q:n notifyä käytetään valmistumisen päivittämiseen.
    // Palauttaa lupauksen.
    function toJson(parsedxlsx) {
      var deferred = $q.defer();
      if (_.isEmpty(parsedxlsx.SheetNames)) {
        deferred.reject(1);
      } else {
        var theaders = generateTableHeaders();
        var perusteenosat = [];
        var name = parsedxlsx.SheetNames[0];
        var sheets = _(parsedxlsx.Sheets[name]).filter(function(value, key) {
          return value.v !== undefined;
        }).value();
        var dimensions = _.first(sheets);
        console.log(theaders);
        sheets = _.rest(sheets);
        // console.log(parsedxlsx.Sheets[name]);
        // console.log(name, dimensions, sheets);
        deferred.resolve(perusteenosat);
      }
      return deferred.promise;
    }

    function parseXLSXToJSON(file) {
      return toJson(XLSX.read(file, { type: 'binary' }));
    }

    $scope.onFileSelect = function(err, file) {
      if (err || !file) {
        // TODO: Hoida virhetilanteet
      } else {
        var promise = parseXLSXToJSON(file);
        promise.then(function(resolve) {
          console.log(resolve);
        }, function(err) {
          console.log(err);
        }, function(event) {
          console.log(event);
        });
      }
    };
    $scope.onProgress = function(state) {
      // TODO: spinner
      console.log(state);
    };
  });
