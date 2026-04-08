package com.example.spring_school.controller;

import com.example.spring_school.entity.*;
import com.example.spring_school.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 爱心榜单控制器
 */
@RestController
@RequestMapping("/api/love-rankings")
@SuppressWarnings("unchecked")
public class LoveRankingController {

    @Autowired
    private FundDonationService fundDonationService;

    @Autowired
    private ItemDonationService itemDonationService;

    @Autowired
    private PublicActivityApplicationService publicActivityApplicationService;

    @Autowired
    private UserService userService;

    @Autowired
    private RedisService redisService;

    private static final String DONATION_RANKING_KEY = "love_ranking:donation";
    private static final String ITEM_RANKING_KEY = "love_ranking:item";
    private static final String ACTIVITY_RANKING_KEY = "love_ranking:activity";
    private static final long CACHE_DURATION = 5;

    /**
     * 获取捐款榜单（前10名）
     */
    @GetMapping("/donation")
    public List<Map<String, Object>> getDonationRanking() {
        // 尝试从Redis缓存中获取数据
        Object cachedData = redisService.get(DONATION_RANKING_KEY);
        if (cachedData != null && cachedData instanceof List) {
            return (List<Map<String, Object>>) cachedData;
        }
        
        // 获取所有已支付的资金捐赠记录
        List<FundDonation> donations = fundDonationService.listByStatus(1);
        
        // 按用户ID分组并计算总金额
        Map<Long, Double> userDonationMap = new HashMap<>();
        for (FundDonation donation : donations) {
            userDonationMap.put(donation.getDonorId(), 
                userDonationMap.getOrDefault(donation.getDonorId(), 0.0) + donation.getAmount().doubleValue());
        }
        
        // 转换为列表并排序
        List<Map.Entry<Long, Double>> sortedEntries = userDonationMap.entrySet().stream()
            .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
            .limit(10)
            .collect(Collectors.toList());
        
        // 构建返回结果
        List<Map<String, Object>> ranking = new ArrayList<>();
        int rank = 1;
        for (Map.Entry<Long, Double> entry : sortedEntries) {
            User user = userService.getById(entry.getKey());
            if (user != null) {
                Map<String, Object> item = new HashMap<>();
                item.put("id", user.getId());
                item.put("name", user.getName());
                item.put("amount", entry.getValue());
                item.put("rank", rank++);
                ranking.add(item);
            }
        }
        
        // 将结果存入Redis缓存，设置5分钟过期
        redisService.set(DONATION_RANKING_KEY, ranking, CACHE_DURATION, TimeUnit.MINUTES);
        
        return ranking;
    }

    /**
     * 获取捐物榜单（前10名）
     */
    @GetMapping("/item")
    public List<Map<String, Object>> getItemRanking() {
        // 尝试从Redis缓存中获取数据
        Object cachedData = redisService.get(ITEM_RANKING_KEY);
        if (cachedData != null && cachedData instanceof List) {
            return (List<Map<String, Object>>) cachedData;
        }
        
        // 获取所有已通过和已寄出的物品捐赠记录
        List<ItemDonation> donations = new ArrayList<>();
        donations.addAll(itemDonationService.listByStatus(1)); // 已通过
        donations.addAll(itemDonationService.listByStatus(3)); // 已寄出
        System.out.println("Total item donations: " + donations.size());
        
        // 按用户ID分组并计算总数量
        Map<Long, Integer> userItemMap = new HashMap<>();
        for (ItemDonation donation : donations) {
            userItemMap.put(donation.getDonorId(), 
                userItemMap.getOrDefault(donation.getDonorId(), 0) + donation.getQuantity());
        }
        System.out.println("Total users with donations: " + userItemMap.size());
        
        // 转换为列表并排序
        List<Map.Entry<Long, Integer>> sortedEntries = userItemMap.entrySet().stream()
            .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
            .limit(10)
            .collect(Collectors.toList());
        System.out.println("Top users: " + sortedEntries.size());
        
        // 构建返回结果
        List<Map<String, Object>> ranking = new ArrayList<>();
        int rank = 1;
        for (Map.Entry<Long, Integer> entry : sortedEntries) {
            User user = userService.getById(entry.getKey());
            if (user != null) {
                Map<String, Object> item = new HashMap<>();
                item.put("id", user.getId());
                item.put("name", user.getName());
                item.put("quantity", entry.getValue());
                item.put("rank", rank++);
                ranking.add(item);
            }
        }
        System.out.println("Final ranking size: " + ranking.size());
        
        // 将结果存入Redis缓存，设置5分钟过期
        redisService.set(ITEM_RANKING_KEY, ranking, CACHE_DURATION, TimeUnit.MINUTES);
        
        return ranking;
    }

    /**
     * 获取活动参与榜单（前10名）
     */
    @GetMapping("/activity")
    public List<Map<String, Object>> getActivityRanking() {
        // 尝试从Redis缓存中获取数据
        Object cachedData = redisService.get(ACTIVITY_RANKING_KEY);
        if (cachedData != null && cachedData instanceof List) {
            return (List<Map<String, Object>>) cachedData;
        }
        
        // 获取所有已通过的活动申请
        List<PublicActivityApplication> applications = publicActivityApplicationService.listByStatus(1);
        
        // 按用户ID分组并计算参与次数
        Map<Long, Integer> userActivityMap = new HashMap<>();
        for (PublicActivityApplication application : applications) {
            userActivityMap.put(application.getUserId(), 
                userActivityMap.getOrDefault(application.getUserId(), 0) + 1);
        }
        
        // 转换为列表并排序
        List<Map.Entry<Long, Integer>> sortedEntries = userActivityMap.entrySet().stream()
            .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
            .limit(10)
            .collect(Collectors.toList());
        
        // 构建返回结果
        List<Map<String, Object>> ranking = new ArrayList<>();
        int rank = 1;
        for (Map.Entry<Long, Integer> entry : sortedEntries) {
            User user = userService.getById(entry.getKey());
            if (user != null) {
                Map<String, Object> item = new HashMap<>();
                item.put("id", user.getId());
                item.put("name", user.getName());
                item.put("times", entry.getValue());
                item.put("rank", rank++);
                ranking.add(item);
            }
        }
        
        // 将结果存入Redis缓存，设置5分钟过期
        redisService.set(ACTIVITY_RANKING_KEY, ranking, CACHE_DURATION, TimeUnit.MINUTES);
        
        return ranking;
    }
}