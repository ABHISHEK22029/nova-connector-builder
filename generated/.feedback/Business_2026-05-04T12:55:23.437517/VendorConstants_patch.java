package com.saba.integration.marketplace.vendor;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * @author curkudkar on 5/21/20
 **/
public interface VendorConstants {
    String ULTIPRO="integ15743711107576451b40205e41042530";
    String LINKEDIN_LEARNING="integ1575939913732429ee6e60b53a046cc0";
    String WORKDAY="integ15743711107576451b40201588718030";
    String CSX_WORKDAY="integ8c47970b30bb446bba715d8cd35fbc36";
    String CSX_WORKDAY_OUTBOUND="integ6d510ea79cf64bd3b873a5e9cb6e9d00";
    String ADP_WFN="integ1576870947315a747f95c0fba4047420";
    String UDEMY="integc94b5ba2bd1d7165643ecf27f9dddabc";
    String WEBEX_MEETINGS ="integ15743711107576451b40201615384792";
    String MS_TEAMS_MEETINGS="integ67c31f0c9d3540258bf992c21bfbefee";
    String ZOOM_MEETINGS="integ9d729c43589d4a058fc80660dca3a883";
    String ZOOM_WEBINAR="integ7fca1cc80fec448c9832a734522c609d";

    String ZOOM_USER_MEETING="integ61b5a71efc7147e4b3ab73f9d7411dd7";
    String WEBEX_TRAININGS="integ24389e963f3644e5828a9a3616b4a4ef";
    String ADOBE_CONNECT="integ364c5118369e4f59a1432d699e1cf8d5";

    String SFTP="integ9d729c43589d4a058fc8061633582671";
    String GENERIC_SFTP="integc540811f872645a9b0c7a87ba8bc9423";
    String CCA="integc4b5abf18aed4cc98c0c997f81250710";
    String PERCIPIO="integ71b03ef0345148288c4938302dcc906a";
    String SKILLPILL ="integ6419f4820fb74c5da93856bdfe28b4d9";
    String CREDLY ="integ5f2f68e12cf648c6ba28dded1b64b88c";
    String MS_TEAMS_BOTS ="intega79a6a0685b547da9f8873da57eb72e4";
    String WEBEX_MEETINGS_v2 ="intege9e7c20ee81244d9a7871740cc86ed54";
    String CSX ="integ67c31f0c9d3540258bf9921654057376";
    String SBX ="integ15746680281997bad7d050d8bd04dc70";
    String CCA_CODE ="CCA";
    String EVOLVE_SUMTOTAL_SFTP = "integc2136135eb2e4e67a578c2b3e6e0c4ed";
    String EVOLVE_SABA_SFTP = "integcd76087cd5ef44c49aa1adae13e14c18";
    String EVOLVE_TALENTSPACE_SFTP = "integ61e2a17a616e45fa9b5788300d7742be";
    String CCC = "integ88074530e83641c1bce89958cb5d1c0b";
    String LIL = "integ9a315e8480a341e98c9eb8a8ca94f67d";
    String EVOLVE_CSX = "intege919ed7423e04d91acea34910e3302b3";
    String EDCAST="integ67c31f0c9d3540258bf9921654060729";
    String PROVIDER_SPECIFIC_CCC="integ84bcd6a3c98245b98dbdbb349086c589";
    String KALTURA = "intega00fd710ab80679960abd9db1a70a885";
    String BUSINESS = "integ17228300940000000000000000000000"; // Unique ID for Business connector

    String EVOLVE_PRODUCT_CODE_SABA = "SABA";
    String EVOLVE_PRODUCT_CODE_SUMTOTAL = "SUMTOTAL";
    String EVOLVE_PRODUCT_CODE_TALENTSPACE = "TALENTSPACE";
    Set<String> EVOLVE_PRODUCT_CODES = new HashSet<>(Arrays.asList(EVOLVE_PRODUCT_CODE_SABA, EVOLVE_PRODUCT_CODE_SUMTOTAL, EVOLVE_PRODUCT_CODE_TALENTSPACE));
    Set<String> EVOLVE_INTEGRATION_IDS= new HashSet<>(Arrays.asList(EVOLVE_SUMTOTAL_SFTP, EVOLVE_SABA_SFTP, EVOLVE_TALENTSPACE_SFTP));
}