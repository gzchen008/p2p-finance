DROP TABLE IF EXISTS t_red_packet;
CREATE TABLE
    t_red_packet
    (
        id INT NOT NULL AUTO_INCREMENT COMMENT '���id',
        balance DECIMAL(13,2) DEFAULT '0.00' COMMENT '������',
        total DECIMAL(13,2) DEFAULT '0.00' COMMENT '����ܽ��',
        send DECIMAL(13,2) DEFAULT '0.00' COMMENT '�Ѿ����ͺ�����',
        create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON  UPDATE CURRENT_TIMESTAMP COMMENT '����ʱ��',
        min_value DECIMAL(13,2) DEFAULT '0.00' COMMENT '�����Сֵ',
        max_value DECIMAL(13,2) DEFAULT '0.00' COMMENT '������ֵ',
        total_num INT DEFAULT '0' COMMENT '����������',
        send_num INT DEFAULT '0' COMMENT '�Ѿ���������',
        act_name VARCHAR(35) COMMENT '�������',
        remark VARCHAR(50) COMMENT '���ע',
        logo_img_url VARCHAR(110) COMMENT 'logoͼƬ',
        content VARCHAR(110) COMMENT '�����',
        share_img_url VARCHAR(110) COMMENT '����ͼƬ����',
        share_url VARCHAR(110) COMMENT '��������',
        wishing VARCHAR(110) COMMENT 'ף����',
        status VARCHAR(5) COMMENT '״̬��0���رգ�1������',
        couple INT(2) COMMENT '��ȡ����',
        PRIMARY KEY (id)
    )
    ENGINE=InnoDB DEFAULT CHARSET=utf8