package com.zz.sccommon.constant;

/**
 * 常量
 */
public class WXPayConstants {

    public enum SignType {
        MD5, HMACSHA256
    }

    public static final String DOMAIN_API = "api.mch.weixin.qq.com";
    public static final String DOMAIN_API2 = "api2.mch.weixin.qq.com";
    public static final String DOMAIN_APIHK = "apihk.mch.weixin.qq.com";
    public static final String DOMAIN_APIUS = "apius.mch.weixin.qq.com";


    public static final String HMACSHA256 = "HMAC-SHA256";
    public static final String MD5 = "MD5";

    public static final String CONTRACT_VERSION = "1.0";

    public static final String WXPAYSDK_VERSION = "WXPaySDK/3.0.9";

    public static final String MICROPAY_URL_SUFFIX = "/pay/micropay";
    public static final String UNIFIEDORDER_URL_SUFFIX = "/pay/unifiedorder";
    public static final String ORDERQUERY_URL_SUFFIX = "/pay/orderquery";
    public static final String REVERSE_URL_SUFFIX = "/secapi/pay/reverse";
    public static final String CLOSEORDER_URL_SUFFIX = "/pay/closeorder";
    public static final String REFUND_URL_SUFFIX = "/secapi/pay/refund";
    public static final String REFUNDQUERY_URL_SUFFIX = "/pay/refundquery";
    public static final String DOWNLOADBILL_URL_SUFFIX = "/pay/downloadbill";
    public static final String REPORT_URL_SUFFIX = "/payitil/report";
    public static final String SHORTURL_URL_SUFFIX = "/tools/shorturl";
    public static final String AUTHCODETOOPENID_URL_SUFFIX = "/tools/authcodetoopenid";
    public static final String QUERYCONTRACT_URL_SUFFIX = "/papay/querycontract";
    public static final String PREENTRUSTWEB_URL_SUFFIX = "/papay/preentrustweb";
    public static final String DELETECONTRACT_URL_SUFFIX = "/papay/deletecontract";
    public static final String PAPPAYAPPLY_URL_SUFFIX = "/pay/pappayapply";
    public static final String PAPORDERQUERY_URL_SUFFIX = "/pay/paporderquery";


    // sandbox
    public static final String SANDBOX_MICROPAY_URL_SUFFIX = "/sandboxnew/pay/micropay";
    public static final String SANDBOX_UNIFIEDORDER_URL_SUFFIX = "/sandboxnew/pay/unifiedorder";
    public static final String SANDBOX_ORDERQUERY_URL_SUFFIX = "/sandboxnew/pay/orderquery";
    public static final String SANDBOX_REVERSE_URL_SUFFIX = "/sandboxnew/secapi/pay/reverse";
    public static final String SANDBOX_CLOSEORDER_URL_SUFFIX = "/sandboxnew/pay/closeorder";
    public static final String SANDBOX_REFUND_URL_SUFFIX = "/sandboxnew/secapi/pay/refund";
    public static final String SANDBOX_REFUNDQUERY_URL_SUFFIX = "/sandboxnew/pay/refundquery";
    public static final String SANDBOX_DOWNLOADBILL_URL_SUFFIX = "/sandboxnew/pay/downloadbill";
    public static final String SANDBOX_REPORT_URL_SUFFIX = "/sandboxnew/payitil/report";
    public static final String SANDBOX_SHORTURL_URL_SUFFIX = "/sandboxnew/tools/shorturl";
    public static final String SANDBOX_AUTHCODETOOPENID_URL_SUFFIX = "/sandboxnew/tools/authcodetoopenid";

    //filed
    public static final String FIELD_SIGN = "sign";
    public static final String FIELD_SIGN_TYPE = "sign_type";
    public static final String FIELD_APPID = "appid";
    public static final String FIELD_MCH_ID = "mch_id";
    public static final String FIELD_NONCE_STR = "nonce_str";
    public static final String FIELD_DETAIL = "detail";
    public static final String FIELD_BODY = "body";
    public static final String FIELD_OUT_TRADE_NO = "out_trade_no";
    public static final String FIELD_TOTAL_FEE = "total_fee";
    public static final String FIELD_REFUND_FEE = "refund_fee";
    public static final String FIELD_SPBILL_CREATE_IP = "spbill_create_ip";
    public static final String FIELD_TIME_START = "time_start";
    public static final String FIELD_TIME_EXPIRE = "time_expire";
    public static final String FIELD_NOTIFY_URL = "notify_url";
    public static final String FIELD_TRADE_TYPE = "trade_type";

    public static final String FIELD_RETURN_CODE = "return_code";
    public static final String FIELD_RETURN_MSG = "return_msg";
    public static final String FIELD_RESULT_CODE = "result_code";
    public static final String FIELD_ERR_CODE = "err_code";
    public static final String FIELD_ERR_CODE_DES = "err_code_des";
    public static final String FIELD_PREPAY_ID = "prepay_id";

    public static final String FIELD_TRADE_STATE = "trade_state";
    public static final String FIELD_TRANSACTION_ID = "transaction_id";
    public static final String FIELD_TIME_END = "time_end";
    public static final String FIELD_BANK_TYPE = "bank_type";
    public static final String FIELD_SETTLEMENT_TOTAL_FEE = "settlement_total_fee";

