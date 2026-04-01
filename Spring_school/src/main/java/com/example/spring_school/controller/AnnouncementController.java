package com.example.spring_school.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.spring_school.entity.Announcement;
import com.example.spring_school.service.AnnouncementService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

/**
 * <p>
 * 公告表 前端控制器
 * </p>
 *
 * @author author
 * @since 2026-03-15
 */
@RestController
@RequestMapping("/api/announcements")
public class AnnouncementController {

    @Autowired
    private AnnouncementService announcementService;

    /**
     * 分页获取公告
     */
    @GetMapping("/page")
    public Page<Announcement> pageAnnouncements(
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String status) {
        Page<Announcement> page = new Page<>(current, size);
        QueryWrapper<Announcement> queryWrapper = new QueryWrapper<>();
        if (keyword != null && !keyword.trim().isEmpty()) {
            queryWrapper.like("title", keyword).or().like("content", keyword);
        }
        if (status != null && !status.trim().isEmpty()) {
            try {
                Integer statusValue = Integer.parseInt(status);
                queryWrapper.eq("status", statusValue);
            } catch (NumberFormatException ignored) {}
        }
        // 按ID升序排序（1、2、3...）
        queryWrapper.orderByAsc("id");
        return announcementService.page(page, queryWrapper);
    }

    /**
     * 更新公告置顶状态
     */
    @PutMapping("/{id}/top")
    public boolean updateAnnouncementTop(@PathVariable Long id, @RequestParam Integer isTop) {
        Announcement announcement = announcementService.getById(id);
        if (announcement == null) {
            return false;
        }
        announcement.setIsTop(isTop);
        return announcementService.updateById(announcement);
    }

    /**
     * 添加公告
     */
    @PostMapping
    public boolean createAnnouncement(@RequestBody Announcement announcement) {
        announcement.setPublishTime(LocalDateTime.now());
        return announcementService.save(announcement);
    }

    /**
     * 更新公告
     */
    @PutMapping("/{id}")
    public boolean updateAnnouncement(@PathVariable Long id, @RequestBody Announcement announcement) {
        announcement.setId(id);
        return announcementService.updateById(announcement);
    }

    /**
     * 删除公告
     */
    @DeleteMapping("/{id}")
    public boolean deleteAnnouncement(@PathVariable Long id) {
        return announcementService.removeById(id);
    }
}