--Udemy Connector Registration

-- Vendor Entity
call mpp_vendor_entity_ins('mpent7e91911929984519851919819851911','Udemy','Udemy',null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null);

-- Integration Entity Association
call mpp_integration_entity_assoc_ins('minea9b9b9b9b9b9b9b9b9b9b9b9b9b9b9b9','integ77c9a589942b49c9b299123456789abc','mpent7e91911929984519851919819851911');

-- Entity Configs
call mpp_entity_config_ins('entnc1a2b3c4d5e6f7a8b9c0d1e2f3a4b5c6','Udemy Content','Content','Content',null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null);

-- Integration Entity Config Association
call mpp_integration_entity_config_assoc_ins('minea0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0','integ77c9a589942b49c9b299123456789abc','entnc1a2b3c4d5e6f7a8b9c0d1e2f3a4b5c6');

-- Vendor Entity Config Association
call mpp_vendor_entity_config_assoc_ins('mpenta1b2c3d4e5f6a7b8c9d0e1f2a3b4c5d6','mpent7e91911929984519851919819851911','entnc1a2b3c4d5e6f7a8b9c0d1e2f3a4b5c6');

-- Content Master Tenant record addition
call mpp_tenant_entity_config_master_ins('entnc1a2b3c4d5e6f7a8b9c0d1e2f3a4b5c6','Udemy Content','tnant0000004d53ded70137fe761b4f008000','minea9b9b9b9b9b9b9b9b9b9b9b9b9b9b9b9','IMPORT');