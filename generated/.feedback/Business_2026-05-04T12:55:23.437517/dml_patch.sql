-- 1. Register vendor entity
call mpp_vendor_entity_ins(
    'integ17228300940000000000000000000000',         -- integration_id
    'Business',                 -- display_name
    'Content',                     -- entity_type
    'IMPORT',                      -- direction
    'ACTIVE',                      -- status
    null                           -- optional params
);

-- 2. Register entity association
call mpp_integration_entity_assoc_ins(
    'minea17228300940000000000000000000002',         -- assoc_id
    'integ17228300940000000000000000000000',         -- integration_id
    'mpent17228300940000000000000000000001',         -- entity_id
    'content',                     -- entity_type
    1,                             -- schedule_order
    'integration.business.import.content'  -- flow_bean_name
);

-- 3. Register entity config
call mpp_entity_config_ins(
    'mpent17228300940000000000000000000001',         -- entity_id
    'UPDATE_FROM',                 -- config_key
    '2020-01-01',                  -- default_value
    'DATE'                         -- value_type
);