-- ============================================================
-- AgroLink - Script Completo de Base de Datos PostgreSQL
-- Compatible con Render PostgreSQL
-- Incluye TODAS las tablas del sistema
-- ============================================================

-- ============================================================
-- ELIMINAR TABLAS EN ORDEN DE DEPENDENCIAS
-- ============================================================
DROP TABLE IF EXISTS movimiento_stock CASCADE;
DROP TABLE IF EXISTS detalle_pedido CASCADE;
DROP TABLE IF EXISTS historial_estado_pedido CASCADE;
DROP TABLE IF EXISTS pedido CASCADE;
DROP TABLE IF EXISTS evento_produccion CASCADE;
DROP TABLE IF EXISTS lote CASCADE;
DROP TABLE IF EXISTS cultivo CASCADE;
DROP TABLE IF EXISTS datos_contacto CASCADE;
DROP TABLE IF EXISTS usuario CASCADE;

-- ============================================================
-- ELIMINAR TIPOS ENUM SI EXISTEN
-- ============================================================
DROP TYPE IF EXISTS rol_enum CASCADE;
DROP TYPE IF EXISTS estado_validacion_enum CASCADE;
DROP TYPE IF EXISTS tipo_comprador_enum CASCADE;
DROP TYPE IF EXISTS estado_cultivo_enum CASCADE;
DROP TYPE IF EXISTS categoria_producto_enum CASCADE;
DROP TYPE IF EXISTS tipo_evento_enum CASCADE;
DROP TYPE IF EXISTS calidad_lote_enum CASCADE;
DROP TYPE IF EXISTS estado_lote_enum CASCADE;
DROP TYPE IF EXISTS estado_pedido_enum CASCADE;
DROP TYPE IF EXISTS estado_detalle_enum CASCADE;
DROP TYPE IF EXISTS tipo_movimiento_enum CASCADE;

-- ============================================================
-- TIPOS ENUM
-- ============================================================

DO $$ BEGIN
    CREATE TYPE rol_enum AS ENUM ('AGRICULTOR', 'COMPRADOR', 'ADMINISTRADOR');
EXCEPTION WHEN duplicate_object THEN NULL; END $$;

DO $$ BEGIN
    CREATE TYPE estado_validacion_enum AS ENUM ('PENDIENTE', 'APROBADO', 'OBSERVADO', 'RECHAZADO');
EXCEPTION WHEN duplicate_object THEN NULL; END $$;

DO $$ BEGIN
    CREATE TYPE tipo_comprador_enum AS ENUM ('MAYORISTA', 'RESTAURANTE', 'MERCADO', 'OTRO');
EXCEPTION WHEN duplicate_object THEN NULL; END $$;

DO $$ BEGIN
    CREATE TYPE estado_cultivo_enum AS ENUM ('SEMBRADO', 'CRECIMIENTO', 'COSECHA', 'FINALIZADO', 'PERDIDA');
EXCEPTION WHEN duplicate_object THEN NULL; END $$;

DO $$ BEGIN
    CREATE TYPE categoria_producto_enum AS ENUM ('FRUTAS', 'VERDURAS', 'TUBERCULOS', 'CEREALES', 'LEGUMBRES', 'HORTALIZAS', 'OTROS');
EXCEPTION WHEN duplicate_object THEN NULL; END $$;

DO $$ BEGIN
    CREATE TYPE tipo_evento_enum AS ENUM ('SIEMBRA', 'FERTILIZACION', 'RIEGO', 'CONTROL_PLAGAS', 'COSECHA', 'PERDIDA_PARCIAL');
EXCEPTION WHEN duplicate_object THEN NULL; END $$;

DO $$ BEGIN
    CREATE TYPE calidad_lote_enum AS ENUM ('PRIMERA', 'SEGUNDA', 'TERCERA');
EXCEPTION WHEN duplicate_object THEN NULL; END $$;

DO $$ BEGIN
    CREATE TYPE estado_lote_enum AS ENUM ('ACTIVO', 'INACTIVO', 'AGOTADO');
EXCEPTION WHEN duplicate_object THEN NULL; END $$;

DO $$ BEGIN
    CREATE TYPE estado_pedido_enum AS ENUM ('PENDIENTE', 'CONFIRMADO', 'DESPACHADO', 'ENTREGADO', 'CANCELADO');
