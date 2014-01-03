'use strict';

describe('Controller: HakuCtrl', function () {

  // load the controller's module
  beforeEach(module('eperusteApp'));

  var SearchCtrl,
    scope;

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
