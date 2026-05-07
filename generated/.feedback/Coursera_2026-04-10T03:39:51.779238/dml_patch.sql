--Coursera

--Vendor
call mpp_vendor_entity_ins('mpent49f699999c824d9289594351a55c69a','Coursera','Coursera',null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null);

--Integration
call mpp_integration_entity_assoc_ins('integ79f8e96c99994587a9330866512a3a83','Coursera Connector','Coursera Connector','mpent49f699999c824d9289594351a55c69a',null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null);

--Content
call mpp_entity_config_ins('entnc47e99989c824d9289594351a55c69a','Coursera Content','Coursera Content','integ79f8e96c99994587a9330866512a3a83',null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null);
call mpp_integration_entity_assoc_ins('minea47e99989c824d9289594351a55c69a','Coursera Content Mapping','Coursera Content Mapping','entnc47e99989c824d9289594351a55c69a',null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null);

--Content Master Tenant
call mpp_tenant_entity_config_master_ins('entnc47e99989c824d9289594351a55c69aa','Coursera Content','tnant0000004d53ded70137fe761b4f008000','minea47e99989c824d9289594351a55c69a','IMPORT');