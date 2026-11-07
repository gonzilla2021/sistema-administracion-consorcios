-- Migracion: un administrador puede administrar varios consorcios (o ninguno).
-- Reemplaza la columna Usuarios.ConsorcioId por la tabla UsuariosConsorcios.
-- Se ejecuta UNA vez sobre una base creada con el esquema anterior.

BEGIN;

CREATE TABLE dbo.UsuariosConsorcios (
    UsuarioId   UUID NOT NULL,
    ConsorcioId UUID NOT NULL,
    CONSTRAINT PK_UsuariosConsorcios PRIMARY KEY (UsuarioId, ConsorcioId),
    CONSTRAINT FK_UsuCons_Usuarios   FOREIGN KEY (UsuarioId)   REFERENCES dbo.Usuarios (Id) ON DELETE CASCADE,
    CONSTRAINT FK_UsuCons_Consorcios FOREIGN KEY (ConsorcioId) REFERENCES dbo.Consorcios (Id)
);

CREATE INDEX IX_UsuariosConsorcios_ConsorcioId ON dbo.UsuariosConsorcios (ConsorcioId);

-- Conserva las asignaciones actuales.
INSERT INTO dbo.UsuariosConsorcios (UsuarioId, ConsorcioId)
SELECT Id, ConsorcioId FROM dbo.Usuarios WHERE ConsorcioId IS NOT NULL;

DROP INDEX IF EXISTS dbo.IX_Usuarios_ConsorcioId;
ALTER TABLE dbo.Usuarios DROP CONSTRAINT IF EXISTS CK_Usuarios_ConsorcioSoloAdmin;
ALTER TABLE dbo.Usuarios DROP CONSTRAINT IF EXISTS FK_Usuarios_Consorcios;
ALTER TABLE dbo.Usuarios DROP COLUMN ConsorcioId;

-- Control: debe coincidir con la cantidad de administradores que tenian consorcio.
SELECT COUNT(*) AS asignaciones FROM dbo.UsuariosConsorcios;

COMMIT;
