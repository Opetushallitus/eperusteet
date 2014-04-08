'use strict';

describe('Controller: AloitussivuCtrl', function () {

  // load the controller's module
  beforeEach(module('eperusteApp'));

  var AloitussivuCtrl,
    scope;

  // Initialize the controller and a mock scope
  beforeEach(inject(function ($controller, $rootScope) {
    scope = $rootScope.$new();
    AloitussivuCtrl = $controller('AloitussivuCtrl', {
      $scope: scope
    });
  }));

  it('should work', function () {
    // TODO
  });
});
