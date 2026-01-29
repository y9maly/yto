create domain revision as bigint not null default 1;

create or replace function increment_revision()
    returns trigger language plpgsql as $$
begin
    NEW.revision := coalesce(OLD.revision, 0) + 1;
    RETURN NEW;
end; $$;

create or replace function setup_revision(p_table regclass)
    returns void language plpgsql as $$
begin
    execute format(
        'create trigger increment_revision
         before insert or update on %s
         for each row
         execute function increment_revision()',
        p_table
    );
end; $$;
