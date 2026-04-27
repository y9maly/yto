CREATE DATABASE db;
\c db;

SET statement_timeout = 0;
SET lock_timeout = 0;
SET idle_in_transaction_session_timeout = 0;
SET transaction_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SELECT pg_catalog.set_config('search_path', '', false);
SET check_function_bodies = false;
SET xmloption = content;
SET client_min_messages = warning;
SET row_security = off;

CREATE TYPE public.__mortal_pattern__foreign_key AS (
                                                        target_table regclass,
                                                        target_column text,
                                                        constraint_name text
                                                    );

CREATE TYPE public.__mortal_pattern__mortal_reference AS (
                                                             alive_reference text,
                                                             grave_reference text,
                                                             typename text,
                                                             alive_table regclass,
                                                             alive_table_pk text,
                                                             grave_table regclass,
                                                             grave_table_pk text
                                                         );

CREATE TYPE public.image_format AS ENUM (
    'png',
    'jpeg',
    'webp',
    'other'
    );

CREATE TYPE public.post_content_type AS ENUM (
    'standalone',
    'repost'
    );

CREATE DOMAIN public.post_id AS bigint;



CREATE DOMAIN public.revision AS bigint NOT NULL DEFAULT 1;



CREATE TYPE public.t_post_reference AS (
                                           id bigint,
                                           alive boolean
                                       );



CREATE DOMAIN public.user_id AS bigint;



CREATE TYPE public.vector_image_format AS ENUM (
    'svg',
    'other'
    );



CREATE TYPE public.video_format AS ENUM (
    'mp4',
    'webm',
    'mov',
    'mkv',
    'avi'
    );



CREATE CAST (text AS public.post_content_type) WITH INOUT;



CREATE FUNCTION public.__mortal_pattern__get_column_foreign_keys(p_table regclass, p_column text) RETURNS public.__mortal_pattern__foreign_key[]
    LANGUAGE plpgsql
AS $$ declare
    result __mortal_pattern__foreign_key[];
begin
        execute format('SELECT ''%s''::regclass;', p_table);
    execute format(
            'SELECT has_column_privilege(''%s'', ''%s'', ''SELECT'');',
            p_table,
            p_column
            );

    select array_agg((
                      c.confrelid::regclass,
                      af.attname::text,
                      c.conname::text
        )::__mortal_pattern__foreign_key)
    into result
    from pg_constraint c
             join pg_attribute a
                  on a.attrelid = c.conrelid
                      and a.attnum = any (c.conkey)
             join pg_attribute af
                  on af.attrelid = c.confrelid
                      and af.attnum = any (c.confkey)
    where c.contype = 'f'
      and c.conrelid = p_table
      and a.attname = p_column;

    return coalesce(result, '{}');
end; $$;



CREATE FUNCTION public.__mortal_pattern__get_single_foreign_key(p_table regclass, p_column text) RETURNS public.__mortal_pattern__foreign_key
    LANGUAGE plpgsql
AS $$ declare
    foreign_keys __mortal_pattern__foreign_key[] := __mortal_pattern__get_column_foreign_keys(p_table, p_column);
begin
    if array_length(foreign_keys, 1) != 1 then
        raise exception 'exactly one foreign constraint expected for table % for column %, but got %', p_table, p_column, array_length(foreign_keys, 1);
    end if;
    return foreign_keys[1];
end; $$;



CREATE FUNCTION public.__mortal_pattern__get_table_from_references_column(p_table regclass, p_column text) RETURNS regclass
    LANGUAGE plpgsql
AS $$ declare
    foreign_key __mortal_pattern__foreign_key := __mortal_pattern__get_single_foreign_key(p_table, p_column);
begin
    return foreign_key.target_table;
end; $$;



CREATE FUNCTION public.__mortal_pattern__get_table_mortal_reference_by_alive_reference(_table regclass, alive_reference text) RETURNS public.__mortal_pattern__mortal_reference
    LANGUAGE plpgsql
AS $$ declare
    all_references __mortal_pattern__mortal_reference[] := __mortal_pattern__get_table_mortal_references(_table);
    reference __mortal_pattern__mortal_reference;
begin
    foreach reference in array all_references loop
            if reference.alive_reference = alive_reference then
                return reference;
            end if;
        end loop;

    raise exception 'table % doesn''t have column %', _table, alive_reference;
end; $$;



CREATE FUNCTION public.__mortal_pattern__get_table_mortal_references(_table regclass) RETURNS public.__mortal_pattern__mortal_reference[]
    LANGUAGE plpgsql
AS $$ declare
    result __mortal_pattern__mortal_reference[];
