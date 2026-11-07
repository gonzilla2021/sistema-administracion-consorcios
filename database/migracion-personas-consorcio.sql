-- Migracion: las personas pasan a pertenecer a un consorcio.
-- Se ejecuta UNA vez sobre una base ya creada con el esquema anterior (Dni unico global).
-- Revisar el resultado de los SELECT de control antes de confirmar.

BEGIN;

ALTER TABLE dbo.Personas ADD COLUMN ConsorcioId UUID;

-- 1) Personas vinculadas a una unidad: toman el consorcio de esa unidad.
UPDATE dbo.Personas p
SET ConsorcioId = (
    SELECT uf.ConsorcioId
    FROM dbo.VinculacionesUnidad v
    JOIN dbo.UnidadesFuncionales uf ON uf.Id = v.UnidadFuncionalId
    WHERE v.PersonaId = p.Id
    ORDER BY v.FechaInicio
    LIMIT 1
)
WHERE p.ConsorcioId IS NULL;

-- 2) Personas con usuario administrador de un consorcio.
UPDATE dbo.Personas p
SET ConsorcioId = u.ConsorcioId
FROM dbo.Usuarios u
WHERE u.PersonaId = p.Id AND u.ConsorcioId IS NOT NULL AND p.ConsorcioId IS NULL;

-- Control: personas que quedaron sin consorcio (hay que asignarlas a mano o borrarlas).
SELECT Id, Nombre, Apellido, Dni FROM dbo.Personas WHERE ConsorcioId IS NULL;

-- Control: personas vinculadas a unidades de mas de un consorcio (solo se conserva el primero).
SELECT v.PersonaId, COUNT(DISTINCT uf.ConsorcioId) AS consorcios
FROM dbo.VinculacionesUnidad v
JOIN dbo.UnidadesFuncionales uf ON uf.Id = v.UnidadFuncionalId
GROUP BY v.PersonaId
HAVING COUNT(DISTINCT uf.ConsorcioId) > 1;

-- Si el primer SELECT devolvio filas, resolverlas y recien despues ejecutar lo siguiente:
ALTER TABLE dbo.Personas ALTER COLUMN ConsorcioId SET NOT NULL;
ALTER TABLE dbo.Personas ADD CONSTRAINT FK_Personas_Consorcios FOREIGN KEY (ConsorcioId) REFERENCES dbo.Consorcios (Id);
ALTER TABLE dbo.Personas DROP CONSTRAINT UQ_Personas_Dni;
ALTER TABLE dbo.Personas ADD CONSTRAINT UQ_Personas_Consorcio_Dni UNIQUE (ConsorcioId, Dni);
CREATE INDEX IX_Personas_ConsorcioId ON dbo.Personas (ConsorcioId);

COMMIT;
