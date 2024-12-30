DROP DATABASE shopping;
-- 创建数据库 shopping
CREATE DATABASE IF NOT EXISTS shopping;
USE shopping;

-- 创建用户表
CREATE TABLE IF NOT EXISTS shoppingUser (
    username VARCHAR(40) PRIMARY KEY,   -- 用户名，主键
    password VARCHAR(40) NOT NULL,      -- 密码，非空
    email VARCHAR(100) NOT NULL UNIQUE   -- 邮箱名，非空、唯一
);

-- 创建会话表
CREATE TABLE IF NOT EXISTS session (
    username VARCHAR(40) PRIMARY KEY    -- 用户名，主键
);

-- 创建商品表
CREATE TABLE IF NOT EXISTS product (
    product_id VARCHAR(100),            -- 主键
    product_name VARCHAR(100),          -- 商品名
    platform_name VARCHAR(40),          -- 电商平台名
    price DOUBLE(10, 2) NOT NULL,       -- 价格，非空
    specification VARCHAR(400),         -- 规格
    image VARCHAR(400),                 -- 图片
    PRIMARY KEY (product_id)            -- 商品名和电商平台名作为联合主键
);

-- 创建商品历史价格表
CREATE TABLE IF NOT EXISTS product_history_price (
    time DATETIME,                      -- 时间，主键之一
    product_id VARCHAR(100),            -- 商品名，主键之一
    price DOUBLE(10, 2) NOT NULL,       -- 价格，非空
    PRIMARY KEY (time, product_id)      -- 时间、商品名和电商平台名作为联合主键
);

-- 创建降价提醒表
CREATE TABLE IF NOT EXISTS price_drop_alert (
     product_id VARCHAR(100),           -- 商品名，主键之一
     email VARCHAR(100),                 -- 邮箱名，主键之一
     price DOUBLE(10, 2) NOT NULL,      -- 价格，非空
     PRIMARY KEY (product_id, email)    -- 商品名和邮箱作为联合主键
);