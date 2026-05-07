-- 1. Register vendor entity
call mpp_vendor_entity_ins(
    'integ46537721288743678975119163977807',         -- integration_id
    'Udemy',                 -- display_name
    'Content',                     -- entity_type
    'IMPORT',                      -- direction
    'ACTIVE',                      -- status
    null                           -- optional params
);

-- 2. Register entity association
call mpp_integration_entity_assoc_ins(
    'minea115899b849444999821f9939856991',         -- assoc_id
    'integ46537721288743678975119163977807',         -- integration_id
    'mpent68495498239845648952345234598745',         -- entity_id
    'content',                     -- entity_type
    1,                             -- schedule_order
    'integration.udemy.import.content'  -- flow_bean_name
);

-- 3. Register entity config
call mpp_entity_config_ins(
    'mpent68495498239845648952345234598745',         -- entity_id
    'UPDATE_FROM',                 -- config_key
    '2020-01-01',                  -- default_value
    'DATE'                         -- value_type
);