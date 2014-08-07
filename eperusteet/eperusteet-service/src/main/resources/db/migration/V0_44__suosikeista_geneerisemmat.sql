DELETE FROM suosikki *;

ALTER TABLE suosikki DROP COLUMN parametrit;
ALTER TABLE suosikki RENAME COLUMN tila TO sisalto;

ALTER TABLE suosikki ADD CONSTRAINT uk_suosikki_sisalto UNIQUE(sisalto);
