package com.example.spring_school.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.spring_school.entity.FundDonation;

import java.util.List;

public interface FundDonationService extends IService<FundDonation> {
    List<FundDonation> listByStatus(int status);
}