begin
    select array_agg((
                      a.attname::text,
                      g.attname::text,
                      format_type(a.atttypid, a.atttypmod),

                      fk_alive.confrelid,
                      pk_alive.attname::text,

                      fk_grave.confrelid,
                      pk_grave.attname::text
        )::__mortal_pattern__mortal_reference)
    into result
    from pg_attribute a

             join pg_attribute g
                  on g.attrelid = a.attrelid
                      and g.attname = a.attname || '_grave'
                      and g.attnum > 0
                      and not g.attisdropped

                     join pg_constraint fk_alive
                  on fk_alive.conrelid = a.attrelid
                      and fk_alive.contype = 'f'
                      and a.attnum = any (fk_alive.conkey)

                     join pg_constraint fk_grave
                  on fk_grave.conrelid = g.attrelid
                      and fk_grave.contype = 'f'
                      and g.attnum = any (fk_grave.conkey)

                     join pg_constraint pkc_alive
                  on pkc_alive.conrelid = fk_alive.confrelid
                      and pkc_alive.contype = 'p'

             join pg_attribute pk_alive
                  on pk_alive.attrelid = pkc_alive.conrelid
                      and pk_alive.attnum = any (pkc_alive.conkey)
                      and pk_alive.attnum > 0
                      and not pk_alive.attisdropped

                     join pg_constraint pkc_grave
                  on pkc_grave.conrelid = fk_grave.confrelid
                      and pkc_grave.contype = 'p'

             join pg_attribute pk_grave
                  on pk_grave.attrelid = pkc_grave.conrelid
                      and pk_grave.attnum = any (pkc_grave.conkey)
                      and pk_grave.attnum > 0
                      and not pk_grave.attisdropped

    where a.attrelid = _table
      and a.attnum > 0
      and not a.attisdropped
      and a.attname not like '%\_grave' escape '\';

    return coalesce(result, '{}');
end; $$;



CREATE FUNCTION public.increment_revision() RETURNS trigger
    LANGUAGE plpgsql
AS $$
begin
    NEW.revision := coalesce(OLD.revision, 0) + 1;
    RETURN NEW;
end; $$;



CREATE FUNCTION public.require(code text, value anyelement) RETURNS anyelement
    LANGUAGE plpgsql
AS $$ begin
    if value is null then
        raise exception '%', code;
    end if;
    return value;
end $$;



CREATE FUNCTION public.setup_bury_reference_trigger(referencestable regclass, alivereference text) RETURNS void
    LANGUAGE plpgsql
AS $_$ declare
    alive_table regclass := __mortal_pattern__get_table_from_references_column(referencesTable, aliveReference);
    mortal_reference __mortal_pattern__mortal_reference := __mortal_pattern__get_table_mortal_reference_by_alive_reference(referencesTable, aliveReference);
    referenced_column_PK_name text := mortal_reference.alive_table_pk;
begin
        execute format('SELECT ''%s''::regclass;', referencesTable);
    execute format('SELECT has_column_privilege(''%s'', ''%s'', ''SELECT'');', referencesTable, aliveReference);
    execute format('SELECT has_column_privilege(''%s'', ''%s'', ''SELECT'');', referencesTable, aliveReference || '_grave');
    execute format('SELECT ''%s''::regclass;', alive_table);
    execute format('SELECT has_column_privilege(''%s'', ''%s'', ''SELECT'');', alive_table, referenced_column_PK_name);

    execute format(
            'alter table %s drop constraint if exists %s_check;',
            referencesTable, aliveReference
            );

    execute format(
            'alter table %s' ||
            ' add constraint %s_check' ||
            ' check ( num_nonnulls(%s, %s_grave) <= 1 );',
            referencesTable,
            aliveReference,
            aliveReference, aliveReference
            );

    execute format(
            'create or replace function trigger__%s__%s_bury() returns trigger as $$ begin' || chr(10) ||
            'update %s' || chr(10) ||
            'set %s_grave = OLD.%s, %s = NULL' || chr(10) ||
            'where %s = OLD.%s;' || chr(10) ||
            'return OLD;' || chr(10) ||
            'end; $$ language plpgsql;',
            referencesTable, aliveReference,
            referencesTable,
            aliveReference, referenced_column_PK_name, aliveReference,
            aliveReference, referenced_column_PK_name
            );

    if referencesTable = alive_table then
        execute format(
                'create or replace trigger bury__%s__%s' || chr(10) ||
                'after delete on %s' || chr(10) ||
                'for each row execute function trigger__%s__%s_bury();',
                referencesTable, aliveReference,
                alive_table,
                referencesTable, aliveReference
                );
    else
        execute format(
                'create or replace trigger bury__%s__%s' || chr(10) ||
                'before delete on %s' || chr(10) ||
                'for each row execute function trigger__%s__%s_bury();',
                referencesTable, aliveReference,
                alive_table,
                referencesTable, aliveReference
                );
    end if;
