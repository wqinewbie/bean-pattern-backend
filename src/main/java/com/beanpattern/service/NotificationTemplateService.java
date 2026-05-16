package com.beanpattern.service;

import com.beanpattern.entity.NotificationTemplate;
import com.beanpattern.mapper.NotificationTemplateMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

/**
 * 消息模板配置服务
 */
@Service
@RequiredArgsConstructor
public class NotificationTemplateService {

    private final NotificationTemplateMapper notificationTemplateMapper;

    /**
     * 获取所有模板（管理后台使用）
     */
    public List<NotificationTemplate> listAll() {
        return notificationTemplateMapper.listAll();
    }

    /**
     * 获取启用的模板
     */
    public List<NotificationTemplate> listActive() {
        return notificationTemplateMapper.listActive();
    }

    /**
     * 根据ID获取模板
     */
    public NotificationTemplate getById(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("模板ID不能为空");
        }
        NotificationTemplate template = notificationTemplateMapper.findById(id);
        if (template == null) {
            throw new IllegalArgumentException("模板不存在：" + id);
        }
        return template;
    }

    /**
     * 根据编码获取模板
     */
    public NotificationTemplate getByCode(String code) {
        if (code == null || code.trim().isEmpty()) {
            return null;
        }
        return notificationTemplateMapper.findByCode(code);
    }

    /**
     * 创建模板
     */
    @Transactional
    public NotificationTemplate create(NotificationTemplate template) {
        // 验证必填字段
        if (template.getCode() == null || template.getCode().trim().isEmpty()) {
            throw new IllegalArgumentException("模板编码不能为空");
        }
        if (template.getName() == null || template.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("模板名称不能为空");
        }
        if (template.getTitle() == null || template.getTitle().trim().isEmpty()) {
            throw new IllegalArgumentException("标题模板不能为空");
        }
        if (template.getContent() == null || template.getContent().trim().isEmpty()) {
            throw new IllegalArgumentException("内容模板不能为空");
        }

        // 检查编码唯一性
        NotificationTemplate existing = notificationTemplateMapper.findByCode(template.getCode());
        if (existing != null) {
            throw new IllegalArgumentException("模板编码已存在：" + template.getCode());
        }

        // 设置默认值
        if (template.getIcon() == null || template.getIcon().trim().isEmpty()) {
            template.setIcon("🔔");
        }
        if (template.getActionType() == null || template.getActionType().trim().isEmpty()) {
            template.setActionType("NONE");
        }
        if (template.getIsActive() == null) {
            template.setIsActive(true);
        }

        notificationTemplateMapper.insert(template);
        return template;
    }

    /**
     * 更新模板
     */
    @Transactional
    public NotificationTemplate update(NotificationTemplate template) {
        if (template.getId() == null) {
            throw new IllegalArgumentException("模板ID不能为空");
        }

        // 验证模板是否存在
        NotificationTemplate existing = notificationTemplateMapper.findById(template.getId());
        if (existing == null) {
            throw new IllegalArgumentException("模板不存在：" + template.getId());
        }

        // 验证必填字段
        if (template.getName() == null || template.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("模板名称不能为空");
        }
        if (template.getTitle() == null || template.getTitle().trim().isEmpty()) {
            throw new IllegalArgumentException("标题模板不能为空");
        }
        if (template.getContent() == null || template.getContent().trim().isEmpty()) {
            throw new IllegalArgumentException("内容模板不能为空");
        }

        // 设置默认值
        if (template.getIcon() == null || template.getIcon().trim().isEmpty()) {
            template.setIcon("🔔");
        }
        if (template.getActionType() == null || template.getActionType().trim().isEmpty()) {
            template.setActionType("NONE");
        }
        if (template.getIsActive() == null) {
            template.setIsActive(true);
        }

        notificationTemplateMapper.update(template);
        return notificationTemplateMapper.findById(template.getId());
    }

    /**
     * 更新模板状态（启用/禁用）
     */
    @Transactional
    public void updateStatus(Long id, Boolean isActive) {
        if (id == null) {
            throw new IllegalArgumentException("模板ID不能为空");
        }
        if (isActive == null) {
            throw new IllegalArgumentException("状态不能为空");
        }

        NotificationTemplate existing = notificationTemplateMapper.findById(id);
        if (existing == null) {
            throw new IllegalArgumentException("模板不存在：" + id);
        }

        notificationTemplateMapper.updateStatus(id, isActive);
    }

    /**
     * 删除模板
     */
    @Transactional
    public void delete(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("模板ID不能为空");
        }

        NotificationTemplate existing = notificationTemplateMapper.findById(id);
        if (existing == null) {
            throw new IllegalArgumentException("模板不存在：" + id);
        }

        notificationTemplateMapper.deleteById(id);
    }

    /**
     * 渲染模板（变量替换）
     * @param template 模板字符串
     * @param variables 变量映射
     * @return 渲染后的字符串
     */
    public String renderTemplate(String template, Map<String, Object> variables) {
        if (template == null || template.isEmpty()) {
            return template;
        }
        if (variables == null || variables.isEmpty()) {
            return template;
        }

        String result = template;
        for (Map.Entry<String, Object> entry : variables.entrySet()) {
            String placeholder = "{" + entry.getKey() + "}";
            String value = entry.getValue() != null ? String.valueOf(entry.getValue()) : "";
            result = result.replace(placeholder, value);
        }
        return result;
    }
}
