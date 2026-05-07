-- 1. Register vendor entity
call mpp_vendor_entity_ins(
    'integ79564a99514b41188111e391599a497a',         -- integration_id
    'Udemy',                 -- display_name
    'Content',                     -- entity_type
    'IMPORT',                      -- direction
    'ACTIVE',                      -- status
    null                           -- optional params
);

-- 2. Register entity association
call mpp_integration_entity_assoc_ins(
    'minea54149993e64463493172147482932',         -- assoc_id
    'integ79564a99514b41188111e391599a497a',         -- integration_id
    'mpent157852367760824ad188f04b3804c9b0',         -- entity_id
    'content',                     -- entity_type
    1,                             -- schedule_order
    'integration.udemy.import.content'  -- flow_bean_name
);

-- 3. Register entity config
call mpp_entity_config_ins(
    'mpent157852367760824ad188f04b3804c9b0',         -- entity_id
    'UPDATE_FROM',                 -- config_key
    '2020-01-01',                  -- default_value
    'DATE'                         -- value_type
);