end; $_$;



CREATE FUNCTION public.setup_bury_trigger(alive_table regclass, columns text) RETURNS void
    LANGUAGE plpgsql
AS $_$ declare
    grave_table regclass := 'grave_' || alive_table;
    columns_array text[] := regexp_split_to_array(columns, ',\s*');
    direct_copy_columns text[] := '{}';
    indirect_columns text[] := '{}';

    alive_table_all_mortal_references __mortal_pattern__mortal_reference[] := __mortal_pattern__get_table_mortal_references(alive_table);
    alive_table_all_alive_references text[] := array(select (unnest(alive_table_all_mortal_references)).alive_reference);
    grave_table_all_mortal_references __mortal_pattern__mortal_reference[] := __mortal_pattern__get_table_mortal_references(grave_table);
    mortal_references __mortal_pattern__mortal_reference[] := '{}';

        declarations text := '';
    declarations_initialization text := '';
    create_function text := '';

        mortal_reference __mortal_pattern__mortal_reference;
    current_column text;
begin
        execute format('SELECT ''%s''::regclass;', alive_table);
    execute format('SELECT ''%s''::regclass;', 'grave_' || alive_table);
        execute format(
            'SELECT has_column_privilege(''%s'', ''%s'', ''SELECT'');' || chr(10),
            alive_table,
            array_to_string(
                    regexp_split_to_array(columns, ',\s*'),
                    ''', ''SELECT'');' || chr(10) || 'SELECT has_column_privilege(''' || alive_table || ''', '''
            )
            );

        foreach mortal_reference in array grave_table_all_mortal_references loop
            if exists (select 1 from unnest(columns_array) where unnest = mortal_reference.alive_reference) then
                mortal_references := array_append(mortal_references, mortal_reference);
            end if;
        end loop;

        foreach mortal_reference in array mortal_references loop
            declarations := declarations
                                || 'v_' || mortal_reference.alive_reference || ' ' || mortal_reference.typename || ' := null;' || chr(10)
                                || 'v_' || mortal_reference.grave_reference || ' ' || mortal_reference.typename || ' := null;' || chr(10);
        end loop;

        foreach current_column in array columns_array loop
            if not exists (select 1 from unnest(mortal_references) where alive_reference = current_column) then
                direct_copy_columns := array_append(direct_copy_columns, current_column);
            else
                indirect_columns := array_append(indirect_columns, current_column);
            end if;
        end loop;

                                            foreach mortal_reference in array mortal_references loop
            if exists (select 1 from unnest(alive_table_all_alive_references) where unnest = mortal_reference.alive_reference) then
                declarations_initialization := declarations_initialization
                                                   || 'if OLD.' || mortal_reference.grave_reference || ' is not null then' || chr(10)
                                                   || '    v_' || mortal_reference.grave_reference || ' := OLD.' || mortal_reference.grave_reference || ';' || chr(10)
                                                   || 'elseif OLD.' || mortal_reference.alive_reference || ' is null then' || chr(10)
                                                   || '    -- do nothing' || chr(10)
                                                   || 'elseif exists (select 1 from ' || mortal_reference.alive_table || ' where ' || mortal_reference.alive_table_pk || ' = OLD.' || mortal_reference.alive_reference || ') then' || chr(10)
                                                   || '    v_' || mortal_reference.alive_reference || ' := OLD.' || mortal_reference.alive_reference || ';' || chr(10)
                                                   || 'else' || chr(10)
                                                   || '    v_' || mortal_reference.grave_reference || ' := OLD.' || mortal_reference.alive_reference || ';' || chr(10)
                                                   || 'end if;' || chr(10) || chr(10);
            else
                declarations_initialization := declarations_initialization
                                                   || 'if OLD.' || mortal_reference.alive_reference || ' is null then' || chr(10)
                                                   || '    -- do nothing' || chr(10)
                                                   || 'elseif exists (select 1 from ' || mortal_reference.alive_table || ' where ' || mortal_reference.alive_table_pk || ' = OLD.' || mortal_reference.alive_reference || ') then' || chr(10)
                                                   || '    v_' || mortal_reference.alive_reference || ' := OLD.' || mortal_reference.alive_reference || ';' || chr(10)
                                                   || 'else' || chr(10)
                                                   || '    v_' || mortal_reference.grave_reference || ' := OLD.' || mortal_reference.alive_reference || ';' || chr(10)
                                                   || 'end if;' || chr(10) || chr(10);
            end if;
        end loop;

    create_function := create_function
                           || 'create or replace function trigger__' || alive_table || '__bury() returns trigger as $$ declare' || chr(10)
                           || '    ' || replace(declarations, chr(10), chr(10) || '    ') || chr(10)
                           || 'begin' || chr(10)
                           || '' || chr(10)
                           || '    ' || replace(declarations_initialization, chr(10), chr(10) || '    ') || chr(10)
                           || '' || chr(10)
                           || '    insert into grave_' || alive_table || ' (deleted_at, ';
        create_function := create_function || array_to_string(direct_copy_columns, ', ');
        foreach current_column in array indirect_columns loop
            create_function := create_function || ', ' || current_column || ', ' || current_column || '_grave';
        end loop;
    create_function := create_function
                           || ')' || chr(10)
                           || '    values (current_timestamp, OLD.' || array_to_string(direct_copy_columns, ', OLD.');
        foreach current_column in array indirect_columns loop
            create_function := create_function || ', v_' || current_column || ', v_' || current_column || '_grave';
        end loop;
    create_function := create_function
                           || ');' || chr(10)
                           || '    return OLD;' || chr(10)
        || 'end; $$ language plpgsql;';


    execute create_function;

    execute format(
            'create or replace trigger bury before delete on %s ' || chr(10) ||
            'for each row execute function trigger__%s__bury();',
            alive_table, alive_table
            );
