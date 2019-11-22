alter table suoritustapa
    add constraint FK_ecrtbrmrt6v62ht10aix1hr7b
    foreign key (tutkinnon_rakenne_id)
    references tutkinnon_rakenne;
    