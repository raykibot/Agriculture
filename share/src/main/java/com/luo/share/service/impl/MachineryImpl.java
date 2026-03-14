package com.luo.share.service.impl;

import com.alibaba.fastjson2.JSON;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.luo.share.mapper.MachineryMapper;
import com.luo.share.model.dto.MachineryAddDTO;
import com.luo.share.model.entity.Category;
import com.luo.share.model.entity.Machinery;
import com.luo.share.model.vo.MachineryVO;
import com.luo.share.service.IMachineryService;
import org.apache.ibatis.annotations.Param;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

@Service
public class MachineryImpl implements IMachineryService {


    @Autowired
    private MachineryMapper machineryMapper;

    @Override
    public Page<MachineryVO> getMachineryPage(Integer current, Integer size, Long categoryId, String keyword) {

        Page<MachineryVO> page = new Page<>(current, size);
        List<MachineryVO> list = machineryMapper.selectSimpleList(page, categoryId, keyword);
        page.setRecords(list);
        page.setTotal(page.getTotal());
        page.setPages(page.getPages());
        return page;
    }

    @Override
    public List<MachineryVO> getPopularMachinery() {
        return machineryMapper.selectRandomFromEachCategory();
    }

    @Override
    public MachineryVO getById(Long id) {
        return machineryMapper.getMachineryByID(id);
    }

    @Override
    public void publishMachinery(MachineryAddDTO dto) {


        Machinery machinery = new Machinery();
        BeanUtils.copyProperties(dto, machinery);

        if (dto.getParameters() != null && !dto.getParameters().isEmpty()) {
            machinery.setParameters(dto.getParameters());
        }
        machinery.setStatus(0); // 0表示闲置可租
        machinery.setStock(1);  // 默认库存为1
        machinery.setLockedStock(0);
        machinery.setAvgRating(0.0); // 初始满分
        machinery.setCreateTime(new Date());

        machineryMapper.insertMachinery(machinery);
    }

    @Override
    public List<Category> getCategory() {
        return machineryMapper.selectAllCategories();
    }

    @Override
    public List<MachineryVO> getMyPublishedMachinery(Long ownerId) {
        return machineryMapper.selectMyPublishedMachinery(ownerId);
    }
}
