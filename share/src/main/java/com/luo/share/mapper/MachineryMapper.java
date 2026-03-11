package com.luo.share.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.luo.share.model.vo.MachineryVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface MachineryMapper extends BaseMapper<MachineryVO> {

    List<MachineryVO> selectSimpleList(Page<MachineryVO> page,
                                       @Param("categoryId") Long categoryId,
                                       @Param("keyword") String keyword);


    // 随机获取各分类热门机型
    List<MachineryVO> selectRandomFromEachCategory();


    /**
     * 根据分类 ID 查询设备列表
     */
    List<MachineryVO> selectListByCategory(@Param("categoryId") Integer categoryId);
}
