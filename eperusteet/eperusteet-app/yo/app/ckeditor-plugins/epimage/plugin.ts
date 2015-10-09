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

CKEDITOR.plugins.add( 'epimage', {
	requires: 'dialog',
	icons: 'epimage',
	init: function( editor ) {
    var kaanna = editor.config.customData.kaanna;
    editor.addCommand('epimageEdit', new CKEDITOR.dialogCommand('epimageDialog'));

    editor.addCommand('epimageDelete', {
      exec: function (editor) {
        var selection = editor.getSelection();
        if (selection) {
          var element = selection.getStartElement();
          if (element) {
            element = element.getAscendant('img', true);
          }
          if (element && element.is('img')) {
            element.remove(1);
          }
        }
      }
    });

    editor.ui.addButton('epimage', {
      label: kaanna('epimage-plugin-button-label'),
      command: 'epimageEdit',
      toolbar: 'insert'
    });

    if (editor.contextMenu) {
      editor.addMenuGroup('epimageGroup');
      editor.addMenuItem('epimageEditItem', {
        label: kaanna('epimage-plugin-menu-muokkaa'),
        icon: this.path + 'icons/epimage.png',
        command: 'epimageEdit',
        group: 'epimageGroup'
      });
      editor.addMenuItem('epimageDeleteItem', {
        label: kaanna('epimage-plugin-menu-poista'),
        icon: this.path + 'icons/epimage.png',
        command: 'epimageDelete',
        group: 'epimageGroup'
      });
      editor.contextMenu.addListener(function(element) {
        if (element.getAscendant('img', true)) {
          return {epimageEditItem: CKEDITOR.TRISTATE_OFF, epimageDeleteItem: CKEDITOR.TRISTATE_OFF};
        }
      });
    }

    CKEDITOR.dialog.add('epimageDialog', this.path + 'dialogs/epimage.js');
  }
});
