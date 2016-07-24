package controllers.app.common;

/***
 * XXX_XXXX
 *
 * _前为业务类型
 * _后4位为数字：第一位：0(info) 1(warn) 2(error业务逻辑) 3(fatal系统异常)
 *             第二位：业务代码
 *                    0 公用部分
 *                    1 注册登录
 *                    2 账户中心
 *
 *                    4 交易
 *                    5 p2p
 */
public enum MsgCode {

    //operation platform
    OPERATE_SUCC("0000", "操作成功", ""),
	CREATE_SUCCESS("0001", "创建成功", ""),
    UPDATE_SUCCESS("0002", "更新成功", ""),
    DELETE_SUCCESS("0003", "删除成功", ""),
    OPERATE_WARNING("1000", "警告", ""),
    
    //p2p app模块
    LOAN_BID_QUERY_SUCC("0501","查询借款标列表成功"),
    LOAN_BID_DETAIL_QUERY_SUCC("0502","查询借款标详情成功"),
    LOAN_BID_PROJECT_DETAIL_QUERY_SUCC("0503","查询项目详情成功"),
    LOAN_BID_FUND_SECURITY_QUERY_SUCC("0504","查询资金安全详情成功"),
    LOAN_BID_INVEST_RECORDS_SUCC("0505","查询投标记录详情成功"),
    LOAN_BID_SHOW_QUERY_SUCC("0506","查询首页展示标的成功"),
    LOAN_BID_APR_CALCULATOR_SUCC("0507","查询利率计算器成功"),
    LOAN_BID_RETURN_MODE_SUCC("0508","查询收益获取成功"),
    AUTH_TOKEN_SUCC("0509","p2p授权登录成功"),
    AUTH_TOKEN_CLEAN_SUCC("0510","AuthToken清除成功"),
    SAVE_USER_INFO_SUCC("0511","完善用户资料成功"),
    QUERY_ACC_BALANCE_SUCC("0512", "账户余额查询成功"),
    SEARCH_INVEST_SUCC("0513", "投标查询成功"),
    ALL_INVEST_SUCC("0514", "全投成功"),
    ENCHASH_SUCC("0515", "取现成功"),
    SOCIAL_LOGIN_SUCC("0516", "社交登录成功"),
    SOCIAL_BINDING_SUCC("0517", "社交绑定成功"),
    BID_SHOW_QUERY_SUCC("0518","投资记录查询成功"),
    USER_INVEST_QUERY_SUCC("0519","个人财富查询成功"),



    ACCESS_FAIL("2001", "访问失败", "传入参数不能为空"),
    PARAMETER_ERROR("2001", "传入参数有误"),
    CURRENT_USER_FAIL("2500","未获取到当前用户", "请登录后再试"),

    LOAN_BID_QUERY_FAIL("2501","查询借款标列表失败"),
    LOAN_BID_DETAIL_QUERY_ID_FAIL("2502","借款id有误"),
    LOAN_BID_DETAIL_QUERY_USERID_FAIL("2503","解析用户id有误"),
    LOAN_BID_DETAIL_QUERY_ERROR("2504","查询出现异常，给您带来的不便敬请谅解"),
    LOAN_BID_SHOW_QUERY_FAIL("2505","查询首页展示标的失败"),
    LOAN_BID_APR_CALCULATOR_FAIL("2506","查询利率计算器失败"),
    AUTH_TOKEN_FAIL("2507","p2p授权登录失败"),
    SAVE_USER_INFO_FAIL("2508","完善用户资料失败"),
    RECHARGE_ERROR("2509", "请输入正确的充值金额"),
    QUERY_ACC_BALANCE_FAIL("2510", "账户余额查询失败"),
    CONFIRM_INVEST_FAIL("2511", "确认投标失败"),
    ENCHASH_ERROR("2512", "取现失败"),
    SOCIAL_LOGIN_FAIL("2513", "社交登录失败"),
    SOCIAL_BINDING_FAIL("2514", "社交绑定失败"),
    BID_SHOW_QUERY_FALL("2515","投资记录查询失败"),
    USER_INVEST_QUERY_FALL("2516","个人财富查询失败"),





    ;





    private String code;
    private String message;
    private String detail;

    private MsgCode(String code, String message){
        this(code, message,"");
    }

    private MsgCode(String code, String message, String detail){
        this.code = code;
        this.message = message;
        this.detail = detail;
    }

    public String getCode(){
        return this.code;
    }

    public String getMessage(){
        return this.message;
    }

    public String getDetail(){
        return this.detail;
    }

    public static String getDescByCode(String code) {
        if(code == null) {
            return null;
        }
        for(MsgCode msgCode : MsgCode.values()) {
            if(code.equals(msgCode.getCode())) {
                return msgCode.getMessage();
            }
        }
        return null;
    }
}
