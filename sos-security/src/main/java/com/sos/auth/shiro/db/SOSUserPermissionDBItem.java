package com.sos.auth.shiro.db;

import javax.persistence.*;

import com.sos.hibernate.classes.DbItem;

@Entity
@Table(name = "SOS_USER_PERMISSION")
public class SOSUserPermissionDBItem extends DbItem {

    private Long id;
    private Long roleId;
    private Long userId;
    private String sosUserPermission;

    public SOSUserPermissionDBItem() {}

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "[ID]")
    public Long getId() {
        return id;
    }

    @Column(name = "[ID]")
    public void setId(Long id) {
        this.id = id;
    }

    public void setSosUserPermission(String sosUserPermission) {
        this.sosUserPermission = sosUserPermission;
    }

    @Column(name = "[SOS_USER_PERMISSION`", nullable = false)
    public String getSosUserPermission() {
        return sosUserPermission;
    }

    @Column(name = "[USER_ID]")
    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    @Column(name = "[ROLE_ID]")
    public Long getRoleId() {
        return roleId;
    }

    public void setRoleId(Long roleId) {
        this.roleId = roleId;
    }

}
