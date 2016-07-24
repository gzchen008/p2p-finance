DROP TABLE IF EXISTS t_red_packet_bill;
CREATE TABLE
    t_red_packet_bill
    (
        id INT NOT NULL AUTO_INCREMENT COMMENT '�����ˮid',
        red_packet_id INT COMMENT '���id',
        bill_no VARCHAR(50) COMMENT '���������',
        open_id VARCHAR(50) COMMENT '�û�΢��openid',
        balance DECIMAL(13,2) DEFAULT '0.00' COMMENT '������',
        amount DECIMAL(13,2) DEFAULT '0.00' COMMENT '��ȡ����Ľ��',
        add_time TIMESTAMP COMMENT '�����ȡʱ��',
        return_code INT COMMENT '�����ȡ���1.ʧ��2.�ɹ�',
        return_message VARCHAR(100) COMMENT '������Ϣ',
        remark VARCHAR(500) COMMENT '��ע',
        PRIMARY KEY (id)
    )
    ENGINE=InnoDB DEFAULT CHARSET=utf8