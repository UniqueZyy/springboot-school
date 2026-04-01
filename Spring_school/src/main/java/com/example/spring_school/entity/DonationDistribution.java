package com.example.spring_school.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import java.time.LocalDateTime;
import java.io.Serializable;

/**
 * <p>
 * 捐赠分配表
 * </p>
 *
 * @author author
 * @since 2026-03-15
 */
@TableName("donation_distribution")
public class DonationDistribution implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 物资类型ID
     */
    private Long typeId;

    /**
     * 分配数量
     */
    private Integer quantity;

    /**
     * 分配对象
     */
    private String distributionObject;

    /**
     * 分配原因
     */
    private String reason;

    /**
     * 分配时间
     */
    private LocalDateTime distributionTime;

    /**
     * 操作人ID
     */
    private Long operatorId;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getTypeId() {
        return typeId;
    }

    public void setTypeId(Long typeId) {
        this.typeId = typeId;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public String getDistributionObject() {
        return distributionObject;
    }

    public void setDistributionObject(String distributionObject) {
        this.distributionObject = distributionObject;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public LocalDateTime getDistributionTime() {
        return distributionTime;
    }

    public void setDistributionTime(LocalDateTime distributionTime) {
        this.distributionTime = distributionTime;
    }

    public Long getOperatorId() {
        return operatorId;
    }

    public void setOperatorId(Long operatorId) {
        this.operatorId = operatorId;
    }

    @Override
    public String toString() {
        return "DonationDistribution{" +
        "id=" + id +
        ", typeId=" + typeId +
        ", quantity=" + quantity +
        ", distributionObject=" + distributionObject +
        ", reason=" + reason +
        ", distributionTime=" + distributionTime +
        ", operatorId=" + operatorId +
        "}";
    }
}