EXCEPTION WHEN duplicate_object THEN NULL; END $$;

DO $$ BEGIN
    CREATE TYPE estado_detalle_enum AS ENUM ('PENDIENTE', 'CONFIRMADO', 'CANCELADO');
EXCEPTION WHEN duplicate_object THEN NULL; END $$;

DO $$ BEGIN
    CREATE TYPE tipo_movimiento_enum AS ENUM ('ENTRADA', 'SALIDA', 'AJUSTE');
EXCEPTION WHEN duplicate_object THEN NULL; END $$;

-- ============================================================
-- TABLA: usuario
-- RF01 - Gestión de usuarios y roles
-- RF13 - Registro de agricultores
-- RF14 - Registro de compradores
-- RF15 - Validación de cuenta de agricultor
-- ============================================================
CREATE TABLE IF NOT EXISTS usuario (
    id                      BIGSERIAL PRIMARY KEY,

    -- Datos básicos
    nombre                  VARCHAR(100)  NOT NULL,
    apellido                VARCHAR(100)  NOT NULL,
    email                   VARCHAR(150)  NOT NULL UNIQUE,
    password                VARCHAR(255)  NOT NULL,   -- BCrypt (RNF03)
    telefono                VARCHAR(20),
    rol                     VARCHAR(20)   NOT NULL,   -- AGRICULTOR | COMPRADOR | ADMINISTRADOR

    -- Datos específicos AGRICULTOR (RF13)
    dni                     VARCHAR(20),
    region                  VARCHAR(100),
    productores_principales TEXT,
    descripcion_finca       TEXT,

    -- Validación agricultor (RF15)
    estado_validacion       VARCHAR(20),              -- PENDIENTE | APROBADO | OBSERVADO | RECHAZADO
    motivo_observacion      TEXT,

    -- Datos específicos COMPRADOR (RF14)
    ruc                     VARCHAR(20),
    razon_social            VARCHAR(200),
    direccion_comercial     VARCHAR(300),
    tipo_comprador          VARCHAR(20),              -- MAYORISTA | RESTAURANTE | MERCADO | OTRO

    -- Control
    activo                  BOOLEAN       NOT NULL DEFAULT TRUE,
    fecha_registro          TIMESTAMP     NOT NULL DEFAULT NOW(),

    CONSTRAINT chk_rol CHECK (rol IN ('AGRICULTOR', 'COMPRADOR', 'ADMINISTRADOR')),
    CONSTRAINT chk_estado_validacion CHECK (
        estado_validacion IS NULL OR
        estado_validacion IN ('PENDIENTE', 'APROBADO', 'OBSERVADO', 'RECHAZADO')
    ),
    CONSTRAINT chk_tipo_comprador CHECK (
        tipo_comprador IS NULL OR
        tipo_comprador IN ('MAYORISTA', 'RESTAURANTE', 'MERCADO', 'OTRO')
    )
);

COMMENT ON TABLE  usuario                          IS 'RF01, RF13, RF14, RF15 - Usuarios del sistema con roles diferenciados';
COMMENT ON COLUMN usuario.password                 IS 'Hash BCrypt - RNF03';
COMMENT ON COLUMN usuario.estado_validacion        IS 'RF15 - Solo aplica para AGRICULTOR';
COMMENT ON COLUMN usuario.activo                   IS 'RF01 - Activar/desactivar cuenta';

-- ============================================================
-- TABLA: datos_contacto
-- Información de contacto extendida por usuario
-- ============================================================
CREATE TABLE IF NOT EXISTS datos_contacto (
    id                   BIGSERIAL PRIMARY KEY,
    usuario_id           BIGINT        REFERENCES usuario(id) ON DELETE CASCADE,
    direccion            VARCHAR(300),
    referencia           VARCHAR(300),
    email_contacto       VARCHAR(150),
    telefono_adicional   VARCHAR(20)
);

COMMENT ON TABLE datos_contacto IS 'Datos de contacto adicionales por usuario';

