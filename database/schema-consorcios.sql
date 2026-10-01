
/* =====================================================================
   Sistema de Consorcios - Esquema de base de datos (PostgreSQL adaptado)
   Generado a partir del diagrama de clases del modelo de dominio.

   Convenciones:
   - UUID            -> UUID con gen_random_uuid() (requiere extensión pgcrypto)
   - BigDecimal      -> DECIMAL(18,2) para importes, DECIMAL(9,6) para coeficientes
   - Enumeraciones   -> VARCHAR + CHECK constraint
   - LocalDate       -> DATE, LocalDateTime -> TIMESTAMP (UTC)
   ===================================================================== */

-- Activar extensión para UUID
CREATE EXTENSION IF NOT EXISTS "pgcrypto";

-- Crear esquema dbo (para compatibilidad con SQL Server)
CREATE SCHEMA IF NOT EXISTS dbo;

-- ---------------------------------------------------------------------
-- Personas
-- ---------------------------------------------------------------------
CREATE TABLE dbo.Personas (
    Id          UUID NOT NULL DEFAULT gen_random_uuid(),
    Nombre      VARCHAR(100)    NOT NULL,
    Apellido    VARCHAR(100)    NOT NULL,
    Dni         VARCHAR(10)     NOT NULL,
    Email       VARCHAR(254),
    Telefono    VARCHAR(30),
    CONSTRAINT PK_Personas PRIMARY KEY (Id),
    CONSTRAINT UQ_Personas_Dni UNIQUE (Dni)
);

-- ---------------------------------------------------------------------
-- Consorcios
-- ---------------------------------------------------------------------
CREATE TABLE dbo.Consorcios (
    Id                  UUID NOT NULL DEFAULT gen_random_uuid(),
    Nombre              VARCHAR(150)    NOT NULL,
    Direccion           VARCHAR(250)    NOT NULL,
    TotalCoeficientes   DECIMAL(9,6)    NOT NULL DEFAULT 100,
    FechaAlta           DATE            NOT NULL DEFAULT CURRENT_DATE,
    CONSTRAINT PK_Consorcios PRIMARY KEY (Id),
    CONSTRAINT CK_Consorcios_TotalCoef CHECK (TotalCoeficientes = 100)
);

-- ---------------------------------------------------------------------
-- Usuarios
-- ---------------------------------------------------------------------
CREATE TABLE dbo.Usuarios (
    Id              UUID NOT NULL DEFAULT gen_random_uuid(),
    Email           VARCHAR(254)     NOT NULL,
    PasswordHash    VARCHAR(255)     NOT NULL,
    Rol             VARCHAR(30)      NOT NULL,
    Activo          BOOLEAN          NOT NULL DEFAULT TRUE,
    FechaAlta       TIMESTAMP        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PersonaId       UUID,
    ConsorcioId     UUID,
    CONSTRAINT PK_Usuarios PRIMARY KEY (Id),
    CONSTRAINT UQ_Usuarios_Email UNIQUE (Email),
    CONSTRAINT CK_Usuarios_Rol CHECK (Rol IN ('SUPERADMINISTRADOR','ADMINISTRADOR_CONSORCIO','PROPIETARIO','INQUILINO')),
    CONSTRAINT CK_Usuarios_ConsorcioSoloAdmin CHECK (ConsorcioId IS NULL OR Rol = 'ADMINISTRADOR_CONSORCIO'),
    CONSTRAINT FK_Usuarios_Personas   FOREIGN KEY (PersonaId)   REFERENCES dbo.Personas (Id),
    CONSTRAINT FK_Usuarios_Consorcios FOREIGN KEY (ConsorcioId) REFERENCES dbo.Consorcios (Id)
);

CREATE UNIQUE INDEX UX_Usuarios_PersonaId ON dbo.Usuarios (PersonaId) WHERE PersonaId IS NOT NULL;
CREATE INDEX IX_Usuarios_ConsorcioId ON dbo.Usuarios (ConsorcioId) WHERE ConsorcioId IS NOT NULL;

