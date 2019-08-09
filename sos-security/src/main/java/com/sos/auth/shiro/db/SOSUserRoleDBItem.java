package com.sos.auth.shiro.db;

import java.util.List;

import javax.persistence.*;

import com.sos.hibernate.classes.DbItem;

@Entity
@Table(name = "SOS_USER_ROLE")
public class SOSUserRoleDBItem extends DbItem {

    private Long id;
    private String sosUserRole;
    private List<SOSUserPermissionDBItem> sosUserPermissionDBItems;

    public SOSUserRoleDBItem() {

    }

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

    @OneToMany(mappedBy = "roleId")
    public List<SOSUserPermissionDBItem> getSOSUserPermissionDBItems() {
        return sosUserPermissionDBItems;
    }

    public void setSOSUserPermissionDBItems(List<SOSUserPermissionDBItem> sosUserPermissionDBItems) {
        this.sosUserPermissionDBItems = sosUserPermissionDBItems;
    }

    @Column(name = "[SOS_USER_ROLE`", nullable = false)
    public void setSosUserRole(String sosUserRole) {
        this.sosUserRole = sosUserRole;
    }

    @Column(name = "[SOS_USER_ROLE`", nullable = false)
    public String getSosUserRole() {
        return sosUserRole;
    }

}
