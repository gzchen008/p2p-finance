DELETE FROM t_content_news_types WHERE NAME = '����';

ALTER TABLE t_users ADD authentication_id VARCHAR(100) NULL COMMENT 'ע�����ĵ�ID';
ALTER TABLE t_bids ADD project_introduction VARCHAR(1000) NULL COMMENT '��Ŀ����';
ALTER TABLE t_bids ADD company_info VARCHAR(1000) NULL COMMENT '�����ҵ��Ϣ';
ALTER TABLE t_bids ADD repayment_res VARCHAR(1000) NULL COMMENT '������Դ';
ALTER TABLE t_bids ADD risk_control VARCHAR(1000) NULL COMMENT '��ش�ʩ';
ALTER TABLE t_bids ADD about_risk VARCHAR(1000) NULL COMMENT '������ʾ';
ALTER TABLE t_bids ADD feeType DOUBLE(5,2) NULL DEFAULT '0.00' COMMENT '������'; 
ALTER TABLE t_bids ADD is_new BIT(1) NULL DEFAULT b'0' COMMENT '�Ƿ����ֱ꣬0��ʾ���ֱ�';
ALTER TABLE t_bids ADD repayment_tips VARCHAR(1000) NULL COMMENT '������ʾ';
ALTER TABLE t_bids ADD sell_time DATETIME NULL COMMENT '����ʱ��';
ALTER TABLE t_bids ADD qixi_date DATETIME NULL COMMENT '��Ϣ��';
ALTER TABLE t_bids ADD repayall_date DATETIME NULL COMMENT '������Ϣ��';
ALTER TABLE t_bids ADD moneyback_time DATETIME NULL COMMENT 'Ԥ���ʽ���ʱ��';
