USE shopping;
-- 插入示例用户
INSERT INTO shoppingUser (username, password, email) VALUES
        ('lilefan', '123456a', 'lilefan20040202@outlook.com'), -- 请改为您的邮箱号,
        ('liqiang', '111111a', '3460697936@qq.com');     -- 当然可以通过注册账户方式实现

-- 插入示例商品数据
INSERT INTO product (product_id, product_name, platform_name, price, specification, image) VALUES
        ('756775095301', '女装', '淘宝', 50.00, '高腰百搭羊羔绒阔腿裤冬季保暖',
         'https://img.alicdn.com/img/bao/uploaded/i4/O1CN01TpegE82EAUGWQD2JI_!!0-item_pic.jpg');

-- 插入商品历史价格数据
INSERT INTO product_history_price (time, product_id, price) VALUES
        ('2024-12-01 10:00:00', '756775095301', 100.00),
        ('2024-12-02 10:00:00', '756775095301', 70.00),
        ('2024-12-03 10:00:00', '756775095301', 60.00);

-- 插入降价提醒数据
INSERT INTO price_drop_alert (product_id, email, price) VALUES
        ('756775095301', 'lilefan20040202@outlook.com', 60.00); -- 请改为您的邮箱号