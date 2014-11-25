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
/* global CKEDITOR, _ */

CKEDITOR.dialog.add('termiDialog', function( editor ) {
  var kaanna = editor.config.customData.kaanna;
  var service = editor.config.customData.termistoService;
  var PLACEHOLDER = [kaanna('termi-plugin-select-placeholder'), ''];
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
            type: 'select',
            id: 'termi-viite',
            label: kaanna('termi-plugin-label-termi'),
            items: [PLACEHOLDER],
            setup: function(element) {
              this.setValue(element.getAttribute('data-viite'));
            },
            commit: function(element) {
              element.setAttribute('data-viite', this.getValue());
            },
            validate: CKEDITOR.dialog.validate.notEmpty(kaanna('termi-plugin-virhe-viite-tyhja')),
            onShow: function () {
              this.clear();
              var self = this;
              var dialog = this.getDialog();
              service.getAll().then(function (res) {
                var items = _.sortBy(res, function (item) {
                  return kaanna(item.termi).toLowerCase();
                });
                self.clear();
                self.add(PLACEHOLDER[0], PLACEHOLDER[1]);
                _.each(items, function (item) {
                  self.add(kaanna(item.termi), item.avain);
                });
                if (!dialog.insertMode) {
                  dialog.setupContent(dialog.element);
                }
              });
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
