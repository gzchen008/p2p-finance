package vo;

import java.io.Serializable;

/**
 * <p>Project: com.shovesoft.sp2p</p>
 * <p>Title: BidInvestInfoVo.java</p>
 * <p>Description: </p>
 * <p>Copyright (c) 2014 Sunlights.cc</p>
 * <p>All Rights Reserved.</p>
 *
 * @author <a href="mailto:jiaming.wang@sunlights.cc">wangJiaMing</a>
 */
public class BidInvestInfoVo implements Serializable{
    public Long id;
    public Long bidId;//标的ID
    public String title;//标题
    public Long mainTypeId;//产品类型 1浮动类 2其它
    public String investAmounts;//投资金额
    public String investUnit;//投资金额单位
    public String incomeAmounts;//收益金额
    public String incomeDesc;//收益描述
    public int status;//状态
    public String repaymentTime;//到期时间
    public String quotient;//当前份额
    public String netvalue;//净值



}
