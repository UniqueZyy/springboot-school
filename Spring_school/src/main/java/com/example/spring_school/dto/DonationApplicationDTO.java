package com.example.spring_school.dto;

import lombok.Data;

@Data
public class DonationApplicationDTO {

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

    private String approvalComment;

    private Long approverId;

    private String approverName;
}
