ALTER TABLE suosikki DROP CONSTRAINT uk_suosikki_sisalto;
ALTER TABLE suosikki ADD CONSTRAINT uk_suosikki_sisalto_kayttaja UNIQUE(kayttajaprofiili_id, sisalto);