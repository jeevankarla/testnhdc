-- This script is meant to reset all accounting-related master data.  
-- After this cleanup, new accounting master data will need to be loaded for LMS

DELETE FROM `ACCTG_TRANS_ENTRY`;
DELETE FROM `ACCTG_TRANS`;
DELETE FROM `PAYMENT_APPLICATION`;
DELETE FROM `PAYMENT`;
UPDATE `INVOICE` SET `STATUS_ID`='INVOICE_APPROVED' WHERE `STATUS_ID`='INVOICE_PAID';
DELETE FROM `INVOICE_STATUS` WHERE `STATUS_ID`='INVOICE_READY' OR `STATUS_ID`='INVOICE_PAID';




-- GL Account and related cleanup

DELETE FROM `INVOICE_ITEM_TYPE_MAP` WHERE `INVOICE_ITEM_TYPE_ID` <> 'INV_PROD_ITEM' AND `INVOICE_ITEM_TYPE_ID` <> 'INV_FPROD_ITEM' AND `INVOICE_ITEM_TYPE_ID` <> 'INV_FPROD_CARD_ITEM';
DELETE FROM `INVOICE_ITEM_TYPE` WHERE `INVOICE_ITEM_TYPE_ID` <> 'INV_PROD_ITEM' AND `INVOICE_ITEM_TYPE_ID` <> 'INV_FPROD_ITEM' AND `INVOICE_ITEM_TYPE_ID` <> 'INV_FPROD_CARD_ITEM';


DELETE FROM PAYMENT_METHOD_TYPE_GL_ACCOUNT;

DELETE FROM `GL_ACCOUNT_HISTORY`;
DELETE FROM `GL_ACCOUNT_ORGANIZATION`;
DELETE FROM `GL_ACCOUNT_TYPE_DEFAULT`;
SET foreign_key_checks = 0;
DELETE FROM `GL_ACCOUNT`;
SET foreign_key_checks = 1;

