package com.example.spring_school.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.spring_school.dto.DonationApplicationDTO;
import com.example.spring_school.entity.DonationApplication;

import java.util.List;

public interface DonationApplicationService {

    // 分页获取捐赠申请列表
    Page<DonationApplication> getDonationApplicationsPage(int current, int size, String keyword, String status);

    // 获取所有捐赠申请
    List<DonationApplication> getAllDonationApplications();

    // 根据ID获取捐赠申请
    DonationApplication getDonationApplicationById(Long id);

    // 创建捐赠申请
    boolean createDonationApplication(DonationApplicationDTO dto);

    // 批准捐赠申请
    boolean approveDonationApplication(Long id, String approvalComment);

    // 拒绝捐赠申请
    boolean rejectDonationApplication(Long id, String approvalComment);

    // 删除捐赠申请
    boolean deleteDonationApplication(Long id);
}
