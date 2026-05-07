--Udacity Connector Registration
--Vendor
call mpp_vendor_entity_ins('mpent591d459a997d4a99859644945f69b99','Udacity','Udacity',null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null);

--Integration
call mpp_integration_entity_ins('integ7877499f657c44f1941e9999f0e12c7a','Udacity EdCast Content','Udacity EdCast Content','EdCast','content','IMPORT','com.saba.integration.apps.udacity.UdacityEdcastContentFlow','com.saba.integration.apps.udacity.UdacityEdcastContentConnector',null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null);

--Association
call mpp_integration_entity_assoc_ins('intgr7877499f657c44f1941e9999f0e12c7a','mpent591d459a997d4a99859644945f69b99');

--Entity Config
call mpp_entity_config_ins('entcn6b995591927d4999827914759121085a','Udacity to EdCast Content','integ7877499f657c44f1941e9999f0e12c7a','EdCast','content','ref_id','ref_id',null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null);

--Mapping
call mpp_evolve_entity_mapping_ins('minea49b899515934597851596599165399','Udacity to EdCast Content','EdCast','content','entcn6b995591927d4999827914759121085a','com.saba.integration.apps.udacity.UdacityEdcastContentMapping','udacity_edcast_content.xml',null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null);

-- START (EVOVE) Master Tenant record addition
call mpp_tenant_entity_config_master_ins('entnc6b995591927d4999827914759121085a','Udacity to EdCast Content','tnant0000004d53ded70137fe761b4f008000','minea49b899515934597851596599165399','IMPORT');
-- END Master Tenant record addition