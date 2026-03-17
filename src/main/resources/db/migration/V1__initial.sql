create schema if not exists mc_user_service;

create table if not exists mc_user_service.user (
    id         varchar(255) primary key,
    email      varchar(255) not null unique,
    username   varchar(255) not null unique,
    first_name varchar(255) not null,
    last_name  varchar(255) not null,
    created    timestamp    not null
);

create index if not exists idx__user__username on mc_user_service.user (username);
create index if not exists idx__user__first_name on mc_user_service.user (first_name);
create index if not exists idx__user__last_name on mc_user_service.user (last_name);

create table if not exists mc_user_service.organization (
    id          uuid primary key,
    name        varchar(255) not null,
    description varchar(255),
    created     timestamp    not null,
    deleted     boolean      not null
);

create index if not exists idx__organization__name on mc_user_service.organization (name);

create type mc_user_service.organization_role as enum ('OWNER', 'ADMIN', 'MEMBER', 'WAREHOUSE_MANAGER');

create table if not exists mc_user_service.user_organization (
    user_id         varchar(255) references mc_user_service.user (id),
    organization_id uuid references mc_user_service.organization (id),
    role            mc_user_service.organization_role not null,
    created         timestamp                         not null,
    unique (user_id, organization_id)
);

create index if not exists idx__user_organization__user_id on mc_user_service.user_organization (user_id);
create index if not exists idx__user_organization__organization_id on mc_user_service.user_organization (organization_id);

create table if not exists mc_user_service.clients (
    id              uuid primary key,
    first_name      varchar(255) not null,
    middle_name     varchar(255),
    last_name       varchar(255),
    date_of_birth   date,
    email           varchar(255),
    phone_number    varchar(15),
    organization_id uuid references mc_user_service.organization (id)
);

create index if not exists idx__clients__first_name on mc_user_service.clients (first_name);
create index if not exists idx__clients__last_name on mc_user_service.clients (last_name);
create index if not exists idx__clients__date_of_birth on mc_user_service.clients (date_of_birth);
create index if not exists idx__clients__organization_id on mc_user_service.clients (organization_id);
