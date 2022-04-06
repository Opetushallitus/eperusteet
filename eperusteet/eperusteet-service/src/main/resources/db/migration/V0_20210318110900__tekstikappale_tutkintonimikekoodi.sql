ALTER TABLE tekstikappale ADD COLUMN tutkintonimike_id int8;

ALTER TABLE tekstikappale_aud ADD COLUMN tutkintonimike_id int8;

alter table tekstikappale
    add constraint FK_b1lac9omqy1qhavau1xsan7b3
    foreign key (tutkintonimike_id)
    references koodi;
