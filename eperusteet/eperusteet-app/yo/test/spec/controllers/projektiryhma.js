'use strict';

describe('Controller: ProjektiryhmaCtrl', function () {

  // load the controller's module
  beforeEach(module('eperusteApp'));

  var ProjektiryhmaCtrl,
    scope;

  // Initialize the controller and a mock scope
  beforeEach(inject(function ($controller, $rootScope) {
    scope = $rootScope.$new();
    ProjektiryhmaCtrl = $controller('ProjektiryhmaCtrl', {
      $scope: scope
    });
  }));

  it('should work', function () {
    //TODO
  });
});
