-- =====================================================================
-- Datos de prueba del modulo de Amenities (DML). Una sola transaccion.
-- Requiere: database/schema-consorcios.sql, database/amenities.sql y
-- database/datos-prueba-carga.sql ya ejecutados.
--
-- Carga espacios comunes, turnos, un bloqueo administrativo y reservas
-- (activas y una cancelada) en los consorcios demo. Las fechas son
-- relativas al dia de ejecucion, para que siempre caigan dentro de la
-- ventana de reserva de 31 dias. Puede ejecutarse mas de una vez.
-- Se borra con database/datos-prueba-borrar.sql.
-- =====================================================================
begin;

-- Espacios comunes en Demo 1, 3 y 4; en Demo 2 uno inactivo (para probar
-- que un amenity inactivo no admite reservas).
insert into dbo.amenities (id, consorcio_id, nombre, descripcion, activo)
select gen_random_uuid(), c.id, a.nombre, a.descripcion, a.activo
from dbo.consorcios c
join (values
    ('Consorcio Demo 1', 'SUM',      'Salón de usos múltiples, capacidad 30 personas', true),
    ('Consorcio Demo 1', 'Parrilla', 'Parrilla de la terraza',                          true),
    ('Consorcio Demo 1', 'Gimnasio', 'Gimnasio de planta baja',                         true),
    ('Consorcio Demo 3', 'SUM',      'Salón de usos múltiples',                         true),
    ('Consorcio Demo 3', 'Parrilla', 'Parrilla del patio',                              true),
    ('Consorcio Demo 4', 'SUM',      'Salón de usos múltiples',                         true),
    ('Consorcio Demo 4', 'Gimnasio', 'Gimnasio del primer piso',                        true),
    ('Consorcio Demo 2', 'Pileta',   'Pileta descubierta, cerrada por temporada',       false)
) as a(consorcio, nombre, descripcion, activo) on a.consorcio = c.nombre
on conflict (consorcio_id, nombre) do nothing;

-- Turnos: franjas sin superposicion dentro de cada amenity.
insert into dbo.turnos_amenity (id, amenity_id, nombre, hora_inicio, hora_fin, activo)
select gen_random_uuid(), am.id, t.nombre, t.inicio::time, t.fin::time, true
from dbo.amenities am
join dbo.consorcios c on c.id = am.consorcio_id and c.nombre like 'Consorcio Demo %'
join (values
    ('SUM',      'Mañana', '09:00', '13:00'),
    ('SUM',      'Tarde',  '14:00', '18:00'),
    ('SUM',      'Noche',  '19:00', '23:00'),
    ('Parrilla', 'Mediodía', '11:00', '16:00'),
    ('Parrilla', 'Noche',    '19:00', '23:59'),
    ('Gimnasio', 'Mañana', '07:00', '12:00'),
    ('Gimnasio', 'Tarde',  '16:00', '21:00'),
    ('Pileta',   'Día',    '10:00', '19:00')
) as t(amenity, nombre, inicio, fin) on t.amenity = am.nombre
on conflict (amenity_id, nombre) do nothing;

-- Bloqueo administrativo: Parrilla de Demo 1, turno Noche, dentro de 5 dias.
insert into dbo.bloqueos_amenity (id, amenity_id, turno_id, fecha, motivo, fecha_creacion, activo)
select gen_random_uuid(), am.id, tu.id, current_date + 5, 'Mantenimiento de la parrilla', now(), true
from dbo.amenities am
join dbo.consorcios c on c.id = am.consorcio_id and c.nombre = 'Consorcio Demo 1'
join dbo.turnos_amenity tu on tu.amenity_id = am.id and tu.nombre = 'Noche'
where am.nombre = 'Parrilla'
  and not exists (select 1 from dbo.bloqueos_amenity b
                  where b.amenity_id = am.id and b.turno_id = tu.id
                    and b.fecha = current_date + 5 and b.activo);

-- Reservas: personas con una vinculacion vigente en una unidad del mismo
-- consorcio que el amenity (la misma regla que aplica el backend).
with vigentes as (
    select v.personaid, v.unidadfuncionalid, uf.consorcioid,
           row_number() over (partition by uf.consorcioid order by uf.identificador, v.tipo) as n
    from dbo.vinculacionesunidad v
    join dbo.unidadesfuncionales uf on uf.id = v.unidadfuncionalid
    join dbo.consorcios c on c.id = uf.consorcioid and c.nombre in ('Consorcio Demo 1', 'Consorcio Demo 3', 'Consorcio Demo 4')
    where v.fechainicio <= current_date
      and (v.fechafin is null or v.fechafin >= current_date)
),
plan as (
    select * from (values
        ('SUM',      'Tarde',    2, 1, 'ACTIVA'),
        ('SUM',      'Noche',    7, 2, 'ACTIVA'),
        ('Parrilla', 'Mediodía', 3, 3, 'ACTIVA'),
        ('Gimnasio', 'Mañana',   1, 4, 'ACTIVA'),
        ('SUM',      'Mañana',   4, 5, 'CANCELADA_POR_USUARIO')
    ) as p(amenity, turno, dias, persona_n, estado)
)
insert into dbo.reservas_amenity
    (id, amenity_id, turno_id, persona_id, unidad_funcional_id, fecha, estado, fecha_creacion, fecha_cancelacion)
select gen_random_uuid(), am.id, tu.id, vg.personaid, vg.unidadfuncionalid,
       current_date + p.dias, p.estado, now(),
       case when p.estado = 'CANCELADA_POR_USUARIO' then now() end
from plan p
join dbo.amenities am on am.nombre = p.amenity
join dbo.turnos_amenity tu on tu.amenity_id = am.id and tu.nombre = p.turno
join vigentes vg on vg.consorcioid = am.consorcio_id and vg.n = p.persona_n
where not exists (select 1 from dbo.reservas_amenity r
                  where r.amenity_id = am.id and r.turno_id = tu.id
                    and r.fecha = current_date + p.dias
                    and r.persona_id = vg.personaid);

commit;
