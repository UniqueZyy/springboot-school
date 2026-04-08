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
import com.example.spring_school.service.RedisService;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/api/statistics")
@CrossOrigin(origins = "*")
@SuppressWarnings("unchecked")
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

    @Autowired
    private RedisService redisService;

    private static final String DASHBOARD_SUMMARY_KEY = "statistics:dashboard_summary";
    private static final String MONTHLY_FUND_DONATION_KEY = "statistics:monthly_fund_donation";
    private static final String ITEM_DONATION_BY_TYPE_KEY = "statistics:item_donation_by_type";
    private static final String DONATION_DISTRIBUTION_KEY = "statistics:donation_distribution";
    private static final String ACTIVITY_PARTICIPATION_BY_TYPE_KEY = "statistics:activity_participation_by_type";
    private static final long CACHE_DURATION = 5;

    @GetMapping("/dashboard-summary")
    public Map<String, Object> getDashboardSummary() {
        // 尝试从Redis缓存中获取数据
        Object cachedData = redisService.get(DASHBOARD_SUMMARY_KEY);
        if (cachedData != null && cachedData instanceof Map) {
            return (Map<String, Object>) cachedData;
        }
        
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
        
        // 将结果存入Redis缓存，设置5分钟过期
        try {
            redisService.set(DASHBOARD_SUMMARY_KEY, summary, CACHE_DURATION, TimeUnit.MINUTES);
        } catch (Exception e) {
            System.err.println("Redis set 失败，继续返回数据: " + e.getMessage());
        }
        
        return summary;
    }

    @GetMapping("/monthly-fund-donation")
    public List<Map<String, Object>> getMonthlyFundDonation() {
        // 尝试从Redis缓存中获取数据
        Object cachedData = redisService.get(MONTHLY_FUND_DONATION_KEY);
        if (cachedData != null && cachedData instanceof List) {
            return (List<Map<String, Object>>) cachedData;
        }
        
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

        // 将结果存入Redis缓存，设置5分钟过期
        try {
            redisService.set(MONTHLY_FUND_DONATION_KEY, result, CACHE_DURATION, TimeUnit.MINUTES);
        } catch (Exception e) {
            System.err.println("Redis set 失败，继续返回数据: " + e.getMessage());
        }
        
        return result;
    }

    @GetMapping("/item-donation-by-type")
    public List<Map<String, Object>> getItemDonationByType() {
        // 尝试从Redis缓存中获取数据
        Object cachedData = redisService.get(ITEM_DONATION_BY_TYPE_KEY);
        if (cachedData != null && cachedData instanceof List) {
            return (List<Map<String, Object>>) cachedData;
        }
        
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

        // 将结果存入Redis缓存，设置5分钟过期
        try {
            redisService.set(ITEM_DONATION_BY_TYPE_KEY, result, CACHE_DURATION, TimeUnit.MINUTES);
        } catch (Exception e) {
            System.err.println("Redis set 失败，继续返回数据: " + e.getMessage());
        }
        
        return result;
    }

    @GetMapping("/donation-distribution")
    public Map<String, Object> getDonationDistribution() {
        // 尝试从Redis缓存中获取数据
        Object cachedData = redisService.get(DONATION_DISTRIBUTION_KEY);
        if (cachedData != null && cachedData instanceof Map) {
            return (Map<String, Object>) cachedData;
        }
        
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

        // 将结果存入Redis缓存，设置5分钟过期
        try {
            redisService.set(DONATION_DISTRIBUTION_KEY, result, CACHE_DURATION, TimeUnit.MINUTES);
        } catch (Exception e) {
            System.err.println("Redis set 失败，继续返回数据: " + e.getMessage());
        }
        
        return result;
    }

    @GetMapping("/activity-participation-by-type")
    public List<Map<String, Object>> getActivityParticipationByType() {
        // 尝试从Redis缓存中获取数据
        Object cachedData = redisService.get(ACTIVITY_PARTICIPATION_BY_TYPE_KEY);
        if (cachedData != null && cachedData instanceof List) {
            return (List<Map<String, Object>>) cachedData;
        }
        
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

        // 将结果存入Redis缓存，设置5分钟过期
        try {
            redisService.set(ACTIVITY_PARTICIPATION_BY_TYPE_KEY, result, CACHE_DURATION, TimeUnit.MINUTES);
        } catch (Exception e) {
            System.err.println("Redis set 失败，继续返回数据: " + e.getMessage());
        }
        
        return result;
    }

    // 用户个人捐赠统计接口

    @GetMapping("/user/{userId}/summary")
    public Map<String, Object> getUserDonationSummary(@PathVariable Long userId) {
        Map<String, Object> summary = new HashMap<>();
        
        // 统计用户的总捐赠金额
        BigDecimal totalFundAmount = fundDonationService.lambdaQuery()
                .eq(FundDonation::getStatus, 1)
                .eq(FundDonation::getDonorId, userId)
                .list()
                .stream()
                .map(FundDonation::getAmount)
                .filter(amount -> amount != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        summary.put("totalFundAmount", totalFundAmount);
        
        // 统计用户的总捐赠物品数量
        Integer totalItemCount = itemDonationService.lambdaQuery()
                .in(ItemDonation::getStatus, Arrays.asList(1, 3))
                .eq(ItemDonation::getDonorId, userId)
                .list()
                .stream()
                .mapToInt(item -> item.getQuantity() != null ? item.getQuantity() : 0)
                .sum();
        summary.put("totalItemCount", totalItemCount);
        
        // 统计用户参与的活动数量
        Long totalActivityCount = publicActivityApplicationService.lambdaQuery()
                .eq(PublicActivityApplication::getStatus, 1)
                .eq(PublicActivityApplication::getUserId, userId)
                .count();
        summary.put("totalActivityCount", totalActivityCount);
        
        // 统计用户的总捐赠次数
        Long totalDonationCount = fundDonationService.lambdaQuery()
                .eq(FundDonation::getStatus, 1)
                .eq(FundDonation::getDonorId, userId)
                .count()
                + itemDonationService.lambdaQuery()
                .in(ItemDonation::getStatus, Arrays.asList(1, 3))
                .eq(ItemDonation::getDonorId, userId)
                .count();
        summary.put("totalDonationCount", totalDonationCount);
        
        return summary;
    }

    @GetMapping("/user/{userId}/monthly-fund")
    public List<Map<String, Object>> getUserMonthlyFundDonation(@PathVariable Long userId) {
        List<Map<String, Object>> result = new ArrayList<>();

        LocalDateTime now = LocalDateTime.now();
        int currentYear = now.getYear();
        int currentMonth = now.getMonthValue();

        // 只查询当前月份的数据
        List<FundDonation> paidDonations = fundDonationService.lambdaQuery()
                .eq(FundDonation::getStatus, 1)
                .eq(FundDonation::getDonorId, userId)
                .ge(FundDonation::getDonationDate, LocalDateTime.of(currentYear, currentMonth, 1, 0, 0))
                .lt(FundDonation::getDonationDate, LocalDateTime.of(currentYear, currentMonth + 1, 1, 0, 0))
                .list();

        System.out.println("=== 用户当月捐赠资金统计调试 ===");
        System.out.println("用户ID: " + userId);
        System.out.println("查询月份: " + currentYear + "-" + currentMonth);
        System.out.println("已支付捐赠记录数: " + paidDonations.size());

        // 生成当月的日期标签
        int daysInMonth = now.getMonth().length(now.getYear() % 4 == 0 && now.getYear() % 100 != 0 || now.getYear() % 400 == 0);
        for (int day = 1; day <= daysInMonth; day += 5) { // 每5天一个点
            Map<String, Object> dayData = new HashMap<>();
            dayData.put("day", day);
            dayData.put("label", day + "日");

            final int targetDay = day;
            final int endDay = Math.min(day + 4, daysInMonth);

            BigDecimal dayAmount = paidDonations.stream()
                    .filter(d -> {
                        if (d.getDonationDate() == null) return false;
                        int dayOfMonth = d.getDonationDate().getDayOfMonth();
                        return dayOfMonth >= targetDay && dayOfMonth <= endDay;
                    })
                    .map(FundDonation::getAmount)
                    .filter(amount -> amount != null)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            dayData.put("amount", dayAmount.doubleValue());
            result.add(dayData);

            System.out.println("日期: " + day + "-" + endDay + ", 金额: " + dayAmount.doubleValue());
        }

        return result;
    }

    @GetMapping("/user/{userId}/item-by-type")
    public List<Map<String, Object>> getUserItemDonationByType(@PathVariable Long userId) {
        List<Map<String, Object>> result = new ArrayList<>();

        // 查询用户已通过(status=1)和已寄出(status=3)的物品捐赠
        List<ItemDonation> validItems = itemDonationService.lambdaQuery()
                .in(ItemDonation::getStatus, Arrays.asList(1, 3))
                .eq(ItemDonation::getDonorId, userId)
                .list();

        System.out.println("=== 用户物品捐赠统计调试 ===");
        System.out.println("用户ID: " + userId);
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

    @GetMapping("/user/{userId}/activity-participation")
    public List<Map<String, Object>> getUserActivityParticipation(@PathVariable Long userId) {
        List<Map<String, Object>> result = new ArrayList<>();

        List<PublicActivityType> activityTypes = publicActivityTypeService.lambdaQuery()
                .eq(PublicActivityType::getStatus, 1)
                .list();

        List<PublicActivity> activities = publicActivityService.list();

        // 只统计用户已通过状态的申请
        List<PublicActivityApplication> approvedApplications = publicActivityApplicationService.lambdaQuery()
                .eq(PublicActivityApplication::getStatus, 1)
                .eq(PublicActivityApplication::getUserId, userId)
                .list();

        System.out.println("=== 用户活动参与统计调试 ===");
        System.out.println("用户ID: " + userId);
        System.out.println("活动类型数: " + activityTypes.size());
        System.out.println("活动总数: " + activities.size());
        System.out.println("已通过申请数: " + approvedApplications.size());

        // 建立活动ID到类型ID的映射
        Map<Long, Long> activityTypeMap = activities.stream()
                .collect(Collectors.toMap(PublicActivity::getId, PublicActivity::getTypeId, (a, b) -> a));

        // 统计每个类型的参与次数
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
            System.out.println("类型: " + type.getTypeName() + ", 参与次数: " + count);
        }

        return result;
    }

    @GetMapping("/user/{userId}/donation-distribution")
    public Map<String, Object> getUserDonationDistribution(@PathVariable Long userId) {
        Map<String, Object> result = new HashMap<>();

        // 统计用户已支付的捐赠资金
        BigDecimal totalFundAmount = fundDonationService.lambdaQuery()
                .eq(FundDonation::getStatus, 1)
                .eq(FundDonation::getDonorId, userId)
                .list()
                .stream()
                .map(FundDonation::getAmount)
                .filter(amount -> amount != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // 统计用户已通过(status=1)和已寄出(status=3)的物品捐赠数量
        Integer totalItemCount = itemDonationService.lambdaQuery()
                .in(ItemDonation::getStatus, Arrays.asList(1, 3))
                .eq(ItemDonation::getDonorId, userId)
                .list()
                .stream()
                .mapToInt(item -> item.getQuantity() != null ? item.getQuantity() : 0)
                .sum();

        System.out.println("=== 用户捐赠分布统计调试 ===");
        System.out.println("用户ID: " + userId);
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
}
