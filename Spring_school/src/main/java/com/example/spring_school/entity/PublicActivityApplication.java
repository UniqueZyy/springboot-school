package com.example.spring_school.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("public_activity_application")
public class PublicActivityApplication implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("user_id")
    private Long userId;

    
    // 添加用户姓名字段
    @TableField(exist = false)
    private String userName;

    
    @TableField("activity_id")
    private Long activityId;

    
    // 添加活动名称字段
    @TableField(exist = false)
    private String activityName;

    
    // 添加活动负责人字段
    @TableField(exist = false)
    private String activityCreatorName;

    
    // 添加活动状态字段
    @TableField(exist = false)
    private Integer activityStatus;

    // 添加活动开始时间字段
    @TableField(exist = false)
    private String activityStartTime;

    // 添加活动结束时间字段
    @TableField(exist = false)
    private String activityEndTime;


    @TableField("application_reason")
    private String applicationReason;

    @TableField("status")
    private Integer status;

    @TableField("apply_time")
    private LocalDateTime applyTime;

    @TableField("approve_time")
    private LocalDateTime approveTime;

    @TableField("approver_id")
    private Long approverId;

    @TableField("approve_remark")
    private String approveRemark;
}