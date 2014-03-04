'use strict';

angular.module('eperusteApp')
  .controller('ProjektiryhmaCtrl', function($scope, PerusteProjektiService) {
    PerusteProjektiService.watcher($scope, 'projekti');
});