package com.beanpattern.service;

import com.beanpattern.entity.UserEntity;
import com.beanpattern.entity.UserInviteRelation;
import com.beanpattern.mapper.OrderMapper;
import com.beanpattern.mapper.UserInviteRelationMapper;
import com.beanpattern.mapper.UserMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.Random;

@Service
public class InviteCodeService {

    private static final String CHARS = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";

    private final UserMapper userMapper;
    private final UserInviteRelationMapper userInviteRelationMapper;
    private final OrderMapper orderMapper;
    private final Random random = new Random();

    public InviteCodeService(UserMapper userMapper,
                             UserInviteRelationMapper userInviteRelationMapper,
                             OrderMapper orderMapper) {
        this.userMapper = userMapper;
        this.userInviteRelationMapper = userInviteRelationMapper;
        this.orderMapper = orderMapper;
    }

    public String ensureInviteCode(UserEntity user) {
        if (user != null && StringUtils.hasText(user.getInviteCode())) {
            return user.getInviteCode();
        }
        String code;
        do {
            code = randomCode(8);
        } while (userMapper.findByInviteCode(code) != null);
        userMapper.updateInviteCode(user.getId(), code);
        user.setInviteCode(code);
        return code;
    }

    @Transactional
    public void bindInviteRelationIfNeeded(Long inviteeUserId, String inviteCode) {
        if (!StringUtils.hasText(inviteCode) || inviteeUserId == null) return;
        UserInviteRelation existing = userInviteRelationMapper.findByInviteeUserId(inviteeUserId);
        if (existing != null) return;

        UserEntity inviter = userMapper.findByInviteCode(inviteCode.trim().toUpperCase(Locale.ROOT));
        if (inviter == null || inviter.getId() == null || inviter.getId().equals(inviteeUserId)) return;

        UserInviteRelation relation = new UserInviteRelation();
        relation.setInviterUserId(inviter.getId());
        relation.setInviteeUserId(inviteeUserId);
        relation.setInviteCode(inviteCode.trim().toUpperCase(Locale.ROOT));
        relation.setStatus(1);
        userInviteRelationMapper.insert(relation);
    }

    @Transactional
    public void bindInviteRelation(Long inviteeUserId, String inviteCode) {
        if (inviteeUserId == null) {
            throw new IllegalArgumentException("用户信息异常");
        }
        if (!StringUtils.hasText(inviteCode)) {
            throw new IllegalArgumentException("请输入邀请码");
        }
        UserInviteRelation existing = userInviteRelationMapper.findByInviteeUserId(inviteeUserId);
        if (existing != null) {
            throw new IllegalArgumentException("你已绑定过邀请关系");
        }

        String normalizedCode = inviteCode.trim().toUpperCase(Locale.ROOT);
        UserEntity inviter = userMapper.findByInviteCode(normalizedCode);
        if (inviter == null || inviter.getId() == null) {
            throw new IllegalArgumentException("邀请码不存在");
        }
        if (inviter.getId().equals(inviteeUserId)) {
            throw new IllegalArgumentException("不能填写自己的邀请码");
        }

        UserInviteRelation relation = new UserInviteRelation();
        relation.setInviterUserId(inviter.getId());
        relation.setInviteeUserId(inviteeUserId);
        relation.setInviteCode(normalizedCode);
        relation.setStatus(1);
        userInviteRelationMapper.insert(relation);
    }

    @Transactional
    public void markInviteeFirstPaid(Long inviteeUserId) {
        UserInviteRelation relation = userInviteRelationMapper.findByInviteeUserId(inviteeUserId);
        if (relation == null || relation.getFirstPaidAt() != null) return;
        if (!orderMapper.existsPaidOrderByUserId(inviteeUserId)) return;
        userInviteRelationMapper.updateStatusAndFirstPaidAt(relation.getId(), 2, LocalDateTime.now());
    }

    public int countInvitedRegistered(Long inviterUserId) {
        List<UserInviteRelation> relations = userInviteRelationMapper.findByInviterUserId(inviterUserId);
        return relations == null ? 0 : relations.size();
    }

    public int countInvitedPaid(Long inviterUserId) {
        List<UserInviteRelation> relations = userInviteRelationMapper.findByInviterUserId(inviterUserId);
        if (relations == null) return 0;
        return (int) relations.stream().filter(item -> item.getFirstPaidAt() != null).count();
    }

    public List<UserInviteRelation> listInvites(Long inviterUserId) {
        return userInviteRelationMapper.findByInviterUserId(inviterUserId);
    }

    private String randomCode(int length) {
        StringBuilder builder = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            builder.append(CHARS.charAt(random.nextInt(CHARS.length())));
        }
        return builder.toString();
    }
}
