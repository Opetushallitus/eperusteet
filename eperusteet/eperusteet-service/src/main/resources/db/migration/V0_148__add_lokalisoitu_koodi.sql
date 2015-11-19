
ALTER TABLE yl_lukiokurssi ADD COLUMN lokalisoitava_koodi_id INT8
  REFERENCES tekstipalanen(id);
ALTER TABLE yl_lukiokurssi_aud ADD COLUMN lokalisoitava_koodi_id INT8
  REFERENCES tekstipalanen(id);

UPDATE yl_lukiokurssi SET lokalisoitava_koodi_id = newTeksti(
    (SELECT k.koodi_arvo FROM yl_kurssi k WHERE k.id = yl_lukiokurssi.id))
WHERE (SELECT k.koodi_arvo FROM yl_kurssi k WHERE k.id = yl_lukiokurssi.id) IS NOT NULL;

