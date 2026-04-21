create type mc_user_service.chat_session_status as enum ('ACTIVE', 'CLOSED');

create type mc_user_service.chat_sender_type as enum ('ANONYMOUS', 'EMPLOYEE');

create table if not exists mc_user_service.chat_session (
    id              uuid primary key,
    organization_id uuid                                not null references mc_user_service.organization (id),
    anonymous_id    uuid                                not null,
    anonymous_name  varchar(255),
    status          mc_user_service.chat_session_status not null default 'ACTIVE',
    created         timestamp                           not null,
    closed          timestamp
);

create index if not exists idx__chat_session__organization_id on mc_user_service.chat_session (organization_id);
create index if not exists idx__chat_session__anonymous_id on mc_user_service.chat_session (anonymous_id);
create index if not exists idx__chat_session__status on mc_user_service.chat_session (status);

create table if not exists mc_user_service.chat_message (
    id              uuid primary key,
    chat_session_id uuid                               not null references mc_user_service.chat_session (id),
    sender_type     mc_user_service.chat_sender_type   not null,
    sender_id       varchar(255)                       not null,
    content         text                               not null,
    created         timestamp                          not null
);

create index if not exists idx__chat_message__chat_session_id on mc_user_service.chat_message (chat_session_id);
create index if not exists idx__chat_message__created on mc_user_service.chat_message (created);
