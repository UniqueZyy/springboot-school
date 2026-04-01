package com.example.spring_school.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("donation_application")
public class DonationApplication {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long applicantId;

    private String applicantName;

    private Integer applicantRole;

    private Integer applicationType;

    private Long typeId;

    private String donationTypeName;

    private Integer quantity;

    private String reason;

    private Integer status;

    private LocalDateTime applyTime;

    private Long approverId;

    private String approverName;

    private LocalDateTime approveTime;

    private String approvalComment;
}
