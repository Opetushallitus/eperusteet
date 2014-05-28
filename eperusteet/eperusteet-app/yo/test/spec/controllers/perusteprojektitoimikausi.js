'use strict';

describe('Controller: PerusteprojektiToimikausiCtrl', function () {

  // load the controller's module
  beforeEach(module('eperusteApp'));

  var PerusteprojektiToimikausiCtrl,
    scope;

  // Initialize the controller and a mock scope
  beforeEach(inject(function ($controller, $rootScope) {
    scope = $rootScope.$new();
    scope.projekti = {};
    scope.projekti.toimikausiAlku = {};
    scope.projekti.toimikausiLoppu = {};
    PerusteprojektiToimikausiCtrl = $controller('PerusteprojektiToimikausiCtrl', {
      $scope: scope
    });
  }));

  it('should work', function () {
    //TODO
  });
});
