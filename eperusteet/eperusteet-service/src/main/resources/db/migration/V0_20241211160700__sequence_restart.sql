SELECT setval('hibernate_sequence', (with pk_list as (select tbl_ns.nspname as table_schema,
                                                             tbl.relname    as table_name,
                                                             cons.conname   as pk_name,
                                                             col.attname    as pk_column
                                                      from pg_class tbl
                                                               join pg_constraint cons on tbl.oid = cons.conrelid and cons.contype = 'p'
                                                               join pg_namespace tbl_ns on tbl_ns.oid = tbl.relnamespace
                                                               join pg_attribute col
                                                                    on col.attrelid = tbl.oid and col.attnum = cons.conkey[1]
                                                               join pg_type typ on typ.oid = col.atttypid
                                                      where col.attname = 'id'
                                                        and typ.typname not in ('uuid')),
                                          maxvals as (select table_schema,
                                                             table_name,
                                                             pk_column,
                                                             (xpath('/row/max/text()',
                                                                    query_to_xml(format('select max(%I) from %I.%I',
                                                                                        pk_column, table_schema,
                                                                                        table_name), true, true, ''))
                                                                 )[1]::text as max_val
                                                      from pk_list)
                                     select max(max_val::integer)
                                     from maxvals
                                     where pk_column = 'id') + 100)