    public static final String FIELD_OUT_REFUND_NO = "out_refund_no";
    public static final String FIELD_OUT_REFUND_NO_0 = "out_refund_no_0";
    public static final String FIELD_REFUND_ID_0 = "refund_id_0";
    public static final String FIELD_REFUND_CHANNEL_0 = "refund_channel_0";
    public static final String FIELD_REFUND_FEE_0 = "refund_fee_0";
    public static final String FIELD_REFUND_STATUS_0 = "refund_status_0";
    public static final String FIELD_REFUND_STATUS = "refund_status";
    public static final String FIELD_REFUND_SUCCESS_TIME_0 = "refund_success_time_0";
    public static final String FIELD_SUCCESS_TIME = "success_time";
    public static final String FIELD_REFUND_DESC = "refund_desc";
    public static final String FIELD_REFUND_ID = "refund_id";
    public static final String FIELD_REQ_INFO = "req_info";
    public static final String FIELD_CONTRACT_CODE = "contract_code";
    public static final String FIELD_CONTRACT_DISPLAY_ACCOUNT = "contract_display_account";
    public static final String FIELD_OPENID = "openid";
    public static final String FIELD_TIMESTAMP = "timestamp";
    public static final String FIELD_CHANGE_TYPE = "change_type";
    public static final String FIELD_OPERATE_TIME = "operate_time";
    public static final String FIELD_CONTRACT_ID = "contract_id";
    public static final String FIELD_CONTRACT_EXPIRED_TIME = "contract_expired_time";
    public static final String FIELD_CONTRACT_TERMINATION_MODE = "contract_termination_mode";
    public static final String FIELD_REQUEST_SERIAL = "request_serial";
    public static final String FIELD_PLAN_ID = "plan_id";
    public static final String FIELD_VERSION = "version";
    public static final String FIELD_CONTRACT_TERMINATION_REMARK = "contract_termination_remark";
    public static final String FIELD_CONTRACT_STATE = "contract_state";
    public static final String FIELD_CONTRACT_SIGNED_TIME = "contract_signed_time";
    public static final String FIELD_CONTRACT_TERMINATED_TIME = "contract_terminated_time";
    public static final String FIELD_PRE_ENTRUSTWEB_ID = "pre_entrustweb_id";


    //charset
    public static final String DEFAULT_CHARSET = "UTF-8";

    //returnCode
    public static final String RETURN_SUCCESS = "SUCCESS";
    public static final String RETURN_FAIL = "FAIL";
    public static final String REFUNDNOTEXIST = "REFUNDNOTEXIST";
    public static final String ORDERNOTEXIST = "ORDERNOTEXIST";
    public static final String ORDERCLOSED = "ORDERCLOSED";
    public static final String ORDERPAID = "ORDERPAID";
    public static final String BIZERR_NEED_RETRY = "BIZERR_NEED_RETRY";
    public static final String SYSTEMERROR = "SYSTEMERROR";
    public static final String TRADE_OVERDUE = "TRADE_OVERDUE";
    public static final String USER_ACCOUNT_ABNORMAL = "USER_ACCOUNT_ABNORMAL";
    public static final String INVALID_REQ_TOO_MUCH = "INVALID_REQ_TOO_MUCH";
    public static final String NOTENOUGH = "NOTENOUGH";
    public static final String INVALID_TRANSACTIONID = "INVALID_TRANSACTIONID";
    public static final String SIGNCONTRACT_NOT_EXIST = "-25";
    public static final String ORDER_ACCEPTED = "ORDER_ACCEPTED";
    public static final String ACCOUNTERROR = "ACCOUNTERROR"; //用户账户异常
    public static final String CONTRACT_NOT_EXIST = "CONTRACT_NOT_EXIST";//协议不存在，用户已解约
    public static final String RULELIMIT = "RULELIMIT"; //用户账户支付已达上限
    public static final String BANKERROR = "BANKERROR"; //支付银行卡所在行渠道维护中,用户支付银行暂时无法提供服务
    public static final String USER_NOT_EXIST = "USER_NOT_EXIST"; //用户账户注销




    //TRADE_TYPE
    public static final String TRADE_TYPE_APP = "APP";
    public static final String TRADE_TYPE_PAP = "PAP";

    //TRADE_STATE
    public static final String TRADE_STATE_SUCCESS = "SUCCESS";
    public static final String TRADE_STATE_REFUND = "REFUND";
    public static final String TRADE_STATE_NOTPAY = "NOTPAY";
    public static final String TRADE_STATE_CLOSED = "CLOSED";
    public static final String TRADE_STATE_REVOKED = "REVOKED";
    public static final String TRADE_STATE_USERPAYING = "USERPAYING";
    public static final String TRADE_STATE_PAYERROR = "PAYERROR";
    public static final String TRADE_STATE_ACCEPT = "ACCEPT";
    public static final String TRADE_STATE_PAY_FAIL = "PAY_FAIL";

    //REFUND_STATUS
    public static final String REFUND_STATUS_SUCCESS = "SUCCESS";
    public static final String REFUND_STATUS_REFUNDCLOSE = "REFUNDCLOSE";
    public static final String REFUND_STATUS_PROCESSING = "PROCESSING";
    public static final String REFUND_STATUS_CHANGE = "CHANGE";


    //CHANGE_TYPE
    public static final String CHANGE_TYPE_ADD= "ADD";
    public static final String CHANGE_TYPE_DELETE = "DELETE";



}

