'use strict';

describe('Directive: formfield', function () {

  // load the directive's module
  beforeEach(module('eperusteApp',
                    'views/partials/formfield.html',
                    'views/partials/numberinput.html',
                    'views/multiinput.html'));

  var element, scope, $compile;

  beforeEach(inject(function ($rootScope, _$compile_, $httpBackend) {
    $httpBackend.when('GET', /localisation.+/).respond({});
    $httpBackend.when('GET', /eperusteet-service\/api.+/).respond({});
    scope = $rootScope.$new();
    $compile = _$compile_;
  }));

  function kaanna(el) {
    element = angular.element(el);
    element = $compile(element)(scope);
    scope.$digest();
  }

  describe('basic text field', function () {
    it('should set a label and bind it to the input', function () {
      scope.data = {
        text: null
      };
      kaanna('<formfield label="My label" model="data" model-var="text"></formfield>');
      var input = element.find('input');
      var label = element.find('label');
      expect(input.length).toBe(1);
      expect(label.length).toBe(1);
      expect(input.attr('id')).toBe('My-label-0');
      expect(label.attr('for')).toBe('My-label-0');
    });
  });

  describe('deep object text field', function () {
    it('should update the model properly on text input', function () {
      scope.data = {
        deeper: {
          anddeeper: {
            text: null
          }
        }
      };
      kaanna('<formfield label="teksti" model="data" model-var="deeper.anddeeper.text"></formfield>');
      var input = element.find('input');
      input.scope().input.model = 'value';
      scope.$digest();
      expect(scope.data.deeper.anddeeper.text).toBe('value');
    });
  });
});
