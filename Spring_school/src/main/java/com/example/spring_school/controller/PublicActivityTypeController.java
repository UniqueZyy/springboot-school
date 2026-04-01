package com.example.spring_school.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.spring_school.entity.PublicActivityType;
import com.example.spring_school.service.PublicActivityTypeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/activity-types")
public class PublicActivityTypeController {

    @Autowired
    private PublicActivityTypeService publicActivityTypeService;

    @GetMapping
    public List<PublicActivityType> getAllTypes() {
        return publicActivityTypeService.list();
    }

    @GetMapping("/{id}")
    public PublicActivityType getTypeById(@PathVariable Long id) {
        return publicActivityTypeService.getById(id);
    }

    @PostMapping
    public boolean createType(@RequestBody PublicActivityType type) {
        return publicActivityTypeService.save(type);
    }

    @PutMapping("/{id}")
    public boolean updateType(@PathVariable Long id, @RequestBody PublicActivityType type) {
        type.setId(id);
        return publicActivityTypeService.updateById(type);
    }

    @DeleteMapping("/{id}")
    public boolean deleteType(@PathVariable Long id) {
        return publicActivityTypeService.removeById(id);
    }

    @GetMapping("/page")
    public Page<PublicActivityType> pageTypes(@RequestParam(defaultValue = "1") Integer current,
                                               @RequestParam(defaultValue = "10") Integer size) {
        Page<PublicActivityType> page = new Page<>(current, size);
        return publicActivityTypeService.page(page);
    }

    @GetMapping("/status/{status}")
    public List<PublicActivityType> getTypesByStatus(@PathVariable Integer status) {
        return publicActivityTypeService.lambdaQuery()
                .eq(PublicActivityType::getStatus, status)
                .list();
    }

    @GetMapping("/search")
    public List<PublicActivityType> searchTypes(@RequestParam String keyword) {
        return publicActivityTypeService.lambdaQuery()
                .and(wrapper -> wrapper
                        .like(PublicActivityType::getTypeName, keyword)
                        .or()
                        .like(PublicActivityType::getDescription, keyword))
                .list();
    }
}
