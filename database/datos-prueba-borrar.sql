-- Borra SOLO los datos demo (consorcios 'Consorcio Demo %', usuarios y personas @demo.test).
-- Usar antes de recargar si ya se cargo una version anterior. No toca admin@consorcios.test.
-- Incluye los datos del modulo de Amenities de los consorcios demo.
begin;
-- Amenities primero: reservas y bloqueos referencian turnos, personas y unidades.
delete from dbo.reservas_amenity where amenity_id in (select id from dbo.amenities where consorcio_id in (select id from dbo.consorcios where nombre like 'Consorcio Demo %'));
delete from dbo.bloqueos_amenity where amenity_id in (select id from dbo.amenities where consorcio_id in (select id from dbo.consorcios where nombre like 'Consorcio Demo %'));
delete from dbo.turnos_amenity where amenity_id in (select id from dbo.amenities where consorcio_id in (select id from dbo.consorcios where nombre like 'Consorcio Demo %'));
delete from dbo.amenities where consorcio_id in (select id from dbo.consorcios where nombre like 'Consorcio Demo %');
delete from dbo.liquidacionesunidad where consorcioid in (select id from dbo.consorcios where nombre like 'Consorcio Demo %');
delete from dbo.liquidaciones where consorcioid in (select id from dbo.consorcios where nombre like 'Consorcio Demo %');
delete from dbo.gastos where consorcioid in (select id from dbo.consorcios where nombre like 'Consorcio Demo %');
delete from dbo.vinculacionesunidad where unidadfuncionalid in (select id from dbo.unidadesfuncionales where consorcioid in (select id from dbo.consorcios where nombre like 'Consorcio Demo %'));
delete from dbo.usuarios where email like '%@demo.test';
-- Personas antes que consorcios: personas.consorcioid referencia a consorcios.
delete from dbo.personas where email like '%@demo.test'
   or consorcioid in (select id from dbo.consorcios where nombre like 'Consorcio Demo %');
delete from dbo.unidadesfuncionales where consorcioid in (select id from dbo.consorcios where nombre like 'Consorcio Demo %');
delete from dbo.consorcios where nombre like 'Consorcio Demo %';
commit;
