CREATE TABLE `t_m_products` (  `id` INT(11) NOT NULL AUTO_INCREMENT COMMENT '母产品ID',  `time` TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP COMMENT '添加时间',  `name` VARCHAR(100) DEFAULT NULL COMMENT '产品简称',
  `main_type_id` TINYINT(11) DEFAULT '0' COMMENT '产品主类型,字典类型',
  `sub_type_id` TINYINT(11) DEFAULT '0' COMMENT '产品子类型,字典类型',
  `project_name` VARCHAR(100) DEFAULT NULL COMMENT '项目名称',
  `project_code` VARCHAR(100) DEFAULT NULL COMMENT '项目编码',
  `total_amount` DECIMAL(20,2) DEFAULT '0.00' COMMENT '项目融资总额',
  `loaner_name` VARCHAR(100) DEFAULT NULL COMMENT '项目融资方',
  `project_introduction` VARCHAR(1000) DEFAULT NULL COMMENT '项目简述',
  `project_detail` VARCHAR(2000) DEFAULT NULL COMMENT '项目详情',
  `project_image_filename` VARCHAR(200) DEFAULT NULL COMMENT '项目名称图片',
  `capital_usage` VARCHAR(1000) DEFAULT NULL COMMENT '项目资金用途',
  `repayment_res` VARCHAR(1000) DEFAULT NULL COMMENT '还款来源',
  `risk_control` VARCHAR(2000) DEFAULT NULL COMMENT '主要风控条款',
  `security_guarantee` VARCHAR(2000) DEFAULT NULL COMMENT '安全保障',  PRIMARY KEY (`id`),  KEY `index_m_pro_id` (`id`) USING BTREE) ENGINE=INNODB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8 COMMENT='母产品信息表';

ALTER TABLE t_bids ADD m_product_id BIGINT(20) DEFAULT '0' COMMENT '母产品类型Id';
ALTER TABLE t_bids ADD main_type_id BIGINT(20) DEFAULT '0' COMMENT '产品主类型,字典类型';
ALTER TABLE t_bids ADD sub_type_id  BIGINT(20) DEFAULT '0' COMMENT '产品子类型,字典类型';

ALTER TABLE t_bids ADD NAME  VARCHAR(100) DEFAULT NULL COMMENT '产品简称';
ALTER TABLE t_bids ADD project_name  VARCHAR(100) DEFAULT NULL COMMENT '项目名称';
ALTER TABLE t_bids ADD project_code  VARCHAR(100) DEFAULT NULL COMMENT '项目编码';
ALTER TABLE t_bids ADD loaner_name VARCHAR(100) DEFAULT NULL COMMENT '项目融资方';
ALTER TABLE t_bids ADD project_detail VARCHAR(2000) DEFAULT NULL COMMENT '项目详情';
ALTER TABLE t_bids ADD capital_usage VARCHAR(1000) DEFAULT NULL COMMENT '项目资金用途';
ALTER TABLE t_bids ADD security_guarantee VARCHAR(2000) DEFAULT NULL COMMENT '安全保障';
ALTER TABLE t_bids ADD profit_guarantee VARCHAR(2000) DEFAULT NULL COMMENT '收益保障';
ALTER TABLE t_bids ADD supervise_bank VARCHAR(2000) DEFAULT NULL COMMENT '监管银行';

CREATE TABLE `t_dict_mainproducts` (
  `id` INT(11) NOT NULL AUTO_INCREMENT COMMENT '产品主类型ID',
  `name` VARCHAR(50) DEFAULT NULL COMMENT '名称',
  `code` VARCHAR(50) DEFAULT NULL COMMENT '代码',
  `description` VARCHAR(200) DEFAULT NULL COMMENT '描述',
  `is_use` BIT(1) DEFAULT b'1' COMMENT '是否使用',
  PRIMARY KEY (`id`),
  KEY `is_use` (`is_use`),
  KEY `index_mainproducts_id` (`id`) USING BTREE
) ENGINE=INNODB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8 COMMENT='产品主类型字典表';

