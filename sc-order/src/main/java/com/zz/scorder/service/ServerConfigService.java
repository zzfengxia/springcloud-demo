package com.zz.scorder.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zz.scorder.dao.ConfigMapper;
import com.zz.scorder.entity.ConfigEntity;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

/**
 * ************************************
 * create by Intellij IDEA
 *
 * @author Francis.zz
 * @date 2020-05-21 11:01
 * ************************************
 */
@Service
public class ServerConfigService extends ServiceImpl<ConfigMapper, ConfigEntity> {
    @Cacheable("config")
    public ConfigEntity getByIssueId(String issueId) {
        return baseMapper.selectOne(new QueryWrapper<ConfigEntity>().eq("card_external_code", issueId));
    }
    
    public ConfigEntity selectByCardCode(String cardCode) {
        return baseMapper.selectByCardCode(cardCode);
    }
}
