'use strict';

describe('Controller: EsitysCtrl', function() {

  // load the controller's module
  beforeEach(module('eperusteApp'));

  var EsitysCtrl,
    scope;

  // Initialize the controller and a mock scope
  beforeEach(inject(function($controller, $rootScope) {
    scope = $rootScope.$new();
    EsitysCtrl = $controller('EsitysCtrl', {
      $scope: scope
    });
  }));

  /*it('should attach a list of awesomeThings to the scope', function () {
   expect(scope.awesomeThings.length).toBe(3);
   });*/
});
