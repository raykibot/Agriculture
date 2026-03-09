package com.luo.share.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.luo.share.common.api.Result;
import com.luo.share.model.vo.MachineryVO;
import com.luo.share.service.IMachineryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

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

        log.info("产品展示");

        return Result.success(machineryService.getMachineryPage(current, size, categoryId, keyword));
    }
}
