-- ============================================================================
-- GOLDEN REFERENCE: KALTURA VIDEO PLATFORM CONNECTOR - COMPLETE DML SETUP
-- ============================================================================
-- PURPOSE: This script creates the full admin UI tile, configuration fields,
--          entity definitions, and event routing for the Kaltura connector.
--          Use this as the TEMPLATE for any new connector's DML setup.
--
-- AUTH TYPE: Session Token (KS)
-- ENTITY TYPES: Content (KALTURA_MEDIA → EdCast Course)
--
-- ## NAMING CONVENTIONS ##
-- - Partner ID:        mptnr + 32-char hex  (one per vendor company)
-- - Integration ID:    integ + 32-char hex  (one per connector)
-- - Account Config:    accnf + 32-char hex  (one per UI field)
-- - Group:             group + 32-char hex  (logical grouping of fields)
-- - Vendor Entity:     mpent + 32-char hex  (one per entity type)
-- - Entity Assoc:      minea + 32-char hex  (source↔target entity mapping)
-- - Entity Config:     entcn + 32-char hex  (sync settings per entity)
-- - Event Info:        evnif + 32-char hex  (event routing rules)
-- - Channel Event:     event + 32-char hex  (channel event bindings)
-- - Topic:             mesgt + 32-char hex  (Pulsar topic for async processing)
-- - Subscription:      msbs  + 32-char hex  (topic subscription)
-- - Routing Rule:      rrule + 32-char hex  (event→topic routing)
--
-- ## ID CROSS-REFERENCE ##
-- PARTNER ID          : mptnrb2afc27ccf0a362f4a15d80cb26185ec
-- INTEGRATION ID      : intega00fd710ab80679960abd9db1a70a885
--   → This MUST match VendorConstants.KALTURA in Java code
-- INTEGRATION CONF ID : intcf6dddfd963e8325fc27d93b822832816a
-- ============================================================================

-- ─── SECTION 1: PARTNER (Vendor Company) ─────────────────────────────────────
-- Creates the vendor company record. One per vendor. Reuse if vendor already exists.
INSERT INTO sih.mpt_partner (id, created_by, created_date, last_modified_by, last_modified_date,
    contact_no, email_id, code, url, address_id, is_external_partner)
VALUES ('mptnrb2afc27ccf0a362f4a15d80cb26185ec', 'system', NOW(), 'system', NOW(),
    '+1-800-KALTURA', 'support@kaltura.com', 'KALTURA', 'https://kaltura.com',
    'addrs1574667801145ed4bda0d01654056890', TRUE)
ON CONFLICT (id) DO NOTHING;

INSERT INTO mpt_partner_i18n (partner_id, locale, is_inherited, name, description)
VALUES ('mptnrb2afc27ccf0a362f4a15d80cb26185ec',
    'local1574367832571d27e649a058ea000001', true,
    'Kaltura',
    'Kaltura is a leading video platform providing live and on-demand video SaaS solutions.')
ON CONFLICT (partner_id, locale) DO NOTHING;

-- ─── SECTION 2: INTEGRATION TILE ─────────────────────────────────────────────
-- Creates the connector tile in the Nova admin UI.
-- The 'code' parameter ('KALTURA') MUST match VendorConstants.KALTURA in Java.
CALL mpp_integration_ins(
    'intega00fd710ab80679960abd9db1a70a885',  -- integration_id (MUST match VendorConstants)
    'KALTURA',                                  -- code (MUST match VendorConstants)
    '1',                                        -- sequence
    '1',                                        -- priority
    'mptnrb2afc27ccf0a362f4a15d80cb26185ec',   -- partner_id (from Section 1)
    '0',                                        -- account_limit
    'PUBLISHED',                                -- status
    '1',                                        -- is_active
    'categ1575551823349d39999aa02758044340',    -- category_id (standard)
    'Kaltura Video Connector allows you to import video assets...',  -- config_text (help text)
    'Sync Video Content from Kaltura',          -- marketing_text (tile subtitle)
    NULL,                                       -- custom_text
    'Kaltura Video Platform'                    -- display_name (tile title)
);

-- Tile logo image
CALL frp_images_ins(
    'image36ebfb3ff0ac0587151ef32151e209e8',
    '#sihAsset#dashboard/connector/kaltura_logo.png',   -- image path (must exist)
    'intega00fd710ab80679960abd9db1a70a885',            -- integration_id
    'Vendor Image'                                       -- image type
);

