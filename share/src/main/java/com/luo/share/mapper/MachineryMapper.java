package com.luo.share.mapper;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.luo.share.model.vo.MachineryVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface MachineryMapper {

    List<MachineryVO> selectSimpleList(Page<MachineryVO> page,
                                       @Param("categoryId") Long categoryId,
                                       @Param("keyword") String keyword);
}
