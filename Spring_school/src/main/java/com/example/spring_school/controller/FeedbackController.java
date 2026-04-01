package com.example.spring_school.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.spring_school.entity.Feedback;
import com.example.spring_school.service.FeedbackService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * 反馈表 前端控制器
 * </p>
 *
 * @author author
 * @since 2026-03-15
 */
@RestController
@RequestMapping("/api/feedbacks")
public class FeedbackController {

    @Autowired
    private FeedbackService feedbackService;

    /**
     * 分页获取反馈
     */
    @GetMapping("/page")
    public Page<Feedback> pageFeedbacks(
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String status) {
        Page<Feedback> page = new Page<>(current, size);
        QueryWrapper<Feedback> queryWrapper = new QueryWrapper<>();
        if (keyword != null && !keyword.trim().isEmpty()) {
            queryWrapper.like("content", keyword);
        }
        if (type != null && !type.trim().isEmpty()) {
            try {
                Integer typeValue = Integer.parseInt(type);
                queryWrapper.eq("type", typeValue);
            } catch (NumberFormatException ignored) {}
        }
        if (status != null && !status.trim().isEmpty()) {
            try {
                Integer statusValue = Integer.parseInt(status);
                queryWrapper.eq("status", statusValue);
            } catch (NumberFormatException ignored) {}
        }
        return feedbackService.page(page, queryWrapper);
    }

    /**
     * 回复反馈
     */
    @PutMapping("/{id}/reply")
    public boolean replyFeedback(@PathVariable Long id, @RequestBody Map<String, Object> request) {
        Feedback feedback = feedbackService.getById(id);
        if (feedback == null) {
            return false;
        }
        feedback.setStatus(1);
        feedback.setReplyContent((String) request.get("replyContent"));
        feedback.setReplyId((Long) request.get("replyId"));
        feedback.setReplyTime(LocalDateTime.now());
        return feedbackService.updateById(feedback);
    }

    /**
     * 添加反馈
     */
    @PostMapping
    public boolean createFeedback(@RequestBody Feedback feedback) {
        // 设置默认值
        if (feedback.getSubmitTime() == null) {
            feedback.setSubmitTime(LocalDateTime.now());
        }
        if (feedback.getStatus() == null) {
            feedback.setStatus(0); // 0-未处理
        }
        // submitterId由前端传递，如果为空则默认为1
        if (feedback.getSubmitterId() == null) {
            feedback.setSubmitterId(1L);
        }
        return feedbackService.save(feedback);
    }

    /**
     * 根据提交人ID获取反馈列表
     */
    @GetMapping("/submitter/{submitterId}")
    public List<Feedback> getFeedbacksBySubmitterId(@PathVariable Long submitterId) {
        return feedbackService.lambdaQuery()
                .eq(Feedback::getSubmitterId, submitterId)
                .orderByDesc(Feedback::getSubmitTime)
                .list();
    }

    /**
     * 更新反馈
     */
    @PutMapping("/{id}")
    public boolean updateFeedback(@PathVariable Long id, @RequestBody Feedback feedback) {
        feedback.setId(id);
        return feedbackService.updateById(feedback);
    }

    /**
     * 删除反馈
     */
    @DeleteMapping("/{id}")
    public boolean deleteFeedback(@PathVariable Long id) {
        return feedbackService.removeById(id);
    }
}