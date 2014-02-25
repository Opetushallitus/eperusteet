'use strict';

describe('Controller: HakuCtrl', function () {

  // load the controller's module
  beforeEach(module('eperusteApp'));

  var SearchCtrl,
    scope, 
    koulutusalaService;
    
  // Mock koulutusalaService
  beforeEach(module(function($provide) {
    koulutusalaService = {
      haeKoulutusalat: function() {
        return [];
      },
      haeKoulutusalaNimi: function(koodi) {
        return {};
      }
    };
    $provide.value('koulutusalaService', koulutusalaService);
  }));

  // Initialize the controller and a mock scope
  beforeEach(inject(function ($controller, $rootScope) {

    scope = $rootScope.$new();
    SearchCtrl = $controller('HakuCtrl', {
      $scope: scope
    });
  }));

  it('should work', function () {
    //TODO
  });
});
