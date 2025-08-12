ALTER TABLE kall_logg ADD ident VARCHAR(11);

CREATE INDEX kalo_4 ON kall_logg (ident);

COMMENT ON COLUMN kall_logg.ident IS 'FND, DNR eller noe annet som identifiserer brukere';
