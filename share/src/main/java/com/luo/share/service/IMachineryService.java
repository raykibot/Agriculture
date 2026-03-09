package com.luo.share.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.luo.share.model.vo.MachineryVO;

public interface IMachineryService {

    Page<MachineryVO> getMachineryPage(Integer current, Integer size, Long categoryId, String keyword);

}
