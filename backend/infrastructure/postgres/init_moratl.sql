-- Цель этой штуки сделать так, чтобы удаляемые записи из БД не удалялись полностью, а часть нужных данных оставалась.
--
-- Например, здесь можно делать посты и их репосты, отвечать на другие посты.
-- Если просто удалять оригинальный пост, то либо:
--   1. Каскадно удалятся все ответы и репосты
--   2. Все ответы и репосты потеряют информацию о том, что они кому-то отвечали, кого-то репостили
-- Есть паттерн soft-delete, когда добавляешь поле deleted_at, но мне он не понравился:
--   1. Каждая операция с таблицей должна добавлять проверку `deleted_at is null`
--   2. Каскадное удаление не работает, ссылающиеся таблицы не узнают о том, что строка на самом деле была удалена
-- Поэтому я придумал класть удалённые записи в другую таблицу (grave-таблицу)
--
-- Это не является нарушением закона о персональных данных (если довести реализацию до конца) (но я не юрист).
--   В grave-таблице сохраняются не все данные (например при удалении поста удаляется контент, но сохраняется автор).
--   Записи из grave-таблицы должны удалиться, если эти записи больше никому не нужны (пока что это не реализовано).
--   Например:
--     Если ты пересылаешь сообщение от человека в телеграме, а тот человек удалил свой аккаунт, это не является нарушением
--       если телеграм всё еще хранит сообщение от удаленного аккаунта, потому-что другим пользователем была сделана копия.
--     Здесь то же самое, только оптимизируется хранилище: сообщение не копируется, а пересланное ссылается на оригинальное,
--       даже если оригинальное была удалено, при этом фактически это является копией сообщения,
--       просто хранение реализовано без дублирования данных.
--
-- Например у меня при удалении пользователя каскадно удаляются все его посты, но сохраняется имя пользователя,
-- чтобы во всех репостах было написано:
-- "Оригинальный пост был удалён N числа, его создал удаленный пользователь, который на момент удаления имел имя 'Something'"
--
-- В каждой таблице, которая может ссылаться на удаленные данные создается 2 FK:
--   1. FK на живую таблицу
--   2. FK на grave-таблицу
-- С помощью CHECK гарантируется, что только 1 из FK будет NOT NULL.
-- С помощью триггеров реализуется перенос записей в grave-таблицу при удалении записей из оригинальной таблицы.
--   (Я вроде учёл и протестил случаи циклических ссылок и ссылок на эту же таблицу, они тоже переносятся нормально.)
-- С помощью триггеров во всех таблицах живой FK заменяется на grave-FK.
--
--
--
-- create table users (
--     id         bigint generated always as identity primary key,
--     first_name text not null,
--     last_name  text not null
-- );
--
-- create table grave_users (
--     id         bigint primary key,
--     deleted_at timestamp not null default current_timestamp, -- В удаленную запись добавляется инфа о дате удаления
--     first_name text      not null -- Сохраняется имя
--                                   -- Но удаляется фамилия
-- );
--
-- -- Создать триггер, который копирует данные из users в grave_users, сохраняя колонки id и first_name.
-- -- Префикс grave_ захардкожен.
-- select setup_mortal_table('users', 'id, first_name');
--
-- create table orders (
--     id         bigint generated always as identity primary key,
--     user       bigint references users on update cascade,
--     user_grave bigint references grave_users on update cascade
-- );
--
-- -- Создать триггер для orders, который переносит user ссылки на user_grave при удалении пользователя.
-- -- Суффикс _grave захардкожен.
-- select setup_mortal_reference('orders', 'user');


create type __mortal_pattern__mortal_reference as (
    alive_reference text,
    grave_reference text,
    typename        text,
    alive_table     regclass,
    alive_table_pk  text,
    grave_table     regclass,
    grave_table_pk  text
);

create type __mortal_pattern__foreign_key as (
    target_table    regclass,
    target_column   text,
    constraint_name text
);

-- # (mention_id,mention_id_grave,bigint)
-- # (reply_to_post_id,reply_to_post_id_grave,bigint)
create or replace function __mortal_pattern__get_table_mortal_references(_table regclass)
    returns __mortal_pattern__mortal_reference[] as $$ declare
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

        -- foreign key from alive column
        join pg_constraint fk_alive
        on fk_alive.conrelid = a.attrelid
        and fk_alive.contype = 'f'
        and a.attnum = any (fk_alive.conkey)

        -- foreign key from grave column
        join pg_constraint fk_grave
        on fk_grave.conrelid = g.attrelid
        and fk_grave.contype = 'f'
        and g.attnum = any (fk_grave.conkey)

        -- primary key of alive referenced table
        join pg_constraint pkc_alive
        on pkc_alive.conrelid = fk_alive.confrelid
        and pkc_alive.contype = 'p'

        join pg_attribute pk_alive
        on pk_alive.attrelid = pkc_alive.conrelid
        and pk_alive.attnum = any (pkc_alive.conkey)
        and pk_alive.attnum > 0
        and not pk_alive.attisdropped

        -- primary key of grave referenced table
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
end; $$ language plpgsql;

