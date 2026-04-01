package com.example.spring_school.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.spring_school.dto.DonationApplicationDTO;
import com.example.spring_school.entity.DonationApplication;
import com.example.spring_school.service.DonationApplicationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/donation-applications")
public class DonationApplicationController {

    @Autowired
    private DonationApplicationService donationApplicationService;

    // 分页获取捐赠申请列表
    @GetMapping("/page")
    public Page<DonationApplication> getDonationApplicationsPage(
            @RequestParam(defaultValue = "1") int current,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String status) {
        try {
            return donationApplicationService.getDonationApplicationsPage(current, size, keyword, status);
        } catch (Exception e) {
            e.printStackTrace();
            Page<DonationApplication> emptyPage = new Page<>(current, size);
            emptyPage.setRecords(new java.util.ArrayList<>());
            emptyPage.setTotal(0);
            return emptyPage;
        }
    }

    // 获取所有捐赠申请
    @GetMapping
    public List<DonationApplication> getAllDonationApplications() {
        try {
            return donationApplicationService.getAllDonationApplications();
        } catch (Exception e) {
            e.printStackTrace();
            return new java.util.ArrayList<>();
        }
    }

    // 根据ID获取捐赠申请
    @GetMapping("/{id}")
    public DonationApplication getDonationApplicationById(@PathVariable Long id) {
        try {
            return donationApplicationService.getDonationApplicationById(id);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    // 创建捐赠申请
    @PostMapping
    public boolean createDonationApplication(@RequestBody DonationApplicationDTO dto) {
        try {
            return donationApplicationService.createDonationApplication(dto);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    // 批准捐赠申请
    @PutMapping("/{id}/approve")
    public boolean approveDonationApplication(@PathVariable Long id, @RequestBody DonationApplicationDTO dto) {
        try {
            return donationApplicationService.approveDonationApplication(id, dto.getApprovalComment());
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    // 拒绝捐赠申请
    @PutMapping("/{id}/reject")
    public boolean rejectDonationApplication(@PathVariable Long id, @RequestBody DonationApplicationDTO dto) {
        try {
            return donationApplicationService.rejectDonationApplication(id, dto.getApprovalComment());
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    // 删除捐赠申请
    @DeleteMapping("/{id}")
    public boolean deleteDonationApplication(@PathVariable Long id) {
        try {
            return donationApplicationService.deleteDonationApplication(id);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