end; $_$;



CREATE FUNCTION public.setup_mortal_reference(_table regclass, reference text) RETURNS void
    LANGUAGE plpgsql
AS $$ declare
begin
    perform setup_bury_reference_trigger(_table, reference);
end; $$;



CREATE FUNCTION public.setup_mortal_references(_table regclass, columns text) RETURNS void
    LANGUAGE plpgsql
AS $$ declare
    columns_array text[] := regexp_split_to_array(columns, ',\s*');
    reference text;
begin
    foreach reference in array columns_array loop
            perform setup_bury_reference_trigger(_table, reference);
        end loop;
end; $$;



CREATE FUNCTION public.setup_mortal_table(alive_table regclass, columns text) RETURNS void
    LANGUAGE plpgsql
AS $$ declare
    grave_table regclass := 'grave_' || alive_table;
    alive_table_mortal_references __mortal_pattern__mortal_reference[] := __mortal_pattern__get_table_mortal_references(alive_table);
    grave_table_mortal_references __mortal_pattern__mortal_reference[] := __mortal_pattern__get_table_mortal_references(grave_table);
    mortal_reference __mortal_pattern__mortal_reference;
    foreign_keys __mortal_pattern__foreign_key[];
begin

    perform setup_bury_trigger(alive_table, columns);


    foreach mortal_reference in array alive_table_mortal_references loop
            if mortal_reference.alive_table = alive_table then
                foreign_keys := __mortal_pattern__get_column_foreign_keys(alive_table, mortal_reference.alive_reference);
                if array_length(foreign_keys, 1) != 1 then
                    raise exception 'exactly one foreign constraint expected for table % for column %, but got %', alive_table, mortal_reference.alive_reference, array_length(foreign_keys, 1);
                end if;

                execute format(
                        'alter table %s alter constraint %s initially deferred',
                        alive_table,
                        foreign_keys[1].constraint_name
                        );
            end if;
        end loop;

    foreach mortal_reference in array grave_table_mortal_references loop
            if mortal_reference.grave_table = grave_table then
                foreign_keys := __mortal_pattern__get_column_foreign_keys(grave_table, mortal_reference.grave_reference);
                if array_length(foreign_keys, 1) != 1 then
                    raise exception 'exactly one foreign constraint expected for table % for column %, but got %', grave_table, mortal_reference.grave_table, array_length(foreign_keys, 1);
                end if;

                execute format(
                        'alter table %s alter constraint %s initially deferred',
                        grave_table,
                        foreign_keys[1].constraint_name
                        );
            end if;
        end loop;
end; $$;



CREATE FUNCTION public.setup_revision(p_table regclass) RETURNS void
    LANGUAGE plpgsql
AS $$
begin
    execute format(
            'create trigger increment_revision
             before insert or update on %s
             for each row
             execute function increment_revision()',
            p_table
            );
end; $$;



CREATE FUNCTION public.trigger__file__owner_user_bury() RETURNS trigger
    LANGUAGE plpgsql
AS $$ begin
    update file
    set owner_user_grave = OLD.id, owner_user = NULL
    where owner_user = OLD.id;
    return OLD;
end; $$;



CREATE FUNCTION public.trigger__grave_post__author_bury() RETURNS trigger
    LANGUAGE plpgsql
