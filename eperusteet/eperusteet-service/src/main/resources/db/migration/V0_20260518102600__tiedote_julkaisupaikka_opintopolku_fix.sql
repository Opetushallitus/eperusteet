insert into tiedote_julkaisupaikka
SELECT id , 'OPINTOPOLKU'
FROM tiedote
where exists (select 1 from tiedote_koulutustyyppi where tiedote_id = tiedote.id)