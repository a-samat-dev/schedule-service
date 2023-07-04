create table if not exists schedules
(
    id              uuid primary key,
    user_id         uuid      not null,
    start_date_time timestamp not null,
    end_date_time   timestamp not null,
    is_reserved     boolean   not null,
    created_at      timestamp not null
);

alter table if exists schedules
    add unique (user_id, start_date_time);
alter table if exists schedules
    add unique (user_id, end_date_time);