AS $$ begin
    update grave_post
    set author_grave = OLD.id, author = NULL
    where author = OLD.id;
    return OLD;
end; $$;



CREATE FUNCTION public.trigger__post__bury() RETURNS trigger
    LANGUAGE plpgsql
AS $$ declare
    v_author user_id := null;
    v_author_grave user_id := null;

begin

    if OLD.author is null then
            elseif exists (select 1 from users where id = OLD.author) then
        v_author := OLD.author;
    else
        v_author_grave := OLD.author;
    end if;



    insert into grave_post (deleted_at, id, created_at, last_edit_date, author, author_grave)
    values (current_timestamp, OLD.id, OLD.created_at, OLD.last_edit_date, v_author, v_author_grave);
    return OLD;
end; $$;



CREATE FUNCTION public.trigger__post__reply_to_bury() RETURNS trigger
    LANGUAGE plpgsql
AS $$ begin
    update post
    set reply_to_grave = OLD.id, reply_to = NULL
    where reply_to = OLD.id;
    return OLD;
end; $$;



CREATE FUNCTION public.trigger__post__repost__original_bury() RETURNS trigger
    LANGUAGE plpgsql
AS $$ begin
    update post__repost
    set original_grave = OLD.id, original = NULL
    where original = OLD.id;
    return OLD;
end; $$;



CREATE FUNCTION public.trigger__users__bury() RETURNS trigger
    LANGUAGE plpgsql
AS $$ declare

begin



    insert into grave_users (deleted_at, id, first_name, last_name)
    values (current_timestamp, OLD.id, OLD.first_name, OLD.last_name);
    return OLD;
end; $$;


SET default_tablespace = '';

SET default_table_access_method = heap;


CREATE TABLE public.auth_state (
                                   session bigint NOT NULL,
                                   "user" bigint
);



CREATE TABLE public.bitmap (
                               file bigint NOT NULL,
                               width integer,
                               height integer
);



CREATE TABLE public.file (
                             id bigint NOT NULL,
                             uri character varying(512) NOT NULL,
                             name character varying(128) NOT NULL,
                             upload_date timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
                             expires_at timestamp without time zone,
                             size_bytes bigint NOT NULL,
                             owner_session bigint,
                             owner_user bigint,
                             owner_user_grave bigint,
                             CONSTRAINT owner_user_check CHECK ((num_nonnulls(owner_user, owner_user_grave) <= 1))
);



ALTER TABLE public.file ALTER COLUMN id ADD GENERATED ALWAYS AS IDENTITY (
    SEQUENCE NAME public.file_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
    );



CREATE TABLE public.grave_post (
                                   id public.post_id NOT NULL,
                                   created_at timestamp without time zone NOT NULL,
                                   last_edit_date timestamp without time zone,
                                   deleted_at timestamp without time zone NOT NULL,
                                   author public.user_id,
                                   author_grave public.user_id,
                                   CONSTRAINT author_check CHECK ((num_nonnulls(author, author_grave) <= 1))
);



CREATE TABLE public.grave_users (
                                    id public.user_id NOT NULL,
                                    deleted_at timestamp without time zone NOT NULL,
                                    first_name character varying(64) NOT NULL,
                                    last_name character varying(64)
);



CREATE TABLE public.image (
                              file bigint NOT NULL,
                              format character varying(8) NOT NULL,
                              width integer,
                              height integer
);



CREATE TABLE public.post (
                             id bigint NOT NULL,
                             revision public.revision,
                             created_at timestamp without time zone NOT NULL,
                             author public.user_id,
                             reply_to public.post_id,
                             reply_to_grave public.post_id,
                             last_edit_date timestamp without time zone,
                             content_type public.post_content_type NOT NULL,
                             location_global boolean NOT NULL,
                             location_profile bigint,
                             CONSTRAINT reply_to_check CHECK ((num_nonnulls(reply_to, reply_to_grave) <= 1))
);



CREATE TABLE public.post__repost (
                                     id bigint NOT NULL,
                                     original bigint,
                                     original_grave bigint,
                                     comment character varying(4096),
                                     CONSTRAINT original_check CHECK ((num_nonnulls(original, original_grave) <= 1))
);



CREATE TABLE public.post__standalone (
                                         id bigint NOT NULL,
                                         text character varying(4096) NOT NULL
);



CREATE TABLE public.post__standalone__attachments (
                                                      id bigint NOT NULL,
                                                      post bigint,
                                                      file bigint,
                                                      caption character varying(4096) NOT NULL
);



ALTER TABLE public.post__standalone__attachments ALTER COLUMN id ADD GENERATED ALWAYS AS IDENTITY (
    SEQUENCE NAME public.post__standalone__attachments_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
    );



