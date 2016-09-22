ALTER TABLE dokumentti ADD COLUMN generator_version varchar(255) NOT NULL DEFAULT 'UUSI';
update dokumentti set generator_version = 'VANHA';
