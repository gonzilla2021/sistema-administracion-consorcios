begin;

-- =========================================================
-- AMENITIES
-- =========================================================

create table if not exists dbo.amenities (
    id uuid primary key,
    consorcio_id uuid not null,
    nombre varchar(100) not null,
    descripcion varchar(500),
    activo boolean not null default true,

    constraint fk_amenities_consorcio
        foreign key (consorcio_id)
        references dbo.consorcios (id),

    constraint uk_amenity_consorcio_nombre
        unique (consorcio_id, nombre)
);

-- =========================================================
-- TURNOS
-- =========================================================

create table if not exists dbo.turnos_amenity (
    id uuid primary key,
    amenity_id uuid not null,
    nombre varchar(60) not null,
    hora_inicio time not null,
    hora_fin time not null,
    activo boolean not null default true,

    constraint fk_turnos_amenity_amenity
        foreign key (amenity_id)
        references dbo.amenities (id),

    constraint uk_turno_amenity_nombre
        unique (amenity_id, nombre),

    constraint ck_turno_amenity_horario
        check (hora_fin > hora_inicio)
);

-- =========================================================
-- RESERVAS
-- =========================================================

create table if not exists dbo.reservas_amenity (
    id uuid primary key,
    amenity_id uuid not null,
    turno_id uuid not null,
    persona_id uuid not null,
    unidad_funcional_id uuid not null,
    fecha date not null,
    estado varchar(40) not null,
    fecha_creacion timestamp not null,
    fecha_cancelacion timestamp,

    constraint fk_reservas_amenity_amenity
        foreign key (amenity_id)
        references dbo.amenities (id),

    constraint fk_reservas_amenity_turno
        foreign key (turno_id)
        references dbo.turnos_amenity (id),

    constraint fk_reservas_amenity_persona
        foreign key (persona_id)
        references dbo.personas (id),

    constraint fk_reservas_amenity_unidad
        foreign key (unidad_funcional_id)
        references dbo.unidadesfuncionales (id),

    constraint ck_reserva_amenity_estado
        check (
            estado in (
                'ACTIVA',
                'CANCELADA_POR_USUARIO'
            )
        )
);

-- =========================================================
-- BLOQUEOS ADMINISTRATIVOS
-- =========================================================

create table if not exists dbo.bloqueos_amenity (
    id uuid primary key,
    amenity_id uuid not null,
    turno_id uuid not null,
    fecha date not null,
    motivo varchar(500) not null,
    fecha_creacion timestamp not null,
    activo boolean not null default true,

    constraint fk_bloqueos_amenity_amenity
        foreign key (amenity_id)
        references dbo.amenities (id),

    constraint fk_bloqueos_amenity_turno
        foreign key (turno_id)
        references dbo.turnos_amenity (id)
);

-- =========================================================
-- ÍNDICES
-- =========================================================

create index if not exists idx_reserva_amenity_fecha
    on dbo.reservas_amenity (
        amenity_id,
        fecha
    );

create index if not exists idx_bloqueo_amenity_fecha
    on dbo.bloqueos_amenity (
        amenity_id,
        fecha
    );

create unique index if not exists uk_reserva_activa_amenity_turno
    on dbo.reservas_amenity (
        amenity_id,
        turno_id,
        fecha
    )
    where estado = 'ACTIVA';

create unique index if not exists uk_bloqueo_activo_amenity_turno
    on dbo.bloqueos_amenity (
        amenity_id,
        turno_id,
        fecha
    )
    where activo = true;

commit;