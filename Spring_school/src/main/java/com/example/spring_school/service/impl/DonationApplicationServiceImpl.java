package com.example.spring_school.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.spring_school.dto.DonationApplicationDTO;
import com.example.spring_school.entity.DonationApplication;
import com.example.spring_school.mapper.DonationApplicationMapper;
import com.example.spring_school.service.DonationApplicationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class DonationApplicationServiceImpl implements DonationApplicationService {

    @Autowired
    private DonationApplicationMapper donationApplicationMapper;

    @Override
    public Page<DonationApplication> getDonationApplicationsPage(int current, int size, String keyword, String status) {
        Page<DonationApplication> page = new Page<>(current, size);
        QueryWrapper<DonationApplication> wrapper = new QueryWrapper<>();
        
        if (keyword != null && !keyword.isEmpty()) {
            wrapper.like("applicant_name", keyword).or().like("donation_type_name", keyword);
        }
        
        if (status != null && !status.isEmpty()) {
            wrapper.eq("status", status);
        }
        
        return donationApplicationMapper.selectPage(page, wrapper);
    }

    @Override
    public List<DonationApplication> getAllDonationApplications() {
        return donationApplicationMapper.selectList(null);
    }

    @Override
    public DonationApplication getDonationApplicationById(Long id) {
        return donationApplicationMapper.selectById(id);
    }

    @Override
    public boolean createDonationApplication(DonationApplicationDTO dto) {
        DonationApplication application = new DonationApplication();
        application.setApplicantId(dto.getApplicantId());
        application.setApplicantName(dto.getApplicantName());
        application.setApplicantRole(dto.getApplicantRole());
        application.setApplicationType(dto.getApplicationType());
        application.setTypeId(dto.getTypeId());
        application.setDonationTypeName(dto.getDonationTypeName());
        application.setQuantity(dto.getQuantity());
        application.setReason(dto.getReason());
        application.setStatus(0); // 待审批
        application.setApplyTime(LocalDateTime.now());
        
        return donationApplicationMapper.insert(application) > 0;
    }

    @Override
    public boolean approveDonationApplication(Long id, String approvalComment) {
        DonationApplication application = donationApplicationMapper.selectById(id);
        if (application == null) {
            return false;
        }
        
        application.setStatus(1); // 已通过
        application.setApprovalComment(approvalComment);
        application.setApproveTime(LocalDateTime.now());
        
        return donationApplicationMapper.updateById(application) > 0;
    }

    @Override
    public boolean rejectDonationApplication(Long id, String approvalComment) {
        DonationApplication application = donationApplicationMapper.selectById(id);
        if (application == null) {
            return false;
        }
        
        application.setStatus(2); // 已拒绝
        application.setApprovalComment(approvalComment);
        application.setApproveTime(LocalDateTime.now());
        
        return donationApplicationMapper.updateById(application) > 0;
    }

    @Override
    public boolean deleteDonationApplication(Long id) {
        return donationApplicationMapper.deleteById(id) > 0;
    }
}
