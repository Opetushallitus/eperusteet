'use strict';

describe('Directive: ckeditor', function () {

  // load the directive's module
  beforeEach(module('eperusteApp'));

  var element,
    scope;

  beforeEach(inject(function ($rootScope) {
    scope = $rootScope.$new();
    scope.model = 'Editable';
  }));

  it('should make hidden element visible', inject(function ($compile) {
    element = angular.element('<div ckeditori ng-model="model">Editable</div>');
    element = $compile(element)(scope);
    expect(element.text()).toBe('Editable');
  }));
});
