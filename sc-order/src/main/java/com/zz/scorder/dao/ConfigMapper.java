package com.zz.scorder.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zz.scorder.entity.ConfigEntity;
import org.apache.ibatis.annotations.Mapper;

/**
 * ************************************
 * create by Intellij IDEA
 *
 * @author Francis.zz
 * @date 2020-05-21 11:03
 * ************************************
 */
@Mapper
public interface ConfigMapper extends BaseMapper<ConfigEntity> {
    ConfigEntity selectByCardCode(String cardCode);
}