-- ============================================================
-- TABLA: cultivo
-- RF02 - Registro de cultivos
-- RF03 - Seguimiento de producción
-- RF16 - Gestión de lotes agrícolas
-- ============================================================
CREATE TABLE IF NOT EXISTS cultivo (
    id                          BIGSERIAL PRIMARY KEY,

    -- RF02 - Datos del cultivo
    agricultor_id               BIGINT        NOT NULL REFERENCES usuario(id) ON DELETE CASCADE,
    nombre_producto             VARCHAR(150)  NOT NULL,
    variedad                    VARCHAR(100),
    categoria                   VARCHAR(30),
    descripcion                 TEXT,
    fecha_siembra               DATE,
    fecha_cosecha_estimada      DATE,

    -- RF16 - Lote agrícola
    nombre_lote                 VARCHAR(100),
    area_ha                     DECIMAL(10,4),
    ubicacion                   VARCHAR(300),

    -- RF03 - Seguimiento de producción
    estado                      VARCHAR(20)   NOT NULL DEFAULT 'SEMBRADO',
    etapa_productiva            VARCHAR(100),
    observacion_seguimiento     TEXT,
    fecha_ultimo_seguimiento    TIMESTAMP,

    -- Control
    fecha_creacion              TIMESTAMP     NOT NULL DEFAULT NOW(),

    CONSTRAINT chk_estado_cultivo CHECK (estado IN ('SEMBRADO', 'CRECIMIENTO', 'COSECHA', 'FINALIZADO', 'PERDIDA')),
    CONSTRAINT chk_categoria_cultivo CHECK (
        categoria IS NULL OR
        categoria IN ('FRUTAS', 'VERDURAS', 'TUBERCULOS', 'CEREALES', 'LEGUMBRES', 'HORTALIZAS', 'OTROS')
    )
);

COMMENT ON TABLE  cultivo             IS 'RF02, RF03, RF16 - Cultivos registrados por agricultores';
COMMENT ON COLUMN cultivo.nombre_lote IS 'RF16 - Nombre del lote agrícola';
COMMENT ON COLUMN cultivo.area_ha     IS 'RF16 - Área en hectáreas';
COMMENT ON COLUMN cultivo.estado      IS 'RF03 - SEMBRADO | CRECIMIENTO | COSECHA | FINALIZADO | PERDIDA';

-- ============================================================
-- TABLA: evento_produccion
-- RF17 - Gestión de eventos de producción
-- ============================================================
CREATE TABLE IF NOT EXISTS evento_produccion (
    id                      BIGSERIAL PRIMARY KEY,
    cultivo_id              BIGINT        NOT NULL REFERENCES cultivo(id) ON DELETE CASCADE,
    tipo                    VARCHAR(30)   NOT NULL,
    descripcion             TEXT,
    impacto_estimado_pct    DECIMAL(5,2),
    fecha                   TIMESTAMP     NOT NULL DEFAULT NOW(),

    CONSTRAINT chk_tipo_evento CHECK (
        tipo IN ('SIEMBRA', 'FERTILIZACION', 'RIEGO', 'CONTROL_PLAGAS', 'COSECHA', 'PERDIDA_PARCIAL')
    )
);

COMMENT ON TABLE evento_produccion IS 'RF17 - Eventos del ciclo de producción de un cultivo';

-- ============================================================
-- TABLA: lote
-- Lotes de producto listos para comercializar
-- ============================================================
CREATE TABLE IF NOT EXISTS lote (
    id                   BIGSERIAL PRIMARY KEY,
    cultivo_id           BIGINT           REFERENCES cultivo(id) ON DELETE SET NULL,
    cantidad_kg          DECIMAL(12,3),
    calidad              VARCHAR(20),
    precio_unitario      DECIMAL(12,2),
    unidad_medida        VARCHAR(30),
    stock_disponible     DECIMAL(12,3),
    fecha_cosecha        DATE,
    condiciones_entrega  TEXT,
    publicado            BOOLEAN          NOT NULL DEFAULT FALSE,
    fecha_publicacion    TIMESTAMP,

    CONSTRAINT chk_calidad_lote CHECK (
        calidad IS NULL OR
        calidad IN ('PRIMERA', 'SEGUNDA', 'TERCERA')
    )
);

COMMENT ON TABLE  lote            IS 'Lotes de producción disponibles para venta';
COMMENT ON COLUMN lote.publicado  IS 'TRUE cuando el lote está visible en el catálogo';

