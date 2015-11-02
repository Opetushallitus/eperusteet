-- perusteisiin esikatseltava-tieto
ALTER TABLE peruste ADD COLUMN esikatseltavissa boolean DEFAULT false;
ALTER TABLE peruste_aud ADD COLUMN esikatseltavissa boolean;
