drop table if exists users;
create table if not exists schedules
(
    id              uuid primary key,
    user_id         uuid      not null,
    start_date_time timestamp not null,
    end_date_time   timestamp not null,
    is_reserved     boolean   not null,
    created_at      timestamp not null
);