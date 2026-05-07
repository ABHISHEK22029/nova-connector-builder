--Udemy Connector Registration
--Vendor
call mpp_vendor_entity_ins('mpentba9a80f5654a4939929251c55119631','Udemy','Udemy',null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null);

--Integration
call mpp_integration_entity_assoc_ins('integ78797a92e13a4c468a29d343a194456b','Udemy Connector','Udemy Connector','Udemy',null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null);

--Entity
call mpp_entity_config_ins('mpent9e4999557a5a4759a64990699315565','Udemy Content','Udemy Content','CONTENT',null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null);

--Entity Association
call mpp_integration_entity_assoc_ins('integ15746680281997bad7d050d8bd04dc70','Udemy to EdCast Content','Udemy to EdCast Content','EdCast',null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null);

--Config
call mpp_entity_config_ins('entnc7489939009a449388683396f722a921','Udemy to EdCast Content','Udemy to EdCast Content','CONTENT',null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null);

--Mapping
--call mpp_evolve_entity_mapping_del('minea7e946819011444098945961794563a7');
--call mpp_evolve_entity_mapping_ins('minea7e946819011444098945961794563a7','Udemy to EdCast Content','integ78797a92e13a4c468a29d343a194456b','integ15746680281997bad7d050d8bd04dc70','mpent9e4999557a5a4759a64990699315565','mpent1574694635123a21f3fc60d7fd04c9d0','udemy_edcast_content.xml','27B7B49369799798F835437999B67D5A');

-- START (EVOVE) Master Tenant record addition
call mpp_tenant_entity_config_master_ins('entnc7489939009a449388683396f722a921','Udemy to EdCast Content','tnant0000004d53ded70137fe761b4f008000','minea7e946819011444098945961794563a7','IMPORT');
-- END (EVOVE) Master Tenant record addition