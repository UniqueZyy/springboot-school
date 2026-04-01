package com.example.spring_school.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.spring_school.entity.ItemDonation;
import com.example.spring_school.mapper.ItemDonationMapper;
import com.example.spring_school.service.ItemDonationService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ItemDonationServiceImpl extends ServiceImpl<ItemDonationMapper, ItemDonation> implements ItemDonationService {
    @Override
    public List<ItemDonation> listByStatus(int status) {
        QueryWrapper<ItemDonation> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("status", status);
        return baseMapper.selectList(queryWrapper);
    }
}
