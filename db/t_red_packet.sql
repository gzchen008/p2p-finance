DROP TABLE IF EXISTS t_red_packet;
CREATE TABLE
    t_red_packet
    (
        id INT NOT NULL AUTO_INCREMENT COMMENT '红包id',
        balance DECIMAL(13,2) DEFAULT '0.00' COMMENT '红包余额',
        total DECIMAL(13,2) DEFAULT '0.00' COMMENT '红包总金额',
        send DECIMAL(13,2) DEFAULT '0.00' COMMENT '已经发送红包金额',
        create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON  UPDATE CURRENT_TIMESTAMP COMMENT '创建时间',
        min_value DECIMAL(13,2) DEFAULT '0.00' COMMENT '红包最小值',
        max_value DECIMAL(13,2) DEFAULT '0.00' COMMENT '红包最大值',
        total_num INT DEFAULT '0' COMMENT '发送总人数',
        send_num INT DEFAULT '0' COMMENT '已经发送人数',
        act_name VARCHAR(35) COMMENT '活动的名字',
        remark VARCHAR(50) COMMENT '活动备注',
        logo_img_url VARCHAR(110) COMMENT 'logo图片',
        content VARCHAR(110) COMMENT '活动内容',
        share_img_url VARCHAR(110) COMMENT '分享图片链接',
        share_url VARCHAR(110) COMMENT '分享链接',
        wishing VARCHAR(110) COMMENT '祝福语',
        status VARCHAR(5) COMMENT '状态，0：关闭，1：开启',
        couple INT(2) COMMENT '领取次数',
        PRIMARY KEY (id)
    )
    ENGINE=InnoDB DEFAULT CHARSET=utf8