'use strict';

describe('Controller: ProjektinperustiedotCtrl', function () {

  // load the controller's module
  beforeEach(module('eperusteApp'));

  var ProjektinperustiedotCtrl,
    scope;

  // Initialize the controller and a mock scope
  beforeEach(inject(function ($controller, $rootScope) {
    scope = $rootScope.$new();
    ProjektinperustiedotCtrl = $controller('ProjektinperustiedotCtrl', {
      $scope: scope
    });
  }));

  it('should work', function () {
    //TODO
  });
});
