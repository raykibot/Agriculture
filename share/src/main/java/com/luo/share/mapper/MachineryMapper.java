package com.luo.share.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.luo.share.model.entity.Machinery;
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

    MachineryVO getMachineryByID(Long id);

    /**
     * 1. 纯手写：根据ID查询农机信息
     */
    Machinery selectMachineryById(@Param("id") Long id);

    /**
     * 2. 纯手写：锁定库存（乐观锁防超卖核心）
     * 只有 stock > 0 时才会更新成功，返回影响行数
     */
    int lockMachineryStock(@Param("id") Long id);

    /**
     * 释放机器库存：超时或取消时调用
     */
    int releaseMachineryStock(@Param("id") Long id);
}
