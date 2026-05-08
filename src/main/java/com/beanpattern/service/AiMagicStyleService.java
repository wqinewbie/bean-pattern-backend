package com.beanpattern.service;

import com.beanpattern.entity.AiMagicStyle;
import com.beanpattern.mapper.AiMagicStyleMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

/**
 * AI魔法风格服务
 */
@Service
public class AiMagicStyleService {

    @Autowired
    private AiMagicStyleMapper mapper;

    /**
     * 获取启用的风格列表（小程序端使用）
     */
    public List<AiMagicStyle> getEnabledStyles() {
        return mapper.findEnabled();
    }

    /**
     * 获取所有风格列表（管理端使用）
     */
    public List<AiMagicStyle> getAllStyles() {
        return mapper.findAll();
    }

    /**
     * 保存风格
     */
    public void save(AiMagicStyle style) {
        if (style.getId() == null) {
            // 新增
            if (style.getSortOrder() == null) {
                style.setSortOrder(0);
            }
            if (style.getEnabled() == null) {
                style.setEnabled(1);
            }
            mapper.insert(style);
        } else {
            // 更新
            mapper.update(style);
        }
    }

    /**
     * 删除风格
     */
    public void delete(Long id) {
        mapper.deleteById(id);
    }

    /**
     * 根据风格名称获取提示词模板（正式版AI服务使用）
     */
    public String getPromptTemplate(String styleName) {
        AiMagicStyle style = mapper.findByName(styleName);
        if (style != null && style.getPromptTemplate() != null) {
            return style.getPromptTemplate();
        }
        return "";
    }
}
