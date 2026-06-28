CREATE DATABASE IF NOT EXISTS flexible_employment
  DEFAULT CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;

USE flexible_employment;

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

DROP TABLE IF EXISTS company_review;
DROP TABLE IF EXISTS labor_contract;
DROP TABLE IF EXISTS social_payment_record;
DROP TABLE IF EXISTS social_security_apply;
DROP TABLE IF EXISTS message;
DROP TABLE IF EXISTS policy_apply;
DROP TABLE IF EXISTS policy;
DROP TABLE IF EXISTS notice;
DROP TABLE IF EXISTS favorite_job;
DROP TABLE IF EXISTS employment_pref;
DROP TABLE IF EXISTS application;
DROP TABLE IF EXISTS resume;
DROP TABLE IF EXISTS job;
DROP TABLE IF EXISTS admin;
DROP TABLE IF EXISTS company;
DROP TABLE IF EXISTS user;

SET FOREIGN_KEY_CHECKS = 1;

CREATE TABLE user (
  id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '用户ID',
  username VARCHAR(50) NOT NULL UNIQUE COMMENT '用户名',
  password VARCHAR(100) NOT NULL COMMENT '密码',
  real_name VARCHAR(50) COMMENT '真实姓名',
  gender TINYINT DEFAULT 0 COMMENT '性别：0未知 1男 2女',
  phone VARCHAR(20) COMMENT '手机号',
  email VARCHAR(100) COMMENT '邮箱',
  id_card VARCHAR(30) COMMENT '身份证号',
  avatar VARCHAR(255) COMMENT '头像地址',
  status TINYINT DEFAULT 1 COMMENT '状态：0禁用 1正常',
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

CREATE TABLE company (
  id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '企业ID',
  username VARCHAR(50) NOT NULL UNIQUE COMMENT '登录用户名',
  password VARCHAR(100) NOT NULL COMMENT '密码',
  company_name VARCHAR(100) NOT NULL COMMENT '企业名称',
  credit_code VARCHAR(50) COMMENT '统一社会信用代码',
  legal_person VARCHAR(50) COMMENT '法人代表',
  contact_name VARCHAR(50) COMMENT '联系人',
  contact_phone VARCHAR(20) COMMENT '联系电话',
  email VARCHAR(100) COMMENT '企业邮箱',
  address VARCHAR(255) COMMENT '企业地址',
  industry VARCHAR(50) COMMENT '所属行业',
  description TEXT COMMENT '企业简介',
  license_url VARCHAR(255) COMMENT '营业执照地址',
  audit_status TINYINT DEFAULT 0 COMMENT '审核状态：0待审核 1通过 2拒绝',
  audit_remark VARCHAR(255) COMMENT '审核备注',
  status TINYINT DEFAULT 1 COMMENT '账号状态：0禁用 1正常',
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='企业表';

CREATE TABLE admin (
  id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '管理员ID',
  username VARCHAR(50) NOT NULL UNIQUE COMMENT '用户名',
  password VARCHAR(100) NOT NULL COMMENT '密码',
  real_name VARCHAR(50) COMMENT '姓名',
  phone VARCHAR(20) COMMENT '手机号',
  role VARCHAR(50) DEFAULT 'ADMIN' COMMENT '角色',
  status TINYINT DEFAULT 1 COMMENT '状态：0禁用 1正常',
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='管理员表';

CREATE TABLE job (
  id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '岗位ID',
  company_id BIGINT NOT NULL COMMENT '企业ID',
  title VARCHAR(100) NOT NULL COMMENT '岗位名称',
  industry VARCHAR(50) COMMENT '所属行业',
  job_type VARCHAR(50) COMMENT '岗位类型',
  salary_min DECIMAL(10,2) COMMENT '最低薪资',
  salary_max DECIMAL(10,2) COMMENT '最高薪资',
  salary_unit VARCHAR(20) DEFAULT '月' COMMENT '薪资单位：时/月/单',
  work_city VARCHAR(50) COMMENT '工作城市',
  work_address VARCHAR(255) COMMENT '工作地址',
  description TEXT COMMENT '岗位描述',
  requirement TEXT COMMENT '岗位要求',
  recruit_count INT DEFAULT 1 COMMENT '招聘人数',
  view_count INT DEFAULT 0 COMMENT '浏览次数',
  audit_status TINYINT DEFAULT 0 COMMENT '审核状态：0待审核 1通过 2拒绝',
  audit_remark VARCHAR(255) COMMENT '审核备注',
  status TINYINT DEFAULT 1 COMMENT '岗位状态：0下架 1上架',
  publish_time DATETIME COMMENT '发布时间',
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  INDEX idx_job_company_id (company_id),
  INDEX idx_job_audit_status (audit_status),
  INDEX idx_job_city_industry (work_city, industry)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='岗位表';

CREATE TABLE resume (
  id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '简历ID',
  user_id BIGINT NOT NULL COMMENT '用户ID',
  title VARCHAR(100) COMMENT '简历标题',
  real_name VARCHAR(50) COMMENT '姓名',
  gender TINYINT DEFAULT 0 COMMENT '性别：0未知 1男 2女',
  birthday DATE COMMENT '出生日期',
  education VARCHAR(50) COMMENT '学历',
  work_years INT COMMENT '工作年限',
  phone VARCHAR(20) COMMENT '联系电话',
  email VARCHAR(100) COMMENT '邮箱',
  expected_position VARCHAR(100) COMMENT '期望岗位',
  expected_city VARCHAR(50) COMMENT '期望城市',
  self_intro TEXT COMMENT '自我介绍',
  attachment_url VARCHAR(255) COMMENT '简历附件地址',
  audit_status TINYINT DEFAULT 1 COMMENT '审核状态：0待审核 1通过 2拒绝',
  audit_remark VARCHAR(255) COMMENT '审核备注',
  status TINYINT DEFAULT 1 COMMENT '状态：0删除 1正常',
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  INDEX idx_resume_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='简历表';

CREATE TABLE application (
  id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '投递ID',
  user_id BIGINT NOT NULL COMMENT '用户ID',
  job_id BIGINT NOT NULL COMMENT '岗位ID',
  company_id BIGINT NOT NULL COMMENT '企业ID',
  resume_id BIGINT NOT NULL COMMENT '简历ID',
  status TINYINT DEFAULT 0 COMMENT '状态：0待处理 1已查看 2通过 3拒绝 4已撤回',
  apply_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '投递时间',
  handle_time DATETIME COMMENT '处理时间',
  remark VARCHAR(255) COMMENT '处理备注',
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  UNIQUE KEY uk_application_user_job (user_id, job_id),
  INDEX idx_application_user_id (user_id),
  INDEX idx_application_company_id (company_id),
  INDEX idx_application_job_id (job_id),
  INDEX idx_application_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='简历投递表';

CREATE TABLE employment_pref (
  id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '偏好ID',
  user_id BIGINT NOT NULL UNIQUE COMMENT '用户ID',
  industry VARCHAR(50) COMMENT '期望行业',
  position VARCHAR(100) COMMENT '期望岗位',
  salary_min DECIMAL(10,2) COMMENT '期望最低薪资',
  salary_max DECIMAL(10,2) COMMENT '期望最高薪资',
  work_city VARCHAR(50) COMMENT '期望城市',
  job_type VARCHAR(50) COMMENT '期望岗位类型',
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='求职偏好表';

CREATE TABLE favorite_job (
  id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '收藏ID',
  user_id BIGINT NOT NULL COMMENT '用户ID',
  job_id BIGINT NOT NULL COMMENT '岗位ID',
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '收藏时间',
  UNIQUE KEY uk_favorite_user_job (user_id, job_id),
  INDEX idx_favorite_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='岗位收藏表';

CREATE TABLE notice (
  id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '公告ID',
  title VARCHAR(100) NOT NULL COMMENT '标题',
  content TEXT NOT NULL COMMENT '内容',
  type TINYINT DEFAULT 1 COMMENT '类型：1公告 2通知 3就业资讯',
  is_top TINYINT DEFAULT 0 COMMENT '是否置顶：0否 1是',
  status TINYINT DEFAULT 1 COMMENT '状态：0下架 1发布',
  create_by BIGINT COMMENT '创建管理员ID',
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  INDEX idx_notice_status_top (status, is_top)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='公告通知表';

CREATE TABLE policy (
  id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '政策ID',
  title VARCHAR(100) NOT NULL COMMENT '政策标题',
  content TEXT NOT NULL COMMENT '政策内容',
  apply_condition TEXT COMMENT '申报条件',
  material_required TEXT COMMENT '所需材料',
  status TINYINT DEFAULT 1 COMMENT '状态：0下架 1发布',
  create_by BIGINT COMMENT '创建管理员ID',
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='政策服务表';

CREATE TABLE policy_apply (
  id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '申报ID',
  user_id BIGINT NOT NULL COMMENT '用户ID',
  policy_id BIGINT NOT NULL COMMENT '政策ID',
  applicant_name VARCHAR(50) COMMENT '申请人姓名',
  phone VARCHAR(20) COMMENT '联系电话',
  material_url VARCHAR(255) COMMENT '材料附件地址',
  status TINYINT DEFAULT 0 COMMENT '状态：0待审核 1通过 2拒绝',
  audit_remark VARCHAR(255) COMMENT '审核备注',
  apply_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '申请时间',
  audit_time DATETIME COMMENT '审核时间',
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  INDEX idx_policy_apply_user_id (user_id),
  INDEX idx_policy_apply_policy_id (policy_id),
  INDEX idx_policy_apply_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='政策申报表';

CREATE TABLE message (
  id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '消息ID',
  sender_type TINYINT NOT NULL COMMENT '发送者类型：1用户 2企业 3管理员',
  sender_id BIGINT NOT NULL COMMENT '发送者ID',
  receiver_type TINYINT NOT NULL COMMENT '接收者类型：1用户 2企业 3管理员',
  receiver_id BIGINT NOT NULL COMMENT '接收者ID',
  title VARCHAR(100) COMMENT '消息标题',
  content TEXT NOT NULL COMMENT '消息内容',
  is_read TINYINT DEFAULT 0 COMMENT '是否已读：0否 1是',
  status TINYINT DEFAULT 1 COMMENT '状态：0删除 1正常',
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  INDEX idx_message_receiver (receiver_type, receiver_id, is_read),
  INDEX idx_message_sender (sender_type, sender_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='消息表';

CREATE TABLE social_security_apply (
  id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '社保申请ID',
  user_id BIGINT NOT NULL COMMENT '用户ID',
  applicant_name VARCHAR(50) COMMENT '申请人姓名',
  id_card VARCHAR(30) COMMENT '身份证号',
  phone VARCHAR(20) COMMENT '联系电话',
  insurance_type VARCHAR(50) COMMENT '参保类型',
  material_url VARCHAR(255) COMMENT '材料附件地址',
  status TINYINT DEFAULT 0 COMMENT '状态：0待审核 1通过 2拒绝',
  audit_remark VARCHAR(255) COMMENT '审核备注',
  apply_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '申请时间',
  audit_time DATETIME COMMENT '审核时间',
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  INDEX idx_social_apply_user_id (user_id),
  INDEX idx_social_apply_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='社保参保申请表';

CREATE TABLE social_payment_record (
  id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '缴费记录ID',
  user_id BIGINT NOT NULL COMMENT '用户ID',
  bill_no VARCHAR(50) NOT NULL UNIQUE COMMENT '账单编号',
  payment_month VARCHAR(20) COMMENT '缴费月份',
  amount DECIMAL(10,2) NOT NULL COMMENT '缴费金额',
  status TINYINT DEFAULT 0 COMMENT '缴费状态：0待缴费 1已缴费',
  pay_time DATETIME COMMENT '缴费时间',
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  INDEX idx_payment_user_id (user_id),
  INDEX idx_payment_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='社保缴费记录表';

CREATE TABLE labor_contract (
  id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '合同ID',
  user_id BIGINT NOT NULL COMMENT '用户ID',
  company_id BIGINT NOT NULL COMMENT '企业ID',
  job_id BIGINT COMMENT '岗位ID',
  contract_no VARCHAR(50) NOT NULL UNIQUE COMMENT '合同编号',
  title VARCHAR(100) COMMENT '合同标题',
  start_date DATE COMMENT '开始日期',
  end_date DATE COMMENT '结束日期',
  file_url VARCHAR(255) COMMENT '合同文件地址',
  status TINYINT DEFAULT 1 COMMENT '状态：0失效 1有效',
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  INDEX idx_contract_user_id (user_id),
  INDEX idx_contract_company_id (company_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='电子劳动合同表';

CREATE TABLE company_review (
  id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '评价ID',
  user_id BIGINT NOT NULL COMMENT '用户ID',
  company_id BIGINT NOT NULL COMMENT '企业ID',
  job_id BIGINT COMMENT '岗位ID',
  score TINYINT NOT NULL COMMENT '评分：1-5',
  content VARCHAR(500) COMMENT '评价内容',
  status TINYINT DEFAULT 1 COMMENT '状态：0隐藏 1展示',
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  INDEX idx_review_company_id (company_id),
  INDEX idx_review_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='企业评价表';

INSERT INTO admin (id, username, password, real_name, phone, role, status) VALUES
(1, 'admin', '123456', '系统管理员', '13800000000', 'ADMIN', 1);

INSERT INTO user (id, username, password, real_name, gender, phone, email, id_card, status) VALUES
(1, 'user', '123456', '张三', 1, '13900000001', 'user@example.com', '410100199901010011', 1),
(2, 'lisi', '123456', '李四', 2, '13900000002', 'lisi@example.com', '410100199802020022', 1);

INSERT INTO company (id, username, password, company_name, credit_code, legal_person, contact_name, contact_phone, email, address, industry, description, audit_status, status) VALUES
(1, 'company', '123456', '中原便民服务有限公司', '91410100MA00000001', '王经理', '王经理', '13700000001', 'company@example.com', '郑州市高新区科学大道100号', '生活服务', '提供社区便民、配送、家政等灵活就业岗位。', 1, 1),
(2, 'techcorp', '123456', '郑州云创科技有限公司', '91410100MA00000002', '赵经理', '赵经理', '13700000002', 'tech@example.com', '郑州市郑东新区商务内环路88号', '互联网', '面向本地商户提供数字化运营服务。', 1, 1),
(3, 'datastar', '123456', '河南数智云服务有限公司', '91410100MA00000003', '刘经理', '刘经理', '13700000003', 'data@example.com', '郑州市金水区文化路60号', '互联网', '提供软件开发、数据处理和数字化外包服务。', 1, 1),
(4, 'eduservice', '123456', '豫才在线教育科技有限公司', '91410100MA00000004', '陈经理', '陈经理', '13700000004', 'edu@example.com', '郑州市二七区大学路20号', '教育培训', '面向学生提供线上课程辅导和职业技能培训服务。', 1, 1),
(5, 'mediahub', '123456', '中原新媒体运营中心', '91410100MA00000005', '孙经理', '孙经理', '13700000005', 'media@example.com', '郑州市管城区紫荆山路18号', '文化传媒', '提供短视频、直播运营和本地生活内容服务。', 1, 1),
(6, 'logistics', '123456', '豫达同城物流有限公司', '91410100MA00000006', '李经理', '李经理', '13700000006', 'logistics@example.com', '郑州市经开区航海东路168号', '物流配送', '提供同城配送、仓储分拣和即时物流服务。', 1, 1),
(7, 'aitech', '123456', '郑州未来智能科技有限公司', '91410100MA00000007', '周经理', '周经理', '13700000007', 'ai@example.com', '郑州市高新区长椿路11号', '互联网', '专注 AI 应用开发、智能客服、算法模型和企业数字化系统建设。', 1, 1);

INSERT INTO job (id, company_id, title, industry, job_type, salary_min, salary_max, salary_unit, work_city, work_address, description, requirement, recruit_count, view_count, audit_status, status, publish_time) VALUES
(1, 1, '社区配送员', '生活服务', '兼职', 3000.00, 6000.00, '月', '郑州', '郑州市高新区', '负责社区周边订单配送，工作时间灵活。', '身体健康，熟悉周边路线，有责任心。', 10, 120, 1, 1, NOW()),
(2, 1, '家政服务人员', '生活服务', '灵活用工', 80.00, 200.00, '单', '郑州', '郑州市中原区', '提供家庭保洁、整理收纳等服务。', '有相关经验者优先，服务态度良好。', 8, 86, 1, 1, NOW()),
(3, 2, '线上客服', '互联网', '兼职', 20.00, 35.00, '时', '郑州', '可居家办公', '负责线上咨询回复、订单跟进和信息登记。', '普通话流利，会使用办公软件。', 6, 64, 1, 1, NOW()),
(4, 2, '短视频运营助理', '互联网', '兼职', 3000.00, 5000.00, '月', '郑州', '郑州市郑东新区', '协助完成短视频账号内容发布和数据整理。', '了解短视频平台，有基础文案能力。', 3, 51, 0, 1, NOW()),
(5, 2, '前端开发实习生', '互联网', '实习', 2500.00, 4500.00, '月', '郑州', '郑州市郑东新区', '参与企业管理系统页面开发，负责 Vue 页面实现和接口联调。', '熟悉 HTML、CSS、JavaScript，了解 Vue3 优先。', 4, 98, 1, 1, NOW()),
(6, 3, 'Java开发实习生', '互联网', '实习', 3000.00, 5000.00, '月', '郑州', '郑州市金水区', '参与 Spring Boot 后端接口开发、数据库表设计和接口测试。', '熟悉 Java 基础、Spring Boot、MySQL，有课程设计经验优先。', 5, 112, 1, 1, NOW()),
(7, 3, '软件测试助理', '互联网', '兼职', 2500.00, 4200.00, '月', '郑州', '郑州市高新区', '负责 Web 系统功能测试、测试用例编写和缺陷记录。', '细心负责，了解软件测试流程，会使用接口测试工具优先。', 6, 75, 1, 1, NOW()),
(8, 3, '数据标注员', '互联网', '灵活用工', 18.00, 28.00, '时', '线上/远程', '线上远程', '按照规则完成图片、文本和表格数据标注任务。', '有电脑基础，理解能力强，能按时完成线上任务。', 20, 156, 1, 1, NOW()),
(9, 3, '数据库录入员', '互联网', '远程办公', 20.00, 30.00, '时', '线上/远程', '线上远程', '负责业务资料整理、Excel 数据清洗和系统录入。', '熟悉 Excel，打字速度较快，数据准确率高。', 12, 91, 1, 1, NOW()),
(10, 4, '编程课程助教', '教育培训', '兼职', 35.00, 60.00, '时', '郑州', '郑州市二七区', '辅助老师完成 Python、C 语言或 Web 前端课程答疑。', '计算机相关专业优先，有耐心，表达清晰。', 8, 83, 1, 1, NOW()),
(11, 4, '线上作业批改助理', '教育培训', '远程办公', 20.00, 35.00, '时', '线上/远程', '线上远程', '根据评分标准批改学生作业并记录学习问题。', '认真细致，能稳定在线，计算机基础课程成绩较好优先。', 10, 67, 1, 1, NOW()),
(12, 5, '短视频剪辑助理', '文化传媒', '项目制', 100.00, 300.00, '单', '郑州', '郑州市管城区', '根据脚本完成短视频粗剪、字幕整理和封面制作。', '会使用剪映或 PR，有新媒体内容感觉。', 6, 88, 1, 1, NOW()),
(13, 5, '直播运营助理', '文化传媒', '兼职', 25.00, 45.00, '时', '郑州', '郑州市金水区', '协助直播间商品上架、评论互动和数据登记。', '沟通能力好，熟悉直播平台基础操作。', 5, 73, 1, 1, NOW()),
(14, 6, '仓库分拣员', '物流配送', '临时工', 18.00, 25.00, '时', '郑州', '郑州市经开区', '负责快件分拣、扫码入库和货架整理。', '能适应排班，工作认真负责。', 15, 104, 1, 1, NOW()),
(15, 6, '同城配送骑手', '物流配送', '灵活用工', 5000.00, 9000.00, '月', '郑州', '郑州市经开区', '负责同城订单配送，可根据个人时间接单。', '熟悉城区道路，自备交通工具者优先。', 20, 130, 1, 1, NOW()),
(16, 2, 'UI页面制作助理', '互联网', '项目制', 150.00, 400.00, '单', '线上/远程', '线上远程', '根据原型图完成后台管理页面静态实现和样式调整。', '熟悉 HTML、CSS，了解 Element Plus 组件优先。', 6, 69, 1, 1, NOW()),
(17, 3, '小程序开发助理', '互联网', '项目制', 300.00, 800.00, '单', '郑州', '郑州市高新区', '协助完成微信小程序页面开发、接口联调和问题修复。', '了解 JavaScript、小程序开发基础，有项目经验优先。', 3, 62, 1, 1, NOW()),
(18, 1, '社区活动志愿服务岗', '政务公共服务', '兼职', 15.00, 25.00, '时', '郑州', '郑州市中原区', '协助社区完成就业登记、活动签到和资料整理。', '沟通能力好，服务意识强。', 10, 58, 1, 1, NOW()),
(19, 7, 'AI Agent工程师助理', '互联网', '实习', 4000.00, 7000.00, '月', '郑州', '郑州市高新区', '参与智能客服 Agent、知识库问答和工作流自动化应用开发。', '熟悉 Python，了解大模型 API、Prompt 设计或 RAG 基础优先。', 3, 145, 1, 1, NOW()),
(20, 7, 'Python开发工程师', '互联网', '实习', 3500.00, 6500.00, '月', '郑州', '郑州市高新区', '负责 Python 后端接口、数据处理脚本和自动化工具开发。', '熟悉 Python 基础，了解 FastAPI、Flask 或爬虫基础优先。', 4, 132, 1, 1, NOW()),
(21, 7, 'C++开发工程师助理', '互联网', '实习', 4000.00, 7000.00, '月', '郑州', '郑州市高新区', '参与 C++ 模块开发、性能优化和基础组件维护。', '熟悉 C++ 基础语法、面向对象和常用数据结构。', 3, 101, 1, 1, NOW()),
(22, 7, '全栈开发工程师', '互联网', '项目制', 500.00, 1200.00, '单', '线上/远程', '线上远程', '负责小型管理系统前后端开发，包括 Vue 页面、Spring Boot 接口和 MySQL 表设计。', '熟悉 Vue、Java 或 Node.js，能独立完成简单业务模块。', 5, 166, 1, 1, NOW()),
(23, 7, '算法工程师助理', '互联网', '实习', 4500.00, 8000.00, '月', '郑州', '郑州市高新区', '协助完成推荐算法、文本分类和数据分析实验。', '掌握 Python、常用机器学习算法和基础数据结构。', 2, 118, 1, 1, NOW()),
(24, 7, '机器学习工程师助理', '互联网', '实习', 4500.00, 8500.00, '月', '郑州', '郑州市高新区', '参与模型训练数据整理、特征处理和实验结果分析。', '了解 PyTorch 或 scikit-learn，有机器学习课程项目经验优先。', 2, 109, 1, 1, NOW()),
(25, 3, '后端开发工程师助理', '互联网', '实习', 3500.00, 6500.00, '月', '郑州', '郑州市金水区', '参与后端接口开发、业务逻辑实现和数据库联调。', '熟悉 Java、Spring Boot、MyBatis Plus 和 MySQL 基础。', 4, 125, 1, 1, NOW()),
(26, 3, '运维开发工程师助理', '互联网', '兼职', 3000.00, 5500.00, '月', '郑州', '郑州市金水区', '协助完成服务器部署、日志检查、脚本编写和系统巡检。', '了解 Linux 常用命令、Shell 或 Docker 基础优先。', 3, 87, 1, 1, NOW()),
(27, 2, 'Vue前端开发工程师', '互联网', '实习', 3000.00, 6000.00, '月', '郑州', '郑州市郑东新区', '负责 Vue3 页面开发、Element Plus 组件封装和接口联调。', '熟悉 Vue3、Vite、Axios，能根据原型完成页面开发。', 4, 138, 1, 1, NOW()),
(28, 7, '大模型数据处理员', '互联网', '远程办公', 25.00, 45.00, '时', '线上/远程', '线上远程', '负责大模型训练数据清洗、问答样本整理和标注质量检查。', '理解能力强，熟悉 Excel，了解 AI 应用更佳。', 15, 176, 1, 1, NOW());

INSERT INTO resume (id, user_id, title, real_name, gender, birthday, education, work_years, phone, email, expected_position, expected_city, self_intro, audit_status, status) VALUES
(1, 1, '张三的求职简历', '张三', 1, '1999-01-01', '大专', 2, '13900000001', 'user@example.com', '社区配送员', '郑州', '本人时间灵活，沟通能力较好，希望寻找稳定的灵活就业岗位。', 1, 1),
(2, 2, '李四的求职简历', '李四', 2, '1998-02-02', '本科', 1, '13900000002', 'lisi@example.com', '线上客服', '郑州', '熟悉办公软件，认真负责，有线上客服经验。', 1, 1);

INSERT INTO application (id, user_id, job_id, company_id, resume_id, status, apply_time, remark) VALUES
(1, 1, 1, 1, 1, 0, NOW(), '用户已投递，等待企业处理'),
(2, 2, 3, 2, 2, 1, NOW(), '企业已查看简历');

INSERT INTO employment_pref (id, user_id, industry, position, salary_min, salary_max, work_city, job_type) VALUES
(1, 1, '生活服务', '社区配送员', 3000.00, 6000.00, '郑州', '兼职'),
(2, 2, '互联网', '线上客服', 3000.00, 5000.00, '郑州', '兼职');

INSERT INTO favorite_job (id, user_id, job_id) VALUES
(1, 1, 2),
(2, 2, 1);

INSERT INTO notice (id, title, content, type, is_top, status, create_by) VALUES
(1, '平台上线试运行通知', '某市灵活就业管理服务平台已上线试运行，欢迎用户和企业使用平台服务。', 1, 1, 1, 1),
(2, '灵活就业岗位推荐活动', '平台将根据用户求职偏好推荐匹配岗位，请及时完善个人信息和求职偏好。', 3, 0, 1, 1);

INSERT INTO policy (id, title, content, apply_condition, material_required, status, create_by) VALUES
(1, '灵活就业人员社保补贴政策', '符合条件的灵活就业人员可在线申报社保补贴。', '已登记灵活就业并按规定缴纳社会保险。', '身份证、就业登记证明、缴费凭证。', 1, 1),
(2, '灵活就业创业扶持政策', '鼓励灵活就业人员开展自主创业，符合条件者可申请创业扶持。', '具有本市户籍或居住证，且符合创业扶持条件。', '身份证、创业证明、经营材料。', 1, 1);

INSERT INTO policy_apply (id, user_id, policy_id, applicant_name, phone, material_url, status, audit_remark, apply_time) VALUES
(1, 1, 1, '张三', '13900000001', '/upload/materials/policy_apply_1.pdf', 0, '等待管理员审核', NOW());

INSERT INTO message (id, sender_type, sender_id, receiver_type, receiver_id, title, content, is_read, status) VALUES
(1, 3, 1, 1, 1, '完善个人资料提醒', '请及时完善个人资料和求职偏好，以便获取更准确的岗位推荐。', 0, 1),
(2, 2, 1, 1, 1, '岗位投递沟通', '您好，已收到您的岗位投递，请保持电话畅通。', 0, 1),
(3, 1, 1, 2, 1, '咨询岗位工作时间', '您好，请问社区配送员岗位可以周末兼职吗？', 1, 1);

INSERT INTO social_security_apply (id, user_id, applicant_name, id_card, phone, insurance_type, material_url, status, audit_remark, apply_time) VALUES
(1, 1, '张三', '410100199901010011', '13900000001', '灵活就业人员养老保险', '/upload/materials/social_apply_1.pdf', 0, '等待审核', NOW());

INSERT INTO social_payment_record (id, user_id, bill_no, payment_month, amount, status, pay_time) VALUES
(1, 1, 'SS2026060001', '2026-06', 860.00, 0, NULL),
(2, 1, 'SS2026050001', '2026-05', 860.00, 1, '2026-05-20 10:30:00');

INSERT INTO labor_contract (id, user_id, company_id, job_id, contract_no, title, start_date, end_date, file_url, status) VALUES
(1, 1, 1, 1, 'LC2026060001', '社区配送员灵活用工合同', '2026-06-01', '2026-12-31', '/upload/contracts/LC2026060001.pdf', 1);

INSERT INTO company_review (id, user_id, company_id, job_id, score, content, status) VALUES
(1, 1, 1, 1, 5, '企业沟通及时，岗位信息比较清楚。', 1);
