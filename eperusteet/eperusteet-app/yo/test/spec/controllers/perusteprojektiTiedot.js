'use strict';

describe('Controller: ProjektinTiedotCtrl', function () {

  // load the controller's module
  beforeEach(module('eperusteApp'));

  var PerusteprojektiCtrl,
    scope,
    koulutusalaService, 
    opintoalaService;
    
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
  
  // Mock koulutusalaService
  beforeEach(module(function($provide) {
    opintoalaService = {
      haeOpintoalat: function() {
        return [];
      },
      haeOpintoalaNimi: function(koodi) {
        return {};
      }
    };
    $provide.value('opintoalaService', opintoalaService);
  }));

  // Initialize the controller and a mock scope
  beforeEach(inject(function ($controller, $rootScope) {
    scope = $rootScope.$new();
    PerusteprojektiCtrl = $controller('ProjektinTiedotCtrl', {
      $scope: scope
    });
  }));

  it('should work', function () {
    //TODO
  });
});
