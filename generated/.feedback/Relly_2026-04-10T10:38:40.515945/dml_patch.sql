--Relly Connector Registration
--Vendor
call mpp_vendor_entity_ins('mpent8349dd9286914414840933994a937d41','Relly','Relly',null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null);

--Integration
call mpp_integration_entity_ins('integ78754a23b11c4994a99c732ff576967f','Relly','Relly',null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null);

--Integration Vendor Association
call mpp_integration_entity_assoc_ins('intvdr08942c15937441a994908b817a956149','integ78754a23b11c4994a99c732ff576967f','mpent8349dd9286914414840933994a937d41');

--Entity
call mpp_entity_ins('mpent46a9a29985034649a933464a7099a669','EdCast Content','EdCast Content',null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null);

--Entity config
call mpp_entity_config_ins('entcn46a9a29985044649a933464a7099a669','EdCast Content','EdCast Content',null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null);

--Entity config association
call mpp_entity_config_assoc_ins('entass46a9a29985054649a933464a7099a669','mpent46a9a29985034649a933464a7099a669','entcn46a9a29985044649a933464a7099a669');

--Integration Entity Association
call mpp_integration_entity_assoc_ins('inteass46a9a29985064649a933464a7099a669','integ78754a23b11c4994a99c732ff576967f','mpent46a9a29985034649a933464a7099a669');

--Target Entity
call mpp_entity_ins('mpent1574694635123a21f3fc60d7fd04c9d0','EdCast','EdCast',null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null);

-- Default Mapping
INSERT INTO `mpt_default_mapping` (`id`, `name`, `source_integration_id`, `target_integration_id`, `source_entity_id`, `target_entity_id`, `file_name`, `md5_hash`, `is_valid`) VALUES
('integ15746680281997bad7d050d8bd04dc70', 'RELLY EDCAST CONTENT', 'integ78754a23b11c4994a99c732ff576967f', 'integ00000000000000000000000000000002', 'mpent46a9a29985034649a933464a7099a669', 'mpent1574694635123a21f3fc60d7fd04c9d0', 'relly_edcast_content.xml', '6D34A9B7123C49E089C5B26999417871', b'1');

-- START (EVOVE) Master Tenant record addition
--Relly
call mpp_tenant_entity_config_master_ins('entnc46a9a29985074649a933464a7099a669','Relly EdCast Content','tnant0000004d53ded70137fe761b4f008000','integ78754a23b11c4994a99c732ff576967f','IMPORT');