-- ─── SECTION 3: INTEGRATION CAPABILITIES ─────────────────────────────────────
-- Defines what features the connector supports (import, export, scheduling, etc.)
CALL mpp_integration_conf_ins(
    'intcf6dddfd963e8325fc27d93b822832816a',
    false, true, false, true, false, false, false, false,
    true, true, true, true, true, true, true, false, true,
    'intega00fd710ab80679960abd9db1a70a885',
    false, false, false
);
-- Standard capability attributes
CALL mpp_integration_conf_attributes_ins('intcf6dddfd963e8325fc27d93b822832816a', 'NO_OF_ACCOUNT', '1');
CALL mpp_integration_conf_attributes_ins('intcf6dddfd963e8325fc27d93b822832816a', 'HAS_SUBSCRIPTION', 'false');
CALL mpp_integration_conf_attributes_ins('intcf6dddfd963e8325fc27d93b822832816a', 'NO_OF_IMPORT_MAPPING', '-1');
CALL mpp_integration_conf_attributes_ins('intcf6dddfd963e8325fc27d93b822832816a', 'NO_OF_IMPORT_ENTITY', '-1');
CALL mpp_integration_conf_attributes_ins('intcf6dddfd963e8325fc27d93b822832816a', 'CAN_DELETE_IMPORT_ENTITY', 'false');

-- Product association (links to EdCast product)
CALL mpp_product_integration_assoc_ins(
    'prinaa107f7e4c78f92bdd362cb09bc13de7f',
    'intega00fd710ab80679960abd9db1a70a885',
    'prodc157550450499257d9f21701654060189'
);

-- ─── SECTION 4: ACCOUNT CONFIGURATION (UI Fields) ────────────────────────────
-- Each mpp_vendor_account_config_ins creates one field in the admin UI.
-- The 'code' parameter MUST match the key in accountConfigs[] used in Content.js.

-- Account Configuration Group
CALL mpp_group_ins('groupcc917b7b85d28cc3f891a85a0fffca3d',
    'KALTURA_ACCOUNT_GROUP', '1', '3',
    'intega00fd710ab80679960abd9db1a70a885',
    'Account Configuration', NULL);

-- Field: Partner ID (required, text input)
CALL mpp_vendor_account_config_ins('accnf3b36b11ffae4daa814b6822deda9e157',
    'PARTNER_ID',       -- code (matches accountConfigs['PARTNER_ID'] in Content.js)
    NULL,               -- default_value
    '1',                -- sequence (display order)
    '1',                -- is_mandatory
    '1',                -- is_visible
    '0',                -- is_disabled
    '0',                -- is_multi_value
    'datat157598481420466e770020d2a8041930',    -- data_type (string)
    'groupcc917b7b85d28cc3f891a85a0fffca3d',   -- group_id
    'intega00fd710ab80679960abd9db1a70a885',    -- integration_id
    NULL, NULL,
    'Partner ID',       -- display_name
    'Your Kaltura Account ID.'  -- description/tooltip
);

-- Field: Secret Key (required, encrypted)
CALL mpp_vendor_account_config_ins('accnf81b3951a950a7c0b8fd36ab421e58808',
    'SECRET', NULL, '2', '1', '1', '0', '0',
    'datat157911427419485ccc4960a4ff047a60',    -- data_type (password)
    'groupcc917b7b85d28cc3f891a85a0fffca3d',
    'intega00fd710ab80679960abd9db1a70a885',
    NULL, NULL, 'Secret Key', 'The Secret key.'
);
-- Mark SECRET as encrypted (important for security!)
UPDATE mpt_vendor_account_config SET is_encrypted = true
WHERE integration_vendor_id = 'intega00fd710ab80679960abd9db1a70a885' AND code = 'SECRET';

-- Field: Service URL (optional, with default)
CALL mpp_vendor_account_config_ins('accnffd0fd78d8adea9acb5c86a43b3720e83',
    'SERVICE_URL', 'https://www.kaltura.com', '4', '0', '1', '0', '0',
    'datat157598481420466e770020d2a8041930',
    'groupcc917b7b85d28cc3f891a85a0fffca3d',
    'intega00fd710ab80679960abd9db1a70a885',
    NULL, NULL, 'Service URL', 'Kaltura API Endpoint.'
);

-- Field: Session Type (dropdown with LOV)
CALL mpp_vendor_account_config_ins('accnfa64d2f4332193d9538cc54a141c469b7',
    'SESSION_TYPE', '2', '4', '1', '1', '0', '0',
    'datat157598481420466e770020d2a8041930',
    'groupcc917b7b85d28cc3f891a85a0fffca3d',
    'intega00fd710ab80679960abd9db1a70a885',
    NULL, NULL, 'Session Type', 'Set to 2 for Admin or 0 for User.'
);
-- Dropdown (LOV) for Session Type
CALL frp_lov_ins('lovdt774ce70dab861c3a2f0466355c7b15dd', 'accnfa64d2f4332193d9538cc54a141c469b7');
CALL frp_lov_val_ins('lovvle9c92b4c181ebea80983e8258a8e92e6',
    'lovdt774ce70dab861c3a2f0466355c7b15dd', 'SESSION_TYPE', '2,0', 'Admin,User');

