'use strict';

describe('Controller: ProjektinPerusteCtrl', function () {

  // load the controller's module
  beforeEach(module('eperusteApp'));

  var ProjektinperusteCtrl,
    scope;

  // Initialize the controller and a mock scope
  beforeEach(inject(function ($controller, $rootScope) {
    
    scope = $rootScope.$new();
    ProjektinperusteCtrl = $controller('ProjektinPerusteCtrl', {
      $scope: scope
    });
  }));

  it('should work', function () {
    //TODO
  });
});