-- ============================================================
-- TABLA: movimiento_stock
-- Trazabilidad de entradas/salidas/ajustes de stock por lote
-- ============================================================
CREATE TABLE IF NOT EXISTS movimiento_stock (
    id               BIGSERIAL PRIMARY KEY,
    lote_id          BIGINT        REFERENCES lote(id) ON DELETE CASCADE,
    tipo             VARCHAR(20)   NOT NULL,
    cantidad         DECIMAL(12,3),
    motivo           VARCHAR(300),
    fecha_movimiento TIMESTAMP     NOT NULL DEFAULT NOW(),

    CONSTRAINT chk_tipo_movimiento CHECK (
        tipo IN ('ENTRADA', 'SALIDA', 'AJUSTE')
    )
);

COMMENT ON TABLE movimiento_stock IS 'Trazabilidad de cambios de stock por lote';

-- ============================================================
-- TABLA: pedido
-- Pedidos realizados por compradores
-- ============================================================
CREATE TABLE IF NOT EXISTS pedido (
    id                      BIGSERIAL PRIMARY KEY,
    comprador_id            BIGINT          REFERENCES usuario(id) ON DELETE SET NULL,
    estado                  VARCHAR(20)     NOT NULL DEFAULT 'PENDIENTE',
    notas_especiales        TEXT,
    fecha_pedido            TIMESTAMP       NOT NULL DEFAULT NOW(),
    fecha_entrega_estimada  DATE,
    total_estimado          DECIMAL(14,2),

    CONSTRAINT chk_estado_pedido CHECK (
        estado IN ('PENDIENTE', 'CONFIRMADO', 'PREPARADO', 'DESPACHADO', 'ENTREGADO', 'CANCELADO', 'RECHAZADO')
    )
);

COMMENT ON TABLE pedido IS 'Pedidos realizados por compradores';

-- ============================================================
-- TABLA: detalle_pedido
-- Líneas de un pedido (uno por lote solicitado)
-- ============================================================
CREATE TABLE IF NOT EXISTS detalle_pedido (
    id                  BIGSERIAL PRIMARY KEY,
    pedido_id           BIGINT          REFERENCES pedido(id) ON DELETE CASCADE,
    lote_id             BIGINT          REFERENCES lote(id) ON DELETE SET NULL,
    agricultor_id       BIGINT          REFERENCES usuario(id) ON DELETE SET NULL,
    cantidad_solicitada DECIMAL(12,3),
    precio_unitario     DECIMAL(12,2),
    subtotal            DECIMAL(14,2),
    estado_detalle      VARCHAR(20)     NOT NULL DEFAULT 'PENDIENTE',

    CONSTRAINT chk_estado_detalle CHECK (
        estado_detalle IN ('PENDIENTE', 'CONFIRMADO', 'CANCELADO')
    )
);

COMMENT ON TABLE detalle_pedido IS 'Líneas de pedido asociadas a un lote específico';

-- ============================================================
-- TABLA: historial_estado_pedido
-- Auditoría de cambios de estado de cada pedido
-- ============================================================
CREATE TABLE IF NOT EXISTS historial_estado_pedido (
    id              BIGSERIAL PRIMARY KEY,
    pedido_id       BIGINT      REFERENCES pedido(id) ON DELETE CASCADE,
    estado_anterior VARCHAR(20),
    estado_nuevo    VARCHAR(20) NOT NULL,
    observacion     TEXT,
    fecha_cambio    TIMESTAMP   NOT NULL DEFAULT NOW(),

    CONSTRAINT chk_estado_anterior_hist CHECK (
        estado_anterior IS NULL OR
        estado_anterior IN ('PENDIENTE', 'CONFIRMADO', 'PREPARADO', 'DESPACHADO', 'ENTREGADO', 'CANCELADO', 'RECHAZADO')
    ),
    CONSTRAINT chk_estado_nuevo_hist CHECK (
        estado_nuevo IN ('PENDIENTE', 'CONFIRMADO', 'PREPARADO', 'DESPACHADO', 'ENTREGADO', 'CANCELADO', 'RECHAZADO')
    )
);

