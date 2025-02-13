package com.cn.entity;

import com.cn.config.AbstractDateAudit;
import com.cn.enums.GenderEnum;
import com.cn.enums.UserStatusEnum;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.SQLRestriction;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.CredentialsContainer;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import jakarta.persistence.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * 客服管理员实体
 *
 * @author ngcly
 * @since 2017-12-30 16:48
 */
@Getter
@Setter
@Entity
@Table(name = "manager")
@JsonIgnoreProperties(value = {"roleList", "handler", "hibernateLazyInitializer", "fieldHandler"})
@NamedEntityGraph(name = "Role.Graph", attributeNodes = {
        @NamedAttributeNode(value = "roleList", subgraph = "Permission.Graph")
},
        subgraphs = {@NamedSubgraph(name = "Permission.Graph", attributeNodes = {
                @NamedAttributeNode("permissions")
        })}
)
public class Manager extends AbstractDateAudit implements UserDetails, CredentialsContainer {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 用户名
     */
    @Column(length = 16, unique = true, nullable = false)
    private String username;

    /**
     * 密码
     */
    @Column(length = 120, nullable = false)
    private String password;

    /**
     * 性别
     */
    @Enumerated(EnumType.STRING)
    private GenderEnum gender;

    /**
     * 真实姓名
     */
    @Column(length = 8)
    private String realName;

    /**
     * 头像
     */
    private String avatar;

    /**
     * 生日
     */
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate birthday;

    /**
     * 状态
     */
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private UserStatusEnum state;

    /**
     * 一个用户具有多个角色
     * 此处使用set 会比list 性能好 同时也能避免一些重复数据的坑
     * jpa官方也建议使用set 因为list在一对多、多对多 等场景下有很多各种各样的坑
     */
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "manager_role",
            joinColumns = @JoinColumn(name = "manager_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    @SQLRestriction("available=true")
    private Set<Role> roleList;

    @Transient
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime beginTime;
    @Transient
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime endTime;
    @Transient
    private Long[] roleIds;

    public static final String ADMIN = "administrator";

    /**
     * 加载权限
     */
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        Collection<GrantedAuthority> authorities = new HashSet<>();
        SimpleGrantedAuthority authority;
        for (Role role : roleList) {
            authority = new SimpleGrantedAuthority(String.join("_", "ROLE", role.getRoleCode()));
            authorities.add(authority);
            for (Permission permission : role.getPermissions()) {
                authority = new SimpleGrantedAuthority(String.join("_", permission.getHttpMethod(),
                        permission.getUrl()));
                authorities.add(authority);
            }
        }
        return authorities;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return UserStatusEnum.LOCKED != state;
    }

    @Override
    public void eraseCredentials() {
        this.password = null;
    }
}
