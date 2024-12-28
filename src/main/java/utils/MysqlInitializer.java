package utils;

public class MysqlInitializer implements DBInitializer {

    @Override
    public String sqlDropGuest() {
        return "drop table if exists `guest`;";
    }

    @Override
    public String sqlDropShoppingUser() {
        return "drop table if exists `shoppingUser`;";
    }

    @Override
    public String sqlDropSession() {
        return "drop table if exists `session`;";
    }

    @Override
    public String sqlDropProduct() {
        return "drop table if exists `product`;";
    }

    @Override
    public String sqlDropProductHistoryPrice() {
        return "drop table if exists `product_history_price`;";
    }

    @Override
    public String sqlDropPriceDropAlert() {
        return "drop table if exists `price_drop_alert`;";
    }

    @Override
    public String sqlCreateGuest() {
        return "create table `guest` (\n" +
                "    `username` varchar(40) primary key,\n" +
                "    `password` varchar(40) not null,\n" +
                "    `email` varchar(40) not null unique\n" +
                ") engine=innodb charset=utf8mb4;";
    }

    @Override
    public String sqlCreateShoppingUser() {
        return "create table `shoppingUser` (\n" +
                "    `username` varchar(40) primary key,\n" +
                "    `password` varchar(40) not null,\n" +
                "    `email` varchar(40) not null unique\n" +
                ") engine=innodb charset=utf8mb4;";
    }

    @Override
    public String sqlCreateSession() {
        return "create table `session` (\n" +
                "    `username` varchar(40) primary key,\n" +
                "    `email` varchar(40) not null unique\n" +
                ") engine=innodb charset=utf8mb4;";
    }

    @Override
    public String sqlCreateProduct() {
        return "create table `product` (\n" +
                "    `product_name` varchar(100),\n" +
                "    `platform_name` varchar(40),\n" +
                "    `price` double(10, 2) not null,\n" +
                "    `category` varchar(40) not null,\n" +
                "    `specification` varchar(40),\n" +
                "    `image` blob,\n" +
                "    primary key (`product_name`, `platform_name`)\n" +
                ") engine=innodb charset=utf8mb4;";
    }

    @Override
    public String sqlCreateProductHistoryPrice() {
        return "create table `product_history_price` (\n" +
                "    `time` datetime,\n" +
                "    `product_name` varchar(100),\n" +
                "    `platform_name` varchar(40),\n" +
                "    `price` double(10, 2) not null,\n" +
                "    primary key (`time`, `product_name`, `platform_name`)\n" +
                ") engine=innodb charset=utf8mb4;";
    }

    @Override
    public String sqlCreatePriceDropAlert() {
        return "create table `price_drop_alert` (\n" +
                "    `product_name` varchar(100),\n" +
                "    `email` varchar(40) not null unique,\n" +
                "    primary key (`product_name`)\n" +
                ") engine=innodb charset=utf8mb4;";
    }
}