COMMENT ON TABLE historial_estado_pedido IS 'Auditoría de cambios de estado de pedidos';

-- ============================================================
-- ÍNDICES (RNF02 - tiempo de respuesta < 2s)
-- ============================================================

-- usuario
CREATE INDEX IF NOT EXISTS idx_usuario_email            ON usuario(email);
CREATE INDEX IF NOT EXISTS idx_usuario_rol              ON usuario(rol);
CREATE INDEX IF NOT EXISTS idx_usuario_activo           ON usuario(activo);
CREATE INDEX IF NOT EXISTS idx_usuario_estado_val       ON usuario(estado_validacion) WHERE estado_validacion IS NOT NULL;

-- cultivo
CREATE INDEX IF NOT EXISTS idx_cultivo_agricultor       ON cultivo(agricultor_id);
CREATE INDEX IF NOT EXISTS idx_cultivo_estado           ON cultivo(estado);
CREATE INDEX IF NOT EXISTS idx_cultivo_categoria        ON cultivo(categoria);

-- evento_produccion
CREATE INDEX IF NOT EXISTS idx_evento_cultivo           ON evento_produccion(cultivo_id);
CREATE INDEX IF NOT EXISTS idx_evento_tipo              ON evento_produccion(tipo);

-- lote
CREATE INDEX IF NOT EXISTS idx_lote_cultivo             ON lote(cultivo_id);
CREATE INDEX IF NOT EXISTS idx_lote_publicado           ON lote(publicado) WHERE publicado = TRUE;

-- movimiento_stock
CREATE INDEX IF NOT EXISTS idx_movstock_lote            ON movimiento_stock(lote_id);

-- pedido
CREATE INDEX IF NOT EXISTS idx_pedido_comprador         ON pedido(comprador_id);
CREATE INDEX IF NOT EXISTS idx_pedido_estado            ON pedido(estado);

-- detalle_pedido
CREATE INDEX IF NOT EXISTS idx_detalle_pedido           ON detalle_pedido(pedido_id);
CREATE INDEX IF NOT EXISTS idx_detalle_lote             ON detalle_pedido(lote_id);

-- historial_estado_pedido
CREATE INDEX IF NOT EXISTS idx_historial_pedido         ON historial_estado_pedido(pedido_id);

-- ============================================================
-- DATOS INICIALES (seed)
-- Password de todos los usuarios de prueba: Admin123!
-- (hash BCrypt strength 10)
-- ============================================================

-- Administrador por defecto
INSERT INTO usuario (nombre, apellido, email, password, rol, activo)
VALUES (
    'Admin',
    'AgroLink',
    'admin@agrolink.com',
    '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi',
    'ADMINISTRADOR',
    true
) ON CONFLICT (email) DO NOTHING;

-- Agricultor de prueba (estado PENDIENTE - requiere validación de admin)
INSERT INTO usuario (nombre, apellido, email, password, rol, dni, region, productores_principales, estado_validacion, activo)
VALUES (
    'Juan',
    'Huanca',
    'agricultor@agrolink.com',
    '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi',
    'AGRICULTOR',
    '12345678',
    'Cajamarca',
    'Papa, maíz, trigo',
    'PENDIENTE',
    false
) ON CONFLICT (email) DO NOTHING;

-- Agricultor aprobado (listo para usar)
INSERT INTO usuario (nombre, apellido, email, password, rol, dni, region, productores_principales, descripcion_finca, estado_validacion, activo)
VALUES (
    'Pedro',
    'Quispe',
    'agricultor2@agrolink.com',
    '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi',
    'AGRICULTOR',
    '87654321',
    'Junín',
    'Maíz morado, quinua',
    'Finca de 5 hectáreas en las alturas de Junín',
    'APROBADO',
    true
) ON CONFLICT (email) DO NOTHING;

-- Comprador de prueba (activo automáticamente)
INSERT INTO usuario (nombre, apellido, email, password, rol, ruc, razon_social, tipo_comprador, activo)
VALUES (
    'María',
    'Torres',
    'comprador@agrolink.com',
    '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi',
    'COMPRADOR',
    '20123456789',
    'Mercado Central SAC',
    'MERCADO',
    true
) ON CONFLICT (email) DO NOTHING;