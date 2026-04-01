package com.example.spring_school.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.spring_school.entity.ItemDonation;
import com.example.spring_school.service.ItemDonationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/item-donations")
@CrossOrigin(origins = "*")
public class ItemDonationController {

    @Autowired
    private ItemDonationService itemDonationService;

    /**
     * 分页获取物品捐赠记录
     */
    @GetMapping("/page")
    public IPage<ItemDonation> getItemDonationsPage(
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String itemType,
            @RequestParam(required = false) Integer status) {
        Page<ItemDonation> page = new Page<>(current, size);
        return itemDonationService.lambdaQuery()
                .like(keyword != null && !keyword.isEmpty(), ItemDonation::getDonorName, keyword)
                .eq(itemType != null && !itemType.isEmpty(), ItemDonation::getItemType, itemType)
                .eq(status != null, ItemDonation::getStatus, status)
                .orderByDesc(ItemDonation::getDonationDate)
                .page(page);
    }

    /**
     * 根据捐赠人ID获取物品捐赠记录
     */
    @GetMapping("/donor/{donorId}")
    public List<ItemDonation> getItemDonationsByDonorId(@PathVariable Long donorId) {
        return itemDonationService.lambdaQuery()
                .eq(ItemDonation::getDonorId, donorId)
                .orderByDesc(ItemDonation::getDonationDate)
                .list();
    }

    /**
     * 添加物品捐赠记录
     */
    @PostMapping
    public boolean createItemDonation(@RequestBody ItemDonation itemDonation) {
        // 设置默认值
        if (itemDonation.getStatus() == null) {
            itemDonation.setStatus(0); // 0-待审批
        }
        if (itemDonation.getDonationDate() == null) {
            itemDonation.setDonationDate(LocalDateTime.now());
        }
        // donorId由前端传递，如果为空则默认为1
        if (itemDonation.getDonorId() == null) {
            itemDonation.setDonorId(1L);
        }
        return itemDonationService.save(itemDonation);
    }

    /**
     * 更新物品捐赠记录
     */
    @PutMapping("/{id}")
    public boolean updateItemDonation(@PathVariable Long id, @RequestBody ItemDonation itemDonation) {
        itemDonation.setId(id);
        return itemDonationService.updateById(itemDonation);
    }

    /**
     * 删除物品捐赠记录
     */
    @DeleteMapping("/{id}")
    public boolean deleteItemDonation(@PathVariable Long id) {
        return itemDonationService.removeById(id);
    }

    /**
     * 批准物品捐赠
     */
    @PutMapping("/{id}/approve")
    public boolean approveItemDonation(@PathVariable Long id) {
        ItemDonation itemDonation = new ItemDonation();
        itemDonation.setId(id);
        itemDonation.setStatus(1); // 1-已通过
        return itemDonationService.updateById(itemDonation);
    }

    /**
     * 拒绝物品捐赠
     */
    @PutMapping("/{id}/reject")
    public boolean rejectItemDonation(@PathVariable Long id) {
        ItemDonation itemDonation = new ItemDonation();
        itemDonation.setId(id);
        itemDonation.setStatus(2); // 2-已拒绝
        return itemDonationService.updateById(itemDonation);
    }

    /**
     * 标记物品已寄出
     */
    @PutMapping("/{id}/shipped")
    public boolean shipItemDonation(@PathVariable Long id) {
        ItemDonation itemDonation = new ItemDonation();
        itemDonation.setId(id);
        itemDonation.setStatus(3); // 3-已寄出
        return itemDonationService.updateById(itemDonation);
    }
}
