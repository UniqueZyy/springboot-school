package com.example.spring_school.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.io.Serializable;

/**
 * <p>
 * 捐赠记录表
 * </p>
 *
 * @author author
 * @since 2026-03-15
 */
@TableName("donation_record")
public class DonationRecord implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 捐赠人ID
     */
    private Long userId;

    /**
     * 项目ID
     */
    private Long projectId;

    /**
     * 捐赠金额
     */
    private BigDecimal amount;

    /**
     * 捐赠时间
     */
    private LocalDateTime donationTime;

    /**
     * 备注
     */
    private String remark;

    /**
     * 是否匿名：0-否，1-是
     */
    private Integer anonymous;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getProjectId() {
        return projectId;
    }

    public void setProjectId(Long projectId) {
        this.projectId = projectId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public LocalDateTime getDonationTime() {
        return donationTime;
    }

    public void setDonationTime(LocalDateTime donationTime) {
        this.donationTime = donationTime;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public Integer getAnonymous() {
        return anonymous;
    }

    public void setAnonymous(Integer anonymous) {
        this.anonymous = anonymous;
    }

    @Override
    public String toString() {
        return "DonationRecord{" +
        "id=" + id +
        ", userId=" + userId +
        ", projectId=" + projectId +
        ", amount=" + amount +
        ", donationTime=" + donationTime +
        ", remark=" + remark +
        ", anonymous=" + anonymous +
        "}";
    }
}