ALTER TABLE public.post ALTER COLUMN id ADD GENERATED ALWAYS AS IDENTITY (
    SEQUENCE NAME public.post_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
    );



CREATE TABLE public.session (
                                id bigint NOT NULL,
                                revision public.revision,
                                creation_date timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL
);



ALTER TABLE public.session ALTER COLUMN id ADD GENERATED ALWAYS AS IDENTITY (
    SEQUENCE NAME public.session_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
    );



CREATE TABLE public.users (
                              id bigint NOT NULL,
                              revision public.revision,
                              first_name character varying(64) NOT NULL,
                              last_name character varying(64),
                              registration_date timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
                              phone_number character varying(32),
                              telegram_auth_id varchar(255) default null,
                              telegram_user_id bigint default null,
                              email character varying(128),
                              password_plaintext character varying(256),
                              bio character varying(1024),
                              birthday date,
                              cover bigint,
                              avatar bigint,
                              CONSTRAINT users_birthday_check CHECK (((EXTRACT(year FROM birthday) > (1800)::numeric) OR (EXTRACT(year FROM birthday) = (4)::numeric)))
);



ALTER TABLE public.users ALTER COLUMN id ADD GENERATED BY DEFAULT AS IDENTITY (
    SEQUENCE NAME public.users_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
    );



CREATE TABLE public.vector_image (
                                     file bigint NOT NULL,
                                     format character varying(8) NOT NULL,
                                     width real,
                                     height real
);



CREATE TABLE public.video (
                              file bigint NOT NULL,
                              format character varying(8) NOT NULL,
                              duration_millis bigint,
                              width integer,
                              height integer
);



CREATE VIEW public.view_grave_post AS
SELECT p.id,
       p.created_at,
       p.last_edit_date,
       a.id AS author_id,
       a.first_name AS author_first_name,
       a.last_name AS author_last_name
FROM (public.grave_post p
    JOIN public.users a ON ((a.id = (p.author)::bigint)));



CREATE VIEW public.view_post AS
SELECT p.id,
       p.revision,
       p.location_global,
       p.location_profile,
       p.created_at,
       p.last_edit_date,
       p.content_type,
       lu.first_name AS location_profile_first_name,
       lu.last_name AS location_profile_last_name,
       a.id AS author_id,
       a.first_name AS author_first_name,
       a.last_name AS author_last_name,
       COALESCE(r.id, (gr.id)::bigint) AS reply_id,
       COALESCE(r.created_at, gr.created_at) AS reply_created_at,
       COALESCE(NULL::timestamp without time zone, gr.deleted_at) AS reply_deleted_at,
       COALESCE(r.author, gr.author, gr.author_grave) AS reply_author_id,
       COALESCE(ru.first_name, rgu.first_name) AS reply_author_first_name,
       COALESCE(ru.last_name, rgu.last_name) AS reply_author_last_name,
       standalone.text AS standalone_text,
       repost.comment AS repost_comment,
       COALESCE(repost_p.id, (repost_gp.id)::bigint) AS repost_original_id,
       COALESCE(repost_p.created_at, repost_gp.created_at) AS repost_original_created_at,
       COALESCE(NULL::timestamp without time zone, repost_gp.deleted_at) AS repost_original_deleted_at,
       COALESCE(repost_p.last_edit_date, repost_gp.last_edit_date) AS repost_original_last_edit_date,
       COALESCE(repost_u.id, repost_gpu.id, (repost_gpgu.id)::bigint) AS repost_original_author_id,
       COALESCE(repost_gu.deleted_at, repost_gpgu.deleted_at) AS repost_original_author_deleted_at,
       COALESCE(repost_u.first_name, repost_gpu.first_name, repost_gpgu.first_name) AS repost_original_author_first_name,
       COALESCE(repost_u.last_name, repost_gpu.last_name, repost_gpgu.last_name) AS repost_original_author_last_name
