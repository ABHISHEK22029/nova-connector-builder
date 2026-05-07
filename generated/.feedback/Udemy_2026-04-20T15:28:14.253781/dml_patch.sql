--Udemy Connector Registration

-- Vendor Entity
call mpp_vendor_entity_ins('venent.udemy','Udemy','Udemy',null,null,null,null,null,null,null);

-- Integration Entity
call mpp_integration_entity_ins('integ.mpent.minea.59e8b9a2c1d34f6a8b7c0e9d2a3b4f56','Udemy', 'Udemy Connector', 'Udemy', 'HTTP', 'EdCast', 'ToCsv', 'Udemy Content to EdCast', 'Maps Udemy Course data to EdCast format.',null,null,null,null);

-- Integration Entity Association
call mpp_integration_entity_assoc_ins('intnentassoc.udemy.edcast.content','integ.mpent.minea.59e8b9a2c1d34f6a8b7c0e9d2a3b4f56','venent.udemy');

-- Entity Configs
call mpp_entity_config_ins('entnc.udemy.edcast.content','Udemy Content to EdCast','integ.mpent.minea.59e8b9a2c1d34f6a8b7c0e9d2a3b4f56','com.saba.integration.connector.http.HttpConnector','com.saba.integration.target.tocsv.ToCsvConnector',null,null,null);

-- Content
call mpp_entity_config_master_ins('entnc67a9c5f39d2b4e81b7a2d63c9e1b8f04','Udemy Content to EdCast','tnant0000004d53ded70137fe761b4f008000','integ.mpent.minea.67a9c5f39d2b4e81b7a2d63c9e1b8f04','IMPORT');