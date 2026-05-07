-- 1. Register vendor entity
call mpp_vendor_entity_ins(
    'integ7b8c9d0e1f2a3b4c5d6e7f8a9b0c1d2e',         -- integration_id (from VendorConstants.STRIPE)
    'Stripe',                                       -- display_name
    'Content',                                      -- entity_type
    'IMPORT',                                       -- direction
    'ACTIVE',                                       -- status
    null                                            -- optional params
);

-- 2. Register entity association
call mpp_integration_entity_assoc_ins(
    'minea1234567890abcdef1234567890abcdef',         -- assoc_id (unique for this association)
    'integ7b8c9d0e1f2a3b4c5d6e7f8a9b0c1d2e',         -- integration_id (from VendorConstants.STRIPE)
    'mpent0123456789abcdef0123456789abcdef',         -- entity_id (source entity from DefaultMappingConfig)
    'content',                                      -- entity_type
    1,                                              -- schedule_order
    'integration.stripe.import.content'             -- flow_bean_name
);

-- 3. Register entity config for UPDATE_FROM
call mpp_entity_config_ins(
    'mpent0123456789abcdef0123456789abcdef',         -- entity_id (source entity from DefaultMappingConfig)
    'UPDATE_FROM',                                  -- config_key
    '2020-01-01',                                   -- default_value
    'DATE'                                          -- value_type
);