FROM ((((((((((((((public.post p
    JOIN public.users a ON ((a.id = (p.author)::bigint)))
    LEFT JOIN public.users lu ON ((lu.id = p.location_profile)))
    LEFT JOIN public.post r ON ((r.id = (p.reply_to)::bigint)))
    LEFT JOIN public.grave_post gr ON (((gr.id)::bigint = (p.reply_to_grave)::bigint)))
    LEFT JOIN public.users ru ON (((ru.id = (r.author)::bigint) OR (ru.id = (gr.author)::bigint))))
    LEFT JOIN public.grave_users rgu ON (((rgu.id)::bigint = (gr.author_grave)::bigint)))
    LEFT JOIN public.post__standalone standalone ON (((p.content_type = 'standalone'::public.post_content_type) AND (standalone.id = p.id))))
    LEFT JOIN public.post__repost repost ON (((p.content_type = 'repost'::public.post_content_type) AND (repost.id = p.id))))
    LEFT JOIN public.post repost_p ON ((repost_p.id = repost.original)))
    LEFT JOIN public.users repost_u ON ((repost_u.id = (repost_p.author)::bigint)))
    LEFT JOIN public.grave_users repost_gu ON (((repost_gu.id)::bigint = (repost_p.author)::bigint)))
    LEFT JOIN public.grave_post repost_gp ON (((repost_gp.id)::bigint = repost.original_grave)))
    LEFT JOIN public.users repost_gpu ON ((repost_gpu.id = (repost_gp.author)::bigint)))
    LEFT JOIN public.grave_users repost_gpgu ON (((repost_gpgu.id)::bigint = (repost_gp.author_grave)::bigint)))
WHERE (((p.content_type = 'standalone'::public.post_content_type) AND (standalone.text IS NOT NULL)) OR ((p.content_type = 'repost'::public.post_content_type) AND (COALESCE(repost.original, repost.original_grave) IS NOT NULL)));



ALTER TABLE ONLY public.auth_state
    ADD CONSTRAINT auth_state_pkey PRIMARY KEY (session);



ALTER TABLE ONLY public.bitmap
    ADD CONSTRAINT bitmap_pkey PRIMARY KEY (file);



ALTER TABLE ONLY public.file
    ADD CONSTRAINT file_pkey PRIMARY KEY (id);



ALTER TABLE ONLY public.grave_post
    ADD CONSTRAINT grave_post_pkey PRIMARY KEY (id);



ALTER TABLE ONLY public.grave_users
    ADD CONSTRAINT grave_users_pkey PRIMARY KEY (id);



ALTER TABLE ONLY public.image
    ADD CONSTRAINT image_pkey PRIMARY KEY (file);



ALTER TABLE ONLY public.post__repost
    ADD CONSTRAINT post__repost_pkey PRIMARY KEY (id);



ALTER TABLE ONLY public.post__standalone
    ADD CONSTRAINT post__standalone_pkey PRIMARY KEY (id);



ALTER TABLE ONLY public.post
    ADD CONSTRAINT post_pkey PRIMARY KEY (id);



ALTER TABLE ONLY public.session
    ADD CONSTRAINT session_pkey PRIMARY KEY (id);



ALTER TABLE ONLY public.users
    ADD CONSTRAINT users_pkey PRIMARY KEY (id);



ALTER TABLE ONLY public.vector_image
    ADD CONSTRAINT vector_image_pkey PRIMARY KEY (file);



ALTER TABLE ONLY public.video
    ADD CONSTRAINT video_pkey PRIMARY KEY (file);



CREATE TRIGGER bury BEFORE DELETE ON public.post FOR EACH ROW EXECUTE FUNCTION public.trigger__post__bury();



CREATE TRIGGER bury BEFORE DELETE ON public.users FOR EACH ROW EXECUTE FUNCTION public.trigger__users__bury();



CREATE TRIGGER bury__file__owner_user BEFORE DELETE ON public.users FOR EACH ROW EXECUTE FUNCTION public.trigger__file__owner_user_bury();



CREATE TRIGGER bury__grave_post__author BEFORE DELETE ON public.users FOR EACH ROW EXECUTE FUNCTION public.trigger__grave_post__author_bury();



CREATE TRIGGER bury__post__reply_to AFTER DELETE ON public.post FOR EACH ROW EXECUTE FUNCTION public.trigger__post__reply_to_bury();



CREATE TRIGGER bury__post__repost__original BEFORE DELETE ON public.post FOR EACH ROW EXECUTE FUNCTION public.trigger__post__repost__original_bury();



CREATE TRIGGER increment_revision BEFORE INSERT OR UPDATE ON public.post FOR EACH ROW EXECUTE FUNCTION public.increment_revision();



CREATE TRIGGER increment_revision BEFORE INSERT OR UPDATE ON public.session FOR EACH ROW EXECUTE FUNCTION public.increment_revision();



CREATE TRIGGER increment_revision BEFORE INSERT OR UPDATE ON public.users FOR EACH ROW EXECUTE FUNCTION public.increment_revision();



ALTER TABLE ONLY public.auth_state
    ADD CONSTRAINT auth_state_session_fkey FOREIGN KEY (session) REFERENCES public.session(id) ON UPDATE CASCADE ON DELETE CASCADE;



ALTER TABLE ONLY public.auth_state
    ADD CONSTRAINT auth_state_user_fkey FOREIGN KEY ("user") REFERENCES public.users(id) ON UPDATE CASCADE ON DELETE CASCADE;