-- LXP Configuration Group
CALL mpp_group_ins('groupbd3ef3fddc82283be2d83e447d7ff6c0',
    'KALTURA_EDCAST_GROUP', '2', '2',
    'intega00fd710ab80679960abd9db1a70a885',
    'Cornerstone Learning Experience Configuration', NULL);

-- Field: Source Name (EdCast source identifier)
CALL mpp_vendor_account_config_ins('accnf455a8affcf818dd3e77bf89b943b624d',
    'SOURCE_NAME', NULL, '1', '1', '1', '0', '0',
    'datat157598481420466e770020d2a8041930',
    'groupbd3ef3fddc82283be2d83e447d7ff6c0',
    'intega00fd710ab80679960abd9db1a70a885',
    NULL, NULL, 'Source Name or Id', 'Unique content source identifier in LXP.'
);

-- ─── SECTION 5: ENTITY DEFINITION ────────────────────────────────────────────
-- Defines what data entity types this connector syncs.
-- Each entity maps: SOURCE vendor entity → TARGET EdCast entity
CALL mpp_vendor_entity_ins(
    'mpentafc88b2c7934097a77208b7b77b87969',
    'KALTURA_MEDIA',                                    -- entity_code (matches Content.js)
    'intega00fd710ab80679960abd9db1a70a885',            -- integration_id
    'IMPORT',                                           -- direction
    'Course',                                           -- display_name
    NULL
);

-- Entity association: KALTURA_MEDIA → EdCast Content
CALL mpp_integration_entity_assoc_ins(
    'minead36e2954ba4afdf3935ad56bbf61269e',
    'mpentafc88b2c7934097a77208b7b77b87969',            -- source_entity_id
    'intega00fd710ab80679960abd9db1a70a885',            -- source_integration_id
    'mpenta843eae9e50340b3b472fb165411cont',            -- target_entity_id (EdCast Content)
    'integ67c31f0c9d3540258bf9921654060729'             -- target_integration_id (EdCast)
);

-- ─── SECTION 6: ENTITY SYNC SETTINGS ─────────────────────────────────────────
-- Per-entity configuration for sync behavior: delta dates, threading, file prefix

-- Entity Config Group
CALL mpp_group_ins('group231b2e4fdbee7d82dbcf9eb4edf827f5',
    'KALTURA_ENTITY_GROUP', '1', '2',
    'minead36e2954ba4afdf3935ad56bbf61269e',
    'Kaltura Entity Configuration', NULL);

-- UPDATE_FROM: Delta sync start date (CRITICAL for incremental sync)
CALL mpp_entity_config_ins('entcne29e16508f673a235b048afa9aca7f62',
    'UPDATE_FROM', '2000-01-01T00:00:00Z', '1', '1', '1', '0',
    'datat1575984834184ffc6b9f709c8a04cbf0',
    'group231b2e4fdbee7d82dbcf9eb4edf827f5',
    'minead36e2954ba4afdf3935ad56bbf61269e',
    NULL, NULL, 'Update From', 'Start date for delta sync in ISO 8601 format.'
);
CALL mpp_entity_config_owner_ins('entcne29e16508f673a235b048afa9aca7f62',
    'minead36e2954ba4afdf3935ad56bbf61269e');

-- NUMBER_OF_THREADS, UNIQUE_COLUMN_FOR_HASH, FILE_PREFIX
CALL mpp_group_ins('group66f1adb6f339d71ccfa4af17271d2720',
    'KALTURA_CONTENT_CONFIG', '2', '2',
    'minead36e2954ba4afdf3935ad56bbf61269e',
    'Cornerstone Learning Experience Content Config', NULL);

CALL mpp_entity_config_ins('entcnc3b6a9f46855937edbaad1e4effacb56',
    'NUMBER_OF_THREADS', '1', '1', '1', '1', '0',
    'datat157911427419485ccc4960a4ff047a61',
    'group66f1adb6f339d71ccfa4af17271d2720',
    'minead36e2954ba4afdf3935ad56bbf61269e',
    '1', '10', 'Number of Threads', 'Parallel threads for content processing.'
);
CALL mpp_entity_config_owner_ins('entcnc3b6a9f46855937edbaad1e4effacb56',
    'minead36e2954ba4afdf3935ad56bbf61269e');

