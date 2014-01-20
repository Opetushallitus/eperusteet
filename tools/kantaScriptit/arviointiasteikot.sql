CREATE TABLE id_table (id bigint);

// -------------------------------------------------------------- //

insert into arviointiasteikko values (1);

insert into id_table values (nextval('hibernate_sequence'));
insert into tekstipalanen(id) select id from id_table;
insert into tekstipalanen_teksti(tekstipalanen_id, kieli, teksti) select id, 'FI' as kieli, 'Suoritettu S' as teksti from id_table;
insert into tekstipalanen_teksti(tekstipalanen_id, kieli, teksti) select id, 'SV' as kieli, '[Suoritettu S]' as teksti from id_table;

insert into osaamistaso(otsikko_id, id) select id, 1 as id from id_table;
insert into arviointiasteikko_osaamistaso(arviointiasteikko_id, osaamistasot_id, osaamistasot_order) values (1, 1, 0);

delete from id_table;

// -------------------------------------------------------------- //

insert into arviointiasteikko values (2);

insert into id_table values (nextval('hibernate_sequence'));
insert into tekstipalanen(id) select id from id_table;
insert into tekstipalanen_teksti(tekstipalanen_id, kieli, teksti) select id, 'FI' as kieli, 'Tyydyttävä T1' as teksti from id_table;
insert into tekstipalanen_teksti(tekstipalanen_id, kieli, teksti) select id, 'SV' as kieli, '[Tyydyttävä T1]' as teksti from id_table;

insert into osaamistaso(otsikko_id, id) select id, 2 as id from id_table;
insert into arviointiasteikko_osaamistaso(arviointiasteikko_id, osaamistasot_id, osaamistasot_order) values (2, 2, 0);

delete from id_table;

insert into id_table values (nextval('hibernate_sequence'));
insert into tekstipalanen(id) select id from id_table;
insert into tekstipalanen_teksti(tekstipalanen_id, kieli, teksti) select id, 'FI' as kieli, 'Hyvä H2' as teksti from id_table;
insert into tekstipalanen_teksti(tekstipalanen_id, kieli, teksti) select id, 'SV' as kieli, '[Hyvä H2]' as teksti from id_table;

insert into osaamistaso(otsikko_id, id) select id, 3 as id from id_table;
insert into arviointiasteikko_osaamistaso(arviointiasteikko_id, osaamistasot_id, osaamistasot_order) values (2, 3, 1);

delete from id_table;

insert into id_table values (nextval('hibernate_sequence'));
insert into tekstipalanen(id) select id from id_table;
insert into tekstipalanen_teksti(tekstipalanen_id, kieli, teksti) select id, 'FI' as kieli, 'Kiitettävä K3' as teksti from id_table;
insert into tekstipalanen_teksti(tekstipalanen_id, kieli, teksti) select id, 'SV' as kieli, '[Kiitettävä K3]' as teksti from id_table;

insert into osaamistaso(otsikko_id, id) select id, 4 as id from id_table;
insert into arviointiasteikko_osaamistaso(arviointiasteikko_id, osaamistasot_id, osaamistasot_order) values (2, 4, 2);

delete from id_table;

// -------------------------------------------------------------- //

insert into arviointiasteikko values (3);

insert into id_table values (nextval('hibernate_sequence'));
insert into tekstipalanen(id) select id from id_table;
insert into tekstipalanen_teksti(tekstipalanen_id, kieli, teksti) select id, 'FI' as kieli, 'Tyydyttävä T1' as teksti from id_table;
insert into tekstipalanen_teksti(tekstipalanen_id, kieli, teksti) select id, 'SV' as kieli, '[Tyydyttävä T1]' as teksti from id_table;

insert into osaamistaso(otsikko_id, id) select id, 5 as id from id_table;
insert into arviointiasteikko_osaamistaso(arviointiasteikko_id, osaamistasot_id, osaamistasot_order) values (3, 5, 0);

delete from id_table;

insert into id_table values (nextval('hibernate_sequence'));
insert into tekstipalanen(id) select id from id_table;
insert into tekstipalanen_teksti(tekstipalanen_id, kieli, teksti) select id, 'FI' as kieli, 'Tyydyttävä T2' as teksti from id_table;
insert into tekstipalanen_teksti(tekstipalanen_id, kieli, teksti) select id, 'SV' as kieli, '[Tyydyttävä T2]' as teksti from id_table;

insert into osaamistaso(otsikko_id, id) select id, 6 as id from id_table;
insert into arviointiasteikko_osaamistaso(arviointiasteikko_id, osaamistasot_id, osaamistasot_order) values (3, 6, 1);

delete from id_table;

insert into id_table values (nextval('hibernate_sequence'));
insert into tekstipalanen(id) select id from id_table;
insert into tekstipalanen_teksti(tekstipalanen_id, kieli, teksti) select id, 'FI' as kieli, 'Hyvä H3' as teksti from id_table;
insert into tekstipalanen_teksti(tekstipalanen_id, kieli, teksti) select id, 'SV' as kieli, '[Hyvä H3]' as teksti from id_table;

insert into osaamistaso(otsikko_id, id) select id, 7 as id from id_table;
insert into arviointiasteikko_osaamistaso(arviointiasteikko_id, osaamistasot_id, osaamistasot_order) values (3, 7, 2);

delete from id_table;

insert into id_table values (nextval('hibernate_sequence'));
insert into tekstipalanen(id) select id from id_table;
insert into tekstipalanen_teksti(tekstipalanen_id, kieli, teksti) select id, 'FI' as kieli, 'Hyvä H4' as teksti from id_table;
insert into tekstipalanen_teksti(tekstipalanen_id, kieli, teksti) select id, 'SV' as kieli, '[Hyvä H4]' as teksti from id_table;

insert into osaamistaso(otsikko_id, id) select id, 8 as id from id_table;
insert into arviointiasteikko_osaamistaso(arviointiasteikko_id, osaamistasot_id, osaamistasot_order) values (3, 8, 3);

delete from id_table;

insert into id_table values (nextval('hibernate_sequence'));
insert into tekstipalanen(id) select id from id_table;
insert into tekstipalanen_teksti(tekstipalanen_id, kieli, teksti) select id, 'FI' as kieli, 'Kiitettävä K5' as teksti from id_table;
insert into tekstipalanen_teksti(tekstipalanen_id, kieli, teksti) select id, 'SV' as kieli, '[Kiitettävä K5]' as teksti from id_table;

insert into osaamistaso(otsikko_id, id) select id, 9 as id from id_table;
insert into arviointiasteikko_osaamistaso(arviointiasteikko_id, osaamistasot_id, osaamistasot_order) values (3, 9, 4);

delete from id_table;

drop table id_table;