-- ---------------------------------------------------------------------
-- Unidades funcionales
-- ---------------------------------------------------------------------
CREATE TABLE dbo.UnidadesFuncionales (
    Id                  UUID NOT NULL DEFAULT gen_random_uuid(),
    ConsorcioId         UUID NOT NULL,
    Identificador       VARCHAR(20) NOT NULL,
    CoeficientePropiedad DECIMAL(9,6) NOT NULL,
    CONSTRAINT PK_UnidadesFuncionales PRIMARY KEY (Id),
    CONSTRAINT UQ_UF_Consorcio_Identificador UNIQUE (ConsorcioId, Identificador),
    CONSTRAINT UQ_UF_Id_Consorcio UNIQUE (Id, ConsorcioId),
    CONSTRAINT CK_UF_Coeficiente CHECK (CoeficientePropiedad > 0 AND CoeficientePropiedad <= 100),
    CONSTRAINT FK_UF_Consorcios FOREIGN KEY (ConsorcioId) REFERENCES dbo.Consorcios (Id)
);

-- Regla: si un consorcio tiene unidades, la suma de sus coeficientes debe ser
-- 100 (porcentuales del reglamento de copropiedad), con una tolerancia de
-- +/- 0.0001 por redondeo (ej.: 3 unidades de 33.333333 suman 99.999999).
-- Un CHECK solo ve una fila, por eso se usa un constraint
-- trigger diferido: se valida al COMMIT, así se pueden cargar todas las
-- unidades en una misma transacción. Un consorcio sin unidades es válido.
CREATE FUNCTION dbo.fn_validar_suma_coeficientes() RETURNS TRIGGER AS $$
DECLARE
    v_consorcio UUID;
    v_suma      DECIMAL(18,6);
BEGIN
    FOR v_consorcio IN
        SELECT DISTINCT c FROM (VALUES (OLD.ConsorcioId), (NEW.ConsorcioId)) AS t(c) WHERE c IS NOT NULL
    LOOP
        SELECT SUM(CoeficientePropiedad) INTO v_suma
        FROM dbo.UnidadesFuncionales WHERE ConsorcioId = v_consorcio;

        IF v_suma IS NOT NULL AND ABS(v_suma - 100) > 0.0001 THEN
            RAISE EXCEPTION 'La suma de coeficientes del consorcio % es %, debe ser 100 (tolerancia 0.0001)', v_consorcio, v_suma
                USING ERRCODE = 'check_violation';
        END IF;
    END LOOP;
    RETURN NULL;
END;
$$ LANGUAGE plpgsql;

CREATE CONSTRAINT TRIGGER TR_UF_SumaCoeficientes
    AFTER INSERT OR UPDATE OR DELETE ON dbo.UnidadesFuncionales
    DEFERRABLE INITIALLY DEFERRED
    FOR EACH ROW EXECUTE FUNCTION dbo.fn_validar_suma_coeficientes();

-- ---------------------------------------------------------------------
-- Vinculaciones persona <-> unidad
-- ---------------------------------------------------------------------
CREATE TABLE dbo.VinculacionesUnidad (
    Id              UUID NOT NULL DEFAULT gen_random_uuid(),
    UnidadFuncionalId UUID NOT NULL,
    PersonaId       UUID NOT NULL,
    Tipo            VARCHAR(15) NOT NULL,
    FechaInicio     DATE NOT NULL,
    FechaFin        DATE,
    CONSTRAINT PK_VinculacionesUnidad PRIMARY KEY (Id),
    CONSTRAINT CK_Vinc_Tipo CHECK (Tipo IN ('PROPIETARIO','INQUILINO')),
    CONSTRAINT CK_Vinc_Fechas CHECK (FechaFin IS NULL OR FechaFin >= FechaInicio),
    CONSTRAINT FK_Vinc_UF FOREIGN KEY (UnidadFuncionalId) REFERENCES dbo.UnidadesFuncionales (Id) ON DELETE CASCADE,
    CONSTRAINT FK_Vinc_Personas FOREIGN KEY (PersonaId) REFERENCES dbo.Personas (Id)
);

CREATE INDEX IX_Vinc_UF ON dbo.VinculacionesUnidad (UnidadFuncionalId, Tipo);
CREATE INDEX IX_Vinc_Persona ON dbo.VinculacionesUnidad (PersonaId);

