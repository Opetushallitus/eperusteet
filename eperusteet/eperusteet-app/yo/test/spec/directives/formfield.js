'use strict';
/* global _ */

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

  it('should assign unique labels', function () {
    scope.data = {text: null};
    var template = _.template('<formfield type="<%= type %>" label="thelabel" model="data" model-var="text"></formfield>');
    var field, input, label;
    _.each(_.range(5), function (item, index) {
      field = template({type: index > 2 ? 'number' : 'text'});
      kaanna(field);
      input = element.find('input');
      label = element.find('label');
      expect(input.length).toBe(1);
      expect(label.length).toBe(1);
      expect(input.attr('id')).toBe('thelabel-' + index);
      expect(label.attr('for')).toBe('thelabel-' + index);
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

  describe('number type input', function () {
    it('should render itself as type="number"', function () {
      scope.data = {number: 0};
      kaanna('<formfield type="number" label="numero" model="data" model-var="number"></formfield>');
      var input = element.find('input');
      expect(input.length).toBe(1);
      expect(input.attr('type')).toBe('number');
    });

    it('should validate itself on a form', function () {
      scope.data = {number: 0};
      kaanna('<form name="myform" role="form">' +
             '<formfield type="number" label="numero" model="data" min="2" max="10" model-var="number" form="form" name="numberfield"></formfield>' +
             '</form>');
      var form = scope.myform;
      // TODO: pitäisi olla form.numberfield...?
      var field = form.tmpName;
      field.$setViewValue('asdf');
      expect(field.$valid).toBe(false);
      expect(field.$error.number).toBe(true);
      field.$setViewValue('8');
      expect(field.$valid).toBe(true);
      field.$setViewValue('1');
      expect(field.$error.min).toBe(true);
      field.$setViewValue('100');
      expect(field.$error.max).toBe(true);
    });
  });
});
