'use strict';
/* global _ */

angular.module('eperusteApp')
  .controller('RakenneosaModalCtrl', function ($scope, $modalInstance, rakenneosa, Koodisto) {
    var setupRyhma = function(rakenneosa) {
      $scope.rakenneosa = _.cloneDeep(rakenneosa);
      if (!$scope.rakenneosa.kuvaus || !_.isObject($scope.rakenneosa.kuvaus)) {
        $scope.rakenneosa.kuvaus = {};
      }
    };
    setupRyhma(rakenneosa);

    $scope.vieraskoodiModaali = Koodisto.modaali(function(koodi) {
      $scope.rakenneosa.vieras = _.pick(koodi, 'nimi', 'koodiArvo', 'koodiUri');
      $scope.rakenneosa.vieras = {
        nimi: koodi.nimi,
        uri: koodi.koodiUri,
        arvo: koodi.koodiArvo,
      };
    }, {
      tyyppi: function() { return 'tutkinnonosat'; },
      ylarelaatioTyyppi: function() { return ''; }
    }, angular.noop, null);

    $scope.ok = function() {
      $modalInstance.close($scope.rakenneosa);
    };

    $scope.peruuta = $modalInstance.dismiss;
  });
