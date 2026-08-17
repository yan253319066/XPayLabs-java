package com.yan.xpay.mapper;

import com.yan.xpay.domain.vo.DashboardSymbolStatVo;
import com.yan.xpay.domain.vo.DashboardStaleTrackerVo;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Map;

public interface DashboardMapper {

    @Select("SELECT COUNT(*) FROM t_merchant")
    Long countMerchants();

    @Select("""
        SELECT COUNT(DISTINCT merchant_id) FROM t_payment_order
        WHERE create_time >= #{start} AND create_time < #{end}
        """)
    Long countActiveMerchants(@Param("start") Date start, @Param("end") Date end);

    @Select("""
        <script>
        SELECT
          COUNT(*) AS totalCount,
          SUM(CASE WHEN status = 'SUCCESS' THEN 1 ELSE 0 END) AS successCount,
          COALESCE(SUM(CASE WHEN status = 'SUCCESS' THEN actual_amount ELSE 0 END), 0) AS successAmount
        FROM t_payment_order
        WHERE order_type = #{orderType}
          AND create_time &gt;= #{start} AND create_time &lt; #{end}
          <if test="merchantId != null">AND merchant_id = #{merchantId}</if>
        </script>
        """)
    Map<String, Object> aggregateOrders(@Param("orderType") String orderType,
                                        @Param("start") Date start,
                                        @Param("end") Date end,
                                        @Param("merchantId") Long merchantId);

    @Select("""
        <script>
        SELECT symbol AS symbol, order_type AS orderType,
               COUNT(*) AS successCount,
               COALESCE(SUM(actual_amount), 0) AS successAmount
        FROM t_payment_order
        WHERE status = 'SUCCESS'
          AND create_time &gt;= #{start} AND create_time &lt; #{end}
          <if test="merchantId != null">AND merchant_id = #{merchantId}</if>
        GROUP BY symbol, order_type
        </script>
        """)
    List<DashboardSymbolStatVo> groupOrderSuccessBySymbol(@Param("start") Date start,
                                                          @Param("end") Date end,
                                                          @Param("merchantId") Long merchantId);

    @Select("""
        <script>
        SELECT
          COUNT(*) AS successCount,
          COALESCE(SUM(amount), 0) AS successAmount
        FROM t_merchant_recharge_withdraw
        WHERE type = #{type} AND status = 'SUCCESS'
          AND create_time &gt;= #{start} AND create_time &lt; #{end}
          <if test="merchantId != null">AND merchant_id = #{merchantId}</if>
        </script>
        """)
    Map<String, Object> aggregateRechargeWithdraw(@Param("type") String type,
                                                  @Param("start") Date start,
                                                  @Param("end") Date end,
                                                  @Param("merchantId") Long merchantId);

    @Select("""
        <script>
        SELECT COALESCE(SUM(fee), 0) FROM t_merchant_recharge_withdraw
        WHERE status = 'SUCCESS'
          AND create_time &gt;= #{start} AND create_time &lt; #{end}
          <if test="merchantId != null">AND merchant_id = #{merchantId}</if>
        </script>
        """)
    BigDecimal sumRwFee(@Param("start") Date start, @Param("end") Date end, @Param("merchantId") Long merchantId);

    @Select("""
        <script>
        SELECT COALESCE(SUM(handing_fee), 0) FROM t_payment_order
        WHERE status = 'SUCCESS'
          AND create_time &gt;= #{start} AND create_time &lt; #{end}
          <if test="merchantId != null">AND merchant_id = #{merchantId}</if>
        </script>
        """)
    BigDecimal sumOrderFee(@Param("start") Date start, @Param("end") Date end, @Param("merchantId") Long merchantId);

    @Select("""
        <script>
        SELECT symbol AS symbol, type AS orderType,
               COUNT(*) AS successCount,
               COALESCE(SUM(amount), 0) AS successAmount
        FROM t_merchant_recharge_withdraw
        WHERE status = 'SUCCESS'
          AND create_time &gt;= #{start} AND create_time &lt; #{end}
          <if test="merchantId != null">AND merchant_id = #{merchantId}</if>
        GROUP BY symbol, type
        </script>
        """)
    List<DashboardSymbolStatVo> groupRwSuccessBySymbol(@Param("start") Date start,
                                                       @Param("end") Date end,
                                                       @Param("merchantId") Long merchantId);

    @Select("""
        <script>
        SELECT COUNT(*) FROM t_payment_order
        WHERE status = #{status}
          AND create_time &gt;= #{start} AND create_time &lt; #{end}
          <if test="merchantId != null">AND merchant_id = #{merchantId}</if>
        </script>
        """)
    Long countOrdersByStatus(@Param("status") String status,
                             @Param("start") Date start,
                             @Param("end") Date end,
                             @Param("merchantId") Long merchantId);

    @Select("""
        <script>
        SELECT COUNT(*) FROM t_payment_order
        WHERE status = 'PENDING_CONFIRMATION'
          <if test="merchantId != null">AND merchant_id = #{merchantId}</if>
        </script>
        """)
    Long countPendingConfirm(@Param("merchantId") Long merchantId);

    @Select("""
        <script>
        SELECT COUNT(*) FROM t_callback_notice
        WHERE notify_status = 'FAIL'
          <if test="start != null">AND create_time &gt;= #{start} AND create_time &lt; #{end}</if>
          <if test="merchantId != null">AND merchant_id = #{merchantId}</if>
        </script>
        """)
    Long countCallbackFail(@Param("start") Date start, @Param("end") Date end, @Param("merchantId") Long merchantId);

    @Select("SELECT COUNT(*) FROM t_error_block")
    Long countErrorBlocks();

    @Select("""
        <script>
        SELECT COUNT(*) FROM t_error_block
        WHERE create_time &gt;= #{start} AND create_time &lt; #{end}
        </script>
        """)
    Long countErrorBlocksInRange(@Param("start") Date start, @Param("end") Date end);

    @Select("""
        SELECT chain AS chain, last_height AS lastHeight, update_time AS updateTime
        FROM t_block_height_tracker
        WHERE update_time &lt; #{threshold}
        """)
    List<DashboardStaleTrackerVo> listStaleTrackers(@Param("threshold") Date threshold);
}
