-- user microservice -------------------------------------------------------------------
CREATE TABLE public.users
(
    id            bigserial             NOT NULL,
    payload_id    varchar NULL,
    username      varchar NULL,
    email         varchar NULL,
    password_hash varchar NULL,
    salt          varchar NULL,
    "role"        varchar NULL,
    verified      boolean DEFAULT false NOT NULL,
    "locked"      boolean DEFAULT false NOT NULL,
    lock_until    timestamp NULL,
    created_at    timestamp             NOT NULL,
    created_by    varchar               NOT NULL,
    updated_at    timestamp             NOT NULL,
    updated_by    varchar               NOT NULL,
    CONSTRAINT users_pk PRIMARY KEY (id)
);

-- product microservice -------------------------------------------------------------------
-- product
CREATE TABLE public.products
(
    id                bigserial                   NOT NULL,
    user_id           bigserial                   NOT NULL,
    payload_id        varchar                     NOT NULL,
    "name"            varchar                     NOT NULL,
    description       text NULL,
    price             numeric(10, 2) DEFAULT 0.00 NOT NULL,
    price_id          varchar NULL,
    stripe_id         varchar NULL,
    category          varchar NULL,                                                 -- ui_kits | icons
    product_file_url  varchar                     NOT NULL,
    approved_for_sale varchar        DEFAULT 'pending'::character varying NOT NULL, -- pending | approved | denied
    created_at        timestamp                   NOT NULL,
    created_by        varchar                     NOT NULL,
    updated_at        timestamp                   NOT NULL,
    updated_by        varchar                     NOT NULL,
    CONSTRAINT products_pk PRIMARY KEY (id)
);

-- Column comments

COMMENT
ON COLUMN public.products.category IS 'ui_kits | icons';
COMMENT
ON COLUMN public.products.approved_for_sale IS 'pending | approved | denied';

-- public.products foreign keys
ALTER TABLE public.products
    ADD CONSTRAINT products_users_fk FOREIGN KEY (user_id) REFERENCES public.users (id);


-- product microservice - product_images -------------------------------------------------------------------
CREATE TABLE public.product_images
(
    id         bigserial NOT NULL,
    product_id bigserial NOT NULL,
    payload_id varchar   NOT NULL,
    url        varchar   NOT NULL,
    filename   varchar NULL,
    filesize   numeric(10, 1) NULL, -- calculated in KB
    height     numeric NULL,        -- pixels
    width      numeric NULL,        -- -1 means no limit
    mime_type  varchar NULL,        -- image/*
    file_type  varchar NULL,        -- thumbnail, card, tablet
    created_at timestamp NOT NULL,
    created_by varchar   NOT NULL,
    updated_at timestamp NOT NULL,
    updated_by varchar   NOT NULL,
    CONSTRAINT product_images_pk PRIMARY KEY (id)
);

-- Column comments

COMMENT
ON COLUMN public.product_images.filesize IS 'calculated in KB';
COMMENT
ON COLUMN public.product_images.height IS 'pixels';
COMMENT
ON COLUMN public.product_images.width IS '-1 means no limit';
COMMENT
ON COLUMN public.product_images.mime_type IS 'image/*';
COMMENT
ON COLUMN public.product_images.file_type IS 'thumbnail, card, tablet';


-- public.product_images foreign keys

ALTER TABLE public.product_images
    ADD CONSTRAINT product_images_products_fk FOREIGN KEY (product_id) REFERENCES public.products (id);

-- Column comments

COMMENT
ON COLUMN public.product_images.filesize IS 'calculated in KB';
COMMENT
ON COLUMN public.product_images.height IS 'pixels';
COMMENT
ON COLUMN public.product_images.width IS '-1 means no limit';
COMMENT
ON COLUMN public.product_images.mime_type IS 'image/*';
COMMENT
ON COLUMN public.product_images.file_type IS 'thumbnail, card, tablet';

-- order microservice -------------------------------------------------------------------
CREATE TABLE public.orders
(
    id         bigserial NOT NULL,
    payload_id varchar NULL,
    user_id    bigserial NOT NULL,
    is_paid    bool DEFAULT false NULL,
    created_at timestamp NULL,
    created_by varchar NULL,
    updated_at timestamp NULL,
    updated_by varchar NULL,
    CONSTRAINT orders_pk PRIMARY KEY (id)
);

-- public.orders foreign keys

ALTER TABLE public.orders
    ADD CONSTRAINT orders_users_fk FOREIGN KEY (user_id) REFERENCES public.users (id);

-- public.link_orders_products definition
CREATE TABLE public.link_orders_products
(
    id         bigserial NOT NULL,
    product_id bigserial NOT NULL,
    order_id   bigserial NOT NULL,
    created_at timestamp NULL,
    created_by varchar NULL,
    updated_at timestamp NULL,
    updated_by varchar NULL,
    CONSTRAINT link_orders_products_pk PRIMARY KEY (id)
);

-- public.link_orders_products foreign keys

ALTER TABLE public.link_orders_products
    ADD CONSTRAINT link_orders_products_orders_fk FOREIGN KEY (order_id) REFERENCES public.orders (id);
ALTER TABLE public.link_orders_products
    ADD CONSTRAINT link_orders_products_products_fk FOREIGN KEY (product_id) REFERENCES public.products (id);


-- public.carts definition
CREATE TABLE public.carts
(
    id         bigserial NOT NULL,
    product_id bigserial NOT NULL,
    user_id    bigserial NOT NULL,
    created_at timestamp NOT NULL,
    created_by varchar   NOT NULL,
    updated_at timestamp NOT NULL,
    updated_by varchar   NOT NULL,
    CONSTRAINT carts_pk PRIMARY KEY (id)
);

-- public.carts foreign keys

ALTER TABLE public.carts
    ADD CONSTRAINT carts_products_fk FOREIGN KEY (product_id) REFERENCES public.products (id);
ALTER TABLE public.carts
    ADD CONSTRAINT carts_users_fk FOREIGN KEY (user_id) REFERENCES public.users (id);