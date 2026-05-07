--Hrms Connector Registration
--Vendor
call mpp_vendor_entity_ins('mpent19f1475399964064855c6091691670b','Hrms','Hrms','Hrms',null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null);

--Integration
call mpp_integration_entity_ins('integ75e46c98996b49e78ca8645ca5544596','Hrms','Hrms Integration','Hrms',null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null);

--Association
call mpp_integration_entity_assoc_ins('intassc0b9565e45674179890a814966999727','integ75e46c98996b49e78ca8645ca5544596','mpent19f1475399964064855c6091691670b');

--Content
call mpp_entity_config_ins('entnc4ca41e9964748198557450f80a8944f','Hrms Content','Content','com.saba.integration.apps.hrms.HrmsContent','com.saba.integration.apps.hrms.flow.HrmsContentFlow','HTTP','TRANSFORM','Hrms','EdCast','hrms_edcast_content.xml','IMPORT','ACTIVE','false','ref_id','ref_id',null,null,null,null,null,null,null,null,null,null,null,null,null,null);
call mpp_integration_entity_config_assoc_ins('integc4ca41e9964748198557450f80a8944f','integ75e46c98996b49e78ca8645ca5544596','entnc4ca41e9964748198557450f80a8944f');

-- START (EVOVE) Master Tenant record addition
call mpp_tenant_entity_config_master_ins('entnc4ca41e9964748198557450f80a8944f','Hrms Content','tnant0000004d53ded70137fe761b4f008000','minea4ca41e9964748198557450f80a8944f','IMPORT');
-- END (EVOVE) Master Tenant record addition