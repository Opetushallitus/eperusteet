ALTER TABLE skeduloitu_ajo ADD COLUMN status varchar(255) not null DEFAULT 'pysaytetty';
ALTER TABLE skeduloitu_ajo ADD COLUMN viimeisin_ajo_kaynnistys timestamp;
ALTER TABLE skeduloitu_ajo ADD COLUMN viimeisin_ajo_lopetus timestamp;
ALTER TABLE skeduloitu_ajo DROP COLUMN viimeisinajo;
