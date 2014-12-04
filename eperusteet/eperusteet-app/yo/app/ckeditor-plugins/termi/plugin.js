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
    editor.addCommand('termi', new CKEDITOR.dialogCommand('termiDialog'));
    editor.ui.addButton('Termi', {
      label: kaanna('termi-plugin-button-label'),
      command: 'termi',
      toolbar: 'insert'
    });

    if (editor.contextMenu) {
      editor.addMenuGroup('termiGroup');
      editor.addMenuItem('termiItem', {
        label: kaanna('termi-plugin-menu-muokkaa'),
        icon: this.path + 'icons/termi.png',
        command: 'termi',
        group: 'termiGroup'
      });
      editor.contextMenu.addListener(function(element) {
        if (element.getAscendant('abbr', true)) {
          return {termiItem: CKEDITOR.TRISTATE_OFF};
        }
      });
    }

    CKEDITOR.dialog.add('termiDialog', this.path + 'dialogs/termi.js');
  }
});