INSERT INTO `t_dict_mainproducts` (`id`, `name`, `code`, `description`, `is_use`) VALUES('1','浮动型',NULL,NULL,'');
INSERT INTO `t_dict_mainproducts` (`id`, `name`, `code`, `description`, `is_use`) VALUES('2','固定收益型',NULL,NULL,'');


CREATE TABLE `t_dict_subproducts` (
  `id` INT(11) NOT NULL AUTO_INCREMENT COMMENT '产品子类型ID',
  `main_id` INT(11) NOT NULL COMMENT '隶属产品主类型ID',
  `name` VARCHAR(50) DEFAULT NULL COMMENT '名称',
  `code` VARCHAR(50) DEFAULT NULL COMMENT '代码',
  `description` VARCHAR(200) DEFAULT NULL COMMENT '描述',
  `is_use` BIT(1) DEFAULT b'1' COMMENT '是否使用',
  PRIMARY KEY (`id`),
  KEY `is_use` (`is_use`),
  KEY `index_subproducts_id` (`id`) USING BTREE
) ENGINE=INNODB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8 COMMENT='产品子类型字典表';

INSERT INTO `t_dict_subproducts` (`id`, `main_id`, `name`, `code`, `description`, `is_use`) VALUES('1','1','阳光私募',NULL,NULL,'');
INSERT INTO `t_dict_subproducts` (`id`, `main_id`, `name`, `code`, `description`, `is_use`) VALUES('2','1','海外',NULL,NULL,'');
INSERT INTO `t_dict_subproducts` (`id`, `main_id`, `name`, `code`, `description`, `is_use`) VALUES('3','1','对冲基金',NULL,NULL,'');
INSERT INTO `t_dict_subproducts` (`id`, `main_id`, `name`, `code`, `description`, `is_use`) VALUES('4','1','基金专户',NULL,NULL,'');
INSERT INTO `t_dict_subproducts` (`id`, `main_id`, `name`, `code`, `description`, `is_use`) VALUES('5','1','资管类',NULL,NULL,'');
INSERT INTO `t_dict_subproducts` (`id`, `main_id`, `name`, `code`, `description`, `is_use`) VALUES('6','1','信托类',NULL,NULL,'');
INSERT INTO `t_dict_subproducts` (`id`, `main_id`, `name`, `code`, `description`, `is_use`) VALUES('7','1','公募基金',NULL,NULL,'');
INSERT INTO `t_dict_subproducts` (`id`, `main_id`, `name`, `code`, `description`, `is_use`) VALUES('8','1','现金管理类',NULL,NULL,'');
INSERT INTO `t_dict_subproducts` (`id`, `main_id`, `name`, `code`, `description`, `is_use`) VALUES('9','1','打包类产品',NULL,NULL,'');

INSERT INTO `t_dict_subproducts` (`id`, `main_id`, `name`, `code`, `description`, `is_use`) VALUES('10','2','债权类',NULL,NULL,'');
INSERT INTO `t_dict_subproducts` (`id`, `main_id`, `name`, `code`, `description`, `is_use`) VALUES('11','2','新手产品',NULL,NULL,'');
INSERT INTO `t_dict_subproducts` (`id`, `main_id`, `name`, `code`, `description`, `is_use`) VALUES('12','2','阳光私募',NULL,NULL,'');
INSERT INTO `t_dict_subproducts` (`id`, `main_id`, `name`, `code`, `description`, `is_use`) VALUES('13','2','短期理财',NULL,NULL,'');
INSERT INTO `t_dict_subproducts` (`id`, `main_id`, `name`, `code`, `description`, `is_use`) VALUES('14','2','资管类',NULL,NULL,'');
INSERT INTO `t_dict_subproducts` (`id`, `main_id`, `name`, `code`, `description`, `is_use`) VALUES('15','2','信托类',NULL,NULL,'');
INSERT INTO `t_dict_subproducts` (`id`, `main_id`, `name`, `code`, `description`, `is_use`) VALUES('16','2','打包类产品',NULL,NULL,'');
INSERT INTO `t_dict_subproducts` (`id`, `main_id`, `name`, `code`, `description`, `is_use`) VALUES('17','2','票据类',NULL,NULL,'');
