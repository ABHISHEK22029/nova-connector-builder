--Udemy Connector Registration

-- Vendor Entity
call mpp_vendor_entity_ins('mpent1ca1a1987a0a40868545529999f6649','Udemy','Udemy',null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null);

-- Integration Entity Association
call mpp_integration_entity_assoc_ins('minea4896668919e4796b192190998177664', 'integ4f6f1c11a7a348a385c8801199f35999', 'mpent1ca1a1987a0a40868545529999f6649');

-- Entity Configs
call mpp_entity_config_ins('entnc40a9a9a9a9a4a9a9a9a9a9a9a9a9a9a','Udemy to EdCast Content','minea79a969933944158999b165935d229','mpent1ca1a1987a0a40868545529999f6649','content','IMPORT','com.saba.integration.marketplace.mapping.impl.ContentMappingServiceImpl','udemy_edcast_content.xml',null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null);

-- Master Tenant record addition
call mpp_tenant_entity_config_master_ins('entnc40a9a9a9a9a4a9a9a9a9a9a9a9a9a9a','Udemy to EdCast Content','tnant0000004d53ded70137fe761b4f008000','minea4896668919e4796b192190998177664','IMPORT');