create or replace function __mortal_pattern__get_table_mortal_reference_by_alive_reference(_table regclass, alive_reference text)
    returns __mortal_pattern__mortal_reference as $$ declare
    all_references __mortal_pattern__mortal_reference[] := __mortal_pattern__get_table_mortal_references(_table);
    reference __mortal_pattern__mortal_reference;
begin
    foreach reference in array all_references loop
        if reference.alive_reference = alive_reference then
            return reference;
        end if;
    end loop;

    raise exception 'table % doesn''t have column %', _table, alive_reference;
end; $$ language plpgsql;

create or replace function __mortal_pattern__get_column_foreign_keys(p_table regclass, p_column text)
    returns __mortal_pattern__foreign_key[] as $$ declare
    result __mortal_pattern__foreign_key[];
begin
    -- validate
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
end; $$ language plpgsql;

create or replace function __mortal_pattern__get_single_foreign_key(p_table regclass, p_column text)
    returns __mortal_pattern__foreign_key as $$ declare
    foreign_keys __mortal_pattern__foreign_key[] := __mortal_pattern__get_column_foreign_keys(p_table, p_column);
begin
    if array_length(foreign_keys, 1) != 1 then
        raise exception 'exactly one foreign constraint expected for table % for column %, but got %', p_table, p_column, array_length(foreign_keys, 1);
    end if;
    return foreign_keys[1];
end; $$ language plpgsql;

create or replace function __mortal_pattern__get_table_from_references_column(p_table regclass, p_column text)
    returns regclass as $$ declare
    foreign_key __mortal_pattern__foreign_key := __mortal_pattern__get_single_foreign_key(p_table, p_column);
begin
    return foreign_key.target_table;
end; $$ language plpgsql;



-- create or replace function trigger__<TABLE>__bury() returns trigger as $$ declare
--     for each <mortal reference>:
--         v_<alive reference> <type> := null;
--         v_<grave reference> <type> := null;
-- begin
--     for each <mortal reference>:
--         if OLD.<grave reference> is not null then
--             v_<grave reference> := OLD.<grave reference>;
--         elseif OLD.<alive reference> is null then
--             -- do nothing
--         elseif exists (select 1 from <alive table> where <alive table PK> = OLD.<alive reference>) then
--             v_<alive reference> := OLD.<alive reference>;
--         else
--             v_<grave reference> := OLD.<alive reference>;
--         end if;
--
--     insert into grave_<TABLE> (
--         deleted_at,
--         <array_to_string(regexp_split_to_array(columns, ',\s*'), ', ')>
--         -- id, username, birthday
--     )
--     values (
--         current_timestamp,
--         <array_to_string(regexp_split_to_array('a, b,   c,d', ',\s*'), ', OLD.')>
--         -- OLD.id, OLD.username, OLD.birthday
--     );
--     return OLD;
-- end; $$ language plpgsql;
--
-- create or replace trigger bury before delete on <TABLE>
-- for each row execute function trigger__<TABLE>__bury();
create or replace function setup_bury_trigger(alive_table regclass, columns text) returns void as $_$ declare
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

    -- foreach variables
    mortal_reference __mortal_pattern__mortal_reference;
    current_column text;
