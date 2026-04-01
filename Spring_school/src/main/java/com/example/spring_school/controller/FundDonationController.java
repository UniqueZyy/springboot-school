package com.example.spring_school.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.spring_school.entity.FundDonation;
import com.example.spring_school.service.FundDonationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/fund-donations")
@CrossOrigin(origins = "*")
public class FundDonationController {

    @Autowired
    private FundDonationService fundDonationService;

    /**
     * 分页获取资金捐赠记录
     */
    @GetMapping("/page")
    public IPage<FundDonation> getFundDonationsPage(
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer status) {
        Page<FundDonation> page = new Page<>(current, size);
        return fundDonationService.lambdaQuery()
                .like(keyword != null && !keyword.isEmpty(), FundDonation::getDonorName, keyword)
                .eq(status != null, FundDonation::getStatus, status)
                .orderByDesc(FundDonation::getDonationDate)
                .page(page);
    }

    /**
     * 根据捐赠人ID获取资金捐赠记录
     */
    @GetMapping("/donor/{donorId}")
    public List<FundDonation> getFundDonationsByDonorId(@PathVariable Long donorId) {
        return fundDonationService.lambdaQuery()
                .eq(FundDonation::getDonorId, donorId)
                .orderByDesc(FundDonation::getDonationDate)
                .list();
    }

    /**
     * 添加资金捐赠记录
     */
    @PostMapping
    public boolean createFundDonation(@RequestBody FundDonation fundDonation) {
        // 设置默认值
        if (fundDonation.getStatus() == null) {
            fundDonation.setStatus(0); // 0-待支付
        }
        if (fundDonation.getDonationDate() == null) {
            fundDonation.setDonationDate(LocalDateTime.now());
        }
        // donorId由前端传递，如果为空则默认为1
        if (fundDonation.getDonorId() == null) {
            fundDonation.setDonorId(1L);
        }
        return fundDonationService.save(fundDonation);
    }

    /**
     * 更新资金捐赠记录
     */
    @PutMapping("/{id}")
    public boolean updateFundDonation(@PathVariable Long id, @RequestBody FundDonation fundDonation) {
        fundDonation.setId(id);
        return fundDonationService.updateById(fundDonation);
    }

    /**
     * 删除资金捐赠记录
     */
    @DeleteMapping("/{id}")
    public boolean deleteFundDonation(@PathVariable Long id) {
        return fundDonationService.removeById(id);
    }

    /**
     * 标记资金已支付
     */
    @PutMapping("/{id}/pay")
    public boolean payFundDonation(@PathVariable Long id) {
        FundDonation fundDonation = new FundDonation();
        fundDonation.setId(id);
        fundDonation.setStatus(1); // 1-已支付
        return fundDonationService.updateById(fundDonation);
    }

    /**
     * 标记支付失败
     */
    @PutMapping("/{id}/fail")
    public boolean failFundDonation(@PathVariable Long id) {
        FundDonation fundDonation = new FundDonation();
        fundDonation.setId(id);
        fundDonation.setStatus(2); // 2-支付失败
        return fundDonationService.updateById(fundDonation);
    }
}
