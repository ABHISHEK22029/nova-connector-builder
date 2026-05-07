--Zoom Connector Registration

-- Vendor
call mpp_vendor_entity_ins('mpent7e5a9a59759b49a9a9a9a9a9a9a9a9a','Zoom','Zoom Connector',null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null);

-- Integration
call mpp_integration_entity_ins('integ4f9f8c8a9b3b4a2a8c7c6d9e1e2d3a4b','Zoom Connector','Zoom',null,'Zoom',null,null,null,null,null,null,null,null,null,null,null,null,null,null,null);

-- Vendor Association
call mpp_integration_entity_assoc_ins('intvea1a2b3c4d5e6f7a8b9c0d1e2f3a4b5c','integ4f9f8c8a9b3b4a2a8c7c6d9e1e2d3a4b','mpent7e5a9a59759b49a9a9a9a9a9a9a9a9a',null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null);

-- Content
call mpp_entity_config_ins('entnc1a2b3c4d5e6f7a8b9c0d1e2f3a4b5c6','Zoom to EdCast Content','Zoom EdCast Content',null,'Zoom','EdCast','content','ToCsv',null,null,null,null,null,null,null,null,null,null,null,null);
call mpp_integration_entity_assoc_ins('inteaa1a2b3c4d5e6f7a8b9c0d1e2f3a4b5c6','integ4f9f8c8a9b3b4a2a8c7c6d9e1e2d3a4b','entnc1a2b3c4d5e6f7a8b9c0d1e2f3a4b5c6',null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null);

-- User
call mpp_entity_config_ins('entnc7b8a9c0d1e2f3a4b5c6d7e8f9a0b1c2','Zoom to EdCast User','Zoom EdCast User',null,'Zoom','EdCast','user','ToCsv',null,null,null,null,null,null,null,null,null,null,null,null);
call mpp_integration_entity_assoc_ins('inteba3c4d5e6f7a8b9c0d1e2f3a4b5c6d7','integ4f9f8c8a9b3b4a2a8c7c6d9e1e2d3a4b','entnc7b8a9c0d1e2f3a4b5c6d7e8f9a0b1c2',null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null);

-- Group
call mpp_entity_config_ins('entnc9d0e1f2a3b4c5d6e7f8a9b0c1d2e3f4','Zoom to EdCast Group','Zoom EdCast Group',null,'Zoom','EdCast','group','ToCsv',null,null,null,null,null,null,null,null,null,null,null,null);
call mpp_integration_entity_assoc_ins('intega5e6f7a8b9c0d1e2f3a4b5c6d7e8f9a0b','integ4f9f8c8a9b3b4a2a8c7c6d9e1e2d3a4b','entnc9d0e1f2a3b4c5d6e7f8a9b0c1d2e3f4',null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null);