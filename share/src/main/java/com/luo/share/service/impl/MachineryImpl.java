package com.luo.share.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.luo.share.mapper.MachineryMapper;
import com.luo.share.model.vo.MachineryVO;
import com.luo.share.service.IMachineryService;
import org.apache.ibatis.annotations.Param;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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


}
