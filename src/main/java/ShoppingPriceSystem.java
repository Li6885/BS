import entities.*;
import queries.ApiResult;

import java.sql.SQLException;

/**
 * Note:
 *      (1) all functions in this interface will be regarded as a
 *          transaction. this means that after successfully completing
 *          all operations in a function, you need to call commit(),
 *          or call rollback() if one of the operations in a function fails.
 *          as an example, you can see {@link ShoppingPriceSystemImpl
 *          to find how to use commit() and rollback().
 *      (2) for each function, you need to briefly introduce how to
 *          achieve this function and how to solve challenges in your
 *          lab report.
 *      (3) if you don't know what the function means, or what it is
 *          supposed to do, looking to the test code might help.
 */
public interface ShoppingPriceSystem {

    /* 用户相关接口 */

    /**
     * 用户注册
     * 需要检查用户名和邮箱是否已经注册
     *
     * @param guest 游客类
     * @return 注册结果
     */
    ApiResult registerUser(Guest guest) throws SQLException;

    /**
     * 用户登录
     * 需要检查用户名和密码是否匹配
     * 此外，若用户名、密码匹配还需要检查是否正在登录，存在会话类session
     *
     * @param guest 游客类
     * @return 登录结果
     */
    ApiResult loginUser(Guest guest) throws SQLException;

    /**
     * 用户退出登录时，注销会话
     * @param userName 用户名
     * @return 注销结果
     */
    ApiResult logOutSession(String userName) throws SQLException;

    /* 搜索相关接口 */

    /**
     * 查询商品在不同平台的价格。
     *
     * @param productName 商品名
     * @return 各平台最低价格
     */
    ApiResult queryProductPricesByPlatform(String productName, String platform) throws SQLException;

    /**
     * 查询某商品的历史价格
     * @param productId 商品id
     * @return 商品的历史价格
     */
    ApiResult getPriceHistory(String productId) throws SQLException;

    /* 降价提醒相关接口 */

    /**
     * 设置商品降价提醒
     * 如果该商品已经有降价提醒记录，则更新邮箱
     *
     * @param productName 商品名
     * @param email 用户邮箱
     * @param stdPrice 目标价格
     * @return 设置提醒结果
     */
    ApiResult setPriceDropAlert(String productName, String email, double stdPrice) throws SQLException;

    /**
     * 降价提醒自动检查
     * @param alert 降价提醒表内的数据
     * @param platform 电商平台名
     * @param nowPrice 现在的价格
     * 无返回值。定时器触发
     */
    void priceDropAlert(PriceDropAlert alert, String platform, double nowPrice);

    /* 更新数据库相关接口 */

    /**
     * 自动更新降价提醒表内的商品信息
     * @throws InterruptedException, SQLException
     */
    void updatePricesWithAlert() throws InterruptedException, SQLException;

    /**
     * 自动更新数据库，并检查降价表
     */
    void checkAlert();

    /**
     * 重置数据库
     */
    ApiResult resetDatabase();
}

