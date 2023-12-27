alter table kevyttekstikappale_aud drop constraint kevyttekstikappale_aud_pkey;
alter table kevyttekstikappale_aud add primary key(id, rev);