'use strict';

describe('Controller: MainCtrl', function () {

  // load the controller's module
  beforeEach(module('eperusteApp'));

  var MainCtrl,
    scope;

  // Initialize the controller and a mock scope
  beforeEach(inject(function ($controller, $rootScope) {
    scope = $rootScope.$new();
    MainCtrl = $controller('MuokkausCtrl', {
      $scope: scope
    });
  }));

  it('should work', function () {
  	//TODO
  });
});