-- ---------------------------------------------------------------------
-- Gastos
-- ---------------------------------------------------------------------
CREATE TABLE dbo.Gastos (
    Id          UUID NOT NULL DEFAULT gen_random_uuid(),
    ConsorcioId UUID NOT NULL,
    Descripcion VARCHAR(300) NOT NULL,
    Monto       DECIMAL(18,2) NOT NULL,
    Tipo        VARCHAR(15) NOT NULL,
    Fecha       DATE NOT NULL,   -- fecha de imputación: su mes (YYYY-MM) define la liquidación en que entra
    Comprobante VARCHAR(500),
    CONSTRAINT PK_Gastos PRIMARY KEY (Id),
    CONSTRAINT CK_Gastos_Tipo CHECK (Tipo IN ('ORDINARIO','EXTRAORDINARIO')),
    CONSTRAINT CK_Gastos_Monto CHECK (Monto > 0),
    CONSTRAINT FK_Gastos_Consorcios FOREIGN KEY (ConsorcioId) REFERENCES dbo.Consorcios (Id)
);

CREATE INDEX IX_Gastos_Consorcio_Fecha ON dbo.Gastos (ConsorcioId, Fecha DESC);

-- ---------------------------------------------------------------------
-- Liquidaciones
-- ---------------------------------------------------------------------
CREATE TABLE dbo.Liquidaciones (
    Id              UUID NOT NULL DEFAULT gen_random_uuid(),
    ConsorcioId     UUID NOT NULL,
    Periodo         CHAR(7) NOT NULL,   -- 'YYYY-MM'; toma los gastos del consorcio con Gastos.Fecha en ese mes
    FechaGeneracion DATE NOT NULL DEFAULT CURRENT_DATE,
    Estado          VARCHAR(10) NOT NULL DEFAULT 'ABIERTA',
    CONSTRAINT PK_Liquidaciones PRIMARY KEY (Id),
    CONSTRAINT UQ_Liq_Consorcio_Periodo UNIQUE (ConsorcioId, Periodo),
    CONSTRAINT UQ_Liq_Id_Consorcio UNIQUE (Id, ConsorcioId),
    CONSTRAINT CK_Liq_Estado CHECK (Estado IN ('ABIERTA','CERRADA')),
    CONSTRAINT FK_Liq_Consorcios FOREIGN KEY (ConsorcioId) REFERENCES dbo.Consorcios (Id)
);

-- ---------------------------------------------------------------------
-- LiquidacionesUnidad
-- ---------------------------------------------------------------------
CREATE TABLE dbo.LiquidacionesUnidad (
    Id              UUID NOT NULL DEFAULT gen_random_uuid(),
    ConsorcioId     UUID NOT NULL,
    LiquidacionId   UUID NOT NULL,
    UnidadFuncionalId UUID NOT NULL,
    Importe         DECIMAL(18,2) NOT NULL,
    EstadoPago      VARCHAR(10) NOT NULL DEFAULT 'PENDIENTE',
    FechaPago       DATE,
    VisibleParaInquilino BOOLEAN NOT NULL DEFAULT FALSE,
    CONSTRAINT PK_LiquidacionesUnidad PRIMARY KEY (Id),
    CONSTRAINT UQ_LiqUF_Liquidacion_Unidad UNIQUE (LiquidacionId, UnidadFuncionalId),
    CONSTRAINT CK_LiqUF_Importe CHECK (Importe >= 0),
    CONSTRAINT CK_LiqUF_EstadoPago CHECK (EstadoPago IN ('PENDIENTE','PAGADO')),
    CONSTRAINT FK_LiqUF_Liquidaciones FOREIGN KEY (LiquidacionId, ConsorcioId) REFERENCES dbo.Liquidaciones (Id, ConsorcioId),
    CONSTRAINT FK_LiqUF_UF FOREIGN KEY (UnidadFuncionalId, ConsorcioId) REFERENCES dbo.UnidadesFuncionales (Id, ConsorcioId)
);

CREATE INDEX IX_LiqUF_UF_Estado ON dbo.LiquidacionesUnidad (UnidadFuncionalId, EstadoPago);
CREATE INDEX IX_LiqUF_Pendientes ON dbo.LiquidacionesUnidad (ConsorcioId) WHERE EstadoPago = 'PENDIENTE';
