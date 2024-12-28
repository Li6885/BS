package utils;

public interface DBInitializer {

    // SQL语句：删除表
    String sqlDropGuest();
    String sqlDropShoppingUser();
    String sqlDropSession();
    String sqlDropProduct();
    String sqlDropProductHistoryPrice();
    String sqlDropPriceDropAlert();

    // SQL语句：创建表
    String sqlCreateGuest();
    String sqlCreateShoppingUser();
    String sqlCreateSession();
    String sqlCreateProduct();
    String sqlCreateProductHistoryPrice();
    String sqlCreatePriceDropAlert();
}