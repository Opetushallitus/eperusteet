INSERT INTO tiedote_julkaisupaikka
SELECT id, 'OPINTOPOLKU_ETUSIVU'
FROM tiedote t
WHERE julkinen = TRUE AND yleinen = true
AND NOT EXISTS (SELECT id FROM tiedote_julkaisupaikka j where j.tiedote_id = t.id AND j.julkaisupaikka = 'OPINTOPOLKU_ETUSIVU')
