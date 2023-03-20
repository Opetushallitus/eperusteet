UPDATE julkaistu_peruste SET julkinen = TRUE
                         WHERE id in (SELECT DISTINCT ON (peruste_id) id
                                                      FROM julkaistu_peruste
                                                      ORDER BY peruste_id, revision DESC);
