-- 1. Register vendor entity for Udemy
call mpp_vendor_entity_ins(
    'integ9f8e7d6c5b4a3e2d1c0b9a8f7e6d5c4b',         -- integration_id (from VendorConstants.UDEMY)
    'Udemy',                                         -- display_name
    'Content',                                       -- entity_type
    'IMPORT',                                        -- direction
    'ACTIVE',                                        -- status
    null                                             -- optional params
);

-- 2. Register entity association for Udemy Content
call mpp_integration_entity_assoc_ins(
    'minea1d2e3f4a5b6c7d8e9f0a1b2c3d4e5f6a',         -- assoc_id (unique for this association)
    'integ9f8e7d6c5b4a3e2d1c0b9a8f7e6d5c4b',         -- integration_id (from VendorConstants.UDEMY)
    'mpent1a2b3c4d5e6f7a8b9c0d1e2f3a4b5c6d',         -- entity_id (from DefaultMappingConfig.UDEMY_CONTENT)
    'content',                                       -- entity_type (lowercase)
    1,                                               -- schedule_order
    'integration.udemy.import.content'               -- flow_bean_name
);

-- 3. Register entity config for Udemy Content (UPDATE_FROM)
call mpp_entity_config_ins(
    'mpent1a2b3c4d5e6f7a8b9c0d1e2f3a4b5c6d',         -- entity_id (from DefaultMappingConfig.UDEMY_CONTENT)
    'UPDATE_FROM',                                   -- config_key
    '2020-01-01',                                    -- default_value
    'DATE'                                           -- value_type
);