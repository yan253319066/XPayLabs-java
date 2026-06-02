更换项目里的全部地址，t_user_address表的地址是全部删除的，前端用户会重新分配新地址。
1.先备份t_address_pool表
2.执行下面的sql
DELETE FROM `t_address_pool`;
UPDATE `t_asset_type` SET hot_address = '';
UPDATE `t_merchant_address` SET hot_address = '';
DELETE FROM `t_user_address`;
3.先运行xpay-tron，xpay-eth生成新地址
4.运行XPayLabs，会自动执行UpdateAllAddress类的modifyAllAddress方法。

xpay 8077
xpay-tron 8075
xpay-eth 8076
xpay-merchant 8078
xpay-sui 8074