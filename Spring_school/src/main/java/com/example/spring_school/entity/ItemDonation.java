package com.example.spring_school.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("item_donation")
public class ItemDonation {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private Long donorId;
    
    private String donorName;
    
    private String itemType;
    
    private String itemName;
    
    private Integer quantity;
    
    private LocalDateTime donationDate;
    
    private String remark;
    
    private Integer status;
}
