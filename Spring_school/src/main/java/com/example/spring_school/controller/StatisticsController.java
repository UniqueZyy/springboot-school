package com.example.spring_school.controller;

import com.example.spring_school.entity.FundDonation;
import com.example.spring_school.entity.ItemDonation;
import com.example.spring_school.entity.PublicActivity;
import com.example.spring_school.entity.PublicActivityApplication;
import com.example.spring_school.entity.PublicActivityType;
import com.example.spring_school.service.FundDonationService;
import com.example.spring_school.service.ItemDonationService;
import com.example.spring_school.service.PublicActivityApplicationService;
import com.example.spring_school.service.PublicActivityService;
import com.example.spring_school.service.PublicActivityTypeService;
import com.example.spring_school.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/statistics")
@CrossOrigin(origins = "*")
public class StatisticsController {

    @Autowired
    private FundDonationService fundDonationService;

    @Autowired
    private ItemDonationService itemDonationService;

    @Autowired
    private PublicActivityApplicationService publicActivityApplicationService;

    @Autowired
    private PublicActivityService publicActivityService;

    @Autowired
    private PublicActivityTypeService publicActivityTypeService;

    @Autowired
    private UserService userService;

    @GetMapping("/dashboard-summary")
    public Map<String, Object> getDashboardSummary() {
        Map<String, Object> summary = new HashMap<>();
        
        BigDecimal totalFundAmount = fundDonationService.lambdaQuery()
                .eq(FundDonation::getStatus, 1)
                .list()
                .stream()
                .map(FundDonation::getAmount)
                .filter(amount -> amount != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        summary.put("totalFundAmount", totalFundAmount);
        
        Integer totalItemCount = itemDonationService.lambdaQuery()
                .in(ItemDonation::getStatus, Arrays.asList(1, 3))
                .list()
                .stream()
                .mapToInt(item -> item.getQuantity() != null ? item.getQuantity() : 0)
                .sum();
        summary.put("totalItemCount", totalItemCount);
        
        Long totalActivityCount = publicActivityService.lambdaQuery()
                .count();
        summary.put("totalActivityCount", totalActivityCount);
        
        Long totalApplicationCount = publicActivityApplicationService.lambdaQuery()
                .eq(PublicActivityApplication::getStatus, 1)
                .count();
        summary.put("totalApplicationCount", totalApplicationCount);
        
        Long totalStudentCount = userService.lambdaQuery()
                .ne(com.example.spring_school.entity.User::getRoleType, 5)
                .count();
        summary.put("totalStudentCount", totalStudentCount);
        
        return summary;
    }

    @GetMapping("/monthly-fund-donation")
    public List<Map<String, Object>> getMonthlyFundDonation() {
        List<Map<String, Object>> result = new ArrayList<>();

        LocalDateTime now = LocalDateTime.now();
        int currentYear = now.getYear();
        int lastYear = currentYear - 1;

        // 只查询上一年度的数据
        List<FundDonation> paidDonations = fundDonationService.lambdaQuery()
                .eq(FundDonation::getStatus, 1)
                .ge(FundDonation::getDonationDate, LocalDateTime.of(lastYear, 1, 1, 0, 0))
                .lt(FundDonation::getDonationDate, LocalDateTime.of(currentYear, 1, 1, 0, 0))
                .list();

        System.out.println("=== 捐赠资金统计调试 ===");
        System.out.println("查询年份: " + lastYear);
        System.out.println("已支付捐赠记录数: " + paidDonations.size());

        for (int month = 1; month <= 12; month++) {
            Map<String, Object> monthData = new HashMap<>();
            monthData.put("year", lastYear);
            monthData.put("month", month);
            monthData.put("label", lastYear + "-" + String.format("%02d", month));

            final int targetMonth = month;

            BigDecimal monthAmount = paidDonations.stream()
                    .filter(d -> {
                        if (d.getDonationDate() == null) return false;
                        return d.getDonationDate().getYear() == lastYear
                                && d.getDonationDate().getMonthValue() == targetMonth;
                    })
                    .map(FundDonation::getAmount)
                    .filter(amount -> amount != null)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            monthData.put("amount", monthAmount.doubleValue());
            result.add(monthData);

            System.out.println("月份: " + month + ", 金额: " + monthAmount.doubleValue());
        }

        return result;
    }

    @GetMapping("/item-donation-by-type")
    public List<Map<String, Object>> getItemDonationByType() {
        List<Map<String, Object>> result = new ArrayList<>();

        // 查询已通过(status=1)和已寄出(status=3)的物品捐赠
        List<ItemDonation> validItems = itemDonationService.lambdaQuery()
                .in(ItemDonation::getStatus, Arrays.asList(1, 3))
                .list();

        System.out.println("=== 物品捐赠统计调试 ===");
        System.out.println("有效物品捐赠记录数: " + validItems.size());

        Map<String, Integer> typeCountMap = validItems.stream()
                .collect(Collectors.groupingBy(
                        item -> item.getItemType() != null ? item.getItemType() : "其他",
                        Collectors.summingInt(item -> item.getQuantity() != null ? item.getQuantity() : 0)
                ));

        // 确保所有类型都显示，即使没有数据也显示0
        List<String> allTypes = Arrays.asList("书籍", "衣物", "学习用品", "生活用品", "其他");
        for (String type : allTypes) {
            Map<String, Object> typeData = new HashMap<>();
            typeData.put("itemType", type);
            int count = typeCountMap.getOrDefault(type, 0);
            typeData.put("quantity", count);
            result.add(typeData);
            System.out.println("类型: " + type + ", 数量: " + count);
        }

        return result;
    }

    @GetMapping("/donation-distribution")
    public Map<String, Object> getDonationDistribution() {
        Map<String, Object> result = new HashMap<>();

        // 统计已支付的捐赠资金
        BigDecimal totalFundAmount = fundDonationService.lambdaQuery()
                .eq(FundDonation::getStatus, 1)
                .list()
                .stream()
                .map(FundDonation::getAmount)
                .filter(amount -> amount != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // 统计已通过(status=1)和已寄出(status=3)的物品捐赠数量
        Integer totalItemCount = itemDonationService.lambdaQuery()
                .in(ItemDonation::getStatus, Arrays.asList(1, 3))
                .list()
                .stream()
                .mapToInt(item -> item.getQuantity() != null ? item.getQuantity() : 0)
                .sum();

        System.out.println("=== 捐赠分布统计调试 ===");
        System.out.println("捐赠资金总额: " + totalFundAmount.doubleValue());
        System.out.println("捐赠物品总数: " + totalItemCount);

        List<Map<String, Object>> distribution = new ArrayList<>();

        Map<String, Object> fundData = new HashMap<>();
        fundData.put("name", "捐赠资金");
        fundData.put("value", totalFundAmount.doubleValue());
        distribution.add(fundData);

        Map<String, Object> itemData = new HashMap<>();
        itemData.put("name", "捐赠物品");
        itemData.put("value", totalItemCount);
        distribution.add(itemData);

        result.put("distribution", distribution);
        result.put("totalFundAmount", totalFundAmount.doubleValue());
        result.put("totalItemCount", totalItemCount);

        return result;
    }

    @GetMapping("/activity-participation-by-type")
    public List<Map<String, Object>> getActivityParticipationByType() {
        List<Map<String, Object>> result = new ArrayList<>();

        List<PublicActivityType> activityTypes = publicActivityTypeService.lambdaQuery()
                .eq(PublicActivityType::getStatus, 1)
                .list();

        List<PublicActivity> activities = publicActivityService.list();

        // 只统计已通过状态的申请
        List<PublicActivityApplication> approvedApplications = publicActivityApplicationService.lambdaQuery()
                .eq(PublicActivityApplication::getStatus, 1)
                .list();

        System.out.println("=== 活动参与统计调试 ===");
        System.out.println("活动类型数: " + activityTypes.size());
        System.out.println("活动总数: " + activities.size());
        System.out.println("已通过申请数: " + approvedApplications.size());

        // 建立活动ID到类型ID的映射
        Map<Long, Long> activityTypeMap = activities.stream()
                .collect(Collectors.toMap(PublicActivity::getId, PublicActivity::getTypeId, (a, b) -> a));

        // 统计每个类型的参与人数
        Map<Long, Long> participationByType = new HashMap<>();

        for (PublicActivityApplication app : approvedApplications) {
            Long activityId = app.getActivityId();
            Long typeId = activityTypeMap.get(activityId);
            if (typeId != null) {
                participationByType.merge(typeId, 1L, (a, b) -> Long.valueOf(a.longValue() + b.longValue()));
                System.out.println("申请ID: " + app.getId() + ", 活动ID: " + activityId + ", 类型ID: " + typeId);
            } else {
                System.out.println("警告: 找不到活动ID " + activityId + " 对应的类型");
            }
        }

        for (PublicActivityType type : activityTypes) {
            Map<String, Object> typeData = new HashMap<>();
            typeData.put("typeId", type.getId());
            typeData.put("typeName", type.getTypeName());
            Long count = participationByType.getOrDefault(type.getId(), 0L);
            typeData.put("participantCount", count.intValue());
            result.add(typeData);
            System.out.println("类型: " + type.getTypeName() + ", 参与人数: " + count);
        }

        return result;
    }
}