begin
    -- validate
    execute format('SELECT ''%s''::regclass;', alive_table);
    execute format('SELECT ''%s''::regclass;', 'grave_' || alive_table);
    -- SELECT has_column_privilege('<TABLE>', '<COLUMN>', 'SELECT');
    execute format(
        'SELECT has_column_privilege(''%s'', ''%s'', ''SELECT'');' || chr(10),
        alive_table,
        array_to_string(
            regexp_split_to_array(columns, ',\s*'),
            ''', ''SELECT'');' || chr(10) || 'SELECT has_column_privilege(''' || alive_table || ''', '''
        )
    );

    -- Инициализировать mortal_references: Оставить только те колонки, которые копируются в grave таблицу (есть в columns):
    foreach mortal_reference in array grave_table_all_mortal_references loop
        if exists (select 1 from unnest(columns_array) where unnest = mortal_reference.alive_reference) then
            mortal_references := array_append(mortal_references, mortal_reference);
        end if;
    end loop;

    -- declarations
    foreach mortal_reference in array mortal_references loop
        declarations := declarations
        || 'v_' || mortal_reference.alive_reference || ' ' || mortal_reference.typename || ' := null;' || chr(10)
        || 'v_' || mortal_reference.grave_reference || ' ' || mortal_reference.typename || ' := null;' || chr(10);
    end loop;

    -- Инициализировать direct_copy_columns. Это всё кроме mortal_reference.
    foreach current_column in array columns_array loop
        if not exists (select 1 from unnest(mortal_references) where alive_reference = current_column) then
            direct_copy_columns := array_append(direct_copy_columns, current_column);
        else
            indirect_columns := array_append(indirect_columns, current_column);
        end if;
    end loop;

    -- declarations_initialization
    -- if OLD.<grave reference> is not null then
    --     v_<grave reference> := OLD.<grave reference>;
    -- elseif OLD.<alive reference> is null then
    --     -- do nothing
    -- elseif exists (select 1 from <alive table> where <alive table PK> = OLD.<alive reference>) then
    --     v_<alive reference> := OLD.<alive reference>;
    -- else
    --     v_<grave reference> := OLD.<alive reference>;
    -- end if;
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
    -- direct columns
    create_function := create_function || array_to_string(direct_copy_columns, ', ');
    -- indirect columns
    foreach current_column in array indirect_columns loop
        create_function := create_function || ', ' || current_column || ', ' || current_column || '_grave';
    end loop;
    create_function := create_function
    || ')' || chr(10)
    || '    values (current_timestamp, OLD.' || array_to_string(direct_copy_columns, ', OLD.');
    -- indirect columns
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
end; $_$ language plpgsql;



-- alter table <REF_TABLE> drop constraint if exists <ALIVE_REFERENCE>_check;
--
-- alter table <REF_TABLE>
-- add constraint <ALIVE_REFERENCE>_check
-- check ( num_nonnulls(<ALIVE_REFERENCE>, <ALIVE_REFERENCE>_grave) <= 1 );
--
-- create or replace function trigger__<REF_TABLE>__<ALIVE_REFERENCE>_bury() returns trigger as $$ begin
--     update <REF_TABLE>
--     set <ALIVE_REFERENCE>_grave = OLD.<REFERENCES_COLUMN_NAME>, <ALIVE_REFERENCE> = NULL
--     where <ALIVE_REFERENCE> = OLD.<REFERENCES_COLUMN_NAME>;
--     return OLD;
-- end; $$ language plpgsql;
--
-- create or replace trigger bury__<REF_TABLE>__<ALIVE_REFERENCE>
-- before/after delete on <ALIVE_TABLE>
-- for each row execute function trigger__<REF_TABLE>__<ALIVE_REFERENCE>_bury();
create or replace function setup_bury_reference_trigger(
    referencesTable regclass,
    aliveReference text
) returns void as $_$ declare
    alive_table regclass := __mortal_pattern__get_table_from_references_column(referencesTable, aliveReference);
    mortal_reference __mortal_pattern__mortal_reference := __mortal_pattern__get_table_mortal_reference_by_alive_reference(referencesTable, aliveReference);
    referenced_column_PK_name text := mortal_reference.alive_table_pk;
begin
    -- validate
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
end; $_$ language plpgsql;



create or replace function setup_mortal_table(alive_table regclass, columns text) returns void as $_$ declare
    grave_table regclass := 'grave_' || alive_table;
    alive_table_mortal_references __mortal_pattern__mortal_reference[] := __mortal_pattern__get_table_mortal_references(alive_table);
    grave_table_mortal_references __mortal_pattern__mortal_reference[] := __mortal_pattern__get_table_mortal_references(grave_table);
    mortal_reference __mortal_pattern__mortal_reference;
    foreign_keys __mortal_pattern__foreign_key[];
begin
    -- Setup bury trigger
    perform setup_bury_trigger(alive_table, columns);

    -- Setup initially deferred for columns (resolve cyclic references abd references to the same table)
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
end; $_$ language plpgsql;


---------------------
--- api functions ---
---------------------


create or replace function setup_mortal_reference(_table regclass, reference text) returns void as $_$ declare
begin
    perform setup_bury_reference_trigger(_table, reference);
end; $_$ language plpgsql;

create or replace function setup_mortal_references(_table regclass, columns text) returns void as $_$ declare
    columns_array text[] := regexp_split_to_array(columns, ',\s*');
    reference text;
begin
    foreach reference in array columns_array loop
        perform setup_bury_reference_trigger(_table, reference);
    end loop;
end; $_$ language plpgsql;
