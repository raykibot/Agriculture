package com.luo.share.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.luo.share.common.api.Result;
import com.luo.share.model.vo.MachineryVO;
import com.luo.share.service.IMachineryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.crypto.Mac;
import java.util.List;

@RestController
@RequestMapping("/api/machinery")
@CrossOrigin
@Slf4j
public class MachineryController {

    @Autowired
    private IMachineryService machineryService;

    //简单查询器械页面
    @GetMapping("/list")
    public Result<Page<MachineryVO>> listMachinery(
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String keyword) {

        log.info("分类产品展示");
        return Result.success(machineryService.getMachineryPage(current, size, categoryId, keyword));
    }


    @GetMapping("/popular")
    public Result<List<MachineryVO>> getPopularList() {
        List<MachineryVO> data = machineryService.getPopularMachinery();
        log.info("首页产品展示");
        return Result.success(data);
    }

    /**
     * 根据 ID 获取农机详情
     */
    @GetMapping("/detail/{id}")
    public Result<MachineryVO> getMachineryDetail(@PathVariable("id") Long id) {
        // 直接使用 MyBatis-Plus 默认的 getById 方法查询数据库
        MachineryVO machinery = machineryService.getById(id);
        if (machinery == null) {
            return Result.failed(500, "该设备不存在或已被下架");
        }
        return Result.success(machinery);
    }


}