CALL mpp_entity_config_ins('entcn74b62df64ed784fc68b4d65a3d824745',
    'UNIQUE_COLUMN_FOR_HASH', '', '2', '1', '1', '0',
    'datat157598481420466e770020d2a8041930',
    'group66f1adb6f339d71ccfa4af17271d2720',
    'minead36e2954ba4afdf3935ad56bbf61269e',
    NULL, NULL, 'Column for Delta Hash', 'Columns for hash-based change detection.'
);
CALL mpp_entity_config_owner_ins('entcn74b62df64ed784fc68b4d65a3d824745',
    'minead36e2954ba4afdf3935ad56bbf61269e');

CALL mpp_entity_config_ins('entcn9ae8c4efcbcab92ef88b1b0cd3ed16d9',
    'FILE_PREFIX', 'kaltura', '3', '0', '1', '0',
    'datat157598481420466e770020d2a8041930',
    'group66f1adb6f339d71ccfa4af17271d2720',
    'minead36e2954ba4afdf3935ad56bbf61269e',
    NULL, NULL, 'File Prefix', 'Prefix for generated data files.'
);
CALL mpp_entity_config_owner_ins('entcn9ae8c4efcbcab92ef88b1b0cd3ed16d9',
    'minead36e2954ba4afdf3935ad56bbf61269e');

-- ─── SECTION 7: EVENT ROUTING (Pulsar Messaging) ─────────────────────────────
-- Defines how sync events flow through the system.
-- Pattern: event_info → channel_event → topic → subscription → routing_rule → bean

CALL inp_event_info_ins('evnif4348abfad3e9e3006655b2eda888e155',
    'KALTURA', 'EDCAST', 'ALL', 'ALL', 'FETCH_DATA', 'API');
CALL inp_event_info_ins('evnif596c1c01f5b6a3d594b55955f1ef2dd9',
    'KALTURA', 'EDCAST', 'ALL', 'ALL', 'SEND_DATA', 'API', 'INDIVIDUAL');

-- Channel event → matches entity code KALTURA_MEDIA
CALL inp_channel_event_ins('eventfc4597ab644a1062517d5e37d21c6245',
    'API', 'KALTURA_MEDIA', 'FETCH_DATA', 'ALL',
    'event1577996406072cb4736870572104d8e0');

-- Pulsar topic for async processing
CALL inp_channel_topic_ins('mesgt52f1fbfde06b3dc4ca9dc6798ad6a122',
    'Topic for fetching media from Kaltura',
    'KalturaMediaFetch',        -- topic name (CamelCase convention)
    'integration', 'INTEGRATION', 'integrationsystem', 'pulsar'
);

-- Subscription (consumers for the topic)
CALL inp_channel_subscription_ins('msbs0e9d1782d5671a4b871cb4d2315ae0baf',
    '5', '5', 'Shared', 'KalturaMediaFetch',
    'mesgt52f1fbfde06b3dc4ca9dc6798ad6a122');

-- Routing rule: KALTURA events → EDCAST destination
CALL inp_channel_routing_rule_ins('rrule8e44e0097c51e9fc4a7dc66b26d2d948',
    'EDCAST', '4', 'KALTURA',
    'tnant0000004d53ded70137fe761b4f008000',
    'eventfc4597ab644a1062517d5e37d21c6245',
    'mesgt52f1fbfde06b3dc4ca9dc6798ad6a122');

-- Bean binding: subscription → Spring Integration flow bean
-- The bean name MUST match the @Bean name in Flows.java!
CALL inp_channel_mesg_sub_owner_ins('msubo72cf045fddd64a8444e4211d962dde93',
    NULL,
    'integration.kaltura.import.content',   -- MUST match @Bean name in KalturaFlows.java
    'msbs0e9d1782d5671a4b871cb4d2315ae0baf'
);

-- Subscription profile
CALL inp_channel_sub_profile_ins('msubp2a392b467e069af50db1cde8ad353bd4',
    'KalturaProfile', 'msubp1577996406012e50543eb01d44041170');
CALL inp_channel_sub_profile_assoc_ins('msubp2a392b467e069af50db1cde8ad353bd4',
    'msbs0e9d1782d5671a4b871cb4d2315ae0baf');

-- ─── SECTION 8: MAPPING CLEANUP (for re-provisioning) ────────────────────────
CALL mpp_mapping_del('minead36e2954ba4afdf3935ad56bbf61269e');
DELETE FROM mpt_target_attributes_custom_value
WHERE mapping_id IN (SELECT id FROM mpt_mapping WHERE integration_entity = 'minead36e2954ba4afdf3935ad56bbf61269e');
DELETE FROM mpt_mapping_detail
WHERE mapping_key IN (
    SELECT id FROM mpt_mapping
    WHERE tenant_id = 'tnant0000004d53ded70137fe761b4f008000'
    AND integration_entity = 'minead36e2954ba4afdf3935ad56bbf61269e'
);

COMMIT;
