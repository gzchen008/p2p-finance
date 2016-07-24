DROP TABLE IF EXISTS t_red_packet_bill;
CREATE TABLE
    t_red_packet_bill
    (
        id INT NOT NULL AUTO_INCREMENT COMMENT '红包流水id',
        red_packet_id INT COMMENT '红包id',
        bill_no VARCHAR(50) COMMENT '红包订单号',
        open_id VARCHAR(50) COMMENT '用户微信openid',
        balance DECIMAL(13,2) DEFAULT '0.00' COMMENT '红包余额',
        amount DECIMAL(13,2) DEFAULT '0.00' COMMENT '领取红包的金额',
        add_time TIMESTAMP COMMENT '红包领取时间',
        return_code INT COMMENT '红包领取结果1.失败2.成功',
        return_message VARCHAR(100) COMMENT '返回信息',
        remark VARCHAR(500) COMMENT '备注',
        PRIMARY KEY (id)
    )
    ENGINE=InnoDB DEFAULT CHARSET=utf8