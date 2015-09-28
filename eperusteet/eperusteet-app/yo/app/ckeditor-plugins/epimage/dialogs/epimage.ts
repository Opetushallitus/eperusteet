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

CKEDITOR.dialog.add('epimageDialog', function( editor ) {
  var kaanna = editor.config.customData.kaanna;
  var controllerScope = null;
  return {
    title: kaanna('epimage-plugin-title'),
    minWidth: 400,
    minHeight: 200,
    contents: [
      {
        id: 'tab-basic',
        label: kaanna('epimage-plugin-label'),
        elements: [
          {
            type: 'html',
            id: 'epimage-html',
            validate: function() {
              return !this.getValue() ? kaanna('epimage-plugin-virhe-viite-tyhja') : true;
            },
            html: '<div ng-controller="EpImagePluginController" class="ckeplugin-ui-select">' +
            '<label class="ckeditor-plugin-label">{{\'epimage-plugin-label-epimage\'|kaanna}}</label>' +
            '<ui-select ng-model="model.chosen" ng-if="images.length > 0">' +
            '  <ui-select-match placeholder="{{\'epimage-plugin-select-placeholder\'|kaanna}}">{{$select.selected.nimi|kaanna}}</ui-select-match>' +
            '  <ui-select-choices repeat="epimage in filtered track by $index" refresh="filterImages($select.search)" refresh-delay="0">' +
            '  <span ng-bind-html="epimage.nimi|kaanna|highlight:$select.search"></span></ui-select-choices>' +
            '</ui-select>' +
            '<p class="empty-epimaget" ng-if="images.length === 0" kaanna="\'ei-kuvia\'"></p>' +
            '<div class="epimage-plugin-add">' +
            '  <label class="ckeditor-plugin-label">{{\'epimage-plugin-lisaa-uusi\'|kaanna}}</label>'+
            '  <div><button class="btn btn-default" ng-model-rejected="model.rejected" ngf-accept="\'.jpg,.jpeg,.png\'" ngf-select ng-model="model.files"><span kaanna="\'epimage-plugin-valitse\'"></span></button>' +
            '    <button ng-disabled="!model.files || model.files.length !== 1" class="btn btn-primary" ng-click="saveNew()" kaanna="lisaa"></button>' +
            '    <img ng-show="showPreview" ngf-thumbnail="model.files[0]" class="epimage-thumb">' +
            '  </div>' +
            '  <p class="success-message" ng-show="message">{{message|kaanna}}</p>' +
            '  <p class="error-message" ng-show="model.rejected.length > 0">{{\'epimage-plugin-hylatty\'|kaanna}}</p>' +
            '</div></div>',
            onLoad: function () {
              var self = this;
              var el = this.getElement().$;
              angular.element('body').injector().invoke(function($compile) {
                var scope = angular.element(el).scope();
                $compile(el)(scope);
                controllerScope = angular.element(el).scope();
                controllerScope.init();
                controllerScope.registerListener(function onChange(value) {
                  if (value && value.id) {
                    self.setValue(value.id);
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
              var value = element.getAttribute('data-uid');
              controllerScope.setValue(value);
              this.setValue(value);
            },
            commit: function (element) {
              element.setAttribute('data-uid', this.getValue());
              element.setAttribute('src', controllerScope.urlForImage({id: this.getValue()}));
            }
          }
        ]
      },
    ],
    onShow: function () {
      var selection = editor.getSelection();
      var element = selection.getStartElement();
      if (element) {
        element = element.getAscendant('img', true);
      }
      if (!element || element.getName() !== 'img') {
        element = editor.document.createElement('img');
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