ALTER TABLE ONLY public.bitmap
    ADD CONSTRAINT bitmap_file_fkey FOREIGN KEY (file) REFERENCES public.file(id) ON UPDATE CASCADE ON DELETE CASCADE;



ALTER TABLE ONLY public.file
    ADD CONSTRAINT file_owner_session_fkey FOREIGN KEY (owner_session) REFERENCES public.session(id) ON UPDATE CASCADE;



ALTER TABLE ONLY public.file
    ADD CONSTRAINT file_owner_user_fkey FOREIGN KEY (owner_user) REFERENCES public.users(id) ON UPDATE CASCADE;



ALTER TABLE ONLY public.file
    ADD CONSTRAINT file_owner_user_grave_fkey FOREIGN KEY (owner_user_grave) REFERENCES public.grave_users(id) ON UPDATE CASCADE;



ALTER TABLE ONLY public.grave_post
    ADD CONSTRAINT grave_post_author_fkey FOREIGN KEY (author) REFERENCES public.users(id) ON UPDATE CASCADE;



ALTER TABLE ONLY public.grave_post
    ADD CONSTRAINT grave_post_author_grave_fkey FOREIGN KEY (author_grave) REFERENCES public.grave_users(id) ON UPDATE CASCADE;



ALTER TABLE ONLY public.image
    ADD CONSTRAINT image_file_fkey FOREIGN KEY (file) REFERENCES public.file(id) ON UPDATE CASCADE ON DELETE CASCADE;



ALTER TABLE ONLY public.post__repost
    ADD CONSTRAINT post__repost_id_fkey FOREIGN KEY (id) REFERENCES public.post(id) ON UPDATE CASCADE ON DELETE CASCADE;



ALTER TABLE ONLY public.post__repost
    ADD CONSTRAINT post__repost_original_fkey FOREIGN KEY (original) REFERENCES public.post(id) ON UPDATE CASCADE;



ALTER TABLE ONLY public.post__repost
    ADD CONSTRAINT post__repost_original_grave_fkey FOREIGN KEY (original_grave) REFERENCES public.grave_post(id) ON UPDATE CASCADE;



ALTER TABLE ONLY public.post__standalone__attachments
    ADD CONSTRAINT post__standalone__attachments_file_fkey FOREIGN KEY (file) REFERENCES public.file(id) ON UPDATE CASCADE;



ALTER TABLE ONLY public.post__standalone__attachments
    ADD CONSTRAINT post__standalone__attachments_post_fkey FOREIGN KEY (post) REFERENCES public.post(id) ON UPDATE CASCADE ON DELETE CASCADE;



ALTER TABLE ONLY public.post__standalone
    ADD CONSTRAINT post__standalone_id_fkey FOREIGN KEY (id) REFERENCES public.post(id) ON UPDATE CASCADE ON DELETE CASCADE;



ALTER TABLE ONLY public.post
    ADD CONSTRAINT post_author_fkey FOREIGN KEY (author) REFERENCES public.users(id) ON UPDATE CASCADE ON DELETE CASCADE;



ALTER TABLE ONLY public.post
    ADD CONSTRAINT post_location_profile_fkey FOREIGN KEY (location_profile) REFERENCES public.users(id) ON UPDATE CASCADE ON DELETE CASCADE;



ALTER TABLE ONLY public.post
    ADD CONSTRAINT post_reply_to_fkey FOREIGN KEY (reply_to) REFERENCES public.post(id) ON UPDATE CASCADE DEFERRABLE INITIALLY DEFERRED;



ALTER TABLE ONLY public.post
    ADD CONSTRAINT post_reply_to_grave_fkey FOREIGN KEY (reply_to_grave) REFERENCES public.grave_post(id) ON UPDATE CASCADE;



ALTER TABLE ONLY public.users
    ADD CONSTRAINT users_avatar_fkey FOREIGN KEY (avatar) REFERENCES public.file(id) ON UPDATE CASCADE;



ALTER TABLE ONLY public.users
    ADD CONSTRAINT users_cover_fkey FOREIGN KEY (cover) REFERENCES public.file(id) ON UPDATE CASCADE;



ALTER TABLE ONLY public.vector_image
    ADD CONSTRAINT vector_image_file_fkey FOREIGN KEY (file) REFERENCES public.file(id) ON UPDATE CASCADE ON DELETE CASCADE;



ALTER TABLE ONLY public.video
    ADD CONSTRAINT video_file_fkey FOREIGN KEY (file) REFERENCES public.file(id) ON UPDATE CASCADE ON DELETE CASCADE;

