use shopping;
-- 插入示例用户
INSERT INTO shoppingUser (username, password, email) VALUES
        ('lilefan', '123456a', '3220105251@zju.edu.cn'), -- 请改为您的邮箱号,
        ('liqiang', '111111a', '3460697936@qq.com');     -- 当然可以通过注册账户方式实现
-- 插入示例商品数据
INSERT INTO product (product_id, product_name, platform_name, price, specification, image) VALUES
        ('756775095301', '女装', '淘宝', 50.00, '高腰百搭羊羔绒阔腿裤冬季保暖',
         'https://img.alicdn.com/img/bao/uploaded/i4/O1CN01TpegE82EAUGWQD2JI_!!0-item_pic.jpg'),
        ('825927041956', '女装', '淘宝', 44.40, '德绒圆领打底衫短款修身内搭长袖T恤女秋冬纯色弧形小众百搭上衣',
         'https://img.alicdn.com/img/bao/uploaded/i4/O1CN01xuzAuZ2LY21GBhlfE_!!3937219703-0-C2M.jpg'),
        ('10072445837225', '女装', '京东', 220, '恒源祥民族风复古高端香云纱袍子长款中长袖秋季新款连衣裙女装 高档女装高档女装衣服：绿色 高档女装高档女装衣服：XL',
         'https://img11.360buyimg.com/n0/jfs/t1/229888/17/7138/103455/657d33e2Fb291bf94/e0550ecfc9d4cf72.jpg'),
        ('10072392554898', '女装', '京东', 208, '恒源祥气质夏装网纱裙洋气减龄短袖裙子薄款中年女装连衣裙 高档女装高档女装衣服：粉色 高档女装高档女装衣服：XL',
         'https://img11.360buyimg.com/n0/jfs/t1/233125/37/8742/159235/657d33e3Fc9955b24/1e529d7bc36850ec.jpg');

-- 插入商品历史价格数据
INSERT INTO product_history_price (time, product_id, price) VALUES
        ('2024-12-01 10:00:00', '756775095301', 100.00),
        ('2024-12-02 10:00:00', '756775095301', 70.00),
        ('2024-12-03 10:00:00', '756775095301', 60.00);

-- 插入降价提醒数据
INSERT INTO price_drop_alert (product_id, email, price) VALUES
        ('756775095301', '3220105251@zju.edu.cn', 60.00);