--
-- Patches DML for Vendor: Udemy
--

SET @VENDOR_NAME = 'Udemy';
SET @VENDOR_CODE = 'UDEMY';
SET @INTEGRATION_NAME = 'Udemy Content';
SET @INTEGRATION_ID = 'integ-4a9bb7789c454a49994059d3a4c7429b';
SET @ENTITY_ID = 'learningAsset';
SET @MP_ENTITY_ID = 'mpent-5b9c7b3a9b5a4b8a8b5a9b5a9b5a9b5a';
SET @UI_SCREEN_FLOW_ID = 'minea-6c3e5a7e8b7a4b8a8b7a8b7a8b7a8b7a';

-- Insert vendor entity
CALL mpp_vendor_entity_ins(@VENDOR_NAME, @VENDOR_CODE, @INTEGRATION_ID, @INTEGRATION_NAME);

-- Insert integration entity association
CALL mpp_integration_entity_assoc_ins(@INTEGRATION_ID, @ENTITY_ID);

-- Insert entity config for learningAsset
CALL mpp_entity_config_ins(@ENTITY_ID, 'Content', 'Learning Asset', 1, 1);

-- Insert mapping ui screen flow
CALL mpp_ui_screen_flow_ins(@UI_SCREEN_FLOW_ID, 'Udemy Content Mapping', 'Content Mapping for Udemy');

-- Associate integration with UI screen flow
CALL mpp_integration_ui_assoc_ins(@INTEGRATION_ID, @UI_SCREEN_FLOW_ID);

-- Insert entity config properties (example - adjust as needed based on your actual needs)
-- Example: API URL
CALL mpp_entity_config_property_ins(@MP_ENTITY_ID, 'api_url', 'API URL', 'string', 'https://www.udemy.com/api-2.0/courses/', NULL, 1, 1, @INTEGRATION_ID);
-- Example: API Client ID
CALL mpp_entity_config_property_ins(@MP_ENTITY_ID, 'client_id', 'Client ID', 'string', '', NULL, 2, 1, @INTEGRATION_ID);
-- Example: API Client Secret
CALL mpp_entity_config_property_ins(@MP_ENTITY_ID, 'client_secret', 'Client Secret', 'string', '', NULL, 3, 1, @INTEGRATION_ID);