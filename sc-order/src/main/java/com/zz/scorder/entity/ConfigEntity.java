package com.zz.scorder.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * ************************************
 * create by Intellij IDEA
 *
 * @author Francis.zz
 * @date 2020-05-21 11:02
 * ************************************
 */
@TableName("card_code_dict")
@Data
public class ConfigEntity {
    @TableId
    private Integer id;
    /**
     * 卡片外部编码（用于对接其他系统中的卡片唯一标识）
     */
    @NotNull
    private String cardExternalCode;
    /**
     * 流程代码
     */
    private String processCode;
    /**
     * 渠道
     */
    private String sourceChnl;
    /**
     * 卡片编码
     */
    @NotNull
    private String cardCode;
    /**
     * 卡片编码描述，用于创建订单时显示
     */
    private String cardDesc;
    /**
     * 卡片名称
     */
    private String cardName;
    /**
     * 服务器地址
     */
    private String serverUrl;
    
}
