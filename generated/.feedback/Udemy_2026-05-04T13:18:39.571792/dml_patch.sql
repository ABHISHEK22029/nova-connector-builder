-- 1. Register vendor entity for Udemy
call mpp_vendor_entity_ins(
    'integf1e2d3c4b5a6f7e8d9c0b1a2f3e4d5c6',         -- integration_id (from VendorConstants_patch.java)
    'Udemy',                                        -- display_name
    'Content',                                      -- entity_type
    'IMPORT',                                       -- direction
    'ACTIVE',                                       -- status
    null                                            -- optional params
);

-- 2. Register entity association for Udemy Content
call mpp_integration_entity_assoc_ins(
    'minea0123456789abcdef0123456789abcdef',         -- assoc_id (new unique ID)
    'integf1e2d3c4b5a6f7e8d9c0b1a2f3e4d5c6',         -- integration_id
    'mpent1a2b3c4d5e6f7a8b9c0d1e2f3a4b5c6d',         -- entity_id (from DefaultMappingConfig_patch.java)
    'content',                                      -- entity_type
    1,                                              -- schedule_order
    'integration.udemy.import.content'              -- flow_bean_name
);

-- 3. Register entity config for Udemy Content (UPDATE_FROM)
call mpp_entity_config_ins(
    'mpent1a2b3c4d5e6f7a8b9c0d1e2f3a4b5c6d',         -- entity_id
    'UPDATE_FROM',                                  -- config_key
    '2020-01-01',                                   -- default_value
    'DATE'                                          -- value_type
);