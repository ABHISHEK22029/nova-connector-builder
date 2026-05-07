-- 1. Register vendor entity
call mpp_vendor_entity_ins(
    'integ5f2f68e12cf648c6ba28dded1b64b88c',         -- integration_id
    'Successfactor',                 -- display_name
    'Content',                     -- entity_type
    'IMPORT',                      -- direction
    'ACTIVE',                      -- status
    null                           -- optional params
);

-- 2. Register entity association
call mpp_integration_entity_assoc_ins(
    'minea731a809653c40898573a213b0567616',         -- assoc_id
    'integ5f2f68e12cf648c6ba28dded1b64b88c',         -- integration_id
    'mpent12a99a364e94441868471e9999a499c',         -- entity_id
    'content',                     -- entity_type
    1,                             -- schedule_order
    'integration.successfactor.import.content'  -- flow_bean_name
);

-- 3. Register entity config
call mpp_entity_config_ins(
    'mpent12a99a364e94441868471e9999a499c',         -- entity_id
    'UPDATE_FROM',                 -- config_key
    '2020-01-01',                  -- default_value
    'DATE'                         -- value_type
);