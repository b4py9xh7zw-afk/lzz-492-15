SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- 创建数据库
CREATE DATABASE IF NOT EXISTS `scaffolding_db` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE `scaffolding_db`;

-- 文件信息表
DROP TABLE IF EXISTS `file_info`;
CREATE TABLE `file_info` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `file_name` varchar(255) NOT NULL COMMENT '文件名称',
  `original_name` varchar(255) NOT NULL COMMENT '原始文件名',
  `file_path` varchar(500) NOT NULL COMMENT '文件路径',
  `file_size` bigint(20) DEFAULT '0' COMMENT '文件大小（字节）',
  `file_type` varchar(50) DEFAULT NULL COMMENT '文件类型',
  `file_extension` varchar(20) DEFAULT NULL COMMENT '文件扩展名',
  `upload_user_id` bigint(20) DEFAULT NULL COMMENT '上传人ID',
  `upload_user_name` varchar(50) DEFAULT NULL COMMENT '上传人姓名',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint(1) DEFAULT '0' COMMENT '逻辑删除标识（0-未删除，1-已删除）',
  PRIMARY KEY (`id`),
  KEY `idx_file_type` (`file_type`),
  KEY `idx_upload_user_id` (`upload_user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='文件信息表';

-- 工作管理表
DROP TABLE IF EXISTS `work`;
CREATE TABLE `work` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `work_name` varchar(100) NOT NULL COMMENT '工作名称',
  `work_content` text COMMENT '工作内容',
  `work_status` varchar(20) DEFAULT 'pending' COMMENT '工作状态（pending-待处理，in_progress-进行中，completed-已完成，cancelled-已取消）',
  `work_time` datetime DEFAULT NULL COMMENT '工作时间',
  `start_time` datetime DEFAULT NULL COMMENT '开始时间',
  `end_time` datetime DEFAULT NULL COMMENT '结束时间',
  `priority` varchar(20) DEFAULT 'normal' COMMENT '优先级（low-低，normal-普通，high-高，urgent-紧急）',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint(1) DEFAULT '0' COMMENT '逻辑删除标识（0-未删除，1-已删除）',
  PRIMARY KEY (`id`),
  KEY `idx_work_status` (`work_status`),
  KEY `idx_work_time` (`work_time`),
  KEY `idx_priority` (`priority`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='工作管理表';

-- 用户表
DROP TABLE IF EXISTS `user`;
CREATE TABLE `user` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `username` varchar(50) NOT NULL COMMENT '用户名（账号）',
  `password` varchar(100) NOT NULL COMMENT '密码（不加密）',
  `nickname` varchar(50) DEFAULT NULL COMMENT '昵称',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint(1) DEFAULT '0' COMMENT '逻辑删除标识（0-未删除，1-已删除）',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_username` (`username`),
  KEY `idx_username` (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- 插入默认admin账号
INSERT INTO `user` (`username`, `password`, `nickname`) VALUES ('admin', '123456', '管理员');

-- 招聘渠道表
DROP TABLE IF EXISTS `recruit_channel`;
CREATE TABLE `recruit_channel` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `channel_name` varchar(100) NOT NULL COMMENT '渠道名称',
  `channel_type` varchar(20) NOT NULL COMMENT '渠道类型（store_poster-门店海报，short_video-短视频投放，labor_agency-劳务中介，referral-熟人推荐）',
  `settle_price` decimal(10,2) NOT NULL DEFAULT '0.00' COMMENT '结算单价（元/每个真实到岗且稳定的人）',
  `status` varchar(20) NOT NULL DEFAULT 'active' COMMENT '渠道状态（active-合作中，paused-已暂停）',
  `pause_reason` varchar(500) DEFAULT NULL COMMENT '暂停原因',
  `pause_time` datetime DEFAULT NULL COMMENT '暂停时间',
  `contact_person` varchar(50) DEFAULT NULL COMMENT '联系人',
  `contact_phone` varchar(20) DEFAULT NULL COMMENT '联系电话',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint(1) DEFAULT '0' COMMENT '逻辑删除标识（0-未删除，1-已删除）',
  PRIMARY KEY (`id`),
  KEY `idx_channel_type` (`channel_type`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='招聘渠道表';

-- 招聘候选人表
DROP TABLE IF EXISTS `recruit_candidate`;
CREATE TABLE `recruit_candidate` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `candidate_name` varchar(50) NOT NULL COMMENT '候选人姓名',
  `phone` varchar(20) NOT NULL COMMENT '手机号',
  `channel_id` bigint(20) NOT NULL COMMENT '来源渠道ID',
  `position` varchar(100) DEFAULT NULL COMMENT '应聘岗位',
  `stage` varchar(20) NOT NULL DEFAULT 'registered' COMMENT '当前阶段（registered-已报名，interviewed-已面试，trial-试岗中，onboard-已到岗，retained-留任七天，resigned-已离职，eliminated-已淘汰）',
  `apply_time` datetime DEFAULT NULL COMMENT '报名时间',
  `interview_time` datetime DEFAULT NULL COMMENT '面试时间',
  `trial_time` datetime DEFAULT NULL COMMENT '试岗时间',
  `onboard_time` datetime DEFAULT NULL COMMENT '真实到岗时间',
  `retain_time` datetime DEFAULT NULL COMMENT '留任七天确认时间',
  `resign_time` datetime DEFAULT NULL COMMENT '离职时间',
  `resign_reason` varchar(500) DEFAULT NULL COMMENT '离职原因',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint(1) DEFAULT '0' COMMENT '逻辑删除标识（0-未删除，1-已删除）',
  PRIMARY KEY (`id`),
  KEY `idx_channel_id` (`channel_id`),
  KEY `idx_stage` (`stage`),
  KEY `idx_phone` (`phone`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='招聘候选人表';

-- 渠道质量回算快照表
DROP TABLE IF EXISTS `recruit_channel_stats`;
CREATE TABLE `recruit_channel_stats` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `channel_id` bigint(20) NOT NULL COMMENT '渠道ID',
  `stat_date` date NOT NULL COMMENT '回算日期',
  `registered_count` int(11) NOT NULL DEFAULT '0' COMMENT '报名人数',
  `interview_count` int(11) NOT NULL DEFAULT '0' COMMENT '面试人数',
  `trial_count` int(11) NOT NULL DEFAULT '0' COMMENT '试岗人数',
  `onboard_count` int(11) NOT NULL DEFAULT '0' COMMENT '真实到岗人数',
  `retained_count` int(11) NOT NULL DEFAULT '0' COMMENT '留任七天人数',
  `resigned_count` int(11) NOT NULL DEFAULT '0' COMMENT '离职人数',
  `arrival_rate` decimal(5,2) NOT NULL DEFAULT '0.00' COMMENT '到岗率（%）= 真实到岗 / 报名',
  `stability_rate` decimal(5,2) NOT NULL DEFAULT '0.00' COMMENT '稳定率（%）= 留任七天 / 真实到岗',
  `settle_amount` decimal(12,2) NOT NULL DEFAULT '0.00' COMMENT '回算结算金额（元）= 真实到岗 × 结算单价 × 稳定率',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint(1) DEFAULT '0' COMMENT '逻辑删除标识（0-未删除，1-已删除）',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_channel_date` (`channel_id`, `stat_date`),
  KEY `idx_stat_date` (`stat_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='渠道质量回算快照表';

-- 插入招聘渠道示例数据
INSERT INTO `recruit_channel` (`channel_name`, `channel_type`, `settle_price`, `status`, `contact_person`, `contact_phone`, `remark`) VALUES
('门店海报-人民路店', 'store_poster', 300.00, 'active', '王店长', '13800000001', '门店橱窗海报扫码报名'),
('短视频投放-抖音同城', 'short_video', 500.00, 'active', '李运营', '13800000002', '抖音同城招聘短视频投放'),
('劳务中介-汇通人力', 'labor_agency', 800.00, 'active', '张经理', '13800000003', '按到岗人数月结'),
('熟人推荐-员工内推', 'referral', 200.00, 'active', '陈主管', '13800000004', '老员工推荐新员工奖励');

-- 插入候选人示例数据（覆盖完整漏斗：报名→面试→试岗→到岗→留任七天→离职）
INSERT INTO `recruit_candidate` (`candidate_name`, `phone`, `channel_id`, `position`, `stage`, `apply_time`, `interview_time`, `trial_time`, `onboard_time`, `retain_time`, `resign_time`, `resign_reason`) VALUES
('刘一帆', '13911110001', 1, '门店店员', 'retained', '2026-08-01 09:00:00', '2026-08-02 10:00:00', '2026-08-04 09:00:00', '2026-08-06 09:00:00', '2026-08-13 18:00:00', NULL, NULL),
('陈小雨', '13911110002', 1, '门店店员', 'retained', '2026-08-03 14:00:00', '2026-08-04 11:00:00', '2026-08-06 09:00:00', '2026-08-08 09:00:00', '2026-08-15 18:00:00', NULL, NULL),
('赵铁柱', '13911110003', 1, '理货员', 'onboard', '2026-09-01 10:00:00', '2026-09-02 10:00:00', '2026-09-03 09:00:00', '2026-09-05 09:00:00', NULL, NULL, NULL),
('孙丽丽', '13911110004', 1, '收银员', 'interviewed', '2026-09-08 09:30:00', '2026-09-09 14:00:00', NULL, NULL, NULL, NULL, NULL, NULL),
('周文博', '13911110005', 1, '门店店员', 'registered', '2026-09-10 16:00:00', NULL, NULL, NULL, NULL, NULL, NULL, NULL),
('吴海涛', '13911110006', 2, '门店店员', 'retained', '2026-08-02 09:00:00', '2026-08-03 10:00:00', '2026-08-05 09:00:00', '2026-08-07 09:00:00', '2026-08-14 18:00:00', NULL, NULL),
('郑晓梅', '13911110007', 2, '促销员', 'resigned', '2026-08-02 10:00:00', '2026-08-03 11:00:00', '2026-08-05 09:00:00', '2026-08-07 09:00:00', NULL, '2026-08-10 12:00:00', '觉得工作强度太大，干了3天离职'),
('冯建军', '13911110008', 2, '理货员', 'resigned', '2026-08-05 09:00:00', '2026-08-06 10:00:00', '2026-08-08 09:00:00', '2026-08-10 09:00:00', NULL, '2026-08-12 18:00:00', '家里有事，主动离职'),
('褚建华', '13911110009', 2, '门店店员', 'trial', '2026-09-07 09:00:00', '2026-09-08 10:00:00', '2026-09-10 09:00:00', NULL, NULL, NULL, NULL),
('卫东来', '13911110010', 2, '收银员', 'eliminated', '2026-09-05 11:00:00', '2026-09-06 14:00:00', NULL, NULL, NULL, NULL, NULL),
('蒋鹏飞', '13911110011', 3, '仓库管理员', 'retained', '2026-08-01 08:00:00', '2026-08-02 09:00:00', '2026-08-03 09:00:00', '2026-08-05 09:00:00', '2026-08-12 18:00:00', NULL, NULL),
('沈国强', '13911110012', 3, '仓库管理员', 'retained', '2026-08-04 08:00:00', '2026-08-05 09:00:00', '2026-08-06 09:00:00', '2026-08-08 09:00:00', '2026-08-15 18:00:00', NULL, NULL),
('韩志明', '13911110013', 3, '配送员', 'resigned', '2026-08-06 08:00:00', '2026-08-07 09:00:00', '2026-08-08 09:00:00', '2026-08-10 09:00:00', NULL, '2026-08-11 17:00:00', '嫌工资低，入职1天离职'),
('杨秀英', '13911110014', 3, '配送员', 'interviewed', '2026-09-09 10:00:00', '2026-09-10 14:00:00', NULL, NULL, NULL, NULL, NULL, NULL),
('朱晓峰', '13911110015', 4, '门店店员', 'retained', '2026-08-03 09:00:00', '2026-08-04 10:00:00', '2026-08-05 09:00:00', '2026-08-07 09:00:00', '2026-08-14 18:00:00', NULL, NULL),
('秦浩然', '13911110016', 4, '收银员', 'retained', '2026-08-10 09:00:00', '2026-08-11 10:00:00', '2026-08-12 09:00:00', '2026-08-14 09:00:00', '2026-08-21 18:00:00', NULL, NULL),
('许文静', '13911110017', 4, '门店店员', 'onboard', '2026-09-02 09:00:00', '2026-09-03 10:00:00', '2026-09-04 09:00:00', '2026-09-06 09:00:00', NULL, NULL, NULL);

SET FOREIGN_KEY_CHECKS = 1;
