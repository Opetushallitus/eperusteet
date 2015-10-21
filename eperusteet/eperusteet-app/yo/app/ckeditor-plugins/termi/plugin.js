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

CKEDITOR.plugins.add('termi', {
  icons: 'termi',
  init: function( editor ) {
    var kaanna = editor.config.customData.kaanna;
    editor.addCommand('termiEdit', new CKEDITOR.dialogCommand('termiDialog'));

    editor.addCommand('termiDelete', {
      exec: function (editor) {
        var selection = editor.getSelection();
        if (selection) {
          var element = selection.getStartElement();
          if (element) {
            element = element.getAscendant('abbr', true);
          }
          if (element && element.is('abbr')) {
            element.remove(1);
          }
        }
      }
    });

    editor.ui.addButton('Termi', {
      label: kaanna('termi-plugin-button-label'),
      command: 'termiEdit',
      toolbar: 'insert'
    });

    if (editor.contextMenu) {
      editor.addMenuGroup('termiGroup');
      editor.addMenuItem('termiEditItem', {
        label: kaanna('termi-plugin-menu-muokkaa'),
        icon: this.path + 'icons/termi.png',
        command: 'termiEdit',
        group: 'termiGroup'
      });
      editor.addMenuItem('termiDeleteItem', {
        label: kaanna('termi-plugin-menu-poista'),
        icon: this.path + 'icons/termi.png',
        command: 'termiDelete',
        group: 'termiGroup'
      });
      editor.contextMenu.addListener(function(element) {
        if (element.getAscendant('abbr', true)) {
          return {termiEditItem: CKEDITOR.TRISTATE_OFF, termiDeleteItem: CKEDITOR.TRISTATE_OFF};
        }
      });
    }

    CKEDITOR.dialog.add('termiDialog', this.path + 'dialogs/termi.js');
  }
});
