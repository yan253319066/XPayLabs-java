package com.yan.xpay.domain.vo;

import lombok.Data;

import java.util.Date;

@Data
public class DashboardStaleTrackerVo {
    private String chain;
    private Long lastHeight;
    private Date updateTime;
}
