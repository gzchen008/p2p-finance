DELETE FROM t_content_news_types WHERE NAME = '借款技巧';

ALTER TABLE t_users ADD authentication_id VARCHAR(100) NULL COMMENT '注册中心的ID';
ALTER TABLE t_bids ADD project_introduction VARCHAR(1000) NULL COMMENT '项目简述';
ALTER TABLE t_bids ADD company_info VARCHAR(1000) NULL COMMENT '相关企业信息';
ALTER TABLE t_bids ADD repayment_res VARCHAR(1000) NULL COMMENT '还款来源';
ALTER TABLE t_bids ADD risk_control VARCHAR(1000) NULL COMMENT '风控措施';
ALTER TABLE t_bids ADD about_risk VARCHAR(1000) NULL COMMENT '风险提示';
ALTER TABLE t_bids ADD feeType DOUBLE(5,2) NULL DEFAULT '0.00' COMMENT '手续费'; 
ALTER TABLE t_bids ADD is_new BIT(1) NULL DEFAULT b'0' COMMENT '是否新手标，0表示新手标';
ALTER TABLE t_bids ADD repayment_tips VARCHAR(1000) NULL COMMENT '还款提示';
ALTER TABLE t_bids ADD sell_time DATETIME NULL COMMENT '开售时间';
ALTER TABLE t_bids ADD qixi_date DATETIME NULL COMMENT '起息日';
ALTER TABLE t_bids ADD repayall_date DATETIME NULL COMMENT '还本结息日';
ALTER TABLE t_bids ADD moneyback_time DATETIME NULL COMMENT '预计资金到账时间';
