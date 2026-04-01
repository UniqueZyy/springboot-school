package com.example.spring_school.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.spring_school.entity.Feedback;
import com.example.spring_school.mapper.FeedbackMapper;
import com.example.spring_school.service.FeedbackService;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 反馈表 服务实现类
 * </p>
 *
 * @author author
 * @since 2026-03-15
 */
@Service
public class FeedbackServiceImpl extends ServiceImpl<FeedbackMapper, Feedback> implements FeedbackService {

}