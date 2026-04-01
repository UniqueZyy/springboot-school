package com.example.spring_school.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.spring_school.entity.ItemDonation;

import java.util.List;

public interface ItemDonationService extends IService<ItemDonation> {
    List<ItemDonation> listByStatus(int status);
}
