package com.luo.share.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.luo.share.model.dto.MachineryAddDTO;
import com.luo.share.model.entity.Category;
import com.luo.share.model.vo.MachineryVO;

import java.util.List;

public interface IMachineryService {

    Page<MachineryVO> getMachineryPage(Integer current, Integer size, Long categoryId, String keyword);

    List<MachineryVO> getPopularMachinery();


    MachineryVO getById(Long id);

    void publishMachinery(MachineryAddDTO dto);

    List<Category> getCategory();

    // 接口
    List<MachineryVO> getMyPublishedMachinery(Long ownerId);

}
