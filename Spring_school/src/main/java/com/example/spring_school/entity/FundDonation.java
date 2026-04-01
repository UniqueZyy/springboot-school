package com.example.spring_school.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("fund_donation")
public class FundDonation {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private Long donorId;
    
    private String donorName;
    
    private BigDecimal amount;
    
    private LocalDateTime donationDate;
    
    private String remark;
    
    private Integer status;
}
