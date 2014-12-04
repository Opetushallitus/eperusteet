/*
 * Copyright (c) 2013 The Finnish Board of Education - Opetushallitus
 *
 * This program is free software: Licensed under the EUPL, Version 1.1 or - as
 * soon as they will be approved by the European Commission - subsequent versions
 * of the EUPL (the "Licence");
 *
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at: http://ec.europa.eu/idabc/eupl
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * European Union Public Licence for more details.
 */

'use strict';
/* global CKEDITOR */

CKEDITOR.dialog.add('termiDialog', function( editor ) {
  var kaanna = editor.config.customData.kaanna;
  var controllerScope = null;
  return {
    title: kaanna('termi-plugin-title'),
    minWidth: 400,
    minHeight: 200,
    contents: [
      {
        id: 'tab-basic',
        label: kaanna('termi-plugin-label'),
        elements: [
          {
            type: 'text',
            id: 'termi-text',
            label: kaanna('termi-plugin-label-teksti'),
            validate: CKEDITOR.dialog.validate.notEmpty(kaanna('termi-plugin-virhe-teksti-tyhja')),
            setup: function(element) {
              this.setValue(element.getText());
            },
            commit: function(element) {
              element.setText(this.getValue());
            }
          },
          {
            type: 'html',
            id: 'termi-html',
            validate: function() {
              return !this.getValue() ? kaanna('termi-plugin-virhe-viite-tyhja') : true;
            },
            html: '<div ng-controller="TermiPluginController" class="ckeplugin-ui-select">' +
            '<label>{{\'termi-plugin-label-termi\'|kaanna}}</label>' +
            '<ui-select ng-model="model.chosen">' +
            '<ui-select-match placeholder="{{\'termi-plugin-select-placeholder\'|kaanna}}">{{$select.selected.termi|kaanna}}</ui-select-match>' +
            '<ui-select-choices repeat="termi in filtered track by $index" refresh="filterTermit($select.search)" refresh-delay="0">' +
            '<span ng-bind-html="termi.termi|kaanna|highlight:$select.search"></span></ui-select-choices>' +
            '</ui-select>' +
            '</div>',
            onLoad: function () {
              var self = this;
              var el = this.getElement().$;
              angular.element('body').injector().invoke(function($compile) {
                var scope = angular.element(el).scope();
                $compile(el)(scope);
                controllerScope = angular.element(el).scope();
                controllerScope.init();
                controllerScope.registerListener(function onChange(value) {
                  if (value && value.avain) {
                    self.setValue(value.avain);
                  }
                });
              });
            },
            onShow: function () {
              var dialog = this.getDialog();
              if (!dialog.insertMode) {
                dialog.setupContent(dialog.element);
              }
            },
            setup: function (element) {
              var value = element.getAttribute('data-viite');
              controllerScope.setValue(value);
              this.setValue(value);
            },
            commit: function (element) {
              element.setAttribute('data-viite', this.getValue());
            }
          }
        ]
      },
    ],
    onShow: function () {
      var selection = editor.getSelection();
      var element = selection.getStartElement();
      if (element) {
        element = element.getAscendant('abbr', true);
      }
      if (!element || element.getName() !== 'abbr') {
        element = editor.document.createElement('abbr');
        element.appendText(selection.getSelectedText());
        this.insertMode = true;
      } else {
        this.insertMode = false;
      }
      this.setupContent(element);
      this.element = element;
    },
    onOk: function() {
      var el = this.element;
      this.commitContent(el);
      if (this.insertMode) {
        editor.insertElement(el);
      }
    }
  };
});
