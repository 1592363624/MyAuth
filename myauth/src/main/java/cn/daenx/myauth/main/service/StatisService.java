package cn.daenx.myauth.main.service;

import cn.daenx.myauth.base.vo.Result;
import cn.daenx.myauth.base.vo.MyPage;
import cn.daenx.myauth.main.entity.Soft;

/**
 * <p>
 * 服务类
 * </p>
 *
 * @author DaenMax
 * @since 2022-01-06
 */
public interface StatisService {
    /**
     * 获取在线人数
     *
     * @param skey
     * @return
     */
    Result getOnlineUserCount(String skey);

    /**
     * 获取用户总数
     *
     * @param skey
     * @return
     */
    Result getUserCount(String skey);

    /**
     * 获取软件统计
     *
     * @param soft
     * @return
     */
    Result getSoftStatisData(Soft soft);

    /**
     * 获取全部软件统计数据（用于数据看板列表展示）
     * 一次性返回所有软件的：软件名、在线客户端数、总用户数、1/7/30天内新增用户数
     *
     * @return
     */
    Result getAllSoftStatisData();

    /**
     * 获取数据排行
     *
     * @param soft
     * @param myPage
     * @return
     */
    Result getDataRanking(Soft soft, MyPage myPage);

    /**
     * 获取设备排行
     *
     * @param soft
     * @param myPage
     * @return
     */
    Result getUserDeviceInfoRanking(Soft soft, MyPage myPage);

    /**
     * 获取近7天每日新增用户数
     *
     * @return
     */
    Result getUserDatNew();

    /**
     * 获取用户分布比例
     *
     * @return
     */
    Result getUserDistribution();

    /**
     * 获取卡密状态比例
     *
     * @return
     */
    Result getCardDistribution();

    /**
     * 获取封禁类型数量
     *
     * @return
     */
    Result getBanTypeCount();
}
